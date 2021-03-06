/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.batch;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.openiot.rest.model.common.MetadataProviderEntity;
import com.openiot.rest.model.datatype.JsonDateSerializer;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.batch.BatchOperationStatus;
import com.openiot.spi.device.batch.IBatchOperation;
import com.openiot.spi.device.batch.OperationType;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Model object for a batch operation.
 * 
 * @author Derek
 */
public class BatchOperation extends MetadataProviderEntity implements IBatchOperation, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = -228183022121018340L;

	/** Unqiue token */
	private String token;

	/** Operation type requested */
	private OperationType operationType;

	/** Operation parameters */
	private Map<String, String> parameters = new HashMap<String, String>();

	/** Processing status for operation */
	private BatchOperationStatus processingStatus = BatchOperationStatus.Unprocessed;

	/** Date when operation processing started */
	private Date processingStartedDate;

	/** Date when operation processing ended */
	private Date processingEndedDate;

	/*
	 * (non-Javadoc)
	 * 
	 * @see IBatchOperation#getToken()
	 */
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IBatchOperation#getOperationType()
	 */
	public OperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IBatchOperation#getParameters()
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IBatchOperation#getProcessingStatus()
	 */
	public BatchOperationStatus getProcessingStatus() {
		return processingStatus;
	}

	public void setProcessingStatus(BatchOperationStatus processingStatus) {
		this.processingStatus = processingStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IBatchOperation#getProcessingStartedDate()
	 */
	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getProcessingStartedDate() {
		return processingStartedDate;
	}

	public void setProcessingStartedDate(Date processingStartedDate) {
		this.processingStartedDate = processingStartedDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IBatchOperation#getProcessingEndedDate()
	 */
	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getProcessingEndedDate() {
		return processingEndedDate;
	}

	public void setProcessingEndedDate(Date processingEndedDate) {
		this.processingEndedDate = processingEndedDate;
	}

	public static BatchOperation copy(IBatchOperation input) throws OpenIoTException {
		BatchOperation result = new BatchOperation();
		result.setToken(input.getToken());
		result.setOperationType(input.getOperationType());
		result.getParameters().putAll(input.getParameters());
		result.setProcessingStatus(input.getProcessingStatus());
		result.setProcessingStartedDate(input.getProcessingStartedDate());
		result.setProcessingEndedDate(input.getProcessingEndedDate());
		MetadataProviderEntity.copy(input, result);
		return result;
	}
}