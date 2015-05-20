/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.request;

import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.spi.device.batch.BatchOperationStatus;
import com.openiot.spi.device.request.IBatchOperationUpdateRequest;

import java.io.Serializable;
import java.util.Date;

/**
 * Holds information needed to update a batch operation.
 * 
 * @author Derek
 */
public class BatchOperationUpdateRequest extends MetadataProvider implements IBatchOperationUpdateRequest,
		Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = 7636526750514669256L;

	/** Processing status for operation */
	private BatchOperationStatus processingStatus;

	/** Date when operation processing started */
	private Date processingStartedDate;

	/** Date when operation processing ended */
	private Date processingEndedDate;

	public BatchOperationStatus getProcessingStatus() {
		return processingStatus;
	}

	public void setProcessingStatus(BatchOperationStatus processingStatus) {
		this.processingStatus = processingStatus;
	}

	public Date getProcessingStartedDate() {
		return processingStartedDate;
	}

	public void setProcessingStartedDate(Date processingStartedDate) {
		this.processingStartedDate = processingStartedDate;
	}

	public Date getProcessingEndedDate() {
		return processingEndedDate;
	}

	public void setProcessingEndedDate(Date processingEndedDate) {
		this.processingEndedDate = processingEndedDate;
	}
}