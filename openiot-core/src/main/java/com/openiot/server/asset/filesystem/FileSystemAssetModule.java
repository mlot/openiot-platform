/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.server.asset.filesystem;

import com.openiot.configuration.TomcatConfigurationResolver;
import com.openiot.rest.model.asset.Asset;
import com.openiot.rest.model.command.CommandResponse;
import com.openiot.server.asset.AssetMatcher;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.asset.IAssetModule;
import com.openiot.spi.command.CommandResult;
import com.openiot.spi.command.ICommandResponse;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for asset modules that load asset information from the filesystem.
 * 
 * @author Derek
 *
 * @param <T>
 */
public abstract class FileSystemAssetModule<T extends Asset> extends LifecycleComponent implements
		IAssetModule<T> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(FileSystemAssetModule.class);

	/** Map of assets by unique id */
	protected Map<String, T> assetsById;

	/** Matcher used for searches */
	protected AssetMatcher matcher = new AssetMatcher();

	/** Filename used to load assets */
	private String filename;

	/** Module id */
	private String moduleId;

	/** Module name */
	private String moduleName;

	public FileSystemAssetModule(String filename, String moduleId, String moduleName) {
		super(LifecycleComponentType.AssetModule);
		this.filename = filename;
		this.moduleId = moduleId;
		this.moduleName = moduleName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	public void start() throws OpenIoTException {
		reload();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	public void stop() throws OpenIoTException {
	}

	/**
	 * Reloads list of person assets from the filesystem.
	 */
	protected void reload() throws OpenIoTException {
		File config = TomcatConfigurationResolver.getOpenIoTConfigFolder();
		File assetsFolder = new File(config, IFileSystemAssetModuleConstants.ASSETS_FOLDER);
		if (!assetsFolder.exists()) {
			throw new OpenIoTException("Assets subfolder not found. Looking for: "
					+ assetsFolder.getAbsolutePath());
		}
		File configFile = new File(assetsFolder, getFilename());
		if (!configFile.exists()) {
			throw new OpenIoTException("Asset module file missing. Looking for: "
					+ configFile.getAbsolutePath());
		}
		LOGGER.info("Loading assets from: " + configFile.getAbsolutePath());

		// Unmarshal assets from XML file and store in data object.
		List<T> assets = unmarshal(configFile);
		this.assetsById = new HashMap<String, T>();
		for (T asset : assets) {
			assetsById.put(asset.getId(), asset);
		}
		showLoadResults();
	}

	/**
	 * Log the number of assets loaded for each type.
	 */
	protected void showLoadResults() {
		String message = "Loaded " + assetsById.size() + " assets.";
		LOGGER.info(message);
	}

	/**
	 * Implemented in subclasses to unmarshal file into assets.
	 * 
	 * @param file
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected abstract List<T> unmarshal(File file) throws OpenIoTException;

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
	 * @see IAssetModule#getId()
	 */
	public String getId() {
		return getModuleId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModule#getName()
	 */
	public String getName() {
		return getModuleName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModule#getAssetById(java.lang.String)
	 */
	public T getAssetById(String id) throws OpenIoTException {
		return assetsById.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModule#search(java.lang.String)
	 */
	public List<T> search(String criteria) throws OpenIoTException {
		criteria = criteria.toLowerCase();
		List<T> results = new ArrayList<T>();
		if (criteria.length() == 0) {
			results.addAll(assetsById.values());
			return results;
		}
		for (T asset : assetsById.values()) {
			if (matcher.isMatch(getAssetType(), asset, criteria)) {
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
	public ICommandResponse refresh() throws OpenIoTException {
		try {
			reload();
			showLoadResults();
			return new CommandResponse(CommandResult.Successful, "Refresh successful.");
		} catch (OpenIoTException e) {
			return new CommandResponse(CommandResult.Failed, e.getMessage());
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
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
}