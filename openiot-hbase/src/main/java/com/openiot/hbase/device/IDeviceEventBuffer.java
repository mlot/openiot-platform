/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.device;

import com.openiot.spi.OpenIoTException;
import org.apache.hadoop.hbase.client.Put;

/**
 * Interface for buffer used for saving device events.
 * 
 * @author Derek
 */
public interface IDeviceEventBuffer {

	/**
	 * Start buffer lifecycle.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void start() throws OpenIoTException;

	/**
	 * Stop buffer lifecycle.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void stop() throws OpenIoTException;

	/**
	 * Add a {@link Put} to be buffered.
	 * 
	 * @param put
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void add(Put put) throws OpenIoTException;
}