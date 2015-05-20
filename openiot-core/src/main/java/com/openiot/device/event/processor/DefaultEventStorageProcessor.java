/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.event.processor;

import com.openiot.OpenIoT;
import com.openiot.rest.model.device.event.request.DeviceCommandResponseCreateRequest;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.IDevice;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.event.IDeviceAlert;
import com.openiot.spi.device.event.IDeviceCommandResponse;
import com.openiot.spi.device.event.IDeviceLocation;
import com.openiot.spi.device.event.IDeviceMeasurements;
import com.openiot.spi.device.event.processor.IInboundEventProcessor;
import com.openiot.spi.device.event.request.*;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import org.apache.log4j.Logger;

/**
 * Implementation of {@link IInboundEventProcessor} that attempts to store the inbound
 * event request using device management APIs.
 * 
 * @author Derek
 */
public class DefaultEventStorageProcessor extends InboundEventProcessor {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(DefaultEventStorageProcessor.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.rest.model.device.event.processor.InboundEventProcessor#
	 * onRegistrationRequest(java.lang.String, java.lang.String,
	 * IDeviceRegistrationRequest)
	 */
	@Override
	public void onRegistrationRequest(String hardwareId, String originator, IDeviceRegistrationRequest request)
			throws OpenIoTException {
		OpenIoT.getServer().getDeviceProvisioning().getRegistrationManager().handleDeviceRegistration(
				request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.rest.model.device.event.processor.InboundEventProcessor#
	 * onDeviceCommandResponseRequest(java.lang.String, java.lang.String,
	 * IDeviceCommandResponseCreateRequest)
	 */
	@Override
	public void onDeviceCommandResponseRequest(String hardwareId, String originator,
			IDeviceCommandResponseCreateRequest request) throws OpenIoTException {
		IDeviceAssignment assignment = getCurrentAssignment(hardwareId);
		OpenIoT.getServer().getDeviceManagement().addDeviceCommandResponse(assignment.getToken(), request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.rest.model.device.event.processor.InboundEventProcessor#
	 * onDeviceMeasurementsCreateRequest(java.lang.String, java.lang.String,
	 * IDeviceMeasurementsCreateRequest)
	 */
	@Override
	public void onDeviceMeasurementsCreateRequest(String hardwareId, String originator,
			IDeviceMeasurementsCreateRequest request) throws OpenIoTException {
		IDeviceAssignment assignment = getCurrentAssignment(hardwareId);
		IDeviceMeasurements measurements =
				OpenIoT.getServer().getDeviceManagement().addDeviceMeasurements(assignment.getToken(),
						request);
		handleLinkResponseToInvocation(originator, measurements.getId(), assignment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.rest.model.device.event.processor.InboundEventProcessor#
	 * onDeviceLocationCreateRequest(java.lang.String, java.lang.String,
	 * IDeviceLocationCreateRequest)
	 */
	@Override
	public void onDeviceLocationCreateRequest(String hardwareId, String originator,
			IDeviceLocationCreateRequest request) throws OpenIoTException {
		IDeviceAssignment assignment = getCurrentAssignment(hardwareId);
		IDeviceLocation location =
				OpenIoT.getServer().getDeviceManagement().addDeviceLocation(assignment.getToken(), request);
		handleLinkResponseToInvocation(originator, location.getId(), assignment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.rest.model.device.event.processor.InboundEventProcessor#
	 * onDeviceAlertCreateRequest(java.lang.String, java.lang.String,
	 * IDeviceAlertCreateRequest)
	 */
	@Override
	public void onDeviceAlertCreateRequest(String hardwareId, String originator,
			IDeviceAlertCreateRequest request) throws OpenIoTException {
		IDeviceAssignment assignment = getCurrentAssignment(hardwareId);
		IDeviceAlert alert =
				OpenIoT.getServer().getDeviceManagement().addDeviceAlert(assignment.getToken(), request);
		handleLinkResponseToInvocation(originator, alert.getId(), assignment);
	}

	/**
	 * Get the current assignment or throw errors if it can not be resolved.
	 * 
	 * @param hardwareId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected IDeviceAssignment getCurrentAssignment(String hardwareId) throws OpenIoTException {
		IDevice device = OpenIoT.getServer().getDeviceManagement().getDeviceByHardwareId(hardwareId);
		if (device == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidHardwareId, ErrorLevel.ERROR);
		}
		if (device.getAssignmentToken() == null) {
			throw new OpenIoTSystemException(ErrorCode.DeviceNotAssigned, ErrorLevel.ERROR);
		}
		return OpenIoT.getServer().getDeviceManagement().getDeviceAssignmentByToken(
				device.getAssignmentToken());
	}

	/**
	 * If an originator was assocaited with the event, create a
	 * {@link IDeviceCommandResponse} that links back to the original invocation.
	 * 
	 * @param originator
	 * @param eventId
	 * @param assignment
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void handleLinkResponseToInvocation(String originator, String eventId,
			IDeviceAssignment assignment) throws OpenIoTException {
		if ((originator != null) && (!originator.isEmpty())) {
			DeviceCommandResponseCreateRequest response = new DeviceCommandResponseCreateRequest();
			response.setOriginatingEventId(originator);
			response.setResponseEventId(eventId);
			OpenIoT.getServer().getDeviceManagement().addDeviceCommandResponse(assignment.getToken(),
					response);
		}
	}
}