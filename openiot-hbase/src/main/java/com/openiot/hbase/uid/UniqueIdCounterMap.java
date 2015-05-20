/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.uid;

import com.openiot.hbase.IOpenIoTHBase;
import com.openiot.hbase.IOpenIoTHBaseClient;
import com.openiot.hbase.common.HBaseUtils;
import com.openiot.spi.OpenIoTException;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Unique id mapper that generates UUIDs as keys and matches them to integer values.
 * 
 * @author Derek
 */
public class UniqueIdCounterMap extends UniqueIdMap<String, Long> {

	public UniqueIdCounterMap(IOpenIoTHBaseClient hbase, byte keyIndicator, byte valueIndicator) {
		super(hbase, keyIndicator, valueIndicator);
	}

	/**
	 * Create a UUID and add it to the UID table with corresponding numeric value.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public String createUniqueId() throws OpenIoTException {
		String uuid = UUID.randomUUID().toString();
		Long value = getNextCounterValue();
		create(uuid, value);
		return uuid;
	}

	/**
	 * Uses an externally specified identifier to map the next available id.
	 * 
	 * @param uuid
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public String useExistingId(String uuid) throws OpenIoTException {
		Long value = getNextCounterValue();
		create(uuid, value);
		return uuid;
	}

	/**
	 * Uses a counter row to keep unique values for the given key indicator type.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public Long getNextCounterValue() throws OpenIoTException {
		ByteBuffer counterRow = ByteBuffer.allocate(2);
		counterRow.put(UniqueIdType.CounterPlaceholder.getIndicator());
		counterRow.put(getKeyIndicator());
		byte[] counterKey = counterRow.array();
		HTableInterface uids = null;
		try {
			uids = hbase.getTableInterface(IOpenIoTHBase.UID_TABLE_NAME);
			return uids.incrementColumnValue(counterKey, IOpenIoTHBase.FAMILY_ID, UniqueIdMap.VALUE_QUAL,
					1L);
		} catch (IOException e) {
			throw new OpenIoTException("Error scanning user rows.", e);
		} finally {
			HBaseUtils.closeCleanly(uids);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.uid.UniqueIdMap#convertName(byte[])
	 */
	public String convertName(byte[] bytes) {
		return new String(bytes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.uid.UniqueIdMap#convertName(java.lang.Object)
	 */
	public byte[] convertName(String name) {
		return name.getBytes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.uid.UniqueIdMap#convertValue(byte[])
	 */
	public Long convertValue(byte[] bytes) {
		return Bytes.toLong(bytes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.uid.UniqueIdMap#convertValue(java.lang.Object)
	 */
	public byte[] convertValue(Long value) {
		return Bytes.toBytes(value);
	}
}