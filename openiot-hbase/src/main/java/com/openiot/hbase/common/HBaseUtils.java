/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.common;

import com.openiot.core.OpenIoTPersistence;
import com.openiot.hbase.IOpenIoTHBase;
import com.openiot.hbase.IOpenIoTHBaseClient;
import com.openiot.hbase.encoder.IPayloadMarshaler;
import com.openiot.hbase.encoder.PayloadEncoding;
import com.openiot.hbase.encoder.PayloadMarshalerResolver;
import com.openiot.rest.model.common.MetadataProviderEntity;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.common.IFilter;
import com.openiot.spi.search.ISearchCriteria;
import org.apache.hadoop.hbase.client.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Handle common HBase functionality.
 * 
 * @author Derek
 */
public class HBaseUtils {

	/** Static logger instance */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(HBaseUtils.class);

	/**
	 * Create or update primary record.
	 * 
	 * @param client
	 * @param marshaler
	 * @param tableName
	 * @param entity
	 * @param token
	 * @param builder
	 * @param qualifiers
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static <T> T createOrUpdate(IOpenIoTHBaseClient client, IPayloadMarshaler marshaler,
			byte[] tableName, T entity, String token, IRowKeyBuilder builder, Map<byte[], byte[]> qualifiers)
			throws OpenIoTException {
		byte[] primary = builder.buildPrimaryKey(token);
		byte[] payload = marshaler.encode(entity);

		HTableInterface table = null;
		try {
			table = client.getTableInterface(tableName);
			Put put = new Put(primary);
			HBaseUtils.addPayloadFields(marshaler.getEncoding(), put, payload);
			for (byte[] key : qualifiers.keySet()) {
				put.add(IOpenIoTHBase.FAMILY_ID, key, qualifiers.get(key));
			}
			table.put(put);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to create entity data for " + entity.getClass().getName(), e);
		} finally {
			HBaseUtils.closeCleanly(table);
		}

		return entity;
	}

	/**
	 * Save payload.
	 * 
	 * @param client
	 * @param marshaler
	 * @param tableName
	 * @param entity
	 * @param token
	 * @param builder
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static <T> T put(IOpenIoTHBaseClient client, IPayloadMarshaler marshaler, byte[] tableName,
			T entity, String token, IRowKeyBuilder builder) throws OpenIoTException {
		byte[] primary = builder.buildPrimaryKey(token);
		byte[] payload = marshaler.encode(entity);

		HTableInterface table = null;
		try {
			table = client.getTableInterface(tableName);
			Put put = new Put(primary);
			HBaseUtils.addPayloadFields(marshaler.getEncoding(), put, payload);
			table.put(put);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to put JSON data for " + entity.getClass().getName(), e);
		} finally {
			HBaseUtils.closeCleanly(table);
		}

		return entity;
	}

	/**
	 * Get a primary record by token.
	 * 
	 * @param context
	 * @param tableName
	 * @param token
	 * @param builder
	 * @param type
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static <T> T get(IOpenIoTHBaseClient client, byte[] tableName, String token,
			IRowKeyBuilder builder, Class<T> type) throws OpenIoTException {
		byte[] primary = builder.buildPrimaryKey(token);

		HTableInterface table = null;
		try {
			table = client.getTableInterface(tableName);
			Get get = new Get(primary);
			get.addColumn(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
			get.addColumn(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
			Result result = table.get(get);

			byte[] ptype = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
			byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
			if ((ptype == null) || (payload == null)) {
				return null;
			}

			return PayloadMarshalerResolver.getInstance().getMarshaler(ptype).decode(payload, type);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to load data for token: " + token, e);
		} finally {
			HBaseUtils.closeCleanly(table);
		}
	}

	/**
	 * Get all matching records, sort them, and get matching pages. TODO: This is not
	 * efficient since it always processes all records.
	 * 
	 * @param client
	 * @param tableName
	 * @param builder
	 * @param includeDeleted
	 * @param intf
	 * @param clazz
	 * @param filter
	 * @param criteria
	 * @param comparator
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@SuppressWarnings("unchecked")
	public static <I, C> SearchResults<I> getFilteredList(IOpenIoTHBaseClient client, byte[] tableName,
			IRowKeyBuilder builder, boolean includeDeleted, Class<I> intf, Class<C> clazz, IFilter<C> filter,
			ISearchCriteria criteria, Comparator<C> comparator) throws OpenIoTException {
		List<C> results = getRecordList(client, tableName, builder, includeDeleted, clazz, filter);
		Collections.sort(results, comparator);
		Pager<I> pager = new Pager<I>(criteria);
		for (C result : results) {
			pager.process((I) result);
		}
		return new SearchResults<I>(pager.getResults(), pager.getTotal());
	}

	/**
	 * Get list of records that match the given criteria.
	 * 
	 * @param client
	 * @param tableName
	 * @param builder
	 * @param includeDeleted
	 * @param clazz
	 * @param filter
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static <T> List<T> getRecordList(IOpenIoTHBaseClient client, byte[] tableName,
			IRowKeyBuilder builder, boolean includeDeleted, Class<T> clazz, IFilter<T> filter)
			throws OpenIoTException {
		HTableInterface table = null;
		ResultScanner scanner = null;
		try {
			table = client.getTableInterface(tableName);
			Scan scan = new Scan();
			scan.setStartRow(new byte[] { builder.getTypeIdentifier() });
			scan.setStopRow(new byte[] { (byte) (builder.getTypeIdentifier() + 1) });
			scanner = table.getScanner(scan);

			List<T> results = new ArrayList<T>();
			for (Result result : scanner) {
				byte[] row = result.getRow();

				// Only match primary rows.
				if ((row[0] != builder.getTypeIdentifier())
						|| (row[builder.getKeyIdLength() + 1] != builder.getPrimaryIdentifier())) {
					continue;
				}

				boolean shouldAdd = true;
				byte[] payloadType = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
				byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
				byte[] deleted = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.DELETED);

				if ((deleted != null) && (!includeDeleted)) {
					shouldAdd = false;
				}
				
				if ((shouldAdd) && (payload != null)) {
					T instance =
							PayloadMarshalerResolver.getInstance().getMarshaler(payloadType).decode(payload,
									clazz);
					if (!filter.isExcluded(instance)) {
						results.add(instance);
					}
				}
			}
			return results;
		} catch (IOException e) {
			throw new OpenIoTException("Error in list operation.", e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			HBaseUtils.closeCleanly(table);
		}
	}

	/**
	 * Delete a primary record by token.
	 * 
	 * @param context
	 * @param tableName
	 * @param token
	 * @param force
	 * @param builder
	 * @param type
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static <T extends MetadataProviderEntity> T delete(IOpenIoTHBaseClient client,
			IPayloadMarshaler marshaler, byte[] tableName, String token, boolean force,
			IRowKeyBuilder builder, Class<T> type) throws OpenIoTException {
		T existing = get(client, tableName, token, builder, type);
		existing.setDeleted(true);

		byte[] primary = builder.buildPrimaryKey(token);
		if (force) {
			builder.deleteReference(token);
			HTableInterface table = null;
			try {
				Delete delete = new Delete(primary);
				table = client.getTableInterface(tableName);
				table.delete(delete);
			} catch (IOException e) {
				throw new OpenIoTException("Unable to delete data for token: " + token, e);
			} finally {
				HBaseUtils.closeCleanly(table);
			}
		} else {
			byte[] marker = { (byte) 0x01 };
			OpenIoTPersistence.setUpdatedEntityMetadata(existing);
			byte[] updated = marshaler.encode(existing);

			HTableInterface devices = null;
			try {
				devices = client.getTableInterface(tableName);
				Put put = new Put(primary);
				put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE,
						marshaler.getEncoding().getIndicator());
				put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD, updated);
				put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.DELETED, marker);
				devices.put(put);
			} catch (IOException e) {
				throw new OpenIoTException("Unable to flag deleted for token: " + token, e);
			} finally {
				HBaseUtils.closeCleanly(devices);
			}
		}
		return existing;
	}

	/**
	 * Adds payload fields to an HBase put.
	 * 
	 * @param context
	 * @param put
	 * @param encoded
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static void addPayloadFields(PayloadEncoding encoding, Put put, byte[] encoded)
			throws OpenIoTException {
		put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE, encoding.getIndicator());
		put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD, encoded);
	}

	/**
	 * Adds payload fields to an HBase get.
	 * 
	 * @param get
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static void addPayloadFields(Get get) throws OpenIoTException {
		get.addColumn(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
		get.addColumn(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
	}

	/**
	 * Prevent having to add custom {@link IOException} handling.
	 * 
	 * @param table
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static void closeCleanly(HTableInterface table) throws OpenIoTException {
		try {
			if (table != null) {
				table.close();
			}
		} catch (IOException e) {
			throw new OpenIoTException("Exception closing table.", e);
		}
	}
}