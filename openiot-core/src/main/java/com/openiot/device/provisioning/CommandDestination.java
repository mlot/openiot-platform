/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning;

import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.IDeviceNestingContext;
import com.openiot.spi.device.command.IDeviceCommandExecution;
import com.openiot.spi.device.command.ISystemCommand;
import com.openiot.spi.device.provisioning.ICommandDeliveryParameterExtractor;
import com.openiot.spi.device.provisioning.ICommandDeliveryProvider;
import com.openiot.spi.device.provisioning.ICommandDestination;
import com.openiot.spi.device.provisioning.ICommandExecutionEncoder;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;

/**
 * Default implementation of {@link ICommandDestination}.
 * 
 * @author Derek
 * 
 * @param <T>
 */
public class CommandDestination<T, P> extends LifecycleComponent implements ICommandDestination<T, P> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(CommandDestination.class);

	/** Unique destination id */
	private String destinationId;

	/** Configured command execution encoder */
	private ICommandExecutionEncoder<T> commandExecutionEncoder;

	/** Configured command delivery parameter extractor */
	private ICommandDeliveryParameterExtractor<P> commandDeliveryParameterExtractor;

	/** Configured command delivery provider */
	private ICommandDeliveryProvider<T, P> commandDeliveryProvider;

	public CommandDestination() {
		super(LifecycleComponentType.CommandDestination);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ICommandDestination#deliverCommand(com.openiot
	 * .spi.device.command.IDeviceCommandExecution,
	 * IDeviceNestingContext,
	 * IDeviceAssignment)
	 */
	@Override
	public void deliverCommand(IDeviceCommandExecution execution, IDeviceNestingContext nesting,
			IDeviceAssignment assignment) throws OpenIoTException {
		T encoded = getCommandExecutionEncoder().encode(execution, nesting, assignment);
		P params =
				getCommandDeliveryParameterExtractor().extractDeliveryParameters(nesting, assignment,
						execution);
		getCommandDeliveryProvider().deliver(nesting, assignment, execution, encoded, params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ICommandDestination#deliverSystemCommand(
	 * ISystemCommand,
	 * IDeviceNestingContext,
	 * IDeviceAssignment)
	 */
	@Override
	public void deliverSystemCommand(ISystemCommand command, IDeviceNestingContext nesting,
			IDeviceAssignment assignment) throws OpenIoTException {
		T encoded = getCommandExecutionEncoder().encodeSystemCommand(command, nesting, assignment);
		P params =
				getCommandDeliveryParameterExtractor().extractDeliveryParameters(nesting, assignment, null);
		getCommandDeliveryProvider().deliverSystemCommand(nesting, assignment, encoded, params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		LOGGER.info("Starting command destination '" + getDestinationId() + "'.");

		// Start command execution encoder.
		if (getCommandExecutionEncoder() == null) {
			throw new OpenIoTException("No command execution encoder configured for destination.");
		}
		startNestedComponent(getCommandExecutionEncoder(), true);

		// Start command execution encoder.
		if (getCommandDeliveryParameterExtractor() == null) {
			throw new OpenIoTException(
					"No command delivery parameter extractor configured for destination.");
		}

		// Start command delivery provider.
		if (getCommandDeliveryProvider() == null) {
			throw new OpenIoTException("No command delivery provider configured for destination.");
		}
		startNestedComponent(getCommandDeliveryProvider(), true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see LifecycleComponent#getComponentName()
	 */
	@Override
	public String getComponentName() {
		return "Command Destination (" + getDestinationId() + ")";
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
		LOGGER.info("Stopping command destination '" + getDestinationId() + "'.");

		// Stop command execution encoder.
		if (getCommandExecutionEncoder() != null) {
			getCommandExecutionEncoder().lifecycleStop();
		}

		// Stop command delivery provider.
		if (getCommandDeliveryProvider() != null) {
			getCommandDeliveryProvider().lifecycleStop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ICommandDestination#getDestinationId()
	 */
	public String getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(String destinationId) {
		this.destinationId = destinationId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ICommandDestination#getCommandExecutionEncoder
	 * ()
	 */
	public ICommandExecutionEncoder<T> getCommandExecutionEncoder() {
		return commandExecutionEncoder;
	}

	public void setCommandExecutionEncoder(ICommandExecutionEncoder<T> commandExecutionEncoder) {
		this.commandExecutionEncoder = commandExecutionEncoder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ICommandDestination#
	 * getCommandDeliveryParameterExtractor()
	 */
	public ICommandDeliveryParameterExtractor<P> getCommandDeliveryParameterExtractor() {
		return commandDeliveryParameterExtractor;
	}

	public void setCommandDeliveryParameterExtractor(
			ICommandDeliveryParameterExtractor<P> commandDeliveryParameterExtractor) {
		this.commandDeliveryParameterExtractor = commandDeliveryParameterExtractor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ICommandDestination#getCommandDeliveryProvider
	 * ()
	 */
	public ICommandDeliveryProvider<T, P> getCommandDeliveryProvider() {
		return commandDeliveryProvider;
	}

	public void setCommandDeliveryProvider(ICommandDeliveryProvider<T, P> commandDeliveryProvider) {
		this.commandDeliveryProvider = commandDeliveryProvider;
	}
}