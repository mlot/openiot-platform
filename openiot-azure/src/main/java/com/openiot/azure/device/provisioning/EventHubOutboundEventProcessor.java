/*
 * Copyright (c) Microsoft Open Technologies (Shanghai) Company Limited.  All rights reserved.
 *
 *  The MIT License (MIT)
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package com.openiot.azure.device.provisioning;

import com.openiot.azure.device.provisioning.sender.EventHubSender;
import com.openiot.azure.device.provisioning.sender.EventHubSenderImpl;
import com.openiot.common.MarshalUtils;
import com.openiot.device.event.processor.OutboundEventProcessor;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.*;
import com.openiot.spi.device.event.processor.IOutboundEventProcessor;
import org.apache.log4j.Logger;

import java.net.URLEncoder;

/**
 * Implementation of {@link IOutboundEventProcessor} that sends events to an EventHub
 * running on Azure.
 * 
 * @author Derek
 */
public class EventHubOutboundEventProcessor extends OutboundEventProcessor {

	/** Static logger instance */
	private static final Logger LOGGER = Logger.getLogger(EventHubOutboundEventProcessor.class);

	/** SAS identity name */
	private String sasName;

	/** SAS key */
	private String sasKey;

	/** Service bus name */
	private String serviceBusName;

	/** Event hub name or Queue/Topic name*/
	private String entityPath;

    private EventHubSender sender;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		try {

			String key = URLEncoder.encode(getSasKey(), "UTF8");
			String connectionString = "amqps://" + getSasName() + ":" + key + "@" + getServiceBusName();

            //todo sending to specific partition is not supported so far
            //todo should using sender thread pool
            sender = new EventHubSenderImpl(connectionString, getEntityPath(),"");
            sender.open();

		} catch (Exception e) {
			throw new OpenIoTException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
		if (sender != null) {
			try {
				sender.close();
			} catch (Exception e) {
				LOGGER.warn("Error closing message source for EventHub processor.", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * OutboundEventProcessor#onMeasurements(com.
	 * sitewhere.spi.device.event.IDeviceMeasurements)
	 */
	@Override
	public void onMeasurements(IDeviceMeasurements measurements) throws OpenIoTException {
		//sendEvent(measurements);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * OutboundEventProcessor#onLocation(com.openiot
	 * .spi.device.event.IDeviceLocation)
	 */
	@Override
	public void onLocation(IDeviceLocation location) throws OpenIoTException {
		//sendEvent(location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * OutboundEventProcessor#onAlert(com.openiot
	 * .spi.device.event.IDeviceAlert)
	 */
	@Override
	public void onAlert(IDeviceAlert alert) throws OpenIoTException {
		sendEvent(alert);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * OutboundEventProcessor#onCommandInvocation
	 * (IDeviceCommandInvocation)
	 */
	@Override
	public void onCommandInvocation(IDeviceCommandInvocation invocation) throws OpenIoTException {
		sendEvent(invocation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * OutboundEventProcessor#onCommandResponse(com
	 * .sitewhere.spi.device.event.IDeviceCommandResponse)
	 */
	@Override
	public void onCommandResponse(IDeviceCommandResponse response) throws OpenIoTException {
		sendEvent(response);
	}

	/**
	 * Marshals an event to JSON and sends it to EventHub via AMQP.
	 * 
	 * @param event
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void sendEvent(IDeviceEvent event) throws OpenIoTException {
		try {
            String message = new String(MarshalUtils.marshalJson(event), "UTF-8");
			sender.send(message);
		} catch (Exception e) {
			throw new OpenIoTException(e);
		}
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

	public String getSasName() {
		return sasName;
	}

	public void setSasName(String sasName) {
		this.sasName = sasName;
	}

	public String getSasKey() {
		return sasKey;
	}

	public void setSasKey(String sasKey) {
		this.sasKey = sasKey;
	}

	public String getServiceBusName() {
		return serviceBusName;
	}

	public void setServiceBusName(String serviceBusName) {
		this.serviceBusName = serviceBusName;
	}

	public String getEntityPath() {
		return entityPath;
	}

	public void setEntityPath(String entityPath) {
		this.entityPath = entityPath;
	}
}