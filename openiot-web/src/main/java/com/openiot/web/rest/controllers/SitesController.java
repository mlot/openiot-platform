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
import com.openiot.device.marshaling.DeviceAssignmentMarshalHelper;
import com.openiot.rest.model.device.DeviceAssignment;
import com.openiot.rest.model.device.Site;
import com.openiot.rest.model.device.Zone;
import com.openiot.rest.model.device.asset.*;
import com.openiot.rest.model.device.request.SiteCreateRequest;
import com.openiot.rest.model.device.request.ZoneCreateRequest;
import com.openiot.rest.model.search.DateRangeSearchCriteria;
import com.openiot.rest.model.search.SearchCriteria;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.asset.IAssetModuleManager;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.ISite;
import com.openiot.spi.device.IZone;
import com.openiot.spi.device.event.*;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.search.ISearchResults;
import com.openiot.spi.server.debug.TracerCategory;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller for site operations.
 * 
 * @author Derek Adams
 */
@Controller
@RequestMapping(value = "/sites")
@Api(value = "sites", description = "Operations related to OpenIoT sites.")
public class SitesController extends OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(SitesController.class);

	/**
	 * Create a new site.
	 * 
	 * @param input
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create a new site")
	@Secured({ SitewhereRoles.ROLE_ADMINISTER_SITES })
	public Site createSite(@RequestBody SiteCreateRequest input) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createSite", LOGGER);
		try {
			ISite site = OpenIoT.getServer().getDeviceManagement().createSite(input);
			return Site.copy(site);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get information for a given site based on site token.
	 * 
	 * @param siteToken
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{siteToken}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a site by unique token")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public Site getSiteByToken(
			@ApiParam(value = "Unique token that identifies site", required = true) @PathVariable String siteToken)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getSiteByToken", LOGGER);
		try {
			ISite site = OpenIoT.getServer().getDeviceManagement().getSiteByToken(siteToken);
			if (site == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.ERROR);
			}
			return Site.copy(site);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Update information for a site.
	 * 
	 * @param input
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{siteToken}", method = RequestMethod.PUT)
	@ResponseBody
	@ApiOperation(value = "Update an existing site")
	@Secured({ SitewhereRoles.ROLE_ADMINISTER_SITES })
	public Site updateSite(
			@ApiParam(value = "Unique token that identifies site", required = true) @PathVariable String siteToken,
			@RequestBody SiteCreateRequest request) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "updateSite", LOGGER);
		try {
			ISite site = OpenIoT.getServer().getDeviceManagement().updateSite(siteToken, request);
			return Site.copy(site);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Delete information for a given site based on site token.
	 * 
	 * @param siteToken
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{siteToken}", method = RequestMethod.DELETE)
	@ResponseBody
	@ApiOperation(value = "Delete a site by unique token")
	@Secured({ SitewhereRoles.ROLE_ADMINISTER_SITES })
	public Site deleteSiteByToken(
			@ApiParam(value = "Unique token that identifies site", required = true) @PathVariable String siteToken,
			@ApiParam(value = "Delete permanently", required = false) @RequestParam(defaultValue = "false") boolean force)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "deleteSiteByToken", LOGGER);
		try {
			ISite site = OpenIoT.getServer().getDeviceManagement().deleteSite(siteToken, force);
			return Site.copy(site);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List all sites and wrap as search results.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List all sites")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<ISite> listSites(
			@ApiParam(value = "Page Number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listSites", LOGGER);
		try {
			SearchCriteria criteria = new SearchCriteria(page, pageSize);
			return OpenIoT.getServer().getDeviceManagement().listSites(criteria);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get device measurements for a given site.
	 * 
	 * @param siteToken
	 * @param count
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{siteToken}/measurements", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List measurements associated with a site")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceMeasurements> listDeviceMeasurementsForSite(
			@ApiParam(value = "Unique token that identifies site", required = true) @PathVariable String siteToken,
			@ApiParam(value = "Page number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDeviceMeasurementsForSite", LOGGER);
		try {
			DateRangeSearchCriteria criteria =
					new DateRangeSearchCriteria(page, pageSize, startDate, endDate);
			ISearchResults<IDeviceMeasurements> results =
					OpenIoT.getServer().getDeviceManagement().listDeviceMeasurementsForSite(siteToken,
							criteria);

			// Marshal with asset info since multiple assignments might match.
			List<IDeviceMeasurements> wrapped = new ArrayList<IDeviceMeasurements>();
			IAssetModuleManager assets = OpenIoT.getServer().getAssetModuleManager();
			for (IDeviceMeasurements result : results.getResults()) {
				wrapped.add(new DeviceMeasurementsWithAsset(result, assets));
			}
			return new SearchResults<IDeviceMeasurements>(wrapped, results.getNumResults());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get device locations for a given site.
	 * 
	 * @param siteToken
	 * @param count
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{siteToken}/locations", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List locations associated with a site")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceLocation> listDeviceLocationsForSite(
			@ApiParam(value = "Unique token that identifies site", required = true) @PathVariable String siteToken,
			@ApiParam(value = "Page number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDeviceLocationsForSite", LOGGER);
		try {
			DateRangeSearchCriteria criteria =
					new DateRangeSearchCriteria(page, pageSize, startDate, endDate);
			ISearchResults<IDeviceLocation> results =
					OpenIoT.getServer().getDeviceManagement().listDeviceLocationsForSite(siteToken,
							criteria);

			// Marshal with asset info since multiple assignments might match.
			List<IDeviceLocation> wrapped = new ArrayList<IDeviceLocation>();
			IAssetModuleManager assets = OpenIoT.getServer().getAssetModuleManager();
			for (IDeviceLocation result : results.getResults()) {
				wrapped.add(new DeviceLocationWithAsset(result, assets));
			}
			return new SearchResults<IDeviceLocation>(wrapped, results.getNumResults());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get device alerts for a given site.
	 * 
	 * @param siteToken
	 * @param count
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{siteToken}/alerts", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List alerts associated with a site")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceAlert> listDeviceAlertsForSite(
			@ApiParam(value = "Unique token that identifies site", required = true) @PathVariable String siteToken,
			@ApiParam(value = "Page number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDeviceAlertsForSite", LOGGER);
		try {
			DateRangeSearchCriteria criteria =
					new DateRangeSearchCriteria(page, pageSize, startDate, endDate);
			ISearchResults<IDeviceAlert> results =
					OpenIoT.getServer().getDeviceManagement().listDeviceAlertsForSite(siteToken, criteria);

			// Marshal with asset info since multiple assignments might match.
			List<IDeviceAlert> wrapped = new ArrayList<IDeviceAlert>();
			IAssetModuleManager assets = OpenIoT.getServer().getAssetModuleManager();
			for (IDeviceAlert result : results.getResults()) {
				wrapped.add(new DeviceAlertWithAsset(result, assets));
			}
			return new SearchResults<IDeviceAlert>(wrapped, results.getNumResults());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get device command invocations for a given site.
	 * 
	 * @param siteToken
	 * @param count
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{siteToken}/invocations", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List command invocations associated with a site")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceCommandInvocation> listDeviceCommandInvocationsForSite(
			@ApiParam(value = "Unique token that identifies site", required = true) @PathVariable String siteToken,
			@ApiParam(value = "Page number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDeviceCommandInvocationsForSite", LOGGER);
		try {
			DateRangeSearchCriteria criteria =
					new DateRangeSearchCriteria(page, pageSize, startDate, endDate);
			ISearchResults<IDeviceCommandInvocation> results =
					OpenIoT.getServer().getDeviceManagement().listDeviceCommandInvocationsForSite(
							siteToken, criteria);

			// Marshal with asset info since multiple assignments might match.
			List<IDeviceCommandInvocation> wrapped = new ArrayList<IDeviceCommandInvocation>();
			IAssetModuleManager assets = OpenIoT.getServer().getAssetModuleManager();
			for (IDeviceCommandInvocation result : results.getResults()) {
				wrapped.add(new DeviceCommandInvocationWithAsset(result, assets));
			}
			return new SearchResults<IDeviceCommandInvocation>(wrapped, results.getNumResults());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get device command responses for a given site.
	 * 
	 * @param siteToken
	 * @param count
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{siteToken}/responses", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List command responses associated with a site")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceCommandResponse> listDeviceCommandResponsesForSite(
			@ApiParam(value = "Unique token that identifies site", required = true) @PathVariable String siteToken,
			@ApiParam(value = "Page number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDeviceCommandResponsesForSite", LOGGER);
		try {
			DateRangeSearchCriteria criteria =
					new DateRangeSearchCriteria(page, pageSize, startDate, endDate);
			ISearchResults<IDeviceCommandResponse> results =
					OpenIoT.getServer().getDeviceManagement().listDeviceCommandResponsesForSite(siteToken,
							criteria);

			// Marshal with asset info since multiple assignments might match.
			List<IDeviceCommandResponse> wrapped = new ArrayList<IDeviceCommandResponse>();
			IAssetModuleManager assets = OpenIoT.getServer().getAssetModuleManager();
			for (IDeviceCommandResponse result : results.getResults()) {
				wrapped.add(new DeviceCommandResponseWithAsset(result, assets));
			}
			return new SearchResults<IDeviceCommandResponse>(wrapped, results.getNumResults());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get device state changes for a given site.
	 * 
	 * @param siteToken
	 * @param count
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{siteToken}/statechanges", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List state changes associated with a site")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceStateChange> listDeviceStateChangesForSite(
			@ApiParam(value = "Unique token that identifies site", required = true) @PathVariable String siteToken,
			@ApiParam(value = "Page number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDeviceStateChangesForSite", LOGGER);
		try {
			DateRangeSearchCriteria criteria =
					new DateRangeSearchCriteria(page, pageSize, startDate, endDate);
			ISearchResults<IDeviceStateChange> results =
					OpenIoT.getServer().getDeviceManagement().listDeviceStateChangesForSite(siteToken,
							criteria);

			// Marshal with asset info since multiple assignments might match.
			List<IDeviceStateChange> wrapped = new ArrayList<IDeviceStateChange>();
			IAssetModuleManager assets = OpenIoT.getServer().getAssetModuleManager();
			for (IDeviceStateChange result : results.getResults()) {
				wrapped.add(new DeviceStateChangeWithAsset(result, assets));
			}
			return new SearchResults<IDeviceStateChange>(wrapped, results.getNumResults());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Find device assignments associated with a site.
	 * 
	 * @param siteToken
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{siteToken}/assignments", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List device assignments associated with a site")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<DeviceAssignment> findAssignmentsForSite(
			@ApiParam(value = "Unique token that identifies site", required = true) @PathVariable String siteToken,
			@ApiParam(value = "Include detailed device information", required = false) @RequestParam(defaultValue = "false") boolean includeDevice,
			@ApiParam(value = "Include detailed asset information", required = false) @RequestParam(defaultValue = "false") boolean includeAsset,
			@ApiParam(value = "Include detailed site information", required = false) @RequestParam(defaultValue = "false") boolean includeSite,
			@ApiParam(value = "Page Number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "findAssignmentsForSite", LOGGER);
		try {
			SearchCriteria criteria = new SearchCriteria(page, pageSize);
			ISearchResults<IDeviceAssignment> matches =
					OpenIoT.getServer().getDeviceManagement().getDeviceAssignmentsForSite(siteToken,
							criteria);
			DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper();
			helper.setIncludeAsset(includeAsset);
			helper.setIncludeDevice(includeDevice);
			helper.setIncludeSite(includeSite);
			List<DeviceAssignment> converted = new ArrayList<DeviceAssignment>();
			for (IDeviceAssignment assignment : matches.getResults()) {
				converted.add(helper.convert(assignment, OpenIoT.getServer().getAssetModuleManager()));
			}
			return new SearchResults<DeviceAssignment>(converted, matches.getNumResults());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Create a new zone for a site.
	 * 
	 * @param input
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{siteToken}/zones", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create a new zone associated with a site")
	@Secured({ SitewhereRoles.ROLE_ADMINISTER_SITES })
	public Zone createZone(
			@ApiParam(value = "Unique site token", required = true) @PathVariable String siteToken,
			@RequestBody ZoneCreateRequest request) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createZone", LOGGER);
		try {
			ISite site = OpenIoT.getServer().getDeviceManagement().getSiteByToken(siteToken);
			if (site == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.ERROR);
			}
			IZone zone = OpenIoT.getServer().getDeviceManagement().createZone(site, request);
			return Zone.copy(zone);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List all zones for a site.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{siteToken}/zones", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List zones associated with a site")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IZone> listZonesForSite(
			@ApiParam(value = "Unique token that identifies site", required = true) @PathVariable String siteToken,
			@ApiParam(value = "Page Number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listZonesForSite", LOGGER);
		try {
			SearchCriteria criteria = new SearchCriteria(page, pageSize);
			return OpenIoT.getServer().getDeviceManagement().listZones(siteToken, criteria);
		} finally {
			Tracer.stop(LOGGER);
		}
	}
}