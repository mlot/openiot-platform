/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device;

/**
 * Maps a location in an {@link com.openiot.spi.device.element.IDeviceElementSchema} to an {@link IDevice}.
 * 
 * @author Derek
 */
public interface IDeviceElementMapping {

	/**
	 * Get path in {@link com.openiot.spi.device.element.IDeviceElementSchema} being mapped.
	 * 
	 * @return
	 */
	public String getDeviceElementSchemaPath();

	/**
	 * Get hardware id of {@link IDevice} being mapped.
	 * 
	 * @return
	 */
	public String getHardwareId();
}