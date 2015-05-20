/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.web.swagger;

import com.mangofactory.swagger.paths.RelativeSwaggerPathProvider;

import javax.servlet.ServletContext;

/**
 * Determines path used to reference OpenIoT APIs when creating Swagger data.
 * 
 * @author Derek
 */
public class OpenIoTPathProvider extends RelativeSwaggerPathProvider {

	public OpenIoTPathProvider(ServletContext servletContext) {
		super(servletContext);
	}

	@Override
	protected String applicationPath() {
		return super.applicationPath() + "/api";
	}
}