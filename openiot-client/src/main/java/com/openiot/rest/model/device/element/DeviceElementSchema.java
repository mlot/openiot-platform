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
import com.openiot.spi.device.element.IDeviceElementSchema;

import java.io.Serializable;

/**
 * Default implementation of {@link IDeviceElementSchema}.
 * 
 * @author Derek
 */
@JsonInclude(Include.NON_NULL)
public class DeviceElementSchema extends DeviceUnit implements IDeviceElementSchema, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = -7146013802892756603L;

	public DeviceElementSchema() {
	}
}