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

/**
 * Extracts delivery parameters from
 * 
 * @author Derek
 * 
 * @param <T>
 */
public interface ICommandDeliveryParameterExtractor<T> {

	/**
	 * Extract required delivery parameters from the given sources.
	 * 
	 * @param nesting
	 * @param assignment
	 * @param execution
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public T extractDeliveryParameters(IDeviceNestingContext nesting, IDeviceAssignment assignment,
			IDeviceCommandExecution execution) throws OpenIoTException;
}