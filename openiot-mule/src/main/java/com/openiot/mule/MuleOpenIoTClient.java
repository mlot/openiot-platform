/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.mule;

import com.openiot.mule.spring.MappingJackson2HttpMessageConverter;
import com.openiot.rest.client.OpenIoTClient;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.List;

/**
 * Customized version of {@link com.openiot.rest.client.OpenIoTClient} that hacks around classpath issues when
 * running inside Mule.
 * 
 * @author Derek
 */
public class MuleOpenIoTClient extends OpenIoTClient {

	public MuleOpenIoTClient(String url, String username, String password) {
		super(url, username, password);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.rest.service.OpenIoTClient#addMessageConverters(java.util.List)
	 */
	protected void addMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new MappingJackson2HttpMessageConverter());
	}
}