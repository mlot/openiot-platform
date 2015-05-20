/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.web.rest.controllers;

import com.openiot.OpenIoT;
import com.openiot.Tracer;
import com.openiot.core.user.SitewhereRoles;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.rest.model.user.GrantedAuthority;
import com.openiot.rest.model.user.User;
import com.openiot.rest.model.user.UserSearchCriteria;
import com.openiot.rest.model.user.request.UserCreateRequest;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.server.debug.TracerCategory;
import com.openiot.spi.user.AccountStatus;
import com.openiot.spi.user.IGrantedAuthority;
import com.openiot.spi.user.IUser;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for user operations.
 * 
 * @author Derek Adams
 */
@Controller
@RequestMapping(value = "/users")
@Api(value = "users", description = "Operations related to OpenIoT users.")
public class UsersController extends OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(UsersController.class);

	/**
	 * Create a new user.
	 * 
	 * @param input
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create a new user")
	@Secured({ SitewhereRoles.ROLE_ADMINISTER_USERS })
	public User createUser(@RequestBody UserCreateRequest input) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "createUser", LOGGER);
		try {
			if ((input.getUsername() == null) || (input.getPassword() == null)
					|| (input.getFirstName() == null) || (input.getLastName() == null)) {
				throw new OpenIoTSystemException(ErrorCode.InvalidUserInformation, ErrorLevel.ERROR);
			}
			if (input.getStatus() == null) {
				input.setStatus(AccountStatus.Active);
			}
			IUser user = OpenIoT.getServer().getUserManagement().createUser(input);
			return User.copy(user);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Update an existing user.
	 * 
	 * @param input
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{username}", method = RequestMethod.PUT)
	@ResponseBody
	@ApiOperation(value = "Update an existing user.")
	@Secured({ SitewhereRoles.ROLE_ADMINISTER_USERS })
	public User updateUser(
			@ApiParam(value = "Unique username", required = true) @PathVariable String username,
			@RequestBody UserCreateRequest input) throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "updateUser", LOGGER);
		try {
			IUser user = OpenIoT.getServer().getUserManagement().updateUser(username, input);
			return User.copy(user);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get a user by unique username.
	 * 
	 * @param username
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{username}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Find user by unique username")
	@Secured({ SitewhereRoles.ROLE_ADMINISTER_USERS })
	public User getUserByUsername(
			@ApiParam(value = "Unique username", required = true) @PathVariable String username)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getUserByUsername", LOGGER);
		try {
			IUser user = OpenIoT.getServer().getUserManagement().getUserByUsername(username);
			if (user == null) {
				throw new OpenIoTSystemException(ErrorCode.InvalidUsername, ErrorLevel.ERROR,
						HttpServletResponse.SC_NOT_FOUND);
			}
			return User.copy(user);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Delete information for a given user based on username.
	 * 
	 * @param siteToken
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{username}", method = RequestMethod.DELETE)
	@ResponseBody
	@ApiOperation(value = "Delete a user by unique username")
	@Secured({ SitewhereRoles.ROLE_ADMINISTER_USERS })
	public User deleteUserByUsername(
			@ApiParam(value = "Unique username", required = true) @PathVariable String username,
			@ApiParam(value = "Delete permanently", required = false) @RequestParam(defaultValue = "false") boolean force)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "deleteUserByUsername", LOGGER);
		try {
			IUser user = OpenIoT.getServer().getUserManagement().deleteUser(username, force);
			return User.copy(user);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Get a list of detailed authority information for a given user.
	 * 
	 * @param username
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(value = "/{username}/authorities", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Find authorities assigned to a given user")
	@Secured({ SitewhereRoles.ROLE_ADMINISTER_USERS })
	public SearchResults<GrantedAuthority> getAuthoritiesForUsername(
			@ApiParam(value = "Unique username", required = true) @PathVariable String username)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "getAuthoritiesForUsername", LOGGER);
		try {
			List<IGrantedAuthority> matches =
					OpenIoT.getServer().getUserManagement().getGrantedAuthorities(username);
			List<GrantedAuthority> converted = new ArrayList<GrantedAuthority>();
			for (IGrantedAuthority auth : matches) {
				converted.add(GrantedAuthority.copy(auth));
			}
			return new SearchResults<GrantedAuthority>(converted);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * List devices that match given criteria.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List users that match certain criteria")
	@Secured({ SitewhereRoles.ROLE_ADMINISTER_USERS })
	public SearchResults<User> listUsers(
			@ApiParam(value = "Include deleted", required = false) @RequestParam(defaultValue = "false") boolean includeDeleted,
			@ApiParam(value = "Max records to return", required = false) @RequestParam(defaultValue = "100") int count)
			throws OpenIoTException {
		Tracer.start(TracerCategory.RestApiCall, "listUsers", LOGGER);
		try {
			List<User> usersConv = new ArrayList<User>();
			UserSearchCriteria criteria = new UserSearchCriteria();
			criteria.setIncludeDeleted(includeDeleted);
			List<IUser> users = OpenIoT.getServer().getUserManagement().listUsers(criteria);
			for (IUser user : users) {
				usersConv.add(User.copy(user));
			}
			SearchResults<User> results = new SearchResults<User>(usersConv);
			return results;
		} finally {
			Tracer.stop(LOGGER);
		}
	}
}