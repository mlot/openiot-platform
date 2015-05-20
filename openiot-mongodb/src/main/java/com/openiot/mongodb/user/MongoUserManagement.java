/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.mongodb.user;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.openiot.core.OpenIoTPersistence;
import com.openiot.mongodb.IUserManagementMongoClient;
import com.openiot.mongodb.MongoPersistence;
import com.openiot.mongodb.common.MongoOpenIoTEntity;
import com.openiot.rest.model.user.GrantedAuthority;
import com.openiot.rest.model.user.GrantedAuthoritySearchCriteria;
import com.openiot.rest.model.user.User;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import com.openiot.spi.user.*;
import com.openiot.spi.user.request.IGrantedAuthorityCreateRequest;
import com.openiot.spi.user.request.IUserCreateRequest;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User management implementation that uses MongoDB for persistence.
 * 
 * @author dadams
 */
public class MongoUserManagement extends LifecycleComponent implements IUserManagement {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(MongoUserManagement.class);

	/** Injected with global OpenIoT Mongo client */
	private IUserManagementMongoClient mongoClient;

	public MongoUserManagement() {
		super(LifecycleComponentType.DataStore);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	public void start() throws OpenIoTException {
		/** Ensure that expected indexes exist */
		ensureIndexes();
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

	/**
	 * Ensure that expected collection indexes exist.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void ensureIndexes() throws OpenIoTException {
		getMongoClient().getUsersCollection().ensureIndex(new BasicDBObject("username", 1),
				new BasicDBObject("unique", true));
		getMongoClient().getAuthoritiesCollection().ensureIndex(new BasicDBObject("authority", 1),
				new BasicDBObject("unique", true));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	public void stop() throws OpenIoTException {
		LOGGER.info("Mongo user management stopped.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IUserManagement#createUser(com.openiot.spi.user.request
	 * .IUserCreateRequest)
	 */
	public IUser createUser(IUserCreateRequest request) throws OpenIoTException {
		IUser existing = getUserByUsername(request.getUsername());
		if (existing != null) {
			throw new OpenIoTSystemException(ErrorCode.DuplicateUser, ErrorLevel.ERROR,
					HttpServletResponse.SC_CONFLICT);
		}
		User user = OpenIoTPersistence.userCreateLogic(request);

		DBCollection users = getMongoClient().getUsersCollection();
		DBObject created = MongoUser.toDBObject(user);
		MongoPersistence.insert(users, created);
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserManagement#authenticate(java.lang.String,
	 * java.lang.String)
	 */
	public IUser authenticate(String username, String password) throws OpenIoTException {
		if (password == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidPassword, ErrorLevel.ERROR,
					HttpServletResponse.SC_BAD_REQUEST);
		}
		DBObject userObj = assertUser(username);
		String inPassword = OpenIoTPersistence.encodePassoword(password);
		User match = MongoUser.fromDBObject(userObj);
		if (!match.getHashedPassword().equals(inPassword)) {
			throw new OpenIoTSystemException(ErrorCode.InvalidPassword, ErrorLevel.ERROR,
					HttpServletResponse.SC_UNAUTHORIZED);
		}

		// Update last login date.
		match.setLastLogin(new Date());
		DBObject updated = MongoUser.toDBObject(match);
		DBCollection users = getMongoClient().getUsersCollection();
		BasicDBObject query = new BasicDBObject(MongoUser.PROP_USERNAME, username);
		MongoPersistence.update(users, query, updated);

		return match;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserManagement#updateUser(java.lang.String,
	 * IUserCreateRequest)
	 */
	public IUser updateUser(String username, IUserCreateRequest request) throws OpenIoTException {
		DBObject existing = assertUser(username);

		// Copy any non-null fields.
		User updatedUser = MongoUser.fromDBObject(existing);
		OpenIoTPersistence.userUpdateLogic(request, updatedUser);

		DBObject updated = MongoUser.toDBObject(updatedUser);

		DBCollection users = getMongoClient().getUsersCollection();
		BasicDBObject query = new BasicDBObject(MongoUser.PROP_USERNAME, username);
		MongoPersistence.update(users, query, updated);
		return MongoUser.fromDBObject(updated);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserManagement#getUserByUsername(java.lang.String)
	 */
	public IUser getUserByUsername(String username) throws OpenIoTException {
		DBObject dbUser = getUserObjectByUsername(username);
		if (dbUser != null) {
			return MongoUser.fromDBObject(dbUser);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserManagement#getGrantedAuthorities(java.lang.String)
	 */
	public List<IGrantedAuthority> getGrantedAuthorities(String username) throws OpenIoTException {
		IUser user = getUserByUsername(username);
		List<String> userAuths = user.getAuthorities();
		List<IGrantedAuthority> all = listGrantedAuthorities(new GrantedAuthoritySearchCriteria());
		List<IGrantedAuthority> matched = new ArrayList<IGrantedAuthority>();
		for (IGrantedAuthority auth : all) {
			if (userAuths.contains(auth.getAuthority())) {
				matched.add(auth);
			}
		}
		return matched;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserManagement#addGrantedAuthorities(java.lang.String,
	 * java.util.List)
	 */
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
	public List<IGrantedAuthority> removeGrantedAuthorities(String username, List<String> authorities)
			throws OpenIoTException {
		throw new OpenIoTException("Not implemented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IUserManagement#listUsers(com.openiot.spi.user.request
	 * .IUserSearchCriteria)
	 */
	public List<IUser> listUsers(IUserSearchCriteria criteria) throws OpenIoTException {
		DBCollection users = getMongoClient().getUsersCollection();
		DBObject dbCriteria = new BasicDBObject();
		if (!criteria.isIncludeDeleted()) {
			MongoOpenIoTEntity.setDeleted(dbCriteria, false);
		}
		DBCursor cursor = users.find(dbCriteria).sort(new BasicDBObject(MongoUser.PROP_USERNAME, 1));
		List<IUser> matches = new ArrayList<IUser>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoUser.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserManagement#deleteUser(java.lang.String, boolean)
	 */
	public IUser deleteUser(String username, boolean force) throws OpenIoTException {
		DBObject existing = assertUser(username);
		if (force) {
			DBCollection users = getMongoClient().getUsersCollection();
			MongoPersistence.delete(users, existing);
			return MongoUser.fromDBObject(existing);
		} else {
			MongoOpenIoTEntity.setDeleted(existing, true);
			BasicDBObject query = new BasicDBObject(MongoUser.PROP_USERNAME, username);
			DBCollection users = getMongoClient().getUsersCollection();
			MongoPersistence.update(users, query, existing);
			return MongoUser.fromDBObject(existing);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IUserManagement#createGrantedAuthority(com.openiot.spi
	 * .user.request. IGrantedAuthorityCreateRequest)
	 */
	public IGrantedAuthority createGrantedAuthority(IGrantedAuthorityCreateRequest request)
			throws OpenIoTException {
		IGrantedAuthority existing = getGrantedAuthorityByName(request.getAuthority());
		if (existing != null) {
			throw new OpenIoTSystemException(ErrorCode.DuplicateAuthority, ErrorLevel.ERROR,
					HttpServletResponse.SC_CONFLICT);
		}
		GrantedAuthority auth = new GrantedAuthority();
		auth.setAuthority(request.getAuthority());
		auth.setDescription(request.getDescription());

		DBCollection auths = getMongoClient().getAuthoritiesCollection();
		DBObject created = MongoGrantedAuthority.toDBObject(auth);
		MongoPersistence.insert(auths, created);
		return auth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IUserManagement#getGrantedAuthorityByName(java.lang.String)
	 */
	public IGrantedAuthority getGrantedAuthorityByName(String name) throws OpenIoTException {
		DBObject dbAuth = getGrantedAuthorityObjectByName(name);
		if (dbAuth != null) {
			return MongoGrantedAuthority.fromDBObject(dbAuth);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IUserManagement#updateGrantedAuthority(java.lang.String,
	 * IGrantedAuthorityCreateRequest)
	 */
	public IGrantedAuthority updateGrantedAuthority(String name, IGrantedAuthorityCreateRequest request)
			throws OpenIoTException {
		throw new OpenIoTException("Not implemented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IUserManagement#listGrantedAuthorities(com.openiot.spi
	 * .user. IGrantedAuthoritySearchCriteria)
	 */
	public List<IGrantedAuthority> listGrantedAuthorities(IGrantedAuthoritySearchCriteria criteria)
			throws OpenIoTException {
		DBCollection auths = getMongoClient().getAuthoritiesCollection();
		DBCursor cursor = auths.find().sort(new BasicDBObject(MongoGrantedAuthority.PROP_AUTHORITY, 1));
		List<IGrantedAuthority> matches = new ArrayList<IGrantedAuthority>();
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoGrantedAuthority.fromDBObject(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IUserManagement#deleteGrantedAuthority(java.lang.String)
	 */
	public void deleteGrantedAuthority(String authority) throws OpenIoTException {
		throw new OpenIoTException("Not implemented.");
	}

	/**
	 * Get the {@link DBObject} for a User given username. Throw an exception if not
	 * found.
	 * 
	 * @param username
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject assertUser(String username) throws OpenIoTException {
		DBObject match = getUserObjectByUsername(username);
		if (match == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidUsername, ErrorLevel.ERROR,
					HttpServletResponse.SC_NOT_FOUND);
		}
		return match;
	}

	/**
	 * Get the DBObject for a User given unique username.
	 * 
	 * @param username
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject getUserObjectByUsername(String username) throws OpenIoTException {
		DBCollection users = getMongoClient().getUsersCollection();
		BasicDBObject query = new BasicDBObject(MongoUser.PROP_USERNAME, username);
		return users.findOne(query);
	}

	/**
	 * Get the {@link DBObject} for a GrantedAuthority given name. Throw an exception if
	 * not found.
	 * 
	 * @param name
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject assertGrantedAuthority(String name) throws OpenIoTException {
		DBObject match = getGrantedAuthorityObjectByName(name);
		if (match == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidAuthority, ErrorLevel.ERROR,
					HttpServletResponse.SC_NOT_FOUND);
		}
		return match;
	}

	/**
	 * Get the DBObject for a GrantedAuthority given unique name.
	 * 
	 * @param name
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject getGrantedAuthorityObjectByName(String name) throws OpenIoTException {
		DBCollection auths = getMongoClient().getAuthoritiesCollection();
		BasicDBObject query = new BasicDBObject(MongoGrantedAuthority.PROP_AUTHORITY, name);
		return auths.findOne(query);
	}

	public IUserManagementMongoClient getMongoClient() {
		return mongoClient;
	}

	public void setMongoClient(IUserManagementMongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}
}