/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.request;

import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.rest.model.device.SiteMapData;
import com.openiot.spi.device.request.ISiteCreateRequest;

import java.io.Serializable;

/**
 * Provides parameters needed to create a new site.
 * 
 * @author Derek
 */
public class SiteCreateRequest extends MetadataProvider implements ISiteCreateRequest, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = 574323736888872612L;

	/** Site name */
	private String name;

	/** Site description */
	private String description;

	/** Logo image URL */
	private String imageUrl;

	/** Map data */
	private SiteMapData map = new SiteMapData();

	/*
	 * (non-Javadoc)
	 * 
	 * @see ISiteCreateRequest#getName()
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ISiteCreateRequest#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ISiteCreateRequest#getImageUrl()
	 */
	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ISiteCreateRequest#getMap()
	 */
	public SiteMapData getMap() {
		return map;
	}

	public void setMap(SiteMapData map) {
		this.map = map;
	}
}