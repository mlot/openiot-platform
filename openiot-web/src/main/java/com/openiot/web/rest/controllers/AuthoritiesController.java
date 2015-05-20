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
import com.openiot.rest.model.search.SearchResults;
import com.openiot.rest.model.user.GrantedAuthority;
import com.openiot.rest.model.user.GrantedAuthoritySearchCriteria;
import com.openiot.rest.model.user.request.GrantedAuthorityCreateRequest;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.server.debug.TracerCategory;
import com.openiot.spi.user.IGrantedAuthority;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for user operations.
 * 
 * @author Derek Adams
 */
@Controller
@RequestMapping(value = "/authorities")
@Api(value = "authorities", description = "Operations related to OpenIoT authorities.")
public class AuthoritiesController extends OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(AuthoritiesController.class);

	/**
	 * Create a new authority.
	 * 
	 * @param input
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create a new authority")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public GrantedAuthority createAuthority(@RequestBody GrantedAuthorityCreateRequest input)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createAuthority", LOGGER);
		try {
			IGrantedAuthority auth = OpenIoT.getServer().getUserManagement().createGrantedAuthority(input);
			return GrantedAuthority.copy(auth);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get an authority by unique name.
	 * 
	 * @param name
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{name}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Find authority by unique name")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public GrantedAuthority getAuthorityByName(
			@ApiParam(value = "Authority name", required = true) @PathVariable String name)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getAuthorityByName", LOGGER);
		try {
			IGrantedAuthority auth =
					OpenIoT.getServer().getUserManagement().getGrantedAuthorityByName(name);
			if (auth == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidAuthority, ErrorLevel.ERROR,
						HttpServletResponse.SC_NOT_FOUND);
			}
			return GrantedAuthority.copy(auth);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List authorities that match given criteria.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List authorities that match certain criteria")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public SearchResults<GrantedAuthority> listAuthorities(
			@ApiParam(value = "Max records to return", required = false) @RequestParam(defaultValue = "100") int count)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listAuthorities", LOGGER);
		try {
			List<GrantedAuthority> authsConv = new ArrayList<GrantedAuthority>();
			GrantedAuthoritySearchCriteria criteria = new GrantedAuthoritySearchCriteria();
			List<IGrantedAuthority> auths =
					OpenIoT.getServer().getUserManagement().listGrantedAuthorities(criteria);
			for (IGrantedAuthority auth : auths) {
				authsConv.add(GrantedAuthority.copy(auth));
			}
			return new SearchResults<GrantedAuthority>(authsConv);
		} finally {
			Tracer.stop(LOGGER);
		}
	}
}