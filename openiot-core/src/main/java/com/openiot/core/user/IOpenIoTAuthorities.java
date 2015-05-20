/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.core.user;

/**
 * Constants for granted authorities expected by OpenIoT.
 * 
 * @author Derek
 */
public interface IOpenIoTAuthorities {

	/** Authority assigned to any user authenticated by the system */
	public static final String AUTH_AUTHENTICATED_USER = "AUTHENTICATED_USER";

	/** Authority to administer users */
	public static final String AUTH_ADMIN_USERS = "ADMINISTER_USERS";

	/** Authority to administer sites */
	public static final String AUTH_ADMIN_SITES = "ADMINISTER_SITES";
}