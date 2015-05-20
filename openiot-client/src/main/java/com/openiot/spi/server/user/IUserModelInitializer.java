/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.server.user;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.server.IModelInitializer;
import com.openiot.spi.user.IUserManagement;

/**
 * Class that initializes the user model with data needed to bootstrap the system.
 * 
 * @author Derek
 */
public interface IUserModelInitializer extends IModelInitializer {

	/**
	 * Initialize the user model.
	 * 
	 * @param userManagement
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void initialize(IUserManagement userManagement) throws OpenIoTException;
}