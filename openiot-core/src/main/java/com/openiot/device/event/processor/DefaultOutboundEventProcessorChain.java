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
import com.openiot.spi.device.event.processor.IOutboundEventProcessorChain;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import com.openiot.spi.server.lifecycle.LifecycleStatus;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link IOutboundEventProcessorChain} interface.
 * 
 * @author Derek
 */
public class DefaultOutboundEventProcessorChain extends LifecycleComponent implements
		IOutboundEventProcessorChain {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(DefaultOutboundEventProcessorChain.class);

	/** Indicates whether processing is enabled */
	private boolean processingEnabled = false;

	/** List of event processors */
	private List<IOutboundEventProcessor> processors = new ArrayList<IOutboundEventProcessor>();

	public DefaultOutboundEventProcessorChain() {
		super(LifecycleComponentType.OutboundProcessorChain);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		getLifecycleComponents().clear();
		for (IOutboundEventProcessor processor : getProcessors()) {
			startNestedComponent(processor, false);
		}
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
		for (IOutboundEventProcessor processor : getProcessors()) {
			processor.lifecycleStop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOutboundEventProcessorChain#
	 * setProcessingEnabled(boolean)
	 */
	@Override
	public void setProcessingEnabled(boolean enabled) {
		this.processingEnabled = enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOutboundEventProcessorChain#
	 * isProcessingEnabled()
	 */
	@Override
	public boolean isProcessingEnabled() {
		return processingEnabled;
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
		if (isProcessingEnabled()) {
			for (IOutboundEventProcessor processor : getProcessors()) {
				try {
					if (processor.getLifecycleStatus() == LifecycleStatus.Started) {
						processor.onMeasurements(measurements);
					} else {
						logSkipped(processor);
					}
				} catch (OpenIoTException e) {
					LOGGER.error(e);
				}
			}
		}
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
		if (isProcessingEnabled()) {
			for (IOutboundEventProcessor processor : getProcessors()) {
				try {
					if (processor.getLifecycleStatus() == LifecycleStatus.Started) {
						processor.onLocation(location);
					} else {
						logSkipped(processor);
					}
				} catch (OpenIoTException e) {
					LOGGER.error(e);
				}
			}
		}
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
		if (isProcessingEnabled()) {
			for (IOutboundEventProcessor processor : getProcessors()) {
				try {
					if (processor.getLifecycleStatus() == LifecycleStatus.Started) {
						processor.onAlert(alert);
					} else {
						logSkipped(processor);
					}
				} catch (OpenIoTException e) {
					LOGGER.error(e);
				}
			}
		}
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
		if (isProcessingEnabled()) {
			for (IOutboundEventProcessor processor : getProcessors()) {
				try {
					if (processor.getLifecycleStatus() == LifecycleStatus.Started) {
						processor.onCommandInvocation(invocation);
					} else {
						logSkipped(processor);
					}
				} catch (OpenIoTException e) {
					LOGGER.error(e);
				}
			}
		}
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
		if (isProcessingEnabled()) {
			for (IOutboundEventProcessor processor : getProcessors()) {
				try {
					if (processor.getLifecycleStatus() == LifecycleStatus.Started) {
						processor.onCommandResponse(response);
					} else {
						logSkipped(processor);
					}
				} catch (OpenIoTException e) {
					LOGGER.error(e);
				}
			}
		}
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
		if (isProcessingEnabled()) {
			for (IOutboundEventProcessor processor : getProcessors()) {
				try {
					if (processor.getLifecycleStatus() == LifecycleStatus.Started) {
						processor.onBatchOperation(operation);
					} else {
						logSkipped(processor);
					}
				} catch (OpenIoTException e) {
					LOGGER.error(e);
				}
			}
		}
	}

	/**
	 * Output log message indicating a processor was skipped.
	 * 
	 * @param processor
	 */
	protected void logSkipped(IOutboundEventProcessor processor) {
		getLogger().warn(
				"Skipping event processor " + processor.getComponentName() + " because its state is '"
						+ processor.getLifecycleStatus() + "'");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundEventProcessorChain#getProcessors
	 * ()
	 */
	@Override
	public List<IOutboundEventProcessor> getProcessors() {
		return processors;
	}

	public void setProcessors(List<IOutboundEventProcessor> processors) {
		this.processors = processors;
	}
}