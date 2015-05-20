/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi;

import java.io.IOException;

/**
 * Base class for OpenIoT exceptions.
 * 
 * @author Derek Adams
 */
public class OpenIoTException extends IOException {

	/** Serial version UID */
	private static final long serialVersionUID = 1L;

	public OpenIoTException() {
		super();
	}

	public OpenIoTException(String message, Throwable cause) {
		super(message, cause);
	}

	public OpenIoTException(String message) {
		super(message);
	}

	public OpenIoTException(Throwable cause) {
		super(cause);
	}
}