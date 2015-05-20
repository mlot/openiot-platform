/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning;

import com.openiot.OpenIoT;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.batch.IBatchOperation;
import com.openiot.spi.device.event.*;
import com.openiot.spi.device.event.processor.IOutboundEventProcessorChain;
import com.openiot.spi.device.provisioning.IOutboundProcessingStrategy;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;

/**
 * Implementation of {@link IOutboundProcessingStrategy} that sends messages directly to
 * the {@link IOutboundEventProcessorChain}.
 * 
 * @author Derek
 */
public class DirectOutboundProcessingStrategy extends LifecycleComponent implements
		IOutboundProcessingStrategy {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(DirectOutboundProcessingStrategy.class);

	public DirectOutboundProcessingStrategy() {
		super(LifecycleComponentType.OutboundProcessingStrategy);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#getLogger()
	 */
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundEventProcessor#onMeasurements
	 * (IDeviceMeasurements)
	 */
	@Override
	public void onMeasurements(IDeviceMeasurements measurements) throws OpenIoTException {
		OpenIoT.getServer().getOutboundEventProcessorChain().onMeasurements(measurements);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundEventProcessor#onLocation(com
	 * .sitewhere.spi.device.event.IDeviceLocation)
	 */
	@Override
	public void onLocation(IDeviceLocation location) throws OpenIoTException {
		OpenIoT.getServer().getOutboundEventProcessorChain().onLocation(location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundEventProcessor#onAlert(com.openiot
	 * .spi.device.event.IDeviceAlert)
	 */
	@Override
	public void onAlert(IDeviceAlert alert) throws OpenIoTException {
		OpenIoT.getServer().getOutboundEventProcessorChain().onAlert(alert);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundEventProcessor#onCommandInvocation
	 * (IDeviceCommandInvocation)
	 */
	@Override
	public void onCommandInvocation(IDeviceCommandInvocation invocation) throws OpenIoTException {
		OpenIoT.getServer().getOutboundEventProcessorChain().onCommandInvocation(invocation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundEventProcessor#onCommandResponse
	 * (IDeviceCommandResponse)
	 */
	@Override
	public void onCommandResponse(IDeviceCommandResponse response) throws OpenIoTException {
		OpenIoT.getServer().getOutboundEventProcessorChain().onCommandResponse(response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundEventProcessor#onBatchOperation
	 * (IBatchOperation)
	 */
	@Override
	public void onBatchOperation(IBatchOperation operation) throws OpenIoTException {
		OpenIoT.getServer().getOutboundEventProcessorChain().onBatchOperation(operation);
	}
}