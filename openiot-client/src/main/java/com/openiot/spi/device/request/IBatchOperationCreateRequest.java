/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.request;

import com.openiot.spi.common.IMetadataProvider;
import com.openiot.spi.device.batch.OperationType;

import java.util.List;
import java.util.Map;

/**
 * Provides information needex to create an {@link com.openiot.spi.device.batch.IBatchOperation}.
 * 
 * @author Derek
 */
public interface IBatchOperationCreateRequest extends IMetadataProvider {

	/** Metadata property on events that holds batch id that generated event */
	public static final String META_BATCH_OPERATION_ID = "batch";

	/**
	 * Get the unique token.
	 * 
	 * @return
	 */
	public String getToken();

	/**
	 * Get operation to be performed.
	 * 
	 * @return
	 */
	public OperationType getOperationType();

	/**
	 * Get operation parameters.
	 * 
	 * @return
	 */
	public Map<String, String> getParameters();

	/**
	 * Get list of hardware ids for devices to be operated on.
	 * 
	 * @return
	 */
	public List<String> getHardwareIds();
}