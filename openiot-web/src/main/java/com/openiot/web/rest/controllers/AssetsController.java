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
import com.openiot.rest.model.asset.AssetModule;
import com.openiot.rest.model.command.CommandResponse;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.asset.AssetType;
import com.openiot.spi.asset.IAsset;
import com.openiot.spi.asset.IAssetModule;
import com.openiot.spi.command.ICommandResponse;
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
 * Controller for site operations.
 * 
 * @author Derek Adams
 */
@Controller
@RequestMapping(value = "/assets")
@Api(value = "assets", description = "Operations related to OpenIoT assets.")
public class AssetsController extends OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(AssetsController.class);

	/**
	 * Search for assets in an {@link IAssetModule} that meet the given criteria.
	 * 
	 * @param assetModuleId
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{assetModuleId}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Search hardware assets")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public SearchResults<? extends IAsset> searchAssets(
			@ApiParam(value = "Unique asset module id", required = true) @PathVariable String assetModuleId,
			@ApiParam(value = "Criteria for search", required = false) @RequestParam(defaultValue = "") String criteria)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "searchAssets", LOGGER);
		List<? extends IAsset> found =
				OpenIoT.getServer().getAssetModuleManager().search(assetModuleId, criteria);
		SearchResults<? extends IAsset> results = new SearchResults(found);
		Tracer.stop(LOGGER);
		return results;
	}

	/**
	 * Get an asset from an {@link IAssetModule} by unique id.
	 * 
	 * @param assetModuleId
	 * @param assetId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{assetModuleId}/{assetId}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Find hardware asset by unique id")
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public IAsset getAssetById(
			@ApiParam(value = "Unique asset module id", required = true) @PathVariable String assetModuleId,
			@ApiParam(value = "Unique asset id", required = true) @PathVariable String assetId)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getAssetById", LOGGER);
		try {
			return OpenIoT.getServer().getAssetModuleManager().getAssetById(assetModuleId, assetId);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List all asset modules.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/modules", method = RequestMethod.GET)
	@ResponseBody
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public List<AssetModule> listAssetModules() throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listAssetModules", LOGGER);
		try {
			List<AssetModule> amConverted = new ArrayList<AssetModule>();
			List<IAssetModule<?>> modules = OpenIoT.getServer().getAssetModuleManager().getModules();
			for (IAssetModule<?> module : modules) {
				amConverted.add(AssetModule.copy(module));
			}
			return amConverted;
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List all asset modules that contain device assets.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/modules/devices", method = RequestMethod.GET)
	@ResponseBody
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public List<AssetModule> listDeviceAssetModules() throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listDeviceAssetModules", LOGGER);
		try {
			List<AssetModule> amConverted = new ArrayList<AssetModule>();
			List<IAssetModule<?>> modules = OpenIoT.getServer().getAssetModuleManager().getModules();
			for (IAssetModule<?> module : modules) {
				if (module.getAssetType() == AssetType.Device) {
					amConverted.add(AssetModule.copy(module));
				}
			}
			return amConverted;
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Refresh all asset modules.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/modules/refresh", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ SitewhereRoles.ROLE_AUTHENTICATED_USER })
	public List<CommandResponse> refreshModules() throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "refreshModules", LOGGER);
		try {
			List<ICommandResponse> responses = OpenIoT.getServer().getAssetModuleManager().refreshModules();
			List<CommandResponse> converted = new ArrayList<CommandResponse>();
			for (ICommandResponse response : responses) {
				converted.add(CommandResponse.copy(response));
			}
			return converted;
		} finally {
			Tracer.stop(LOGGER);
		}
	}
}