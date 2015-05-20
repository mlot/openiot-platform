/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.provisioning;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.event.IDeviceCommandInvocation;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;

import java.util.List;

/**
 * Allows an {@link IDeviceCommandInvocation} to be resolved to one or more
 * {@link com.openiot.spi.device.IDeviceAssignment} records that should receive the command.
 * 
 * @author Derek
 */
public interface ICommandTargetResolver extends ILifecycleComponent {

	/**
	 * Resolves a command invocation to a list of assignments that should receive the
	 * command.
	 * 
	 * @param invocation
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<IDeviceAssignment> resolveTargets(IDeviceCommandInvocation invocation)
			throws OpenIoTException;
}