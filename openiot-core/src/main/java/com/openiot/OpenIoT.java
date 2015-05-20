/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.server.IOpenIoTServer;
import com.openiot.spi.server.lifecycle.LifecycleStatus;
import com.openiot.version.VersionHelper;

/**
 * Main class for accessing core OpenIoT functionality.
 * 
 * @author Derek
 */
public class OpenIoT {

	/** Singleton server instance */
	private static IOpenIoTServer SERVER;

	/**
	 * Called once to bootstrap the OpenIoT server.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static void start() throws OpenIoTException {
		Class<? extends IOpenIoTServer> clazz = VersionHelper.getVersion().getServerClass();
		try {
			SERVER = clazz.newInstance();
			SERVER.initialize();
			SERVER.lifecycleStart();

			// Handle errors that prevent server startup.
			if (SERVER.getLifecycleStatus() == LifecycleStatus.Error) {
				throw SERVER.getLifecycleError();
			}
		} catch (InstantiationException e) {
			throw new OpenIoTException("Unable to create OpenIoT server instance.", e);
		} catch (IllegalAccessException e) {
			throw new OpenIoTException("Unable to access OpenIoT server class.", e);
		}
	}

	/**
	 * Called to shut down the OpenIoT server.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static void stop() throws OpenIoTException {
		getServer().lifecycleStop();
	}

	/**
	 * Get the singleton OpenIoT server instance.
	 * 
	 * @return
	 */
	public static IOpenIoTServer getServer() {
		if (SERVER == null) {
			throw new RuntimeException("OpenIoT server has not been initialized.");
		}
		return SERVER;
	}
}