/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.user.request;

import com.openiot.spi.user.request.IGrantedAuthorityCreateRequest;

/**
 * Holds fields needed to create a new granted authority.
 * 
 * @author Derek Adams
 */
public class GrantedAuthorityCreateRequest implements IGrantedAuthorityCreateRequest {

	/** Authority name */
	private String authority;

	/** Authority description */
	private String description;

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}