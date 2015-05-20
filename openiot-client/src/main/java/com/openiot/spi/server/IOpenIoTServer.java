/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.server;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.asset.IAssetModuleManager;
import com.openiot.spi.configuration.IConfigurationResolver;
import com.openiot.spi.device.IDeviceManagement;
import com.openiot.spi.device.IDeviceManagementCacheProvider;
import com.openiot.spi.device.event.processor.IInboundEventProcessorChain;
import com.openiot.spi.device.event.processor.IOutboundEventProcessorChain;
import com.openiot.spi.device.provisioning.IDeviceProvisioning;
import com.openiot.spi.search.external.ISearchProviderManager;
import com.openiot.spi.server.debug.ITracer;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;
import com.openiot.spi.system.IVersion;
import com.openiot.spi.user.IUserManagement;

import java.util.List;

/**
 * Interface for interacting with core OpenIoT server functionality.
 * 
 * @author Derek
 */
public interface IOpenIoTServer extends ILifecycleComponent {

	/**
	 * Get version information.
	 * 
	 * @return
	 */
	public IVersion getVersion();

	/**
	 * Initialize the server.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void initialize() throws OpenIoTException;

	/**
	 * Returns exception if one was thrown on startup.
	 * 
	 * @return
	 */
	public Throwable getServerStartupError();

	/**
	 * Get tracer for debug operations.
	 * 
	 * @return
	 */
	public ITracer getTracer();

	/**
	 * Get class that can be used to location the Spring configuration context.
	 * 
	 * @return
	 */
	public IConfigurationResolver getConfigurationResolver();

	/**
	 * Get the user management implementation.
	 * 
	 * @return
	 */
	public IUserManagement getUserManagement();

	/**
	 * Get the device management implementation.
	 * 
	 * @return
	 */
	public IDeviceManagement getDeviceManagement();

	/**
	 * Get the configured device management cache provider implementation.
	 * 
	 * @return
	 */
	public IDeviceManagementCacheProvider getDeviceManagementCacheProvider();

	/**
	 * Get the inbound event processor chain.
	 * 
	 * @return
	 */
	public IInboundEventProcessorChain getInboundEventProcessorChain();

	/**
	 * Get the outbound event processor chain.
	 * 
	 * @return
	 */
	public IOutboundEventProcessorChain getOutboundEventProcessorChain();

	/**
	 * Get the device provisioning implementation.
	 * 
	 * @return
	 */
	public IDeviceProvisioning getDeviceProvisioning();

	/**
	 * Get the asset modules manager instance.
	 * 
	 * @return
	 */
	public IAssetModuleManager getAssetModuleManager();

	/**
	 * Get the search provider manager implementation.
	 * 
	 * @return
	 */
	public ISearchProviderManager getSearchProviderManager();

	/**
	 * Get list of components that have registered to participate in the server component
	 * lifecycle.
	 * 
	 * @return
	 */
	public List<ILifecycleComponent> getRegisteredLifecycleComponents();

	/**
	 * Gets an {@link ILifecycleComponent} by unique id.
	 * 
	 * @param id
	 * @return
	 */
	public ILifecycleComponent getLifecycleComponentById(String id);

	/**
	 * Get the metrics registry.
	 * 
	 * @return
	 */
	public MetricRegistry getMetricRegistry();

	/**
	 * Get the health check registry.
	 * 
	 * @return
	 */
	public HealthCheckRegistry getHealthCheckRegistry();
}