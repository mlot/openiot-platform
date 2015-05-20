/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.provisioning;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.request.IDeviceRegistrationRequest;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;

/**
 * Manages how devices are dynamically registered into the system.
 * 
 * @author Derek
 */
public interface IRegistrationManager extends ILifecycleComponent {

	/**
	 * Indicates whether the registration manager allows new devices to be created as a
	 * byproduct of the registration process.
	 * 
	 * @return
	 */
	public boolean isAllowNewDevices();

	/**
	 * Indicates whether all new regsitrations can be automatically assigned to a given
	 * site. If not, the site token must be passed as part of the registration payload.
	 * 
	 * @return
	 */
	public boolean isAutoAssignSite();

	/**
	 * Gets the token used for automatic site assignment. This only applies when 'auto
	 * assign site' is set to true.
	 * 
	 * @return
	 */
	public String getAutoAssignSiteToken();

	/**
	 * Handle registration of a new device.
	 * 
	 * @param request
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void handleDeviceRegistration(IDeviceRegistrationRequest request) throws OpenIoTException;
}