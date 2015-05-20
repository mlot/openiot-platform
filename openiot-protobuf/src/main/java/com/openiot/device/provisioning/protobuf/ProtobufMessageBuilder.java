/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning.protobuf;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.openiot.OpenIoT;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.IDeviceNestingContext;
import com.openiot.spi.device.IDeviceSpecification;
import com.openiot.spi.device.command.IDeviceCommandExecution;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Produces an encoded message based on Google Protocol Buffer derived from an
 * {@link IDeviceSpecification}.
 * 
 * @author Derek
 */
public class ProtobufMessageBuilder {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(ProtobufMessageBuilder.class);

	/**
	 * Create a protobuf message for an {@link IDeviceCommandExecution} targeted at the
	 * given {@link IDeviceAssignment}.
	 * 
	 * @param execution
	 * @param nested
	 * @param assignment
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static byte[] createMessage(IDeviceCommandExecution execution, IDeviceNestingContext nested,
			IDeviceAssignment assignment) throws OpenIoTException {
		IDeviceSpecification specification =
				OpenIoT.getServer().getDeviceManagement().getDeviceSpecificationByToken(
						execution.getCommand().getSpecificationToken());
		DescriptorProtos.FileDescriptorProto fdproto = getFileDescriptor(specification);
		LOGGER.debug("Using the following specification proto:\n" + fdproto.toString());
		Descriptors.FileDescriptor[] fdescs = new Descriptors.FileDescriptor[0];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			Descriptors.FileDescriptor filedesc = Descriptors.FileDescriptor.buildFrom(fdproto, fdescs);
			Descriptors.Descriptor mdesc =
					filedesc.findMessageTypeByName(ProtobufNaming.getSpecificationIdentifier(specification));

			// Create the header message.
			Descriptors.Descriptor header = mdesc.findNestedTypeByName(ProtobufNaming.HEADER_MSG_NAME);
			DynamicMessage.Builder headBuilder = DynamicMessage.newBuilder(header);

			// Set enum value based on command.
			Descriptors.EnumDescriptor enumDesc = mdesc.findEnumTypeByName(ProtobufNaming.COMMAND_TYPES_ENUM);
			Descriptors.EnumValueDescriptor enumValue =
					enumDesc.findValueByName(ProtobufNaming.getCommandEnumName(execution.getCommand()));
			if (enumValue == null) {
				throw new OpenIoTException("No enum value found for command: "
						+ execution.getCommand().getName());
			}
			headBuilder.setField(header.findFieldByName(ProtobufNaming.HEADER_COMMAND_FIELD_NAME), enumValue);
			headBuilder.setField(header.findFieldByName(ProtobufNaming.HEADER_ORIGINATOR_FIELD_NAME),
					execution.getInvocation().getId());

			if (nested.getNested() != null) {
				LOGGER.debug("Targeting nested device with specification: "
						+ nested.getNested().getSpecificationToken() + " at path " + nested.getPath());
				headBuilder.setField(header.findFieldByName(ProtobufNaming.HEADER_NESTED_PATH_FIELD_NAME),
						nested.getPath());
				headBuilder.setField(header.findFieldByName(ProtobufNaming.HEADER_NESTED_SPEC_FIELD_NAME),
						nested.getNested().getSpecificationToken());
			}

			DynamicMessage hmessage = headBuilder.build();
			LOGGER.debug("Header:\n" + hmessage.toString());
			hmessage.writeDelimitedTo(out);

			// Find nested type for command and create/populate an instance.
			Descriptors.Descriptor command = mdesc.findNestedTypeByName(execution.getCommand().getName());
			DynamicMessage.Builder cbuilder = DynamicMessage.newBuilder(command);

			// Set each field in the command message.
			for (String name : execution.getParameters().keySet()) {
				Object value = execution.getParameters().get(name);
				Descriptors.FieldDescriptor field = command.findFieldByName(name);
				if (field == null) {
					throw new OpenIoTException("Command parameter '" + name
							+ "' not found in specification: ");
				}
				try {
					cbuilder.setField(field, value);
				} catch (IllegalArgumentException iae) {
					LOGGER.error("Error setting field '" + name + "' with object of type: "
							+ value.getClass().getName(), iae);
				}
			}
			DynamicMessage cmessage = cbuilder.build();
			LOGGER.debug("Message:\n" + cmessage.toString());
			cmessage.writeDelimitedTo(out);

			return out.toByteArray();
		} catch (Descriptors.DescriptorValidationException e) {
			throw new OpenIoTException("Unable to create protobuf message.", e);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to encode protobuf message.", e);
		}
	}

	/**
	 * Gets a file descriptor for protobuf representation of {@link IDeviceSpecification}.
	 * 
	 * @param specification
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static DescriptorProtos.FileDescriptorProto getFileDescriptor(IDeviceSpecification specification)
			throws OpenIoTException {
		return ProtobufSpecificationBuilder.createFileDescriptor(specification);
	}
}