/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning.protobuf;

import com.openiot.core.DataUtils;
import com.openiot.device.provisioning.protobuf.proto.Openiot;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.IDeviceNestingContext;
import com.openiot.spi.device.command.IDeviceCommandExecution;
import com.openiot.spi.device.command.IRegistrationAckCommand;
import com.openiot.spi.device.command.IRegistrationFailureCommand;
import com.openiot.spi.device.command.ISystemCommand;
import com.openiot.spi.device.provisioning.ICommandExecutionEncoder;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Implementation of {@link ICommandExecutionEncoder} that uses Google Protocol Buffers to
 * encode the execution.
 * 
 * @author Derek
 */
public class ProtobufExecutionEncoder extends LifecycleComponent implements ICommandExecutionEncoder<byte[]> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(ProtobufExecutionEncoder.class);

	public ProtobufExecutionEncoder() {
		super(LifecycleComponentType.CommandExecutionEncoder);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ICommandExecutionEncoder#encode(com.openiot
	 * .spi.device.command.IDeviceCommandExecution, IDevice,
	 * IDeviceAssignment)
	 */
	@Override
	public byte[] encode(IDeviceCommandExecution execution, IDeviceNestingContext nested,
			IDeviceAssignment assignment) throws OpenIoTException {
		byte[] encoded = ProtobufMessageBuilder.createMessage(execution, nested, assignment);
		LOGGER.debug("Protobuf message: 0x" + DataUtils.bytesToHex(encoded));
		return encoded;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ICommandExecutionEncoder#encodeSystemCommand
	 * (ISystemCommand,
	 * IDeviceNestingContext,
	 * IDeviceAssignment)
	 */
	@Override
	public byte[] encodeSystemCommand(ISystemCommand command, IDeviceNestingContext nested,
			IDeviceAssignment assignment) throws OpenIoTException {
		switch (command.getType()) {
		case RegistrationAck: {
			IRegistrationAckCommand ack = (IRegistrationAckCommand) command;
			Openiot.Device.RegistrationAck.Builder builder = Openiot.Device.RegistrationAck.newBuilder();
			switch (ack.getReason()) {
			case AlreadyRegistered: {
				builder.setState(Openiot.Device.RegistrationAckState.ALREADY_REGISTERED);
				break;
			}
			case NewRegistration: {
				builder.setState(Openiot.Device.RegistrationAckState.NEW_REGISTRATION);
				break;
			}
			}
			return encodeRegistrationAck(builder.build());
		}
		case RegistrationFailure: {
			IRegistrationFailureCommand fail = (IRegistrationFailureCommand) command;
			Openiot.Device.RegistrationAck.Builder builder = Openiot.Device.RegistrationAck.newBuilder();
			builder.setState(Openiot.Device.RegistrationAckState.REGISTRATION_ERROR);
			builder.setErrorMessage(fail.getErrorMessage());
			switch (fail.getReason()) {
			case NewDevicesNotAllowed: {
				builder.setErrorType(Openiot.Device.RegistrationAckError.NEW_DEVICES_NOT_ALLOWED);
				break;
			}
			case InvalidSpecificationToken: {
				builder.setErrorType(Openiot.Device.RegistrationAckError.INVALID_SPECIFICATION);
				break;
			}
			case SiteTokenRequired: {
				builder.setErrorType(Openiot.Device.RegistrationAckError.SITE_TOKEN_REQUIRED);
				break;
			}
			}
			return encodeRegistrationAck(builder.build());
		}
		}
		throw new OpenIoTException("Unable to encode command: " + command.getClass().getName());
	}

	/**
	 * Encode {@link com.openiot.device.provisioning.protobuf.proto.Openiot.Device.RegistrationAck} as a byte array.
	 * 
	 * @param ack
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected byte[] encodeRegistrationAck(Openiot.Device.RegistrationAck ack) throws OpenIoTException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Openiot.Device.Header header = Openiot.Device.Header.newBuilder().setCommand(Openiot.Device.Command.REGISTER_ACK).build();
			header.writeDelimitedTo(out);

			((Openiot.Device.RegistrationAck) ack).writeDelimitedTo(out);
			out.close();
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal regsiter ack to protobuf.", e);
		}
	}

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
}