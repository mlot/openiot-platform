/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

/*
 * OpenIoT extensions for Leaflet maps.
 */
L.Map.OpenIoT = L.Map.extend({
	
	statics: {
		MAP_TYPE_MAPQUEST: "mapquest",
		MAP_TYPE_GEOSERVER: "geoserver",
	},

	options: {
		siteWhereApi: 'http://localhost:8080/sitewhere/api/',
		siteToken: null,
		showZones: true,
		onZonesLoaded: null,
        showSiteMarker: true
	},
	
	/** Initialize components */
	initialize: function(id, options) {
		L.setOptions(this, options);
		L.Map.prototype.initialize.call(this, id, options);
        
		// Error if no site token specified.
		if (!this.options.siteToken) {
			this._handleNoSiteToken();
		} else {
			this.refresh();
		}
	},
	
	/** Refresh site information */
	refresh: function() {
		var self = this;
		var url = this.options.siteWhereApi + 'sites/' + this.options.siteToken;
		L.OpenIoT.Util.getJSON(url,
				function(site, status, jqXHR) { self._onSiteLoaded(site); }, 
				function(jqXHR, textStatus, errorThrown) { self._onSiteFailed(jqXHR, textStatus, errorThrown); }
		);
	},
	
	/** Called when site data has been loaded successfully */
	_onSiteLoaded: function(site) {
		var mapInfo = site.map.metadata;
		var latitude = (mapInfo.centerLatitude ? mapInfo.centerLatitude : 39.9853);
		var longitude = (mapInfo.centerLongitude ? mapInfo.centerLongitude : -104.6688);
		var zoomLevel = (mapInfo.zoomLevel ? mapInfo.zoomLevel : 10);
		L.Map.prototype.setView.call(this, [latitude, longitude], zoomLevel);
		this._loadMapTileLayer(site, mapInfo);
		if (this.options.showZones) {
			var zones = L.FeatureGroup.OpenIoT.zones({
				siteWhereApi: this.options.siteWhereApi,
				siteToken: this.options.siteToken,
				onZonesLoaded: this.options.onZonesLoaded,
			});
			this.addLayer(zones);
		}
        if (this.options.showSiteMarker) {
            var message =  site.name + ":<br>" + site.description;
            var marker = L.marker([latitude, longitude]).bindPopup(message);
            this.addLayer(marker);
        }
	},
	
	/** Loads a TileLayer based on map type and metadata associated with site */
	_loadMapTileLayer: function(site, mapInfo) {
		if (site.map.type == L.Map.OpenIoT.MAP_TYPE_MAPQUEST) {
			var mapquestUrl = 'http://{s}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png';
			var subDomains = ['otile1','otile2','otile3','otile4'];
			var mapquestAttrib = 'MapQuest data';
			var mapquest = new L.TileLayer(mapquestUrl, {maxZoom: 18, attribution: mapquestAttrib, subdomains: subDomains});		
			mapquest.addTo(this);
		} else if (site.map.type == L.Map.OpenIoT.MAP_TYPE_GEOSERVER) {
			var gsBaseUrl = (mapInfo.geoserverBaseUrl ? mapInfo.geoserverBaseUrl : "http://localhost:8080/geoserver/");
			var gsRelativeUrl = "geoserver/gwc/service/gmaps?layers=";
			var gsLayerName = (mapInfo.geoserverLayerName ? mapInfo.geoserverLayerName : "tiger:tiger_roads");
			var gsParams = "&zoom={z}&x={x}&y={y}&format=image/png";
			var gsUrl = gsBaseUrl + gsRelativeUrl + gsLayerName + gsParams;
			var geoserver = new L.TileLayer(gsUrl, {maxZoom: 18});		
			geoserver.addTo(this);
		}
	},
	
	/** Called when site data load fails */
	_onSiteFailed: function(jqXHR, textStatus, errorThrown) {
		alert('Site load failed! ' + errorThrown);
	},
	
	/** Handle error condition if no site token was specified */
	_handleNoSiteToken: function() {
		alert('No site token.');
	}
});

L.Map.siteWhere = function (id, options) {
    return new L.Map.OpenIoT(id, options);
};

/*
 * Container for OpenIoT feature groups.
 */
L.FeatureGroup.OpenIoT = {};

/*
 * Feature group for OpenIoT zones.
 */
L.FeatureGroup.OpenIoT.Zones = L.FeatureGroup.extend({

	options: {
		siteWhereApi: 'http://localhost:8080/sitewhere/api/',
		siteToken: null,
		onZonesLoaded: null,
		zoneTokenToSkip: null,
	},
	
	initialize: function(options) {
		L.setOptions(this, options);
		L.FeatureGroup.prototype.initialize.call(this);
        
		// Error if no site token specified.
		if (!this.options.siteToken) {
			this._handleNoSiteToken();
		} else {
			this.refresh();
		}
	},
	
	/** Refresh zones information */
	refresh: function() {
		var self = this;
		var url = this.options.siteWhereApi + 'sites/' + this.options.siteToken + "/zones";
		L.OpenIoT.Util.getJSON(url,
				function(zones, status, jqXHR) { self._onZonesLoaded(zones); }, 
				function(jqXHR, textStatus, errorThrown) { self._onZonesFailed(jqXHR, textStatus, errorThrown); }
		);
	},
	
	/** Called when zones data has been loaded successfully */
	_onZonesLoaded: function(zones) {
		var zone, results = zones.results;
		var polygon;
		
		// Add newest last.
		results.reverse();
		
		// Add a polygon layer for each zone.
		for (var zoneIndex = 0; zoneIndex < results.length; zoneIndex++) {
			zone = results[zoneIndex];
			if (zone.token != this.options.zoneTokenToSkip) {
				polygon = this._createPolygonForZone(zone);
				this.addLayer(polygon);
			}
		}
		
		// Callback for actions taken after zones are loaded.
		if (this.options.onZonesLoaded != null) {
			this.options.onZonesLoaded();
		}
	},
	
	/** Create a polygon layer based on zone information */
	_createPolygonForZone: function(zone) {
		var coords = zone.coordinates;
		var latLngs = [];
		for (var coordIndex = 0; coordIndex < coords.length; coordIndex++) {
			coordinate = coords[coordIndex];
			latLngs.push(new L.LatLng(coordinate.latitude, coordinate.longitude));
		}
		var polygon = new L.Polygon(latLngs, {
			"color": zone.borderColor, "opacity": 1, weight: 3,
			"fillColor": zone.fillColor, "fillOpacity": zone.opacity,
			"clickable": false});
		return polygon;
	},
	
	/** Called when zones data load fails */
	_onZonesFailed: function(jqXHR, textStatus, errorThrown) {
		alert('Zones load failed! ' + errorThrown);
	},
	
	/** Handle error condition if no site token was specified */
	_handleNoSiteToken: function() {
		alert('No site token.');
	},
});

L.FeatureGroup.OpenIoT.zones = function (options) {
	return new L.FeatureGroup.OpenIoT.Zones(options);
};


/*
 * Feature group for recent locations associated with an assignment.
 */
L.FeatureGroup.OpenIoT.AssignmentLocations = L.FeatureGroup.extend({

	options: {
		// Data options.
		siteWhereApi: 'http://localhost:8080/sitewhere/api/',
		assignmentToken: null,
		maxResults: 30,
		
		// Line rendering options (see L.Path).
		showLine: true,
		lineOptions: {
            stroke: true,
            color: '#005599',
            weight: 5,
            opacity: 0.5,
		},
		
		// Marker rendering options.
		showMarkers: true,
		
		// Event callbacks.
		onLocationsLoaded: null,
		onError: null,
	},
	
	initialize: function(options) {
		L.setOptions(this, options);
		L.FeatureGroup.prototype.initialize.call(this);
        
		// Error if no assignment token specified.
		if (!this.options.assignmentToken) {
			this._handleNoAssignmentToken();
		} else {
			this.refresh();
		}
	},
	
	/** Refresh zones information */
	refresh: function() {
		var self = this;
		var url = this.options.siteWhereApi + 'assignments/' + this.options.assignmentToken + '/locations';
		L.OpenIoT.Util.getJSON(url,
			function(locations, status, jqXHR) { 
				self._onLocationsLoaded(locations); }, 
			function(jqXHR, textStatus, errorThrown) { 
				self._onLocationsFailed(jqXHR, textStatus, errorThrown); }
		);
	},
	
	/** Pan map to most recent location */
	panToLastLocation: function(map) {
		if (this.lastLocation) {
    		map.panTo(this.lastLocation);
		}
	},
	
	/** LatLng for last location in list */
	lastLocation: null,
	
	/** Called when location data has been loaded successfully */
	_onLocationsLoaded: function(locations) {
    	this.clearLayers();
    	this.lastLocation = null;
    	
		var location, results = locations.results;
		var marker;
		
		// Add newest last.
		results.reverse();
		
		// Add a marker for each location.
    	var latLngs = [];
    	var latLng;
		for (var locIndex = 0; locIndex < results.length; locIndex++) {
			location = results[locIndex];
			if (this.options.showMarkers) {
				marker = this._createMarkerForLocation(location);
				this.addLayer(marker);
			}
			latLng = new L.LatLng(location.latitude, location.longitude);
    		latLngs.push(latLng);
    		this.lastLocation = latLng;
		}
    	if ((latLngs.length > 0) && (this.options.showLine)) {
    		this._createLineForLocations(this, latLngs);
    	}
		
		// Callback for actions taken after locations are loaded.
		if (this.options.onLocationsLoaded != null) {
			this.options.onLocationsLoaded();
		}
	},
	
	/** Create a marker for the given location */
	_createMarkerForLocation: function(location) {
		return L.marker([location.latitude, location.longitude]).bindPopup(location.assetName);
	},
	
	/** Create a line that connects the locations */
	_createLineForLocations: function(layer, latLngs) {
		var line = L.polyline(latLngs, this.options.lineOptions);
		layer.addLayer(line);	
	},
	
	/** Called when location data load fails */
	_onLocationsFailed: function(jqXHR, textStatus, errorThrown) {
		this._handleError('Locations load failed. ' + errorThrown);
	},
	
	/** Handle error condition if no assignment token was specified */
	_handleNoAssignmentToken: function() {
		this._handleError('No assignment token specified.');
	},
	
	/** Handle error in processing */
	_handleError: function(message) {
		if (this.options.onError != null) {
            this.options.onError(message);
		}
	}
});

L.FeatureGroup.OpenIoT.assignmentLocations = function (options) {
	return new L.FeatureGroup.OpenIoT.AssignmentLocations(options);
};


L.FeatureGroup.OpenIoT.AssignmentsLocationInSite = L.FeatureGroup.extend({

    options: {
        // Data source.
        dataSource: null,

        // Data options.
        siteWhereApi: 'http://localhost:8080/sitewhere/api/',
        siteToken: null,
        maxResults: 100,

        // Marker rendering options.
        showMarkers: true,

        // Measurement rendering options.
        showMeasurements: true,

        // Event callbacks.
        onAssignmentsLoaded: null,
        onError: null
    },

    initialize: function(options) {
        L.setOptions(this, options);
        L.FeatureGroup.prototype.initialize.call(this);

        // Error if no assignment token specified.
        if (!this.options.siteToken && !this.options.dataSource) {
            this._handleNoSiteToken();
        } else {
            this.refresh();
        }
    },

    refreshView: function() {
        var self = this;
        var view = self.options.dataSource.view();
        self._onAssignmentsLoaded(view);
    },

    /** Refresh zones information */
    refresh: function() {
        var self = this;
        if (self.options.dataSource){
            self.options.dataSource.fetch(function(){
                var view = self.options.dataSource.view();
                self._onAssignmentsLoaded(view);
            });

        } else {
            var url = this.options.siteWhereApi + 'sites/' + this.options.siteToken + '/assignments?includeDevice=true&includeAsset=true';
            L.OpenIoT.Util.getJSON(url,
                function(assignments, status, jqXHR) {
                    self._onAssignmentsLoaded(assignments.results); },
                function(jqXHR, textStatus, errorThrown) {
                    self._onAssignmentsFailed(jqXHR, textStatus, errorThrown); }
            );
        }
    },

    /** Pan map to most recent location */
    panToLastLocation: function(map) {
        if (this.lastLocation) {
            map.panTo(this.lastLocation);
        }
    },

    /** LatLng for last location in list */
    lastLocation: null,

    /** Called when location data has been loaded successfully */
    _onAssignmentsLoaded: function(assignments) {
        this.clearLayers();

        var location, assignmentData;
        var marker;

        // Add a marker for each location.
        var latLngs = [];
        var latLng;
        for (var locIndex = 0; locIndex < assignments.length; locIndex++) {
            if (assignments[locIndex].state != null && assignments[locIndex].state.lastLocation != null){
                location = assignments[locIndex].state.lastLocation;
                assignmentData = assignments[locIndex];
                if (this.options.showMarkers) {
                    marker = this._createMarkerForLocation(location, assignmentData);
                    this.addLayer(marker);
                }
                latLng = new L.LatLng(location.latitude, location.longitude);
                latLngs.push(latLng);
                this.lastLocation = latLng;
            }
            //todo add default location here for devices without last location data.

        }

        // Callback for actions taken after locations are loaded.
        if (this.options.onAssignmentsLoaded != null) {
            this.options.onAssignmentsLoaded();
        }
    },

    /** Create a marker for the given location */
    _createMarkerForLocation: function(location, asData) {
        var self = this;
        if (asData.status != "Active"){return;}

        var imageUrl, message;

        if (asData.associatedPerson) {
            imageUrl = asData.associatedPerson.imageUrl;
            message = asData.associatedPerson.name + "<br>Email:" + asData.associatedPerson.emailAddress;
        } else if (asData.associatedHardware) {
            imageUrl = asData.associatedHardware.imageUrl;
            message = asData.associatedHardware.name + "<br>SKU:" + asData.associatedHardware.sku;
        } else if (asData.associatedLocation) {
            imageUrl = asData.associatedLocation.imageUrl;
            message = asData.associatedLocation.name;
        } else if ((asData.assignmentType == 'Unassociated') && (asData.device)) {
            imageUrl = asData.device.assetImageUrl;
            message = asData.device.assetName + "<br>ID:" + asData.deviceHardwareId;
        }


        var myIcon = L.icon({
            iconUrl: imageUrl,
            iconSize: [30, 30],
            iconAnchor: [22, 94],
            popupAnchor: [-3, -76]
        });

        var map = null;
        var contents = {};
        contents["message"] = message;

        if (this.options.showMeasurements){
            map = {};
            contents["measurements"] = map;
            var divId = "m"+ asData.token;
            contents["divId"] = divId;
            message = message + "<br>" + "<div id='"+ divId +"' />";
            var key, mEntry;
            var url = this.options.siteWhereApi + 'assignments/' + asData.token + '/measurements?page=1&pageSize=20';
            L.OpenIoT.Util.getJSON(url,
                function(measurements, status, jqXHR) {
                    var m = measurements.results;
                    for (var index = 0; index < m.length; index++) {
                        mEntry = m[index].measurements;
                        //console.log(mEntry);
                        //keys = Object.keys(mEntry);
                        for (key in mEntry) {
                            //console.log("key is:"+ key);
                            if (map.hasOwnProperty(key)) {
                                map[key].push(mEntry[key]);
                            } else {
                                map[key] = [];
                                map[key].push(mEntry[key]);
                            }
                        }
                    }
                    //latest measurement last
                    for (var k in map) {
                        map[k].reverse();
                        console.log(map[k]);
                    }
                },
                function(jqXHR, textStatus, errorThrown) {
                    this._handleError('Measurements load failed. ' + errorThrown); }
            )
        }

        var popup = L.popup().setContent(message);
        popup["contents"] = contents;
        var marker = L.marker([location.latitude, location.longitude], {icon: myIcon}).bindPopup(popup);
        //marker["contents"] = contents;

        return marker;
        //return marker.bindPopup(message);
        //return L.marker([location.latitude, location.longitude], {icon: myIcon}).bindPopup(message);

    },

    addListener: function(map){
        map.on("popupopen", function(e){
            var p = e.popup;
            //console.log("popup is " + p);
            var contents = p["contents"];
            //console.log("contents is " + contents);
            var measurements = contents["measurements"];
            if (measurements){
                var divId = contents["divId"];
                var containerDiv = $("#" + divId);
                //console.log("divId is: #"+divId);
                for (var key in measurements){
                    var sparkItem = $("<span id=''>&nbsp;</span>");
                    var nameItem = $("<span>"+key+":</span><br>");
                    var lineItem = $("<div></div>");
                    var valueArray = measurements[key];
                    var length = valueArray.length;
                    var latestValue = $("<span>" + valueArray[length-1] + "</span>" );
                    lineItem.append(nameItem);
                    lineItem.append(sparkItem);
                    lineItem.append(latestValue);
                    containerDiv.append(lineItem);
                    sparkItem.sparkline(valueArray, {
                        type: 'line',
                        width: '100',
                        height: '30',
                        lineColor: '#cd151e',
                        lineWidth: 2
                    });
                }
                $.sparkline_display_visible();
            }
        });
    },


    /** Called when location data load fails */
    _onAssignmentsFailed: function(jqXHR, textStatus, errorThrown) {
        this._handleError('Locations load failed. ' + errorThrown);
    },

    /** Handle error condition if no assignment token was specified */
    _handleNoSiteToken: function() {
        this._handleError('No site token specified.');
    },

    /** Handle error in processing */
    _handleError: function(message) {
        if (this.options.onError != null) {
            this.options.onError(message);
        }
    }
});


L.FeatureGroup.OpenIoT.assignmentsLocationInSite = function (options) {
    return new L.FeatureGroup.OpenIoT.AssignmentsLocationInSite(options);
};


/*
 * Container for OpenIoT classes.
 */
L.OpenIoT = {};

/*
 * OpenIoT utility functions.
 */
L.OpenIoT.Util = L.Class.extend({
	
	statics: {
		
		/** Make a JSONP GET request */
		getJSON: function(url, onSuccess, onFail) {
			return jQuery.ajax({
				'type' : 'GET',
				'dataType': 'jsonp',
				'url' : url,
				'contentType' : 'application/json',
				'success' : onSuccess,
				'error' : onFail
			});
		},
	},
})