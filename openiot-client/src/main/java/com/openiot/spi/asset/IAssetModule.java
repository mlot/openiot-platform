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
 * Interface for a module that provides access to one or more asset types.
 * 
 * @author dadams
 */
public interface IAssetModule<T extends IAsset> extends ILifecycleComponent {

	/**
	 * Get the unique module identifier.
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * Get the module name.
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Indicates the type of assets provided.
	 * 
	 * @return
	 */
	public AssetType getAssetType();

	/**
	 * Get an asset by unique id.
	 * 
	 * @param id
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public T getAssetById(String id) throws OpenIoTException;

	/**
	 * Search for all assets of a given type that meet the criteria.
	 * 
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<T> search(String criteria) throws OpenIoTException;

	/**
	 * Refresh any cached data in the module.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ICommandResponse refresh() throws OpenIoTException;
}