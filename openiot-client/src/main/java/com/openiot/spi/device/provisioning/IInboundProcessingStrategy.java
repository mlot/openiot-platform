/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.provisioning;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.request.IDeviceAlertCreateRequest;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;

/**
 * Provides a strategy for moving decoded events from an {@link IInboundEventSource} onto
 * the {@link com.openiot.spi.device.event.processor.IInboundEventProcessorChain}.
 * 
 * @author Derek
 */
public interface IInboundProcessingStrategy extends ILifecycleComponent {

	/**
	 * Process an {@link com.openiot.spi.device.event.request.IDeviceRegistrationRequest}.
	 * 
	 * @param request
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void processRegistration(IDecodedDeviceEventRequest request) throws OpenIoTException;

	/**
	 * Process an {@link com.openiot.spi.device.event.request.IDeviceCommandResponseCreateRequest}.
	 * 
	 * @param request
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void processDeviceCommandResponse(IDecodedDeviceEventRequest request) throws OpenIoTException;

	/**
	 * Process an {@link com.openiot.spi.device.event.request.IDeviceMeasurementsCreateRequest}.
	 * 
	 * @param request
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void processDeviceMeasurements(IDecodedDeviceEventRequest request) throws OpenIoTException;

	/**
	 * Process an {@link com.openiot.spi.device.event.request.IDeviceLocationCreateRequest}.
	 * 
	 * @param request
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void processDeviceLocation(IDecodedDeviceEventRequest request) throws OpenIoTException;

	/**
	 * Process an {@link IDeviceAlertCreateRequest}.
	 * 
	 * @param request
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void processDeviceAlert(IDecodedDeviceEventRequest request) throws OpenIoTException;
}