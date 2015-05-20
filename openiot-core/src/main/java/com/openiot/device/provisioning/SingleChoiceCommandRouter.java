/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.IDeviceNestingContext;
import com.openiot.spi.device.command.IDeviceCommandExecution;
import com.openiot.spi.device.command.ISystemCommand;
import com.openiot.spi.device.provisioning.ICommandDestination;
import com.openiot.spi.device.provisioning.IOutboundCommandRouter;
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * Implementation of {@link IOutboundCommandRouter} that assumes a single
 * {@link ICommandDestination} is available and delivers commands to it.
 * 
 * @author Derek
 */
public class SingleChoiceCommandRouter extends OutboundCommandRouter {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(SingleChoiceCommandRouter.class);

	/** Destinations that will deliver all commands */
	private ICommandDestination<?, ?> destination;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundCommandRouter#routeCommand(com.openiot
	 * .spi.device.command.IDeviceCommandExecution,
	 * IDeviceNestingContext,
	 * IDeviceAssignment)
	 */
	@Override
	public void routeCommand(IDeviceCommandExecution execution, IDeviceNestingContext nesting,
			IDeviceAssignment assignment) throws OpenIoTException {
		destination.deliverCommand(execution, nesting, assignment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundCommandRouter#routeSystemCommand
	 * (ISystemCommand,
	 * IDeviceNestingContext,
	 * IDeviceAssignment)
	 */
	@Override
	public void routeSystemCommand(ISystemCommand command, IDeviceNestingContext nesting,
			IDeviceAssignment assignment) throws OpenIoTException {
		destination.deliverSystemCommand(command, nesting, assignment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		LOGGER.info("Starting single choice command router...");
		if (getDestinations().size() != 1) {
			throw new OpenIoTException("Expected exactly one destination for command routing but found "
					+ getDestinations().size() + ".");
		}
		Iterator<ICommandDestination<?, ?>> it = getDestinations().values().iterator();
		this.destination = it.next();
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
		LOGGER.info("Stopped single choice command router.");
	}
}