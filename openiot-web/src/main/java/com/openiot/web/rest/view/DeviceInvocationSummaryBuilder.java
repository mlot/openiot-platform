/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.web.rest.view;

import com.openiot.OpenIoT;
import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.rest.model.device.event.DeviceCommandInvocation;
import com.openiot.rest.model.device.event.view.DeviceCommandInvocationSummary;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.command.ICommandParameter;
import com.openiot.spi.device.event.*;

import java.util.List;

/**
 * Used to build a {@link DeviceCommandInvocationSummary}.
 * 
 * @author Derek
 */
public class DeviceInvocationSummaryBuilder {

	/**
	 * Creates a {@link DeviceCommandInvocationSummary} using data from a
	 * {@link DeviceCommandInvocation} that has its command information populated.
	 * 
	 * @param invocation
	 * @param responses
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DeviceCommandInvocationSummary build(DeviceCommandInvocation invocation,
			List<IDeviceCommandResponse> responses) throws OpenIoTException {
		DeviceCommandInvocationSummary summary = new DeviceCommandInvocationSummary();
		summary.setName(invocation.getCommand().getName());
		summary.setNamespace(invocation.getCommand().getNamespace());
		summary.setInvocationDate(invocation.getEventDate());
		for (ICommandParameter parameter : invocation.getCommand().getParameters()) {
			DeviceCommandInvocationSummary.Parameter param = new DeviceCommandInvocationSummary.Parameter();
			param.setName(parameter.getName());
			param.setType(parameter.getType().name());
			param.setRequired(parameter.isRequired());
			param.setValue(invocation.getParameterValues().get(parameter.getName()));
			summary.getParameters().add(param);
		}
		for (IDeviceCommandResponse response : responses) {
			DeviceCommandInvocationSummary.Response rsp = new DeviceCommandInvocationSummary.Response();
			rsp.setDate(response.getEventDate());
			if (response.getResponseEventId() != null) {
				IDeviceEvent event =
						OpenIoT.getServer().getDeviceManagement().getDeviceEventById(
								response.getResponseEventId());
				rsp.setDescription(getDeviceEventDescription(event));
			} else if (response.getResponse() != null) {
				rsp.setDescription("Ack (\"" + response.getResponse() + "\")");
			} else {
				rsp.setDescription("Response received.");
			}
			summary.getResponses().add(rsp);
		}
		MetadataProvider.copy(invocation, summary);
		return summary;
	}

	/**
	 * Get a short description of a device event.
	 * 
	 * @param event
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static String getDeviceEventDescription(IDeviceEvent event) throws OpenIoTException {
		if (event instanceof IDeviceMeasurements) {
			IDeviceMeasurements m = (IDeviceMeasurements) event;
			return "Measurements (" + m.getMeasurementsSummary() + ")";
		} else if (event instanceof IDeviceLocation) {
			IDeviceLocation l = (IDeviceLocation) event;
			return "Location (" + l.getLatitude() + "/" + l.getLongitude() + "/" + l.getElevation() + ")";
		} else if (event instanceof IDeviceAlert) {
			IDeviceAlert a = (IDeviceAlert) event;
			return "Alert (\"" + a.getMessage() + "\")";
		}
		return "Unknown Event Type";
	}
}