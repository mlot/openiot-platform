/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.server;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.openiot.configuration.ExternalConfigurationResolver;
import com.openiot.configuration.TomcatConfigurationResolver;
import com.openiot.device.event.processor.OutboundProcessingStrategyDecorator;
import com.openiot.rest.model.search.SearchCriteria;
import com.openiot.rest.model.user.User;
import com.openiot.rest.model.user.UserSearchCriteria;
import com.openiot.security.SitewhereAuthentication;
import com.openiot.security.SitewhereUserDetails;
import com.openiot.server.debug.NullTracer;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.asset.IAssetModuleManager;
import com.openiot.spi.configuration.IConfigurationResolver;
import com.openiot.spi.device.ICachingDeviceManagement;
import com.openiot.spi.device.IDeviceManagement;
import com.openiot.spi.device.IDeviceManagementCacheProvider;
import com.openiot.spi.device.ISite;
import com.openiot.spi.device.event.processor.IInboundEventProcessorChain;
import com.openiot.spi.device.event.processor.IOutboundEventProcessorChain;
import com.openiot.spi.device.provisioning.IDeviceProvisioning;
import com.openiot.spi.search.ISearchResults;
import com.openiot.spi.search.external.ISearchProviderManager;
import com.openiot.spi.server.IOpenIoTServer;
import com.openiot.spi.server.IOpenIoTServerEnvironment;
import com.openiot.spi.server.debug.ITracer;
import com.openiot.spi.server.device.IDeviceModelInitializer;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import com.openiot.spi.server.user.IUserModelInitializer;
import com.openiot.spi.system.IVersion;
import com.openiot.spi.user.IGrantedAuthority;
import com.openiot.spi.user.IUser;
import com.openiot.spi.user.IUserManagement;
import com.openiot.version.VersionHelper;
import org.apache.log4j.Logger;
import org.mule.util.StringMessageUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link com.openiot.spi.server.IOpenIoTServer} for community edition.
 * 
 * @author Derek Adams
 */
public class OpenIoTServer extends LifecycleComponent implements IOpenIoTServer {

	/** Private logger instance */
	private static Logger LOGGER = Logger.getLogger(OpenIoTServer.class);

	/** Spring context for server */
	public static ApplicationContext SERVER_SPRING_CONTEXT;

	/** Contains version information */
	private IVersion version;

	/** Server startup error */
	private Throwable serverStartupError;

	/** Provides hierarchical tracing for debugging */
	private ITracer tracer = new NullTracer();

	/** Allows Spring configuration to be resolved */
	private IConfigurationResolver configurationResolver = new TomcatConfigurationResolver();

	/** Interface to user management implementation */
	private IUserManagement userManagement;

	/** Device management cache provider implementation */
	private IDeviceManagementCacheProvider deviceManagementCacheProvider;

	/** Interface to device management implementation */
	private IDeviceManagement deviceManagement;

	/** Interface to inbound event processor chain */
	private IInboundEventProcessorChain inboundEventProcessorChain;

	/** Interface to outbound event processor chain */
	private IOutboundEventProcessorChain outboundEventProcessorChain;

	/** Interface to device provisioning implementation */
	private IDeviceProvisioning deviceProvisioning;

	/** Interface for the asset module manager */
	private IAssetModuleManager assetModuleManager;

	/** Interface for the search provider manager */
	private ISearchProviderManager searchProviderManager;

	/** List of components registered to participate in OpenIoT server lifecycle */
	private List<ILifecycleComponent> registeredLifecycleComponents = new ArrayList<ILifecycleComponent>();

	/** Map of component ids to lifecycle components */
	private Map<String, ILifecycleComponent> lifecycleComponentsById =
			new HashMap<String, ILifecycleComponent>();

	/** Metric regsitry */
	private MetricRegistry metricRegistry = new MetricRegistry();

	/** Health check registry */
	private HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

	public OpenIoTServer() {
		super(LifecycleComponentType.System);
	}

	/**
	 * Get Spring application context for Atlas server objects.
	 * 
	 * @return
	 */
	public static ApplicationContext getServerSpringContext() {
		return SERVER_SPRING_CONTEXT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#getVersion()
	 */
	public IVersion getVersion() {
		return version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#getServerStartupError()
	 */
	public Throwable getServerStartupError() {
		return serverStartupError;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#getTracer()
	 */
	public ITracer getTracer() {
		return tracer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#getConfigurationResolver()
	 */
	public IConfigurationResolver getConfigurationResolver() {
		return configurationResolver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#getUserManagement()
	 */
	public IUserManagement getUserManagement() {
		return userManagement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#getDeviceManagement()
	 */
	public IDeviceManagement getDeviceManagement() {
		return deviceManagement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#getDeviceManagementCacheProvider()
	 */
	public IDeviceManagementCacheProvider getDeviceManagementCacheProvider() {
		return deviceManagementCacheProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#getInboundEventProcessorChain()
	 */
	public IInboundEventProcessorChain getInboundEventProcessorChain() {
		return inboundEventProcessorChain;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#getOutboundEventProcessorChain()
	 */
	public IOutboundEventProcessorChain getOutboundEventProcessorChain() {
		return outboundEventProcessorChain;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#getDeviceProvisioning()
	 */
	public IDeviceProvisioning getDeviceProvisioning() {
		return deviceProvisioning;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#getAssetModuleManager()
	 */
	public IAssetModuleManager getAssetModuleManager() {
		return assetModuleManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#getSearchProviderManager()
	 */
	public ISearchProviderManager getSearchProviderManager() {
		return searchProviderManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#getRegisteredLifecycleComponents()
	 */
	public List<ILifecycleComponent> getRegisteredLifecycleComponents() {
		return registeredLifecycleComponents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#getMetricRegistry()
	 */
	public MetricRegistry getMetricRegistry() {
		return metricRegistry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#getHealthCheckRegistry()
	 */
	public HealthCheckRegistry getHealthCheckRegistry() {
		return healthCheckRegistry;
	}

	/**
	 * Returns a fake account used for operations on the data model done by the system.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SitewhereAuthentication getSystemAuthentication() throws OpenIoTException {
		User fake = new User();
		fake.setUsername("system");
		SitewhereUserDetails details = new SitewhereUserDetails(fake, new ArrayList<IGrantedAuthority>());
		SitewhereAuthentication auth = new SitewhereAuthentication(details, null);
		return auth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		// Clear the component list.
		getLifecycleComponents().clear();

		// Start all lifecycle components.
		for (ILifecycleComponent component : getRegisteredLifecycleComponents()) {
			startNestedComponent(component, component.getComponentName() + " startup failed.", true);
		}

		// Start device management.
		startNestedComponent(getDeviceManagement(), "Device management startup failed.", true);

		// Start device management cache provider if specificed.
		if (getDeviceManagementCacheProvider() != null) {
			startNestedComponent(getDeviceManagementCacheProvider(),
					"Device management cache provider startup failed.", true);
		}

		// Start user management.
		startNestedComponent(getUserManagement(), "User management startup failed.", true);

		// Start asset module manager.
		startNestedComponent(getAssetModuleManager(), "Asset module manager startup failed.", true);

		// Start search provider manager.
		startNestedComponent(getSearchProviderManager(), "Search provider manager startup failed.", true);

		// Populate data if requested.
		verifyUserModel();
		verifyDeviceModel();

		// Enable provisioning.
		if (getOutboundEventProcessorChain() != null) {
			startNestedComponent(getOutboundEventProcessorChain(),
					"Outbound processor chain startup failed.", true);
			getOutboundEventProcessorChain().setProcessingEnabled(true);
		}
		if (getInboundEventProcessorChain() != null) {
			startNestedComponent(getInboundEventProcessorChain(), "Inbound processor chain startup failed.",
					true);
		}

		// Start device provisioning.
		startNestedComponent(getDeviceProvisioning(), "Device provisioning startup failed.", true);

		// Force refresh on components-by-id map.
		refreshLifecycleComponentMap(this, lifecycleComponentsById);
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
	 * @see LifecycleComponent#getComponentName()
	 */
	@Override
	public String getComponentName() {
		return "OpenIoT Server " + getVersion().getEditionIdentifier() + " "
				+ getVersion().getVersionIdentifier();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOpenIoTServer#getLifecycleComponentById(java.lang.String
	 * )
	 */
	@Override
	public ILifecycleComponent getLifecycleComponentById(String id) {
		return lifecycleComponentsById.get(id);
	}

	/**
	 * Recursively navigates component structure and creates a map of components by id.
	 * 
	 * @param current
	 * @param map
	 */
	protected void refreshLifecycleComponentMap(ILifecycleComponent current,
			Map<String, ILifecycleComponent> map) {
		map.put(current.getComponentId(), current);
		for (ILifecycleComponent sub : current.getLifecycleComponents()) {
			refreshLifecycleComponentMap(sub, map);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
		// Disable provisioning.
		getDeviceProvisioning().lifecycleStop();
		getInboundEventProcessorChain().lifecycleStop();
		getOutboundEventProcessorChain().setProcessingEnabled(false);
		getOutboundEventProcessorChain().lifecycleStop();

		// Stop core management implementations.
		if (getDeviceManagementCacheProvider() != null) {
			getDeviceManagementCacheProvider().lifecycleStop();
		}
		getDeviceManagement().lifecycleStop();
		getUserManagement().lifecycleStop();
		getAssetModuleManager().lifecycleStop();
		getSearchProviderManager().lifecycleStop();

		// Start all lifecycle components.
		for (ILifecycleComponent component : getRegisteredLifecycleComponents()) {
			component.lifecycleStop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTServer#initialize()
	 */
	public void initialize() throws OpenIoTException {
		LOGGER.info("Initializing OpenIoT server components.");

		this.version = VersionHelper.getVersion();

		// Initialize Spring.
		initializeSpringContext();

		// Initialize tracer.
		initializeTracer();

		// Initialize device provisioning.
		initializeDeviceProvisioning();

		// Initialize device management.
		initializeDeviceManagement();

		// Initialize processing chain for inbound events.
		initializeInboundEventProcessorChain();

		// Initialize user management.
		initializeUserManagement();

		// Initialize asset management.
		initializeAssetManagement();

		// Initialize search provider management.
		initializeSearchProviderManagement();

		// Show banner containing server information.
		showServerBanner();
	}

	/**
	 * Displays the server information banner in the log.
	 */
	protected void showServerBanner() {
		String os = System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")";
		String java = System.getProperty("java.vendor") + " (" + System.getProperty("java.version") + ")";

		// Print version information.
		List<String> messages = new ArrayList<String>();
		messages.add("OpenIoT Server " + version.getEdition());
		messages.add("");
		messages.add("Version: " + version.getVersionIdentifier() + "." + version.getBuildTimestamp());
		messages.add("Operating System: " + os);
		messages.add("Java Runtime: " + java);
		messages.add("");
		messages.add("Copyright (c) 2009-2015 OpenIoTPlatform.org");
		String message = StringMessageUtils.getBoilerPlate(messages, '*', 60);
		LOGGER.info("\n" + message + "\n");
	}

	/**
	 * Allows subclasses to add their own banner messages.
	 * 
	 * @param messages
	 */
	protected void addBannerMessages(List<String> messages) {
	}

	/**
	 * Verifies and loads the Spring configuration file.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void initializeSpringContext() throws OpenIoTException {
		String extConfig = System.getenv(IOpenIoTServerEnvironment.ENV_EXTERNAL_CONFIGURATION_URL);
		if (extConfig != null) {
			IConfigurationResolver resolver = new ExternalConfigurationResolver(extConfig);
			SERVER_SPRING_CONTEXT = resolver.resolveOpenIoTContext(getVersion());
		} else {
			SERVER_SPRING_CONTEXT = getConfigurationResolver().resolveOpenIoTContext(getVersion());
		}
	}

	/**
	 * Initialize debug tracing implementation.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void initializeTracer() throws OpenIoTException {
		try {
			this.tracer = (ITracer) SERVER_SPRING_CONTEXT.getBean(OpenIoTServerBeans.BEAN_TRACER);
			LOGGER.info("Tracer implementation using: " + tracer.getClass().getName());
		} catch (NoSuchBeanDefinitionException e) {
			LOGGER.info("No Tracer implementation configured.");
			this.tracer = new NullTracer();
		}
	}

	/**
	 * Initialize device management implementation and associated decorators.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void initializeDeviceManagement() throws OpenIoTException {
		// Load device management cache provider if configured.
		try {
			this.deviceManagementCacheProvider =
					(IDeviceManagementCacheProvider) SERVER_SPRING_CONTEXT.getBean(OpenIoTServerBeans.BEAN_DEVICE_MANAGEMENT_CACHE_PROVIDER);
			LOGGER.info("Device management cache provider using: "
					+ deviceManagementCacheProvider.getClass().getName());
		} catch (NoSuchBeanDefinitionException e) {
			LOGGER.info("No device management cache provider configured. Caching disabled.");
		}

		// Verify that a device management implementation exists.
		try {
			IDeviceManagement deviceManagementImpl =
					(IDeviceManagement) SERVER_SPRING_CONTEXT.getBean(OpenIoTServerBeans.BEAN_DEVICE_MANAGEMENT);
			this.deviceManagement = configureDeviceManagement(deviceManagementImpl);
			LOGGER.info("Device management implementation using: "
					+ deviceManagementImpl.getClass().getName());

		} catch (NoSuchBeanDefinitionException e) {
			throw new OpenIoTException("No device management implementation configured.");
		}
	}

	/**
	 * Configure device management implementation by injecting configured options or
	 * wrapping to add functionality.
	 * 
	 * @param wrapped
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected IDeviceManagement configureDeviceManagement(IDeviceManagement management)
			throws OpenIoTException {

		// Inject cache provider if available.
		if (getDeviceManagementCacheProvider() != null) {
			if (management instanceof ICachingDeviceManagement) {
				((ICachingDeviceManagement) management).setCacheProvider(getDeviceManagementCacheProvider());
				LOGGER.info("Device management implementation is using configured cache provider.");
			} else {
				LOGGER.info("Device management implementation not using cache provider.");
			}
		}

		// If device event processor chain is defined, use it.
		try {
			outboundEventProcessorChain =
					(IOutboundEventProcessorChain) SERVER_SPRING_CONTEXT.getBean(OpenIoTServerBeans.BEAN_OUTBOUND_PROCESSOR_CHAIN);
			management = new OutboundProcessingStrategyDecorator(management);
			LOGGER.info("Event processor chain found with "
					+ outboundEventProcessorChain.getProcessors().size() + " processors.");
		} catch (NoSuchBeanDefinitionException e) {
			LOGGER.info("No outbound event processor chain found in configuration file.");
		}

		return management;
	}

	/**
	 * Initializes the {@link IInboundEventProcessorChain} that handles events coming into
	 * the system from external devices.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void initializeInboundEventProcessorChain() throws OpenIoTException {
		try {
			inboundEventProcessorChain =
					(IInboundEventProcessorChain) SERVER_SPRING_CONTEXT.getBean(OpenIoTServerBeans.BEAN_INBOUND_PROCESSOR_CHAIN);
		} catch (NoSuchBeanDefinitionException e) {
			LOGGER.info("No inbound event processor chain found in configuration file.");
		}
	}

	/**
	 * Verify and initialize device provisioning implementation.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void initializeDeviceProvisioning() throws OpenIoTException {
		try {
			deviceProvisioning =
					(IDeviceProvisioning) SERVER_SPRING_CONTEXT.getBean(OpenIoTServerBeans.BEAN_DEVICE_PROVISIONING);
		} catch (NoSuchBeanDefinitionException e) {
			throw new OpenIoTException("No device provisioning implementation configured.");
		}
	}

	/**
	 * Verify and initialize user management implementation.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void initializeUserManagement() throws OpenIoTException {
		try {
			userManagement =
					(IUserManagement) SERVER_SPRING_CONTEXT.getBean(OpenIoTServerBeans.BEAN_USER_MANAGEMENT);
		} catch (NoSuchBeanDefinitionException e) {
			throw new OpenIoTException("No user management implementation configured.");
		}
	}

	/**
	 * Verify and initialize asset module manager.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void initializeAssetManagement() throws OpenIoTException {
		try {
			assetModuleManager =
					(IAssetModuleManager) SERVER_SPRING_CONTEXT.getBean(OpenIoTServerBeans.BEAN_ASSET_MODULE_MANAGER);
		} catch (NoSuchBeanDefinitionException e) {
			throw new OpenIoTException("No asset module manager implementation configured.");
		}
	}

	/**
	 * Verify and initialize search provider manager.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void initializeSearchProviderManagement() throws OpenIoTException {
		try {
			searchProviderManager =
					(ISearchProviderManager) SERVER_SPRING_CONTEXT.getBean(OpenIoTServerBeans.BEAN_SEARCH_PROVIDER_MANAGER);
		} catch (NoSuchBeanDefinitionException e) {
			throw new OpenIoTException("No search provider manager implementation configured.");
		}
	}

	/**
	 * Read a line from standard in.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected String readLine() throws OpenIoTException {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			return br.readLine();
		} catch (IOException e) {
			throw new OpenIoTException(e);
		}
	}

	/**
	 * Check whether user model is populated and offer to bootstrap system if not.
	 */
	protected void verifyUserModel() {
		try {
			IUserModelInitializer init =
					(IUserModelInitializer) SERVER_SPRING_CONTEXT.getBean(OpenIoTServerBeans.BEAN_USER_MODEL_INITIALIZER);
			List<IUser> users = getUserManagement().listUsers(new UserSearchCriteria());
			if (users.size() == 0) {
				List<String> messages = new ArrayList<String>();
				messages.add("User model is currently empty. A default user and permissions can be "
						+ "created automatically so that the admin interface and web services can authenticate. "
						+ "Create default user and permissions now?");
				String message = StringMessageUtils.getBoilerPlate(messages, '*', 60);
				LOGGER.info("\n" + message + "\n");
				System.out.println("Initialize user model? Yes/No (Default is Yes)");
				String response = readLine();
				if ((response == null) && (init.isInitializeIfNoConsole())) {
					response = "Y";
				} else if ((response == null) && (!init.isInitializeIfNoConsole())) {
					response = "N";
				}
				if ((response.length() == 0) || (response.toLowerCase().startsWith("y"))) {
					init.initialize(getUserManagement());
				}
			}
		} catch (NoSuchBeanDefinitionException e) {
			LOGGER.info("No user model initializer found in Spring bean configuration. Skipping.");
			return;
		} catch (OpenIoTException e) {
			LOGGER.warn("Error verifying user model.", e);
		}
	}

	/**
	 * Check whether device model is populated and offer to bootstrap system if not.
	 */
	protected void verifyDeviceModel() {
		try {
			IDeviceModelInitializer init =
					(IDeviceModelInitializer) SERVER_SPRING_CONTEXT.getBean(OpenIoTServerBeans.BEAN_DEVICE_MODEL_INITIALIZER);
			ISearchResults<ISite> sites = getDeviceManagement().listSites(new SearchCriteria(1, 1));
			if (sites.getNumResults() == 0) {
				List<String> messages = new ArrayList<String>();
				messages.add("There are currently no sites defined in the system. You have the option of loading "
						+ "a default dataset for previewing system functionality. Would you like to load the default "
						+ "dataset?");
				String message = StringMessageUtils.getBoilerPlate(messages, '*', 60);
				LOGGER.info("\n" + message + "\n");
				System.out.println("Load default dataset? Yes/No (Default is Yes)");
				String response = readLine();
				if ((response == null) && (init.isInitializeIfNoConsole())) {
					response = "Y";
				} else if ((response == null) && (!init.isInitializeIfNoConsole())) {
					response = "N";
				}
				if ((response.length() == 0) || (response.toLowerCase().startsWith("y"))) {
					init.initialize(getDeviceManagement());
				}
			}
		} catch (NoSuchBeanDefinitionException e) {
			LOGGER.info("No device model initializer found in Spring bean configuration. Skipping.");
			return;
		} catch (OpenIoTException e) {
			LOGGER.warn("Unable to read from device model.", e);
		}
	}
}