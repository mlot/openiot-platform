/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.geospatial;

import com.openiot.spi.common.ILocation;
import com.openiot.spi.device.IZone;
import com.openiot.spi.device.event.IDeviceLocation;
import com.vividsolutions.jts.geom.*;

import java.util.List;

/**
 * Utility functions for dealing with geographic information.
 * 
 * @author Derek
 */
public class GeoUtils {

	/**
	 * Creates a JTS point from a device location.
	 * 
	 * @param location
	 * @return
	 */
	public static Point createPointForLocation(IDeviceLocation location) {
		GeometryFactory fact = new GeometryFactory();
		return fact.createPoint(new Coordinate(location.getLongitude(), location.getLatitude()));
	}

	/**
	 * Creates a JTS polygon based on zone definition.
	 * 
	 * @param zone
	 * @return
	 */
	public static Polygon createPolygonForZone(IZone zone) {
		return createPolygonForLocations(zone.getCoordinates());
	}

	/**
	 * Create a polgon for a list of locations.
	 * 
	 * @param locations
	 * @return
	 */
	public static <T extends ILocation> Polygon createPolygonForLocations(List<T> locations) {
		Coordinate[] coords = new Coordinate[locations.size() + 1];
		for (int x = 0; x < locations.size(); x++) {
			ILocation loc = locations.get(x);
			coords[x] = new Coordinate(loc.getLongitude(), loc.getLatitude());
		}
		ILocation loc = locations.get(0);
		coords[locations.size()] = new Coordinate(loc.getLongitude(), loc.getLatitude());

		GeometryFactory fact = new GeometryFactory();
		LinearRing linear = new GeometryFactory().createLinearRing(coords);
		return new Polygon(linear, null, fact);
	}
}
