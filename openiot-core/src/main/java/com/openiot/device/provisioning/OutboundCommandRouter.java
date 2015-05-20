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
import com.openiot.spi.device.provisioning.ICommandDestination;
import com.openiot.spi.device.provisioning.IOutboundCommandRouter;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for {@link IOutboundCommandRouter} implementations.
 * 
 * @author Derek
 */
public abstract class OutboundCommandRouter extends LifecycleComponent implements IOutboundCommandRouter {

	/** List of destinations serviced by the router */
	private Map<String, ICommandDestination<?, ?>> destinations =
			new HashMap<String, ICommandDestination<?, ?>>();

	public OutboundCommandRouter() {
		super(LifecycleComponentType.CommandRouter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundCommandRouter#initialize(java.util
	 * .List)
	 */
	@Override
	public void initialize(List<ICommandDestination<?, ?>> destinationList) throws OpenIoTException {
		this.destinations.clear();

		// Create map of destinations by id.
		for (ICommandDestination<?, ?> destination : destinationList) {
			destinations.put(destination.getDestinationId(), destination);
		}
	}

	public Map<String, ICommandDestination<?, ?>> getDestinations() {
		return destinations;
	}
}