/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.cache.ICache;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;

/**
 * Interface for entity that provides caching for device management objects.
 * 
 * @author Derek
 */
public interface IDeviceManagementCacheProvider extends ILifecycleComponent {

	/**
	 * Gets cache mapping site tokens to {@link ISite} objects.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ICache<String, ISite> getSiteCache() throws OpenIoTException;

	/**
	 * Gets cache mapping specification tokens for {@link IDeviceSpecification} objects.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ICache<String, IDeviceSpecification> getDeviceSpecificationCache() throws OpenIoTException;

	/**
	 * Gets cache mapping hardware ids to {@link IDevice} objects.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ICache<String, IDevice> getDeviceCache() throws OpenIoTException;

	/**
	 * Get cache mapping assignment tokens to {@link IDeviceAssignment} objects.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ICache<String, IDeviceAssignment> getDeviceAssignmentCache() throws OpenIoTException;
}