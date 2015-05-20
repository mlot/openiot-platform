/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.asset;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.asset.IAssetModuleManager;
import com.openiot.spi.device.event.IDeviceStateChange;
import com.openiot.spi.device.event.state.StateChangeCategory;
import com.openiot.spi.device.event.state.StateChangeType;

import java.util.Map;

/**
 * Wraps a {@link IDeviceStateChange} so that information about the asset associated with
 * its assignment is available.
 * 
 * @author Derek
 */
public class DeviceStateChangeWithAsset extends DeviceEventWithAsset implements IDeviceStateChange {

	public DeviceStateChangeWithAsset(IDeviceStateChange wrapped, IAssetModuleManager assets)
			throws OpenIoTException {
		super(wrapped, assets);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceStateChange#getCategory()
	 */
	@Override
	public StateChangeCategory getCategory() {
		return ((IDeviceStateChange) getWrapped()).getCategory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceStateChange#getType()
	 */
	@Override
	public StateChangeType getType() {
		return ((IDeviceStateChange) getWrapped()).getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceStateChange#getPreviousState()
	 */
	@Override
	public String getPreviousState() {
		return ((IDeviceStateChange) getWrapped()).getPreviousState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceStateChange#getNewState()
	 */
	@Override
	public String getNewState() {
		return ((IDeviceStateChange) getWrapped()).getNewState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceStateChange#getData()
	 */
	@Override
	public Map<String, String> getData() {
		return ((IDeviceStateChange) getWrapped()).getData();
	}
}