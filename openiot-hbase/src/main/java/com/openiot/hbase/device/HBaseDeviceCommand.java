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
import com.openiot.hbase.encoder.PayloadMarshalerResolver;
import com.openiot.hbase.uid.IdManager;
import com.openiot.rest.model.device.command.DeviceCommand;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.IDeviceSpecification;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.request.IDeviceCommandCreateRequest;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import org.apache.hadoop.hbase.client.*;

import java.io.IOException;
import java.util.*;

/**
 * HBase specifics for dealing with OpenIoT device commands.
 * 
 * @author Derek
 */
public class HBaseDeviceCommand {

	/**
	 * Create a new device command for an existing device specification.
	 * 
	 * @param context
	 * @param spec
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceCommand createDeviceCommand(IHBaseContext context, IDeviceSpecification spec,
			IDeviceCommandCreateRequest request) throws OpenIoTException {
		Long specId = IdManager.getInstance().getSpecificationKeys().getValue(spec.getToken());
		if (specId == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceSpecificationToken, ErrorLevel.ERROR);
		}
		String uuid = ((request.getToken() != null) ? request.getToken() : UUID.randomUUID().toString());

		// Use common logic so all backend implementations work the same.
		List<IDeviceCommand> existing = listDeviceCommands(context, spec.getToken(), false);
		DeviceCommand command = OpenIoTPersistence.deviceCommandCreateLogic(spec, request, uuid, existing);

		// Create unique row for new device.
		Long nextId = HBaseDeviceSpecification.allocateNextCommandId(context, specId);
		byte[] rowkey = HBaseDeviceSpecification.getDeviceCommandRowKey(specId, nextId);
		IdManager.getInstance().getCommandKeys().create(uuid, rowkey);

		return putDeviceCommandPayload(context, command);
	}

	/**
	 * List device commands that match the given criteria.
	 * 
	 * @param context
	 * @param specToken
	 * @param includeDeleted
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static List<IDeviceCommand> listDeviceCommands(IHBaseContext context, String specToken,
			boolean includeDeleted) throws OpenIoTException {
		List<IDeviceCommand> matches = getFilteredDeviceCommands(context, specToken, includeDeleted);
		Collections.sort(matches, new Comparator<IDeviceCommand>() {

			@Override
			public int compare(IDeviceCommand a, IDeviceCommand b) {
				return a.getCreatedDate().compareTo(b.getCreatedDate());
			}
		});
		return matches;
	}

	/**
	 * Get device commands that correspond to the given criteria.
	 * 
	 * @param context
	 * @param specToken
	 * @param includeDeleted
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static List<IDeviceCommand> getFilteredDeviceCommands(IHBaseContext context, String specToken,
			boolean includeDeleted) throws OpenIoTException {
		Long specId = IdManager.getInstance().getSpecificationKeys().getValue(specToken);
		if (specId == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceSpecificationToken, ErrorLevel.ERROR);
		}

		HTableInterface devices = null;
		ResultScanner scanner = null;

		try {
			devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
			Scan scan = new Scan();
			scan.setStartRow(HBaseDeviceSpecification.getDeviceCommandRowPrefix(specId));
			scan.setStopRow(HBaseDeviceSpecification.getEndRowPrefix(specId));
			scanner = devices.getScanner(scan);

			List<IDeviceCommand> results = new ArrayList<IDeviceCommand>();
			for (Result result : scanner) {
				boolean shouldAdd = true;
				byte[] type = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
				byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
				byte[] deleted = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.DELETED);

				if ((deleted != null) && (!includeDeleted)) {
					shouldAdd = false;
				}

				if ((shouldAdd) && (type != null) && (payload != null)) {
					results.add(PayloadMarshalerResolver.getInstance().getMarshaler(type).decodeDeviceCommand(
							payload));
				}
			}
			return results;
		} catch (IOException e) {
			throw new OpenIoTException("Error scanning device command rows.", e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			HBaseUtils.closeCleanly(devices);
		}
	}

	/**
	 * Get a device command by unique token.
	 * 
	 * @param context
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DeviceCommand getDeviceCommandByToken(IHBaseContext context, String token)
			throws OpenIoTException {
		byte[] rowkey = IdManager.getInstance().getCommandKeys().getValue(token);
		if (rowkey == null) {
			return null;
		}

		HTableInterface devices = null;
		try {
			devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
			Get get = new Get(rowkey);
			get.addColumn(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
			get.addColumn(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
			Result result = devices.get(get);

			byte[] type = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
			byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
			if ((type == null) || (payload == null)) {
				return null;
			}

			return PayloadMarshalerResolver.getInstance().getMarshaler(type).decodeDeviceCommand(payload);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to load device command by token.", e);
		} finally {
			HBaseUtils.closeCleanly(devices);
		}
	}

	/**
	 * Update an existing device command.
	 * 
	 * @param context
	 * @param token
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DeviceCommand updateDeviceCommand(IHBaseContext context, String token,
			IDeviceCommandCreateRequest request) throws OpenIoTException {
		DeviceCommand updated = assertDeviceCommand(context, token);
		List<IDeviceCommand> existing = listDeviceCommands(context, updated.getSpecificationToken(), false);
		OpenIoTPersistence.deviceCommandUpdateLogic(request, updated, existing);
		return putDeviceCommandPayload(context, updated);
	}

	/**
	 * Delete an existing device command (or mark as deleted if 'force' is not true).
	 * 
	 * @param context
	 * @param token
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceCommand deleteDeviceCommand(IHBaseContext context, String token, boolean force)
			throws OpenIoTException {
		DeviceCommand existing = assertDeviceCommand(context, token);
		existing.setDeleted(true);

		byte[] rowkey = IdManager.getInstance().getCommandKeys().getValue(token);
		if (force) {
			IdManager.getInstance().getSpecificationKeys().delete(token);
			HTableInterface devices = null;
			try {
				Delete delete = new Delete(rowkey);
				devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
				devices.delete(delete);
			} catch (IOException e) {
				throw new OpenIoTException("Unable to delete device command.", e);
			} finally {
				HBaseUtils.closeCleanly(devices);
			}
		} else {
			byte[] marker = { (byte) 0x01 };
			OpenIoTPersistence.setUpdatedEntityMetadata(existing);
			byte[] updated = context.getPayloadMarshaler().encodeDeviceCommand(existing);

			HTableInterface devices = null;
			try {
				devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
				Put put = new Put(rowkey);
				HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, updated);
				put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.DELETED, marker);
				devices.put(put);
			} catch (IOException e) {
				throw new OpenIoTException("Unable to set deleted flag for device command.", e);
			} finally {
				HBaseUtils.closeCleanly(devices);
			}
		}
		return existing;
	}

	/**
	 * Gets a device command by token or throws an exception if not found.
	 * 
	 * @param context
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DeviceCommand assertDeviceCommand(IHBaseContext context, String token)
			throws OpenIoTException {
		DeviceCommand existing = getDeviceCommandByToken(context, token);
		if (existing == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceCommandToken, ErrorLevel.ERROR);
		}
		return existing;
	}

	/**
	 * Save payload for device command.
	 * 
	 * @param context
	 * @param command
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DeviceCommand putDeviceCommandPayload(IHBaseContext context, DeviceCommand command)
			throws OpenIoTException {
		byte[] rowkey = IdManager.getInstance().getCommandKeys().getValue(command.getToken());
		byte[] payload = context.getPayloadMarshaler().encodeDeviceCommand(command);

		HTableInterface devices = null;
		try {
			devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
			Put put = new Put(rowkey);
			HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
			devices.put(put);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to put device command data.", e);
		} finally {
			HBaseUtils.closeCleanly(devices);
		}

		return command;
	}
}