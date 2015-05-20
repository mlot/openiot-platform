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
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolves OpenIoT configuration relative to the Tomcat installation base directory.
 * 
 * @author Derek
 */
public class TomcatConfigurationResolver implements IConfigurationResolver {

	/** Static logger instance */
	public static Logger LOGGER = Logger.getLogger(TomcatConfigurationResolver.class);

	/** File name for OpenIoT server config file */
	public static final String SERVER_CONFIG_FILE_NAME = "openiot-server.xml";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IConfigurationResolver#resolveOpenIoTContext(
	 * IVersion)
	 */
	@Override
	public ApplicationContext resolveOpenIoTContext(IVersion version) throws OpenIoTException {
		LOGGER.info("Loading Spring configuration ...");
		File sitewhereConf = getOpenIoTConfigFolder();
		File serverConfigFile = new File(sitewhereConf, SERVER_CONFIG_FILE_NAME);
		if (!serverConfigFile.exists()) {
			throw new OpenIoTException("OpenIoT server configuration not found: "
					+ serverConfigFile.getAbsolutePath());
		}
		GenericApplicationContext context = new GenericApplicationContext();

		// Plug in custom property source.
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("openiot.edition", version.getEditionIdentifier().toLowerCase());

		MapPropertySource source = new MapPropertySource("openiot", properties);
		context.getEnvironment().getPropertySources().addLast(source);

		// Read context from XML configuration file.
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
		reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
		reader.loadBeanDefinitions(new FileSystemResource(serverConfigFile));

		context.refresh();
		return context;
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

	/**
	 * Gets the CATALINA/conf/sitewhere folder where configs are stored.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static File getOpenIoTConfigFolder() throws OpenIoTException {
		String catalina = System.getProperty("catalina.base");
		if (catalina == null) {
			throw new OpenIoTException("CATALINA_HOME not set.");
		}
		File catFolder = new File(catalina);
		if (!catFolder.exists()) {
			throw new OpenIoTException("CATALINA_HOME folder does not exist.");
		}
		File confDir = new File(catalina, "conf");
		if (!confDir.exists()) {
			throw new OpenIoTException("CATALINA_HOME conf folder does not exist.");
		}
		File sitewhereDir = new File(confDir, "openiot");
		if (!confDir.exists()) {
			throw new OpenIoTException("CATALINA_HOME conf/openiot folder does not exist.");
		}
		return sitewhereDir;
	}

	/**
	 * Gets the CATALINA/data folder where data is stored.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static File getOpenIoTDataFolder() throws OpenIoTException {
		String catalina = System.getProperty("catalina.base");
		if (catalina == null) {
			throw new OpenIoTException("CATALINA_HOME not set.");
		}
		File catFolder = new File(catalina);
		if (!catFolder.exists()) {
			throw new OpenIoTException("CATALINA_HOME folder does not exist.");
		}
		File dataDir = new File(catalina, "data");
		if (!dataDir.exists()) {
			dataDir.mkdir();
		}
		return dataDir;
	}
}