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
 * Encodes an {@link IDeviceCommandExecution} into a format that can be transmitted.
 * 
 * @author Derek
 * 
 * @param <T>
 *            format for encoded command. Must be compatible with the
 *            {@link ICommandDeliveryProvider} that will deliver the command.
 */
public interface ICommandExecutionEncoder<T> extends ILifecycleComponent {

	/**
	 * Encodes a command execution.
	 * 
	 * @param command
	 * @param nested
	 * @param assignment
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public T encode(IDeviceCommandExecution command, IDeviceNestingContext nested,
			IDeviceAssignment assignment) throws OpenIoTException;

	/**
	 * Encodes a OpenIoT system command.
	 * 
	 * @param command
	 * @param nested
	 * @param assignment
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public T encodeSystemCommand(ISystemCommand command, IDeviceNestingContext nested,
			IDeviceAssignment assignment) throws OpenIoTException;
}