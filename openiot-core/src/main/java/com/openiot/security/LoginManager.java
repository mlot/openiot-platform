/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.security;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.user.IUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletResponse;

/**
 * Provides helper methods for dealing with currently logged in user.
 * 
 * @author Derek
 */
public class LoginManager {

	/**
	 * Get the currently logged in user from Spring Security.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IUser getCurrentlyLoggedInUser() throws OpenIoTException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			throw new OpenIoTSystemException(ErrorCode.NotLoggedIn, ErrorLevel.ERROR,
					HttpServletResponse.SC_FORBIDDEN);
		}
		if (!(auth instanceof SitewhereAuthentication)) {
			throw new OpenIoTException("Authentication was not of expected type: "
					+ SitewhereAuthentication.class.getName() + " found " + auth.getClass().getName()
					+ " instead.");
		}
		return (IUser) ((SitewhereAuthentication) auth).getPrincipal();
	}
}