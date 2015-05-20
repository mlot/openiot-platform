/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.server.user;

import com.openiot.core.user.IOpenIoTAuthorities;
import com.openiot.rest.model.user.request.GrantedAuthorityCreateRequest;
import com.openiot.rest.model.user.request.UserCreateRequest;
import com.openiot.server.OpenIoTServer;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.server.user.IUserModelInitializer;
import com.openiot.spi.user.AccountStatus;
import com.openiot.spi.user.IGrantedAuthority;
import com.openiot.spi.user.IUserManagement;
import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to load a default user and granted authorities into an empty user model. This acts
 * as a bootstrap for systems that have just been installed.
 * 
 * @author Derek
 */
public class DefaultUserModelInitializer implements IUserModelInitializer {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(DefaultUserModelInitializer.class);

	/** Default administrator username */
	public static final String DEFAULT_USERNAME = "admin";

	/** Default administrator password */
	public static final String DEFAULT_PASSWORD = "password";

	/** Prefix for create authority */
	public static final String PREFIX_CREATE_AUTH = "[Create Authority]";

	/** Prefix for create user */
	public static final String PREFIX_CREATE_USER = "[Create User]";

	/** User management instance */
	private IUserManagement userManagement;

	/** Indiates whether model should be initialized if no console is available for input */
	private boolean initializeIfNoConsole = false;

	/**
	 * Initialize the user model with a expected list of granted authorities and default
	 * user.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void initialize(IUserManagement userManagement) throws OpenIoTException {
		setUserManagement(userManagement);

		// Use the system account for logging "created by" on created elements.
		SecurityContextHolder.getContext().setAuthentication(OpenIoTServer.getSystemAuthentication());

		GrantedAuthorityCreateRequest gaReq = new GrantedAuthorityCreateRequest();

		// Create authenticated user authority.
		IGrantedAuthority authUser =
				getUserManagement().getGrantedAuthorityByName(IOpenIoTAuthorities.AUTH_AUTHENTICATED_USER);
		if (authUser == null) {
			gaReq.setAuthority(IOpenIoTAuthorities.AUTH_AUTHENTICATED_USER);
			gaReq.setDescription("Log in to the system and perform basic functions.");
			authUser = getUserManagement().createGrantedAuthority(gaReq);
			LOGGER.info(PREFIX_CREATE_AUTH + " " + IOpenIoTAuthorities.AUTH_AUTHENTICATED_USER);
		}

		// Create user administration authority.
		IGrantedAuthority userAdmin =
				getUserManagement().getGrantedAuthorityByName(IOpenIoTAuthorities.AUTH_ADMIN_USERS);
		if (userAdmin == null) {
			gaReq.setAuthority(IOpenIoTAuthorities.AUTH_ADMIN_USERS);
			gaReq.setDescription("Create, Maintain, and delete user accounts.");
			userAdmin = getUserManagement().createGrantedAuthority(gaReq);
			LOGGER.info(PREFIX_CREATE_AUTH + " " + IOpenIoTAuthorities.AUTH_ADMIN_USERS);
		}

		// Create site administration authority.
		IGrantedAuthority siteAdmin =
				getUserManagement().getGrantedAuthorityByName(IOpenIoTAuthorities.AUTH_ADMIN_SITES);
		if (siteAdmin == null) {
			gaReq.setAuthority(IOpenIoTAuthorities.AUTH_ADMIN_SITES);
			gaReq.setDescription("Create, Maintain, and delete site information.");
			siteAdmin = getUserManagement().createGrantedAuthority(gaReq);
			LOGGER.info(PREFIX_CREATE_AUTH + " " + IOpenIoTAuthorities.AUTH_ADMIN_SITES);
		}

		List<String> auths = new ArrayList<String>();
		auths.add(authUser.getAuthority());
		auths.add(userAdmin.getAuthority());
		auths.add(siteAdmin.getAuthority());

		UserCreateRequest ureq = new UserCreateRequest();
		ureq.setFirstName("Admin");
		ureq.setLastName("User");
		ureq.setUsername(DEFAULT_USERNAME);
		ureq.setPassword(DEFAULT_PASSWORD);
		ureq.setAuthorities(auths);
		ureq.setStatus(AccountStatus.Active);

		getUserManagement().createUser(ureq);
		SecurityContextHolder.getContext().setAuthentication(null);
		LOGGER.info(PREFIX_CREATE_USER + " " + ureq.getUsername());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IModelInitializer#isInitializeIfNoConsole()
	 */
	public boolean isInitializeIfNoConsole() {
		return initializeIfNoConsole;
	}

	public void setInitializeIfNoConsole(boolean initializeIfNoConsole) {
		this.initializeIfNoConsole = initializeIfNoConsole;
	}

	public IUserManagement getUserManagement() {
		return userManagement;
	}

	public void setUserManagement(IUserManagement userManagement) {
		this.userManagement = userManagement;
	}
}