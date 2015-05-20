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
import com.openiot.spi.device.event.IDeviceCommandResponse;

/**
 * Wraps a {@link com.openiot.rest.model.device.event.DeviceCommandResponse} so that information about the asset associated
 * with its assignment is available.
 * 
 * @author Derek
 */
public class DeviceCommandResponseWithAsset extends DeviceEventWithAsset implements IDeviceCommandResponse {

	public DeviceCommandResponseWithAsset(IDeviceCommandResponse wrapped, IAssetModuleManager assets)
			throws OpenIoTException {
		super(wrapped, assets);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandResponse#getOriginatingEventId()
	 */
	@Override
	public String getOriginatingEventId() {
		return ((IDeviceCommandResponse) getWrapped()).getOriginatingEventId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandResponse#getResponseEventId()
	 */
	@Override
	public String getResponseEventId() {
		return ((IDeviceCommandResponse) getWrapped()).getResponseEventId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandResponse#getResponse()
	 */
	@Override
	public String getResponse() {
		return ((IDeviceCommandResponse) getWrapped()).getResponse();
	}
}