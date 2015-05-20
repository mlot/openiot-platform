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
 * Delegate that executes processing logic with access to OpenIoT and Mule internals.
 * 
 * @author dadams
 */
public interface IOpenIoTDelegate {

	/**
	 * Process information using OpenIoT and Mule information. If response is non-null, it will be returned
	 * as the new payload. Otherwise, the current OpenIoT context will be returned.
	 * 
	 * @param context
	 * @param client
	 * @param event
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public Object process(IOpenIoTContext context, IOpenIoTClient client, MuleEvent event)
			throws OpenIoTException;
}