/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.search.external;

import com.openiot.spi.server.lifecycle.ILifecycleComponent;

import java.util.List;

/**
 * Manages a list of search providers that can be used by OpenIoT.
 * 
 * @author Derek
 */
public interface ISearchProviderManager extends ILifecycleComponent {

	/**
	 * Get list of available search providers.
	 * 
	 * @return
	 */
	public List<ISearchProvider> getSearchProviders();

	/**
	 * Get search provider with the given unique id.
	 * 
	 * @param id
	 * @return
	 */
	public ISearchProvider getSearchProvider(String id);
}