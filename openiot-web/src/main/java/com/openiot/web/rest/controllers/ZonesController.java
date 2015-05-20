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
import com.openiot.rest.model.device.Zone;
import com.openiot.rest.model.device.request.ZoneCreateRequest;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IZone;
import com.openiot.spi.server.debug.TracerCategory;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for site operations.
 * 
 * @author Derek Adams
 */
@Controller
@RequestMapping(value = "/zones")
@Api(value = "zones", description = "Operations related to OpenIoT zones")
public class ZonesController extends OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(ZonesController.class);

	@RequestMapping(value = "/{zoneToken}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get zone by unique token")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public Zone getZone(
			@ApiParam(value = "Unique token that identifies zone", required = true) @PathVariable String zoneToken)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getZone", LOGGER);
		try {
			IZone found = OpenIoT.getServer().getDeviceManagement().getZone(zoneToken);
			return Zone.copy(found);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Update information for a zone.
	 * 
	 * @param input
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{zoneToken}", method = RequestMethod.PUT)
	@ResponseBody
	@ApiOperation(value = "Update an existing zone")
	@Secured({ SitewhereRoles.ROLE_ADMINISTER_SITES })
	public Zone updateZone(
			@ApiParam(value = "Unique token that identifies zone", required = true) @PathVariable String zoneToken,
			@RequestBody ZoneCreateRequest request) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "updateZone", LOGGER);
		try {
			IZone zone = OpenIoT.getServer().getDeviceManagement().updateZone(zoneToken, request);
			return Zone.copy(zone);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Delete an existing zone.
	 * 
	 * @param zoneToken
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{zoneToken}", method = RequestMethod.DELETE)
	@ResponseBody
	@ApiOperation(value = "Delete zone based on unique token")
	@Secured({ SitewhereRoles.ROLE_ADMINISTER_SITES })
	public Zone deleteZone(
			@ApiParam(value = "Unique token that identifies zone", required = true) @PathVariable String zoneToken,
			@ApiParam(value = "Delete permanently", required = false) @RequestParam(defaultValue = "false") boolean force)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "deleteZone", LOGGER);
		try {
			IZone deleted = OpenIoT.getServer().getDeviceManagement().deleteZone(zoneToken, force);
			return Zone.copy(deleted);
		} finally {
			Tracer.stop(LOGGER);
		}
	}
}