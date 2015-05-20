/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning.mqtt;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.IDeviceNestingContext;
import com.openiot.spi.device.command.IDeviceCommandExecution;
import com.openiot.spi.device.provisioning.ICommandDeliveryParameterExtractor;

/**
 * Implements {@link ICommandDeliveryParameterExtractor} for {@link MqttParameters},
 * allowing expressions to be defined such that the device hardware id may be included in
 * the topic name to target a specific device.
 * 
 * @author Derek
 */
public class HardwareIdMqttParameterExtractor implements ICommandDeliveryParameterExtractor<MqttParameters> {

	/** Default command topic */
	public static final String DEFAULT_COMMAND_TOPIC = "OpenIoT/command/%s";

	/** Default system topic */
	public static final String DEFAULT_SYSTEM_TOPIC = "OpenIoT/system/%s";

	/** Command topic prefix */
	private String commandTopicExpr = DEFAULT_COMMAND_TOPIC;

	/** System topic prefix */
	private String systemTopicExpr = DEFAULT_SYSTEM_TOPIC;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ICommandDeliveryParameterExtractor#
	 * extractDeliveryParameters(IDeviceNestingContext,
	 * IDeviceAssignment,
	 * IDeviceCommandExecution)
	 */
	@Override
	public MqttParameters extractDeliveryParameters(IDeviceNestingContext nesting,
			IDeviceAssignment assignment, IDeviceCommandExecution execution) throws OpenIoTException {
		MqttParameters params = new MqttParameters();

		String commandTopic = String.format(getCommandTopicExpr(), nesting.getGateway().getHardwareId());
		params.setCommandTopic(commandTopic);

		String systemTopic = String.format(getSystemTopicExpr(), nesting.getGateway().getHardwareId());
		params.setSystemTopic(systemTopic);

		return params;
	}

	public String getCommandTopicExpr() {
		return commandTopicExpr;
	}

	public void setCommandTopicExpr(String commandTopicExpr) {
		this.commandTopicExpr = commandTopicExpr;
	}

	public String getSystemTopicExpr() {
		return systemTopicExpr;
	}

	public void setSystemTopicExpr(String systemTopicExpr) {
		this.systemTopicExpr = systemTopicExpr;
	}
}