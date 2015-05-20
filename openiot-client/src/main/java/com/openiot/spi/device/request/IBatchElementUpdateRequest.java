/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.request;

import com.openiot.spi.common.IMetadataProvider;
import com.openiot.spi.device.batch.ElementProcessingStatus;
import com.openiot.spi.device.batch.IBatchElement;

import java.util.Date;

/**
 * Defines fields that can be updated on an {@link IBatchElement}.
 * 
 * @author Derek
 */
public interface IBatchElementUpdateRequest extends IMetadataProvider {

	/**
	 * Get processing status indicator.
	 * 
	 * @return
	 */
	public ElementProcessingStatus getProcessingStatus();

	/**
	 * Get date element was processed.
	 * 
	 * @return
	 */
	public Date getProcessedDate();
}