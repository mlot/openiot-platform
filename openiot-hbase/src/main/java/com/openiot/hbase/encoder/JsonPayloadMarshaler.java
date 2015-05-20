/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.encoder;

import com.openiot.common.MarshalUtils;
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
 * Implementation of {@link IPayloadMarshaler} that marshals objects to JSON.
 * 
 * @author Derek
 */
public class JsonPayloadMarshaler implements IPayloadMarshaler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#getEncoding()
	 */
	@Override
	public PayloadEncoding getEncoding() throws OpenIoTException {
		return PayloadEncoding.Json;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#encode(java.lang.Object)
	 */
	@Override
	public byte[] encode(Object obj) throws OpenIoTException {
		return MarshalUtils.marshalJson(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decode(byte[], java.lang.Class)
	 */
	@Override
	public <T> T decode(byte[] payload, Class<T> type) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeSite(com.openiot.spi.device
	 * .ISite)
	 */
	@Override
	public byte[] encodeSite(ISite site) throws OpenIoTException {
		return MarshalUtils.marshalJson(site);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decodeSite(byte[])
	 */
	@Override
	public Site decodeSite(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, Site.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeZone(com.openiot.spi.device
	 * .IZone)
	 */
	@Override
	public byte[] encodeZone(IZone zone) throws OpenIoTException {
		return MarshalUtils.marshalJson(zone);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decodeZone(byte[])
	 */
	@Override
	public Zone decodeZone(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, Zone.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeDeviceSpecification(com.openiot
	 * .spi.device.IDeviceSpecification)
	 */
	@Override
	public byte[] encodeDeviceSpecification(IDeviceSpecification specification) throws OpenIoTException {
		return MarshalUtils.marshalJson(specification);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#decodeDeviceSpecification(byte[])
	 */
	@Override
	public DeviceSpecification decodeDeviceSpecification(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, DeviceSpecification.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeDevice(com.openiot.spi.device
	 * .IDevice)
	 */
	@Override
	public byte[] encodeDevice(IDevice device) throws OpenIoTException {
		return MarshalUtils.marshalJson(device);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decodeDevice(byte[])
	 */
	@Override
	public Device decodeDevice(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, Device.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeDeviceAssignment(com.openiot
	 * .spi.device.IDeviceAssignment)
	 */
	@Override
	public byte[] encodeDeviceAssignment(IDeviceAssignment assignment) throws OpenIoTException {
		return MarshalUtils.marshalJson(assignment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decodeDeviceAssignment(byte[])
	 */
	@Override
	public DeviceAssignment decodeDeviceAssignment(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, DeviceAssignment.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeDeviceAssignmentState(com.openiot
	 * .spi.device.IDeviceAssignmentState)
	 */
	@Override
	public byte[] encodeDeviceAssignmentState(IDeviceAssignmentState state) throws OpenIoTException {
		return MarshalUtils.marshalJson(state);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#decodeDeviceAssignmentState(byte[])
	 */
	@Override
	public DeviceAssignmentState decodeDeviceAssignmentState(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, DeviceAssignmentState.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeDeviceMeasurements(com.openiot
	 * .spi.device.event.IDeviceMeasurements)
	 */
	@Override
	public byte[] encodeDeviceMeasurements(IDeviceMeasurements measurements) throws OpenIoTException {
		return MarshalUtils.marshalJson(measurements);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decodeDeviceMeasurements(byte[])
	 */
	@Override
	public DeviceMeasurements decodeDeviceMeasurements(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, DeviceMeasurements.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeDeviceLocation(com.openiot
	 * .spi.device.event.IDeviceLocation)
	 */
	@Override
	public byte[] encodeDeviceLocation(IDeviceLocation location) throws OpenIoTException {
		return MarshalUtils.marshalJson(location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decodeDeviceLocation(byte[])
	 */
	@Override
	public DeviceLocation decodeDeviceLocation(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, DeviceLocation.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeDeviceAlert(com.openiot.spi
	 * .device.event.IDeviceAlert)
	 */
	@Override
	public byte[] encodeDeviceAlert(IDeviceAlert alert) throws OpenIoTException {
		return MarshalUtils.marshalJson(alert);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decodeDeviceAlert(byte[])
	 */
	@Override
	public DeviceAlert decodeDeviceAlert(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, DeviceAlert.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeDeviceCommandInvocation(com
	 * .sitewhere.spi.device.event.IDeviceCommandInvocation)
	 */
	@Override
	public byte[] encodeDeviceCommandInvocation(IDeviceCommandInvocation invocation)
			throws OpenIoTException {
		return MarshalUtils.marshalJson(invocation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#decodeDeviceCommandInvocation(byte[])
	 */
	@Override
	public DeviceCommandInvocation decodeDeviceCommandInvocation(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, DeviceCommandInvocation.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeDeviceStateChange(com.openiot
	 * .spi.device.event.IDeviceStateChange)
	 */
	@Override
	public byte[] encodeDeviceStateChange(IDeviceStateChange change) throws OpenIoTException {
		return MarshalUtils.marshalJson(change);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decodeDeviceStateChange(byte[])
	 */
	@Override
	public DeviceStateChange decodeDeviceStateChange(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, DeviceStateChange.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeDeviceCommandResponse(com.openiot
	 * .spi.device.event.IDeviceCommandResponse)
	 */
	@Override
	public byte[] encodeDeviceCommandResponse(IDeviceCommandResponse response) throws OpenIoTException {
		return MarshalUtils.marshalJson(response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#decodeDeviceCommandResponse(byte[])
	 */
	@Override
	public DeviceCommandResponse decodeDeviceCommandResponse(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, DeviceCommandResponse.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeBatchOperation(com.openiot
	 * .spi.device.batch.IBatchOperation)
	 */
	@Override
	public byte[] encodeBatchOperation(IBatchOperation operation) throws OpenIoTException {
		return MarshalUtils.marshalJson(operation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decodeBatchOperation(byte[])
	 */
	@Override
	public BatchOperation decodeBatchOperation(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, BatchOperation.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeBatchElement(com.openiot.
	 * spi.device.batch.IBatchElement)
	 */
	@Override
	public byte[] encodeBatchElement(IBatchElement element) throws OpenIoTException {
		return MarshalUtils.marshalJson(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decodeBatchElement(byte[])
	 */
	@Override
	public BatchElement decodeBatchElement(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, BatchElement.class);
	}

	/* (non-Javadoc)
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#encodeDeviceGroup(IDeviceGroup)
	 */
	@Override
	public byte[] encodeDeviceGroup(IDeviceGroup group) throws OpenIoTException {
		return MarshalUtils.marshalJson(group);
	}

	/* (non-Javadoc)
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decodeDeviceGroup(byte[])
	 */
	@Override
	public DeviceGroup decodeDeviceGroup(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, DeviceGroup.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeDeviceGroupElement(com.openiot
	 * .spi.device.group.IDeviceGroupElement)
	 */
	@Override
	public byte[] encodeDeviceGroupElement(IDeviceGroupElement element) throws OpenIoTException {
		return MarshalUtils.marshalJson(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decodeDeviceGroupElement(byte[])
	 */
	@Override
	public DeviceGroupElement decodeDeviceGroupElement(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, DeviceGroupElement.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeDeviceCommand(com.openiot
	 * .spi.device.command.IDeviceCommand)
	 */
	@Override
	public byte[] encodeDeviceCommand(IDeviceCommand command) throws OpenIoTException {
		return MarshalUtils.marshalJson(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decodeDeviceCommand(byte[])
	 */
	@Override
	public DeviceCommand decodeDeviceCommand(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, DeviceCommand.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeUser(com.openiot.spi.user
	 * .IUser)
	 */
	@Override
	public byte[] encodeUser(IUser user) throws OpenIoTException {
		return MarshalUtils.marshalJson(user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decodeUser(byte[])
	 */
	@Override
	public User decodeUser(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, User.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.IPayloadMarshaler#encodeGrantedAuthority(com.openiot
	 * .spi.user.IGrantedAuthority)
	 */
	@Override
	public byte[] encodeGrantedAuthority(IGrantedAuthority auth) throws OpenIoTException {
		return MarshalUtils.marshalJson(auth);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#decodeGrantedAuthority(byte[])
	 */
	@Override
	public GrantedAuthority decodeGrantedAuthority(byte[] payload) throws OpenIoTException {
		return MarshalUtils.unmarshalJson(payload, GrantedAuthority.class);
	}
}