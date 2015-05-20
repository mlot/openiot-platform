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
import com.openiot.spi.device.IDeviceNestingContext;
import com.openiot.spi.device.command.IDeviceCommandExecution;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;

/**
 * Handles delivery of encoded command information on an underlying transport.
 * 
 * @author Derek
 * 
 * @param <T>
 *            type of data that was encoded by the {@link ICommandExecutionEncoder}/
 * @param <P>
 *            parameters specific to the delivery provider
 */
public interface ICommandDeliveryProvider<T, P> extends ILifecycleComponent {

	/**
	 * Deliver the given encoded invocation. The device, assignment and invocation details
	 * are included since they may contain metadata important to the delivery mechanism.
	 * 
	 * @param nested
	 * @param assignment
	 * @param execution
	 * @param encoded
	 * @param parameters
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void deliver(IDeviceNestingContext nested, IDeviceAssignment assignment,
			IDeviceCommandExecution execution, T encoded, P parameters) throws OpenIoTException;

	/**
	 * Delivers a system command.
	 * 
	 * @param nested
	 * @param assignment
	 * @param encoded
	 * @param parameters
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void deliverSystemCommand(IDeviceNestingContext nested, IDeviceAssignment assignment, T encoded,
			P parameters) throws OpenIoTException;
}