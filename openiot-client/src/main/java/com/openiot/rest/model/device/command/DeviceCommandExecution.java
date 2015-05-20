/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.command;

import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.command.IDeviceCommandExecution;
import com.openiot.spi.device.event.IDeviceCommandInvocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the {@link IDeviceCommandExecution} interface.
 * 
 * @author Derek
 */
public class DeviceCommandExecution implements IDeviceCommandExecution {

	/** Command being executed */
	private IDeviceCommand command;

	/** Command invocation details */
	private IDeviceCommandInvocation invocation;

	/** Map of parameter names to values calculated from invocation */
	private Map<String, Object> parameters = new HashMap<String, Object>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandExecution#getCommand()
	 */
	public IDeviceCommand getCommand() {
		return command;
	}

	public void setCommand(IDeviceCommand command) {
		this.command = command;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandExecution#getInvocation()
	 */
	public IDeviceCommandInvocation getInvocation() {
		return invocation;
	}

	public void setInvocation(IDeviceCommandInvocation invocation) {
		this.invocation = invocation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandExecution#getParameters()
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
}