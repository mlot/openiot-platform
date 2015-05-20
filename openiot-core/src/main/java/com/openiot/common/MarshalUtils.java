/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openiot.spi.OpenIoTException;
import org.apache.log4j.Logger;

public class MarshalUtils {

	/** Static logger instance */
	@SuppressWarnings("unused")
	private static Logger LOGGER = Logger.getLogger(MarshalUtils.class);

	/** Singleton object mapper for JSON marshaling */
	private static ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * Marshal an object to a byte array.
	 * 
	 * @param object
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static byte[] marshalJson(Object object) throws OpenIoTException {
		try {
			return MAPPER.writeValueAsBytes(object);
		} catch (JsonProcessingException e) {
			throw new OpenIoTException("Could not marshal device as JSON.", e);
		}
	}

	/**
	 * Marshal an object to a JSON string.
	 * 
	 * @param object
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static String marshalJsonAsString(Object object) throws OpenIoTException {
		try {
			return MAPPER.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new OpenIoTException("Could not marshal device as JSON.", e);
		}
	}

	/**
	 * Unmarshal a JSON string to an object.
	 * 
	 * @param json
	 * @param type
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static <T> T unmarshalJson(byte[] json, Class<T> type) throws OpenIoTException {
		try {
			return MAPPER.readValue(json, type);
		} catch (Throwable e) {
			throw new OpenIoTException("Unable to parse JSON.", e);
		}
	}
}
