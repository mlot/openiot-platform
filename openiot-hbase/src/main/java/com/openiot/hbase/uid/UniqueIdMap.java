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
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles conversions to/from a given type of unique id.
 * 
 * @author Derek
 */
public abstract class UniqueIdMap<N, V> {

	/** Qualifier for columns containing values */
	public static final byte[] VALUE_QUAL = Bytes.toBytes("value");

	/** HBase client */
	protected IOpenIoTHBaseClient hbase;

	/** Key type indicator */
	protected byte keyIndicator;

	/** Value type indicator */
	protected byte valueIndicator;

	/** Map of names to values */
	private Map<N, V> nameToValue = new HashMap<N, V>();

	/** Maps of values to names */
	private Map<V, N> valueToName = new HashMap<V, N>();

	public UniqueIdMap(IOpenIoTHBaseClient hbase, byte keyIndicator, byte valueIndicator) {
		this.hbase = hbase;
		this.keyIndicator = keyIndicator;
		this.valueIndicator = valueIndicator;
	}

	/**
	 * Create mapping and reverse mapping in UID table. Create value-to-name first, so if
	 * it fails we do not have names without reverse mappings.
	 * 
	 * @param name
	 * @param value
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void create(N name, V value) throws OpenIoTException {
		createValueToName(value, name);
		createNameToValue(name, value);
	}

	/**
	 * Delete a mapping and reverse mapping in UID table.
	 * 
	 * @param name
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void delete(N name) throws OpenIoTException {
		V value = nameToValue.get(name);
		deleteNameToValue(name);
		deleteValueToName(value);
	}

	/**
	 * Create name to value row in the UID table.
	 * 
	 * @param name
	 * @param value
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void createNameToValue(N name, V value) throws OpenIoTException {
		byte[] nameBytes = convertName(name);
		ByteBuffer nameBuffer = ByteBuffer.allocate(nameBytes.length + 1);
		nameBuffer.put(keyIndicator);
		nameBuffer.put(nameBytes);
		byte[] valueBytes = convertValue(value);

		HTableInterface uids = null;
		try {
			uids = hbase.getTableInterface(IOpenIoTHBase.UID_TABLE_NAME);
			Put put = new Put(nameBuffer.array());
			put.add(IOpenIoTHBase.FAMILY_ID, VALUE_QUAL, valueBytes);
			uids.put(put);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to store value mapping in UID table.", e);
		} finally {
			HBaseUtils.closeCleanly(uids);
		}
		nameToValue.put(name, value);
	}

	/**
	 * Delete an existing name to value mapping.
	 * 
	 * @param name
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void deleteNameToValue(N name) throws OpenIoTException {
		byte[] nameBytes = convertName(name);
		ByteBuffer nameBuffer = ByteBuffer.allocate(nameBytes.length + 1);
		nameBuffer.put(keyIndicator);
		nameBuffer.put(nameBytes);

		HTableInterface uids = null;
		try {
			uids = hbase.getTableInterface(IOpenIoTHBase.UID_TABLE_NAME);
			Delete delete = new Delete(nameBuffer.array());
			uids.delete(delete);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to delete UID forward mapping.", e);
		} finally {
			HBaseUtils.closeCleanly(uids);
		}
		nameToValue.remove(name);
	}

	/**
	 * Create value to name row in the UID table.
	 * 
	 * @param name
	 * @param value
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void createValueToName(V value, N name) throws OpenIoTException {
		byte[] valueBytes = convertValue(value);
		ByteBuffer valueBuffer = ByteBuffer.allocate(valueBytes.length + 1);
		valueBuffer.put(valueIndicator);
		valueBuffer.put(valueBytes);
		byte[] nameBytes = convertName(name);

		HTableInterface uids = null;
		try {
			uids = hbase.getTableInterface(IOpenIoTHBase.UID_TABLE_NAME);
			Put put = new Put(valueBuffer.array());
			put.add(IOpenIoTHBase.FAMILY_ID, VALUE_QUAL, nameBytes);
			uids.put(put);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to store value mapping in UID table.", e);
		} finally {
			HBaseUtils.closeCleanly(uids);
		}
		valueToName.put(value, name);
	}

	/**
	 * Delete an existing value to name mapping.
	 * 
	 * @param value
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void deleteValueToName(V value) throws OpenIoTException {
		byte[] valueBytes = convertValue(value);
		ByteBuffer valueBuffer = ByteBuffer.allocate(valueBytes.length + 1);
		valueBuffer.put(valueIndicator);
		valueBuffer.put(valueBytes);

		HTableInterface uids = null;
		try {
			uids = hbase.getTableInterface(IOpenIoTHBase.UID_TABLE_NAME);
			Delete delete = new Delete(valueBuffer.array());
			uids.delete(delete);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to delete UID backward mapping.", e);
		} finally {
			HBaseUtils.closeCleanly(uids);
		}
		valueToName.remove(value);
	}

	/**
	 * Refresh from HBase UID table.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void refresh() throws OpenIoTException {
		try {
			List<Result> ntvList = getValuesForType(keyIndicator);
			for (Result ntv : ntvList) {
				byte[] key = ntv.getRow();
				byte[] nameBytes = new byte[key.length - 1];
				System.arraycopy(key, 1, nameBytes, 0, nameBytes.length);
				N name = convertName(nameBytes);
				V value = convertValue(ntv.value());
				nameToValue.put(name, value);
			}
			List<Result> vtnList = getValuesForType(valueIndicator);
			for (Result vtn : vtnList) {
				byte[] key = vtn.getRow();
				byte[] valueBytes = new byte[key.length - 1];
				System.arraycopy(key, 1, valueBytes, 0, valueBytes.length);
				V value = convertValue(valueBytes);
				N name = convertName(vtn.value());
				valueToName.put(value, name);
			}
		} catch (Throwable t) {
			throw new OpenIoTException(t);
		}
	}

	/**
	 * Get all {@link Result} results for the given uid type.
	 * 
	 * @param start
	 * @param end
	 * @return
	 * @throws Exception
	 */
	protected List<Result> getValuesForType(byte type) throws Exception {
		byte startByte = type;
		byte stopByte = type;
		stopByte++;
		byte[] startKey = { startByte };
		byte[] stopKey = { stopByte };

		HTableInterface uids = null;
		ResultScanner scanner = null;
		try {
			uids = hbase.getTableInterface(IOpenIoTHBase.UID_TABLE_NAME);
			Scan scan = new Scan();
			scan.setStartRow(startKey);
			scan.setStopRow(stopKey);
			scanner = uids.getScanner(scan);

			List<Result> results = new ArrayList<Result>();
			for (Result result : scanner) {
				results.add(result);
			}
			return results;
		} catch (IOException e) {
			throw new OpenIoTException("Error scanning site rows.", e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			HBaseUtils.closeCleanly(uids);
		}
	}

	/**
	 * Get value based on name.
	 * 
	 * @param name
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public V getValue(N name) throws OpenIoTException {
		V result = nameToValue.get(name);
		if (result == null) {
			result = getValueFromTable(name);
			if (result != null) {
				nameToValue.put(name, result);
				valueToName.put(result, name);
			}
		}
		return result;
	}

	/**
	 * Get the current value for name from UID table.
	 * 
	 * @param name
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected V getValueFromTable(N name) throws OpenIoTException {
		byte[] nameBytes = convertName(name);
		ByteBuffer nameBuffer = ByteBuffer.allocate(nameBytes.length + 1);
		nameBuffer.put(keyIndicator);
		nameBuffer.put(nameBytes);

		HTableInterface uids = null;
		try {
			uids = hbase.getTableInterface(IOpenIoTHBase.UID_TABLE_NAME);
			Get get = new Get(nameBuffer.array());
			Result result = uids.get(get);
			if (result.size() > 0) {
				return convertValue(result.value());
			}
			return null;
		} catch (IOException e) {
			throw new OpenIoTException("Error locating name to value mapping.", e);
		} finally {
			HBaseUtils.closeCleanly(uids);
		}
	}

	/**
	 * Get name based on value.
	 * 
	 * @param value
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public N getName(V value) throws OpenIoTException {
		N result = valueToName.get(value);
		if (result == null) {
			result = getNameFromTable(value);
			if (result != null) {
				nameToValue.put(result, value);
				valueToName.put(value, result);
			}
		}
		return result;
	}

	/**
	 * Get the current name for value from UID table.
	 * 
	 * @param value
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected N getNameFromTable(V value) throws OpenIoTException {
		byte[] valueBytes = convertValue(value);
		ByteBuffer valueBuffer = ByteBuffer.allocate(valueBytes.length + 1);
		valueBuffer.put(valueIndicator);
		valueBuffer.put(valueBytes);

		HTableInterface uids = null;
		try {
			uids = hbase.getTableInterface(IOpenIoTHBase.UID_TABLE_NAME);
			Get get = new Get(valueBuffer.array());
			Result result = uids.get(get);
			if (result.size() > 0) {
				return convertName(result.value());
			}
			return null;
		} catch (IOException e) {
			throw new OpenIoTException("Error locating value to name mapping.", e);
		} finally {
			HBaseUtils.closeCleanly(uids);
		}
	}

	/** Used to convert stored name to correct datatype */
	public abstract N convertName(byte[] bytes);

	/** Used to convert stored name to correct datatype */
	public abstract byte[] convertName(N name);

	/** Used to convert stored value to correct datatype */
	public abstract V convertValue(byte[] bytes);

	/** Used to convert stored value to correct datatype */
	public abstract byte[] convertValue(V value);

	/** Get HBase connectivity accessor */
	public IOpenIoTHBaseClient getHbase() {
		return hbase;
	}

	/** Get indicator for key rows for this type */
	public byte getKeyIndicator() {
		return keyIndicator;
	}

	/** Get indicator for value rows for this type */
	public byte getValueIndicator() {
		return valueIndicator;
	}
}