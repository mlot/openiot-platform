/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device;

import com.openiot.spi.device.event.IDeviceAlert;
import com.openiot.spi.device.event.IDeviceLocation;
import com.openiot.spi.device.event.IDeviceMeasurement;

import java.util.Date;
import java.util.List;

/**
 * Holds event state for a device assignment including most recent location, measurements
 * and alerts.
 * 
 * @author Derek
 */
public interface IDeviceAssignmentState {

	/**
	 * Date of last interaction with device.
	 * 
	 * @return
	 */
	public Date getLastInteractionDate();

	/**
	 * Get last device location.
	 * 
	 * @return
	 */
	public IDeviceLocation getLastLocation();

	/**
	 * Get last measurement for each measurement id.
	 * 
	 * @return
	 */
	public List<IDeviceMeasurement> getLatestMeasurements();

	/**
	 * Get last alert for each alert type.
	 * 
	 * @return
	 */
	public List<IDeviceAlert> getLatestAlerts();

	/**
	 * Get the last 'reply to' for assignment.
	 * 
	 * @return
	 */
	public String getLastReplyTo();
}