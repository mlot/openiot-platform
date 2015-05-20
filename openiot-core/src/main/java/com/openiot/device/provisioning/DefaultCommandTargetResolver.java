/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning;

import com.openiot.OpenIoT;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.event.IDeviceCommandInvocation;
import com.openiot.spi.device.provisioning.ICommandTargetResolver;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses information in an {@link IDeviceCommandInvocation} to determine a list of target
 * {@link IDeviceAssignment} objects. This implementation returns the
 * {@link IDeviceAssignment} associated with the invocation.
 * 
 * @author Derek
 */
public class DefaultCommandTargetResolver extends LifecycleComponent implements ICommandTargetResolver {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(DefaultCommandTargetResolver.class);

	public DefaultCommandTargetResolver() {
		super(LifecycleComponentType.CommandTargetResolver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ICommandTargetResolver#resolveTargets(com
	 * .sitewhere.spi.device.event.IDeviceCommandInvocation)
	 */
	@Override
	public List<IDeviceAssignment> resolveTargets(IDeviceCommandInvocation invocation)
			throws OpenIoTException {
		LOGGER.debug("Resolving target for invocation.");
		IDeviceAssignment assignment =
				OpenIoT.getServer().getDeviceManagement().getDeviceAssignmentByToken(
						invocation.getDeviceAssignmentToken());
		List<IDeviceAssignment> results = new ArrayList<IDeviceAssignment>();
		results.add(assignment);
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		LOGGER.info("Started command target resolver.");
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
		LOGGER.info("Stopped command target resolver");
	}
}