/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning.protobuf;

import com.openiot.device.provisioning.protobuf.proto.Openiot;
import com.openiot.rest.model.device.event.request.*;
import com.openiot.rest.model.device.provisioning.DecodedDeviceEventRequest;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.AlertLevel;
import com.openiot.spi.device.provisioning.IDecodedDeviceEventRequest;
import com.openiot.spi.device.provisioning.IDeviceEventDecoder;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Decodes a message payload that was previously encoded using the Google Protocol Buffers
 * with the OpenIoT proto.
 * 
 * @author Derek
 */
public class ProtobufDeviceEventDecoder implements IDeviceEventDecoder<byte[]> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(ProtobufDeviceEventDecoder.class);

    public static final String EnqueueTimeKey = "enqueueTime";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceEventDecoder#decode(java.lang.Object)
	 */
	@Override
	public List<IDecodedDeviceEventRequest> decode(byte[] payload, Map context) throws OpenIoTException {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(payload);
			Openiot.OpenIoT.Header header = Openiot.OpenIoT.Header.parseDelimitedFrom(stream);
			List<IDecodedDeviceEventRequest> results = new ArrayList<IDecodedDeviceEventRequest>();
			DecodedDeviceEventRequest decoded = new DecodedDeviceEventRequest();
			if (header.hasOriginator()) {
				decoded.setOriginator(header.getOriginator());
			}
			results.add(decoded);
			switch (header.getCommand()) {
			case REGISTER: {
				Openiot.OpenIoT.RegisterDevice register = Openiot.OpenIoT.RegisterDevice.parseDelimitedFrom(stream);
				LOGGER.debug("Decoded registration for: " + register.getHardwareId());
				DeviceRegistrationRequest request = new DeviceRegistrationRequest();
				request.setHardwareId(register.getHardwareId());
				request.setSpecificationToken(register.getSpecificationToken());
				if (register.hasSiteToken()) {
					request.setSiteToken(register.getSiteToken());
				}
				List<Openiot.OpenIoT.Metadata> metadata = register.getMetadataList();
				for (Openiot.OpenIoT.Metadata meta : metadata) {
					request.addOrReplaceMetadata(meta.getName(), meta.getValue());
				}
				decoded.setHardwareId(register.getHardwareId());
				decoded.setRequest(request);
				return results;
			}
			case ACKNOWLEDGE: {
				Openiot.OpenIoT.Acknowledge ack = Openiot.OpenIoT.Acknowledge.parseDelimitedFrom(stream);
				LOGGER.debug("Decoded acknowledge for: " + ack.getHardwareId());
				DeviceCommandResponseCreateRequest request = new DeviceCommandResponseCreateRequest();
				request.setOriginatingEventId(header.getOriginator());
				request.setResponse(ack.getMessage());
				decoded.setHardwareId(ack.getHardwareId());
				decoded.setRequest(request);
				return results;
			}
			case DEVICEMEASUREMENT: {
				Openiot.OpenIoT.DeviceMeasurements dm = Openiot.OpenIoT.DeviceMeasurements.parseDelimitedFrom(stream);
				LOGGER.debug("Decoded measurement for: " + dm.getHardwareId());
				DeviceMeasurementsCreateRequest request = new DeviceMeasurementsCreateRequest();
				List<Openiot.OpenIoT.Measurement> measurements = dm.getMeasurementList();
				for (Openiot.OpenIoT.Measurement current : measurements) {
					request.addOrReplaceMeasurement(current.getMeasurementId(), Double.parseDouble(current.getMeasurementValue()));
				}
				List<Openiot.OpenIoT.Metadata> metadata = dm.getMetadataList();
				for (Openiot.OpenIoT.Metadata meta : metadata) {
					request.addOrReplaceMetadata(meta.getName(), meta.getValue());
				}
				if (dm.hasEventDate()) {
					request.setEventDate(new Date(dm.getEventDate()));
				} else if (context.containsKey(EnqueueTimeKey)) {
                    request.setEventDate(new Date((Long)context.get(EnqueueTimeKey)));
                } else {
					request.setEventDate(new Date());
				}
				decoded.setHardwareId(dm.getHardwareId());
				decoded.setRequest(request);
				return results;
			}
			case DEVICELOCATION: {
				Openiot.OpenIoT.DeviceLocation location = Openiot.OpenIoT.DeviceLocation.parseDelimitedFrom(stream);
				LOGGER.debug("Decoded location for: " + location.getHardwareId());
				DeviceLocationCreateRequest request = new DeviceLocationCreateRequest();
				request.setLatitude(Double.parseDouble(location.getLatitude()));
				request.setLongitude(Double.parseDouble(location.getLongitude()));
				request.setElevation(Double.parseDouble(location.getElevation()));
				List<Openiot.OpenIoT.Metadata> metadata = location.getMetadataList();
				for (Openiot.OpenIoT.Metadata meta : metadata) {
					request.addOrReplaceMetadata(meta.getName(), meta.getValue());
				}
				if (location.hasEventDate()) {
					request.setEventDate(new Date(location.getEventDate()));
				} else if (context.containsKey(EnqueueTimeKey)) {
                    request.setEventDate(new Date((Long)context.get(EnqueueTimeKey)));
                } else {
					request.setEventDate(new Date());
				}
				decoded.setHardwareId(location.getHardwareId());
				decoded.setRequest(request);
				return results;
			}
			case DEVICEALERT: {
				Openiot.OpenIoT.DeviceAlert alert = Openiot.OpenIoT.DeviceAlert.parseDelimitedFrom(stream);
				LOGGER.debug("Decoded alert for: " + alert.getHardwareId());
				DeviceAlertCreateRequest request = new DeviceAlertCreateRequest();
				request.setType(alert.getAlertType());
				request.setMessage(alert.getAlertMessage());
				request.setLevel(AlertLevel.Info);
				List<Openiot.OpenIoT.Metadata> metadata = alert.getMetadataList();
				for (Openiot.OpenIoT.Metadata meta : metadata) {
					request.addOrReplaceMetadata(meta.getName(), meta.getValue());
				}
				if (alert.hasEventDate()) {
					request.setEventDate(new Date(alert.getEventDate()));
				} else if (context.containsKey(EnqueueTimeKey)) {
                    request.setEventDate(new Date((Long)context.get(EnqueueTimeKey)));
                } else {
					request.setEventDate(new Date());
				}
				decoded.setHardwareId(alert.getHardwareId());
				decoded.setRequest(request);
				return results;
			}
			default: {
				throw new OpenIoTException("Unable to decode message. Type not supported: "
						+ header.getCommand().name());
			}
			}
		} catch (IOException e) {
			throw new OpenIoTException("Unable to decode protobuf message.", e);
		}
	}
}