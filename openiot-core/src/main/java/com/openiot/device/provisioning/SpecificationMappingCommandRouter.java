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

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link IOutboundCommandRouter} that maps specification ids to
 * {@link ICommandDestination} ids and routes accordingly.
 * 
 * @author Derek
 */
public class SpecificationMappingCommandRouter extends OutboundCommandRouter {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(SpecificationMappingCommandRouter.class);

	/** Map of specification tokens to command destination ids */
	private Map<String, String> mappings = new HashMap<String, String>();

	/** Default destination for unmapped specifications */
	private String defaultDestination = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
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
	}

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
		ICommandDestination<?, ?> destination = getDestinationForDevice(nesting);
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
		ICommandDestination<?, ?> destination = getDestinationForDevice(nesting);
		destination.deliverSystemCommand(command, nesting, assignment);
	}

	/**
	 * Get {@link ICommandDestination} for device based on specification token associated
	 * with the device.
	 * 
	 * @param device
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected ICommandDestination<?, ?> getDestinationForDevice(IDeviceNestingContext nesting)
			throws OpenIoTException {
		String specToken = nesting.getGateway().getSpecificationToken();
		String destinationId = mappings.get(specToken);
		if (destinationId == null) {
			if (getDefaultDestination() != null) {
				destinationId = getDefaultDestination();
			} else {
				throw new OpenIoTException("No command destination mapping for specification: " + specToken);
			}
		}
		ICommandDestination<?, ?> destination = getDestinations().get(destinationId);
		if (destination == null) {
			throw new OpenIoTException("No destination found for destination id: " + destinationId);
		}
		return destination;
	}

	public Map<String, String> getMappings() {
		return mappings;
	}

	public void setMappings(Map<String, String> mappings) {
		this.mappings = mappings;
	}

	public String getDefaultDestination() {
		return defaultDestination;
	}

	public void setDefaultDestination(String defaultDestination) {
		this.defaultDestination = defaultDestination;
	}
}