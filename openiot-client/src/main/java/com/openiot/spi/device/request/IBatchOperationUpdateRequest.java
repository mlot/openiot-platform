/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.request;

import com.openiot.spi.common.IMetadataProvider;
import com.openiot.spi.device.batch.BatchOperationStatus;

import java.util.Date;

/**
 * Defines fields that can be updated on an {@link com.openiot.spi.device.batch.IBatchOperation}.
 * 
 * @author Derek
 */
public interface IBatchOperationUpdateRequest extends IMetadataProvider {

	/**
	 * Get updated processing status for the batch operation.
	 * 
	 * @return
	 */
	public BatchOperationStatus getProcessingStatus();

	/**
	 * Get updated processing start date.
	 * 
	 * @return
	 */
	public Date getProcessingStartedDate();

	/**
	 * Get updated processing end date.
	 * 
	 * @return
	 */
	public Date getProcessingEndedDate();
}