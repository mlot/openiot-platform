/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.server.device;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDeviceManagement;
import com.openiot.spi.server.IModelInitializer;

/**
 * Class that initializes the device model with data needed to bootstrap the system.
 * 
 * @author Derek
 */
public interface IDeviceModelInitializer extends IModelInitializer {

	/**
	 * Initialize the device model.
	 * 
	 * @param deviceManagement
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void initialize(IDeviceManagement deviceManagement) throws OpenIoTException;
}