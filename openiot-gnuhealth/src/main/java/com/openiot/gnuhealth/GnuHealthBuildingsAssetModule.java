/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.gnuhealth;

import com.openiot.rest.model.asset.LocationAsset;
import com.openiot.server.asset.AssetMatcher;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.asset.AssetType;
import com.openiot.spi.asset.IAssetModule;
import com.openiot.spi.asset.ILocationAsset;
import com.openiot.spi.command.ICommandResponse;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Asset module that represents GNU Health buildings as {@link ILocationAsset}.
 * 
 * @author Derek
 */
public class GnuHealthBuildingsAssetModule extends LifecycleComponent implements IAssetModule<LocationAsset> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(GnuHealthBuildingsAssetModule.class);

	/** Module id */
	private static final String MODULE_ID = "gnuhealth-buildings";

	/** Module name */
	private static final String MODULE_NAME = "GNU Health - Buildings";

	/** Unique module id */
	private String moduleId = MODULE_ID;

	/** Module name */
	private String moduleName = MODULE_NAME;

	/** Common GNU Health configuration */
	private GnuHealthConfiguration configuration;

	/** Used to find search results */
	private AssetMatcher matcher = new AssetMatcher();

	public GnuHealthBuildingsAssetModule() {
		super(LifecycleComponentType.AssetModule);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		// Trigger data caching if not already done.
		getConfiguration().getGnuHealthData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#getLogger()
	 */
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModule#getAssetById(java.lang.String)
	 */
	@Override
	public LocationAsset getAssetById(String id) throws OpenIoTException {
		return getConfiguration().getGnuHealthData().getCachedBuildings().get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModule#search(java.lang.String)
	 */
	@Override
	public List<LocationAsset> search(String criteria) throws OpenIoTException {
		criteria = criteria.toLowerCase();
		List<LocationAsset> results = new ArrayList<LocationAsset>();
		Map<String, LocationAsset> cache = getConfiguration().getGnuHealthData().getCachedBuildings();
		if (criteria.length() == 0) {
			results.addAll(cache.values());
			return results;
		}
		for (LocationAsset asset : cache.values()) {
			if (matcher.isLocationMatch(asset, criteria)) {
				results.add(asset);
			}
		}
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModule#refresh()
	 */
	@Override
	public ICommandResponse refresh() throws OpenIoTException {
		return getConfiguration().getGnuHealthData().refreshBuildings();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModule#getId()
	 */
	@Override
	public String getId() {
		return getModuleId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModule#getName()
	 */
	@Override
	public String getName() {
		return getModuleName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModule#getAssetType()
	 */
	@Override
	public AssetType getAssetType() {
		return AssetType.Location;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public GnuHealthConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(GnuHealthConfiguration configuration) {
		this.configuration = configuration;
	}
}