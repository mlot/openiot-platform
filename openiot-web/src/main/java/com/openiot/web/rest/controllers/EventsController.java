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
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.IDeviceEvent;
import com.openiot.spi.server.debug.TracerCategory;
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
 * Controller for event operations.
 * 
 * @author Derek Adams
 */
@Controller
@RequestMapping(value = "/events")
@Api(value = "events", description = "Operations related to OpenIoT device events.")
public class EventsController extends OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(EventsController.class);

	/**
	 * Used by AJAX calls to find an event by unique id.
	 * 
	 * @param eventId
	 * @return
	 */
	@RequestMapping(value = "/{eventId}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get an event by unique id.")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceEvent getEventById(
			@ApiParam(value = "Event id", required = true) @PathVariable String eventId)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getEventById", LOGGER);
		try {
			return OpenIoT.getServer().getDeviceManagement().getDeviceEventById(eventId);
		} finally {
			Tracer.stop(LOGGER);
		}
	}
}