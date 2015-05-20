/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest;

/**
 * Interface for constants used in web operations.
 * 
 * @author dadams
 */
public interface IOpenIoTWebConstants {

	/** Header that holds sitewhere error string on error response */
	public static final String HEADER_OPENIOT_ERROR = "X-OpenIoT-Error";

	/** Header that holds sitewhere error code on error response */
	public static final String HEADER_OPENIOT_ERROR_CODE = "X-OpenIoT-Error-Code";
}