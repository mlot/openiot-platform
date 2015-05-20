/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.provisioning;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.command.IDeviceCommandExecution;
import com.openiot.spi.device.event.IDeviceCommandInvocation;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;

/**
 * Used to build an {@link IDeviceCommandExecution} from an {@link IDeviceCommand} and a
 * {@link IDeviceCommandInvocation}.
 * 
 * @author Derek
 */
public interface ICommandExecutionBuilder extends ILifecycleComponent {

	/**
	 * Create an execution from a command and invocation details.
	 * 
	 * @param command
	 * @param invocation
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceCommandExecution createExecution(IDeviceCommand command, IDeviceCommandInvocation invocation)
			throws OpenIoTException;
}