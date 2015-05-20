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
import com.openiot.rest.model.device.Zone;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.ISite;
import com.openiot.spi.device.IZone;
import com.openiot.spi.device.request.IZoneCreateRequest;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * HBase specifics for dealing with OpenIoT zones.
 * 
 * @author Derek
 */
public class HBaseZone {

	/** Length of site identifier (subset of 8 byte long) */
	public static final int ZONE_IDENTIFIER_LENGTH = 4;

	/**
	 * Create a new zone.
	 * 
	 * @param context
	 * @param site
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IZone createZone(IHBaseContext context, ISite site, IZoneCreateRequest request)
			throws OpenIoTException {
		Long siteId = IdManager.getInstance().getSiteKeys().getValue(site.getToken());
		if (siteId == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.ERROR);
		}
		Long zoneId = HBaseSite.allocateNextZoneId(context, siteId);
		byte[] rowkey = getPrimaryRowkey(siteId, zoneId);

		// Associate new UUID with zone row key.
		String uuid = IdManager.getInstance().getZoneKeys().createUniqueId(rowkey);

		// Use common processing logic so all backend implementations work the same.
		Zone zone = OpenIoTPersistence.zoneCreateLogic(request, site.getToken(), uuid);

		byte[] payload = context.getPayloadMarshaler().encodeZone(zone);

		HTableInterface sites = null;
		try {
			sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
			Put put = new Put(rowkey);
			HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
			sites.put(put);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to create zone.", e);
		} finally {
			HBaseUtils.closeCleanly(sites);
		}

		return zone;
	}

	/**
	 * Update an existing zone.
	 * 
	 * @param context
	 * @param token
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static Zone updateZone(IHBaseContext context, String token, IZoneCreateRequest request)
			throws OpenIoTException {
		Zone updated = getZone(context, token);

		// Use common update logic so that backend implemetations act the same way.
		OpenIoTPersistence.zoneUpdateLogic(request, updated);

		byte[] zoneId = IdManager.getInstance().getZoneKeys().getValue(token);
		byte[] payload = context.getPayloadMarshaler().encodeZone(updated);

		HTableInterface sites = null;
		try {
			sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
			Put put = new Put(zoneId);
			HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
			sites.put(put);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to update zone.", e);
		} finally {
			HBaseUtils.closeCleanly(sites);
		}
		return updated;
	}

	/**
	 * Get a zone by unique token.
	 * 
	 * @param context
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static Zone getZone(IHBaseContext context, String token) throws OpenIoTException {
		byte[] rowkey = IdManager.getInstance().getZoneKeys().getValue(token);
		if (rowkey == null) {
			return null;
		}

		HTableInterface sites = null;
		try {
			sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
			Get get = new Get(rowkey);
			HBaseUtils.addPayloadFields(get);
			Result result = sites.get(get);

			byte[] type = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
			byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
			if ((type == null) || (payload == null)) {
				return null;
			}

			return PayloadMarshalerResolver.getInstance().getMarshaler(type).decodeZone(payload);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to load zone by token.", e);
		} finally {
			HBaseUtils.closeCleanly(sites);
		}
	}

	/**
	 * Delete an existing zone.
	 * 
	 * @param context
	 * @param token
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static Zone deleteZone(IHBaseContext context, String token, boolean force)
			throws OpenIoTException {
		byte[] zoneId = IdManager.getInstance().getZoneKeys().getValue(token);
		if (zoneId == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidZoneToken, ErrorLevel.ERROR);
		}
		Zone existing = getZone(context, token);
		existing.setDeleted(true);
		if (force) {
			IdManager.getInstance().getZoneKeys().delete(token);
			HTableInterface sites = null;
			try {
				Delete delete = new Delete(zoneId);
				sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
				sites.delete(delete);
			} catch (IOException e) {
				throw new OpenIoTException("Unable to delete zone.", e);
			} finally {
				HBaseUtils.closeCleanly(sites);
			}
		} else {
			byte[] marker = { (byte) 0x01 };
			OpenIoTPersistence.setUpdatedEntityMetadata(existing);
			byte[] payload = context.getPayloadMarshaler().encodeZone(existing);
			HTableInterface sites = null;
			try {
				sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
				Put put = new Put(zoneId);
				HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
				put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.DELETED, marker);
				sites.put(put);
			} catch (IOException e) {
				throw new OpenIoTException("Unable to set deleted flag for zone.", e);
			} finally {
				HBaseUtils.closeCleanly(sites);
			}
		}
		return existing;
	}

	/**
	 * Get primary row key for a given zone.
	 * 
	 * @param siteId
	 * @return
	 */
	public static byte[] getPrimaryRowkey(Long siteId, Long zoneId) {
		byte[] baserow = HBaseSite.getZoneRowKey(siteId);
		byte[] zoneIdBytes = getZoneIdentifier(zoneId);
		ByteBuffer buffer = ByteBuffer.allocate(baserow.length + zoneIdBytes.length);
		buffer.put(baserow);
		buffer.put(zoneIdBytes);
		return buffer.array();
	}

	/**
	 * Truncate zone id value to expected length. This will be a subset of the full 8-bit
	 * long value.
	 * 
	 * @param value
	 * @return
	 */
	public static byte[] getZoneIdentifier(Long value) {
		byte[] bytes = Bytes.toBytes(value);
		byte[] result = new byte[ZONE_IDENTIFIER_LENGTH];
		System.arraycopy(bytes, bytes.length - ZONE_IDENTIFIER_LENGTH, result, 0, ZONE_IDENTIFIER_LENGTH);
		return result;
	}
}