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
import com.openiot.spi.device.event.request.*;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;

/**
 * Default implementation of {@link IInboundEventProcessor} interface with nothing
 * implemented.
 * 
 * @author Derek
 */
public abstract class InboundEventProcessor extends LifecycleComponent implements IInboundEventProcessor {

	public InboundEventProcessor() {
		super(LifecycleComponentType.InboundEventProcessor);
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
	}
}