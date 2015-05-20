package com.openiot;

import com.openiot.server.OpenIoTServer;
import com.openiot.spi.server.IOpenIoTServer;
import com.openiot.spi.system.IVersion;

/**
 * Used as basis for generating version information. This file is modified by the Maven
 * build process so that the correct values exist in the compiled classes.
 * 
 * @author Derek
 */
public class Version implements IVersion {

	/** Version identifier supplied by the Maven POM */
	public static final String VERSION_IDENTIFIER = "@version.identifier@";

	/** Timestamp for build */
	public static final String BUILD_TIMESTAMP = "@build.timestamp@";

	/*
	 * (non-Javadoc)
	 * 
	 * @see IVersion#getEdition()
	 */
	public String getEdition() {
		return "Community Edition";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IVersion#getEditionIdentifier()
	 */
	public String getEditionIdentifier() {
		return "CE";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IVersion#getVersionIdentifier()
	 */
	public String getVersionIdentifier() {
		return VERSION_IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IVersion#getBuildTimestamp()
	 */
	public String getBuildTimestamp() {
		return BUILD_TIMESTAMP;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IVersion#getServerClass()
	 */
	public Class<? extends IOpenIoTServer> getServerClass() {
		return OpenIoTServer.class;
	}
}