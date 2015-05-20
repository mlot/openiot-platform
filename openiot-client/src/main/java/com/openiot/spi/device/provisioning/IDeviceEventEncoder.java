/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.provisioning;

import com.openiot.spi.OpenIoTException;

/**
 * Encodes a device event into another representation.
 * 
 * @author Derek
 *
 * @param <T>
 */
public interface IDeviceEventEncoder<T> {

	/**
	 * Encode an {@link IDecodedDeviceEventRequest} into another representation.
	 * 
	 * @param event
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public T encode(IDecodedDeviceEventRequest request) throws OpenIoTException;
}