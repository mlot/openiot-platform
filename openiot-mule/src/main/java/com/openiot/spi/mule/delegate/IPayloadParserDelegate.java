/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.mule.delegate;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.request.IDeviceAlertCreateRequest;
import com.openiot.spi.device.event.request.IDeviceLocationCreateRequest;
import com.openiot.spi.device.event.request.IDeviceMeasurementsCreateRequest;
import org.mule.api.MuleEvent;

import java.util.List;

/**
 * Interface for class that parses OpenIoT context information from a given type of payload.
 * 
 * @author Derek
 */
public interface IPayloadParserDelegate {

	/**
	 * Initialize the delegate with information from the current MuleEvent.
	 * 
	 * @param event
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void initialize(MuleEvent event) throws OpenIoTException;

	/**
	 * Get hardware id of the device the payload references.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public String getDeviceHardwareId() throws OpenIoTException;

	/**
	 * Get information for replying to the originator.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public String getReplyTo() throws OpenIoTException;

	/**
	 * Get a list of location create requests associated with the payload.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<IDeviceLocationCreateRequest> getLocations() throws OpenIoTException;

	/**
	 * Get a list of measurement create requests associated with the payload.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<IDeviceMeasurementsCreateRequest> getMeasurements() throws OpenIoTException;

	/**
	 * Get a list of alert create requests associated with the payload.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<IDeviceAlertCreateRequest> getAlerts() throws OpenIoTException;
}