/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hazelcast;

import com.hazelcast.core.ITopic;
import com.openiot.device.event.processor.OutboundEventProcessor;
import com.openiot.device.marshaling.DeviceCommandInvocationMarshalHelper;
import com.openiot.rest.model.device.event.*;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.*;
import com.openiot.spi.server.hazelcast.IOpenIoTHazelcast;
import org.apache.log4j.Logger;

/**
 * Sends processed device events out on Hazelcast topics for further processing.
 * 
 * @author Derek
 */
public class HazelcastEventProcessor extends OutboundEventProcessor {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(HazelcastEventProcessor.class);

	/** Common Hazelcast configuration */
	private OpenIoTHazelcastConfiguration configuration;

	/** Topic for device measurements */
	private ITopic<DeviceMeasurements> measurementsTopic;

	/** Topic for device locations */
	private ITopic<DeviceLocation> locationsTopic;

	/** Topic for device alerts */
	private ITopic<DeviceAlert> alertsTopic;

	/** Topic for device command invocations */
	private ITopic<DeviceCommandInvocation> commandInvocationsTopic;

	/** Topic for device command responses */
	private ITopic<DeviceCommandResponse> commandResponsesTopic;

	/** Used for marshaling command invocations */
	private DeviceCommandInvocationMarshalHelper invocationHelper = new DeviceCommandInvocationMarshalHelper(
			true);

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		if (getConfiguration() == null) {
			throw new OpenIoTException("No Hazelcast configuration provided.");
		}
		this.measurementsTopic =
				getConfiguration().getHazelcastInstance().getTopic(
						IOpenIoTHazelcast.TOPIC_MEASUREMENTS_ADDED);
		this.locationsTopic =
				getConfiguration().getHazelcastInstance().getTopic(IOpenIoTHazelcast.TOPIC_LOCATION_ADDED);
		this.alertsTopic =
				getConfiguration().getHazelcastInstance().getTopic(IOpenIoTHazelcast.TOPIC_ALERT_ADDED);
		this.commandInvocationsTopic =
				getConfiguration().getHazelcastInstance().getTopic(
						IOpenIoTHazelcast.TOPIC_COMMAND_INVOCATION_ADDED);
		this.commandResponsesTopic =
				getConfiguration().getHazelcastInstance().getTopic(
						IOpenIoTHazelcast.TOPIC_COMMAND_RESPONSE_ADDED);
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
	 * com.openiot.rest.model.device.event.processor.OutboundEventProcessor#onMeasurements
	 * (IDeviceMeasurements)
	 */
	@Override
	public void onMeasurements(IDeviceMeasurements measurements) throws OpenIoTException {
		DeviceMeasurements marshaled = DeviceMeasurements.copy(measurements);
		measurementsTopic.publish(marshaled);
		LOGGER.debug("Published measurements event to Hazelcast (id=" + measurements.getId() + ")");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.rest.model.device.event.processor.OutboundEventProcessor#onLocation
	 * (IDeviceLocation)
	 */
	@Override
	public void onLocation(IDeviceLocation location) throws OpenIoTException {
		DeviceLocation marshaled = DeviceLocation.copy(location);
		locationsTopic.publish(marshaled);
		LOGGER.debug("Published location event to Hazelcast (id=" + location.getId() + ")");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.rest.model.device.event.processor.OutboundEventProcessor#onAlert(
	 * IDeviceAlert)
	 */
	@Override
	public void onAlert(IDeviceAlert alert) throws OpenIoTException {
		DeviceAlert marshaled = DeviceAlert.copy(alert);
		alertsTopic.publish(marshaled);
		LOGGER.debug("Published alert event to Hazelcast (id=" + alert.getId() + ")");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.rest.model.device.event.processor.OutboundEventProcessor#
	 * onCommandInvocation(IDeviceCommandInvocation)
	 */
	@Override
	public void onCommandInvocation(IDeviceCommandInvocation invocation) throws OpenIoTException {
		DeviceCommandInvocation converted = invocationHelper.convert(invocation);
		commandInvocationsTopic.publish(converted);
		LOGGER.debug("Published command invocation event to Hazelcast (id=" + invocation.getId() + ")");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.rest.model.device.event.processor.OutboundEventProcessor#
	 * onCommandResponse(IDeviceCommandResponse)
	 */
	@Override
	public void onCommandResponse(IDeviceCommandResponse response) throws OpenIoTException {
		DeviceCommandResponse marshaled = DeviceCommandResponse.copy(response);
		commandResponsesTopic.publish(marshaled);
		LOGGER.debug("Published command response event to Hazelcast (id=" + response.getId() + ")");
	}

	public OpenIoTHazelcastConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(OpenIoTHazelcastConfiguration configuration) {
		this.configuration = configuration;
	}
}