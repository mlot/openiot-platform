/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.request;

import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.rest.model.device.command.CommandParameter;
import com.openiot.spi.device.command.ICommandParameter;
import com.openiot.spi.device.request.IDeviceCommandCreateRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Arguments needed to create a new device command.
 * 
 * @author Derek
 */
public class DeviceCommandCreateRequest extends MetadataProvider implements IDeviceCommandCreateRequest,
		Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = 7791276552702413783L;

	/** Unqiue token */
	private String token;

	/** Optional namespace */
	private String namespace;

	/** Command name */
	private String name;

	/** Command description */
	private String description;

	/** Command parameters */
	private List<CommandParameter> parameters = new ArrayList<CommandParameter>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandCreateRequest#getToken()
	 */
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandCreateRequest#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandCreateRequest#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandCreateRequest#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceCommandCreateRequest#getParameters()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ICommandParameter> getParameters() {
		return (List<ICommandParameter>) (List<? extends ICommandParameter>) parameters;
	}

	public void setParameters(List<CommandParameter> parameters) {
		this.parameters = parameters;
	}
}