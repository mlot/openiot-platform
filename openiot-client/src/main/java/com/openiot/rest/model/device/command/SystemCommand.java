/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.command;

import com.openiot.spi.device.command.ISystemCommand;
import com.openiot.spi.device.command.SystemCommandType;

/**
 * Base class for system command implementations.
 * 
 * @author Derek
 */
public class SystemCommand implements ISystemCommand {

	/** Command type */
	private SystemCommandType type;

	public SystemCommand(SystemCommandType type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ISystemCommand#getType()
	 */
	public SystemCommandType getType() {
		return type;
	}

	public void setType(SystemCommandType type) {
		this.type = type;
	}
}