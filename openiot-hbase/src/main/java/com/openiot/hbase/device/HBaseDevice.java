/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.device;

import com.openiot.OpenIoT;
import com.openiot.Tracer;
import com.openiot.core.OpenIoTPersistence;
import com.openiot.device.marshaling.DeviceMarshalHelper;
import com.openiot.hbase.IHBaseContext;
import com.openiot.hbase.IOpenIoTHBase;
import com.openiot.hbase.common.HBaseUtils;
import com.openiot.hbase.common.Pager;
import com.openiot.hbase.encoder.PayloadMarshalerResolver;
import com.openiot.hbase.uid.IdManager;
import com.openiot.rest.model.device.Device;
import com.openiot.rest.model.device.DeviceAssignment;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.IDevice;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.request.IDeviceCreateRequest;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.search.ISearchCriteria;
import com.openiot.spi.search.device.DeviceSearchType;
import com.openiot.spi.search.device.IDeviceBySpecificationParameters;
import com.openiot.spi.search.device.IDeviceSearchCriteria;
import com.openiot.spi.server.debug.TracerCategory;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HBase specifics for dealing with OpenIoT devices.
 * 
 * @author Derek
 */
public class HBaseDevice {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(HBaseDevice.class);

	/** Length of device identifier (subset of 8 byte long) */
	public static final int DEVICE_IDENTIFIER_LENGTH = 4;

	/** Byte that indicates an assignment history entry qualifier */
	public static final byte ASSIGNMENT_HISTORY_INDICATOR = (byte) 0x01;

	/** Column qualifier for current site */
	public static final byte[] CURRENT_SITE = "site".getBytes();

	/** Column qualifier for current device assignment */
	public static final byte[] CURRENT_ASSIGNMENT = "assn".getBytes();

	/** Used for cloning device results */
	private static DeviceMarshalHelper DEVICE_HELPER =
			new DeviceMarshalHelper().setIncludeAsset(false).setIncludeAssignment(false).setIncludeSpecification(
					false);

	/**
	 * Create a new device.
	 * 
	 * @param context
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDevice createDevice(IHBaseContext context, IDeviceCreateRequest request)
			throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "createDevice (HBase)", LOGGER);
		try {
			Long existing = IdManager.getInstance().getDeviceKeys().getValue(request.getHardwareId());
			if (existing != null) {
				throw new OpenIoTSystemException(ErrorCode.DuplicateHardwareId, ErrorLevel.ERROR,
						HttpServletResponse.SC_CONFLICT);
			}
			Long value = IdManager.getInstance().getDeviceKeys().getNextCounterValue();
			Long inverse = Long.MAX_VALUE - value;
			IdManager.getInstance().getDeviceKeys().create(request.getHardwareId(), inverse);

			Device device = OpenIoTPersistence.deviceCreateLogic(request);
			return putDevicePayload(context, device);
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Update an existing device.
	 * 
	 * @param context
	 * @param hardwareId
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDevice updateDevice(IHBaseContext context, String hardwareId, IDeviceCreateRequest request)
			throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "updateDevice (HBase) " + hardwareId, LOGGER);
		try {
			Device updated = getDeviceByHardwareId(context, hardwareId);
			if (updated == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidHardwareId, ErrorLevel.ERROR);
			}
			OpenIoTPersistence.deviceUpdateLogic(request, updated);
			return putDevicePayload(context, updated);
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * List devices that meet the given criteria.
	 * 
	 * @param context
	 * @param includeDeleted
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDevice> listDevices(IHBaseContext context, boolean includeDeleted,
			IDeviceSearchCriteria criteria) throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "listDevices (HBase)", LOGGER);
		try {
			Pager<IDevice> matches = getFilteredDevices(context, includeDeleted, criteria);
			return new SearchResults<IDevice>(matches.getResults(), matches.getTotal());
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Get a list of devices filtered with certain criteria.
	 * 
	 * @param context
	 * @param includeDeleted
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static Pager<IDevice> getFilteredDevices(IHBaseContext context, boolean includeDeleted,
			IDeviceSearchCriteria criteria) throws OpenIoTException {
		HTableInterface devices = null;
		ResultScanner scanner = null;

		String specificationToken = null;
		if (criteria.getSearchType() == DeviceSearchType.UsesSpecification) {
			IDeviceBySpecificationParameters params = criteria.getDeviceBySpecificationParameters();
			if (params == null) {
				throw new OpenIoTException(
						"Querying devices by specification token, but parameters were not passed.");
			}
			specificationToken = params.getSpecificationToken();
			if (specificationToken == null) {
				throw new OpenIoTException("No specification token passed for device query.");
			}
		}

		try {
			devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
			Scan scan = new Scan();
			scan.setStartRow(new byte[] { DeviceRecordType.Device.getType() });
			scan.setStopRow(new byte[] { DeviceRecordType.DeviceSpecification.getType() });
			scanner = devices.getScanner(scan);

			Pager<IDevice> pager = new Pager<IDevice>(criteria);
			for (Result result : scanner) {
				boolean shouldAdd = true;
				byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
				byte[] deleted = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.DELETED);
				byte[] currAssn = result.getValue(IOpenIoTHBase.FAMILY_ID, CURRENT_ASSIGNMENT);

				if ((deleted != null) && (!includeDeleted)) {
					shouldAdd = false;
				}
				if ((currAssn != null) && (criteria.isExcludeAssigned())) {
					shouldAdd = false;
				}

				if ((shouldAdd) && (payload != null)) {
					Device device = context.getPayloadMarshaler().decodeDevice(payload);
					switch (criteria.getSearchType()) {
					case All: {
						break;
					}
					case UsesSpecification: {
						if (!specificationToken.equals(device.getSpecificationToken())) {
							continue;
						}
					}
					}
					pager.process(device);
				}
			}
			return pager;
		} catch (IOException e) {
			throw new OpenIoTException("Error scanning device rows.", e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			HBaseUtils.closeCleanly(devices);
		}
	}

	/**
	 * Save the payload for a device.
	 * 
	 * @param context
	 * @param device
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static Device putDevicePayload(IHBaseContext context, Device device) throws OpenIoTException {
		Long value = IdManager.getInstance().getDeviceKeys().getValue(device.getHardwareId());
		if (value == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidHardwareId, ErrorLevel.ERROR);
		}
		byte[] primary = getDeviceRowKey(value);
		byte[] payload = context.getPayloadMarshaler().encodeDevice(device);

		HTableInterface devices = null;
		try {
			devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
			Put put = new Put(primary);
			HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
			put.add(IOpenIoTHBase.FAMILY_ID, CURRENT_SITE, Bytes.toBytes(device.getSiteToken()));
			devices.put(put);
			if (context.getCacheProvider() != null) {
				context.getCacheProvider().getDeviceCache().put(device.getHardwareId(), device);
			}
		} catch (IOException e) {
			throw new OpenIoTException("Unable to put device data.", e);
		} finally {
			HBaseUtils.closeCleanly(devices);
		}

		return device;
	}

	/**
	 * Get a device by unique hardware id.
	 * 
	 * @param context
	 * @param hardwareId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static Device getDeviceByHardwareId(IHBaseContext context, String hardwareId)
			throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "getDeviceByHardwareId (HBase) " + hardwareId,
				LOGGER);
		try {
			if (context.getCacheProvider() != null) {
				IDevice result = context.getCacheProvider().getDeviceCache().get(hardwareId);
				if (result != null) {
					Tracer.info("Returning cached device.", LOGGER);
					return DEVICE_HELPER.convert(result, OpenIoT.getServer().getAssetModuleManager());
				}
			}
			Long deviceId = IdManager.getInstance().getDeviceKeys().getValue(hardwareId);
			if (deviceId == null) {
				Tracer.info("Device not found for hardware id.", LOGGER);
				return null;
			}

			// Find row key based on value associated with hardware id.
			byte[] primary = getDeviceRowKey(deviceId);

			HTableInterface devices = null;
			try {
				devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
				Get get = new Get(primary);
				HBaseUtils.addPayloadFields(get);
				Result result = devices.get(get);

				byte[] type = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
				byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
				if ((type == null) || (payload == null)) {
					return null;
				}

				Device found =
						PayloadMarshalerResolver.getInstance().getMarshaler(type).decodeDevice(payload);
				if ((context.getCacheProvider() != null) && (found != null)) {
					context.getCacheProvider().getDeviceCache().put(hardwareId, found);
				}
				return found;
			} catch (IOException e) {
				throw new OpenIoTException("Unable to load device by hardware id.", e);
			} finally {
				HBaseUtils.closeCleanly(devices);
			}
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Delete a device based on hardware id. Depending on 'force' the record will be
	 * physically deleted or a marker qualifier will be added to mark it as deleted. Note:
	 * Physically deleting a device can leave orphaned references and should not be done
	 * in a production system!
	 * 
	 * @param context
	 * @param hardwareId
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDevice deleteDevice(IHBaseContext context, String hardwareId, boolean force)
			throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "deleteDevice (HBase) " + hardwareId, LOGGER);
		try {
			Long deviceId = IdManager.getInstance().getDeviceKeys().getValue(hardwareId);
			if (deviceId == null) {
				Tracer.warn("Unable to find device to delete by hardware id.", null, LOGGER);
				throw new OpenIoTSystemException(ErrorCode.InvalidHardwareId, ErrorLevel.ERROR);
			}

			Device existing = getDeviceByHardwareId(context, hardwareId);
			existing.setDeleted(true);
			byte[] primary = getDeviceRowKey(deviceId);
			if (force) {
				IdManager.getInstance().getDeviceKeys().delete(hardwareId);
				HTableInterface devices = null;
				try {
					Delete delete = new Delete(primary);
					devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
					devices.delete(delete);
					if (context.getCacheProvider() != null) {
						context.getCacheProvider().getDeviceCache().remove(hardwareId);
					}
				} catch (IOException e) {
					throw new OpenIoTException("Unable to delete device.", e);
				} finally {
					HBaseUtils.closeCleanly(devices);
				}
			} else {
				byte[] marker = { (byte) 0x01 };
				OpenIoTPersistence.setUpdatedEntityMetadata(existing);
				byte[] updated = context.getPayloadMarshaler().encodeDevice(existing);

				HTableInterface devices = null;
				try {
					devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
					Put put = new Put(primary);
					put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE,
							context.getPayloadMarshaler().getEncoding().getIndicator());
					put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD, updated);
					put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.DELETED, marker);
					devices.put(put);
					if (context.getCacheProvider() != null) {
						context.getCacheProvider().getDeviceCache().remove(hardwareId);
					}
				} catch (IOException e) {
					throw new OpenIoTException("Unable to set deleted flag for device.", e);
				} finally {
					HBaseUtils.closeCleanly(devices);
				}
			}
			return existing;
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Get the current device assignment id if assigned or null if not assigned.
	 * 
	 * @param context
	 * @param hardwareId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static String getCurrentAssignmentId(IHBaseContext context, String hardwareId)
			throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "getCurrentAssignmentId (HBase) " + hardwareId,
				LOGGER);
		try {
			if (context.getCacheProvider() != null) {
				IDevice result = context.getCacheProvider().getDeviceCache().get(hardwareId);
				if (result != null) {
					Tracer.info("Returning cached device assignment token.", LOGGER);
					return result.getAssignmentToken();
				}
			}
			Long deviceId = IdManager.getInstance().getDeviceKeys().getValue(hardwareId);
			if (deviceId == null) {
				return null;
			}
			byte[] primary = getDeviceRowKey(deviceId);

			HTableInterface devices = null;
			try {
				devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
				Get get = new Get(primary);
				get.addColumn(IOpenIoTHBase.FAMILY_ID, CURRENT_ASSIGNMENT);
				Result result = devices.get(get);
				if (result.isEmpty()) {
					return null;
				} else if (result.size() == 1) {
					return new String(result.value());
				} else {
					throw new OpenIoTException(
							"Expected one current assignment entry for device and found: " + result.size());
				}
			} catch (IOException e) {
				throw new OpenIoTException("Unable to load current device assignment value.", e);
			} finally {
				HBaseUtils.closeCleanly(devices);
			}
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Set the current device assignment for a device.
	 * 
	 * @param context
	 * @param hardwareId
	 * @param assignmentToken
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static void setDeviceAssignment(IHBaseContext context, String hardwareId, String assignmentToken)
			throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "setDeviceAssignment (HBase) " + hardwareId,
				LOGGER);
		try {
			String existing = getCurrentAssignmentId(context, hardwareId);
			if (existing != null) {
				throw new OpenIoTSystemException(ErrorCode.DeviceAlreadyAssigned, ErrorLevel.ERROR);
			}

			// Load object to update assignment token.
			Device updated = getDeviceByHardwareId(context, hardwareId);
			updated.setAssignmentToken(assignmentToken);
			byte[] payload = context.getPayloadMarshaler().encodeDevice(updated);

			Long deviceId = IdManager.getInstance().getDeviceKeys().getValue(hardwareId);
			if (deviceId == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidHardwareId, ErrorLevel.ERROR);
			}
			byte[] primary = getDeviceRowKey(deviceId);
			byte[] assnHistory = getNextDeviceAssignmentHistoryKey();

			HTableInterface devices = null;
			try {
				devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
				Put put = new Put(primary);
				HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
				put.add(IOpenIoTHBase.FAMILY_ID, CURRENT_ASSIGNMENT, assignmentToken.getBytes());
				put.add(IOpenIoTHBase.FAMILY_ID, assnHistory, assignmentToken.getBytes());
				devices.put(put);

				// Make sure that cache is using updated device information.
				if (context.getCacheProvider() != null) {
					context.getCacheProvider().getDeviceCache().put(updated.getHardwareId(), updated);
				}
			} catch (IOException e) {
				throw new OpenIoTException("Unable to set device assignment.", e);
			} finally {
				HBaseUtils.closeCleanly(devices);
			}
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Removes the device assignment row if present.
	 * 
	 * @param context
	 * @param hardwareId
	 * @param cache
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static void removeDeviceAssignment(IHBaseContext context, String hardwareId)
			throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "removeDeviceAssignment (HBase) " + hardwareId,
				LOGGER);
		try {
			Long deviceId = IdManager.getInstance().getDeviceKeys().getValue(hardwareId);
			if (deviceId == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidHardwareId, ErrorLevel.ERROR);
			}
			byte[] primary = getDeviceRowKey(deviceId);

			Device updated = getDeviceByHardwareId(context, hardwareId);
			updated.setAssignmentToken(null);
			byte[] payload = context.getPayloadMarshaler().encodeDevice(updated);

			HTableInterface devices = null;
			try {
				devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
				Put put = new Put(primary);
				put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE,
						context.getPayloadMarshaler().getEncoding().getIndicator());
				put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD, payload);
				devices.put(put);
				Delete delete = new Delete(primary);
				delete.deleteColumn(IOpenIoTHBase.FAMILY_ID, CURRENT_ASSIGNMENT);
				devices.delete(delete);

				// Make sure that cache is using updated device information.
				if (context.getCacheProvider() != null) {
					context.getCacheProvider().getDeviceCache().put(updated.getHardwareId(), updated);
				}
			} catch (IOException e) {
				throw new OpenIoTException("Unable to remove device assignment.", e);
			} finally {
				HBaseUtils.closeCleanly(devices);
			}
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Get the assignment history for a device.
	 * 
	 * @param context
	 * @param hardwareId
	 * @param criteria
	 * @param cache
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceAssignment> getDeviceAssignmentHistory(IHBaseContext context,
			String hardwareId, ISearchCriteria criteria) throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "getDeviceAssignmentHistory (HBase) "
				+ hardwareId, LOGGER);
		try {
			Long deviceId = IdManager.getInstance().getDeviceKeys().getValue(hardwareId);
			if (deviceId == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidHardwareId, ErrorLevel.ERROR);
			}
			byte[] primary = getDeviceRowKey(deviceId);

			HTableInterface devices = null;
			try {
				devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
				Get get = new Get(primary);
				Result result = devices.get(get);

				Map<byte[], byte[]> map = result.getFamilyMap(IOpenIoTHBase.FAMILY_ID);
				Pager<String> pager = new Pager<String>(criteria);
				for (byte[] qualifier : map.keySet()) {
					if (qualifier[0] == ASSIGNMENT_HISTORY_INDICATOR) {
						byte[] value = map.get(qualifier);
						pager.process(new String(value));
					}
				}
				List<IDeviceAssignment> results = new ArrayList<IDeviceAssignment>();
				for (String token : pager.getResults()) {
					DeviceAssignment assn = HBaseDeviceAssignment.getDeviceAssignment(context, token);
					results.add(assn);
				}
				return new SearchResults<IDeviceAssignment>(results, pager.getTotal());
			} catch (IOException e) {
				throw new OpenIoTException("Unable to load current device assignment history.", e);
			} finally {
				HBaseUtils.closeCleanly(devices);
			}
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Get the unique device identifier based on the long value associated with the device
	 * UUID. This will be a subset of the full 8-bit long value.
	 * 
	 * @param value
	 * @return
	 */
	public static byte[] getTruncatedIdentifier(Long value) {
		byte[] bytes = Bytes.toBytes(value);
		byte[] result = new byte[DEVICE_IDENTIFIER_LENGTH];
		System.arraycopy(bytes, bytes.length - DEVICE_IDENTIFIER_LENGTH, result, 0, DEVICE_IDENTIFIER_LENGTH);
		return result;
	}

	/**
	 * Get row key for a device with the given id.
	 * 
	 * @param deviceId
	 * @return
	 */
	public static byte[] getDeviceRowKey(Long deviceId) {
		ByteBuffer buffer = ByteBuffer.allocate(DEVICE_IDENTIFIER_LENGTH + 1);
		buffer.put(DeviceRecordType.Device.getType());
		buffer.put(getTruncatedIdentifier(deviceId));
		return buffer.array();
	}

	/**
	 * Creates key with an indicator byte followed by the inverted timestamp to order
	 * assignments in most recent to least recent order.
	 * 
	 * @return
	 */
	public static byte[] getNextDeviceAssignmentHistoryKey() {
		long time = System.currentTimeMillis() / 1000;
		byte[] timeBytes = Bytes.toBytes(time);
		ByteBuffer buffer = ByteBuffer.allocate(5);
		buffer.put(ASSIGNMENT_HISTORY_INDICATOR);
		buffer.put((byte) ~timeBytes[4]);
		buffer.put((byte) ~timeBytes[5]);
		buffer.put((byte) ~timeBytes[6]);
		buffer.put((byte) ~timeBytes[7]);
		return buffer.array();
	}
}