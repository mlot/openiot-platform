/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.common;

import java.util.Date;

/**
 * Base interface for entites managed by OpenIoT.
 * 
 * @author Derek Adams
 */
public interface IOpenIoTEntity {

	/**
	 * Get date when entity was created.
	 * 
	 * @return
	 */
	public Date getCreatedDate();

	/**
	 * Get username that created entity.
	 * 
	 * @return
	 */
	public String getCreatedBy();

	/**
	 * Get date when entity was last updated.
	 * 
	 * @return
	 */
	public Date getUpdatedDate();

	/**
	 * Get username that last updated entity.
	 * 
	 * @return
	 */
	public String getUpdatedBy();

	/**
	 * Indicates if entity has been deleted.
	 * 
	 * @return
	 */
	public boolean isDeleted();
}