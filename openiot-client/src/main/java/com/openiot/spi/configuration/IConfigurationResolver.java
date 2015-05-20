/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.configuration;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.system.IVersion;
import org.springframework.context.ApplicationContext;

import java.io.File;

/**
 * Allows for pluggable implementations that can resolve the Spring configuration for the
 * system.
 * 
 * @author Derek
 */
public interface IConfigurationResolver {

	/**
	 * Resolves the OpenIoT Spring configuration context.
	 * 
	 * @param version
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ApplicationContext resolveOpenIoTContext(IVersion version) throws OpenIoTException;

	/**
	 * Gets the root {@link File} where OpenIoT configuration files are stored.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public File getConfigurationRoot() throws OpenIoTException;
}