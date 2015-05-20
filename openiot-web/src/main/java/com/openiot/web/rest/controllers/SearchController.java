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
import com.openiot.rest.model.search.external.SearchProvider;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.event.IDeviceEvent;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.search.external.IDeviceEventSearchProvider;
import com.openiot.spi.search.external.ISearchProvider;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for search operations.
 * 
 * @author Derek
 */
@Controller
@RequestMapping(value = "/search")
@Api(value = "search", description = "Operations related to external search providers.")
public class SearchController extends OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(SearchController.class);

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get list of available search providers")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public List<SearchProvider> listSearchProviders() throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listSearchProviders", LOGGER);
		try {
			List<ISearchProvider> providers =
					OpenIoT.getServer().getSearchProviderManager().getSearchProviders();
			List<SearchProvider> retval = new ArrayList<SearchProvider>();
			for (ISearchProvider provider : providers) {
				retval.add(SearchProvider.copy(provider));
			}
			return retval;
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	@RequestMapping(value = "/{providerId}/events", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Search provider for events that match the given criteria")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public List<IDeviceEvent> searchDeviceEvents(
			@ApiParam(value = "Search provider id", required = true) @PathVariable String providerId,
			HttpServletRequest request) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "searchDeviceEvents", LOGGER);
		try {
			ISearchProvider provider =
					OpenIoT.getServer().getSearchProviderManager().getSearchProvider(providerId);
			if (provider == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidSearchProviderId, ErrorLevel.ERROR,
						HttpServletResponse.SC_NOT_FOUND);
			}
			if (!(provider instanceof IDeviceEventSearchProvider)) {
				throw new OpenIoTException("Search provider does not provide event search capability.");
			}
			String query = request.getQueryString();
			return ((IDeviceEventSearchProvider) provider).executeQuery(query);
		} finally {
			Tracer.stop(LOGGER);
		}
	}
}