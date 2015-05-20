/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.server.debug;

import com.openiot.spi.server.debug.ITracer;
import com.openiot.spi.server.debug.TracerCategory;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link ITracer} that does nothing with the data.
 * 
 * @author Derek
 */
public class NullTracer implements ITracer {

	/** Enablement indicator */
	private boolean enabled = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ITracer#start(com.openiot.spi.server.debug.
	 * TracerCategory, java.lang.String, org.apache.log4j.Logger)
	 */
	@Override
	public void start(TracerCategory category, String message, Logger logger) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ITracer#stop(org.apache.log4j.Logger)
	 */
	@Override
	public void stop(Logger logger) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ITracer#isEnabled()
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ITracer#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ITracer#asHtml()
	 */
	@Override
	public String asHtml() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ITracer#push(com.openiot.spi.server.debug.
	 * TracerCategory, java.lang.String, org.apache.log4j.Logger)
	 */
	@Override
	public void push(TracerCategory category, String message, Logger logger) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ITracer#pop(org.apache.log4j.Logger)
	 */
	@Override
	public void pop(Logger logger) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ITracer#debug(java.lang.String,
	 * org.apache.log4j.Logger)
	 */
	@Override
	public void debug(String message, Logger logger) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ITracer#info(java.lang.String,
	 * org.apache.log4j.Logger)
	 */
	@Override
	public void info(String message, Logger logger) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ITracer#warn(java.lang.String,
	 * java.lang.Throwable, org.apache.log4j.Logger)
	 */
	@Override
	public void warn(String message, Throwable error, Logger logger) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ITracer#error(java.lang.String,
	 * java.lang.Throwable, org.apache.log4j.Logger)
	 */
	@Override
	public void error(String message, Throwable error, Logger logger) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ITracer#timing(java.lang.String, long,
	 * java.util.concurrent.TimeUnit, org.apache.log4j.Logger)
	 */
	@Override
	public void timing(String message, long delta, TimeUnit unit, Logger logger) {
	}
}