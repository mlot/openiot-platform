/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning.debug;

import com.openiot.core.DataUtils;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.provisioning.IDecodedDeviceEventRequest;
import com.openiot.spi.device.provisioning.IDeviceEventDecoder;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link IDeviceEventDecoder} that logs the event payload but does not
 * actually produce any events. This is useful for debugging when implementing decoders
 * for hardware sending human-readable commands across the wire.
 * 
 * @author Derek
 */
public class PayloadLoggerEventDecoder implements IDeviceEventDecoder<byte[]> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(PayloadLoggerEventDecoder.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceEventDecoder#decode(java.lang.Object)
	 */
	@Override
	public List<IDecodedDeviceEventRequest> decode(byte[] payload, Map context) throws OpenIoTException {
		LOGGER.info("=== EVENT DATA BEGIN ===");
		LOGGER.info(new String(payload));
		LOGGER.info("(hex) " + DataUtils.bytesToHex(payload));
		LOGGER.info("=== EVENT DATA END ===");
		return new ArrayList<IDecodedDeviceEventRequest>();
	}
}