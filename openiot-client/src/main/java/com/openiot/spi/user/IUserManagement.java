/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.user;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;
import com.openiot.spi.user.request.IGrantedAuthorityCreateRequest;
import com.openiot.spi.user.request.IUserCreateRequest;

import java.util.List;

/**
 * Interface for user management operations.
 * 
 * @author Derek
 */
public interface IUserManagement extends ILifecycleComponent {

	/**
	 * Create a new user based on the given input.
	 * 
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IUser createUser(IUserCreateRequest request) throws OpenIoTException;

	/**
	 * Authenticate the given username and password.
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IUser authenticate(String username, String password) throws OpenIoTException;

	/**
	 * Update details for a user.
	 * 
	 * @param username
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IUser updateUser(String username, IUserCreateRequest request) throws OpenIoTException;

	/**
	 * Get a user given unique username.
	 * 
	 * @param username
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IUser getUserByUsername(String username) throws OpenIoTException;

	/**
	 * Get the granted authorities for a specific user. Does not include any authorities
	 * inherited from groups.
	 * 
	 * @param username
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<IGrantedAuthority> getGrantedAuthorities(String username) throws OpenIoTException;

	/**
	 * Add user authorities. Duplicates are ignored.
	 * 
	 * @param username
	 * @param authorities
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<IGrantedAuthority> addGrantedAuthorities(String username, List<String> authorities)
			throws OpenIoTException;

	/**
	 * Remove user authorities. Ignore if not previously granted.
	 * 
	 * @param username
	 * @param authorities
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<IGrantedAuthority> removeGrantedAuthorities(String username, List<String> authorities)
			throws OpenIoTException;

	/**
	 * Get the list of all users that meet the given criteria.
	 * 
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<IUser> listUsers(IUserSearchCriteria criteria) throws OpenIoTException;

	/**
	 * Delete the user with the given username.
	 * 
	 * @param username
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IUser deleteUser(String username, boolean force) throws OpenIoTException;

	/**
	 * Create a new granted authority.
	 * 
	 * @param request
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IGrantedAuthority createGrantedAuthority(IGrantedAuthorityCreateRequest request)
			throws OpenIoTException;

	/**
	 * Get a granted authority by name.
	 * 
	 * @param name
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IGrantedAuthority getGrantedAuthorityByName(String name) throws OpenIoTException;

	/**
	 * Update a granted authority.
	 * 
	 * @param name
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IGrantedAuthority updateGrantedAuthority(String name, IGrantedAuthorityCreateRequest request)
			throws OpenIoTException;

	/**
	 * List granted authorities that match the given criteria.
	 * 
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<IGrantedAuthority> listGrantedAuthorities(IGrantedAuthoritySearchCriteria criteria)
			throws OpenIoTException;

	/**
	 * Delete a granted authority.
	 * 
	 * @param authority
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void deleteGrantedAuthority(String authority) throws OpenIoTException;
}