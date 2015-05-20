/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.asset;

import com.openiot.spi.asset.AssetType;
import com.openiot.spi.asset.IPersonAsset;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for a person asset.
 * 
 * @author dadams
 */
public class PersonAsset extends Asset implements IPersonAsset {

	/** Asset username */
	private String userName;

	/** Asset email address */
	private String emailAddress;

	/** List of roles */
	private List<String> roles = new ArrayList<String>();

	public PersonAsset() {
		setType(AssetType.Person);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IPersonAsset#getUserName()
	 */
	@Override
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IPersonAsset#getEmailAddress()
	 */
	@Override
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IPersonAsset#getRoles()
	 */
	@Override
	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
}