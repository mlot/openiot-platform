/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi;

import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.event.*;
import com.openiot.spi.device.event.request.IDeviceAlertCreateRequest;
import com.openiot.spi.device.event.request.IDeviceLocationCreateRequest;
import com.openiot.spi.device.event.request.IDeviceMeasurementsCreateRequest;

import java.util.List;

/**
 * Holds OpenIoT information associated with a reqeust.
 * 
 * @author dadams
 */
public interface IOpenIoTContext {

	/**
	 * Get current assignment for device associated with the request.
	 * 
	 * @return
	 */
	public IDeviceAssignment getDeviceAssignment();

	/**
	 * Get a list of device measurements that have not been persisted.
	 * 
	 * @return
	 */
	public List<IDeviceMeasurementsCreateRequest> getUnsavedDeviceMeasurements();

	/**
	 * Get a list of device locations that have not been persisted.
	 * 
	 * @return
	 */
	public List<IDeviceLocationCreateRequest> getUnsavedDeviceLocations();

	/**
	 * Get a list of device alerts that have not been persisted.
	 * 
	 * @return
	 */
	public List<IDeviceAlertCreateRequest> getUnsavedDeviceAlerts();

	/**
	 * Get the {@link IDeviceMeasurements} events.
	 * 
	 * @return
	 */
	public List<IDeviceMeasurements> getDeviceMeasurements();

	/**
	 * Get the {@link IDeviceLocation} events.
	 * 
	 * @return
	 */
	public List<IDeviceLocation> getDeviceLocations();

	/**
	 * Get the {@link IDeviceAlert} events.
	 * 
	 * @return
	 */
	public List<IDeviceAlert> getDeviceAlerts();

	/**
	 * Get the {@link IDeviceCommandInvocation} events.
	 * 
	 * @return
	 */
	public List<IDeviceCommandInvocation> getDeviceCommandInvocations();

	/**
	 * Get the {@link IDeviceCommandResponse} events.
	 * 
	 * @return
	 */
	public List<IDeviceCommandResponse> getDeviceCommandResponses();

	/**
	 * Get information for replying to originator.
	 * 
	 * @return
	 */
	public String getReplyTo();
}