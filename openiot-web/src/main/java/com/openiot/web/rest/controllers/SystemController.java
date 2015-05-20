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
import com.openiot.spi.server.debug.TracerCategory;
import com.openiot.spi.system.IVersion;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for system operations.
 * 
 * @author Derek Adams
 */
@Controller
@RequestMapping(value = "/system")
@Api(value = "system", description = "Operations related to OpenIoT CE system management.")
public class SystemController extends OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(SystemController.class);

	@RequestMapping(value = "/version", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get version information")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IVersion getVersion() throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getVersion", LOGGER);
		try {
			return OpenIoT.getServer().getVersion();
		} finally {
			Tracer.stop(LOGGER);
		}
	}
}