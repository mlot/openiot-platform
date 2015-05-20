/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning;

import com.openiot.server.batch.BatchOperationManager;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.provisioning.IDeviceProvisioning;
import org.apache.log4j.Logger;

/**
 * Default implementation of the {@link IDeviceProvisioning} interface.
 * 
 * @author Derek
 */
public class DefaultDeviceProvisioning extends DeviceProvisioning {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(DefaultDeviceProvisioning.class);

	public DefaultDeviceProvisioning() {
		setRegistrationManager(new RegistrationManager());
		setBatchOperationManager(new BatchOperationManager());
		setInboundProcessingStrategy(new BlockingQueueInboundProcessingStrategy());
		setCommandProcessingStrategy(new DefaultCommandProcessingStrategy());
		setOutboundProcessingStrategy(new BlockingQueueOutboundProcessingStrategy());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see DeviceProvisioning#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		super.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see LifecycleComponent#getComponentName()
	 */
	@Override
	public String getComponentName() {
		return "CE Device Provisioning";
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
	 * @see DeviceProvisioning#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
		LOGGER.info("Stopping CE device provisioning implementation.");
		super.stop();
		LOGGER.info("Completed CE device provisioning shutdown.");
	}
}