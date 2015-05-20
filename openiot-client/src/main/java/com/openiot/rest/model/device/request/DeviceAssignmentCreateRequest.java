/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.request;

import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.spi.device.DeviceAssignmentType;
import com.openiot.spi.device.request.IDeviceAssignmentCreateRequest;

import java.io.Serializable;

/**
 * Holds fields needed to create a device assignment.
 * 
 * @author Derek Adams
 */
public class DeviceAssignmentCreateRequest extends MetadataProvider implements
		IDeviceAssignmentCreateRequest, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = -6880578458870122016L;

	/** Device hardware id */
	private String deviceHardwareId;

	/** Type of assignment */
	private DeviceAssignmentType assignmentType;

	/** Asset module id */
	private String assetModuleId;

	/** Unique asset id */
	private String assetId;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceAssignmentCreateRequest#getDeviceHardwareId
	 * ()
	 */
	public String getDeviceHardwareId() {
		return deviceHardwareId;
	}

	public void setDeviceHardwareId(String deviceHardwareId) {
		this.deviceHardwareId = deviceHardwareId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceAssignmentCreateRequest#getAssignmentType()
	 */
	public DeviceAssignmentType getAssignmentType() {
		return assignmentType;
	}

	public void setAssignmentType(DeviceAssignmentType assignmentType) {
		this.assignmentType = assignmentType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceAssignmentCreateRequest#getAssetModuleId()
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
	 * @see IDeviceAssignmentCreateRequest#getAssetId()
	 */
	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}
}