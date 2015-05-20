/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.provisioning.socket;

/**
 * Factory that produces {@link ISocketInteractionHandler} instances.
 * 
 * @author Derek
 *
 * @param <T>
 */
public interface ISocketInteractionHandlerFactory<T> {

	/**
	 * Creates a new {@link ISocketInteractionHandler} instance.
	 * 
	 * @return
	 */
	public ISocketInteractionHandler<T> newInstance();
}