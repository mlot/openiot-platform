/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.search.device;

import com.openiot.rest.model.search.SearchCriteria;
import com.openiot.spi.device.batch.ElementProcessingStatus;
import com.openiot.spi.search.device.IBatchElementSearchCriteria;

/**
 * Search criteria specific to searching a list of batch operation elements.
 * 
 * @author Derek
 */
public class BatchElementSearchCriteria extends SearchCriteria implements IBatchElementSearchCriteria {

	/** Element processing status */
	private ElementProcessingStatus processingStatus;

	public BatchElementSearchCriteria(int pageNumber, int pageSize) {
		super(pageNumber, pageSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IBatchElementSearchCriteria#getProcessingStatus()
	 */
	public ElementProcessingStatus getProcessingStatus() {
		return processingStatus;
	}

	public void setProcessingStatus(ElementProcessingStatus processingStatus) {
		this.processingStatus = processingStatus;
	}
}