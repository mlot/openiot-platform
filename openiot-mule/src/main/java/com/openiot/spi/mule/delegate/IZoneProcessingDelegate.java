/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.mule.delegate;

import com.openiot.spi.IOpenIoTContext;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.request.IDeviceAlertCreateRequest;
import com.openiot.spi.geospatial.IZoneMatcher;

import java.util.List;

/**
 * Delegate that receives callbacks for zone processing so that developers can specify
 * responses to locations inside/outside of a given zone.
 * 
 * @author Derek Adams
 */
public interface IZoneProcessingDelegate {

	/**
	 * Called by zone check code to delegate handling of client-specific zone logic.
	 * 
	 * @param context
	 * @param matcher
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<IDeviceAlertCreateRequest> handleZoneResults(IOpenIoTContext context, IZoneMatcher matcher)
			throws OpenIoTException;
}