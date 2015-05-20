/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.command;

import com.openiot.spi.device.command.IRegistrationAckCommand;
import com.openiot.spi.device.command.RegistrationSuccessReason;
import com.openiot.spi.device.command.SystemCommandType;

/**
 * Default implementation of {@link IRegistrationAckCommand}.
 * 
 * @author Derek
 */
public class RegistrationAckCommand extends SystemCommand implements IRegistrationAckCommand {

	/** Success reason */
	private RegistrationSuccessReason reason;

	public RegistrationAckCommand() {
		super(SystemCommandType.RegistrationAck);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IRegistrationAckCommand#getReason()
	 */
	public RegistrationSuccessReason getReason() {
		return reason;
	}

	public void setReason(RegistrationSuccessReason reason) {
		this.reason = reason;
	}
}