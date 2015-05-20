/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.group;

import com.openiot.rest.model.common.MetadataProviderEntity;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.group.IDeviceGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model object for a device group.
 * 
 * @author Derek
 */
public class DeviceGroup extends MetadataProviderEntity implements IDeviceGroup, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = -4993194128786517276L;

	/** Unique token */
	private String token;

	/** Group name */
	private String name;

	/** Group description */
	private String description;

	/** List of roles */
	private List<String> roles = new ArrayList<String>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceGroup#getToken()
	 */
	@Override
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceGroup#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceGroup#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceGroup#getRoles()
	 */
	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public static DeviceGroup copy(IDeviceGroup input) throws OpenIoTException {
		DeviceGroup result = new DeviceGroup();
		result.setToken(input.getToken());
		result.setName(input.getName());
		result.setDescription(input.getDescription());
		result.getRoles().addAll(input.getRoles());
		MetadataProviderEntity.copy(input, result);
		return result;
	}
}