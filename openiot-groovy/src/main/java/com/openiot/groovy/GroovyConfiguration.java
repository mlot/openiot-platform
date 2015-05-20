/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.groovy;

import com.openiot.OpenIoT;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import groovy.util.GroovyScriptEngine;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.io.IOException;

/**
 * Global configuration for Groovy scripting support.
 * 
 * @author Derek
 */
public class GroovyConfiguration extends LifecycleComponent implements InitializingBean {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(GroovyConfiguration.class);

	/** Bean name where global Groovy configuration is expected */
	public static final String GROOVY_CONFIGURATION_BEAN = "swGroovyConfiguration";

	/** Path relative to configuration root where Groovy scripts are stored */
	private static final String GROOVY_REL_SCRIPT_PATH = "groovy";

	/** Groovy script engine */
	private GroovyScriptEngine groovyScriptEngine;

	/** Field for setting GSE verbose flag */
	private boolean verbose = false;

	/** Field for setting GSE debug flag */
	private boolean debug = false;

	public GroovyConfiguration() {
		super(LifecycleComponentType.Other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		OpenIoT.getServer().getRegisteredLifecycleComponents().add(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		File root = OpenIoT.getServer().getConfigurationResolver().getConfigurationRoot();
		File scriptPath = new File(root, GROOVY_REL_SCRIPT_PATH);
		if (!scriptPath.exists()) {
			throw new OpenIoTException("Groovy configured, but scripts path does not exist.");
		}
		try {
			groovyScriptEngine = new GroovyScriptEngine(scriptPath.getAbsolutePath());
			groovyScriptEngine.getConfig().setVerbose(isVerbose());
			groovyScriptEngine.getConfig().setDebug(isDebug());
			LOGGER.info("Groovy script engine configured with (verbose:" + isVerbose() + ") (debug:"
					+ isDebug() + ").");
		} catch (IOException e) {
			throw new OpenIoTException("Unable to configure Groovy script engine.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#getLogger()
	 */
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	public GroovyScriptEngine getGroovyScriptEngine() {
		return groovyScriptEngine;
	}

	public void setGroovyScriptEngine(GroovyScriptEngine groovyScriptEngine) {
		this.groovyScriptEngine = groovyScriptEngine;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}