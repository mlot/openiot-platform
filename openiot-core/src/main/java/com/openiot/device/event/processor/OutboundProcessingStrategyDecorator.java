/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.event.processor;

import com.openiot.OpenIoT;
import com.openiot.core.OpenIoTPersistence;
import com.openiot.device.DeviceManagementDecorator;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDeviceManagement;
import com.openiot.spi.device.batch.IBatchOperation;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.event.*;
import com.openiot.spi.device.event.processor.IOutboundEventProcessorChain;
import com.openiot.spi.device.event.request.*;
import com.openiot.spi.device.provisioning.IOutboundProcessingStrategy;
import com.openiot.spi.device.request.IBatchCommandInvocationRequest;
import com.openiot.spi.device.request.IBatchOperationCreateRequest;

/**
 * Acts as a decorator for injecting a {@link IOutboundEventProcessorChain} into the
 * default processing flow.
 * 
 * @author Derek
 */
public class OutboundProcessingStrategyDecorator extends DeviceManagementDecorator {

	/** Processor chain */
	private IOutboundProcessingStrategy outbound;

	public OutboundProcessingStrategyDecorator(IDeviceManagement delegate) {
		this(delegate, OpenIoT.getServer().getDeviceProvisioning().getOutboundProcessingStrategy());
	}

	public OutboundProcessingStrategyDecorator(IDeviceManagement delegate,
			IOutboundProcessingStrategy outbound) {
		super(delegate);
		this.outbound = outbound;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.rest.model.device.DeviceManagementDecorator#addDeviceEventBatch(java
	 * .lang.String, IDeviceEventBatch)
	 */
	@Override
	public IDeviceEventBatchResponse addDeviceEventBatch(String assignmentToken, IDeviceEventBatch batch)
			throws OpenIoTException {
		return OpenIoTPersistence.deviceEventBatchLogic(assignmentToken, batch, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.rest.model.device.DeviceManagementDecorator#addDeviceMeasurements
	 * (java.lang.String,
	 * IDeviceMeasurementsCreateRequest)
	 */
	@Override
	public IDeviceMeasurements addDeviceMeasurements(String assignmentToken,
			IDeviceMeasurementsCreateRequest request) throws OpenIoTException {
		IDeviceMeasurements result = super.addDeviceMeasurements(assignmentToken, request);
		outbound.onMeasurements(result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.rest.model.device.DeviceManagementDecorator#addDeviceLocation(java
	 * .lang.String, IDeviceLocationCreateRequest)
	 */
	@Override
	public IDeviceLocation addDeviceLocation(String assignmentToken, IDeviceLocationCreateRequest request)
			throws OpenIoTException {
		IDeviceLocation result = super.addDeviceLocation(assignmentToken, request);
		outbound.onLocation(result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.rest.model.device.DeviceManagementDecorator#addDeviceAlert(java.lang
	 * .String, IDeviceAlertCreateRequest)
	 */
	@Override
	public IDeviceAlert addDeviceAlert(String assignmentToken, IDeviceAlertCreateRequest request)
			throws OpenIoTException {
		IDeviceAlert result = super.addDeviceAlert(assignmentToken, request);
		outbound.onAlert(result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.rest.model.device.DeviceManagementDecorator#addDeviceCommandInvocation
	 * (java.lang.String, IDeviceCommand,
	 * IDeviceCommandInvocationCreateRequest)
	 */
	@Override
	public IDeviceCommandInvocation addDeviceCommandInvocation(String assignmentToken,
			IDeviceCommand command, IDeviceCommandInvocationCreateRequest request) throws OpenIoTException {
		IDeviceCommandInvocation result = super.addDeviceCommandInvocation(assignmentToken, command, request);
		outbound.onCommandInvocation(result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.rest.model.device.DeviceManagementDecorator#addDeviceCommandResponse
	 * (java.lang.String,
	 * IDeviceCommandResponseCreateRequest)
	 */
	@Override
	public IDeviceCommandResponse addDeviceCommandResponse(String assignmentToken,
			IDeviceCommandResponseCreateRequest request) throws OpenIoTException {
		IDeviceCommandResponse result = super.addDeviceCommandResponse(assignmentToken, request);
		outbound.onCommandResponse(result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * DeviceManagementDecorator#createBatchOperation(com.openiot
	 * .spi.device.request.IBatchOperationCreateRequest)
	 */
	@Override
	public IBatchOperation createBatchOperation(IBatchOperationCreateRequest request)
			throws OpenIoTException {
		IBatchOperation result = super.createBatchOperation(request);
		outbound.onBatchOperation(result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * DeviceManagementDecorator#createBatchCommandInvocation(com
	 * .sitewhere.spi.device.request.IBatchCommandInvocationRequest)
	 */
	@Override
	public IBatchOperation createBatchCommandInvocation(IBatchCommandInvocationRequest request)
			throws OpenIoTException {
		IBatchOperation result = super.createBatchCommandInvocation(request);
		outbound.onBatchOperation(result);
		return result;
	}
}