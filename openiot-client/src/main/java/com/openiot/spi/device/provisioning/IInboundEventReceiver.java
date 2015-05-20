/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.provisioning;

import com.openiot.spi.server.lifecycle.ILifecycleComponent;

import java.util.Map;

/**
 * Handles receipt of device event information from an underlying transport.
 * 
 * @author Derek
 */
public interface IInboundEventReceiver<T> extends ILifecycleComponent {

	/**
	 * Get name shown in user interfaces when referencing receiver.
	 * 
	 * @return
	 */
	public String getDisplayName();

	/**
	 * Called when an event payload is received.
	 * 
	 * @param payload
	 */
	public void onEventPayloadReceived(T payload, Map context);

	/**
	 * Set the parent event source that will process events.
	 * 
	 * @param source
	 */
	public void setEventSource(IInboundEventSource<T> source);

	/**
	 * Get the parent event source.
	 * 
	 * @return
	 */
	public IInboundEventSource<T> getEventSource();
}