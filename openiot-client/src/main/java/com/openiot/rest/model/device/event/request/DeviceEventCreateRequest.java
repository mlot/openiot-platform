/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.event.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.rest.model.datatype.JsonDateSerializer;
import com.openiot.rest.model.device.event.DeviceEvent;
import com.openiot.spi.device.event.request.IDeviceEventCreateRequest;

import java.io.Serializable;
import java.util.Date;

/**
 * Holds common fields for creating {@link DeviceEvent} subclasses.
 * 
 * @author Derek
 */
public class DeviceEventCreateRequest extends MetadataProvider implements IDeviceEventCreateRequest,
		Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = -8906177904822194407L;

	/** Date event occurred */
	private Date eventDate;

	/** Indicates whether device assignment state should be updated */
	private boolean updateState = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.request.IDeviceEventCreateRequest#getEventDate()
	 */
	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getEventDate() {
		return eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceEventCreateRequest#isUpdateState()
	 */
	public boolean isUpdateState() {
		return updateState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceEventCreateRequest#setUpdateState
	 * (boolean)
	 */
	public void setUpdateState(boolean updateState) {
		this.updateState = updateState;
	}
}