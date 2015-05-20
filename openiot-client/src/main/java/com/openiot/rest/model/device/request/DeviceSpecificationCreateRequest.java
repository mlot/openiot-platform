/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.request;

import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.rest.model.device.element.DeviceElementSchema;
import com.openiot.spi.device.DeviceContainerPolicy;
import com.openiot.spi.device.element.IDeviceElementSchema;
import com.openiot.spi.device.request.IDeviceSpecificationCreateRequest;

import java.io.Serializable;

/**
 * Holds fields needed to create a new device specification.
 * 
 * @author Derek Adams
 */
public class DeviceSpecificationCreateRequest extends MetadataProvider implements
		IDeviceSpecificationCreateRequest, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = 1L;

	/** Specification name */
	private String name;

	/** Asset module id */
	private String assetModuleId;

	/** Asset id */
	private String assetId;

	/** Specification id (Optional) */
	private String token;

	/** Indicates if device instances can contain nested devices */
	private DeviceContainerPolicy containerPolicy;

	/** Device element schema for specifications that support nested devices */
	private DeviceElementSchema deviceElementSchema;

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceSpecificationCreateRequest#getName()
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceSpecificationCreateRequest#getAssetModuleId
	 * ()
	 */
	public String getAssetModuleId() {
		return assetModuleId;
	}

	public void setAssetModuleId(String assetModuleId) {
		this.assetModuleId = assetModuleId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceSpecificationCreateRequest#getAssetId()
	 */
	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceSpecificationCreateRequest#getToken()
	 */
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceSpecificationCreateRequest#getContainerPolicy
	 * ()
	 */
	public DeviceContainerPolicy getContainerPolicy() {
		return containerPolicy;
	}

	public void setContainerPolicy(DeviceContainerPolicy containerPolicy) {
		this.containerPolicy = containerPolicy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceSpecificationCreateRequest#
	 * getDeviceElementSchema()
	 */
	public IDeviceElementSchema getDeviceElementSchema() {
		return deviceElementSchema;
	}

	public void setDeviceElementSchema(DeviceElementSchema deviceElementSchema) {
		this.deviceElementSchema = deviceElementSchema;
	}
}