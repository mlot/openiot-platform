/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.device;

import com.openiot.Tracer;
import com.openiot.core.OpenIoTPersistence;
import com.openiot.hbase.IHBaseContext;
import com.openiot.hbase.IOpenIoTHBase;
import com.openiot.hbase.common.HBaseUtils;
import com.openiot.hbase.common.Pager;
import com.openiot.hbase.encoder.PayloadMarshalerResolver;
import com.openiot.hbase.uid.IdManager;
import com.openiot.rest.model.device.DeviceAssignment;
import com.openiot.rest.model.device.Site;
import com.openiot.rest.model.device.Zone;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.ISite;
import com.openiot.spi.device.IZone;
import com.openiot.spi.device.request.ISiteCreateRequest;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.search.ISearchCriteria;
import com.openiot.spi.server.debug.TracerCategory;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.ByteArrayComparable;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * HBase specifics for dealing with OpenIoT sites.
 * 
 * @author Derek
 */
public class HBaseSite {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(HBaseSite.class);

	/** Length of site identifier (subset of 8 byte long) */
	public static final int SITE_IDENTIFIER_LENGTH = 2;

	/** Column qualifier for zone counter */
	public static final byte[] ZONE_COUNTER = Bytes.toBytes("zonectr");

	/** Column qualifier for assignment counter */
	public static final byte[] ASSIGNMENT_COUNTER = Bytes.toBytes("assnctr");

	/** Regex for getting site rows */
	public static final String REGEX_SITE = "^.{2}$";

	/**
	 * Create a new site.
	 * 
	 * @param context
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static ISite createSite(IHBaseContext context, ISiteCreateRequest request)
			throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "createSite (HBase)", LOGGER);
		try {
			String uuid = IdManager.getInstance().getSiteKeys().createUniqueId();
			Long value = IdManager.getInstance().getSiteKeys().getValue(uuid);
			byte[] primary = getPrimaryRowkey(value);

			// Use common logic so all backend implementations work the same.
			Site site = OpenIoTPersistence.siteCreateLogic(request, uuid);

			// Create primary site record.
			byte[] payload = context.getPayloadMarshaler().encodeSite(site);
			byte[] maxLong = Bytes.toBytes(Long.MAX_VALUE);

			HTableInterface sites = null;
			try {
				sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
				Put put = new Put(primary);
				HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
				put.add(IOpenIoTHBase.FAMILY_ID, ZONE_COUNTER, maxLong);
				put.add(IOpenIoTHBase.FAMILY_ID, ASSIGNMENT_COUNTER, maxLong);
				sites.put(put);
			} catch (IOException e) {
				throw new OpenIoTException("Unable to create site.", e);
			} finally {
				HBaseUtils.closeCleanly(sites);
			}
			return site;
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Get a site based on unique token.
	 * 
	 * @param context
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static Site getSiteByToken(IHBaseContext context, String token) throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "getSiteByToken (HBase) " + token, LOGGER);
		try {
			if (context.getCacheProvider() != null) {
				ISite result = context.getCacheProvider().getSiteCache().get(token);
				if (result != null) {
					Tracer.info("Returning cached site.", LOGGER);
					return Site.copy(result);
				}
			}
			Long siteId = IdManager.getInstance().getSiteKeys().getValue(token);
			if (siteId == null) {
				return null;
			}
			byte[] primary = getPrimaryRowkey(siteId);
			HTableInterface sites = null;
			try {
				sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
				Get get = new Get(primary);
				HBaseUtils.addPayloadFields(get);
				Result result = sites.get(get);

				byte[] type = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
				byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
				if ((type == null) || (payload == null)) {
					throw new OpenIoTException("Payload fields not found for site.");
				}

				Site site = PayloadMarshalerResolver.getInstance().getMarshaler(type).decodeSite(payload);
				if (context.getCacheProvider() != null) {
					context.getCacheProvider().getSiteCache().put(token, site);
				}
				return site;
			} catch (IOException e) {
				throw new OpenIoTException("Unable to load site by token.", e);
			} finally {
				HBaseUtils.closeCleanly(sites);
			}
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Update information for an existing site.
	 * 
	 * @param context
	 * @param token
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static Site updateSite(IHBaseContext context, String token, ISiteCreateRequest request)
			throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "updateSite (HBase) " + token, LOGGER);
		try {
			Site updated = getSiteByToken(context, token);
			if (updated == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.ERROR);
			}

			// Use common update logic so that backend implemetations act the same way.
			OpenIoTPersistence.siteUpdateLogic(request, updated);

			Long siteId = IdManager.getInstance().getSiteKeys().getValue(token);
			byte[] rowkey = getPrimaryRowkey(siteId);
			byte[] payload = context.getPayloadMarshaler().encodeSite(updated);

			HTableInterface sites = null;
			try {
				sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
				Put put = new Put(rowkey);
				HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
				sites.put(put);
				if (context.getCacheProvider() != null) {
					context.getCacheProvider().getSiteCache().put(token, updated);
				}
			} catch (IOException e) {
				throw new OpenIoTException("Unable to update site.", e);
			} finally {
				HBaseUtils.closeCleanly(sites);
			}
			return updated;
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * List all sites that match the given criteria.
	 * 
	 * @param context
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<ISite> listSites(IHBaseContext context, ISearchCriteria criteria)
			throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "listSites (HBase)", LOGGER);
		try {
			RegexStringComparator comparator = new RegexStringComparator(REGEX_SITE);
			Pager<ISite> pager =
					getFilteredSiteRows(context, false, criteria, comparator, null, null, Site.class,
							ISite.class);
			return new SearchResults<ISite>(pager.getResults());
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * List device assignments for a given site.
	 * 
	 * @param context
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceAssignment> listDeviceAssignmentsForSite(IHBaseContext context,
			String siteToken, ISearchCriteria criteria) throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "listDeviceAssignmentsForSite (HBase) "
				+ siteToken, LOGGER);
		try {
			Long siteId = IdManager.getInstance().getSiteKeys().getValue(siteToken);
			if (siteId == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.ERROR);
			}
			byte[] assnPrefix = getAssignmentRowKey(siteId);
			byte[] after = getAfterAssignmentRowKey(siteId);
			BinaryPrefixComparator comparator = new BinaryPrefixComparator(assnPrefix);
			Pager<IDeviceAssignment> pager =
					getFilteredSiteRows(context, false, criteria, comparator, assnPrefix, after,
							DeviceAssignment.class, IDeviceAssignment.class);
			return new SearchResults<IDeviceAssignment>(pager.getResults());
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * List zones for a given site.
	 * 
	 * @param context
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IZone> listZonesForSite(IHBaseContext context, String siteToken,
			ISearchCriteria criteria) throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "listZonesForSite (HBase) " + siteToken, LOGGER);
		try {
			Long siteId = IdManager.getInstance().getSiteKeys().getValue(siteToken);
			if (siteId == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.ERROR);
			}
			byte[] zonePrefix = getZoneRowKey(siteId);
			byte[] after = getAssignmentRowKey(siteId);
			BinaryPrefixComparator comparator = new BinaryPrefixComparator(zonePrefix);
			Pager<IZone> pager =
					getFilteredSiteRows(context, false, criteria, comparator, zonePrefix, after, Zone.class,
							IZone.class);
			return new SearchResults<IZone>(pager.getResults());
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Get filtered results from the Site table.
	 * 
	 * @param context
	 * @param includeDeleted
	 * @param criteria
	 * @param comparator
	 * @param startRow
	 * @param stopRow
	 * @param type
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@SuppressWarnings("unchecked")
	public static <T, I> Pager<I> getFilteredSiteRows(IHBaseContext context, boolean includeDeleted,
			ISearchCriteria criteria, ByteArrayComparable comparator, byte[] startRow, byte[] stopRow,
			Class<T> type, Class<I> iface) throws OpenIoTException {
		HTableInterface sites = null;
		ResultScanner scanner = null;
		try {
			sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
			RowFilter matcher = new RowFilter(CompareOp.EQUAL, comparator);
			Scan scan = new Scan();
			if (startRow != null) {
				scan.setStartRow(startRow);
			}
			if (stopRow != null) {
				scan.setStopRow(stopRow);
			}
			scan.setFilter(matcher);
			scanner = sites.getScanner(scan);

			Pager<I> pager = new Pager<I>(criteria);
			for (Result result : scanner) {
				boolean shouldAdd = true;
				byte[] payloadType = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
				byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
				byte[] deleted = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.DELETED);

				if ((deleted != null) && (!includeDeleted)) {
					shouldAdd = false;
				}

				if ((shouldAdd) && (payloadType != null) && (payload != null)) {
					pager.process((I) PayloadMarshalerResolver.getInstance().getMarshaler(payloadType).decode(
							payload, type));
				}
			}
			return pager;
		} catch (IOException e) {
			throw new OpenIoTException("Error scanning site rows.", e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			HBaseUtils.closeCleanly(sites);
		}
	}

	/**
	 * Delete an existing site.
	 * 
	 * @param context
	 * @param token
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static Site deleteSite(IHBaseContext context, String token, boolean force)
			throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "deleteSite (HBase) " + token, LOGGER);
		try {
			Site existing = getSiteByToken(context, token);
			if (existing == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.ERROR);
			}
			existing.setDeleted(true);

			Long siteId = IdManager.getInstance().getSiteKeys().getValue(token);
			byte[] rowkey = getPrimaryRowkey(siteId);
			if (force) {
				IdManager.getInstance().getSiteKeys().delete(token);
				HTableInterface sites = null;
				try {
					Delete delete = new Delete(rowkey);
					sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
					sites.delete(delete);
					if (context.getCacheProvider() != null) {
						context.getCacheProvider().getSiteCache().remove(token);
					}
				} catch (IOException e) {
					throw new OpenIoTException("Unable to delete site.", e);
				} finally {
					HBaseUtils.closeCleanly(sites);
				}
			} else {
				byte[] marker = { (byte) 0x01 };
				OpenIoTPersistence.setUpdatedEntityMetadata(existing);
				byte[] updated = context.getPayloadMarshaler().encodeSite(existing);
				HTableInterface sites = null;
				try {
					sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
					Put put = new Put(rowkey);
					HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, updated);
					put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.DELETED, marker);
					sites.put(put);
					if (context.getCacheProvider() != null) {
						context.getCacheProvider().getSiteCache().remove(token);
					}
				} catch (IOException e) {
					throw new OpenIoTException("Unable to set deleted flag for site.", e);
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
	 * Allocate the next zone id and return the new value. (Each id is less than the last)
	 * 
	 * @param context
	 * @param siteId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static Long allocateNextZoneId(IHBaseContext context, Long siteId) throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "allocateNextZoneId (HBase)", LOGGER);
		try {
			byte[] primary = getPrimaryRowkey(siteId);
			HTableInterface sites = null;
			try {
				sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
				Increment increment = new Increment(primary);
				increment.addColumn(IOpenIoTHBase.FAMILY_ID, ZONE_COUNTER, -1);
				Result result = sites.increment(increment);
				return Bytes.toLong(result.value());
			} catch (IOException e) {
				throw new OpenIoTException("Unable to allocate next zone id.", e);
			} finally {
				HBaseUtils.closeCleanly(sites);
			}
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Allocate the next assignment id and return the new value. (Each id is less than the
	 * last)
	 * 
	 * @param context
	 * @param siteId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static Long allocateNextAssignmentId(IHBaseContext context, Long siteId) throws OpenIoTException {
		Tracer.push(TracerCategory.DeviceManagementApiCall, "allocateNextAssignmentId (HBase)", LOGGER);
		try {
			byte[] primary = getPrimaryRowkey(siteId);
			HTableInterface sites = null;
			try {
				sites = context.getClient().getTableInterface(IOpenIoTHBase.SITES_TABLE_NAME);
				Increment increment = new Increment(primary);
				increment.addColumn(IOpenIoTHBase.FAMILY_ID, ASSIGNMENT_COUNTER, -1);
				Result result = sites.increment(increment);
				return Bytes.toLong(result.value());
			} catch (IOException e) {
				throw new OpenIoTException("Unable to allocate next assignment id.", e);
			} finally {
				HBaseUtils.closeCleanly(sites);
			}
		} finally {
			Tracer.pop(LOGGER);
		}
	}

	/**
	 * Get the unique site identifier based on the long value associated with the site
	 * UUID. This will be a subset of the full 8-bit long value.
	 * 
	 * @param value
	 * @return
	 */
	public static byte[] getSiteIdentifier(Long value) {
		byte[] bytes = Bytes.toBytes(value);
		byte[] result = new byte[SITE_IDENTIFIER_LENGTH];
		System.arraycopy(bytes, bytes.length - SITE_IDENTIFIER_LENGTH, result, 0, SITE_IDENTIFIER_LENGTH);
		return result;
	}

	/**
	 * Get primary row key for a given site.
	 * 
	 * @param siteId
	 * @return
	 */
	public static byte[] getPrimaryRowkey(Long siteId) {
		byte[] sid = getSiteIdentifier(siteId);
		ByteBuffer rowkey = ByteBuffer.allocate(sid.length);
		rowkey.put(sid);
		return rowkey.array();
	}

	/**
	 * Get zone row key for a given site.
	 * 
	 * @param siteId
	 * @return
	 */
	public static byte[] getZoneRowKey(Long siteId) {
		byte[] sid = getSiteIdentifier(siteId);
		ByteBuffer rowkey = ByteBuffer.allocate(sid.length + 1);
		rowkey.put(sid);
		rowkey.put(SiteRecordType.Zone.getType());
		return rowkey.array();
	}

	/**
	 * Get device assignment row key for a given site.
	 * 
	 * @param siteId
	 * @return
	 */
	public static byte[] getAssignmentRowKey(Long siteId) {
		byte[] sid = getSiteIdentifier(siteId);
		ByteBuffer rowkey = ByteBuffer.allocate(sid.length + 1);
		rowkey.put(sid);
		rowkey.put(SiteRecordType.Assignment.getType());
		return rowkey.array();
	}

	/**
	 * Get key that marks finish of assignment records for a site.
	 * 
	 * @param siteId
	 * @return
	 */
	public static byte[] getAfterAssignmentRowKey(Long siteId) {
		byte[] sid = getSiteIdentifier(siteId);
		ByteBuffer rowkey = ByteBuffer.allocate(sid.length + 1);
		rowkey.put(sid);
		rowkey.put(SiteRecordType.End.getType());
		return rowkey.array();
	}
}
