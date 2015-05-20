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
import com.openiot.device.marshaling.DeviceAssignmentMarshalHelper;
import com.openiot.hbase.IHBaseContext;
import com.openiot.hbase.IOpenIoTHBase;
import com.openiot.hbase.common.HBaseUtils;
import com.openiot.hbase.encoder.PayloadMarshalerResolver;
import com.openiot.hbase.uid.IdManager;
import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.rest.model.device.Device;
import com.openiot.rest.model.device.DeviceAssignment;
import com.openiot.rest.model.device.DeviceAssignmentState;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.common.IMetadataProvider;
import com.openiot.spi.device.DeviceAssignmentStatus;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.IDeviceAssignmentState;
import com.openiot.spi.device.request.IDeviceAssignmentCreateRequest;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.server.debug.TracerCategory;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * HBase specifics for dealing with OpenIoT device assignments.
 * 
 * @author Derek
 */
public class HBaseDeviceAssignment {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(HBaseDeviceAssignment.class);

	/** Length of device identifier (subset of 8 byte long) */
	public static final int ASSIGNMENT_IDENTIFIER_LENGTH = 4;

	/** Qualifier for assignment status */
	public static final byte[] ASSIGNMENT_STATUS = Bytes.toBytes("status");

	/** Qualifier for assignment state */
	public static final byte[] ASSIGNMENT_STATE = Bytes.toBytes("state");

	/** Used for cloning device assignment results */
	private static DeviceAssignmentMarshalHelper ASSIGNMENT_HELPER =
			new DeviceAssignmentMarshalHelper().setIncludeAsset(false).setIncludeDevice(false).setIncludeSite(
					false);

	/**
	 * Create a new device assignment.
	 * 
	 * @param context
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceAssignment createDeviceAssignment(IHBaseContext context,
			IDeviceAssignmentCreateRequest request) throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "createDeviceAssignment (HBase)", LOGGER);
		try {
			Device device = HBaseDevice.getDeviceByHardwareId(context, request.getDeviceHardwareId());
			if (device == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidHardwareId, ErrorLevel.ERROR);
			}
			Long siteId = IdManager.getInstance().getSiteKeys().getValue(device.getSiteToken());
			if (siteId == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.ERROR);
			}
			if (device.getAssignmentToken() != null) {
				throw new OpenIoTSystemException(ErrorCode.DeviceAlreadyAssigned, ErrorLevel.ERROR);
			}
			byte[] baserow = HBaseSite.getAssignmentRowKey(siteId);
			Long assnId = HBaseSite.allocateNextAssignmentId(context, siteId);
			byte[] assnIdBytes = getAssignmentIdentifier(assnId);
			ByteBuffer buffer = ByteBuffer.allocate(baserow.length + assnIdBytes.length);
			buffer.put(baserow);
			buffer.put(assnIdBytes);
			byte[] rowkey = buffer.array();

			// Associate new UUID with assignment row key.
			String uuid = IdManager.getInstance().getAssignmentKeys().createUniqueId(rowkey);

			// Create device assignment for JSON.
			DeviceAssignment newAssignment =
					OpenIoTPersistence.deviceAssignmentCreateLogic(request, device, uuid);
			byte[] payload = context.getPayloadMarshaler().encodeDeviceAssignment(newAssignment);

			HTableInterface sites = null;
			try {
				sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
				Put put = new Put(rowkey);
				HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
				put.add(IOpenIoTHBase.FAMILY_ID, ASSIGNMENT_STATUS,
						DeviceAssignmentStatus.Active.name().getBytes());
				sites.put(put);
			} catch (IOException e) {
				throw new OpenIoTException("Unable to create device assignment.", e);
			} finally {
				HBaseUtils.closeCleanly(sites);
			}

			// Set the back reference from the device that indicates it is currently
			// assigned.
			HBaseDevice.setDeviceAssignment(context, request.getDeviceHardwareId(), uuid);

			return newAssignment;
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Get a device assignment based on its unique token.
	 * 
	 * @param context
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DeviceAssignment getDeviceAssignment(IHBaseContext context, String token)
			throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "getDeviceAssignment (HBase) " + token, LOGGER);
		try {
			if (context.getCacheProvider() != null) {
				IDeviceAssignment result = context.getCacheProvider().getDeviceAssignmentCache().get(token);
				if (result != null) {
					Tracer.info("Returning cached device assignment.", LOGGER);
					return ASSIGNMENT_HELPER.convert(result, OpenIoT.getServer().getAssetModuleManager());
				}
			}
			byte[] rowkey = IdManager.getInstance().getAssignmentKeys().getValue(token);
			if (rowkey == null) {
				return null;
			}

			HTableInterface sites = null;
			try {
				sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
				Get get = new Get(rowkey);
				HBaseUtils.addPayloadFields(get);
				get.addColumn(IOpenIoTHBase.FAMILY_ID, ASSIGNMENT_STATE);
				Result result = sites.get(get);

				byte[] type = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
				byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
				byte[] state = result.getValue(IOpenIoTHBase.FAMILY_ID, ASSIGNMENT_STATE);
				if ((type == null) || (payload == null)) {
					return null;
				}

				DeviceAssignment found =
						PayloadMarshalerResolver.getInstance().getMarshaler(type).decodeDeviceAssignment(
								payload);
				if (state != null) {
					DeviceAssignmentState assnState =
							PayloadMarshalerResolver.getInstance().getMarshaler(type).decodeDeviceAssignmentState(
									state);
					found.setState(assnState);
				}
				if ((context.getCacheProvider() != null) && (found != null)) {
					context.getCacheProvider().getDeviceAssignmentCache().put(token, found);
				}
				return found;
			} catch (IOException e) {
				throw new OpenIoTException("Unable to load device assignment by token.", e);
			} finally {
				HBaseUtils.closeCleanly(sites);
			}
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Update metadata associated with a device assignment.
	 * 
	 * @param context
	 * @param token
	 * @param metadata
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DeviceAssignment updateDeviceAssignmentMetadata(IHBaseContext context, String token,
			IMetadataProvider metadata) throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall,
				"updateDeviceAssignmentMetadata (HBase) " + token, LOGGER);
		try {
			DeviceAssignment updated = getDeviceAssignment(context, token);
			updated.clearMetadata();
			MetadataProvider.copy(metadata, updated);
			OpenIoTPersistence.setUpdatedEntityMetadata(updated);

			byte[] rowkey = IdManager.getInstance().getAssignmentKeys().getValue(token);
			byte[] payload = context.getPayloadMarshaler().encodeDeviceAssignment(updated);

			HTableInterface sites = null;
			try {
				sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
				Put put = new Put(rowkey);
				HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
				sites.put(put);

				// Make sure that cache is using updated assignment information.
				if (context.getCacheProvider() != null) {
					context.getCacheProvider().getDeviceAssignmentCache().put(updated.getToken(), updated);
				}
			} catch (IOException e) {
				throw new OpenIoTException("Unable to update device assignment metadata.", e);
			} finally {
				HBaseUtils.closeCleanly(sites);
			}
			return updated;
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Update state associated with device assignment.
	 * 
	 * @param context
	 * @param token
	 * @param state
	 * @param cache
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DeviceAssignment updateDeviceAssignmentState(IHBaseContext context, String token,
			IDeviceAssignmentState state) throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "updateDeviceAssignmentState (HBase) " + token,
				LOGGER);
		try {
			DeviceAssignment updated = getDeviceAssignment(context, token);
			updated.setState(DeviceAssignmentState.copy(state));

			byte[] rowkey = IdManager.getInstance().getAssignmentKeys().getValue(token);
			byte[] updatedState = context.getPayloadMarshaler().encodeDeviceAssignmentState(state);

			HTableInterface sites = null;
			try {
				sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
				Put put = new Put(rowkey);
				put.add(IOpenIoTHBase.FAMILY_ID, ASSIGNMENT_STATE, updatedState);
				sites.put(put);

				// Make sure that cache is using updated assignment information.
				if (context.getCacheProvider() != null) {
					context.getCacheProvider().getDeviceAssignmentCache().put(updated.getToken(), updated);
				}
			} catch (IOException e) {
				throw new OpenIoTException("Unable to update device assignment state.", e);
			} finally {
				HBaseUtils.closeCleanly(sites);
			}
			return updated;
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Update status for a given device assignment.
	 * 
	 * @param context
	 * @param token
	 * @param status
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DeviceAssignment updateDeviceAssignmentStatus(IHBaseContext context, String token,
			DeviceAssignmentStatus status) throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "updateDeviceAssignmentStatus (HBase) " + token,
				LOGGER);
		try {
			DeviceAssignment updated = getDeviceAssignment(context, token);
			updated.setStatus(status);
			OpenIoTPersistence.setUpdatedEntityMetadata(updated);

			byte[] rowkey = IdManager.getInstance().getAssignmentKeys().getValue(token);
			byte[] payload = context.getPayloadMarshaler().encodeDeviceAssignment(updated);

			HTableInterface sites = null;
			try {
				sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
				Put put = new Put(rowkey);
				HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
				put.add(IOpenIoTHBase.FAMILY_ID, ASSIGNMENT_STATUS, status.name().getBytes());
				sites.put(put);

				// Make sure that cache is using updated assignment information.
				if (context.getCacheProvider() != null) {
					context.getCacheProvider().getDeviceAssignmentCache().put(updated.getToken(), updated);
				}
			} catch (IOException e) {
				throw new OpenIoTException("Unable to update device assignment status.", e);
			} finally {
				HBaseUtils.closeCleanly(sites);
			}
			return updated;
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * End a device assignment.
	 * 
	 * @param context
	 * @param token
	 * @param cache
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DeviceAssignment endDeviceAssignment(IHBaseContext context, String token)
			throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "endDeviceAssignment (HBase) " + token, LOGGER);
		try {
			DeviceAssignment updated = getDeviceAssignment(context, token);
			updated.setStatus(DeviceAssignmentStatus.Released);
			updated.setReleasedDate(new Date());
			OpenIoTPersistence.setUpdatedEntityMetadata(updated);

			// Remove assignment reference from device.
			HBaseDevice.removeDeviceAssignment(context, updated.getDeviceHardwareId());

			// Update json and status qualifier.
			byte[] rowkey = IdManager.getInstance().getAssignmentKeys().getValue(token);
			byte[] payload = context.getPayloadMarshaler().encodeDeviceAssignment(updated);

			HTableInterface sites = null;
			try {
				sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
				Put put = new Put(rowkey);
				HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
				put.add(IOpenIoTHBase.FAMILY_ID, ASSIGNMENT_STATUS,
						DeviceAssignmentStatus.Released.name().getBytes());
				sites.put(put);

				// Make sure that cache is using updated assignment information.
				if (context.getCacheProvider() != null) {
					context.getCacheProvider().getDeviceAssignmentCache().put(updated.getToken(), updated);
				}
			} catch (IOException e) {
				throw new OpenIoTException("Unable to update device assignment status.", e);
			} finally {
				HBaseUtils.closeCleanly(sites);
			}
			return updated;
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Delete a device assignmant based on token. Depending on 'force' the record will be
	 * physically deleted or a marker qualifier will be added to mark it as deleted. Note:
	 * Physically deleting an assignment can leave orphaned references and should not be
	 * done in a production system!
	 * 
	 * @param context
	 * @param token
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceAssignment deleteDeviceAssignment(IHBaseContext context, String token, boolean force)
			throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "deleteDeviceAssignment (HBase) " + token, LOGGER);
		try {
			byte[] assnId = IdManager.getInstance().getAssignmentKeys().getValue(token);
			if (assnId == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidDeviceAssignmentToken, ErrorLevel.ERROR);
			}
			DeviceAssignment existing = getDeviceAssignment(context, token);
			existing.setDeleted(true);
			try {
				HBaseDevice.removeDeviceAssignment(context, existing.getDeviceHardwareId());
			} catch (OpenIoTSystemException e) {
				// Ignore missing reference to handle case where device was deleted
				// underneath
				// assignment.
			}
			if (force) {
				IdManager.getInstance().getAssignmentKeys().delete(token);
				HTableInterface sites = null;
				try {
					Delete delete = new Delete(assnId);
					sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
					sites.delete(delete);
				} catch (IOException e) {
					throw new OpenIoTException("Unable to delete device.", e);
				} finally {
					HBaseUtils.closeCleanly(sites);
				}
			} else {
				byte[] marker = { (byte) 0x01 };
				OpenIoTPersistence.setUpdatedEntityMetadata(existing);
				byte[] updated = context.getPayloadMarshaler().encodeDeviceAssignment(existing);
				HTableInterface sites = null;
				try {
					sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
					Put put = new Put(assnId);
					put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE,
							context.getPayloadMarshaler().getEncoding().getIndicator());
					put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD, updated);
					put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.DELETED, marker);
					sites.put(put);
				} catch (IOException e) {
					throw new OpenIoTException("Unable to set deleted flag for device assignment.", e);
				} finally {
					HBaseUtils.closeCleanly(sites);
				}
			}
			return existing;
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Truncate assignment id value to expected length. This will be a subset of the full
	 * 8-bit long value.
	 * 
	 * @param value
	 * @return
	 */
	public static byte[] getAssignmentIdentifier(Long value) {
		byte[] bytes = Bytes.toBytes(value);
		byte[] result = new byte[ASSIGNMENT_IDENTIFIER_LENGTH];
		System.arraycopy(bytes, bytes.length - ASSIGNMENT_IDENTIFIER_LENGTH, result, 0,
				ASSIGNMENT_IDENTIFIER_LENGTH);
		return result;
	}
}