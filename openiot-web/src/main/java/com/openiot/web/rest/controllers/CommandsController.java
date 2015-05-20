/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.web.rest.controllers;

import com.openiot.OpenIoT;
import com.openiot.Tracer;
import com.openiot.core.user.SitewhereRoles;
import com.openiot.rest.model.device.request.DeviceCommandCreateRequest;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.server.debug.TracerCategory;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * Controller for device command operations.
 * 
 * @author Derek Adams
 */
@Controller
@RequestMapping(value = "/commands")
@Api(value = "commands", description = "Operations related to OpenIoT device commands.")
public class CommandsController extends OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(CommandsController.class);

	/**
	 * Update an existing device command.
	 * 
	 * @param token
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}", method = RequestMethod.PUT)
	@ResponseBody
	@ApiOperation(value = "Update device command information")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceCommand updateDeviceCommand(
			@ApiParam(value = "Token", required = true) @PathVariable String token,
			@RequestBody DeviceCommandCreateRequest request) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "updateDeviceCommand", LOGGER);
		try {
			return OpenIoT.getServer().getDeviceManagement().updateDeviceCommand(token, request);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get a device command by unique token.
	 * 
	 * @param hardwareId
	 * @return
	 */
	@RequestMapping(value = "/{token}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a device command by unique token")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceCommand getDeviceCommandByToken(
			@ApiParam(value = "Token", required = true) @PathVariable String token) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getDeviceCommandByToken", LOGGER);
		try {
			return assertDeviceCommandByToken(token);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Delete an existing device command.
	 * 
	 * @param token
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}", method = RequestMethod.DELETE)
	@ResponseBody
	@ApiOperation(value = "Delete a device command based on token")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceCommand deleteDeviceCommand(
			@ApiParam(value = "Token", required = true) @PathVariable String token,
			@ApiParam(value = "Delete permanently", required = false) @RequestParam(defaultValue = "false") boolean force)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "deleteDeviceCommand", LOGGER);
		try {
			return OpenIoT.getServer().getDeviceManagement().deleteDeviceCommand(token, force);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Gets a device command by token and throws an exception if not found.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected IDeviceCommand assertDeviceCommandByToken(String token) throws OpenIoTException {
		IDeviceCommand result = OpenIoT.getServer().getDeviceManagement().getDeviceCommandByToken(token);
		if (result == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceCommandToken, ErrorLevel.ERROR,
					HttpServletResponse.SC_NOT_FOUND);
		}
		return result;
	}
}