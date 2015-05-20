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
import com.openiot.spi.device.batch.IBatchOperationManager;
import com.openiot.spi.device.command.ISystemCommand;
import com.openiot.spi.device.event.IDeviceCommandInvocation;
import com.openiot.spi.device.provisioning.*;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for implementations of {@link IDeviceProvisioning}. Takes care of starting
 * and stopping nested components in the correct order.
 * 
 * @author Derek
 */
public abstract class DeviceProvisioning extends LifecycleComponent implements IDeviceProvisioning {

	/** Configured registration manager */
	private IRegistrationManager registrationManager;

	/** Configured batch operation manager */
	private IBatchOperationManager batchOperationManager;

	/** Configured inbound processing strategy */
	private IInboundProcessingStrategy inboundProcessingStrategy;

	/** Configured list of inbound event sources */
	private List<IInboundEventSource<?>> inboundEventSources = new ArrayList<IInboundEventSource<?>>();

	/** Configured command processing strategy */
	private ICommandProcessingStrategy commandProcessingStrategy;

	/** Configured outbound processing strategy */
	private IOutboundProcessingStrategy outboundProcessingStrategy;

	/** Configured outbound command router */
	private IOutboundCommandRouter outboundCommandRouter;

	/** Configured list of command destinations */
	private List<ICommandDestination<?, ?>> commandDestinations = new ArrayList<ICommandDestination<?, ?>>();

	public DeviceProvisioning() {
		super(LifecycleComponentType.DeviceProvisioning);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		getLifecycleComponents().clear();

		// Start command processing strategy.
		if (getCommandProcessingStrategy() == null) {
			throw new OpenIoTException("No command processing strategy configured for provisioning.");
		}
		startNestedComponent(getCommandProcessingStrategy(), true);

		// Start command destinations.
		if (getCommandDestinations() != null) {
			for (ICommandDestination<?, ?> destination : getCommandDestinations()) {
				startNestedComponent(destination, false);
			}
		}

		// Start outbound command router.
		if (getOutboundCommandRouter() == null) {
			throw new OpenIoTException("No command router for provisioning.");
		}
		getOutboundCommandRouter().initialize(getCommandDestinations());
		startNestedComponent(getOutboundCommandRouter(), true);

		// Start outbound processing strategy.
		if (getOutboundProcessingStrategy() == null) {
			throw new OpenIoTException("No outbound processing strategy configured for provisioning.");
		}
		startNestedComponent(getOutboundProcessingStrategy(), true);

		// Start registration manager.
		if (getRegistrationManager() == null) {
			throw new OpenIoTException("No registration manager configured for provisioning.");
		}
		startNestedComponent(getRegistrationManager(), true);

		// Start batch operation manager.
		if (getBatchOperationManager() == null) {
			throw new OpenIoTException("No batch operation manager configured for provisioning.");
		}
		startNestedComponent(getBatchOperationManager(), true);

		// Start inbound processing strategy.
		if (getInboundProcessingStrategy() == null) {
			throw new OpenIoTException("No inbound processing strategy configured for provisioning.");
		}
		startNestedComponent(getInboundProcessingStrategy(), true);

		// Start device event sources.
		if (getInboundEventSources() != null) {
			for (IInboundEventSource<?> processor : getInboundEventSources()) {
				startNestedComponent(processor, false);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
		// Stop inbound event sources.
		if (getInboundEventSources() != null) {
			for (IInboundEventSource<?> processor : getInboundEventSources()) {
				processor.lifecycleStop();
			}
		}

		// Stop inbound processing strategy.
		if (getInboundProcessingStrategy() != null) {
			getInboundProcessingStrategy().lifecycleStop();
		}

		// Stop batch operation manager.
		if (getBatchOperationManager() != null) {
			getBatchOperationManager().lifecycleStop();
		}

		// Stop registration manager.
		if (getRegistrationManager() != null) {
			getRegistrationManager().lifecycleStop();
		}

		// Stop outbound processing strategy.
		if (getOutboundProcessingStrategy() != null) {
			getOutboundProcessingStrategy().lifecycleStop();
		}

		// Stop command processing strategy.
		if (getCommandProcessingStrategy() != null) {
			getCommandProcessingStrategy().lifecycleStop();
		}

		// Start command destinations.
		if (getCommandDestinations() != null) {
			for (ICommandDestination<?, ?> destination : getCommandDestinations()) {
				destination.lifecycleStop();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceProvisioning#deliverCommand(com.openiot
	 * .spi.device.event.IDeviceCommandInvocation)
	 */
	@Override
	public void deliverCommand(IDeviceCommandInvocation invocation) throws OpenIoTException {
		getCommandProcessingStrategy().deliverCommand(this, invocation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceProvisioning#deliverSystemCommand(
	 * java.lang.String, ISystemCommand)
	 */
	@Override
	public void deliverSystemCommand(String hardwareId, ISystemCommand command) throws OpenIoTException {
		getCommandProcessingStrategy().deliverSystemCommand(this, hardwareId, command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceProvisioning#getRegistrationManager()
	 */
	public IRegistrationManager getRegistrationManager() {
		return registrationManager;
	}

	public void setRegistrationManager(IRegistrationManager registrationManager) {
		this.registrationManager = registrationManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceProvisioning#getBatchOperationManager
	 * ()
	 */
	public IBatchOperationManager getBatchOperationManager() {
		return batchOperationManager;
	}

	public void setBatchOperationManager(IBatchOperationManager batchOperationManager) {
		this.batchOperationManager = batchOperationManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceProvisioning#getInboundProcessingStrategy
	 * ()
	 */
	public IInboundProcessingStrategy getInboundProcessingStrategy() {
		return inboundProcessingStrategy;
	}

	public void setInboundProcessingStrategy(IInboundProcessingStrategy inboundProcessingStrategy) {
		this.inboundProcessingStrategy = inboundProcessingStrategy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceProvisioning#getInboundEventSources()
	 */
	public List<IInboundEventSource<?>> getInboundEventSources() {
		return inboundEventSources;
	}

	public void setInboundEventSources(List<IInboundEventSource<?>> inboundEventSources) {
		this.inboundEventSources = inboundEventSources;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceProvisioning#getCommandProcessingStrategy
	 * ()
	 */
	public ICommandProcessingStrategy getCommandProcessingStrategy() {
		return commandProcessingStrategy;
	}

	public void setCommandProcessingStrategy(ICommandProcessingStrategy commandProcessingStrategy) {
		this.commandProcessingStrategy = commandProcessingStrategy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceProvisioning#getOutboundProcessingStrategy
	 * ()
	 */
	public IOutboundProcessingStrategy getOutboundProcessingStrategy() {
		return outboundProcessingStrategy;
	}

	public void setOutboundProcessingStrategy(IOutboundProcessingStrategy outboundProcessingStrategy) {
		this.outboundProcessingStrategy = outboundProcessingStrategy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceProvisioning#getOutboundCommandRouter
	 * ()
	 */
	public IOutboundCommandRouter getOutboundCommandRouter() {
		return outboundCommandRouter;
	}

	public void setOutboundCommandRouter(IOutboundCommandRouter outboundCommandRouter) {
		this.outboundCommandRouter = outboundCommandRouter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceProvisioning#getCommandDestinations()
	 */
	public List<ICommandDestination<?, ?>> getCommandDestinations() {
		return commandDestinations;
	}

	public void setCommandDestinations(List<ICommandDestination<?, ?>> commandDestinations) {
		this.commandDestinations = commandDestinations;
	}
}