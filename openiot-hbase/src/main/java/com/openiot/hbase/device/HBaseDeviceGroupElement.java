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
import com.openiot.rest.model.device.group.DeviceGroupElement;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.group.IDeviceGroupElement;
import com.openiot.spi.device.request.IDeviceGroupElementCreateRequest;
import com.openiot.spi.search.ISearchCriteria;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * HBase specifics for dealing with OpenIoT device group elements.
 * 
 * @author Derek
 */
public class HBaseDeviceGroupElement {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(HBaseDeviceGroupElement.class);

	/** Length of element index info (subset of 8 byte long) */
	public static final int INDEX_LENGTH = 4;

	/** Column qualifier for element identifier (type+id) */
	public static final byte[] ELEMENT_IDENTIFIER = Bytes.toBytes("i");

	/**
	 * Create a group of group elements.
	 * 
	 * @param context
	 * @param groupToken
	 * @param requests
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static List<IDeviceGroupElement> createDeviceGroupElements(IHBaseContext context,
			String groupToken, List<IDeviceGroupElementCreateRequest> requests) throws OpenIoTException {
		byte[] groupKey = HBaseDeviceGroup.KEY_BUILDER.buildPrimaryKey(groupToken);
		List<IDeviceGroupElement> results = new ArrayList<IDeviceGroupElement>();
		for (IDeviceGroupElementCreateRequest request : requests) {
			Long eid = HBaseDeviceGroup.allocateNextElementId(context, groupKey);
			results.add(HBaseDeviceGroupElement.createDeviceGroupElement(context, groupToken, eid, request));
		}
		return results;
	}

	/**
	 * Create a new device group element.
	 * 
	 * @param context
	 * @param groupToken
	 * @param index
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceGroupElement createDeviceGroupElement(IHBaseContext context, String groupToken,
			Long index, IDeviceGroupElementCreateRequest request) throws OpenIoTException {
		byte[] elementKey = getElementRowKey(groupToken, index);

		// Use common processing logic so all backend implementations work the same.
		DeviceGroupElement element =
				OpenIoTPersistence.deviceGroupElementCreateLogic(request, groupToken, index);

		byte[] payload = context.getPayloadMarshaler().encodeDeviceGroupElement(element);

		HTableInterface devices = null;
		try {
			devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
			Put put = new Put(elementKey);
			HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
			put.add(IOpenIoTHBase.FAMILY_ID, ELEMENT_IDENTIFIER, getCombinedIdentifier(request));
			devices.put(put);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to create device group element.", e);
		} finally {
			HBaseUtils.closeCleanly(devices);
		}

		return element;
	}

	/**
	 * Remove the given device group elements.
	 * 
	 * @param context
	 * @param groupToken
	 * @param elements
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static List<IDeviceGroupElement> removeDeviceGroupElements(IHBaseContext context,
			String groupToken, List<IDeviceGroupElementCreateRequest> elements) throws OpenIoTException {
		List<byte[]> combinedIds = new ArrayList<byte[]>();
		for (IDeviceGroupElementCreateRequest request : elements) {
			combinedIds.add(getCombinedIdentifier(request));
		}
		return deleteElements(context, groupToken, combinedIds);
	}

	/**
	 * Handles logic for finding and deleting device group elements.
	 * 
	 * @param context
	 * @param groupToken
	 * @param combinedIds
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static List<IDeviceGroupElement> deleteElements(IHBaseContext context, String groupToken,
			List<byte[]> combinedIds) throws OpenIoTException {
		HTableInterface table = null;
		ResultScanner scanner = null;
		try {
			table = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
			byte[] primary =
					HBaseDeviceGroup.KEY_BUILDER.buildSubkey(groupToken,
							DeviceGroupRecordType.DeviceGroupElement.getType());
			byte[] after =
					HBaseDeviceGroup.KEY_BUILDER.buildSubkey(groupToken,
							(byte) (DeviceGroupRecordType.DeviceGroupElement.getType() + 1));
			Scan scan = new Scan();
			scan.setStartRow(primary);
			scan.setStopRow(after);
			scanner = table.getScanner(scan);

			List<DeleteRecord> matches = new ArrayList<DeleteRecord>();
			for (Result result : scanner) {
				byte[] row = result.getRow();

				boolean shouldAdd = false;
				byte[] type = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
				byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
				byte[] ident = result.getValue(IOpenIoTHBase.FAMILY_ID, ELEMENT_IDENTIFIER);
				if (ident != null) {
					for (byte[] toDelete : combinedIds) {
						if (Bytes.equals(toDelete, ident)) {
							shouldAdd = true;
							break;
						}
					}
				}
				if ((shouldAdd) && (type != null) && (payload != null)) {
					matches.add(new DeleteRecord(row, type, payload));
				}
			}
			List<IDeviceGroupElement> results = new ArrayList<IDeviceGroupElement>();
			for (DeleteRecord dr : matches) {
				try {
					Delete delete = new Delete(dr.getRowkey());
					table.delete(delete);
					results.add(PayloadMarshalerResolver.getInstance().getMarshaler(dr.getPayloadType()).decodeDeviceGroupElement(
							dr.getPayload()));
				} catch (IOException e) {
					LOGGER.warn("Group element delete failed for key: " + dr.getRowkey());
				}
			}
			return results;
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
	 * Deletes all elements for a device group. TODO: There is probably a much more
	 * efficient method of deleting the records than calling a delete for each.
	 * 
	 * @param context
	 * @param groupToken
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static void deleteElements(IHBaseContext context, String groupToken) throws OpenIoTException {
		HTableInterface table = null;
		ResultScanner scanner = null;
		try {
			table = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
			byte[] primary =
					HBaseDeviceGroup.KEY_BUILDER.buildSubkey(groupToken,
							DeviceGroupRecordType.DeviceGroupElement.getType());
			byte[] after =
					HBaseDeviceGroup.KEY_BUILDER.buildSubkey(groupToken,
							(byte) (DeviceGroupRecordType.DeviceGroupElement.getType() + 1));
			Scan scan = new Scan();
			scan.setStartRow(primary);
			scan.setStopRow(after);
			scanner = table.getScanner(scan);

			List<DeleteRecord> matches = new ArrayList<DeleteRecord>();
			for (Result result : scanner) {
				byte[] row = result.getRow();
				byte[] type = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
				byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
				if ((type != null) && (payload != null)) {
					matches.add(new DeleteRecord(row, type, payload));
				}
			}
			for (DeleteRecord dr : matches) {
				try {
					Delete delete = new Delete(dr.getRowkey());
					table.delete(delete);
				} catch (IOException e) {
					LOGGER.warn("Group element delete failed for key: " + dr.getRowkey());
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
	 * Get paged results for listing device group elements. TODO: This is not optimized!
	 * Getting the correct record count requires a full scan of all elements in the group.
	 * 
	 * @param context
	 * @param groupToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceGroupElement> listDeviceGroupElements(IHBaseContext context,
			String groupToken, ISearchCriteria criteria) throws OpenIoTException {
		HTableInterface table = null;
		ResultScanner scanner = null;
		try {
			table = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
			byte[] primary =
					HBaseDeviceGroup.KEY_BUILDER.buildSubkey(groupToken,
							DeviceGroupRecordType.DeviceGroupElement.getType());
			byte[] after =
					HBaseDeviceGroup.KEY_BUILDER.buildSubkey(groupToken,
							(byte) (DeviceGroupRecordType.DeviceGroupElement.getType() + 1));
			Scan scan = new Scan();
			scan.setStartRow(primary);
			scan.setStopRow(after);
			scanner = table.getScanner(scan);

			Pager<IDeviceGroupElement> pager = new Pager<IDeviceGroupElement>(criteria);
			for (Result result : scanner) {
				byte[] type = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
				byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
				if ((type != null) && (payload != null)) {
					pager.process(PayloadMarshalerResolver.getInstance().getMarshaler(type).decodeDeviceGroupElement(
							payload));
				}
			}
			return new SearchResults<IDeviceGroupElement>(pager.getResults());
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
	 * Get key for a network element.
	 * 
	 * @param groupToken
	 * @param elementId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static byte[] getElementRowKey(String groupToken, Long elementId) throws OpenIoTException {
		byte[] baserow =
				HBaseDeviceGroup.KEY_BUILDER.buildSubkey(groupToken,
						DeviceGroupRecordType.DeviceGroupElement.getType());
		byte[] eidBytes = getTruncatedIdentifier(elementId);
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

	/**
	 * Create an identifier based on element type and id.
	 * 
	 * @param request
	 * @return
	 */
	public static byte[] getCombinedIdentifier(IDeviceGroupElementCreateRequest request) {
		byte[] id = Bytes.toBytes(request.getElementId());
		ByteBuffer buffer = ByteBuffer.allocate(1 + id.length);
		switch (request.getType()) {
		case Device: {
			buffer.put((byte) 0x00);
			break;
		}
		case Group: {
			buffer.put((byte) 0x01);
			break;
		}
		default: {
			throw new RuntimeException("Unknown device group element type: " + request.getType().name());
		}
		}
		buffer.put(id);
		return buffer.array();
	}
}