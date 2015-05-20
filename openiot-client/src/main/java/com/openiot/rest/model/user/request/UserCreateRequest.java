/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.user.request;

import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.spi.user.AccountStatus;
import com.openiot.spi.user.request.IUserCreateRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds fields needed to create a new user.
 * 
 * @author Derek Adams
 */
public class UserCreateRequest extends MetadataProvider implements IUserCreateRequest, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = -8552286827982676928L;

	/** Username */
	private String username;

	/** Password */
	private String password;

	/** First name */
	private String firstName;

	/** Last name */
	private String lastName;

	/** Account status */
	private AccountStatus status;

	/** List of granted authorities */
	private List<String> authorities = new ArrayList<String>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserCreateRequest#getUsername()
	 */
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserCreateRequest#getPassword()
	 */
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserCreateRequest#getFirstName()
	 */
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserCreateRequest#getLastName()
	 */
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserCreateRequest#getStatus()
	 */
	public AccountStatus getStatus() {
		return status;
	}

	public void setStatus(AccountStatus status) {
		this.status = status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUserCreateRequest#getAuthorities()
	 */
	public List<String> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<String> authorities) {
		this.authorities = authorities;
	}
}