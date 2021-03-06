/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.geospatial;

import com.openiot.spi.device.IZone;
import com.openiot.spi.device.event.IDeviceLocation;
import com.openiot.spi.geospatial.IZoneRelationship;
import com.openiot.spi.geospatial.ZoneContainment;

/**
 * Specifies relationship between a location and a zone.
 * 
 * @author Derek
 */
public class ZoneRelationship implements IZoneRelationship {

	/** Location */
	private IDeviceLocation location;

	/** Zone */
	private IZone zone;

	/** Containment */
	private ZoneContainment containment;

	public ZoneRelationship(IDeviceLocation location, IZone zone, ZoneContainment containment) {
		this.location = location;
		this.zone = zone;
		this.containment = containment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.geo.IZoneRelationship#getLocation()
	 */
	public IDeviceLocation getLocation() {
		return location;
	}

	public void setLocation(IDeviceLocation location) {
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.geo.IZoneRelationship#getZone()
	 */
	public IZone getZone() {
		return zone;
	}

	public void setZone(IZone zone) {
		this.zone = zone;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.geo.IZoneRelationship#getContainment()
	 */
	public ZoneContainment getContainment() {
		return containment;
	}

	public void setContainment(ZoneContainment containment) {
		this.containment = containment;
	}
}