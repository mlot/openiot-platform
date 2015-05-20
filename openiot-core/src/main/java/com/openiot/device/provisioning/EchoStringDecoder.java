/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.provisioning.IDecodedDeviceEventRequest;
import com.openiot.spi.device.provisioning.IDeviceEventDecoder;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link IDeviceEventDecoder} that does not actually decode anything,
 * but rather prints the payload to the log.
 * 
 * @author Derek
 */
public class EchoStringDecoder implements IDeviceEventDecoder<String> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(EchoStringDecoder.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceEventDecoder#decode(java.lang.Object)
	 */
	@Override
	public List<IDecodedDeviceEventRequest> decode(String payload, Map map) throws OpenIoTException {
		LOGGER.info("Payload: " + payload);
		return new ArrayList<IDecodedDeviceEventRequest>();
	}
}