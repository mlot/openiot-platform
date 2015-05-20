/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.web;

import com.openiot.OpenIoT;
import com.openiot.spi.OpenIoTException;
import org.apache.log4j.Logger;
import org.mule.util.StringMessageUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.ArrayList;
import java.util.List;

/**
 * Initializes the OpenIoT server.
 * 
 * @author Derek
 */
public class OpenIoTServerLoader extends HttpServlet {

	/** Serial version UUID */
	private static final long serialVersionUID = -8696135593175193509L;

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(OpenIoTServerLoader.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		super.init();
		try {
			OpenIoT.start();
			LOGGER.info("Server started successfully.");
			OpenIoT.getServer().logState();
		} catch (OpenIoTException e) {
			List<String> messages = new ArrayList<String>();
			messages.add("!!!! OpenIoT Server Failed to Start !!!!");
			messages.add("");
			messages.add("Error: " + e.getMessage());
			String message = StringMessageUtils.getBoilerPlate(messages, '*', 60);
			LOGGER.info("\n" + message + "\n");
		} catch (Throwable e) {
			List<String> messages = new ArrayList<String>();
			messages.add("!!!! Unhandled Exception !!!!");
			messages.add("");
			messages.add("Error: " + e.getMessage());
			String message = StringMessageUtils.getBoilerPlate(messages, '*', 60);
			LOGGER.info("\n" + message + "\n");
		}
	}
}