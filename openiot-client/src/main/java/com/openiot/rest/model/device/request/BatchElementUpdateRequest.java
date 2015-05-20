/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.request;

import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.spi.device.batch.ElementProcessingStatus;
import com.openiot.spi.device.request.IBatchElementUpdateRequest;

import java.io.Serializable;
import java.util.Date;

/**
 * Holds information needed to update a batch operation element.
 * 
 * @author Derek
 */
public class BatchElementUpdateRequest extends MetadataProvider implements IBatchElementUpdateRequest,
		Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = -3369336266183401785L;

	/** Processing status for update */
	private ElementProcessingStatus processingStatus;

	/** Date element was processed */
	private Date processedDate;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IBatchElementUpdateRequest#getProcessingStatus()
	 */
	public ElementProcessingStatus getProcessingStatus() {
		return processingStatus;
	}

	public void setProcessingStatus(ElementProcessingStatus processingStatus) {
		this.processingStatus = processingStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IBatchElementUpdateRequest#getProcessedDate()
	 */
	public Date getProcessedDate() {
		return processedDate;
	}

	public void setProcessedDate(Date processedDate) {
		this.processedDate = processedDate;
	}
}