/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.openiot.rest.model.common.MetadataProviderEntity;
import com.openiot.rest.model.datatype.JsonDateSerializer;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.user.AccountStatus;
import com.openiot.spi.user.IUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model class for a User.
 * 
 * @author Derek Adams
 */
public class User extends MetadataProviderEntity implements IUser, Serializable {

	/** For {@link Serializable} */
	private static final long serialVersionUID = -3322129570954465956L;

	/** Unique username */
	private String username;

	/** Hashed password */
	private String hashedPassword;

	/** First name */
	private String firstName;

	/** Last name */
	private String lastName;

	/** Last login */
	private Date lastLogin;

	/** Account status */
	private AccountStatus status;

	/** List of granted authorities */
	private List<String> authorities = new ArrayList<String>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUser#getUsername()
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
	 * @see IUser#getHashedPassword()
	 */
	public String getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUser#getFirstName()
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
	 * @see IUser#getLastName()
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
	 * @see IUser#getLastLogin()
	 */
	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IUser#getStatus()
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
	 * @see IUser#getAuthorities()
	 */
	public List<String> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<String> authorities) {
		this.authorities = authorities;
	}

	/**
	 * Copy contents from the SPI class.
	 * 
	 * @param input
	 * @return
	 */
	public static User copy(IUser input) throws OpenIoTException {
		User result = new User();
		result.setUsername(input.getUsername());
		result.setHashedPassword(input.getHashedPassword());
		result.setFirstName(input.getFirstName());
		result.setLastName(input.getLastName());
		result.setLastLogin(input.getLastLogin());
		result.setStatus(input.getStatus());
		result.setAuthorities(new ArrayList<String>(input.getAuthorities()));
		MetadataProviderEntity.copy(input, result);
		return result;
	}
}