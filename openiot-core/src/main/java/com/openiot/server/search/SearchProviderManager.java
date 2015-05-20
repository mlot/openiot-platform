/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.server.search;

import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.search.external.ISearchProvider;
import com.openiot.spi.search.external.ISearchProviderManager;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages a list of {@link ISearchProvider} that are available for querying device events
 * 
 * @author Derek
 */
public class SearchProviderManager extends LifecycleComponent implements ISearchProviderManager {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(SearchProviderManager.class);

	/** List of available search providers */
	private List<ISearchProvider> searchProviders;

	/** Map of search providers by id */
	private Map<String, ISearchProvider> providersById = new HashMap<String, ISearchProvider>();

	public SearchProviderManager() {
		super(LifecycleComponentType.SearchProviderManager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		for (ISearchProvider provider : getSearchProviders()) {
			provider.lifecycleStart();
			providersById.put(provider.getId(), provider);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#getLogger()
	 */
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
		for (ISearchProvider provider : getSearchProviders()) {
			provider.lifecycleStop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ISearchProviderManager#getSearchProviders()
	 */
	public List<ISearchProvider> getSearchProviders() {
		return searchProviders;
	}

	public void setSearchProviders(List<ISearchProvider> searchProviders) {
		this.searchProviders = searchProviders;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ISearchProviderManager#getSearchProvider(java
	 * .lang.String)
	 */
	@Override
	public ISearchProvider getSearchProvider(String id) {
		return providersById.get(id);
	}
}