/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.provisioning;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.IDeviceNestingContext;
import com.openiot.spi.device.command.IDeviceCommandExecution;
import com.openiot.spi.device.command.ISystemCommand;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;

import java.util.List;

/**
 * Routes commands to one or more {@link ICommandDestination} implementations.
 * 
 * @author Derek
 */
public interface IOutboundCommandRouter extends ILifecycleComponent {

	/**
	 * Initialize the router with destination information.
	 * 
	 * @param destinations
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void initialize(List<ICommandDestination<?, ?>> destinations) throws OpenIoTException;

	/**
	 * Route a command to one of the available destinations.
	 * 
	 * @param execution
	 * @param nesting
	 * @param assignment
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void routeCommand(IDeviceCommandExecution execution, IDeviceNestingContext nesting,
			IDeviceAssignment assignment) throws OpenIoTException;

	/**
	 * Route a system command to one of the available destinations.
	 * 
	 * @param command
	 * @param nesting
	 * @param assignment
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void routeSystemCommand(ISystemCommand command, IDeviceNestingContext nesting,
			IDeviceAssignment assignment) throws OpenIoTException;
}