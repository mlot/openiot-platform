/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.configuration;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.configuration.IConfigurationResolver;
import com.openiot.spi.system.IVersion;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.InputStreamResource;

import java.io.File;
import java.net.URL;

/**
 * Allows OpenIoT configuration to be loaded from an external URL.
 * 
 * @author Derek
 */
public class ExternalConfigurationResolver implements IConfigurationResolver {

	/** Static logger instance */
	public static Logger LOGGER = Logger.getLogger(ExternalConfigurationResolver.class);

	/** URL used for loading configuration */
	private String remoteConfigUrl;

	public ExternalConfigurationResolver(String url) {
		this.remoteConfigUrl = url;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IConfigurationResolver#resolveOpenIoTContext(
	 * IVersion)
	 */
	@Override
	public ApplicationContext resolveOpenIoTContext(IVersion version) throws OpenIoTException {
		LOGGER.info("Loading configuration from external source: " + getRemoteConfigUrl());
		try {
			GenericApplicationContext context = new GenericApplicationContext();
			XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
			reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);

			URL remote = new URL(getRemoteConfigUrl());
			reader.loadBeanDefinitions(new InputStreamResource(remote.openStream()));

			context.refresh();
			return context;
		} catch (Exception e) {
			throw new OpenIoTException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IConfigurationResolver#getConfigurationRoot()
	 */
	@Override
	public File getConfigurationRoot() throws OpenIoTException {
		return TomcatConfigurationResolver.getOpenIoTConfigFolder();
	}

	public String getRemoteConfigUrl() {
		return remoteConfigUrl;
	}

	public void setRemoteConfigUrl(String remoteConfigUrl) {
		this.remoteConfigUrl = remoteConfigUrl;
	}
}