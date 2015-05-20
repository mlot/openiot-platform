/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.system;

import com.openiot.spi.server.IOpenIoTServer;
import com.openiot.spi.system.IVersion;

/**
 * Model object used to read version information from REST call.
 * 
 * @author Derek
 */
public class Version implements IVersion {

	/** Full edition */
	private String edition;

	/** Edition identifier */
	private String editionIdentifier;

	/** Version identifier */
	private String versionIdentifier;

	/** Build timestamp */
	private String buildTimestamp;

	/** Server implementation class */
	private Class<? extends IOpenIoTServer> serverClass;

	public Version() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IVersion#getEdition()
	 */
	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IVersion#getEditionIdentifier()
	 */
	public String getEditionIdentifier() {
		return editionIdentifier;
	}

	public void setEditionIdentifier(String editionIdentifier) {
		this.editionIdentifier = editionIdentifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IVersion#getVersionIdentifier()
	 */
	public String getVersionIdentifier() {
		return versionIdentifier;
	}

	public void setVersionIdentifier(String versionIdentifier) {
		this.versionIdentifier = versionIdentifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IVersion#getBuildTimestamp()
	 */
	public String getBuildTimestamp() {
		return buildTimestamp;
	}

	public void setBuildTimestamp(String buildTimestamp) {
		this.buildTimestamp = buildTimestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IVersion#getServerClass()
	 */
	public Class<? extends IOpenIoTServer> getServerClass() {
		return serverClass;
	}

	public void setServerClass(Class<? extends IOpenIoTServer> serverClass) {
		this.serverClass = serverClass;
	}
}