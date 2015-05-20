/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.common;

import com.openiot.hbase.IOpenIoTHBase;
import com.openiot.hbase.IOpenIoTHBaseClient;
import com.openiot.spi.OpenIoTException;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.apache.log4j.Logger;

/**
 * Utility method for OpenIoT HBase tables.
 * 
 * @author Derek
 */
public class OpenIoTTables {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(OpenIoTTables.class);

	/**
	 * Assure that the given table exists and create it if not.
	 * 
	 * @param hbase
	 * @param tableName
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static void assureTable(IOpenIoTHBaseClient hbase, byte[] tableName, BloomType bloom)
			throws OpenIoTException {
		try {
			String tnameStr = new String(tableName);
			if (!hbase.getAdmin().tableExists(tableName)) {
				LOGGER.info("Table '" + tnameStr + "' does not exist. Creating table...");
				HTableDescriptor table = new HTableDescriptor(TableName.valueOf(tableName));
				HColumnDescriptor family = new HColumnDescriptor(IOpenIoTHBase.FAMILY_ID);
				family.setBloomFilterType(bloom);
				table.addFamily(family);
				hbase.getAdmin().createTable(table);
				LOGGER.info("Table '" + tnameStr + "' created successfully.");
			} else {
				LOGGER.info("Table '" + tnameStr + "' verfied.");
			}
		} catch (Throwable e) {
			throw new OpenIoTException(e);
		}
	}
}