/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * Constants associated with the OpenIoT column family.
 * 
 * @author Derek
 */
public interface IOpenIoTHBase {

	/***************
	 * TABLE NAMES *
	 ***************/

	/** Sites table name */
	public static final byte[] UID_TABLE_NAME = Bytes.toBytes("uids");

	/** Sites table name */
	public static final byte[] SITES_TABLE_NAME = Bytes.toBytes("sites");

	/** Events table name */
	public static final byte[] EVENTS_TABLE_NAME = Bytes.toBytes("events");

	/** Devices table name */
	public static final byte[] DEVICES_TABLE_NAME = Bytes.toBytes("devices");

	/** Users table name */
	public static final byte[] USERS_TABLE_NAME = Bytes.toBytes("users");

	/*******************
	 * COLUMN FAMILIES *
	 *******************/

	/** OpenIoT family id */
	public static final byte[] FAMILY_ID = Bytes.toBytes("s");

	/*********************
	 * COMMON QUALIFIERS *
	 *********************/

	/** Column qualifier for object payload */
	public static final byte[] PAYLOAD = Bytes.toBytes("z");

	/** Column qualifier for object payload type */
	public static final byte[] PAYLOAD_TYPE = Bytes.toBytes("q");

	/** Column qualifier that indicates a deleted record */
	public static final byte[] DELETED = Bytes.toBytes("x");
}