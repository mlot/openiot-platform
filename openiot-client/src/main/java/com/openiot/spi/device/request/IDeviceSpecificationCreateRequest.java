/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.request;

import com.openiot.spi.common.IMetadataProvider;
import com.openiot.spi.device.DeviceContainerPolicy;
import com.openiot.spi.device.element.IDeviceElementSchema;

/**
 * Interface for arguments needed to create a device specification.
 * 
 * @author Derek
 */
public interface IDeviceSpecificationCreateRequest extends IMetadataProvider {

	/**
	 * Get name that describes specification.
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Get id for asset module.
	 * 
	 * @return
	 */
	public String getAssetModuleId();

	/**
	 * Get id for specification asset type.
	 * 
	 * @return
	 */
	public String getAssetId();

	/**
	 * Allows the specification id to be specified. (Optional)
	 * 
	 * @return
	 */
	public String getToken();

	/**
	 * Get container policy.
	 * 
	 * @return
	 */
	public DeviceContainerPolicy getContainerPolicy();

	/**
	 * Get {@link com.openiot.spi.device.element.IDeviceElementSchema} for locating nested devices.
	 * 
	 * @return
	 */
	public IDeviceElementSchema getDeviceElementSchema();
}