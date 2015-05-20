/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.command;

import com.openiot.spi.command.CommandResult;
import com.openiot.spi.command.ICommandResponse;

/**
 * Model object for a command response.
 * 
 * @author dadams
 */
public class CommandResponse implements ICommandResponse {

	/** Command result */
	private CommandResult result;

	/** Detail message */
	private String message;

	public CommandResponse() {
	}

	public CommandResponse(CommandResult result, String message) {
		this.result = result;
		this.message = message;
	}

	public CommandResult getResult() {
		return result;
	}

	public void setResult(CommandResult result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Copy an SPI object to one that can marshaled.
	 * 
	 * @param input
	 * @return
	 */
	public static CommandResponse copy(ICommandResponse input) {
		CommandResponse response = new CommandResponse();
		response.setMessage(input.getMessage());
		response.setResult(input.getResult());
		return response;
	}
}