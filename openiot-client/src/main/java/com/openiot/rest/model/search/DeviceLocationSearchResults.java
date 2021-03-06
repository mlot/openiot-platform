/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.search;

import com.openiot.rest.model.device.event.DeviceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Search results that contain device locations.
 * 
 * @author dadams
 */
public class DeviceLocationSearchResults extends SearchResults<DeviceLocation> {

	public DeviceLocationSearchResults() {
		super(new ArrayList<DeviceLocation>());
	}

	public DeviceLocationSearchResults(List<DeviceLocation> results) {
		super(results);
	}
}