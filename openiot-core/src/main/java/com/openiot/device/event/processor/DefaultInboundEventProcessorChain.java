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
import com.openiot.spi.device.event.processor.IInboundEventProcessor;
import com.openiot.spi.device.event.processor.IInboundEventProcessorChain;
import com.openiot.spi.device.event.request.*;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link IInboundEventProcessorChain} interface.
 * 
 * @author Derek
 */
public class DefaultInboundEventProcessorChain extends LifecycleComponent implements
		IInboundEventProcessorChain {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(DefaultInboundEventProcessorChain.class);

	/** List of processors */
	private List<IInboundEventProcessor> processors = new ArrayList<IInboundEventProcessor>();

	public DefaultInboundEventProcessorChain() {
		super(LifecycleComponentType.InboundProcessorChain);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		getLifecycleComponents().clear();
		for (IInboundEventProcessor processor : getProcessors()) {
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
		for (IInboundEventProcessor processor : getProcessors()) {
			processor.lifecycleStop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IInboundEventProcessor#onRegistrationRequest
	 * (java.lang.String, java.lang.String,
	 * IDeviceRegistrationRequest)
	 */
	@Override
	public void onRegistrationRequest(String hardwareId, String originator, IDeviceRegistrationRequest request)
			throws OpenIoTException {
		for (IInboundEventProcessor processor : getProcessors()) {
			try {
				processor.onRegistrationRequest(hardwareId, originator, request);
			} catch (OpenIoTException e) {
				LOGGER.error("Processor failed to process registration request.", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IInboundEventProcessor#
	 * onDeviceCommandResponseRequest(java.lang.String, java.lang.String,
	 * IDeviceCommandResponseCreateRequest)
	 */
	@Override
	public void onDeviceCommandResponseRequest(String hardwareId, String originator,
			IDeviceCommandResponseCreateRequest request) throws OpenIoTException {
		for (IInboundEventProcessor processor : getProcessors()) {
			try {
				processor.onDeviceCommandResponseRequest(hardwareId, originator, request);
			} catch (OpenIoTException e) {
				LOGGER.error("Processor failed to process command response request.", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IInboundEventProcessor#
	 * onDeviceMeasurementsCreateRequest(java.lang.String, java.lang.String,
	 * IDeviceMeasurementsCreateRequest)
	 */
	@Override
	public void onDeviceMeasurementsCreateRequest(String hardwareId, String originator,
			IDeviceMeasurementsCreateRequest request) throws OpenIoTException {
		for (IInboundEventProcessor processor : getProcessors()) {
			try {
				processor.onDeviceMeasurementsCreateRequest(hardwareId, originator, request);
			} catch (OpenIoTException e) {
				LOGGER.error("Processor failed to process measurements create request.", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IInboundEventProcessor#
	 * onDeviceLocationCreateRequest(java.lang.String, java.lang.String,
	 * IDeviceLocationCreateRequest)
	 */
	@Override
	public void onDeviceLocationCreateRequest(String hardwareId, String originator,
			IDeviceLocationCreateRequest request) throws OpenIoTException {
		for (IInboundEventProcessor processor : getProcessors()) {
			try {
				processor.onDeviceLocationCreateRequest(hardwareId, originator, request);
			} catch (OpenIoTException e) {
				LOGGER.error("Processor failed to process location create request.", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IInboundEventProcessor#
	 * onDeviceAlertCreateRequest(java.lang.String, java.lang.String,
	 * IDeviceAlertCreateRequest)
	 */
	@Override
	public void onDeviceAlertCreateRequest(String hardwareId, String originator,
			IDeviceAlertCreateRequest request) throws OpenIoTException {
		for (IInboundEventProcessor processor : getProcessors()) {
			try {
				processor.onDeviceAlertCreateRequest(hardwareId, originator, request);
			} catch (OpenIoTException e) {
				LOGGER.error("Processor failed to process alert create request.", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IInboundEventProcessorChain#getProcessors
	 * ()
	 */
	public List<IInboundEventProcessor> getProcessors() {
		return processors;
	}

	public void setProcessors(List<IInboundEventProcessor> processors) {
		this.processors = processors;
	}
}