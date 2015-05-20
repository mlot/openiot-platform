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
import com.openiot.device.group.DeviceGroupUtils;
import com.openiot.device.marshaling.DeviceAssignmentMarshalHelper;
import com.openiot.device.marshaling.DeviceMarshalHelper;
import com.openiot.rest.model.device.DeviceElementMapping;
import com.openiot.rest.model.device.event.DeviceEventBatch;
import com.openiot.rest.model.device.event.request.DeviceAlertCreateRequest;
import com.openiot.rest.model.device.event.request.DeviceLocationCreateRequest;
import com.openiot.rest.model.device.event.request.DeviceMeasurementsCreateRequest;
import com.openiot.rest.model.device.request.DeviceCreateRequest;
import com.openiot.rest.model.search.SearchCriteria;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.rest.model.search.device.DeviceSearchCriteria;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.IDevice;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.event.IDeviceEventBatchResponse;
import com.openiot.spi.device.event.request.IDeviceAlertCreateRequest;
import com.openiot.spi.device.event.request.IDeviceLocationCreateRequest;
import com.openiot.spi.device.event.request.IDeviceMeasurementsCreateRequest;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.search.ISearchResults;
import com.openiot.spi.search.device.IDeviceSearchCriteria;
import com.openiot.spi.server.debug.TracerCategory;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Controller for device operations.
 * 
 * @author Derek Adams
 */
@Controller
@RequestMapping(value = "/devices")
@Api(value = "devices", description = "Operations related to OpenIoT devices.")
public class DevicesController extends OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(DevicesController.class);

	/**
	 * Create a device.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create a new device")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDevice createDevice(@RequestBody DeviceCreateRequest request) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createDevice", LOGGER);
		try {
			IDevice result = OpenIoT.getServer().getDeviceManagement().createDevice(request);
			DeviceMarshalHelper helper = new DeviceMarshalHelper();
			helper.setIncludeAsset(false);
			helper.setIncludeAssignment(false);
			return helper.convert(result, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Used by AJAX calls to find a device by hardware id.
	 * 
	 * @param hardwareId
	 * @return
	 */
	@RequestMapping(value = "/{hardwareId}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a device by unique hardware id")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDevice getDeviceByHardwareId(
			@ApiParam(value = "Hardware id", required = true) @PathVariable String hardwareId,
			@ApiParam(value = "Include specification information", required = false) @RequestParam(defaultValue = "true") boolean includeSpecification,
			@ApiParam(value = "Include assignment if associated", required = false) @RequestParam(defaultValue = "true") boolean includeAssignment,
			@ApiParam(value = "Include site information", required = false) @RequestParam(defaultValue = "true") boolean includeSite,
			@ApiParam(value = "Include detailed asset information", required = false) @RequestParam(defaultValue = "true") boolean includeAsset,
			@ApiParam(value = "Include detailed nested device information", required = false) @RequestParam(defaultValue = "false") boolean includeNested)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getDeviceByHardwareId", LOGGER);
		try {
			IDevice result = assertDeviceByHardwareId(hardwareId);
			DeviceMarshalHelper helper = new DeviceMarshalHelper();
			helper.setIncludeSpecification(includeSpecification);
			helper.setIncludeAsset(includeAsset);
			helper.setIncludeAssignment(includeAssignment);
			helper.setIncludeSite(includeSite);
			helper.setIncludeNested(includeNested);
			return helper.convert(result, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Update device information.
	 * 
	 * @param hardwareId
	 *            unique hardware id
	 * @param request
	 *            updated information
	 * @return the updated device
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{hardwareId}", method = RequestMethod.PUT)
	@ResponseBody
	@ApiOperation(value = "Update device information")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDevice updateDevice(
			@ApiParam(value = "Hardware id", required = true) @PathVariable String hardwareId,
			@RequestBody DeviceCreateRequest request) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "updateDevice", LOGGER);
		try {
			IDevice result = OpenIoT.getServer().getDeviceManagement().updateDevice(hardwareId, request);
			DeviceMarshalHelper helper = new DeviceMarshalHelper();
			helper.setIncludeAsset(true);
			helper.setIncludeAssignment(true);
			return helper.convert(result, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Delete device identified by hardware id.
	 * 
	 * @param hardwareId
	 * @return
	 */
	@RequestMapping(value = "/{hardwareId}", method = RequestMethod.DELETE)
	@ResponseBody
	@ApiOperation(value = "Delete a device based on unique hardware id")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDevice deleteDevice(
			@ApiParam(value = "Hardware id", required = true) @PathVariable String hardwareId,
			@ApiParam(value = "Delete permanently", required = false) @RequestParam(defaultValue = "false") boolean force)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "deleteDevice", LOGGER);
		try {
			IDevice result = OpenIoT.getServer().getDeviceManagement().deleteDevice(hardwareId, force);
			DeviceMarshalHelper helper = new DeviceMarshalHelper();
			helper.setIncludeAsset(true);
			helper.setIncludeAssignment(true);
			return helper.convert(result, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List device assignment history for a given device hardware id.
	 * 
	 * @param hardwareId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{hardwareId}/assignment", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get the current assignment for a device")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceAssignment getDeviceCurrentAssignment(
			@ApiParam(value = "Hardware id", required = true) @PathVariable String hardwareId,
			@ApiParam(value = "Include detailed asset information", required = false) @RequestParam(defaultValue = "true") boolean includeAsset,
			@ApiParam(value = "Include detailed device information", required = false) @RequestParam(defaultValue = "false") boolean includeDevice,
			@ApiParam(value = "Include detailed site information", required = false) @RequestParam(defaultValue = "true") boolean includeSite)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getDeviceCurrentAssignment", LOGGER);
		try {
			IDevice device = assertDeviceByHardwareId(hardwareId);
			IDeviceAssignment assignment =
					OpenIoT.getServer().getDeviceManagement().getCurrentDeviceAssignment(device);
			if (assignment == null) {
				throw new OpenIoTSystemException(ErrorCode.DeviceNotAssigned, ErrorLevel.INFO,
						HttpServletResponse.SC_NOT_FOUND);
			}
			DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper();
			helper.setIncludeAsset(includeAsset);
			helper.setIncludeDevice(includeDevice);
			helper.setIncludeSite(includeSite);
			return helper.convert(assignment, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List device assignment history for a given device hardware id.
	 * 
	 * @param hardwareId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{hardwareId}/assignments", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get assignment history for a device")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceAssignment> listDeviceAssignmentHistory(
			@ApiParam(value = "Hardware id", required = true) @PathVariable String hardwareId,
			@ApiParam(value = "Include detailed asset information", required = false) @RequestParam(defaultValue = "false") boolean includeAsset,
			@ApiParam(value = "Include detailed device information", required = false) @RequestParam(defaultValue = "false") boolean includeDevice,
			@ApiParam(value = "Include detailed site information", required = false) @RequestParam(defaultValue = "false") boolean includeSite,
			@ApiParam(value = "Page Number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDeviceAssignmentHistory", LOGGER);
		try {
			SearchCriteria criteria = new SearchCriteria(page, pageSize);
			ISearchResults<IDeviceAssignment> history =
					OpenIoT.getServer().getDeviceManagement().getDeviceAssignmentHistory(hardwareId,
							criteria);
			DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper();
			helper.setIncludeAsset(includeAsset);
			helper.setIncludeDevice(includeDevice);
			helper.setIncludeSite(includeSite);
			List<IDeviceAssignment> converted = new ArrayList<IDeviceAssignment>();
			for (IDeviceAssignment assignment : history.getResults()) {
				converted.add(helper.convert(assignment, OpenIoT.getServer().getAssetModuleManager()));
			}
			return new SearchResults<IDeviceAssignment>(converted, history.getNumResults());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Create a new device element mapping.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{hardwareId}/mappings", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create a new device element mapping")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDevice addDeviceElementMapping(
			@ApiParam(value = "Hardware id", required = true) @PathVariable String hardwareId,
			@RequestBody DeviceElementMapping request) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "addDeviceElementMapping", LOGGER);
		try {
			IDevice updated =
					OpenIoT.getServer().getDeviceManagement().createDeviceElementMapping(hardwareId,
							request);
			DeviceMarshalHelper helper = new DeviceMarshalHelper();
			helper.setIncludeAsset(false);
			helper.setIncludeAssignment(false);
			return helper.convert(updated, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	@RequestMapping(value = "/{hardwareId}/mappings", method = RequestMethod.DELETE)
	@ResponseBody
	@ApiOperation(value = "Delete an existing device element mapping")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDevice deleteDeviceElementMapping(
			@ApiParam(value = "Hardware id", required = true) @PathVariable String hardwareId,
			@ApiParam(value = "Device element path", required = true) @RequestParam(required = true) String path)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "deleteDeviceElementMapping", LOGGER);
		try {
			IDevice updated =
					OpenIoT.getServer().getDeviceManagement().deleteDeviceElementMapping(hardwareId, path);
			DeviceMarshalHelper helper = new DeviceMarshalHelper();
			helper.setIncludeAsset(false);
			helper.setIncludeAssignment(false);
			return helper.convert(updated, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List devices that match given criteria.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List devices that match certain criteria")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDevice> listDevices(
			@ApiParam(value = "Include deleted devices", required = false) @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
			@ApiParam(value = "Exclude assigned devices", required = false) @RequestParam(required = false, defaultValue = "false") boolean excludeAssigned,
			@ApiParam(value = "Include specification information", required = false) @RequestParam(required = false, defaultValue = "false") boolean includeSpecification,
			@ApiParam(value = "Include assignment information if associated", required = false) @RequestParam(required = false, defaultValue = "false") boolean includeAssignment,
			@ApiParam(value = "Page Number (First page is 1)", required = false) @RequestParam(required = false, defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(required = false, defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDevices", LOGGER);
		try {
			IDeviceSearchCriteria criteria =
					DeviceSearchCriteria.createDefaultSearch(page, pageSize, startDate, endDate,
							excludeAssigned);
			ISearchResults<IDevice> results =
					OpenIoT.getServer().getDeviceManagement().listDevices(includeDeleted, criteria);
			DeviceMarshalHelper helper = new DeviceMarshalHelper();
			helper.setIncludeAsset(true);
			helper.setIncludeSpecification(includeSpecification);
			helper.setIncludeAssignment(includeAssignment);
			List<IDevice> devicesConv = new ArrayList<IDevice>();
			for (IDevice device : results.getResults()) {
				devicesConv.add(helper.convert(device, OpenIoT.getServer().getAssetModuleManager()));
			}
			return new SearchResults<IDevice>(devicesConv, results.getNumResults());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	@RequestMapping(value = "/specification/{specificationToken}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List devices that use a given specification")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDevice> listDevicesForSpecification(
			@ApiParam(value = "Specification token", required = true) @PathVariable String specificationToken,
			@ApiParam(value = "Include deleted devices", required = false) @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
			@ApiParam(value = "Exclude assigned devices", required = false) @RequestParam(required = false, defaultValue = "false") boolean excludeAssigned,
			@ApiParam(value = "Include specification information", required = false) @RequestParam(required = false, defaultValue = "false") boolean includeSpecification,
			@ApiParam(value = "Include assignment information if associated", required = false) @RequestParam(required = false, defaultValue = "false") boolean includeAssignment,
			@ApiParam(value = "Page Number (First page is 1)", required = false) @RequestParam(required = false, defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(required = false, defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDevices", LOGGER);
		try {
			IDeviceSearchCriteria criteria =
					DeviceSearchCriteria.createDeviceBySpecificationSearch(specificationToken, page,
							pageSize, startDate, endDate, excludeAssigned);
			ISearchResults<IDevice> results =
					OpenIoT.getServer().getDeviceManagement().listDevices(includeDeleted, criteria);
			DeviceMarshalHelper helper = new DeviceMarshalHelper();
			helper.setIncludeAsset(true);
			helper.setIncludeSpecification(includeSpecification);
			helper.setIncludeAssignment(includeAssignment);
			List<IDevice> devicesConv = new ArrayList<IDevice>();
			for (IDevice device : results.getResults()) {
				devicesConv.add(helper.convert(device, OpenIoT.getServer().getAssetModuleManager()));
			}
			return new SearchResults<IDevice>(devicesConv, results.getNumResults());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	@RequestMapping(value = "/group/{groupToken}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List devices that belong to a given group")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDevice> listDevicesForGroup(
			@ApiParam(value = "Group token", required = true) @PathVariable String groupToken,
			@ApiParam(value = "Specification token", required = false) @RequestParam(required = false) String specification,
			@ApiParam(value = "Include deleted devices", required = false) @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
			@ApiParam(value = "Exclude assigned devices", required = false) @RequestParam(required = false, defaultValue = "false") boolean excludeAssigned,
			@ApiParam(value = "Include specification information", required = false) @RequestParam(required = false, defaultValue = "false") boolean includeSpecification,
			@ApiParam(value = "Include assignment information if associated", required = false) @RequestParam(required = false, defaultValue = "false") boolean includeAssignment,
			@ApiParam(value = "Page Number (First page is 1)", required = false) @RequestParam(required = false, defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(required = false, defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDevicesForGroup", LOGGER);
		try {
			IDeviceSearchCriteria criteria;
			if (specification == null) {
				criteria =
						DeviceSearchCriteria.createDefaultSearch(page, pageSize, startDate, endDate,
								excludeAssigned);
			} else {
				criteria =
						DeviceSearchCriteria.createDeviceBySpecificationSearch(specification, page, pageSize,
								startDate, endDate, excludeAssigned);
			}
			List<IDevice> matches = DeviceGroupUtils.getDevicesInGroup(groupToken, criteria);
			DeviceMarshalHelper helper = new DeviceMarshalHelper();
			helper.setIncludeAsset(true);
			helper.setIncludeSpecification(includeSpecification);
			helper.setIncludeAssignment(includeAssignment);
			List<IDevice> devicesConv = new ArrayList<IDevice>();
			for (IDevice device : matches) {
				devicesConv.add(helper.convert(device, OpenIoT.getServer().getAssetModuleManager()));
			}
			return new SearchResults<IDevice>(devicesConv, matches.size());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	@RequestMapping(value = "/grouprole/{role}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List devices that belong to a groups with a given role")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDevice> listDevicesForGroupsWithRole(
			@ApiParam(value = "Group role", required = true) @PathVariable String role,
			@ApiParam(value = "Specification token", required = false) @RequestParam(required = false) String specification,
			@ApiParam(value = "Include deleted devices", required = false) @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
			@ApiParam(value = "Exclude assigned devices", required = false) @RequestParam(required = false, defaultValue = "false") boolean excludeAssigned,
			@ApiParam(value = "Include specification information", required = false) @RequestParam(required = false, defaultValue = "false") boolean includeSpecification,
			@ApiParam(value = "Include assignment information if associated", required = false) @RequestParam(required = false, defaultValue = "false") boolean includeAssignment,
			@ApiParam(value = "Page Number (First page is 1)", required = false) @RequestParam(required = false, defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(required = false, defaultValue = "100") int pageSize,
			@ApiParam(value = "Start date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
			@ApiParam(value = "End date", required = false) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDevicesForGroupsWithRole", LOGGER);
		try {
			IDeviceSearchCriteria criteria;
			if (specification == null) {
				criteria =
						DeviceSearchCriteria.createDefaultSearch(page, pageSize, startDate, endDate,
								excludeAssigned);
			} else {
				criteria =
						DeviceSearchCriteria.createDeviceBySpecificationSearch(specification, page, pageSize,
								startDate, endDate, excludeAssigned);
			}
			Collection<IDevice> matches = DeviceGroupUtils.getDevicesInGroupsWithRole(role, criteria);
			DeviceMarshalHelper helper = new DeviceMarshalHelper();
			helper.setIncludeAsset(true);
			helper.setIncludeSpecification(includeSpecification);
			helper.setIncludeAssignment(includeAssignment);
			List<IDevice> devicesConv = new ArrayList<IDevice>();
			for (IDevice device : matches) {
				devicesConv.add(helper.convert(device, OpenIoT.getServer().getAssetModuleManager()));
			}
			return new SearchResults<IDevice>(devicesConv, matches.size());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Add a batch of events for the current assignment of the given device. Note that the
	 * hardware id in the URL overrides the one specified in the {@link DeviceEventBatch}
	 * object.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{hardwareId}/batch", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Send a batch of events for the current assignment of the given device.")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceEventBatchResponse addDeviceEventBatch(
			@ApiParam(value = "Hardware id", required = true) @PathVariable String hardwareId,
			@RequestBody DeviceEventBatch batch) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "addDeviceEventBatch", LOGGER);
		try {
			IDevice device = assertDeviceByHardwareId(hardwareId);
			if (device.getAssignmentToken() == null) {
				throw new OpenIoTSystemException(ErrorCode.DeviceNotAssigned, ErrorLevel.ERROR);
			}

			// Set event dates if not set by client.
			for (IDeviceLocationCreateRequest locReq : batch.getLocations()) {
				if (locReq.getEventDate() == null) {
					((DeviceLocationCreateRequest) locReq).setEventDate(new Date());
				}
			}
			for (IDeviceMeasurementsCreateRequest measReq : batch.getMeasurements()) {
				if (measReq.getEventDate() == null) {
					((DeviceMeasurementsCreateRequest) measReq).setEventDate(new Date());
				}
			}
			for (IDeviceAlertCreateRequest alertReq : batch.getAlerts()) {
				if (alertReq.getEventDate() == null) {
					((DeviceAlertCreateRequest) alertReq).setEventDate(new Date());
				}
			}

			return OpenIoT.getServer().getDeviceManagement().addDeviceEventBatch(
					device.getAssignmentToken(), batch);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Gets a device by unique hardware id and throws an exception if not found.
	 * 
	 * @param hardwareId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected IDevice assertDeviceByHardwareId(String hardwareId) throws OpenIoTException {
		IDevice result = OpenIoT.getServer().getDeviceManagement().getDeviceByHardwareId(hardwareId);
		if (result == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidHardwareId, ErrorLevel.ERROR,
					HttpServletResponse.SC_NOT_FOUND);
		}
		return result;
	}
}