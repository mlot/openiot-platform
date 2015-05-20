/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.event.request;

import com.openiot.spi.device.event.IDeviceCommandResponse;
import com.openiot.spi.device.event.request.IDeviceCommandResponseCreateRequest;

import java.io.Serializable;

/**
 * Model object used to create a new {@link IDeviceCommandResponse} via REST APIs.
 * 
 * @author Derek
 */
public class DeviceCommandResponseCreateRequest extends DeviceEventCreateRequest implements
		IDeviceCommandResponseCreateRequest, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = -9170930846188888841L;

	/** Event id that generated response */
	private String originatingEventId;

	/** Event sent in response */
	private String responseEventId;

	/** Data sent for response */
	private String response;

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandResponseCreateRequest#
	 * getOriginatingEventId()
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
	 * @see IDeviceCommandResponseCreateRequest#
	 * getResponseEventId()
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
	 * @see
	 * IDeviceCommandResponseCreateRequest#getResponse
	 * ()
	 */
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
}