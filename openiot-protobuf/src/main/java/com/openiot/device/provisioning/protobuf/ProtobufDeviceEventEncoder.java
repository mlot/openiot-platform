/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning.protobuf;

import com.openiot.device.provisioning.protobuf.proto.Openiot.OpenIoT;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.request.IDeviceEventCreateRequest;
import com.openiot.spi.device.event.request.IDeviceMeasurementsCreateRequest;
import com.openiot.spi.device.provisioning.IDecodedDeviceEventRequest;
import com.openiot.spi.device.provisioning.IDeviceEventEncoder;

import java.io.ByteArrayOutputStream;
import java.util.Set;

/**
 * Implementation of {@link IDeviceEventEncoder} that encodes device events into binary
 * using the OpenIoT Google Protocol Buffers format.
 * 
 * @author Derek
 */
public class ProtobufDeviceEventEncoder implements IDeviceEventEncoder<byte[]> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceEventEncoder#encode(com.openiot.
	 * spi.device.provisioning.IDecodedDeviceEventRequest)
	 */
	@Override
	public byte[] encode(IDecodedDeviceEventRequest event) throws OpenIoTException {
		IDeviceEventCreateRequest request = event.getRequest();
		if (request instanceof IDeviceMeasurementsCreateRequest) {
			return encodeDeviceMeasurements(event);
		}
		throw new OpenIoTException("Protobuf encoder encountered unknown event type: "
				+ event.getClass().getName());
	}

	/**
	 * Encode a {@link IDecodedDeviceEventRequest} in a protobuf message.
	 * 
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected byte[] encodeDeviceMeasurements(IDecodedDeviceEventRequest event) throws OpenIoTException {
		try {
			IDeviceMeasurementsCreateRequest measurements =
					(IDeviceMeasurementsCreateRequest) event.getRequest();
			OpenIoT.DeviceMeasurements.Builder mb = OpenIoT.DeviceMeasurements.newBuilder();
			mb.setHardwareId(event.getHardwareId());
			Set<String> keys = measurements.getMeasurements().keySet();
			for (String key : keys) {
				mb.addMeasurement(OpenIoT.Measurement.newBuilder().setMeasurementId(key).setMeasurementValue(
						String.valueOf(measurements.getMeasurement(key))).build());
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			OpenIoT.Header.Builder builder = OpenIoT.Header.newBuilder();
			builder.setCommand(OpenIoT.Command.DEVICEMEASUREMENT);
			if (event.getOriginator() != null) {
				builder.setOriginator(event.getOriginator());
			}
			
			builder.build().writeDelimitedTo(out);
			mb.build().writeDelimitedTo(out);
			return out.toByteArray();
		} catch (Exception e) {
			throw new OpenIoTException(e);
		}
	}
}