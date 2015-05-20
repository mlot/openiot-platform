/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.user;

import com.openiot.hbase.HBaseContext;
import com.openiot.hbase.IOpenIoTHBase;
import com.openiot.hbase.IOpenIoTHBaseClient;
import com.openiot.hbase.common.OpenIoTTables;
import com.openiot.hbase.encoder.IPayloadMarshaler;
import com.openiot.hbase.encoder.JsonPayloadMarshaler;
import com.openiot.hbase.uid.IdManager;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import com.openiot.spi.user.*;
import com.openiot.spi.user.request.IGrantedAuthorityCreateRequest;
import com.openiot.spi.user.request.IUserCreateRequest;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * HBase implementation of OpenIoT user management.
 * 
 * @author Derek
 */
public class HBaseUserManagement extends LifecycleComponent implements IUserManagement {

	/** Static logger instance */
	private static final Logger LOGGER = Logger.getLogger(HBaseUserManagement.class);

	/** Used to communicate with HBase */
	private IOpenIoTHBaseClient client;

	/** Injected payload encoder */
	private IPayloadMarshaler payloadMarshaler = new JsonPayloadMarshaler();

	/** Supplies context to implementation methods */
	private HBaseContext context;

	public HBaseUserManagement() {
		super(LifecycleComponentType.DataStore);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		LOGGER.info("Verifying tables...");
		ensureTablesExist();

		LOGGER.info("Loading id management...");
		IdManager.getInstance().load(client);

		// Create context from configured options.
		this.context = new HBaseContext();
		context.setClient(getClient());
		context.setPayloadMarshaler(getPayloadMarshaler());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#getLogger()
	 */
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
		LOGGER.info("HBase user management stopped.");
	}

	/**
	 * Ensure that the tables this implementation depends on are there.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void ensureTablesExist() throws OpenIoTException {
		OpenIoTTables.assureTable(client, IOpenIoTHBase.USERS_TABLE_NAME, BloomType.ROW);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IUserManagement#createUser(com.openiot.spi.user.request
	 * .IUserCreateRequest)
	 */
	@Override
	public IUser createUser(IUserCreateRequest request) throws OpenIoTException {
		return HBaseUser.createUser(context, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserManagement#authenticate(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public IUser authenticate(String username, String password) throws OpenIoTException {
		return HBaseUser.authenticate(context, username, password);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserManagement#updateUser(java.lang.String,
	 * IUserCreateRequest)
	 */
	@Override
	public IUser updateUser(String username, IUserCreateRequest request) throws OpenIoTException {
		return HBaseUser.updateUser(context, username, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserManagement#getUserByUsername(java.lang.String)
	 */
	@Override
	public IUser getUserByUsername(String username) throws OpenIoTException {
		return HBaseUser.getUserByUsername(context, username);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserManagement#getGrantedAuthorities(java.lang.String)
	 */
	@Override
	public List<IGrantedAuthority> getGrantedAuthorities(String username) throws OpenIoTException {
		return HBaseUser.getGrantedAuthorities(context, username);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserManagement#addGrantedAuthorities(java.lang.String,
	 * java.util.List)
	 */
	@Override
	public List<IGrantedAuthority> addGrantedAuthorities(String username, List<String> authorities)
			throws OpenIoTException {
		throw new OpenIoTException("Not implemented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IUserManagement#removeGrantedAuthorities(java.lang.String,
	 * java.util.List)
	 */
	@Override
	public List<IGrantedAuthority> removeGrantedAuthorities(String username, List<String> authorities)
			throws OpenIoTException {
		throw new OpenIoTException("Not implemented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserManagement#listUsers(com.openiot.spi.user.
	 * IUserSearchCriteria)
	 */
	@Override
	public List<IUser> listUsers(IUserSearchCriteria criteria) throws OpenIoTException {
		return HBaseUser.listUsers(context, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserManagement#deleteUser(java.lang.String, boolean)
	 */
	@Override
	public IUser deleteUser(String username, boolean force) throws OpenIoTException {
		return HBaseUser.deleteUser(context, username, force);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IUserManagement#createGrantedAuthority(com.openiot.spi
	 * .user.request.IGrantedAuthorityCreateRequest)
	 */
	@Override
	public IGrantedAuthority createGrantedAuthority(IGrantedAuthorityCreateRequest request)
			throws OpenIoTException {
		return HBaseGrantedAuthority.createGrantedAuthority(context, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IUserManagement#getGrantedAuthorityByName(java.lang.String)
	 */
	@Override
	public IGrantedAuthority getGrantedAuthorityByName(String name) throws OpenIoTException {
		return HBaseGrantedAuthority.getGrantedAuthorityByName(context, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IUserManagement#updateGrantedAuthority(java.lang.String,
	 * IGrantedAuthorityCreateRequest)
	 */
	@Override
	public IGrantedAuthority updateGrantedAuthority(String name, IGrantedAuthorityCreateRequest request)
			throws OpenIoTException {
		throw new OpenIoTException("Not implemented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IUserManagement#listGrantedAuthorities(com.openiot.spi
	 * .user.IGrantedAuthoritySearchCriteria)
	 */
	@Override
	public List<IGrantedAuthority> listGrantedAuthorities(IGrantedAuthoritySearchCriteria criteria)
			throws OpenIoTException {
		return HBaseGrantedAuthority.listGrantedAuthorities(context, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IUserManagement#deleteGrantedAuthority(java.lang.String)
	 */
	@Override
	public void deleteGrantedAuthority(String authority) throws OpenIoTException {
		throw new OpenIoTException("Not implemented.");
	}

	public IOpenIoTHBaseClient getClient() {
		return client;
	}

	public void setClient(IOpenIoTHBaseClient client) {
		this.client = client;
	}

	public IPayloadMarshaler getPayloadMarshaler() {
		return payloadMarshaler;
	}

	public void setPayloadMarshaler(IPayloadMarshaler payloadMarshaler) {
		this.payloadMarshaler = payloadMarshaler;
	}
}