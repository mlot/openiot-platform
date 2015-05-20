/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model;

import com.openiot.spi.IOpenIoTContext;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.event.*;
import com.openiot.spi.device.event.request.IDeviceAlertCreateRequest;
import com.openiot.spi.device.event.request.IDeviceLocationCreateRequest;
import com.openiot.spi.device.event.request.IDeviceMeasurementsCreateRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Default context implementation.
 * 
 * @author dadams
 */
public class OpenIoTContext implements IOpenIoTContext {

	/** Current assignment for device */
	private IDeviceAssignment deviceAssignment;

	/** Measurements that have not been persisted */
	private List<IDeviceMeasurementsCreateRequest> unsavedDeviceMeasurements =
			new ArrayList<IDeviceMeasurementsCreateRequest>();

	/** Locations that have not been persisted */
	private List<IDeviceLocationCreateRequest> unsavedDeviceLocations =
			new ArrayList<IDeviceLocationCreateRequest>();

	/** Alerts that have not been persisted */
	private List<IDeviceAlertCreateRequest> unsavedDeviceAlerts = new ArrayList<IDeviceAlertCreateRequest>();

	/** Measurements that have been persisted */
	private List<IDeviceMeasurements> deviceMeasurements = new ArrayList<IDeviceMeasurements>();

	/** Locations that have been persisted */
	private List<IDeviceLocation> deviceLocations = new ArrayList<IDeviceLocation>();

	/** Alerts that have been persisted */
	private List<IDeviceAlert> deviceAlerts = new ArrayList<IDeviceAlert>();

	/** Command invocations that have been persisted */
	private List<IDeviceCommandInvocation> deviceCommandInvocations =
			new ArrayList<IDeviceCommandInvocation>();

	/** Command responses that have been persisted */
	private List<IDeviceCommandResponse> deviceCommandResponses = new ArrayList<IDeviceCommandResponse>();

	/** Information for replying to originator */
	private String replyTo;

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTContext#getDeviceAssignment()
	 */
	public IDeviceAssignment getDeviceAssignment() {
		return deviceAssignment;
	}

	public void setDeviceAssignment(IDeviceAssignment deviceAssignment) {
		this.deviceAssignment = deviceAssignment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTContext#getUnsavedDeviceMeasurements()
	 */
	public List<IDeviceMeasurementsCreateRequest> getUnsavedDeviceMeasurements() {
		return unsavedDeviceMeasurements;
	}

	public void setUnsavedDeviceMeasurements(List<IDeviceMeasurementsCreateRequest> unsavedDeviceMeasurements) {
		this.unsavedDeviceMeasurements = unsavedDeviceMeasurements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTContext#getUnsavedDeviceLocations()
	 */
	public List<IDeviceLocationCreateRequest> getUnsavedDeviceLocations() {
		return unsavedDeviceLocations;
	}

	public void setUnsavedDeviceLocations(List<IDeviceLocationCreateRequest> unsavedDeviceLocations) {
		this.unsavedDeviceLocations = unsavedDeviceLocations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTContext#getUnsavedDeviceAlerts()
	 */
	public List<IDeviceAlertCreateRequest> getUnsavedDeviceAlerts() {
		return unsavedDeviceAlerts;
	}

	public void setUnsavedDeviceAlerts(List<IDeviceAlertCreateRequest> unsavedDeviceAlerts) {
		this.unsavedDeviceAlerts = unsavedDeviceAlerts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTContext#getDeviceMeasurements()
	 */
	public List<IDeviceMeasurements> getDeviceMeasurements() {
		return deviceMeasurements;
	}

	public void setDeviceMeasurements(List<IDeviceMeasurements> deviceMeasurements) {
		this.deviceMeasurements = deviceMeasurements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTContext#getDeviceLocations()
	 */
	public List<IDeviceLocation> getDeviceLocations() {
		return deviceLocations;
	}

	public void setDeviceLocations(List<IDeviceLocation> deviceLocations) {
		this.deviceLocations = deviceLocations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTContext#getDeviceAlerts()
	 */
	public List<IDeviceAlert> getDeviceAlerts() {
		return deviceAlerts;
	}

	public void setDeviceAlerts(List<IDeviceAlert> deviceAlerts) {
		this.deviceAlerts = deviceAlerts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTContext#getDeviceCommandInvocations()
	 */
	public List<IDeviceCommandInvocation> getDeviceCommandInvocations() {
		return deviceCommandInvocations;
	}

	public void setDeviceCommandInvocations(List<IDeviceCommandInvocation> deviceCommandInvocations) {
		this.deviceCommandInvocations = deviceCommandInvocations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTContext#getDeviceCommandResponses()
	 */
	public List<IDeviceCommandResponse> getDeviceCommandResponses() {
		return deviceCommandResponses;
	}

	public void setDeviceCommandResponses(List<IDeviceCommandResponse> deviceCommandResponses) {
		this.deviceCommandResponses = deviceCommandResponses;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTContext#getReplyTo()
	 */
	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	/**
	 * Helper function for add an arbitrary device event.
	 * 
	 * @param event
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void addDeviceEvent(IDeviceEvent event) throws OpenIoTException {
		if (event instanceof IDeviceMeasurements) {
			getDeviceMeasurements().add((IDeviceMeasurements) event);
		} else if (event instanceof IDeviceLocation) {
			getDeviceLocations().add((IDeviceLocation) event);
		} else if (event instanceof IDeviceAlert) {
			getDeviceAlerts().add((IDeviceAlert) event);
		} else if (event instanceof IDeviceCommandInvocation) {
			getDeviceCommandInvocations().add((IDeviceCommandInvocation) event);
		} else if (event instanceof IDeviceCommandResponse) {
			getDeviceCommandResponses().add((IDeviceCommandResponse) event);
		} else {
			throw new OpenIoTException("Context does not support event type: " + event.getClass().getName());
		}
	}
}