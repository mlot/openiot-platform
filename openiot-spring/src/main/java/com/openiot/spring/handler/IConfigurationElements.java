/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spring.handler;

/**
 * Constants related to Spring configuration elements.
 * 
 * @author Derek
 */
public interface IConfigurationElements {

	/** OpenIoT community edition namespace */
	public static final String OPENIOT_COMMUNITY_NS = "http://www.openiotplatform.com/schema/openiot/ce";

	/** OpenIoT enterprise edition namespace */
	public static final String OPENIOT_ENTERPRISE_NS = "http://www.openiotplatform.com/schema/openiot/ee";

	/** Constant for top-level configuration element */
	public static final String CONFIGURATION = "configuration";
}