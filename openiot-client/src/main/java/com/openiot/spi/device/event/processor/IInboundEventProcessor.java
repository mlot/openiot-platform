/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.event.processor;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.IDeviceAlert;
import com.openiot.spi.device.event.IDeviceLocation;
import com.openiot.spi.device.event.IDeviceMeasurements;
import com.openiot.spi.device.event.request.*;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;

/**
 * Allows interested entities to interact with OpenIoT inbound event processing.
 * 
 * @author Derek
 */
public interface IInboundEventProcessor extends ILifecycleComponent {

	/**
	 * Called when a {@link IDeviceRegistrationRequest} is received.
	 * 
	 * @param hardwareId
	 * @param originator
	 * @param request
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void onRegistrationRequest(String hardwareId, String originator, IDeviceRegistrationRequest request)
			throws OpenIoTException;

	/**
	 * Called when an {@link IDeviceCommandResponseCreateRequest} is received.
	 * 
	 * @param hardwareId
	 * @param originator
	 * @param request
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void onDeviceCommandResponseRequest(String hardwareId, String originator,
			IDeviceCommandResponseCreateRequest request) throws OpenIoTException;

	/**
	 * Called to request the creation of a new {@link IDeviceMeasurements} based on the
	 * given information.
	 * 
	 * @param hardwareId
	 * @param originator
	 * @param request
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void onDeviceMeasurementsCreateRequest(String hardwareId, String originator,
			IDeviceMeasurementsCreateRequest request) throws OpenIoTException;

	/**
	 * Called to request the creation of a new {@link IDeviceLocation} based on the given
	 * information.
	 * 
	 * @param hardwareId
	 * @param originator
	 * @param request
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void onDeviceLocationCreateRequest(String hardwareId, String originator,
			IDeviceLocationCreateRequest request) throws OpenIoTException;

	/**
	 * Called to request the creation of a new {@link IDeviceAlert} based on the given
	 * information.
	 * 
	 * @param hardwareId
	 * @param originator
	 * @param request
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void onDeviceAlertCreateRequest(String hardwareId, String originator,
			IDeviceAlertCreateRequest request) throws OpenIoTException;
}