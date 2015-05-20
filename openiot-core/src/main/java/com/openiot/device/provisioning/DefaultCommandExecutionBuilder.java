/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning;

import com.openiot.rest.model.device.command.DeviceCommandExecution;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.command.ICommandParameter;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.command.IDeviceCommandExecution;
import com.openiot.spi.device.event.IDeviceCommandInvocation;
import com.openiot.spi.device.provisioning.ICommandExecutionBuilder;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;

/**
 * Default implementation of the {@link ICommandExecutionBuilder} interface that handles
 * the basic task of merging {@link IDeviceCommand} and {@link IDeviceCommandInvocation}
 * information to produce an {@link IDeviceCommandExecution} that can be encoded and sent
 * to a target.
 * 
 * @author Derek
 */
public class DefaultCommandExecutionBuilder extends LifecycleComponent implements ICommandExecutionBuilder {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(DefaultCommandExecutionBuilder.class);

	public DefaultCommandExecutionBuilder() {
		super(LifecycleComponentType.CommandExecutionBuilder);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ICommandExecutionBuilder#createExecution(
	 * IDeviceCommand,
	 * IDeviceCommandInvocation)
	 */
	@Override
	public IDeviceCommandExecution createExecution(IDeviceCommand command, IDeviceCommandInvocation invocation)
			throws OpenIoTException {
		LOGGER.debug("Building default command execution for invocation.");
		DeviceCommandExecution execution = new DeviceCommandExecution();
		execution.setCommand(command);
		execution.setInvocation(invocation);
		generateParameters(execution);
		return execution;
	}

	/**
	 * Generate a parameters map based on information from the command and invocation.
	 * 
	 * @param execution
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void generateParameters(IDeviceCommandExecution execution) throws OpenIoTException {
		execution.getParameters().clear();
		for (ICommandParameter parameter : execution.getCommand().getParameters()) {
			String paramValue = execution.getInvocation().getParameterValues().get(parameter.getName());
			if (parameter.isRequired() && (paramValue == null)) {
				throw new OpenIoTSystemException(ErrorCode.RequiredCommandParameterMissing,
						ErrorLevel.ERROR);
			}
			Object converted = null;
			switch (parameter.getType()) {
			case Bool: {
				converted = Boolean.parseBoolean(paramValue);
				break;
			}
			case String: {
				converted = paramValue;
				break;
			}
			case Bytes: {
				converted = String.valueOf(converted).getBytes();
				break;
			}
			case Double: {
				try {
					converted = Double.parseDouble(paramValue);
				} catch (NumberFormatException e) {
					throw new OpenIoTException("Field '" + parameter.getName()
							+ "' contains a value that can not be parsed as a double.");
				}
				break;
			}
			case Float: {
				try {
					converted = Float.parseFloat(paramValue);
				} catch (NumberFormatException e) {
					throw new OpenIoTException("Field '" + parameter.getName()
							+ "' contains a value that can not be parsed as a float.");
				}
				break;
			}
			case Int32:
			case UInt32:
			case SInt32:
			case Fixed32:
			case SFixed32: {
				try {
					converted = Integer.parseInt(paramValue);
				} catch (NumberFormatException e) {
					throw new OpenIoTException("Field '" + parameter.getName()
							+ "' contains a value that can not be parsed as an integer.");
				}
				break;
			}
			case Int64:
			case UInt64:
			case SInt64:
			case Fixed64:
			case SFixed64: {
				try {
					converted = Long.parseLong(paramValue);
				} catch (NumberFormatException e) {
					throw new OpenIoTException("Field '" + parameter.getName()
							+ "' contains a value that can not be parsed as an long.");
				}
				break;
			}
			default: {
				throw new OpenIoTException("Unhandled parameter type: " + parameter.getType().name());
			}

			}
			if (converted != null) {
				execution.getParameters().put(parameter.getName(), converted);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		LOGGER.info("Started command execution builder.");
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
		LOGGER.info("Stopped command execution builder.");
	}
}