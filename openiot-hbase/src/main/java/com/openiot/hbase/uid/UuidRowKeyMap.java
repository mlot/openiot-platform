/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.uid;

import com.openiot.hbase.IOpenIoTHBaseClient;
import com.openiot.spi.OpenIoTException;

import java.util.UUID;

/**
 * Maps UUIDs to row keys.
 * 
 * @author Derek
 */
public class UuidRowKeyMap extends UniqueIdMap<String, byte[]> {

	public UuidRowKeyMap(IOpenIoTHBaseClient hbase, byte keyIndicator, byte valueIndicator) {
		super(hbase, keyIndicator, valueIndicator);
	}

	/**
	 * Create a UUID and associate it with the given row key.
	 * 
	 * @param rowkey
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public String createUniqueId(byte[] rowkey) throws OpenIoTException {
		String uuid = UUID.randomUUID().toString();
		create(uuid, rowkey);
		return uuid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.uid.UniqueIdMap#convertName(byte[])
	 */
	public String convertName(byte[] bytes) {
		return new String(bytes);
	}

	@Override
	public byte[] convertName(String name) {
		return name.getBytes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.uid.UniqueIdMap#convertValue(byte[])
	 */
	public byte[] convertValue(byte[] bytes) {
		return bytes;
	}
}