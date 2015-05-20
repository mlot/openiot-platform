/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.command;

import com.openiot.spi.device.command.IRegistrationFailureCommand;
import com.openiot.spi.device.command.RegistrationFailureReason;
import com.openiot.spi.device.command.SystemCommandType;

/**
 * Default implementation of {@link IRegistrationFailureCommand}.
 * 
 * @author Derek
 */
public class RegistrationFailureCommand extends SystemCommand implements IRegistrationFailureCommand {

	/** Failure reason */
	private RegistrationFailureReason reason;

	/** Error message */
	private String errorMessage;

	public RegistrationFailureCommand() {
		super(SystemCommandType.RegistrationFailure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IRegistrationFailureCommand#getReason()
	 */
	public RegistrationFailureReason getReason() {
		return reason;
	}

	public void setReason(RegistrationFailureReason reason) {
		this.reason = reason;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IRegistrationFailureCommand#getErrorMessage()
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}