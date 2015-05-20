/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.server.asset;

import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.asset.IAsset;
import com.openiot.spi.asset.IAssetModule;
import com.openiot.spi.asset.IAssetModuleManager;
import com.openiot.spi.command.ICommandResponse;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Manages the list of modules
 * 
 * @author dadams
 */
public class AssetModuleManager extends LifecycleComponent implements IAssetModuleManager {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(AssetModuleManager.class);

	/** List of asset modules */
	private List<IAssetModule<?>> modules;

	/** Map of asset modules by unique id */
	private Map<String, IAssetModule<?>> modulesById = new HashMap<String, IAssetModule<?>>();

	public AssetModuleManager() {
		super(LifecycleComponentType.AssetModuleManager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	public void start() throws OpenIoTException {
		modulesById.clear();
		for (IAssetModule<?> module : modules) {
			startNestedComponent(module, true);
			modulesById.put(module.getId(), module);
		}
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
	public void stop() {
		for (IAssetModule<?> module : modules) {
			module.lifecycleStop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModuleManager#getAssetById(java.lang.String,
	 * java.lang.String)
	 */
	public IAsset getAssetById(String assetModuleId, String id) throws OpenIoTException {
		IAssetModule<?> match = assertAssetModule(assetModuleId);
		return match.getAssetById(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModuleManager#search(java.lang.String,
	 * java.lang.String)
	 */
	public List<? extends IAsset> search(String assetModuleId, String criteria) throws OpenIoTException {
		IAssetModule<?> match = assertAssetModule(assetModuleId);
		List<? extends IAsset> results = match.search(criteria);
		Collections.sort(results);
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModuleManager#refreshModules()
	 */
	public List<ICommandResponse> refreshModules() throws OpenIoTException {
		List<ICommandResponse> responses = new ArrayList<ICommandResponse>();
		for (IAssetModule<?> module : modules) {
			responses.add(module.refresh());
		}
		return responses;
	}

	/**
	 * Get asset module by id or throw exception if not found.
	 * 
	 * @param id
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected IAssetModule<?> assertAssetModule(String id) throws OpenIoTException {
		IAssetModule<?> match = modulesById.get(id);
		if (match == null) {
			throw new OpenIoTException("Invalid asset module id: " + id);
		}
		return match;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModuleManager#getModules()
	 */
	public List<IAssetModule<?>> getModules() {
		return modules;
	}

	public void setModules(List<IAssetModule<?>> modules) {
		this.modules = modules;
	}
}