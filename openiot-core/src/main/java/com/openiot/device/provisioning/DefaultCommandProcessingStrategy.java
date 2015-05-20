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
import com.openiot.spi.device.IDevice;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.IDeviceManagement;
import com.openiot.spi.device.IDeviceNestingContext;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.command.IDeviceCommandExecution;
import com.openiot.spi.device.command.ISystemCommand;
import com.openiot.spi.device.event.IDeviceCommandInvocation;
import com.openiot.spi.device.provisioning.ICommandExecutionBuilder;
import com.openiot.spi.device.provisioning.ICommandProcessingStrategy;
import com.openiot.spi.device.provisioning.ICommandTargetResolver;
import com.openiot.spi.device.provisioning.IDeviceProvisioning;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Default implementation of {@link ICommandProcessingStrategy}.
 * 
 * @author Derek
 */
public class DefaultCommandProcessingStrategy extends LifecycleComponent implements
		ICommandProcessingStrategy {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(DefaultCommandProcessingStrategy.class);

	/** Configured command target resolver */
	private ICommandTargetResolver commandTargetResolver = new DefaultCommandTargetResolver();

	/** Configured command execution builder */
	private ICommandExecutionBuilder commandExecutionBuilder = new DefaultCommandExecutionBuilder();

	public DefaultCommandProcessingStrategy() {
		super(LifecycleComponentType.CommandProcessingStrategy);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ICommandProcessingStrategy#deliverCommand
	 * (IDeviceProvisioning,
	 * IDeviceCommandInvocation)
	 */
	@Override
	public void deliverCommand(IDeviceProvisioning provisioning, IDeviceCommandInvocation invocation)
			throws OpenIoTException {
		LOGGER.debug("Command processing strategy handling invocation.");
		IDeviceCommand command =
				OpenIoT.getServer().getDeviceManagement().getDeviceCommandByToken(
						invocation.getCommandToken());
		if (command != null) {
			IDeviceCommandExecution execution =
					getCommandExecutionBuilder().createExecution(command, invocation);
			List<IDeviceAssignment> assignments = getCommandTargetResolver().resolveTargets(invocation);
			for (IDeviceAssignment assignment : assignments) {
				IDevice device =
						OpenIoT.getServer().getDeviceManagement().getDeviceForAssignment(assignment);
				if (device == null) {
					throw new OpenIoTException("Targeted assignment references device that does not exist.");
				}

				IDeviceNestingContext nesting = NestedDeviceSupport.calculateNestedDeviceInformation(device);
				provisioning.getOutboundCommandRouter().routeCommand(execution, nesting, assignment);
			}
		} else {
			throw new OpenIoTException("Invalid command referenced from invocation.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ICommandProcessingStrategy#deliverSystemCommand
	 * (IDeviceProvisioning, java.lang.String,
	 * ISystemCommand)
	 */
	@Override
	public void deliverSystemCommand(IDeviceProvisioning provisioning, String hardwareId,
			ISystemCommand command) throws OpenIoTException {
		IDeviceManagement management = OpenIoT.getServer().getDeviceManagement();
		IDevice device = management.getDeviceByHardwareId(hardwareId);
		if (device == null) {
			throw new OpenIoTException("Targeted assignment references device that does not exist.");
		}
		IDeviceAssignment assignment = management.getCurrentDeviceAssignment(device);
		IDeviceNestingContext nesting = NestedDeviceSupport.calculateNestedDeviceInformation(device);
		provisioning.getOutboundCommandRouter().routeSystemCommand(command, nesting, assignment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		LOGGER.info("Started command processing strategy.");

		// Start command execution builder.
		if (getCommandExecutionBuilder() == null) {
			throw new OpenIoTException("No command execution builder configured for provisioning.");
		}
		getCommandExecutionBuilder().lifecycleStart();

		// Start command target resolver.
		if (getCommandTargetResolver() == null) {
			throw new OpenIoTException("No command target resolver configured for provisioning.");
		}
		getCommandTargetResolver().lifecycleStart();
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
		LOGGER.info("Stopped command processing strategy");

		// Stop command execution builder.
		if (getCommandExecutionBuilder() != null) {
			getCommandExecutionBuilder().lifecycleStop();
		}

		// Stop command target resolver.
		if (getCommandTargetResolver() != null) {
			getCommandTargetResolver().lifecycleStop();
		}
	}

	public ICommandTargetResolver getCommandTargetResolver() {
		return commandTargetResolver;
	}

	public void setCommandTargetResolver(ICommandTargetResolver commandTargetResolver) {
		this.commandTargetResolver = commandTargetResolver;
	}

	public ICommandExecutionBuilder getCommandExecutionBuilder() {
		return commandExecutionBuilder;
	}

	public void setCommandExecutionBuilder(ICommandExecutionBuilder commandExecutionBuilder) {
		this.commandExecutionBuilder = commandExecutionBuilder;
	}
}