/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.event.processor;

import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.batch.IBatchOperation;
import com.openiot.spi.device.event.*;
import com.openiot.spi.device.event.processor.IOutboundEventProcessor;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;

/**
 * Default implementation of {@link IOutboundEventProcessor}.
 * 
 * @author Derek
 */
public abstract class OutboundEventProcessor extends LifecycleComponent implements IOutboundEventProcessor {

	public OutboundEventProcessor() {
		super(LifecycleComponentType.OutboundEventProcessor);
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundEventProcessor#onBatchOperation
	 * (IBatchOperation)
	 */
	public void onBatchOperation(IBatchOperation operation) throws OpenIoTException {
	}
}