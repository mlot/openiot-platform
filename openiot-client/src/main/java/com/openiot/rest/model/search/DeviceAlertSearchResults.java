/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.search;

import com.openiot.rest.model.device.event.DeviceAlert;

import java.util.ArrayList;
import java.util.List;

/**
 * Search results that contain device alerts.
 * 
 * @author dadams
 */
public class DeviceAlertSearchResults extends SearchResults<DeviceAlert> {

	public DeviceAlertSearchResults() {
		super(new ArrayList<DeviceAlert>());
	}

	public DeviceAlertSearchResults(List<DeviceAlert> results) {
		super(results);
	}
}