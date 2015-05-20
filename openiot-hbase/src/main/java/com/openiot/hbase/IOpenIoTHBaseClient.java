/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase;

import com.openiot.spi.OpenIoTException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTableInterface;

/**
 * Interface expected for HBase client implementations.
 * 
 * @author Derek
 */
public interface IOpenIoTHBaseClient {

	/**
	 * Get client configuration.
	 * 
	 * @return
	 */
	public Configuration getConfiguration();

	/**
	 * Get HBase admin interface.
	 * 
	 * @return
	 */
	public HBaseAdmin getAdmin();

	/**
	 * Get the named table interface. Auto flush is disabled.
	 * 
	 * @param tableName
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public HTableInterface getTableInterface(byte[] tableName) throws OpenIoTException;

	/**
	 * Get the named table interface.
	 * 
	 * @param tableName
	 * @param autoFlush
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public HTableInterface getTableInterface(byte[] tableName, boolean autoFlush) throws OpenIoTException;
}