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
import com.openiot.hbase.uid.IdManager;
import com.openiot.hbase.uid.UniqueIdCounterMap;
import com.openiot.hbase.uid.UniqueIdCounterMapRowKeyBuilder;
import com.openiot.rest.model.device.group.DeviceGroup;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.common.IFilter;
import com.openiot.spi.device.group.IDeviceGroup;
import com.openiot.spi.device.request.IDeviceGroupCreateRequest;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.search.ISearchCriteria;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * HBase specifics for dealing with OpenIoT device groups.
 * 
 * @author Derek
 */
public class HBaseDeviceGroup {

	/** Length of group identifier (subset of 8 byte long) */
	public static final int IDENTIFIER_LENGTH = 4;

	/** Column qualifier for group entry counter */
	public static final byte[] ENTRY_COUNTER = Bytes.toBytes("entryctr");

	/** Used to look up row keys from tokens */
	public static UniqueIdCounterMapRowKeyBuilder KEY_BUILDER = new UniqueIdCounterMapRowKeyBuilder() {

		@Override
		public UniqueIdCounterMap getMap() {
			return IdManager.getInstance().getDeviceGroupKeys();
		}

		@Override
		public byte getTypeIdentifier() {
			return DeviceRecordType.DeviceGroup.getType();
		}

		@Override
		public byte getPrimaryIdentifier() {
			return DeviceGroupRecordType.DeviceGroup.getType();
		}

		@Override
		public int getKeyIdLength() {
			return 4;
		}

		@Override
		public void throwInvalidKey() throws OpenIoTException {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceGroupToken, ErrorLevel.ERROR);
		}
	};

	/**
	 * Create a device group.
	 * 
	 * @param context
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceGroup createDeviceGroup(IHBaseContext context, IDeviceGroupCreateRequest request)
			throws OpenIoTException {
		String uuid = null;
		if (request.getToken() != null) {
			uuid = KEY_BUILDER.getMap().useExistingId(request.getToken());
		} else {
			uuid = KEY_BUILDER.getMap().createUniqueId();
		}

		// Use common logic so all backend implementations work the same.
		DeviceGroup group = OpenIoTPersistence.deviceGroupCreateLogic(request, uuid);

		Map<byte[], byte[]> qualifiers = new HashMap<byte[], byte[]>();
		byte[] zero = Bytes.toBytes((long) 0);
		qualifiers.put(ENTRY_COUNTER, zero);
		return HBaseUtils.createOrUpdate(context.getClient(), context.getPayloadMarshaler(),
				IOpenIoTHBase.DEVICES_TABLE_NAME, group, uuid, KEY_BUILDER, qualifiers);
	}

	/**
	 * Update device group information.
	 * 
	 * @param context
	 * @param token
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceGroup updateDeviceGroup(IHBaseContext context, String token,
			IDeviceGroupCreateRequest request) throws OpenIoTException {
		DeviceGroup updated = assertDeviceGroup(context, token);
		OpenIoTPersistence.deviceGroupUpdateLogic(request, updated);
		return HBaseUtils.put(context.getClient(), context.getPayloadMarshaler(),
				IOpenIoTHBase.DEVICES_TABLE_NAME, updated, token, KEY_BUILDER);
	}

	/**
	 * Get a {@link DeviceGroup} by unique token. s *
	 * 
	 * @param context
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DeviceGroup getDeviceGroupByToken(IHBaseContext context, String token)
			throws OpenIoTException {
		return HBaseUtils.get(context.getClient(), IOpenIoTHBase.DEVICES_TABLE_NAME, token, KEY_BUILDER,
				DeviceGroup.class);
	}

	/**
	 * Get paged {@link IDeviceGroup} results based on the given search criteria.
	 * 
	 * @param context
	 * @param includeDeleted
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceGroup> listDeviceGroups(IHBaseContext context, boolean includeDeleted,
			ISearchCriteria criteria) throws OpenIoTException {
		Comparator<DeviceGroup> comparator = new Comparator<DeviceGroup>() {

			public int compare(DeviceGroup a, DeviceGroup b) {
				return -1 * (a.getCreatedDate().compareTo(b.getCreatedDate()));
			}

		};
		IFilter<DeviceGroup> filter = new IFilter<DeviceGroup>() {

			public boolean isExcluded(DeviceGroup item) {
				return false;
			}
		};
		return HBaseUtils.getFilteredList(context.getClient(), IOpenIoTHBase.DEVICES_TABLE_NAME,
				KEY_BUILDER, includeDeleted, IDeviceGroup.class, DeviceGroup.class, filter, criteria,
				comparator);
	}

	/**
	 * Get paged {@link IDeviceGroup} results for groups that have a given role based on
	 * the given search criteria.
	 * 
	 * @param context
	 * @param role
	 * @param includeDeleted
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceGroup> listDeviceGroupsWithRole(IHBaseContext context,
			final String role, boolean includeDeleted, ISearchCriteria criteria) throws OpenIoTException {
		Comparator<DeviceGroup> comparator = new Comparator<DeviceGroup>() {

			public int compare(DeviceGroup a, DeviceGroup b) {
				return -1 * (a.getCreatedDate().compareTo(b.getCreatedDate()));
			}

		};
		IFilter<DeviceGroup> filter = new IFilter<DeviceGroup>() {

			public boolean isExcluded(DeviceGroup item) {
				return !item.getRoles().contains(role);
			}
		};
		return HBaseUtils.getFilteredList(context.getClient(), IOpenIoTHBase.DEVICES_TABLE_NAME,
				KEY_BUILDER, includeDeleted, IDeviceGroup.class, DeviceGroup.class, filter, criteria,
				comparator);
	}

	/**
	 * Allocates the next available group element id.
	 * 
	 * @param context
	 * @param primary
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static Long allocateNextElementId(IHBaseContext context, byte[] primary) throws OpenIoTException {
		HTableInterface devices = null;
		try {
			devices = context.getClient().getTableInterface(IOpenIoTHBase.DEVICES_TABLE_NAME);
			Increment increment = new Increment(primary);
			increment.addColumn(IOpenIoTHBase.FAMILY_ID, ENTRY_COUNTER, 1);
			Result result = devices.increment(increment);
			return Bytes.toLong(result.value());
		} catch (IOException e) {
			throw new OpenIoTException("Unable to allocate next group element id.", e);
		} finally {
			HBaseUtils.closeCleanly(devices);
		}
	}

	/**
	 * Delete an existing device group.
	 * 
	 * @param context
	 * @param token
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceGroup deleteDeviceGroup(IHBaseContext context, String token, boolean force)
			throws OpenIoTException {
		// If actually deleting group, delete all group elements.
		if (force) {
			HBaseDeviceGroupElement.deleteElements(context, token);
		}
		return HBaseUtils.delete(context.getClient(), context.getPayloadMarshaler(),
				IOpenIoTHBase.DEVICES_TABLE_NAME, token, force, KEY_BUILDER, DeviceGroup.class);
	}

	/**
	 * Get a {@link DeviceGroup} by token or throw an exception if token is not valid.
	 * 
	 * @param context
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DeviceGroup assertDeviceGroup(IHBaseContext context, String token)
			throws OpenIoTException {
		DeviceGroup existing = getDeviceGroupByToken(context, token);
		if (existing == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceGroupToken, ErrorLevel.ERROR);
		}
		return existing;
	}

	/**
	 * Get the unique device identifier based on the long value associated with the device
	 * group UUID. This will be a subset of the full 8-bit long value.
	 * 
	 * @param value
	 * @return
	 */
	public static byte[] getTruncatedIdentifier(Long value) {
		byte[] bytes = Bytes.toBytes(value);
		byte[] result = new byte[IDENTIFIER_LENGTH];
		System.arraycopy(bytes, bytes.length - IDENTIFIER_LENGTH, result, 0, IDENTIFIER_LENGTH);
		return result;
	}

	/**
	 * Get row key for a device group with the given internal id.
	 * 
	 * @param groupId
	 * @return
	 */
	public static byte[] getPrimaryRowKey(Long groupId) {
		ByteBuffer buffer = ByteBuffer.allocate(IDENTIFIER_LENGTH + 2);
		buffer.put(DeviceRecordType.DeviceGroup.getType());
		buffer.put(getTruncatedIdentifier(groupId));
		buffer.put(DeviceGroupRecordType.DeviceGroup.getType());
		return buffer.array();
	}
}