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
import com.openiot.device.charting.ChartBuilder;
import com.openiot.device.marshaling.DeviceAssignmentMarshalHelper;
import com.openiot.device.marshaling.DeviceCommandInvocationMarshalHelper;
import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.rest.model.device.DeviceAssignment;
import com.openiot.rest.model.device.event.*;
import com.openiot.rest.model.device.event.request.*;
import com.openiot.rest.model.device.request.DeviceAssignmentCreateRequest;
import com.openiot.rest.model.search.DateRangeSearchCriteria;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.DeviceAssignmentStatus;
import com.openiot.spi.device.DeviceAssignmentType;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.IDeviceManagement;
import com.openiot.spi.device.charting.IChartSeries;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.event.*;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.search.ISearchResults;
import com.openiot.spi.server.debug.TracerCategory;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller for assignment operations.
 * 
 * @author Derek Adams
 */
@Controller
@RequestMapping(value = "/assignments")
@Api(value = "assignments", description = "Operations related to OpenIoT device assignments.")
public class AssignmentsController extends OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(AssignmentsController.class);

	/**
	 * Used by AJAX calls to create a device assignment.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create a new device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public DeviceAssignment createDeviceAssignment(@RequestBody DeviceAssignmentCreateRequest request)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createDeviceAssignment", LOGGER);
		try {
			if (StringUtils.isEmpty(request.getDeviceHardwareId())) {
				throw new OpenIoTException("Hardware id required.");
			}
			if (request.getAssignmentType() == null) {
				throw new OpenIoTException("Assignment type required.");
			}
			if (request.getAssignmentType() != DeviceAssignmentType.Unassociated) {
				if (request.getAssetModuleId() == null) {
					throw new OpenIoTException("Asset module id required.");
				}
				if (request.getAssetId() == null) {
					throw new OpenIoTException("Asset id required.");
				}
			}
			IDeviceManagement management = OpenIoT.getServer().getDeviceManagement();
			IDeviceAssignment created = management.createDeviceAssignment(request);
			DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper();
			helper.setIncludeAsset(true);
			helper.setIncludeDevice(true);
			helper.setIncludeSite(true);
			return helper.convert(created, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get an assignment by its unique token.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a device assignment by unique token")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public DeviceAssignment getDeviceAssignment(
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getDeviceAssignment", LOGGER);
		try {
			IDeviceAssignment assignment = assureAssignment(token);
			DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper();
			helper.setIncludeAsset(true);
			helper.setIncludeDevice(true);
			helper.setIncludeSite(true);
			return helper.convert(assignment, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get an assignment by its unique token.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}", method = RequestMethod.DELETE)
	@ResponseBody
	@ApiOperation(value = "Delete a device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public DeviceAssignment deleteDeviceAssignment(
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token,
			@ApiParam(value = "Delete permanently", required = false) @RequestParam(defaultValue = "false") boolean force)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "deleteDeviceAssignment", LOGGER);
		try {
			IDeviceAssignment assignment =
					OpenIoT.getServer().getDeviceManagement().deleteDeviceAssignment(token, force);
			DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper();
			helper.setIncludeAsset(true);
			helper.setIncludeDevice(true);
			helper.setIncludeSite(true);
			return helper.convert(assignment, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Update metadata associated with an assignment.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{token}/metadata", method = RequestMethod.PUT)
	@ResponseBody
	@ApiOperation(value = "Update metadata for a device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public DeviceAssignment updateDeviceAssignmentMetadata(
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token,
			@RequestBody MetadataProvider metadata) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "updateDeviceAssignmentMetadata", LOGGER);
		try {
			IDeviceAssignment result =
					OpenIoT.getServer().getDeviceManagement().updateDeviceAssignmentMetadata(token,
							metadata);
			DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper();
			helper.setIncludeAsset(true);
			helper.setIncludeDevice(true);
			helper.setIncludeSite(true);
			return helper.convert(result, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List all device events for an assignment that match the given criteria.
	 * 
	 * @param token
	 * @param page
	 * @param pageSize
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/events", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List all events for device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceEvent> listEvents(
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token,
			@ApiParam(value = "Page number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listEvents", LOGGER);
		try {
			DateRangeSearchCriteria criteria =
					new DateRangeSearchCriteria(page, pageSize, startDate, endDate);
			return OpenIoT.getServer().getDeviceManagement().listDeviceEvents(token, criteria);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List all device measurements for a given assignment.
	 * 
	 * @param assignmentToken
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/measurements", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List measurement events for device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceMeasurements> listMeasurements(
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token,
			@ApiParam(value = "Page number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listMeasurements", LOGGER);
		try {
			DateRangeSearchCriteria criteria =
					new DateRangeSearchCriteria(page, pageSize, startDate, endDate);
			return OpenIoT.getServer().getDeviceManagement().listDeviceMeasurements(token, criteria);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List device measurements for a given assignment.
	 * 
	 * @param assignmentToken
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/measurements/series", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List measurement events for device assignment in chart format")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public List<IChartSeries<Double>> listMeasurementsAsChartSeries(
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token,
			@ApiParam(value = "Page number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate,
			@ApiParam(value = "Measurement Ids", required = false) @RequestParam(required = false) String[] measurementIds)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listMeasurementsAsChartSeries", LOGGER);
		try {
			DateRangeSearchCriteria criteria =
					new DateRangeSearchCriteria(page, pageSize, startDate, endDate);
			ISearchResults<IDeviceMeasurements> measurements =
					OpenIoT.getServer().getDeviceManagement().listDeviceMeasurements(token, criteria);
			ChartBuilder builder = new ChartBuilder();
			return builder.process(measurements.getResults(), measurementIds);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Create measurements to be associated with a device assignment.
	 * 
	 * @param input
	 * @param token
	 * @param updateState
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/measurements", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create measurements event for a device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public DeviceMeasurements createMeasurements(@RequestBody DeviceMeasurementsCreateRequest input,
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createMeasurements", LOGGER);
		try {
			IDeviceMeasurements result =
					OpenIoT.getServer().getDeviceManagement().addDeviceMeasurements(token, input);
			return DeviceMeasurements.copy(result);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List device locations for a given assignment.
	 * 
	 * @param assignmentToken
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/locations", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List location events for a device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceLocation> listLocations(
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token,
			@ApiParam(value = "Page number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listLocations", LOGGER);
		try {
			DateRangeSearchCriteria criteria =
					new DateRangeSearchCriteria(page, pageSize, startDate, endDate);
			return OpenIoT.getServer().getDeviceManagement().listDeviceLocations(token, criteria);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Create location to be associated with a device assignment.
	 * 
	 * @param input
	 * @param token
	 * @param updateState
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/locations", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create a location event for a device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public DeviceLocation createLocation(@RequestBody DeviceLocationCreateRequest input,
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createLocation", LOGGER);
		try {
			IDeviceLocation result =
					OpenIoT.getServer().getDeviceManagement().addDeviceLocation(token, input);
			return DeviceLocation.copy(result);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List device alerts for a given assignment.
	 * 
	 * @param assignmentToken
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/alerts", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List alert events for a device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceAlert> listAlerts(
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token,
			@ApiParam(value = "Page number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listAlerts", LOGGER);
		try {
			DateRangeSearchCriteria criteria =
					new DateRangeSearchCriteria(page, pageSize, startDate, endDate);
			return OpenIoT.getServer().getDeviceManagement().listDeviceAlerts(token, criteria);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Create alert to be associated with a device assignment.
	 * 
	 * @param input
	 * @param token
	 * @param updateState
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/alerts", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create an alert event for a device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public DeviceAlert createAlert(@RequestBody DeviceAlertCreateRequest input,
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createAlert", LOGGER);
		try {
			IDeviceAlert result = OpenIoT.getServer().getDeviceManagement().addDeviceAlert(token, input);
			return DeviceAlert.copy(result);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Create command invocation to be associated with a device assignment.
	 * 
	 * @param input
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/invocations", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create a command invocation event for a device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public DeviceCommandInvocation createCommandInvocation(
			@RequestBody DeviceCommandInvocationCreateRequest request,
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createCommandInvocation", LOGGER);
		try {
			if (request.getInitiator() == null) {
				throw new OpenIoTException("Command initiator is required.");
			}
			if (request.getTarget() == null) {
				throw new OpenIoTException("Command target is required.");
			}
			IDeviceCommand command = assureDeviceCommand(request.getCommandToken());
			IDeviceCommandInvocation result =
					OpenIoT.getServer().getDeviceManagement().addDeviceCommandInvocation(token, command,
							request);
			DeviceCommandInvocationMarshalHelper helper = new DeviceCommandInvocationMarshalHelper();
			return helper.convert(result);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List device command invocations for a given assignment.
	 * 
	 * @param assignmentToken
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/invocations", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List alert events for a device command invocations")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceCommandInvocation> listCommandInvocations(
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token,
			@ApiParam(value = "Include command information", required = false) @RequestParam(defaultValue = "true") boolean includeCommand,
			@ApiParam(value = "Page number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listCommandInvocations", LOGGER);
		try {
			DateRangeSearchCriteria criteria =
					new DateRangeSearchCriteria(page, pageSize, startDate, endDate);
			ISearchResults<IDeviceCommandInvocation> matches =
					OpenIoT.getServer().getDeviceManagement().listDeviceCommandInvocations(token, criteria);
			DeviceCommandInvocationMarshalHelper helper = new DeviceCommandInvocationMarshalHelper();
			helper.setIncludeCommand(includeCommand);
			List<IDeviceCommandInvocation> converted = new ArrayList<IDeviceCommandInvocation>();
			for (IDeviceCommandInvocation invocation : matches.getResults()) {
				converted.add(helper.convert(invocation));
			}
			return new SearchResults<IDeviceCommandInvocation>(converted);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Create state change to be associated with a device assignment.
	 * 
	 * @param input
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/statechanges", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create an state change event for a device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public DeviceStateChange createStateChange(@RequestBody DeviceStateChangeCreateRequest input,
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createStateChange", LOGGER);
		try {
			IDeviceStateChange result =
					OpenIoT.getServer().getDeviceManagement().addDeviceStateChange(token, input);
			return DeviceStateChange.copy(result);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List device state changes for a given assignment.
	 * 
	 * @param assignmentToken
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/statechanges", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List state change events for a device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceStateChange> listStateChanges(
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token,
			@ApiParam(value = "Page number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listStateChanges", LOGGER);
		try {
			DateRangeSearchCriteria criteria =
					new DateRangeSearchCriteria(page, pageSize, startDate, endDate);
			return OpenIoT.getServer().getDeviceManagement().listDeviceStateChanges(token, criteria);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Create command response to be associated with a device assignment.
	 * 
	 * @param input
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/responses", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create an command response event for a device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public DeviceCommandResponse createCommandResponse(@RequestBody DeviceCommandResponseCreateRequest input,
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createCommandResponse", LOGGER);
		try {
			IDeviceCommandResponse result =
					OpenIoT.getServer().getDeviceManagement().addDeviceCommandResponse(token, input);
			return DeviceCommandResponse.copy(result);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List device command responses for a given assignment.
	 * 
	 * @param assignmentToken
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/responses", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List command response events for a device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceCommandResponse> listCommandResponses(
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token,
			@ApiParam(value = "Page number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listCommandResponses", LOGGER);
		try {
			DateRangeSearchCriteria criteria =
					new DateRangeSearchCriteria(page, pageSize, startDate, endDate);
			return OpenIoT.getServer().getDeviceManagement().listDeviceCommandResponses(token, criteria);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * End an existing device assignment.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/end", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "End an active device assignment")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public DeviceAssignment endDeviceAssignment(
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "endDeviceAssignment", LOGGER);
		try {
			IDeviceManagement management = OpenIoT.getServer().getDeviceManagement();
			IDeviceAssignment updated = management.endDeviceAssignment(token);
			DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper();
			helper.setIncludeAsset(true);
			helper.setIncludeDevice(true);
			helper.setIncludeSite(true);
			return helper.convert(updated, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Mark a device assignment as missing.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/missing", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Mark a device assignment as missing")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public DeviceAssignment missingDeviceAssignment(
			@ApiParam(value = "Assignment token", required = true) @PathVariable String token)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "missingDeviceAssignment", LOGGER);
		try {
			IDeviceManagement management = OpenIoT.getServer().getDeviceManagement();
			IDeviceAssignment updated =
					management.updateDeviceAssignmentStatus(token, DeviceAssignmentStatus.Missing);
			DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper();
			helper.setIncludeAsset(true);
			helper.setIncludeDevice(true);
			helper.setIncludeSite(true);
			return helper.convert(updated, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get an assignment by unique token. Throw an exception if not found.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected IDeviceAssignment assureAssignment(String token) throws OpenIoTException {
		IDeviceAssignment assignment =
				OpenIoT.getServer().getDeviceManagement().getDeviceAssignmentByToken(token);
		if (assignment == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceAssignmentToken, ErrorLevel.ERROR);
		}
		return assignment;
	}

	/**
	 * Get a device command by unique token. Throw an exception if not found.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected IDeviceCommand assureDeviceCommand(String token) throws OpenIoTException {
		IDeviceCommand command = OpenIoT.getServer().getDeviceManagement().getDeviceCommandByToken(token);
		if (command == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceCommandToken, ErrorLevel.ERROR);
		}
		return command;
	}
}