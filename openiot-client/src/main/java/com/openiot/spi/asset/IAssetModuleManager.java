/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.asset;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.command.ICommandResponse;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;

import java.util.List;

/**
 * Interface for interacting with the asset module manager.
 * 
 * @author dadams
 */
public interface IAssetModuleManager extends ILifecycleComponent {

	/**
	 * Get the list of asset modules.
	 * 
	 * @return
	 */
	public List<IAssetModule<?>> getModules();

	/**
	 * Calls the refresh method on each asset module and returns a list of responses.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<ICommandResponse> refreshModules() throws OpenIoTException;

	/**
	 * Finds an asset in a given module.
	 * 
	 * @param assetModuleId
	 * @param id
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IAsset getAssetById(String assetModuleId, String id) throws OpenIoTException;

	/**
	 * Search an asset module for assets matching the given criteria.
	 * 
	 * @param assetModuleId
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<? extends IAsset> search(String assetModuleId, String criteria) throws OpenIoTException;
}