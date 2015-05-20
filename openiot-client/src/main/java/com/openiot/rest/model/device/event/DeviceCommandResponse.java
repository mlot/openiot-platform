/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.DeviceEventType;
import com.openiot.spi.device.event.IDeviceCommandResponse;

import java.io.Serializable;

/**
 * Implementation of {@link IDeviceCommandResponse}.
 * 
 * @author Derek
 */
@JsonIgnoreProperties
@JsonInclude(Include.NON_NULL)
public class DeviceCommandResponse extends DeviceEvent implements IDeviceCommandResponse, Serializable {

	/** For Java serialization */
	private static final long serialVersionUID = 4448837178695704510L;

	/** Event id that generated response */
	private String originatingEventId;

	/** Event sent in response */
	private String responseEventId;

	/** Data sent for response */
	private String response;

	public DeviceCommandResponse() {
		super(DeviceEventType.CommandResponse);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandResponse#getOriginatingEventId()
	 */
	public String getOriginatingEventId() {
		return originatingEventId;
	}

	public void setOriginatingEventId(String originatingEventId) {
		this.originatingEventId = originatingEventId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandResponse#getResponseEventId()
	 */
	public String getResponseEventId() {
		return responseEventId;
	}

	public void setResponseEventId(String responseEventId) {
		this.responseEventId = responseEventId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandResponse#getResponse()
	 */
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	/**
	 * Create a copy of an SPI object. Used by web services for marshaling.
	 * 
	 * @param input
	 * @return
	 */
	public static DeviceCommandResponse copy(IDeviceCommandResponse input) throws OpenIoTException {
		DeviceCommandResponse result = new DeviceCommandResponse();
		copy(input, result);
		result.setOriginatingEventId(input.getOriginatingEventId());
		result.setResponseEventId(input.getResponseEventId());
		result.setResponse(input.getResponse());
		return result;
	}
}