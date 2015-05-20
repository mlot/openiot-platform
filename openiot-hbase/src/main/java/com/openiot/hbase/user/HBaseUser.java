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
import com.openiot.rest.model.user.GrantedAuthoritySearchCriteria;
import com.openiot.rest.model.user.User;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.user.IGrantedAuthority;
import com.openiot.spi.user.IUser;
import com.openiot.spi.user.IUserSearchCriteria;
import com.openiot.spi.user.request.IUserCreateRequest;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * HBase specifics for dealing with OpenIoT users.
 * 
 * @author Derek
 */
public class HBaseUser {

	/**
	 * Create a new device.
	 * 
	 * @param context
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static User createUser(IHBaseContext context, IUserCreateRequest request)
			throws OpenIoTException {
		User existing = getUserByUsername(context, request.getUsername());
		if (existing != null) {
			throw new OpenIoTSystemException(ErrorCode.DuplicateUser, ErrorLevel.ERROR,
					HttpServletResponse.SC_CONFLICT);
		}

		// Create the new user and store it.
		User user = OpenIoTPersistence.userCreateLogic(request);
		byte[] primary = getUserRowKey(request.getUsername());
		byte[] payload = context.getPayloadMarshaler().encodeUser(user);

		HTableInterface users = null;
		try {
			users = context.getClient().getTableInterface(IOpenIoTHBase.USERS_TABLE_NAME);
			Put put = new Put(primary);
			HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
			users.put(put);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to set JSON for user.", e);
		} finally {
			HBaseUtils.closeCleanly(users);
		}

		return user;
	}

	/**
	 * Update an existing user.
	 * 
	 * @param context
	 * @param username
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static User updateUser(IHBaseContext context, String username, IUserCreateRequest request)
			throws OpenIoTException {
		User updated = getUserByUsername(context, username);
		if (updated == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidUsername, ErrorLevel.ERROR);
		}
		OpenIoTPersistence.userUpdateLogic(request, updated);

		byte[] primary = getUserRowKey(username);
		byte[] payload = context.getPayloadMarshaler().encodeUser(updated);

		HTableInterface users = null;
		try {
			users = context.getClient().getTableInterface(IOpenIoTHBase.USERS_TABLE_NAME);
			Put put = new Put(primary);
			HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
			users.put(put);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to set JSON for user.", e);
		} finally {
			HBaseUtils.closeCleanly(users);
		}
		return updated;
	}

	/**
	 * Delete an existing user.
	 * 
	 * @param context
	 * @param username
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static User deleteUser(IHBaseContext context, String username, boolean force)
			throws OpenIoTException {
		User existing = getUserByUsername(context, username);
		if (existing == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidUsername, ErrorLevel.ERROR);
		}
		existing.setDeleted(true);
		byte[] primary = getUserRowKey(username);
		if (force) {
			HTableInterface users = null;
			try {
				users = context.getClient().getTableInterface(IOpenIoTHBase.USERS_TABLE_NAME);
				Delete delete = new Delete(primary);
				users.delete(delete);
			} catch (IOException e) {
				throw new OpenIoTException("Unable to delete user.", e);
			} finally {
				HBaseUtils.closeCleanly(users);
			}
		} else {
			byte[] marker = { (byte) 0x01 };
			OpenIoTPersistence.setUpdatedEntityMetadata(existing);
			byte[] payload = context.getPayloadMarshaler().encodeUser(existing);
			HTableInterface users = null;
			try {
				users = context.getClient().getTableInterface(IOpenIoTHBase.USERS_TABLE_NAME);
				Put put = new Put(primary);
				HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
				put.add(IOpenIoTHBase.FAMILY_ID, IOpenIoTHBase.DELETED, marker);
				users.put(put);
			} catch (IOException e) {
				throw new OpenIoTException("Unable to set deleted flag for user.", e);
			} finally {
				HBaseUtils.closeCleanly(users);
			}
		}
		return existing;
	}

	/**
	 * Get a user by unique username. Returns null if not found.
	 * 
	 * @param context
	 * @param username
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static User getUserByUsername(IHBaseContext context, String username) throws OpenIoTException {
		byte[] rowkey = getUserRowKey(username);

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

			return PayloadMarshalerResolver.getInstance().getMarshaler(type).decodeUser(payload);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to load user by username.", e);
		} finally {
			HBaseUtils.closeCleanly(users);
		}
	}

	/**
	 * Authenticate a username password combination.
	 * 
	 * @param context
	 * @param username
	 * @param password
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static User authenticate(IHBaseContext context, String username, String password)
			throws OpenIoTException {
		if (password == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidPassword, ErrorLevel.ERROR,
					HttpServletResponse.SC_BAD_REQUEST);
		}
		User existing = getUserByUsername(context, username);
		if (existing == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidUsername, ErrorLevel.ERROR,
					HttpServletResponse.SC_UNAUTHORIZED);
		}
		String inPassword = OpenIoTPersistence.encodePassoword(password);
		if (!existing.getHashedPassword().equals(inPassword)) {
			throw new OpenIoTSystemException(ErrorCode.InvalidPassword, ErrorLevel.ERROR,
					HttpServletResponse.SC_UNAUTHORIZED);
		}

		// Update last login date.
		existing.setLastLogin(new Date());
		byte[] primary = getUserRowKey(username);
		byte[] payload = context.getPayloadMarshaler().encodeUser(existing);

		HTableInterface users = null;
		try {
			users = context.getClient().getTableInterface(IOpenIoTHBase.USERS_TABLE_NAME);
			Put put = new Put(primary);
			HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
			users.put(put);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to set deleted flag for user.", e);
		} finally {
			HBaseUtils.closeCleanly(users);
		}
		return existing;
	}

	/**
	 * List users that match certain criteria.
	 * 
	 * @param context
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static List<IUser> listUsers(IHBaseContext context, IUserSearchCriteria criteria)
			throws OpenIoTException {

		HTableInterface users = null;
		ResultScanner scanner = null;
		try {
			users = context.getClient().getTableInterface(IOpenIoTHBase.USERS_TABLE_NAME);
			Scan scan = new Scan();
			scan.setStartRow(new byte[] { UserRecordType.User.getType() });
			scan.setStopRow(new byte[] { UserRecordType.GrantedAuthority.getType() });
			scanner = users.getScanner(scan);

			ArrayList<IUser> matches = new ArrayList<IUser>();
			for (Result result : scanner) {
				boolean shouldAdd = true;
				Map<byte[], byte[]> row = result.getFamilyMap(IOpenIoTHBase.FAMILY_ID);

				byte[] payloadType = null;
				byte[] payload = null;
				for (byte[] qualifier : row.keySet()) {
					if ((Bytes.equals(IOpenIoTHBase.DELETED, qualifier)) && (!criteria.isIncludeDeleted())) {
						shouldAdd = false;
					}
					if (Bytes.equals(IOpenIoTHBase.PAYLOAD_TYPE, qualifier)) {
						payloadType = row.get(qualifier);
					}
					if (Bytes.equals(IOpenIoTHBase.PAYLOAD, qualifier)) {
						payload = row.get(qualifier);
					}
				}
				if ((shouldAdd) && (payloadType != null) && (payload != null)) {
					matches.add(PayloadMarshalerResolver.getInstance().getMarshaler(payloadType).decodeUser(
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
	 * Get the list of granted authorities for a user.
	 * 
	 * @param context
	 * @param username
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static List<IGrantedAuthority> getGrantedAuthorities(IHBaseContext context, String username)
			throws OpenIoTException {
		IUser user = getUserByUsername(context, username);
		List<String> userAuths = user.getAuthorities();
		List<IGrantedAuthority> all =
				HBaseGrantedAuthority.listGrantedAuthorities(context, new GrantedAuthoritySearchCriteria());
		List<IGrantedAuthority> matched = new ArrayList<IGrantedAuthority>();
		for (IGrantedAuthority auth : all) {
			if (userAuths.contains(auth.getAuthority())) {
				matched.add(auth);
			}
		}
		return matched;
	}

	/**
	 * Get row key for a user.
	 * 
	 * @param username
	 * @return
	 */
	public static byte[] getUserRowKey(String username) {
		byte[] userBytes = Bytes.toBytes(username);
		ByteBuffer buffer = ByteBuffer.allocate(1 + userBytes.length);
		buffer.put(UserRecordType.User.getType());
		buffer.put(userBytes);
		return buffer.array();
	}
}
