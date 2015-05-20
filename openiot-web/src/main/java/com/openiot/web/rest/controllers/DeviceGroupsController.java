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
import com.openiot.device.marshaling.DeviceGroupElementMarshalHelper;
import com.openiot.rest.model.device.group.DeviceGroup;
import com.openiot.rest.model.device.request.DeviceGroupCreateRequest;
import com.openiot.rest.model.device.request.DeviceGroupElementCreateRequest;
import com.openiot.rest.model.search.SearchCriteria;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.group.IDeviceGroup;
import com.openiot.spi.device.group.IDeviceGroupElement;
import com.openiot.spi.device.request.IDeviceGroupElementCreateRequest;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.search.ISearchResults;
import com.openiot.spi.server.debug.TracerCategory;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for device group operations.
 * 
 * @author Derek Adams
 */
@Controller
@RequestMapping(value = "/devicegroups")
@Api(value = "devicegroups", description = "Operations related to OpenIoT device groups.")
public class DeviceGroupsController extends OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(DeviceGroupsController.class);

	/**
	 * Create a device group.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create a new device group")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceGroup createDeviceGroup(@RequestBody DeviceGroupCreateRequest request)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createDeviceGroup", LOGGER);
		try {
			IDeviceGroup result = OpenIoT.getServer().getDeviceManagement().createDeviceGroup(request);
			return DeviceGroup.copy(result);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get a device group by unique token.
	 * 
	 * @param groupToken
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{groupToken}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a device group by unique token")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceGroup getDeviceGroupByToken(
			@ApiParam(value = "Unique token that identifies group", required = true) @PathVariable String groupToken)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getDeviceGroupByToken", LOGGER);
		try {
			IDeviceGroup group = OpenIoT.getServer().getDeviceManagement().getDeviceGroup(groupToken);
			if (group == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidDeviceGroupToken, ErrorLevel.ERROR);
			}
			return DeviceGroup.copy(group);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Update an existing device group.
	 * 
	 * @param groupToken
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{groupToken}", method = RequestMethod.PUT)
	@ResponseBody
	@ApiOperation(value = "Update an existing device group")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceGroup updateDeviceGroup(
			@ApiParam(value = "Unique token that identifies device group", required = true) @PathVariable String groupToken,
			@RequestBody DeviceGroupCreateRequest request) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "updateDeviceGroup", LOGGER);
		try {
			IDeviceGroup group =
					OpenIoT.getServer().getDeviceManagement().updateDeviceGroup(groupToken, request);
			return DeviceGroup.copy(group);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Delete an existing device group.
	 * 
	 * @param groupToken
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{groupToken}", method = RequestMethod.DELETE)
	@ResponseBody
	@ApiOperation(value = "Delete a device group by unique token")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceGroup deleteDeviceGroup(
			@ApiParam(value = "Unique token that identifies device group", required = true) @PathVariable String groupToken,
			@ApiParam(value = "Delete permanently", required = false) @RequestParam(defaultValue = "false") boolean force)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "deleteDeviceGroup", LOGGER);
		try {
			IDeviceGroup group =
					OpenIoT.getServer().getDeviceManagement().deleteDeviceGroup(groupToken, force);
			return DeviceGroup.copy(group);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List all device groups.
	 * 
	 * @param role
	 * @param includeDeleted
	 * @param page
	 * @param pageSize
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List all device groups")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceGroup> listDeviceGroups(
			@ApiParam(value = "Role", required = false) @RequestParam(required = false) String role,
			@ApiParam(value = "Include deleted", required = false) @RequestParam(defaultValue = "false") boolean includeDeleted,
			@ApiParam(value = "Page Number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDeviceGroups", LOGGER);
		try {
			SearchCriteria criteria = new SearchCriteria(page, pageSize);
			ISearchResults<IDeviceGroup> results;
			if (role == null) {
				results =
						OpenIoT.getServer().getDeviceManagement().listDeviceGroups(includeDeleted, criteria);
			} else {
				results =
						OpenIoT.getServer().getDeviceManagement().listDeviceGroupsWithRole(role,
								includeDeleted, criteria);
			}
			List<IDeviceGroup> groupsConv = new ArrayList<IDeviceGroup>();
			for (IDeviceGroup group : results.getResults()) {
				groupsConv.add(DeviceGroup.copy(group));
			}
			return new SearchResults<IDeviceGroup>(groupsConv, results.getNumResults());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List elements from a device group that meet the given criteria.
	 * 
	 * @param groupToken
	 * @param page
	 * @param pageSize
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{groupToken}/elements", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List elements from a device group")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceGroupElement> listDeviceGroupElements(
			@ApiParam(value = "Unique token that identifies device group", required = true) @PathVariable String groupToken,
			@ApiParam(value = "Include detailed element information", required = false) @RequestParam(defaultValue = "false") boolean includeDetails,
			@ApiParam(value = "Page Number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDeviceGroupElements", LOGGER);
		try {
			DeviceGroupElementMarshalHelper helper =
					new DeviceGroupElementMarshalHelper().setIncludeDetails(includeDetails);
			SearchCriteria criteria = new SearchCriteria(page, pageSize);
			ISearchResults<IDeviceGroupElement> results =
					OpenIoT.getServer().getDeviceManagement().listDeviceGroupElements(groupToken, criteria);
			List<IDeviceGroupElement> elmConv = new ArrayList<IDeviceGroupElement>();
			for (IDeviceGroupElement elm : results.getResults()) {
				elmConv.add(helper.convert(elm, OpenIoT.getServer().getAssetModuleManager()));
			}
			return new SearchResults<IDeviceGroupElement>(elmConv, results.getNumResults());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Add a list of device group elements to an existing group.
	 * 
	 * @param groupToken
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{groupToken}/elements", method = RequestMethod.PUT)
	@ResponseBody
	@ApiOperation(value = "Add elements to a device group")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public List<IDeviceGroupElement> addDeviceGroupElements(
			@ApiParam(value = "Unique token that identifies device group", required = true) @PathVariable String groupToken,
			@RequestBody List<DeviceGroupElementCreateRequest> request) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "addDeviceGroupElements", LOGGER);
		try {
			DeviceGroupElementMarshalHelper helper =
					new DeviceGroupElementMarshalHelper().setIncludeDetails(false);
			List<IDeviceGroupElementCreateRequest> elements =
					(List<IDeviceGroupElementCreateRequest>) (List<? extends IDeviceGroupElementCreateRequest>) request;
			List<IDeviceGroupElement> results =
					OpenIoT.getServer().getDeviceManagement().addDeviceGroupElements(groupToken, elements);
			List<IDeviceGroupElement> retval = new ArrayList<IDeviceGroupElement>();
			for (IDeviceGroupElement elm : results) {
				retval.add(helper.convert(elm, OpenIoT.getServer().getAssetModuleManager()));
			}
			return retval;
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Delete a list of elements from an existing device group.
	 * 
	 * @param groupToken
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{groupToken}/elements", method = RequestMethod.DELETE)
	@ResponseBody
	@ApiOperation(value = "Delete elements from a device group")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public List<IDeviceGroupElement> deleteDeviceGroupElements(
			@ApiParam(value = "Unique token that identifies device group", required = true) @PathVariable String groupToken,
			@RequestBody List<DeviceGroupElementCreateRequest> request) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "deleteDeviceGroupElements", LOGGER);
		try {
			DeviceGroupElementMarshalHelper helper =
					new DeviceGroupElementMarshalHelper().setIncludeDetails(false);
			List<IDeviceGroupElementCreateRequest> elements =
					(List<IDeviceGroupElementCreateRequest>) (List<? extends IDeviceGroupElementCreateRequest>) request;
			List<IDeviceGroupElement> results =
					OpenIoT.getServer().getDeviceManagement().removeDeviceGroupElements(groupToken,
							elements);
			List<IDeviceGroupElement> retval = new ArrayList<IDeviceGroupElement>();
			for (IDeviceGroupElement elm : results) {
				retval.add(helper.convert(elm, OpenIoT.getServer().getAssetModuleManager()));
			}
			return retval;
		} finally {
			Tracer.stop(LOGGER);
		}
	}
}