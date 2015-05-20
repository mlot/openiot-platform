/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.device;

import com.openiot.core.OpenIoTPersistence;
import com.openiot.hbase.IHBaseContext;
import com.openiot.hbase.IOpenIoTHBase;
import com.openiot.hbase.common.HBaseUtils;
import com.openiot.hbase.common.Pager;
import com.openiot.hbase.encoder.PayloadMarshalerResolver;
import com.openiot.rest.model.device.batch.BatchElement;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.batch.IBatchElement;
import com.openiot.spi.device.request.IBatchElementUpdateRequest;
import com.openiot.spi.search.device.IBatchElementSearchCriteria;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * HBase specifics for dealing with OpenIoT batch operation elements.
 * 
 * @author Derek
 */
public class HBaseBatchElement {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(HBaseBatchElement.class);

	/** Length of element index info (subset of 8 byte long) */
	public static final int INDEX_LENGTH = 4;

	/** Column qualifier for element hardware id */
	public static final byte[] HARDWARE_ID = Bytes.toBytes("i");

	/** Column qualifier for element processing status */
	public static final byte[] PROCESSING_STATUS = Bytes.toBytes("s");

	/**
	 * Create a batch element row.
	 * 
	 * @param context
	 * @param devices
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IBatchElement createBatchElement(IHBaseContext context, HTableInterface devices,
			IBatchElement request) throws OpenIoTException {
		byte[] elementKey = getElementRowKey(request.getBatchOperationToken(), request.getIndex());

		// Use common processing logic so all backend implementations work the same.
		BatchElement element =
				OpenIoTPersistence.batchElementCreateLogic(request.getBatchOperationToken(),
                        request.getHardwareId(), request.getIndex());

		// Encode batch element.
		byte[] payload = context.getPayloadMarshaler().encodeBatchElement(element);

		try {
			Put put = new Put(elementKey);
			HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
			put.add(IOpenIoTHBase.FAMILY_ID, HARDWARE_ID, Bytes.toBytes(element.getHardwareId()));
			put.add(IOpenIoTHBase.FAMILY_ID, PROCESSING_STATUS,
					Bytes.toBytes(String.valueOf(request.getProcessingStatus().getCode())));
			devices.put(put);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to create device group element.", e);
		}

		return element;
	}

	/**
	 * Updates an existing batch operation element.
	 * 
	 * @param context
	 * @param operationToken
	 * @param index
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IBatchElement updateBatchElement(IHBaseContext context, String operationToken, long index,
			IBatchElementUpdateRequest request) throws OpenIoTException {
		HTableInterface devices = null;
		try {
			devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
			BatchElement element = getBatchElement(context, devices, operationToken, index);
			byte[] elementKey = getElementRowKey(operationToken, index);

			OpenIoTPersistence.batchElementUpdateLogic(request, element);
			byte[] payload = context.getPayloadMarshaler().encodeBatchElement(element);

			Put put = new Put(elementKey);
			HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
			put.add(IOpenIoTHBase.FAMILY_ID, HARDWARE_ID, Bytes.toBytes(element.getHardwareId()));
			put.add(IOpenIoTHBase.FAMILY_ID, PROCESSING_STATUS,
					Bytes.toBytes(String.valueOf(request.getProcessingStatus().getCode())));
			devices.put(put);
			return element;
		} catch (IOException e) {
			throw new OpenIoTException("Unable to update batch element.", e);
		} finally {
			HBaseUtils.closeCleanly(devices);
		}
	}

	/**
	 * Gets the batch operation element given the parent operation token and unique index.
	 * 
	 * @param context
	 * @param devices
	 * @param operationToken
	 * @param index
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static BatchElement getBatchElement(IHBaseContext context, HTableInterface devices,
			String operationToken, long index) throws OpenIoTException {
		byte[] elementKey = getElementRowKey(operationToken, index);
		try {
			Get get = new Get(elementKey);
			HBaseUtils.addPayloadFields(get);
			Result result = devices.get(get);

			byte[] type = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
			byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
			if ((type == null) || (payload == null)) {
				return null;
			}

			return PayloadMarshalerResolver.getInstance().getMarshaler(type).decodeBatchElement(payload);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to create device group element.", e);
		}
	}

	/**
	 * List batch elements that meet the given criteria.
	 * 
	 * @param context
	 * @param batchToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IBatchElement> listBatchElements(IHBaseContext context, String batchToken,
			IBatchElementSearchCriteria criteria) throws OpenIoTException {
		HTableInterface table = null;
		ResultScanner scanner = null;
		try {
			table = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
			byte[] primary =
					HBaseBatchOperation.KEY_BUILDER.buildSubkey(batchToken,
							BatchOperationRecordType.BatchElement.getType());
			byte[] after =
					HBaseBatchOperation.KEY_BUILDER.buildSubkey(batchToken,
							(byte) (BatchOperationRecordType.BatchElement.getType() + 1));
			Scan scan = new Scan();
			scan.setStartRow(primary);
			scan.setStopRow(after);
			scanner = table.getScanner(scan);

			Pager<IBatchElement> pager = new Pager<IBatchElement>(criteria);
			for (Result result : scanner) {
				byte[] payloadType = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
				byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);

				if ((payload != null) && (payloadType != null)) {
					BatchElement elm =
							PayloadMarshalerResolver.getInstance().getMarshaler(payloadType).decodeBatchElement(
									payload);
					if ((criteria.getProcessingStatus() == null)
							|| (criteria.getProcessingStatus() == elm.getProcessingStatus())) {
						pager.process(elm);
					}
				}
			}
			return new SearchResults<IBatchElement>(pager.getResults());
		} catch (IOException e) {
			throw new OpenIoTException("Error scanning batch element rows.", e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			HBaseUtils.closeCleanly(table);
		}
	}

	/**
	 * Delete all elements for a batch operation.
	 * 
	 * @param context
	 * @param batchToken
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static void deleteBatchElements(IHBaseContext context, String batchToken)
			throws OpenIoTException {
		HTableInterface table = null;
		ResultScanner scanner = null;
		try {
			table = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
			byte[] primary =
					HBaseBatchOperation.KEY_BUILDER.buildSubkey(batchToken,
							BatchOperationRecordType.BatchElement.getType());
			byte[] after =
					HBaseDeviceGroup.KEY_BUILDER.buildSubkey(batchToken,
							(byte) (BatchOperationRecordType.BatchElement.getType() + 1));
			Scan scan = new Scan();
			scan.setStartRow(primary);
			scan.setStopRow(after);
			scanner = table.getScanner(scan);

			List<DeleteRecord> matches = new ArrayList<DeleteRecord>();
			for (Result result : scanner) {
				byte[] row = result.getRow();
				byte[] payloadType = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
				byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
				if (payload != null) {
					matches.add(new DeleteRecord(row, payloadType, payload));
				}
			}
			for (DeleteRecord dr : matches) {
				try {
					Delete delete = new Delete(dr.getRowkey());
					table.delete(delete);
				} catch (IOException e) {
					LOGGER.warn("Batch element delete failed for key: " + dr.getRowkey());
				}
			}
		} catch (IOException e) {
			throw new OpenIoTException("Error scanning device group element rows.", e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			HBaseUtils.closeCleanly(table);
		}
	}

	/**
	 * Get key for batch element.
	 * 
	 * @param batchToken
	 * @param index
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static byte[] getElementRowKey(String batchToken, Long index) throws OpenIoTException {
		byte[] baserow =
				HBaseBatchOperation.KEY_BUILDER.buildSubkey(batchToken,
						BatchOperationRecordType.BatchElement.getType());
		byte[] eidBytes = getTruncatedIdentifier(index);
		ByteBuffer buffer = ByteBuffer.allocate(baserow.length + eidBytes.length);
		buffer.put(baserow);
		buffer.put(eidBytes);
		return buffer.array();
	}

	/**
	 * Truncate element id value to expected length. This will be a subset of the full
	 * 8-bit long value.
	 * 
	 * @param value
	 * @return
	 */
	public static byte[] getTruncatedIdentifier(Long value) {
		byte[] bytes = Bytes.toBytes(value);
		byte[] result = new byte[INDEX_LENGTH];
		System.arraycopy(bytes, bytes.length - INDEX_LENGTH, result, 0, INDEX_LENGTH);
		return result;
	}
}