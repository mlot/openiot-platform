/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.core.user;

/**
 * Constants for core roles installed with OpenIoT.
 * 
 * @author Derek
 */
public interface SitewhereRoles {

	/** Role that indicates user has been authenticated */
	public static final String ROLE_AUTHENTICATED_USER = "ROLE_"
			+ IOpenIoTAuthorities.AUTH_AUTHENTICATED_USER;

	/** Role for site adminstration (edit/delete) */
	public static final String ROLE_ADMINISTER_SITES = "ROLE_" + IOpenIoTAuthorities.AUTH_ADMIN_SITES;

	/** Role for administering user accounts and authorities */
	public static final String ROLE_ADMINISTER_USERS = "ROLE_" + IOpenIoTAuthorities.AUTH_ADMIN_USERS;
}