/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.openiot.rest.spring;

/**
 * Hack to use converter on local classpath since Jackson classes do not want to resolve
 * otherwise.
 * 
 * @author Derek
 */
public class MappingJackson2HttpMessageConverter extends
		org.springframework.http.converter.json.MappingJackson2HttpMessageConverter {
}