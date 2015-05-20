/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.event.processor;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.batch.IBatchOperation;
import com.openiot.spi.device.event.*;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;

/**
 * Allows intereseted entities to interact with OpenIoT outbound event processing.
 * 
 * @author Derek
 */
public interface IOutboundEventProcessor extends ILifecycleComponent {

	/**
	 * Executes code after device measurements have been successfully saved.
	 * 
	 * @param measurements
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void onMeasurements(IDeviceMeasurements measurements) throws OpenIoTException;

	/**
	 * Executes code after device location has been successfully saved.
	 * 
	 * @param location
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void onLocation(IDeviceLocation location) throws OpenIoTException;

	/**
	 * Executes code after device alert has been successfully saved.
	 * 
	 * @param location
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void onAlert(IDeviceAlert alert) throws OpenIoTException;

	/**
	 * Executes code after device command invocation has been successfully saved.
	 * 
	 * @param invocation
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void onCommandInvocation(IDeviceCommandInvocation invocation) throws OpenIoTException;

	/**
	 * Executes code after device command response has been successfully saved.
	 * 
	 * @param response
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void onCommandResponse(IDeviceCommandResponse response) throws OpenIoTException;

	/**
	 * Executes code after batch operation has been successfully saved.
	 * 
	 * @param operation
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void onBatchOperation(IBatchOperation operation) throws OpenIoTException;
}