/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device;

import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.ISiteMapData;

import java.io.Serializable;

/**
 * Model object for site map information.
 * 
 * @author Derek
 */
public class SiteMapData extends MetadataProvider implements ISiteMapData, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = -6587910408267715129L;

	/** Map type */
	private String type;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ISiteMapData#getType()
	 */
	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Copy map information from an SPI object.
	 * 
	 * @param source
	 * @return
	 */
	public static SiteMapData copy(ISiteMapData source) throws OpenIoTException {
		SiteMapData target = new SiteMapData();
		target.setType(source.getType());
		MetadataProvider.copy(source, target);
		return target;
	}
}