/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.group;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.openiot.rest.model.device.Device;
import com.openiot.spi.device.group.GroupElementType;
import com.openiot.spi.device.group.IDeviceGroup;
import com.openiot.spi.device.group.IDeviceGroupElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model object for an element in an {@link IDeviceGroup}.
 * 
 * @author Derek
 */
@JsonInclude(Include.NON_NULL)
public class DeviceGroupElement implements IDeviceGroupElement, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = -5565956152579362877L;

	/** Parent group token */
	private String groupToken;

	/** Element index */
	private long index;

	/** Element type */
	private GroupElementType type;

	/** Element type */
	private String elementId;

	/** List of roles for the element */
	private List<String> roles = new ArrayList<String>();

	/** FIELDS BELOW DEPEND ON MARSHALING PARAMETERS */

	/** Referenced device */
	private Device device;

	/** Referenced device group */
	private DeviceGroup deviceGroup;

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceGroupElement#getGroupToken()
	 */
	public String getGroupToken() {
		return groupToken;
	}

	public void setGroupToken(String groupToken) {
		this.groupToken = groupToken;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceGroupElement#getIndex()
	 */
	public long getIndex() {
		return index;
	}

	public void setIndex(long index) {
		this.index = index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceGroupElement#getType()
	 */
	public GroupElementType getType() {
		return type;
	}

	public void setType(GroupElementType type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceGroupElement#getElementId()
	 */
	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceGroupElement#getRoles()
	 */
	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public DeviceGroup getDeviceGroup() {
		return deviceGroup;
	}

	public void setDeviceGroup(DeviceGroup deviceGroup) {
		this.deviceGroup = deviceGroup;
	}
}