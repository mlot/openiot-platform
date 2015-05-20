/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.command;

import com.openiot.spi.common.IMetadataProviderEntity;

import java.util.List;

/**
 * A parameterized command issued to a device.
 * 
 * @author Derek
 */
public interface IDeviceCommand extends IMetadataProviderEntity {

	/**
	 * Get the unique command token.
	 * 
	 * @return
	 */
	public String getToken();

	/**
	 * Get token for the parent specification.
	 * 
	 * @return
	 */
	public String getSpecificationToken();

	/**
	 * Optional namespace for distinguishing commands.
	 * 
	 * @return
	 */
	public String getNamespace();

	/**
	 * Get command name.
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Get a description of the command.
	 * 
	 * @return
	 */
	public String getDescription();

	/**
	 * Get list of parameters.
	 * 
	 * @return
	 */
	public List<ICommandParameter> getParameters();
}