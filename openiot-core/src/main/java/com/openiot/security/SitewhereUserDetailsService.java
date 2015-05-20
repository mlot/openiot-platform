/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.security;

import com.openiot.OpenIoT;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.user.IGrantedAuthority;
import com.openiot.spi.user.IUser;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;

import java.util.List;

/**
 * OpenIoT implementation of Spring security UserDetailsManager.
 * 
 * @author Derek
 */
public class SitewhereUserDetailsService implements UserDetailsManager {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.security.userdetails.UserDetailsService#loadUserByUsername(
	 * java.lang. String)
	 */
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException,
			DataAccessException {
		try {
			IUser user = OpenIoT.getServer().getUserManagement().getUserByUsername(username);
			List<IGrantedAuthority> auths =
					OpenIoT.getServer().getUserManagement().getGrantedAuthorities(username);
			return new SitewhereUserDetails(user, auths);
		} catch (OpenIoTException e) {
			throw new UsernameNotFoundException("Unable to load user by username.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.userdetails.UserDetailsManager#createUser(org.
	 * springframework .security.userdetails .UserDetails)
	 */
	public void createUser(UserDetails info) {
		// User user = new User();
		// user.setUsername(info.getUsername());
		// user.setHashedPassword(info.getPassword());
		// try {
		// OpenIoTServer.getInstance().getUserManagement().createUser(user);
		// } catch (OpenIoTException e) {
		// throw new RuntimeException(e);
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.security.userdetails.UserDetailsManager#deleteUser(java.lang
	 * .String)
	 */
	public void deleteUser(String username) {
		try {
			OpenIoT.getServer().getUserManagement().deleteUser(username, true);
		} catch (OpenIoTException e) {
			throw new RuntimeException("Unable to delete user.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.security.userdetails.UserDetailsManager#userExists(java.lang
	 * .String)
	 */
	public boolean userExists(String username) {
		return (loadUserByUsername(username) != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.userdetails.UserDetailsManager#updateUser(org.
	 * springframework .security.userdetails .UserDetails)
	 */
	public void updateUser(UserDetails info) {
		throw new RuntimeException("User updates not supported.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.security.userdetails.UserDetailsManager#changePassword(java
	 * .lang.String, java.lang.String)
	 */
	public void changePassword(String oldPassword, String newPassword) {
		throw new RuntimeException("User updates not supported.");
	}
}