/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.asset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.openiot.spi.asset.AssetType;
import com.openiot.spi.asset.IAsset;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base model class for an asset.
 * 
 * @author dadams
 */
public class Asset implements IAsset {

	/** Unique id */
	private String id;

	/** Asset name */
	private String name;

	/** Asset type indicator */
	private AssetType type;

	/** Asset image url */
	private String imageUrl;

	private Map<String, String> properties = new HashMap<String, String>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAsset#getId()
	 */
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAsset#getName()
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
	 * @see IAsset#getType()
	 */
	public AssetType getType() {
		return type;
	}

	public void setType(AssetType type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAsset#getImageUrl()
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
	 * @see IAsset#getPropertyNames()
	 */
	@JsonIgnore
	public Set<String> getPropertyNames() {
		return properties.keySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAsset#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String name, String value) {
		properties.put(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAsset#getProperty(java.lang.String)
	 */
	public String getProperty(String name) {
		return properties.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAsset#getIdProperty()
	 */
	@JsonIgnore
	public String getIdProperty() {
		return null;
	}

	/**
	 * Included for JSON marshaling of properties map.
	 * 
	 * @return
	 */
	public Map<String, String> getProperties() {
		return properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(IAsset other) {
		if (getName() != null) {
			return getName().compareTo(other.getName());
		}
		return 0;
	}
}