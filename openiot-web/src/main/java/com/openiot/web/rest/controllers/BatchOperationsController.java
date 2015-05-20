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
import com.openiot.device.batch.BatchUtils;
import com.openiot.rest.model.device.batch.BatchOperation;
import com.openiot.rest.model.device.request.BatchCommandForCriteriaRequest;
import com.openiot.rest.model.device.request.BatchCommandInvocationRequest;
import com.openiot.rest.model.search.SearchCriteria;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.rest.model.search.device.BatchElementSearchCriteria;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.batch.IBatchElement;
import com.openiot.spi.device.batch.IBatchOperation;
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
 * Controller for batch operations.
 * 
 * @author Derek Adams
 */
@Controller
@RequestMapping(value = "/batch")
@Api(value = "batch", description = "Operations related to OpenIoT batch operations.")
public class BatchOperationsController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(BatchOperationsController.class);

	@RequestMapping(value = "/{batchToken}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a batch operation by unique token")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IBatchOperation getBatchOperationByToken(
			@ApiParam(value = "Unique token that identifies batch operation", required = true) @PathVariable String batchToken)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getBatchOperationByToken", LOGGER);
		try {
			IBatchOperation batch = OpenIoT.getServer().getDeviceManagement().getBatchOperation(batchToken);
			if (batch == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidBatchOperationToken, ErrorLevel.ERROR);
			}
			return BatchOperation.copy(batch);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List all batch operations")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IBatchOperation> listBatchOperations(
			@ApiParam(value = "Include deleted", required = false) @RequestParam(defaultValue = "false") boolean includeDeleted,
			@ApiParam(value = "Page Number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDeviceGroups", LOGGER);
		try {
			SearchCriteria criteria = new SearchCriteria(page, pageSize);
			ISearchResults<IBatchOperation> results =
					OpenIoT.getServer().getDeviceManagement().listBatchOperations(includeDeleted, criteria);
			List<IBatchOperation> opsConv = new ArrayList<IBatchOperation>();
			for (IBatchOperation op : results.getResults()) {
				opsConv.add(BatchOperation.copy(op));
			}
			return new SearchResults<IBatchOperation>(opsConv, results.getNumResults());
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	@RequestMapping(value = "/{operationToken}/elements", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List elements from a batch operation")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public ISearchResults<IBatchElement> listBatchOperationElements(
			@ApiParam(value = "Unique token that identifies batch operation", required = true) @PathVariable String operationToken,
			@ApiParam(value = "Page Number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDeviceGroupElements", LOGGER);
		try {
			BatchElementSearchCriteria criteria = new BatchElementSearchCriteria(page, pageSize);
			ISearchResults<IBatchElement> results =
					OpenIoT.getServer().getDeviceManagement().listBatchElements(operationToken, criteria);
			return results;
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	@RequestMapping(value = "/command", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create a new batch command invocation")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IBatchOperation createBatchCommandInvocation(@RequestBody BatchCommandInvocationRequest request)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createBatchCommandInvocation", LOGGER);
		try {
			IBatchOperation result =
					OpenIoT.getServer().getDeviceManagement().createBatchCommandInvocation(request);
			return BatchOperation.copy(result);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	@RequestMapping(value = "/command/criteria", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create a new batch command invocation based on criteria")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IBatchOperation createBatchCommandByCriteria(@RequestBody BatchCommandForCriteriaRequest request)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createBatchCommandByCriteria", LOGGER);
		try {
			// Resolve hardware ids for devices matching criteria.
			List<String> hardwareIds = BatchUtils.getHardwareIds(request);

			// Create batch command invocation.
			BatchCommandInvocationRequest invoke = new BatchCommandInvocationRequest();
			invoke.setToken(request.getToken());
			invoke.setCommandToken(request.getCommandToken());
			invoke.setParameterValues(request.getParameterValues());
			invoke.setHardwareIds(hardwareIds);

			IBatchOperation result =
					OpenIoT.getServer().getDeviceManagement().createBatchCommandInvocation(invoke);
			return BatchOperation.copy(result);
		} finally {
			Tracer.stop(LOGGER);
		}
	}
}