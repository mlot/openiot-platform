/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.request;

import com.openiot.spi.common.IMetadataProvider;
import com.openiot.spi.device.DeviceStatus;
import com.openiot.spi.device.IDeviceElementMapping;

import java.util.List;

/**
 * Interface for arguments needed to create a device.
 * 
 * @author Derek
 */
public interface IDeviceCreateRequest extends IMetadataProvider {

	/**
	 * Get the unique device hardware id.
	 * 
	 * @return
	 */
	public String getHardwareId();

	/**
	 * Get the site token.
	 * 
	 * @return
	 */
	public String getSiteToken();

	/**
	 * Get the device specification token.
	 * 
	 * @return
	 */
	public String getSpecificationToken();

	/**
	 * Get the parent hardware id (if nested).
	 * 
	 * @return
	 */
	public String getParentHardwareId();

	/**
	 * Indicates whether parent reference should be removed.
	 * 
	 * @return
	 */
	public boolean isRemoveParentHardwareId();

	/**
	 * Get the list of device element mappings.
	 * 
	 * @return
	 */
	public List<IDeviceElementMapping> getDeviceElementMappings();

	/**
	 * Get comments associated with device.
	 * 
	 * @return
	 */
	public String getComments();

	/**
	 * Get device status indicator.
	 * 
	 * @return
	 */
	public DeviceStatus getStatus();
}