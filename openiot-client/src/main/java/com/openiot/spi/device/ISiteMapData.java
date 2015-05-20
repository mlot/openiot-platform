/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device;

import com.openiot.spi.common.IMetadataProvider;

/**
 * Interface for map information associated with a Site.
 * 
 * @author Derek
 */
public interface ISiteMapData extends IMetadataProvider {

	/**
	 * Get the map type.
	 * 
	 * @return
	 */
	public String getType();
}