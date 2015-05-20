/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.group;

import com.openiot.spi.common.IMetadataProviderEntity;

import java.util.List;

/**
 * Interface for a group of related devices.
 * 
 * @author Derek
 */
public interface IDeviceGroup extends IMetadataProviderEntity {

	/**
	 * Get the unique group token.
	 * 
	 * @return
	 */
	public String getToken();

	/**
	 * Get the group name.
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Get the group description.
	 * 
	 * @return
	 */
	public String getDescription();

	/**
	 * Get list of roles associated with element.
	 * 
	 * @return
	 */
	public List<String> getRoles();
}