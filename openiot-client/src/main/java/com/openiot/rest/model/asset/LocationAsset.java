/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.asset;

import com.openiot.spi.asset.AssetType;
import com.openiot.spi.asset.ILocationAsset;

/**
 * Default implementation of {@link ILocationAsset}.
 * 
 * @author Derek
 */
public class LocationAsset extends Asset implements ILocationAsset {

	/** Latitude */
	private Double latitude;

	/** Longitude */
	private Double longitude;

	/** Elevation */
	private Double elevation;

	public LocationAsset() {
		setType(AssetType.Location);
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getElevation() {
		return elevation;
	}

	public void setElevation(Double elevation) {
		this.elevation = elevation;
	}
}