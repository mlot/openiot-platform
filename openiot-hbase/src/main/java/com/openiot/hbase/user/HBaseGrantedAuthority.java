/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.user;

import com.openiot.core.OpenIoTPersistence;
import com.openiot.hbase.IHBaseContext;
import com.openiot.hbase.IOpenIoTHBase;
import com.openiot.hbase.common.HBaseUtils;
import com.openiot.hbase.encoder.PayloadMarshalerResolver;
import com.openiot.rest.model.user.GrantedAuthority;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.user.IGrantedAuthority;
import com.openiot.spi.user.IGrantedAuthoritySearchCriteria;
import com.openiot.spi.user.request.IGrantedAuthorityCreateRequest;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HBase specifics for dealing with OpenIoT granted authorities.
 * 
 * @author Derek
 */
public class HBaseGrantedAuthority {

	/**
	 * Create a new granted authority.
	 * 
	 * @param context
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static GrantedAuthority createGrantedAuthority(IHBaseContext context,
			IGrantedAuthorityCreateRequest request) throws OpenIoTException {
		GrantedAuthority existing = getGrantedAuthorityByName(context, request.getAuthority());
		if (existing != null) {
			throw new OpenIoTSystemException(ErrorCode.DuplicateAuthority, ErrorLevel.ERROR,
					HttpServletResponse.SC_CONFLICT);
		}

		// Create the new granted authority and store it.
		GrantedAuthority auth = OpenIoTPersistence.grantedAuthorityCreateLogic(request);
		byte[] primary = getGrantedAuthorityRowKey(request.getAuthority());
		byte[] payload = context.getPayloadMarshaler().encodeGrantedAuthority(auth);

		HTableInterface users = null;
		try {
			users = context.getClient().getTableInterface(IOpenIoTHBase.USERS_TABLE_NAME);
			Put put = new Put(primary);
			HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
			users.put(put);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to create granted authority.", e);
		} finally {
			HBaseUtils.closeCleanly(users);
		}

		return auth;
	}

	/**
	 * Get a granted authority by unique name.
	 * 
	 * @param hbase
	 * @param name
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static GrantedAuthority getGrantedAuthorityByName(IHBaseContext context, String name)
			throws OpenIoTException {
		byte[] rowkey = getGrantedAuthorityRowKey(name);

		HTableInterface users = null;
		try {
			users = context.getClient().getTableInterface(IOpenIoTHBase.USERS_TABLE_NAME);
			Get get = new Get(rowkey);
			HBaseUtils.addPayloadFields(get);
			Result result = users.get(get);

			byte[] type = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD_TYPE);
			byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.PAYLOAD);
			if ((type == null) || (payload == null)) {
				return null;
			}

			return PayloadMarshalerResolver.getInstance().getMarshaler(type).decodeGrantedAuthority(payload);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to load granted authority by name.", e);
		} finally {
			HBaseUtils.closeCleanly(users);
		}
	}

	/**
	 * List granted authorities that match the given criteria.
	 * 
	 * @param hbase
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static List<IGrantedAuthority> listGrantedAuthorities(IHBaseContext context,
			IGrantedAuthoritySearchCriteria criteria) throws OpenIoTException {
		HTableInterface users = null;
		ResultScanner scanner = null;
		try {
			users = context.getClient().getTableInterface(IOpenIoTHBase.USERS_TABLE_NAME);
			Scan scan = new Scan();
			scan.setStartRow(new byte[] { UserRecordType.GrantedAuthority.getType() });
			scan.setStopRow(new byte[] { UserRecordType.End.getType() });
			scanner = users.getScanner(scan);

			ArrayList<IGrantedAuthority> matches = new ArrayList<IGrantedAuthority>();
			for (Result result : scanner) {
				boolean shouldAdd = true;
				Map<byte[], byte[]> row = result.getFamilyMap(IOpenIoTHBase.FAMILY_ID);

				byte[] payloadType = null;
				byte[] payload = null;
				for (byte[] qualifier : row.keySet()) {
					if (Bytes.equals(IOpenIoTHBase.PAYLOAD_TYPE, qualifier)) {
						payloadType = row.get(qualifier);
					}
					if (Bytes.equals(IOpenIoTHBase.PAYLOAD, qualifier)) {
						payload = row.get(qualifier);
					}
				}
				if ((shouldAdd) && (payloadType != null) && (payload != null)) {
					matches.add(PayloadMarshalerResolver.getInstance().getMarshaler(payloadType).decodeGrantedAuthority(
							payload));
				}
			}
			return matches;
		} catch (IOException e) {
			throw new OpenIoTException("Error scanning user rows.", e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			HBaseUtils.closeCleanly(users);
		}
	}

	/**
	 * Get row key for a granted authority.
	 * 
	 * @param username
	 * @return
	 */
	public static byte[] getGrantedAuthorityRowKey(String name) {
		byte[] gaBytes = Bytes.toBytes(name);
		ByteBuffer buffer = ByteBuffer.allocate(1 + gaBytes.length);
		buffer.put(UserRecordType.GrantedAuthority.getType());
		buffer.put(gaBytes);
		return buffer.array();
	}
}