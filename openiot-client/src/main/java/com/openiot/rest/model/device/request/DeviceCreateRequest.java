/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.request;

import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.rest.model.device.DeviceElementMapping;
import com.openiot.spi.device.DeviceStatus;
import com.openiot.spi.device.IDeviceElementMapping;
import com.openiot.spi.device.request.IDeviceCreateRequest;

import java.io.Serializable;
import java.util.List;

/**
 * Holds fields needed to create a new device.
 * 
 * @author Derek Adams
 */
public class DeviceCreateRequest extends MetadataProvider implements IDeviceCreateRequest, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = 5102270168736590229L;

	/** Hardware id for new device */
	private String hardwareId;

	/** Site token */
	private String siteToken;

	/** Device specification token */
	private String specificationToken;

	/** Parent hardware id (if nested) */
	private String parentHardwareId;

	/** Indicates whether parent hardware id should be removed */
	private boolean removeParentHardwareId = false;

	/** List of device element mappings */
	private List<DeviceElementMapping> deviceElementMappings;

	/** Comments */
	private String comments;

	/** Device status indicator */
	private DeviceStatus status;

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCreateRequest#getHardwareId()
	 */
	public String getHardwareId() {
		return hardwareId;
	}

	public void setHardwareId(String hardwareId) {
		this.hardwareId = hardwareId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCreateRequest#getSiteToken()
	 */
	public String getSiteToken() {
		return siteToken;
	}

	public void setSiteToken(String siteToken) {
		this.siteToken = siteToken;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCreateRequest#getSpecificationToken()
	 */
	public String getSpecificationToken() {
		return specificationToken;
	}

	public void setSpecificationToken(String specificationToken) {
		this.specificationToken = specificationToken;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCreateRequest#getParentHardwareId()
	 */
	public String getParentHardwareId() {
		return parentHardwareId;
	}

	public void setParentHardwareId(String parentHardwareId) {
		this.parentHardwareId = parentHardwareId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceCreateRequest#isRemoveParentHardwareId()
	 */
	public boolean isRemoveParentHardwareId() {
		return removeParentHardwareId;
	}

	public void setRemoveParentHardwareId(boolean removeParentHardwareId) {
		this.removeParentHardwareId = removeParentHardwareId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceCreateRequest#getDeviceElementMappings()
	 */
	@SuppressWarnings("unchecked")
	public List<IDeviceElementMapping> getDeviceElementMappings() {
		return (List<IDeviceElementMapping>) (List<? extends IDeviceElementMapping>) deviceElementMappings;
	}

	public void setDeviceElementMappings(List<DeviceElementMapping> deviceElementMappings) {
		this.deviceElementMappings = deviceElementMappings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCreateRequest#getComments()
	 */
	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCreateRequest#getStatus()
	 */
	public DeviceStatus getStatus() {
		return status;
	}

	public void setStatus(DeviceStatus status) {
		this.status = status;
	}
}