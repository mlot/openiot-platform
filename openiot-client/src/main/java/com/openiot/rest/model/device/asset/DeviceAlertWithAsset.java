/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.asset;

import com.openiot.rest.model.device.event.DeviceAlert;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.asset.IAssetModuleManager;
import com.openiot.spi.device.event.AlertLevel;
import com.openiot.spi.device.event.AlertSource;
import com.openiot.spi.device.event.IDeviceAlert;

/**
 * Wraps a {@link DeviceAlert} so that information about the asset associated with its
 * assignment is available.
 * 
 * @author Derek
 */
public class DeviceAlertWithAsset extends DeviceEventWithAsset implements IDeviceAlert {

	public DeviceAlertWithAsset(IDeviceAlert wrapped, IAssetModuleManager assets) throws OpenIoTException {
		super(wrapped, assets);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IDeviceAlert#getSource()
	 */
	@Override
	public AlertSource getSource() {
		return ((IDeviceAlert) getWrapped()).getSource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IDeviceAlert#getLevel()
	 */
	@Override
	public AlertLevel getLevel() {
		return ((IDeviceAlert) getWrapped()).getLevel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IDeviceAlert#getType()
	 */
	@Override
	public String getType() {
		return ((IDeviceAlert) getWrapped()).getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IDeviceAlert#getMessage()
	 */
	@Override
	public String getMessage() {
		return ((IDeviceAlert) getWrapped()).getMessage();
	}
}