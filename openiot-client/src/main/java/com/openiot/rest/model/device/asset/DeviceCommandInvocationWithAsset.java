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
import com.openiot.spi.device.event.CommandInitiator;
import com.openiot.spi.device.event.CommandStatus;
import com.openiot.spi.device.event.CommandTarget;
import com.openiot.spi.device.event.IDeviceCommandInvocation;

import java.util.Map;

public class DeviceCommandInvocationWithAsset extends DeviceEventWithAsset implements
		IDeviceCommandInvocation {

	public DeviceCommandInvocationWithAsset(IDeviceCommandInvocation wrapped, IAssetModuleManager assets)
			throws OpenIoTException {
		super(wrapped, assets);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandInvocation#getInitiator()
	 */
	@Override
	public CommandInitiator getInitiator() {
		return ((IDeviceCommandInvocation) getWrapped()).getInitiator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandInvocation#getInitiatorId()
	 */
	@Override
	public String getInitiatorId() {
		return ((IDeviceCommandInvocation) getWrapped()).getInitiatorId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandInvocation#getTarget()
	 */
	@Override
	public CommandTarget getTarget() {
		return ((IDeviceCommandInvocation) getWrapped()).getTarget();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandInvocation#getTargetId()
	 */
	@Override
	public String getTargetId() {
		return ((IDeviceCommandInvocation) getWrapped()).getTargetId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandInvocation#getCommandToken()
	 */
	@Override
	public String getCommandToken() {
		return ((IDeviceCommandInvocation) getWrapped()).getCommandToken();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandInvocation#getParameterValues()
	 */
	@Override
	public Map<String, String> getParameterValues() {
		return ((IDeviceCommandInvocation) getWrapped()).getParameterValues();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandInvocation#getStatus()
	 */
	@Override
	public CommandStatus getStatus() {
		return ((IDeviceCommandInvocation) getWrapped()).getStatus();
	}
}