/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openiot.rest.model.device.event.DeviceEventBatch;
import com.openiot.rest.model.device.provisioning.DecodedDeviceEventRequest;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.request.IDeviceAlertCreateRequest;
import com.openiot.spi.device.event.request.IDeviceLocationCreateRequest;
import com.openiot.spi.device.event.request.IDeviceMeasurementsCreateRequest;
import com.openiot.spi.device.provisioning.IDecodedDeviceEventRequest;
import com.openiot.spi.device.provisioning.IDeviceEventDecoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Event decoder that converts a binary payload into the default OpenIoT REST
 * implementations using Jackson to marshal them as JSON.
 * 
 * @author Derek
 */
public class JsonBatchEventDecoder implements IDeviceEventDecoder<byte[]> {

	/** Used to map data into an object based on JSON parsing */
	private ObjectMapper mapper = new ObjectMapper();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceEventDecoder#decode(java.lang.Object)
	 */
	@Override
	public List<IDecodedDeviceEventRequest> decode(byte[] payload, Map context) throws OpenIoTException {
		try {
			List<IDecodedDeviceEventRequest> events = new ArrayList<IDecodedDeviceEventRequest>();
			DeviceEventBatch batch = mapper.readValue(payload, DeviceEventBatch.class);
			for (IDeviceLocationCreateRequest lc : batch.getLocations()) {
				DecodedDeviceEventRequest decoded = new DecodedDeviceEventRequest();
				decoded.setHardwareId(batch.getHardwareId());
				decoded.setRequest(lc);
				events.add(decoded);
			}
			for (IDeviceMeasurementsCreateRequest mc : batch.getMeasurements()) {
				DecodedDeviceEventRequest decoded = new DecodedDeviceEventRequest();
				decoded.setHardwareId(batch.getHardwareId());
				decoded.setRequest(mc);
				events.add(decoded);
			}
			for (IDeviceAlertCreateRequest ac : batch.getAlerts()) {
				DecodedDeviceEventRequest decoded = new DecodedDeviceEventRequest();
				decoded.setHardwareId(batch.getHardwareId());
				decoded.setRequest(ac);
				events.add(decoded);
			}
			return events;
		} catch (JsonParseException e) {
			throw new OpenIoTException(e);
		} catch (JsonMappingException e) {
			throw new OpenIoTException(e);
		} catch (IOException e) {
			throw new OpenIoTException(e);
		}
	}
}