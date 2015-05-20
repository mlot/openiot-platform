/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning.protobuf;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.openiot.OpenIoT;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDeviceSpecification;
import com.openiot.spi.device.command.ICommandParameter;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.command.ParameterType;

import java.util.List;

/**
 * Builds Google Protocol Buffer data structures that allow commands for a specification
 * to be encoded.
 * 
 * @author Derek
 */
public class ProtobufSpecificationBuilder {

	/**
	 * Creates a {@link FileDescriptorProto} based on an {@link IDeviceSpecification}.
	 * 
	 * @param specification
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DescriptorProtos.FileDescriptorProto createFileDescriptor(IDeviceSpecification specification)
			throws OpenIoTException {
		DescriptorProtos.FileDescriptorProto.Builder builder =
				DescriptorProtos.FileDescriptorProto.newBuilder();
		builder.addMessageType(createSpecificationMessage(specification));
		return builder.build();
	}

	/**
	 * Create the message for a specification.
	 * 
	 * @param specification
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DescriptorProtos.DescriptorProto createSpecificationMessage(
			IDeviceSpecification specification) throws OpenIoTException {
		List<IDeviceCommand> commands =
				OpenIoT.getServer().getDeviceManagement().listDeviceCommands(specification.getToken(),
						false);
		DescriptorProtos.DescriptorProto.Builder builder = DescriptorProtos.DescriptorProto.newBuilder();
		builder.setName(ProtobufNaming.getSpecificationIdentifier(specification));
		builder.addEnumType(createCommandsEnum(commands));
		builder.addNestedType(createUuidMessage());
		builder.addNestedType(createHeaderMessage());

		for (IDeviceCommand command : commands) {
			builder.addNestedType(createCommandMessage(command)).build();
		}

		return builder.build();
	}

	/**
	 * Create an enum that lists all commands.
	 * 
	 * @param commands
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DescriptorProtos.EnumDescriptorProto createCommandsEnum(List<IDeviceCommand> commands)
			throws OpenIoTException {
		DescriptorProtos.EnumDescriptorProto.Builder builder =
				DescriptorProtos.EnumDescriptorProto.newBuilder();
		builder.setName(ProtobufNaming.COMMAND_TYPES_ENUM);
		int i = 1;
		for (IDeviceCommand command : commands) {
			DescriptorProtos.EnumValueDescriptorProto.Builder valueBuilder =
					DescriptorProtos.EnumValueDescriptorProto.newBuilder();
			valueBuilder.setName(ProtobufNaming.getCommandEnumName(command));
			valueBuilder.setNumber(i++);
			builder.addValue(valueBuilder.build());
		}
		return builder.build();
	}

	/**
	 * Create message that defines a UUID in terms of two int64 fields.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DescriptorProtos.DescriptorProto createUuidMessage() throws OpenIoTException {
		DescriptorProtos.DescriptorProto.Builder builder = DescriptorProtos.DescriptorProto.newBuilder();
		builder.setName(ProtobufNaming.UUID_MSG_NAME);
		DescriptorProtos.FieldDescriptorProto.Builder lsb =
				DescriptorProtos.FieldDescriptorProto.newBuilder().setName("lsb").setNumber(1).setType(
						Type.TYPE_INT64);
		builder.addField(lsb.build());
		DescriptorProtos.FieldDescriptorProto.Builder msb =
				DescriptorProtos.FieldDescriptorProto.newBuilder().setName("msb").setNumber(2).setType(
						Type.TYPE_INT64);
		builder.addField(msb.build());
		return builder.build();
	}

	/**
	 * Create header message.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DescriptorProtos.DescriptorProto createHeaderMessage() throws OpenIoTException {
		DescriptorProtos.DescriptorProto.Builder builder = DescriptorProtos.DescriptorProto.newBuilder();
		builder.setName(ProtobufNaming.HEADER_MSG_NAME);
		DescriptorProtos.FieldDescriptorProto.Builder command =
				DescriptorProtos.FieldDescriptorProto.newBuilder().setName(
						ProtobufNaming.HEADER_COMMAND_FIELD_NAME).setNumber(1).setTypeName(
						ProtobufNaming.COMMAND_TYPES_ENUM);
		builder.addField(command.build());
		DescriptorProtos.FieldDescriptorProto.Builder originator =
				DescriptorProtos.FieldDescriptorProto.newBuilder().setName(
						ProtobufNaming.HEADER_ORIGINATOR_FIELD_NAME).setNumber(2).setType(Type.TYPE_STRING);
		builder.addField(originator.build());
		DescriptorProtos.FieldDescriptorProto.Builder path =
				DescriptorProtos.FieldDescriptorProto.newBuilder().setName(
						ProtobufNaming.HEADER_NESTED_PATH_FIELD_NAME).setNumber(3).setType(Type.TYPE_STRING);
		builder.addField(path.build());
		DescriptorProtos.FieldDescriptorProto.Builder target =
				DescriptorProtos.FieldDescriptorProto.newBuilder().setName(
						ProtobufNaming.HEADER_NESTED_SPEC_FIELD_NAME).setNumber(4).setType(Type.TYPE_STRING);
		builder.addField(target.build());
		return builder.build();
	}

	/**
	 * Create a descriptor from an {@link IDeviceCommand}.
	 * 
	 * @param command
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DescriptorProtos.DescriptorProto createCommandMessage(IDeviceCommand command)
			throws OpenIoTException {
		DescriptorProtos.DescriptorProto.Builder builder = DescriptorProtos.DescriptorProto.newBuilder();
		builder.setName(command.getName());

		int i = 0;
		for (ICommandParameter parameter : command.getParameters()) {
			i++;
			DescriptorProtos.FieldDescriptorProto field = createField(parameter, i);
			builder.addField(field);
		}
		return builder.build();
	}

	/**
	 * Create field for a parameter.
	 * 
	 * @param parameter
	 * @param fieldNumber
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DescriptorProtos.FieldDescriptorProto createField(ICommandParameter parameter,
			int fieldNumber) throws OpenIoTException {
		DescriptorProtos.FieldDescriptorProto.Builder builder =
				DescriptorProtos.FieldDescriptorProto.newBuilder().setName(parameter.getName()).setNumber(
						fieldNumber).setType(getType(parameter.getType()));
		return builder.build();
	}

	/**
	 * Gets the protobuf parameter type based on OpenIoT parameter type.
	 * 
	 * @param param
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static DescriptorProtos.FieldDescriptorProto.Type getType(ParameterType param)
			throws OpenIoTException {
		switch (param) {
		case Bool:
			return DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL;
		case Bytes:
			return DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES;
		case Double:
			return DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE;
		case Fixed32:
			return DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32;
		case Fixed64:
			return DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64;
		case Float:
			return DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT;
		case Int32:
			return DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32;
		case Int64:
			return DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64;
		case SFixed32:
			return DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32;
		case SFixed64:
			return DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64;
		case SInt32:
			return DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32;
		case SInt64:
			return DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64;
		case String:
			return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
		case UInt32:
			return DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32;
		case UInt64:
			return DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64;
		default:
			throw new OpenIoTException("Unknown parameter type: " + param.name());
		}
	}
}