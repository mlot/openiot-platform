/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.mongodb;

import com.openiot.spi.OpenIoTException;

/**
 * Extends the default MongoDB client to use host and port supplied by environment
 * variables set by Docker.
 * 
 * @author Derek
 */
public class DockerMongoClient extends OpenIoTMongoClient {

	/*
	 * (non-Javadoc)
	 * 
	 * @see OpenIoTMongoClient#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		String host = System.getenv("MONGO_PORT_27017_TCP_ADDR");
		String portStr = System.getenv("MONGO_PORT_27017_TCP_PORT");
		if ((host == null) || (portStr == null)) {
			throw new OpenIoTException("Docker linking information not found for MongoDB. "
					+ "Verify that link arguments were passed.");
		}
		setHostname(host);
		setPort(Integer.parseInt(portStr));
	}
}