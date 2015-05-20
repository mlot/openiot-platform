/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.mongodb;

import com.mongodb.DBCollection;

/**
 * Mongo client that provides user management collections.
 * 
 * @author Derek
 */
public interface IUserManagementMongoClient {

	/** Default collection name for OpenIoT users */
	public static final String DEFAULT_USERS_COLLECTION_NAME = "users";

	/** Default collection name for OpenIoT granted authorities */
	public static final String DEFAULT_AUTHORITIES_COLLECTION_NAME = "authorities";

	/**
	 * Get the collection for users.
	 * 
	 * @return
	 */
	public DBCollection getUsersCollection();

	/**
	 * Get the collection for authorities.
	 * 
	 * @return
	 */
	public DBCollection getAuthoritiesCollection();
}