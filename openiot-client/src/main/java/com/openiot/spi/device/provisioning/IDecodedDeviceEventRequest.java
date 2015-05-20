/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.provisioning;

import com.openiot.spi.device.event.request.IDeviceEventCreateRequest;

/**
 * Contains information decoded by an {@link IDeviceEventDecoder} includng the decoded
 * event and information about its context.
 * 
 * @author Derek
 */
public interface IDecodedDeviceEventRequest {

	/**
	 * Get hardware id the request pertains to.
	 * 
	 * @return
	 */
	public String getHardwareId();

	/**
	 * Get event originator if available.
	 * 
	 * @return
	 */
	public String getOriginator();

	/**
	 * Get event create request.
	 * 
	 * @return
	 */
	public IDeviceEventCreateRequest getRequest();
}