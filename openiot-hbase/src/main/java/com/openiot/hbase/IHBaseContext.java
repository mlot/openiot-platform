/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase;

import com.openiot.hbase.device.IDeviceEventBuffer;
import com.openiot.hbase.encoder.IPayloadMarshaler;
import com.openiot.spi.device.IDeviceManagementCacheProvider;

/**
 * Supplies configuration information to HBase implementation classes.
 * 
 * @author Derek
 */
public interface IHBaseContext {

	/**
	 * Get HBase client accessor.
	 * 
	 * @return
	 */
	public IOpenIoTHBaseClient getClient();

	/**
	 * Get configured cache provider.
	 * 
	 * @return
	 */
	public IDeviceManagementCacheProvider getCacheProvider();

	/**
	 * Get configured payload marshaler.
	 * 
	 * @return
	 */
	public IPayloadMarshaler getPayloadMarshaler();

	/**
	 * Get buffer for storing device events.
	 * 
	 * @return
	 */
	public IDeviceEventBuffer getDeviceEventBuffer();
}