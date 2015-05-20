/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.search.external;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.IDeviceEvent;
import com.openiot.spi.device.event.IDeviceLocation;
import com.openiot.spi.search.IDateRangeSearchCriteria;

import java.util.List;

/**
 * Search provider that provides information about OpenIoT device events.
 * 
 * @author Derek
 */
public interface IDeviceEventSearchProvider extends ISearchProvider {

	/**
	 * Executes an arbitrary event query against the search provider.
	 * 
	 * @param query
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<IDeviceEvent> executeQuery(String query) throws OpenIoTException;

	/**
	 * Get a list of device locations near the given lat/long in the given time period.
	 * 
	 * @param latitude
	 * @param longitude
	 * @param distance
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<IDeviceLocation> getLocationsNear(double latitude, double longitude, double distance,
			IDateRangeSearchCriteria criteria) throws OpenIoTException;
}