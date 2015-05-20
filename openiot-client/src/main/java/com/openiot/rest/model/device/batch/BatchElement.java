/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.batch;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.rest.model.datatype.JsonDateSerializer;
import com.openiot.spi.device.batch.ElementProcessingStatus;
import com.openiot.spi.device.batch.IBatchElement;

import java.io.Serializable;
import java.util.Date;

/**
 * Model object for a batch element.
 * 
 * @author Derek
 */
public class BatchElement extends MetadataProvider implements IBatchElement, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = 7080873473253195755L;

	/** Token for parent batch operation */
	private String batchOperationToken;

	/** Hardware id */
	private String hardwareId;

	/** Element index */
	private long index;

	/** Processing status */
	private ElementProcessingStatus processingStatus;

	/** Date on which element was processed */
	private Date processedDate;

	/*
	 * (non-Javadoc)
	 * 
	 * @see IBatchElement#getBatchOperationToken()
	 */
	public String getBatchOperationToken() {
		return batchOperationToken;
	}

	public void setBatchOperationToken(String batchOperationToken) {
		this.batchOperationToken = batchOperationToken;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IBatchElement#getHardwareId()
	 */
	public String getHardwareId() {
		return hardwareId;
	}

	public void setHardwareId(String hardwareId) {
		this.hardwareId = hardwareId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IBatchElement#getIndex()
	 */
	public long getIndex() {
		return index;
	}

	public void setIndex(long index) {
		this.index = index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IBatchElement#getProcessingStatus()
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
	 * @see IBatchElement#getProcessedDate()
	 */
	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getProcessedDate() {
		return processedDate;
	}

	public void setProcessedDate(Date processedDate) {
		this.processedDate = processedDate;
	}
}