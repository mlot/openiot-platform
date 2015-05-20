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
import com.openiot.device.marshaling.DeviceSpecificationMarshalHelper;
import com.openiot.device.provisioning.protobuf.SpecificationProtoBuilder;
import com.openiot.rest.model.device.command.DeviceCommandNamespace;
import com.openiot.rest.model.device.request.DeviceCommandCreateRequest;
import com.openiot.rest.model.device.request.DeviceSpecificationCreateRequest;
import com.openiot.rest.model.search.SearchCriteria;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.asset.IAsset;
import com.openiot.spi.device.IDeviceSpecification;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.command.IDeviceCommandNamespace;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.search.ISearchResults;
import com.openiot.spi.server.debug.TracerCategory;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Controller for device specification operations.
 * 
 * @author Derek Adams
 */
@Controller
@RequestMapping(value = "/specifications")
@Api(value = "specifications", description = "Operations related to OpenIoT device specifications.")
public class SpecificationsController extends OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(SpecificationsController.class);

	/**
	 * Create a device specification.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create a new device specification")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceSpecification createDeviceSpecification(
			@RequestBody DeviceSpecificationCreateRequest request) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createDeviceSpecification", LOGGER);
		try {
			IAsset asset =
					OpenIoT.getServer().getAssetModuleManager().getAssetById(request.getAssetModuleId(),
							request.getAssetId());
			if (asset == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidAssetReferenceId, ErrorLevel.ERROR,
						HttpServletResponse.SC_NOT_FOUND);
			}
			IDeviceSpecification result =
					OpenIoT.getServer().getDeviceManagement().createDeviceSpecification(request);
			DeviceSpecificationMarshalHelper helper = new DeviceSpecificationMarshalHelper();
			helper.setIncludeAsset(true);
			return helper.convert(result, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get a device specification by unique token.
	 * 
	 * @param hardwareId
	 * @return
	 */
	@RequestMapping(value = "/{token}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a device specification by unique token")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceSpecification getDeviceSpecificationByToken(
			@ApiParam(value = "Token", required = true) @PathVariable String token,
			@ApiParam(value = "Include detailed asset information", required = false) @RequestParam(defaultValue = "true") boolean includeAsset)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getDeviceSpecificationByToken", LOGGER);
		try {
			IDeviceSpecification result = assertDeviceSpecificationByToken(token);
			DeviceSpecificationMarshalHelper helper = new DeviceSpecificationMarshalHelper();
			helper.setIncludeAsset(includeAsset);
			return helper.convert(result, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get a device specification by unique token.
	 * 
	 * @param hardwareId
	 * @return
	 */
	@RequestMapping(value = "/{token}/proto", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a device specification by unique token")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public String getDeviceSpecificationProtoByToken(
			@ApiParam(value = "Token", required = true) @PathVariable String token,
			HttpServletResponse response) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getDeviceSpecificationProtoByToken", LOGGER);
		try {
			IDeviceSpecification specification = assertDeviceSpecificationByToken(token);
			String proto = SpecificationProtoBuilder.getProtoForSpecification(specification);
			response.setContentType("text/plain");
			return proto;
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get a device specification by unique token.
	 * 
	 * @param hardwareId
	 * @return
	 */
	@RequestMapping(value = "/{token}/spec.proto", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a device specification by unique token")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ResponseEntity<byte[]> getDeviceSpecificationProtoFileByToken(
			@ApiParam(value = "Token", required = true) @PathVariable String token,
			HttpServletResponse response) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getDeviceSpecificationProtoFileByToken", LOGGER);
		try {
			IDeviceSpecification specification = assertDeviceSpecificationByToken(token);
			String proto = SpecificationProtoBuilder.getProtoForSpecification(specification);

			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.set("Content-Disposition", "attachment; filename=Spec_" + specification.getToken()
					+ ".proto");
			return new ResponseEntity<byte[]>(proto.getBytes(), headers, HttpStatus.OK);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Update an existing device specification.
	 * 
	 * @param token
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}", method = RequestMethod.PUT)
	@ResponseBody
	@ApiOperation(value = "Update device specification information")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceSpecification updateDeviceSpecification(
			@ApiParam(value = "Token", required = true) @PathVariable String token,
			@RequestBody DeviceSpecificationCreateRequest request) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "updateDeviceSpecification", LOGGER);
		try {
			IDeviceSpecification result =
					OpenIoT.getServer().getDeviceManagement().updateDeviceSpecification(token, request);
			DeviceSpecificationMarshalHelper helper = new DeviceSpecificationMarshalHelper();
			helper.setIncludeAsset(true);
			return helper.convert(result, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List device specifications that meet the given criteria.
	 * 
	 * @param includeDeleted
	 * @param includeAsset
	 * @param page
	 * @param pageSize
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List device specifications that match certain criteria")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceSpecification> listDeviceSpecifications(
			@ApiParam(value = "Include deleted", required = false) @RequestParam(defaultValue = "false") boolean includeDeleted,
			@ApiParam(value = "Include detailed asset information", required = false) @RequestParam(defaultValue = "true") boolean includeAsset,
			@ApiParam(value = "Page Number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDeviceSpecifications", LOGGER);
		try {
			SearchCriteria criteria = new SearchCriteria(page, pageSize);
			ISearchResults<IDeviceSpecification> results =
					OpenIoT.getServer().getDeviceManagement().listDeviceSpecifications(includeDeleted,
							criteria);
			DeviceSpecificationMarshalHelper helper = new DeviceSpecificationMarshalHelper();
			helper.setIncludeAsset(includeAsset);
			List<IDeviceSpecification> specsConv = new ArrayList<IDeviceSpecification>();
			for (IDeviceSpecification device : results.getResults()) {
				specsConv.add(helper.convert(device, OpenIoT.getServer().getAssetModuleManager()));
			}
			Collections.sort(specsConv, new Comparator<IDeviceSpecification>() {
				public int compare(IDeviceSpecification o1, IDeviceSpecification o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			return new SearchResults<IDeviceSpecification>(specsConv, results.getNumResults());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Delete an existing device specification.
	 * 
	 * @param token
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}", method = RequestMethod.DELETE)
	@ResponseBody
	@ApiOperation(value = "Delete a device specification based on token")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceSpecification deleteDeviceSpecification(
			@ApiParam(value = "Token", required = true) @PathVariable String token,
			@ApiParam(value = "Delete permanently", required = false) @RequestParam(defaultValue = "false") boolean force)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "deleteDeviceSpecification", LOGGER);
		try {
			IDeviceSpecification result =
					OpenIoT.getServer().getDeviceManagement().deleteDeviceSpecification(token, force);
			DeviceSpecificationMarshalHelper helper = new DeviceSpecificationMarshalHelper();
			helper.setIncludeAsset(true);
			return helper.convert(result, OpenIoT.getServer().getAssetModuleManager());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Create a device specification.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{token}/commands", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create a new device command for a specification.")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IDeviceCommand createDeviceCommand(
			@ApiParam(value = "Token", required = true) @PathVariable String token,
			@RequestBody DeviceCommandCreateRequest request) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createDeviceCommand", LOGGER);
		try {
			IDeviceSpecification spec = assertDeviceSpecificationByToken(token);
			IDeviceCommand result =
					OpenIoT.getServer().getDeviceManagement().createDeviceCommand(spec, request);
			return result;
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	@RequestMapping(value = "/{token}/commands", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List device commands for a specification")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceCommand> listDeviceCommands(
			@ApiParam(value = "Token", required = true) @PathVariable String token,
			@ApiParam(value = "Include deleted", required = false) @RequestParam(defaultValue = "false") boolean includeDeleted)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDeviceCommands", LOGGER);
		try {
			List<IDeviceCommand> results =
					OpenIoT.getServer().getDeviceManagement().listDeviceCommands(token, includeDeleted);
			Collections.sort(results, new Comparator<IDeviceCommand>() {
				public int compare(IDeviceCommand o1, IDeviceCommand o2) {
					if (o1.getName().equals(o2.getName())) {
						return o1.getNamespace().compareTo(o2.getNamespace());
					}
					return o1.getName().compareTo(o2.getName());
				}
			});
			return new SearchResults<IDeviceCommand>(results);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List commands grouped by namespace.
	 * 
	 * @param token
	 * @param includeDeleted
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{token}/namespaces", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List device command namespaces for a specification")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IDeviceCommandNamespace> listDeviceCommandsByNamespace(
			@ApiParam(value = "Token", required = true) @PathVariable String token,
			@ApiParam(value = "Include deleted", required = false) @RequestParam(defaultValue = "false") boolean includeDeleted)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDeviceCommandsByNamespace", LOGGER);
		try {
			List<IDeviceCommand> results =
					OpenIoT.getServer().getDeviceManagement().listDeviceCommands(token, includeDeleted);
			Collections.sort(results, new Comparator<IDeviceCommand>() {
				public int compare(IDeviceCommand o1, IDeviceCommand o2) {
					if ((o1.getNamespace() == null) && (o2.getNamespace() != null)) {
						return -1;
					}
					if ((o1.getNamespace() != null) && (o2.getNamespace() == null)) {
						return 1;
					}
					if ((o1.getNamespace() == null) && (o2.getNamespace() == null)) {
						return o1.getName().compareTo(o2.getName());
					}
					if (!o1.getNamespace().equals(o2.getNamespace())) {
						return o1.getNamespace().compareTo(o2.getNamespace());
					}
					return o1.getName().compareTo(o2.getName());
				}
			});
			List<IDeviceCommandNamespace> namespaces = new ArrayList<IDeviceCommandNamespace>();
			DeviceCommandNamespace current = null;
			for (IDeviceCommand command : results) {
				if ((current == null)
						|| ((current.getValue() == null) && (command.getNamespace() != null))
						|| ((current.getValue() != null) && (!current.getValue().equals(
								command.getNamespace())))) {
					current = new DeviceCommandNamespace();
					current.setValue(command.getNamespace());
					namespaces.add(current);
				}
				current.getCommands().add(command);
			}
			return new SearchResults<IDeviceCommandNamespace>(namespaces);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Gets a device specification by token and throws an exception if not found.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected IDeviceSpecification assertDeviceSpecificationByToken(String token) throws OpenIoTException {
		IDeviceSpecification result =
				OpenIoT.getServer().getDeviceManagement().getDeviceSpecificationByToken(token);
		if (result == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceSpecificationToken, ErrorLevel.ERROR,
					HttpServletResponse.SC_NOT_FOUND);
		}
		return result;
	}
}