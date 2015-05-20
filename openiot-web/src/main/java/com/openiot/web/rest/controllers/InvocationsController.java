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
import com.openiot.device.marshaling.DeviceCommandInvocationMarshalHelper;
import com.openiot.rest.model.device.event.DeviceCommandInvocation;
import com.openiot.rest.model.device.event.view.DeviceCommandInvocationSummary;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.IDeviceCommandInvocation;
import com.openiot.spi.device.event.IDeviceCommandResponse;
import com.openiot.spi.device.event.IDeviceEvent;
import com.openiot.spi.search.ISearchResults;
import com.openiot.spi.server.debug.TracerCategory;
import com.openiot.web.rest.view.DeviceInvocationSummaryBuilder;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for command invocation operations.
 * 
 * @author Derek Adams
 */
@Controller
@RequestMapping(value = "/invocations")
@Api(value = "invocations", description = "Operations related to OpenIoT command invocations.")
public class InvocationsController extends OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(InvocationsController.class);

	/**
	 * Get a command invocation by unique id.
	 * 
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get device command invocation by unique id.")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceCommandInvocation getDeviceCommandInvocation(
			@ApiParam(value = "Unique id", required = true) @PathVariable String id)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getDeviceCommandInvocation", LOGGER);
		try {
			IDeviceEvent found = OpenIoT.getServer().getDeviceManagement().getDeviceEventById(id);
			if (!(found instanceof IDeviceCommandInvocation)) {
				throw new OpenIoTException("Event with the corresponding id is not a command invocation.");
			}
			DeviceCommandInvocationMarshalHelper helper = new DeviceCommandInvocationMarshalHelper();
			return helper.convert((IDeviceCommandInvocation) found);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get a summarized version of the given device command invocation.
	 * 
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{id}/summary", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get device command invocation summary by unique id.")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public DeviceCommandInvocationSummary getDeviceCommandInvocationSummary(
			@ApiParam(value = "Unique id", required = true) @PathVariable String id)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getDeviceCommandInvocationSummary", LOGGER);
		try {
			IDeviceEvent found = OpenIoT.getServer().getDeviceManagement().getDeviceEventById(id);
			if (!(found instanceof IDeviceCommandInvocation)) {
				throw new OpenIoTException("Event with the corresponding id is not a command invocation.");
			}
			IDeviceCommandInvocation invocation = (IDeviceCommandInvocation) found;
			DeviceCommandInvocationMarshalHelper helper = new DeviceCommandInvocationMarshalHelper();
			helper.setIncludeCommand(true);
			DeviceCommandInvocation converted = helper.convert(invocation);
			ISearchResults<IDeviceCommandResponse> responses =
					OpenIoT.getServer().getDeviceManagement().listDeviceCommandInvocationResponses(
							found.getId());
			return DeviceInvocationSummaryBuilder.build(converted, responses.getResults());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List all responses for a command invocation.
	 * 
	 * @param assignmentToken
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{id}/responses", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List all responses for a device command invocation.")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceCommandResponse> listCommandInvocationResponses(
			@ApiParam(value = "Invocation id", required = true) @PathVariable String id)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listCommandInvocationResponses", LOGGER);
		try {
			return OpenIoT.getServer().getDeviceManagement().listDeviceCommandInvocationResponses(id);
		} finally {
			Tracer.stop(LOGGER);
		}
	}
}