/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.element;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.openiot.spi.device.element.IDeviceSlot;
import com.openiot.spi.device.element.IDeviceUnit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link IDeviceUnit}.
 * 
 * @author Derek
 */
@JsonInclude(Include.NON_NULL)
public class DeviceUnit extends DeviceElement implements IDeviceUnit, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = 5969717501161142392L;

	/** List of device slots */
	private List<DeviceSlot> deviceSlots = new ArrayList<DeviceSlot>();

	/** List of device units */
	private List<DeviceUnit> deviceUnits = new ArrayList<DeviceUnit>();

	public DeviceUnit() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceUnit#getDeviceSlots()
	 */
	@SuppressWarnings("unchecked")
	public List<IDeviceSlot> getDeviceSlots() {
		return (List<IDeviceSlot>) (List<? extends IDeviceSlot>) deviceSlots;
	}

	public void setDeviceSlots(List<DeviceSlot> deviceSlots) {
		this.deviceSlots = deviceSlots;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceUnit#getDeviceUnits()
	 */
	@SuppressWarnings("unchecked")
	public List<IDeviceUnit> getDeviceUnits() {
		return (List<IDeviceUnit>) (List<? extends IDeviceUnit>) deviceUnits;
	}

	public void setDeviceUnits(List<DeviceUnit> deviceUnits) {
		this.deviceUnits = deviceUnits;
	}
}