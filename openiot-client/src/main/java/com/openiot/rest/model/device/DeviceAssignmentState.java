/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.openiot.rest.model.datatype.JsonDateSerializer;
import com.openiot.rest.model.device.event.DeviceAlert;
import com.openiot.rest.model.device.event.DeviceLocation;
import com.openiot.rest.model.device.event.DeviceMeasurement;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDeviceAssignmentState;
import com.openiot.spi.device.event.IDeviceAlert;
import com.openiot.spi.device.event.IDeviceLocation;
import com.openiot.spi.device.event.IDeviceMeasurement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model object for device assignment state.
 * 
 * @author Derek
 */
public class DeviceAssignmentState implements IDeviceAssignmentState, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = -8536671667872805013L;

	/** Date of last interaction with assignment */
	private Date lastInteractionDate;

	/** Last location event */
	private DeviceLocation lastLocation;

	/** Last measurement event for each measurement id */
	private List<DeviceMeasurement> latestMeasurements = new ArrayList<DeviceMeasurement>();

	/** Last alert event for each alert type */
	private List<DeviceAlert> latestAlerts = new ArrayList<DeviceAlert>();

	/** Last reply-to address */
	private String lastReplyTo;

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceAssignmentState#getLastInteractionDate()
	 */
	@Override
	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getLastInteractionDate() {
		return lastInteractionDate;
	}

	public void setLastInteractionDate(Date lastInteractionDate) {
		this.lastInteractionDate = lastInteractionDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceAssignmentState#getLastLocation()
	 */
	@Override
	public IDeviceLocation getLastLocation() {
		return lastLocation;
	}

	public void setLastLocation(DeviceLocation lastLocation) {
		this.lastLocation = lastLocation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceAssignmentState#getLatestMeasurements()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<IDeviceMeasurement> getLatestMeasurements() {
		return (List<IDeviceMeasurement>) (List<? extends IDeviceMeasurement>) latestMeasurements;
	}

	public void setLatestMeasurements(List<DeviceMeasurement> latestMeasurements) {
		this.latestMeasurements = latestMeasurements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceAssignmentState#getLatestAlerts()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<IDeviceAlert> getLatestAlerts() {
		return (List<IDeviceAlert>) (List<? extends IDeviceAlert>) latestAlerts;
	}

	public void setLatestAlerts(List<DeviceAlert> latestAlerts) {
		this.latestAlerts = latestAlerts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceAssignmentState#getLastReplyTo()
	 */
	@Override
	public String getLastReplyTo() {
		return lastReplyTo;
	}

	public void setLastReplyTo(String lastReplyTo) {
		this.lastReplyTo = lastReplyTo;
	}

	public static DeviceAssignmentState copy(IDeviceAssignmentState source) throws OpenIoTException {
		DeviceAssignmentState target = new DeviceAssignmentState();
		target.setLastInteractionDate(source.getLastInteractionDate());
		if (source.getLastLocation() != null) {
			target.setLastLocation(DeviceLocation.copy(source.getLastLocation()));
		}
		List<DeviceMeasurement> measurements = new ArrayList<DeviceMeasurement>();
		for (IDeviceMeasurement sm : source.getLatestMeasurements()) {
			measurements.add(DeviceMeasurement.copy(sm));
		}
		target.setLatestMeasurements(measurements);
		List<DeviceAlert> alerts = new ArrayList<DeviceAlert>();
		for (IDeviceAlert sa : source.getLatestAlerts()) {
			alerts.add(DeviceAlert.copy(sa));
		}
		target.setLatestAlerts(alerts);
		target.setLastReplyTo(source.getLastReplyTo());
		return target;
	}
}