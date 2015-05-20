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

/**
 * Delivers commands to devices by encoding the commands, finding the list of target
 * devices, then using a delivery provider to send the encoded commands.
 * 
 * @author Derek
 * 
 * @param <T>
 */
public interface ICommandDestination<T, P> extends ILifecycleComponent {

	/**
	 * Get unique identifier for destination.
	 * 
	 * @return
	 */
	public String getDestinationId();

	/**
	 * Gets the configured command execution encoder.
	 * 
	 * @return
	 */
	public ICommandExecutionEncoder<T> getCommandExecutionEncoder();

	/**
	 * Get the configured command delivery parameter extractor.
	 * 
	 * @return
	 */
	public ICommandDeliveryParameterExtractor<P> getCommandDeliveryParameterExtractor();

	/**
	 * Gets the configured command delivery provider.
	 * 
	 * @return
	 */
	public ICommandDeliveryProvider<T, P> getCommandDeliveryProvider();

	/**
	 * Deliver a command.
	 * 
	 * @param execution
	 * @param nesting
	 * @param assignment
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void deliverCommand(IDeviceCommandExecution execution, IDeviceNestingContext nesting,
			IDeviceAssignment assignment) throws OpenIoTException;

	/**
	 * Deliver a system command.
	 * 
	 * @param command
	 * @param nesting
	 * @param assignment
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void deliverSystemCommand(ISystemCommand command, IDeviceNestingContext nesting,
			IDeviceAssignment assignment) throws OpenIoTException;
}