/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.search;

import com.openiot.rest.model.device.Zone;

import java.util.ArrayList;
import java.util.List;

/**
 * Search results that contain zones.
 * 
 * @author dadams
 */
public class ZoneSearchResults extends SearchResults<Zone> {

	public ZoneSearchResults() {
		super(new ArrayList<Zone>());
	}

	public ZoneSearchResults(List<Zone> results) {
		super(results);
	}
}