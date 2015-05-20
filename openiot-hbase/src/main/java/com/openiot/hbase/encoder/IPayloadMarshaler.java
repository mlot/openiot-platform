/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.encoder;

import com.openiot.rest.model.device.*;
import com.openiot.rest.model.device.batch.BatchElement;
import com.openiot.rest.model.device.batch.BatchOperation;
import com.openiot.rest.model.device.command.DeviceCommand;
import com.openiot.rest.model.device.event.*;
import com.openiot.rest.model.device.group.DeviceGroup;
import com.openiot.rest.model.device.group.DeviceGroupElement;
import com.openiot.rest.model.user.GrantedAuthority;
import com.openiot.rest.model.user.User;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.*;
import com.openiot.spi.device.batch.IBatchElement;
import com.openiot.spi.device.batch.IBatchOperation;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.event.*;
import com.openiot.spi.device.group.IDeviceGroup;
import com.openiot.spi.device.group.IDeviceGroupElement;
import com.openiot.spi.user.IGrantedAuthority;
import com.openiot.spi.user.IUser;

/**
 * Interface for classes that can encode OpenIoT objects into byte arrays.
 * 
 * @author Derek
 */
public interface IPayloadMarshaler {

	/**
	 * Gets encoding type for the encoder.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public PayloadEncoding getEncoding() throws OpenIoTException;

	/**
	 * Encode an object.
	 * 
	 * @param obj
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encode(Object obj) throws OpenIoTException;

	/**
	 * Decode a payload into an object.
	 * 
	 * @param payload
	 * @param type
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public <T> T decode(byte[] payload, Class<T> type) throws OpenIoTException;

	/**
	 * Encode an {@link ISite}.
	 * 
	 * @param site
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeSite(ISite site) throws OpenIoTException;

	/**
	 * Decode a {@link Site} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public Site decodeSite(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IZone}.
	 * 
	 * @param zone
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeZone(IZone zone) throws OpenIoTException;

	/**
	 * Decode a {@link Zone} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public Zone decodeZone(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IDeviceSpecification}.
	 * 
	 * @param specification
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeDeviceSpecification(IDeviceSpecification specification) throws OpenIoTException;

	/**
	 * Decode a {@link DeviceSpecification} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public DeviceSpecification decodeDeviceSpecification(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IDevice}.
	 * 
	 * @param device
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeDevice(IDevice device) throws OpenIoTException;

	/**
	 * Decodea {@link Device} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public Device decodeDevice(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IDeviceAssignment}.
	 * 
	 * @param assignment
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeDeviceAssignment(IDeviceAssignment assignment) throws OpenIoTException;

	/**
	 * Decode a {@link DeviceAssignment} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public DeviceAssignment decodeDeviceAssignment(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IDeviceAssignmentState}.
	 * 
	 * @param state
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeDeviceAssignmentState(IDeviceAssignmentState state) throws OpenIoTException;

	/**
	 * Decode a {@link DeviceAssignmentState} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public DeviceAssignmentState decodeDeviceAssignmentState(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IDeviceMeasurements}.
	 * 
	 * @param measurements
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeDeviceMeasurements(IDeviceMeasurements measurements) throws OpenIoTException;

	/**
	 * Decode a {@link DeviceMeasurements} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public DeviceMeasurements decodeDeviceMeasurements(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IDeviceLocation}.
	 * 
	 * @param location
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeDeviceLocation(IDeviceLocation location) throws OpenIoTException;

	/**
	 * Decode a {@link DeviceLocation} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public DeviceLocation decodeDeviceLocation(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IDeviceAlert}.
	 * 
	 * @param alert
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeDeviceAlert(IDeviceAlert alert) throws OpenIoTException;

	/**
	 * Decode a {@link DeviceAlert} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public DeviceAlert decodeDeviceAlert(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IDeviceCommandInvocation}.
	 * 
	 * @param invocation
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeDeviceCommandInvocation(IDeviceCommandInvocation invocation)
			throws OpenIoTException;

	/**
	 * Decode a {@link DeviceCommandInvocation} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public DeviceCommandInvocation decodeDeviceCommandInvocation(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IDeviceStateChange}.
	 * 
	 * @param change
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeDeviceStateChange(IDeviceStateChange change) throws OpenIoTException;

	/**
	 * Decode a {@link DeviceStateChange} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public DeviceStateChange decodeDeviceStateChange(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IDeviceCommandResponse}.
	 * 
	 * @param response
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeDeviceCommandResponse(IDeviceCommandResponse response) throws OpenIoTException;

	/**
	 * Decode a {@link DeviceCommandResponse} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public DeviceCommandResponse decodeDeviceCommandResponse(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IBatchOperation}.
	 * 
	 * @param operation
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeBatchOperation(IBatchOperation operation) throws OpenIoTException;

	/**
	 * Decode a {@link BatchOperation} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public BatchOperation decodeBatchOperation(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IBatchElement}.
	 * 
	 * @param element
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeBatchElement(IBatchElement element) throws OpenIoTException;

	/**
	 * Decode a {@link BatchElement} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public BatchElement decodeBatchElement(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IDeviceGroup}.
	 * 
	 * @param group
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeDeviceGroup(IDeviceGroup group) throws OpenIoTException;

	/**
	 * Decode a {@link DeviceGroup} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public DeviceGroup decodeDeviceGroup(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IDeviceGroupElement}.
	 * 
	 * @param element
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeDeviceGroupElement(IDeviceGroupElement element) throws OpenIoTException;

	/**
	 * Decode a {@link DeviceGroupElement} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public DeviceGroupElement decodeDeviceGroupElement(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IDeviceCommand}.
	 * 
	 * @param command
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeDeviceCommand(IDeviceCommand command) throws OpenIoTException;

	/**
	 * Decode a {@link DeviceCommand} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public DeviceCommand decodeDeviceCommand(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IUser}.
	 * 
	 * @param user
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeUser(IUser user) throws OpenIoTException;

	/**
	 * Decode a {@link UserError} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public User decodeUser(byte[] payload) throws OpenIoTException;

	/**
	 * Encode an {@link IGrantedAuthority}.
	 * 
	 * @param auth
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] encodeGrantedAuthority(IGrantedAuthority auth) throws OpenIoTException;

	/**
	 * Encode a {@link GrantedAuthority} from the binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public GrantedAuthority decodeGrantedAuthority(byte[] payload) throws OpenIoTException;
}