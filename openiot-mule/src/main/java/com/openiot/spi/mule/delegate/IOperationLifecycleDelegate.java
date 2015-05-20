/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.mule.delegate;

import com.openiot.spi.IOpenIoTClient;
import com.openiot.spi.IOpenIoTContext;
import com.openiot.spi.OpenIoTException;
import org.mule.api.MuleEvent;

/**
 * Delegate that executes before and after an operation is executed.
 * 
 * @author Derek Adams
 */
public interface IOperationLifecycleDelegate {

	/**
	 * Called before an ESB operation is executed.
	 * 
	 * @param context
	 * @param client
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void beforeOperation(IOpenIoTContext context, IOpenIoTClient client, MuleEvent event)
			throws OpenIoTException;

	/**
	 * Called before an ESB operation is executed.
	 * 
	 * @param context
	 * @param client
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void afterOperation(IOpenIoTContext context, IOpenIoTClient client, MuleEvent event)
			throws OpenIoTException;
}