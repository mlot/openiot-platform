/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.element;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.openiot.spi.device.element.IDeviceElement;

import java.io.Serializable;

/**
 * Default implementation of {@link IDeviceElement}.
 * 
 * @author Derek
 */
@JsonInclude(Include.NON_NULL)
public class DeviceElement implements IDeviceElement, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = 8334544031222730874L;

	/** Element name */
	private String name;

	/** Path relative to parent */
	private String path;

	public DeviceElement() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceElement#getName()
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
	 * @see IDeviceElement#getPath()
	 */
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}