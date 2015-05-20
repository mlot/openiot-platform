/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.protobuf.test;

import com.openiot.device.provisioning.protobuf.ProtobufDeviceEventEncoder;
import com.openiot.rest.model.device.event.request.DeviceMeasurementsCreateRequest;
import com.openiot.rest.model.device.provisioning.DecodedDeviceEventRequest;
import com.openiot.spi.OpenIoTException;

import java.util.Date;

/**
 * Helper class for generating encoded messages using the OpenIoT GPB format.
 * 
 * @author Derek
 */
public class EventsHelper {

	/**
	 * Generate an encoded measurements message for the given hardware id.
	 * 
	 * @param hardwareId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static byte[] generateEncodedMeasurementsMessage(String hardwareId) throws OpenIoTException {
		DecodedDeviceEventRequest request = new DecodedDeviceEventRequest();
		request.setHardwareId(hardwareId);

		DeviceMeasurementsCreateRequest mx = new DeviceMeasurementsCreateRequest();
		mx.setEventDate(new Date());
		mx.addOrReplaceMeasurement("fuel.level", 123.4);
		request.setRequest(mx);

		return (new ProtobufDeviceEventEncoder()).encode(request);
	}
}
