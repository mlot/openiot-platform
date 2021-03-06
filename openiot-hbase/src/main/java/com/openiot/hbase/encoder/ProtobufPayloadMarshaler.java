/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.encoder;

import com.openiot.rest.model.common.Location;
import com.openiot.rest.model.common.MetadataProviderEntity;
import com.openiot.rest.model.device.*;
import com.openiot.rest.model.device.batch.BatchElement;
import com.openiot.rest.model.device.batch.BatchOperation;
import com.openiot.rest.model.device.command.CommandParameter;
import com.openiot.rest.model.device.command.DeviceCommand;
import com.openiot.rest.model.device.element.DeviceElementSchema;
import com.openiot.rest.model.device.element.DeviceSlot;
import com.openiot.rest.model.device.element.DeviceUnit;
import com.openiot.rest.model.device.event.*;
import com.openiot.rest.model.device.group.DeviceGroup;
import com.openiot.rest.model.device.group.DeviceGroupElement;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.common.ILocation;
import com.openiot.spi.common.IMetadataProviderEntity;
import com.openiot.spi.device.*;
import com.openiot.spi.device.batch.*;
import com.openiot.spi.device.command.ICommandParameter;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.command.ParameterType;
import com.openiot.spi.device.element.IDeviceElementSchema;
import com.openiot.spi.device.element.IDeviceSlot;
import com.openiot.spi.device.element.IDeviceUnit;
import com.openiot.spi.device.event.*;
import com.openiot.spi.device.group.GroupElementType;
import com.openiot.spi.device.group.IDeviceGroup;
import com.openiot.spi.device.group.IDeviceGroupElement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link IPayloadMarshaler} that uses Google Protocol Buffers to store
 * data in a binary format.
 * 
 * @author Derek
 */
public class ProtobufPayloadMarshaler extends JsonPayloadMarshaler implements IPayloadMarshaler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.IPayloadMarshaler#getEncoding()
	 */
	@Override
	public PayloadEncoding getEncoding() throws OpenIoTException {
		return PayloadEncoding.ProtocolBuffers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.JsonPayloadMarshaler#encode(java.lang.Object)
	 */
	@Override
	public byte[] encode(Object obj) throws OpenIoTException {
		if (obj instanceof ISite) {
			return encodeSite((ISite) obj);
		} else if (obj instanceof IZone) {
			return encodeZone((IZone) obj);
		} else if (obj instanceof IDeviceSpecification) {
			return encodeDeviceSpecification((IDeviceSpecification) obj);
		} else if (obj instanceof IDeviceCommand) {
			return encodeDeviceCommand((IDeviceCommand) obj);
		} else if (obj instanceof IDevice) {
			return encodeDevice((IDevice) obj);
		} else if (obj instanceof IDeviceAssignment) {
			return encodeDeviceAssignment((IDeviceAssignment) obj);
		} else if (obj instanceof IDeviceAssignmentState) {
			return encodeDeviceAssignmentState((IDeviceAssignmentState) obj);
		} else if (obj instanceof IDeviceLocation) {
			return encodeDeviceLocation((IDeviceLocation) obj);
		} else if (obj instanceof IDeviceMeasurements) {
			return encodeDeviceMeasurements((IDeviceMeasurements) obj);
		} else if (obj instanceof IDeviceAlert) {
			return encodeDeviceAlert((IDeviceAlert) obj);
		} else if (obj instanceof IDeviceCommandInvocation) {
			return encodeDeviceCommandInvocation((IDeviceCommandInvocation) obj);
		} else if (obj instanceof IDeviceCommandResponse) {
			return encodeDeviceCommandResponse((IDeviceCommandResponse) obj);
		} else if (obj instanceof IDeviceGroup) {
			return encodeDeviceGroup((IDeviceGroup) obj);
		} else if (obj instanceof IDeviceGroupElement) {
			return encodeDeviceGroupElement((IDeviceGroupElement) obj);
		} else if (obj instanceof IBatchOperation) {
			return encodeBatchOperation((IBatchOperation) obj);
		} else if (obj instanceof IBatchElement) {
			return encodeBatchElement((IBatchElement) obj);
		}
		return super.encode(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.JsonPayloadMarshaler#decode(byte[],
	 * java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T decode(byte[] payload, Class<T> type) throws OpenIoTException {
		if (ISite.class.isAssignableFrom(type)) {
			return (T) decodeSite(payload);
		}
		if (IZone.class.isAssignableFrom(type)) {
			return (T) decodeZone(payload);
		}
		if (IDeviceSpecification.class.isAssignableFrom(type)) {
			return (T) decodeDeviceSpecification(payload);
		}
		if (IDeviceCommand.class.isAssignableFrom(type)) {
			return (T) decodeDeviceCommand(payload);
		}
		if (IDevice.class.isAssignableFrom(type)) {
			return (T) decodeDevice(payload);
		}
		if (IDeviceAssignment.class.isAssignableFrom(type)) {
			return (T) decodeDeviceAssignment(payload);
		}
		if (IDeviceAssignmentState.class.isAssignableFrom(type)) {
			return (T) decodeDeviceAssignmentState(payload);
		}
		if (IDeviceLocation.class.isAssignableFrom(type)) {
			return (T) decodeDeviceLocation(payload);
		}
		if (IDeviceMeasurements.class.isAssignableFrom(type)) {
			return (T) decodeDeviceMeasurements(payload);
		}
		if (IDeviceAlert.class.isAssignableFrom(type)) {
			return (T) decodeDeviceAlert(payload);
		}
		if (IDeviceCommandInvocation.class.isAssignableFrom(type)) {
			return (T) decodeDeviceCommandInvocation(payload);
		}
		if (IDeviceCommandResponse.class.isAssignableFrom(type)) {
			return (T) decodeDeviceCommandResponse(payload);
		}
		if (IDeviceGroup.class.isAssignableFrom(type)) {
			return (T) decodeDeviceGroup(payload);
		}
		if (IDeviceGroupElement.class.isAssignableFrom(type)) {
			return (T) decodeDeviceGroupElement(payload);
		}
		if (IBatchOperation.class.isAssignableFrom(type)) {
			return (T) decodeBatchOperation(payload);
		}
		if (IBatchElement.class.isAssignableFrom(type)) {
			return (T) decodeBatchElement(payload);
		}
		return super.decode(payload, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeSite(com.openiot.spi.device
	 * .ISite)
	 */
	@Override
	public byte[] encodeSite(ISite site) throws OpenIoTException {
		ProtobufMarshaler.Site.Builder builder = ProtobufMarshaler.Site.newBuilder();
		builder.setToken(site.getToken());
		builder.setName(site.getName());
		builder.setDescription(site.getDescription());
		builder.setImageUrl(site.getImageUrl());
		builder.setMapType(site.getMap().getType());
		List<ProtobufMarshaler.MetadataEntry> mapEntries = asMetadataEntries(site.getMap().getMetadata());
		builder.addAllMapMetadata(mapEntries);
		builder.setEntityData(createEntityData(site));
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			builder.build().writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal site.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeSite(byte[])
	 */
	@Override
	public Site decodeSite(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.Site pb = ProtobufMarshaler.Site.parseFrom(stream);
			Site site = new Site();
			site.setToken(pb.getToken());
			site.setName(pb.getName());
			site.setDescription(pb.getDescription());
			site.setImageUrl(pb.getImageUrl());
			SiteMapData map = new SiteMapData();
			map.setType(pb.getMapType());
			for (ProtobufMarshaler.MetadataEntry entry : pb.getMapMetadataList()) {
				map.addOrReplaceMetadata(entry.getName(), entry.getValue());
			}
			site.setMap(map);
			loadEntityData(pb.getEntityData(), site);
			return site;
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal site.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeZone(com.openiot.spi.device
	 * .IZone)
	 */
	@Override
	public byte[] encodeZone(IZone zone) throws OpenIoTException {
		ProtobufMarshaler.Zone.Builder builder = ProtobufMarshaler.Zone.newBuilder();
		builder.setToken(zone.getToken());
		builder.setSiteToken(zone.getSiteToken());
		builder.setName(zone.getName());
		for (ILocation location : zone.getCoordinates()) {
			ProtobufMarshaler.Location.Builder lbuilder = ProtobufMarshaler.Location.newBuilder();
			lbuilder.setLatitude(location.getLatitude());
			lbuilder.setLongitude(location.getLongitude());
			if (location.getElevation() != null) {
				lbuilder.setElevation(location.getElevation());
			}
			builder.addCoordinates(lbuilder.build());
		}
		builder.setBorderColor(zone.getBorderColor());
		builder.setFillColor(zone.getFillColor());
		builder.setOpacity(zone.getOpacity());
		builder.setEntityData(createEntityData(zone));
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			builder.build().writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal site.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeZone(byte[])
	 */
	@Override
	public Zone decodeZone(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.Zone pb = ProtobufMarshaler.Zone.parseFrom(stream);
			Zone zone = new Zone();
			zone.setToken(pb.getToken());
			zone.setSiteToken(pb.getSiteToken());
			zone.setName(pb.getName());
			for (ProtobufMarshaler.Location pbloc : pb.getCoordinatesList()) {
				Location location = new Location();
				location.setLatitude(pbloc.getLatitude());
				location.setLongitude(pbloc.getLongitude());
				if (pbloc.hasElevation()) {
					location.setElevation(pbloc.getElevation());
				}
				zone.getCoordinates().add(location);
			}
			zone.setBorderColor(pb.getBorderColor());
			zone.setFillColor(pb.getFillColor());
			zone.setOpacity(pb.getOpacity());
			loadEntityData(pb.getEntityData(), zone);
			return zone;
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal site.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeDeviceSpecification(com.
	 * sitewhere.spi.device.IDeviceSpecification)
	 */
	@Override
	public byte[] encodeDeviceSpecification(IDeviceSpecification spec) throws OpenIoTException {
		ProtobufMarshaler.DeviceSpecification.Builder builder =
				ProtobufMarshaler.DeviceSpecification.newBuilder();
		builder.setToken(spec.getToken());
		builder.setName(spec.getName());
		builder.setAssetModuleId(spec.getAssetModuleId());
		builder.setAssetId(spec.getAssetId());
		builder.setContainerPolicy(spec.getContainerPolicy().name());
		if (spec.getDeviceElementSchema() != null) {
			builder.setDeviceElementSchema(encodeDeviceElementSchema(spec.getDeviceElementSchema()));
		}
		builder.setEntityData(createEntityData(spec));
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			builder.build().writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal device specification.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeDeviceSpecification(byte[])
	 */
	@Override
	public DeviceSpecification decodeDeviceSpecification(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.DeviceSpecification pb =
					ProtobufMarshaler.DeviceSpecification.parseFrom(stream);
			DeviceSpecification spec = new DeviceSpecification();
			spec.setToken(pb.getToken());
			spec.setName(pb.getName());
			spec.setAssetModuleId(pb.getAssetModuleId());
			spec.setAssetId(pb.getAssetId());
			spec.setContainerPolicy(DeviceContainerPolicy.valueOf(pb.getContainerPolicy()));
			if (pb.hasDeviceElementSchema()) {
				spec.setDeviceElementSchema(decodeDeviceElementSchema(pb.getDeviceElementSchema()));
			}
			loadEntityData(pb.getEntityData(), spec);
			return spec;
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal device specification.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeDeviceCommand(com.openiot
	 * .spi.device.command.IDeviceCommand)
	 */
	@Override
	public byte[] encodeDeviceCommand(IDeviceCommand command) throws OpenIoTException {
		ProtobufMarshaler.DeviceCommand.Builder builder = ProtobufMarshaler.DeviceCommand.newBuilder();
		builder.setToken(command.getToken());
		builder.setSpecificationToken(command.getSpecificationToken());
		builder.setName(command.getName());
		builder.setNamespace(command.getNamespace());
		builder.setDescription(command.getDescription());
		for (ICommandParameter parameter : command.getParameters()) {
			ProtobufMarshaler.CommandParameter.Builder cbuilder =
					ProtobufMarshaler.CommandParameter.newBuilder();
			cbuilder.setName(parameter.getName());
			cbuilder.setType(parameter.getType().name());
			cbuilder.setRequired(parameter.isRequired());
			builder.addParameters(cbuilder.build());
		}
		builder.setEntityData(createEntityData(command));
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			builder.build().writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal device command.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeDeviceCommand(byte[])
	 */
	@Override
	public DeviceCommand decodeDeviceCommand(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.DeviceCommand pb = ProtobufMarshaler.DeviceCommand.parseFrom(stream);
			DeviceCommand command = new DeviceCommand();
			command.setToken(pb.getToken());
			command.setSpecificationToken(pb.getSpecificationToken());
			command.setName(pb.getName());
			command.setNamespace(pb.getNamespace());
			command.setDescription(pb.getDescription());
			for (ProtobufMarshaler.CommandParameter pbparam : pb.getParametersList()) {
				CommandParameter param = new CommandParameter();
				param.setName(pbparam.getName());
				param.setType(ParameterType.valueOf(pbparam.getType()));
				param.setRequired(pbparam.getRequired());
				command.getParameters().add(param);
			}
			loadEntityData(pb.getEntityData(), command);
			return command;
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal device command.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeDevice(com.openiot.spi
	 * .device.IDevice)
	 */
	@Override
	public byte[] encodeDevice(IDevice device) throws OpenIoTException {
		ProtobufMarshaler.Device.Builder builder = ProtobufMarshaler.Device.newBuilder();
		builder.setHardwareId(device.getHardwareId());
		builder.setSiteToken(device.getSiteToken());
		builder.setSpecificationToken(device.getSpecificationToken());
		if (device.getParentHardwareId() != null) {
			builder.setParentHardwareId(device.getParentHardwareId());
		}
		for (IDeviceElementMapping mapping : device.getDeviceElementMappings()) {
			ProtobufMarshaler.DeviceElementMapping.Builder ebuilder =
					ProtobufMarshaler.DeviceElementMapping.newBuilder();
			ebuilder.setDeviceElementSchemaPath(mapping.getDeviceElementSchemaPath());
			ebuilder.setHardwareId(mapping.getHardwareId());
			builder.addDeviceElementMappings(ebuilder.build());
		}
		if (device.getAssignmentToken() != null) {
			builder.setAssignmentToken(device.getAssignmentToken());
		}
		if (device.getComments() != null) {
			builder.setComments(device.getComments());
		}
		builder.setEntityData(createEntityData(device));
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			builder.build().writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal device.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeDevice(byte[])
	 */
	@Override
	public Device decodeDevice(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.Device pb = ProtobufMarshaler.Device.parseFrom(stream);
			Device device = new Device();
			device.setHardwareId(pb.getHardwareId());
			device.setSiteToken(pb.getSiteToken());
			device.setSpecificationToken(pb.getSpecificationToken());
			if (pb.hasParentHardwareId()) {
				device.setParentHardwareId(pb.getParentHardwareId());
			}
			for (ProtobufMarshaler.DeviceElementMapping pbmapping : pb.getDeviceElementMappingsList()) {
				DeviceElementMapping mapping = new DeviceElementMapping();
				mapping.setDeviceElementSchemaPath(pbmapping.getDeviceElementSchemaPath());
				mapping.setHardwareId(pbmapping.getHardwareId());
				device.getDeviceElementMappings().add(mapping);
			}
			if (pb.hasAssignmentToken()) {
				device.setAssignmentToken(pb.getAssignmentToken());
			}
			if (pb.hasComments()) {
				device.setComments(pb.getComments());
			}
			loadEntityData(pb.getEntityData(), device);
			return device;
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal device command.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeDeviceLocation(com.openiot
	 * .spi.device.event.IDeviceLocation)
	 */
	@Override
	public byte[] encodeDeviceLocation(IDeviceLocation location) throws OpenIoTException {
		try {
			ProtobufMarshaler.DeviceLocation pbloc = marshalDeviceLocation(location);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			pbloc.writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal device location.", e);
		}
	}

	/**
	 * Marshal an {@link IDeviceLocation}.
	 * 
	 * @param location
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected ProtobufMarshaler.DeviceLocation marshalDeviceLocation(IDeviceLocation location)
			throws OpenIoTException {
		ProtobufMarshaler.DeviceLocation.Builder builder = ProtobufMarshaler.DeviceLocation.newBuilder();
		builder.setLatitude(location.getLatitude());
		builder.setLongitude(location.getLongitude());
		builder.setElevation(location.getElevation());
		builder.setEventData(createDeviceEventData(location));
		return builder.build();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeDeviceLocation(byte[])
	 */
	@Override
	public DeviceLocation decodeDeviceLocation(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.DeviceLocation pb = ProtobufMarshaler.DeviceLocation.parseFrom(stream);
			return unmarshalDeviceLocation(pb);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal device location.", e);
		}
	}

	/**
	 * Unmarshal a {@link DeviceLocation}.
	 * 
	 * @param pb
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DeviceLocation unmarshalDeviceLocation(ProtobufMarshaler.DeviceLocation pb)
			throws OpenIoTException {
		DeviceLocation location = new DeviceLocation();
		location.setLatitude(pb.getLatitude());
		location.setLongitude(pb.getLongitude());
		location.setElevation(pb.getElevation());
		loadDeviceEventData(location, pb.getEventData());
		return location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeDeviceMeasurements(com.openiot
	 * .spi.device.event.IDeviceMeasurements)
	 */
	@Override
	public byte[] encodeDeviceMeasurements(IDeviceMeasurements measurements) throws OpenIoTException {
		ProtobufMarshaler.DeviceMeasurements.Builder builder =
				ProtobufMarshaler.DeviceMeasurements.newBuilder();
		for (String key : measurements.getMeasurements().keySet()) {
			ProtobufMarshaler.DeviceMeasurement.Builder mbuilder =
					ProtobufMarshaler.DeviceMeasurement.newBuilder();
			mbuilder.setName(key);
			mbuilder.setValue(measurements.getMeasurement(key));
			builder.addMeasurements(mbuilder.build());
		}
		builder.setEventData(createDeviceEventData(measurements));
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			builder.build().writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal device measurements.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeDeviceMeasurements(byte[])
	 */
	@Override
	public DeviceMeasurements decodeDeviceMeasurements(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.DeviceMeasurements pb = ProtobufMarshaler.DeviceMeasurements.parseFrom(stream);
			DeviceMeasurements measurements = new DeviceMeasurements();
			for (ProtobufMarshaler.DeviceMeasurement pbmx : pb.getMeasurementsList()) {
				measurements.addOrReplaceMeasurement(pbmx.getName(), pbmx.getValue());
			}
			loadDeviceEventData(measurements, pb.getEventData());
			return measurements;
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal device measurements.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeDeviceAlert(com.openiot
	 * .spi.device.event.IDeviceAlert)
	 */
	@Override
	public byte[] encodeDeviceAlert(IDeviceAlert alert) throws OpenIoTException {
		try {
			ProtobufMarshaler.DeviceAlert pbalert = marshalDeviceAlert(alert);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			pbalert.writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal device alert.", e);
		}
	}

	/**
	 * Marshal an {@link IDeviceAlert}.
	 * 
	 * @param alert
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected ProtobufMarshaler.DeviceAlert marshalDeviceAlert(IDeviceAlert alert) throws OpenIoTException {
		ProtobufMarshaler.DeviceAlert.Builder builder = ProtobufMarshaler.DeviceAlert.newBuilder();
		builder.setSource(alert.getSource().name());
		builder.setLevel(alert.getLevel().name());
		builder.setType(alert.getType());
		builder.setMessage(alert.getMessage());
		builder.setEventData(createDeviceEventData(alert));
		return builder.build();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeDeviceAlert(byte[])
	 */
	@Override
	public DeviceAlert decodeDeviceAlert(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.DeviceAlert pb = ProtobufMarshaler.DeviceAlert.parseFrom(stream);
			return unmarshalDeviceAlert(pb);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal device alert.", e);
		}
	}

	/**
	 * Unmarshal a {@link DeviceAlert}.
	 * 
	 * @param pb
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DeviceAlert unmarshalDeviceAlert(ProtobufMarshaler.DeviceAlert pb) throws OpenIoTException {
		DeviceAlert alert = new DeviceAlert();
		alert.setSource(AlertSource.valueOf(pb.getSource()));
		alert.setLevel(AlertLevel.valueOf(pb.getLevel()));
		alert.setType(pb.getType());
		alert.setMessage(pb.getMessage());
		loadDeviceEventData(alert, pb.getEventData());
		return alert;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeDeviceCommandInvocation(
	 * IDeviceCommandInvocation)
	 */
	@Override
	public byte[] encodeDeviceCommandInvocation(IDeviceCommandInvocation invocation)
			throws OpenIoTException {
		try {
			ProtobufMarshaler.DeviceCommandInvocation.Builder builder =
					ProtobufMarshaler.DeviceCommandInvocation.newBuilder();
			builder.setInitiator(invocation.getInitiator().name());
			if (invocation.getInitiatorId() != null) {
				builder.setInitiatorId(invocation.getInitiatorId());
			}
			builder.setTarget(invocation.getTarget().name());
			if (invocation.getTargetId() != null) {
				builder.setTargetId(invocation.getTargetId());
			}
			builder.setCommandToken(invocation.getCommandToken());
			for (String key : invocation.getParameterValues().keySet()) {
				ProtobufMarshaler.MetadataEntry.Builder mbuilder =
						ProtobufMarshaler.MetadataEntry.newBuilder();
				mbuilder.setName(key);
				mbuilder.setValue(invocation.getParameterValues().get(key));
				builder.addParameterValues(mbuilder.build());
			}
			builder.setStatus(invocation.getStatus().name());
			builder.setEventData(createDeviceEventData(invocation));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			builder.build().writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal command invocation.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeDeviceCommandInvocation(
	 * byte[])
	 */
	@Override
	public DeviceCommandInvocation decodeDeviceCommandInvocation(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.DeviceCommandInvocation pb =
					ProtobufMarshaler.DeviceCommandInvocation.parseFrom(stream);
			DeviceCommandInvocation invocation = new DeviceCommandInvocation();
			invocation.setInitiator(CommandInitiator.valueOf(pb.getInitiator()));
			if (pb.hasInitiatorId()) {
				invocation.setInitiatorId(pb.getInitiatorId());
			}
			invocation.setTarget(CommandTarget.valueOf(pb.getTarget()));
			if (pb.hasTargetId()) {
				invocation.setTargetId(pb.getTargetId());
			}
			invocation.setCommandToken(pb.getCommandToken());
			for (ProtobufMarshaler.MetadataEntry entry : pb.getParameterValuesList()) {
				invocation.getParameterValues().put(entry.getName(), entry.getValue());
			}
			invocation.setStatus(CommandStatus.valueOf(pb.getStatus()));
			loadDeviceEventData(invocation, pb.getEventData());
			return invocation;
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal command invocation.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeDeviceCommandResponse(com
	 * .sitewhere.spi.device.event.IDeviceCommandResponse)
	 */
	@Override
	public byte[] encodeDeviceCommandResponse(IDeviceCommandResponse response) throws OpenIoTException {
		try {
			ProtobufMarshaler.DeviceCommandResponse.Builder builder =
					ProtobufMarshaler.DeviceCommandResponse.newBuilder();
			builder.setOriginatingEventId(response.getOriginatingEventId());
			if (response.getResponseEventId() != null) {
				builder.setResponseEventId(response.getResponseEventId());
			}
			if (response.getResponse() != null) {
				builder.setResponse(response.getResponse());
			}
			builder.setEventData(createDeviceEventData(response));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			builder.build().writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal command response.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeDeviceCommandResponse(byte
	 * [])
	 */
	@Override
	public DeviceCommandResponse decodeDeviceCommandResponse(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.DeviceCommandResponse pb =
					ProtobufMarshaler.DeviceCommandResponse.parseFrom(stream);
			DeviceCommandResponse response = new DeviceCommandResponse();
			response.setOriginatingEventId(pb.getOriginatingEventId());
			if (pb.hasResponseEventId()) {
				response.setResponseEventId(pb.getResponseEventId());
			}
			if (pb.hasResponse()) {
				response.setResponse(pb.getResponse());
			}
			loadDeviceEventData(response, pb.getEventData());
			return response;
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal command response.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeDeviceAssignment(com.openiot
	 * .spi.device.IDeviceAssignment)
	 */
	@Override
	public byte[] encodeDeviceAssignment(IDeviceAssignment assignment) throws OpenIoTException {
		ProtobufMarshaler.DeviceAssignment.Builder builder = ProtobufMarshaler.DeviceAssignment.newBuilder();
		builder.setToken(assignment.getToken());
		builder.setDeviceHardwareId(assignment.getDeviceHardwareId());
		builder.setSiteToken(assignment.getSiteToken());
		builder.setAssignmentType(marshalAssignmentType(assignment.getAssignmentType()));
		builder.setAssetModuleId(assignment.getAssetModuleId());
		builder.setAssetId(assignment.getAssetId());
		builder.setStatus(assignment.getStatus().name());
		if (assignment.getActiveDate() != null) {
			builder.setActiveDate(assignment.getActiveDate().getTime());
		}
		if (assignment.getReleasedDate() != null) {
			builder.setReleasedDate(assignment.getReleasedDate().getTime());
		}
		builder.setEntityData(createEntityData(assignment));
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			builder.build().writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal device assignment.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeDeviceAssignment(byte[])
	 */
	@Override
	public DeviceAssignment decodeDeviceAssignment(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.DeviceAssignment pb = ProtobufMarshaler.DeviceAssignment.parseFrom(stream);
			DeviceAssignment assignment = new DeviceAssignment();
			assignment.setToken(pb.getToken());
			assignment.setDeviceHardwareId(pb.getDeviceHardwareId());
			assignment.setSiteToken(pb.getSiteToken());
			assignment.setAssignmentType(unmarshalAssignmentType(pb.getAssignmentType()));
			assignment.setAssetModuleId(pb.getAssetModuleId());
			assignment.setAssetId(pb.getAssetId());
			assignment.setStatus(DeviceAssignmentStatus.valueOf(pb.getStatus()));
			if (pb.hasActiveDate()) {
				assignment.setActiveDate(new Date(pb.getActiveDate()));
			}
			if (pb.hasReleasedDate()) {
				assignment.setReleasedDate(new Date(pb.getReleasedDate()));
			}
			loadEntityData(pb.getEntityData(), assignment);
			return assignment;
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal device assignment.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeDeviceAssignmentState(com
	 * .sitewhere.spi.device.IDeviceAssignmentState)
	 */
	@Override
	public byte[] encodeDeviceAssignmentState(IDeviceAssignmentState state) throws OpenIoTException {
		ProtobufMarshaler.DeviceAssignmentState.Builder builder =
				ProtobufMarshaler.DeviceAssignmentState.newBuilder();
		if (state.getLastInteractionDate() != null) {
			builder.setLastInteractionDate(state.getLastInteractionDate().getTime());
		}
		if (state.getLastLocation() != null) {
			builder.setLastLocation(marshalDeviceLocation(state.getLastLocation()));
		}
		if (state.getLatestMeasurements() != null) {
			for (IDeviceMeasurement mx : state.getLatestMeasurements()) {
				ProtobufMarshaler.DeviceMeasurement.Builder mbuilder =
						ProtobufMarshaler.DeviceMeasurement.newBuilder();
				mbuilder.setName(mx.getName());
				mbuilder.setValue(mx.getValue());
				mbuilder.setEventData(createDeviceEventData(mx));
				builder.addLatestMeasurements(mbuilder.build());
			}
		}
		if (state.getLatestAlerts() != null) {
			for (IDeviceAlert alert : state.getLatestAlerts()) {
				builder.addLatestAlerts(marshalDeviceAlert(alert));
			}
		}
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			builder.build().writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal device assignment state.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeDeviceAssignmentState(byte
	 * [])
	 */
	@Override
	public DeviceAssignmentState decodeDeviceAssignmentState(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.DeviceAssignmentState pb =
					ProtobufMarshaler.DeviceAssignmentState.parseFrom(stream);
			DeviceAssignmentState state = new DeviceAssignmentState();
			if (pb.hasLastInteractionDate()) {
				state.setLastInteractionDate(new Date(pb.getLastInteractionDate()));
			}
			if (pb.hasLastLocation()) {
				state.setLastLocation(unmarshalDeviceLocation(pb.getLastLocation()));
			}
			for (ProtobufMarshaler.DeviceAlert alert : pb.getLatestAlertsList()) {
				state.getLatestAlerts().add(unmarshalDeviceAlert(alert));
			}
			for (ProtobufMarshaler.DeviceMeasurement pbmx : pb.getLatestMeasurementsList()) {
				DeviceMeasurement mx = new DeviceMeasurement();
				mx.setName(pbmx.getName());
				mx.setValue(pbmx.getValue());
				loadDeviceEventData(mx, pbmx.getEventData());
			}
			return state;
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal device assignment state.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeDeviceGroup(com.openiot
	 * .spi.device.group.IDeviceGroup)
	 */
	@Override
	public byte[] encodeDeviceGroup(IDeviceGroup group) throws OpenIoTException {
		ProtobufMarshaler.DeviceGroup.Builder builder = ProtobufMarshaler.DeviceGroup.newBuilder();
		builder.setToken(group.getToken());
		builder.setName(group.getName());
		builder.setDescription(group.getDescription());
		for (String role : group.getRoles()) {
			builder.addRoles(role);
		}
		builder.setEntityData(createEntityData(group));
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			builder.build().writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal device group.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeDeviceGroup(byte[])
	 */
	@Override
	public DeviceGroup decodeDeviceGroup(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.DeviceGroup pb = ProtobufMarshaler.DeviceGroup.parseFrom(stream);
			DeviceGroup group = new DeviceGroup();
			group.setToken(pb.getToken());
			group.setName(pb.getName());
			group.setDescription(pb.getDescription());
			for (String role : pb.getRolesList()) {
				group.getRoles().add(role);
			}
			loadEntityData(pb.getEntityData(), group);
			return group;
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal device assignment.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeDeviceGroupElement(com.openiot
	 * .spi.device.group.IDeviceGroupElement)
	 */
	@Override
	public byte[] encodeDeviceGroupElement(IDeviceGroupElement element) throws OpenIoTException {
		ProtobufMarshaler.DeviceGroupElement.Builder builder =
				ProtobufMarshaler.DeviceGroupElement.newBuilder();
		builder.setGroupToken(element.getGroupToken());
		builder.setIndex(element.getIndex());
		builder.setType(marshalGroupElementType(element.getType()));
		builder.setElementId(element.getElementId());
		for (String role : element.getRoles()) {
			builder.addRoles(role);
		}
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			builder.build().writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal device group element.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeDeviceGroupElement(byte[])
	 */
	@Override
	public DeviceGroupElement decodeDeviceGroupElement(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.DeviceGroupElement pb = ProtobufMarshaler.DeviceGroupElement.parseFrom(stream);
			DeviceGroupElement element = new DeviceGroupElement();
			element.setGroupToken(pb.getGroupToken());
			element.setIndex(pb.getIndex());
			element.setType(unmarshalGroupElementType(pb.getType()));
			element.setElementId(pb.getElementId());
			for (String role : pb.getRolesList()) {
				element.getRoles().add(role);
			}
			return element;
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal device assignment.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeBatchOperation(com.openiot
	 * .spi.device.batch.IBatchOperation)
	 */
	@Override
	public byte[] encodeBatchOperation(IBatchOperation operation) throws OpenIoTException {
		ProtobufMarshaler.BatchOperation.Builder builder = ProtobufMarshaler.BatchOperation.newBuilder();
		builder.setToken(operation.getToken());
		builder.setOperationType(marshalOperationType(operation.getOperationType()));
		for (String key : operation.getParameters().keySet()) {
			ProtobufMarshaler.MetadataEntry.Builder mbuilder = ProtobufMarshaler.MetadataEntry.newBuilder();
			mbuilder.setName(key);
			mbuilder.setValue(operation.getParameters().get(key));
			builder.addParameters(mbuilder.build());
		}
		builder.setProcessingStatus(marshalBatchOperationStatus(operation.getProcessingStatus()));
		if (operation.getProcessingStartedDate() != null) {
			builder.setProcessingStartedDate(operation.getProcessingStartedDate().getTime());
		}
		if (operation.getProcessingEndedDate() != null) {
			builder.setProcessingEndedDate(operation.getProcessingEndedDate().getTime());
		}
		builder.setEntityData(createEntityData(operation));
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			builder.build().writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal batch operation.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeBatchOperation(byte[])
	 */
	@Override
	public BatchOperation decodeBatchOperation(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.BatchOperation pb = ProtobufMarshaler.BatchOperation.parseFrom(stream);
			BatchOperation operation = new BatchOperation();
			operation.setToken(pb.getToken());
			operation.setOperationType(unmarshalOperationType(pb.getOperationType()));
			for (ProtobufMarshaler.MetadataEntry entry : pb.getParametersList()) {
				operation.getParameters().put(entry.getName(), entry.getValue());
			}
			operation.setProcessingStatus(unmarshalBatchOperationStatus(pb.getProcessingStatus()));
			if (pb.hasProcessingStartedDate()) {
				operation.setProcessingStartedDate(new Date(pb.getProcessingStartedDate()));
			}
			if (pb.hasProcessingEndedDate()) {
				operation.setProcessingEndedDate(new Date(pb.getProcessingEndedDate()));
			}
			loadEntityData(pb.getEntityData(), operation);
			return operation;
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal batch operation.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.hbase.encoder.JsonPayloadMarshaler#encodeBatchElement(com.openiot
	 * .spi.device.batch.IBatchElement)
	 */
	@Override
	public byte[] encodeBatchElement(IBatchElement element) throws OpenIoTException {
		ProtobufMarshaler.BatchElement.Builder builder = ProtobufMarshaler.BatchElement.newBuilder();
		builder.setBatchOperationToken(element.getBatchOperationToken());
		builder.setHardwareId(element.getHardwareId());
		builder.setIndex(element.getIndex());
		builder.setProcessingStatus(marshalElementProcessingStatus(element.getProcessingStatus()));
		if (element.getProcessedDate() != null) {
			builder.setProcessedDate(element.getProcessedDate().getTime());
		}
		for (String key : element.getMetadata().keySet()) {
			ProtobufMarshaler.MetadataEntry.Builder mbuilder = ProtobufMarshaler.MetadataEntry.newBuilder();
			mbuilder.setName(key);
			mbuilder.setValue(element.getMetadata(key));
			builder.addMetadata(mbuilder.build());
		}
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			builder.build().writeTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new OpenIoTException("Unable to marshal batch operation.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.hbase.encoder.JsonPayloadMarshaler#decodeBatchElement(byte[])
	 */
	@Override
	public BatchElement decodeBatchElement(byte[] payload) throws OpenIoTException {
		ByteArrayInputStream stream = new ByteArrayInputStream(payload);
		try {
			ProtobufMarshaler.BatchElement pb = ProtobufMarshaler.BatchElement.parseFrom(stream);
			BatchElement element = new BatchElement();
			element.setBatchOperationToken(pb.getBatchOperationToken());
			element.setHardwareId(pb.getHardwareId());
			element.setIndex(pb.getIndex());
			element.setProcessingStatus(unmarshalElementProcessingStatus(pb.getProcessingStatus()));
			if (pb.hasProcessedDate()) {
				element.setProcessedDate(new Date(pb.getProcessedDate()));
			}
			for (ProtobufMarshaler.MetadataEntry entry : pb.getMetadataList()) {
				element.addOrReplaceMetadata(entry.getName(), entry.getValue());
			}
			return element;
		} catch (IOException e) {
			throw new OpenIoTException("Unable to unmarshal batch operation.", e);
		}
	}

	/**
	 * Recursively encode an {@link IDeviceElementSchema}.
	 * 
	 * @param schema
	 * @return
	 */
	protected ProtobufMarshaler.DeviceElementSchema encodeDeviceElementSchema(IDeviceElementSchema schema) {
		ProtobufMarshaler.DeviceElementSchema.Builder builder =
				ProtobufMarshaler.DeviceElementSchema.newBuilder();
		for (IDeviceUnit subunit : schema.getDeviceUnits()) {
			ProtobufMarshaler.DeviceUnit encoded = encodeDeviceUnit(subunit);
			builder.addUnits(encoded);
		}
		for (IDeviceSlot slot : schema.getDeviceSlots()) {
			ProtobufMarshaler.DeviceSlot encoded = encodeDeviceSlot(slot);
			builder.addSlots(encoded);
		}
		return builder.build();
	}

	/**
	 * Recursively encode an {@link IDeviceUnit}.
	 * 
	 * @param unit
	 * @return
	 */
	protected ProtobufMarshaler.DeviceUnit encodeDeviceUnit(IDeviceUnit unit) {
		ProtobufMarshaler.DeviceUnit.Builder builder = ProtobufMarshaler.DeviceUnit.newBuilder();
		builder.setName(unit.getName());
		builder.setPath(unit.getPath());
		for (IDeviceUnit subunit : unit.getDeviceUnits()) {
			ProtobufMarshaler.DeviceUnit encoded = encodeDeviceUnit(subunit);
			builder.addUnits(encoded);
		}
		for (IDeviceSlot slot : unit.getDeviceSlots()) {
			ProtobufMarshaler.DeviceSlot encoded = encodeDeviceSlot(slot);
			builder.addSlots(encoded);
		}
		return builder.build();
	}

	/**
	 * Encode an {@link IDeviceSlot}.
	 * 
	 * @param slot
	 * @return
	 */
	protected ProtobufMarshaler.DeviceSlot encodeDeviceSlot(IDeviceSlot slot) {
		ProtobufMarshaler.DeviceSlot.Builder builder = ProtobufMarshaler.DeviceSlot.newBuilder();
		builder.setName(slot.getName());
		builder.setPath(slot.getPath());
		return builder.build();
	}

	protected DeviceElementSchema decodeDeviceElementSchema(ProtobufMarshaler.DeviceElementSchema pb) {
		DeviceElementSchema schema = new DeviceElementSchema();
		for (ProtobufMarshaler.DeviceUnit subunit : pb.getUnitsList()) {
			schema.getDeviceUnits().add(decodeDeviceUnit(subunit));
		}
		for (ProtobufMarshaler.DeviceSlot slot : pb.getSlotsList()) {
			schema.getDeviceSlots().add(decodeDeviceSlot(slot));
		}
		return schema;
	}

	protected DeviceUnit decodeDeviceUnit(ProtobufMarshaler.DeviceUnit pb) {
		DeviceUnit unit = new DeviceUnit();
		unit.setName(pb.getName());
		unit.setPath(pb.getPath());
		for (ProtobufMarshaler.DeviceUnit subunit : pb.getUnitsList()) {
			unit.getDeviceUnits().add(decodeDeviceUnit(subunit));
		}
		for (ProtobufMarshaler.DeviceSlot slot : pb.getSlotsList()) {
			unit.getDeviceSlots().add(decodeDeviceSlot(slot));
		}
		return unit;
	}

	protected DeviceSlot decodeDeviceSlot(ProtobufMarshaler.DeviceSlot pb) {
		DeviceSlot slot = new DeviceSlot();
		slot.setName(pb.getName());
		slot.setPath(pb.getPath());
		return slot;
	}

	/**
	 * Marshal a {@link DeviceEventType}.
	 * 
	 * @param type
	 * @return
	 */
	protected ProtobufMarshaler.DeviceEventType marshalEventType(DeviceEventType type) {
		switch (type) {
		case Alert: {
			return ProtobufMarshaler.DeviceEventType.DEAlert;
		}
		case CommandInvocation: {
			return ProtobufMarshaler.DeviceEventType.DECommandInvocation;
		}
		case CommandResponse: {
			return ProtobufMarshaler.DeviceEventType.DECommandResponse;
		}
		case Location: {
			return ProtobufMarshaler.DeviceEventType.DELocation;
		}
		case Measurement: {
			return ProtobufMarshaler.DeviceEventType.DEMeasurement;
		}
		case Measurements: {
			return ProtobufMarshaler.DeviceEventType.DEMeasurements;
		}
		case StateChange: {
			return ProtobufMarshaler.DeviceEventType.DEStateChange;
		}
		}
		throw new RuntimeException("Unknown DeviceEventType: " + type);
	}

	/**
	 * Unmarshal a {@link DeviceEventType}.
	 * 
	 * @param type
	 * @return
	 */
	protected DeviceEventType unmarshalEventType(ProtobufMarshaler.DeviceEventType type) {
		switch (type) {
		case DEAlert: {
			return DeviceEventType.Alert;
		}
		case DECommandInvocation: {
			return DeviceEventType.CommandInvocation;
		}
		case DECommandResponse: {
			return DeviceEventType.CommandResponse;
		}
		case DELocation: {
			return DeviceEventType.Location;
		}
		case DEMeasurement: {
			return DeviceEventType.Measurement;
		}
		case DEMeasurements: {
			return DeviceEventType.Measurements;
		}
		case DEStateChange: {
			return DeviceEventType.StateChange;
		}
		}
		throw new RuntimeException("Unknown ProtobufMarshaler.DeviceEventType: " + type);
	}

	/**
	 * Marshal a {@link DeviceAssignmentType}.
	 * 
	 * @param type
	 * @return
	 */
	protected ProtobufMarshaler.DeviceAssignmentType marshalAssignmentType(DeviceAssignmentType type) {
		switch (type) {
		case Associated: {
			return ProtobufMarshaler.DeviceAssignmentType.DAAssociated;
		}
		case Unassociated: {
			return ProtobufMarshaler.DeviceAssignmentType.DAUnassociated;
		}
		}
		throw new RuntimeException("Unknown DeviceAssignmentType: " + type);
	}

	/**
	 * Unmarshal a {@link DeviceAssignmentType}.
	 * 
	 * @param type
	 * @return
	 */
	protected DeviceAssignmentType unmarshalAssignmentType(ProtobufMarshaler.DeviceAssignmentType type) {
		switch (type) {
		case DAAssociated: {
			return DeviceAssignmentType.Associated;
		}
		case DAUnassociated: {
			return DeviceAssignmentType.Unassociated;
		}
		}
		throw new RuntimeException("Unknown ProtobufMarshaler.DeviceAssignmentType: " + type);
	}

	/**
	 * Marshal a {@link GroupElementType}.
	 * 
	 * @param type
	 * @return
	 */
	protected ProtobufMarshaler.GroupElementType marshalGroupElementType(GroupElementType type) {
		switch (type) {
		case Device: {
			return ProtobufMarshaler.GroupElementType.GEDevice;
		}
		case Group: {
			return ProtobufMarshaler.GroupElementType.GEGroup;
		}
		}
		throw new RuntimeException("Unknown GroupElementType: " + type);
	}

	/**
	 * Unmarshal a {@link GroupElementType}.
	 * 
	 * @param type
	 * @return
	 */
	protected GroupElementType unmarshalGroupElementType(ProtobufMarshaler.GroupElementType type) {
		switch (type) {
		case GEDevice: {
			return GroupElementType.Device;
		}
		case GEGroup: {
			return GroupElementType.Group;
		}
		}
		throw new RuntimeException("Unknown ProtobufMarshaler.GroupElementType: " + type);
	}

	/**
	 * Marshal an {@link OperationType}.
	 * 
	 * @param type
	 * @return
	 */
	protected ProtobufMarshaler.OperationType marshalOperationType(OperationType type) {
		switch (type) {
		case InvokeCommand: {
			return ProtobufMarshaler.OperationType.OTInvokeCommand;
		}
		case UpdateFirmware: {
			return ProtobufMarshaler.OperationType.OTUpdateFirmware;
		}
		}
		throw new RuntimeException("Unknown operation type: " + type);
	}

	/**
	 * Unmarshal an {@link OperationType}.
	 * 
	 * @param type
	 * @return
	 */
	protected OperationType unmarshalOperationType(ProtobufMarshaler.OperationType type) {
		switch (type) {
		case OTInvokeCommand: {
			return OperationType.InvokeCommand;
		}
		case OTUpdateFirmware: {
			return OperationType.UpdateFirmware;
		}
		}
		throw new RuntimeException("Unknown ProtobufMarshaler.OperationType type: " + type);
	}

	/**
	 * Marshal a {@link BatchOperationStatus}.
	 * 
	 * @param type
	 * @return
	 */
	protected ProtobufMarshaler.BatchOperationStatus marshalBatchOperationStatus(BatchOperationStatus type) {
		switch (type) {
		case FinishedSuccessfully: {
			return ProtobufMarshaler.BatchOperationStatus.BOSFinishedSuccessfully;
		}
		case FinishedWithErrors: {
			return ProtobufMarshaler.BatchOperationStatus.BOSFinishedWithErrors;
		}
		case Processing: {
			return ProtobufMarshaler.BatchOperationStatus.BOSProcessing;
		}
		case Unprocessed: {
			return ProtobufMarshaler.BatchOperationStatus.BOSUnprocessed;
		}
		}
		throw new RuntimeException("Unknown batch operation status: " + type);
	}

	/**
	 * Unmarshal a {@link BatchOperationStatus}.
	 * 
	 * @param type
	 * @return
	 */
	protected BatchOperationStatus unmarshalBatchOperationStatus(ProtobufMarshaler.BatchOperationStatus type) {
		switch (type) {
		case BOSFinishedSuccessfully: {
			return BatchOperationStatus.FinishedSuccessfully;
		}
		case BOSFinishedWithErrors: {
			return BatchOperationStatus.FinishedWithErrors;
		}
		case BOSProcessing: {
			return BatchOperationStatus.Processing;
		}
		case BOSUnprocessed: {
			return BatchOperationStatus.Unprocessed;
		}
		}
		throw new RuntimeException("Unknown ProtobufMarshaler.BatchOperationStatus: " + type);
	}

	/**
	 * Marshal an {@link ElementProcessingStatus}.
	 * 
	 * @param type
	 * @return
	 */
	protected ProtobufMarshaler.ElementProcessingStatus marshalElementProcessingStatus(
			ElementProcessingStatus type) {
		switch (type) {
		case Failed: {
			return ProtobufMarshaler.ElementProcessingStatus.EPSFailed;
		}
		case Processing: {
			return ProtobufMarshaler.ElementProcessingStatus.EPSProcessing;
		}
		case Succeeded: {
			return ProtobufMarshaler.ElementProcessingStatus.EPSSucceeded;
		}
		case Unprocessed: {
			return ProtobufMarshaler.ElementProcessingStatus.EPSUnprocessed;
		}
		}
		throw new RuntimeException("Unknown ElementProcessingStatus: " + type);
	}

	/**
	 * Unmarshal an {@link ElementProcessingStatus}.
	 * 
	 * @param type
	 * @return
	 */
	protected ElementProcessingStatus unmarshalElementProcessingStatus(
			ProtobufMarshaler.ElementProcessingStatus type) {
		switch (type) {
		case EPSFailed: {
			return ElementProcessingStatus.Failed;
		}
		case EPSProcessing: {
			return ElementProcessingStatus.Processing;
		}
		case EPSSucceeded: {
			return ElementProcessingStatus.Succeeded;
		}
		case EPSUnprocessed: {
			return ElementProcessingStatus.Unprocessed;
		}
		}
		throw new RuntimeException("Unknown ProtobufMarshaler.ElementProcessingStatus: " + type);
	}

	/**
	 * Create object that stores common device event data.
	 * 
	 * @param event
	 * @return
	 */
	protected ProtobufMarshaler.DeviceEventData createDeviceEventData(IDeviceEvent event) {
		ProtobufMarshaler.DeviceEventData.Builder builder = ProtobufMarshaler.DeviceEventData.newBuilder();
		builder.setId(event.getId());
		builder.setEventType(marshalEventType(event.getEventType()));
		builder.setSiteToken(event.getSiteToken());
		builder.setDeviceAssignmentToken(event.getDeviceAssignmentToken());
		builder.setAssignmentType(marshalAssignmentType(event.getAssignmentType()));
		builder.setAssetModuleId(event.getAssetModuleId());
		builder.setAssetId(event.getAssetId());
		builder.setEventDate(event.getEventDate().getTime());
		builder.setReceivedDate(event.getReceivedDate().getTime());
		builder.addAllMetadata(asMetadataEntries(event.getMetadata()));
		return builder.build();
	}

	/**
	 * Load event data from common storage object.
	 * 
	 * @param event
	 * @param pb
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void loadDeviceEventData(DeviceEvent event, ProtobufMarshaler.DeviceEventData pb)
			throws OpenIoTException {
		event.setId(pb.getId());
		event.setEventType(unmarshalEventType(pb.getEventType()));
		event.setSiteToken(pb.getSiteToken());
		event.setDeviceAssignmentToken(pb.getDeviceAssignmentToken());
		event.setAssignmentType(unmarshalAssignmentType(pb.getAssignmentType()));
		event.setAssetModuleId(pb.getAssetModuleId());
		event.setAssetId(pb.getAssetId());
		event.setEventDate(new Date(pb.getEventDate()));
		event.setReceivedDate(new Date(pb.getReceivedDate()));
		for (ProtobufMarshaler.MetadataEntry entry : pb.getMetadataList()) {
			event.addOrReplaceMetadata(entry.getName(), entry.getValue());
		}
	}

	/**
	 * Converts a metadata map into individial entries.
	 * 
	 * @param metadata
	 * @return
	 */
	protected List<ProtobufMarshaler.MetadataEntry> asMetadataEntries(Map<String, String> metadata) {
		List<ProtobufMarshaler.MetadataEntry> entries = new ArrayList<ProtobufMarshaler.MetadataEntry>();
		for (String key : metadata.keySet()) {
			ProtobufMarshaler.MetadataEntry entry =
					ProtobufMarshaler.MetadataEntry.newBuilder().setName(key).setValue(metadata.get(key)).build();
			entries.add(entry);
		}
		return entries;
	}

	/**
	 * Builds common entity data object.
	 * 
	 * @param entity
	 * @return
	 */
	protected ProtobufMarshaler.EntityData createEntityData(IMetadataProviderEntity entity) {
		ProtobufMarshaler.EntityData.Builder builder = ProtobufMarshaler.EntityData.newBuilder();
		builder.setCreatedDate(entity.getCreatedDate().getTime());
		builder.setCreatedBy(entity.getCreatedBy());
		if (entity.getUpdatedDate() != null) {
			builder.setUpdatedDate(entity.getUpdatedDate().getTime());
		}
		if (entity.getUpdatedBy() != null) {
			builder.setUpdatedBy(entity.getUpdatedBy());
		}
		builder.setDeleted(entity.isDeleted());
		return builder.build();
	}

	/**
	 * Load common entity data.
	 * 
	 * @param pb
	 * @param entity
	 */
	protected void loadEntityData(ProtobufMarshaler.EntityData pb, MetadataProviderEntity entity)
			throws OpenIoTException {
		entity.setCreatedDate(new Date(pb.getCreatedDate()));
		entity.setCreatedBy(pb.getCreatedBy());
		if (pb.hasUpdatedDate()) {
			entity.setUpdatedDate(new Date(pb.getUpdatedDate()));
		}
		if (pb.hasUpdatedBy()) {
			entity.setUpdatedBy(pb.getUpdatedBy());
		}
		if (pb.hasDeleted()) {
			entity.setDeleted(pb.getDeleted());
		}
		for (ProtobufMarshaler.MetadataEntry entry : pb.getMetadataList()) {
			entity.addOrReplaceMetadata(entry.getName(), entry.getValue());
		}
	}
}