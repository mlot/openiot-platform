/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.provisioning;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.command.ISystemCommand;
import com.openiot.spi.device.event.IDeviceCommandInvocation;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;

/**
 * Defines the flow executed for processing a command for delivery.
 * 
 * @author Derek
 */
public interface ICommandProcessingStrategy extends ILifecycleComponent {

	/**
	 * Get the {@link ICommandTargetResolver} implementation.
	 * 
	 * @return
	 */
	public ICommandTargetResolver getCommandTargetResolver();

	/**
	 * Send a command using the given provisioning implementation.
	 * 
	 * @param provisioning
	 * @param invocation
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void deliverCommand(IDeviceProvisioning provisioning, IDeviceCommandInvocation invocation)
			throws OpenIoTException;

	/**
	 * Delivers a system command using the given provisioning implementation.
	 * 
	 * @param provisioning
	 * @param hardwareId
	 * @param command
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void deliverSystemCommand(IDeviceProvisioning provisioning, String hardwareId,
			ISystemCommand command) throws OpenIoTException;
}