/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.mongodb;

import com.openiot.mongodb.device.*;
import com.openiot.spi.device.*;
import com.openiot.spi.device.batch.IBatchElement;
import com.openiot.spi.device.batch.IBatchOperation;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.event.*;
import com.openiot.spi.device.group.IDeviceGroup;
import com.openiot.spi.device.group.IDeviceGroupElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages classes used to convert between Mongo and SPI objects.
 * 
 * @author Derek
 */
public class MongoConverters implements IMongoConverterLookup {

	/** Maps interface classes to their associated converters */
	private static Map<Class<?>, MongoConverter<?>> CONVERTERS = new HashMap<Class<?>, MongoConverter<?>>();

	/** Create a list of converters for various types */
	static {
		CONVERTERS.put(IDeviceSpecification.class, new MongoDeviceSpecification());
		CONVERTERS.put(IDeviceCommand.class, new MongoDeviceCommand());
		CONVERTERS.put(IDevice.class, new MongoDevice());
		CONVERTERS.put(IDeviceAssignment.class, new MongoDeviceAssignment());
		CONVERTERS.put(IDeviceMeasurements.class, new MongoDeviceMeasurements());
		CONVERTERS.put(IDeviceAlert.class, new MongoDeviceAlert());
		CONVERTERS.put(IDeviceLocation.class, new MongoDeviceLocation());
		CONVERTERS.put(IDeviceCommandInvocation.class, new MongoDeviceCommandInvocation());
		CONVERTERS.put(IDeviceCommandResponse.class, new MongoDeviceCommandResponse());
		CONVERTERS.put(IDeviceStateChange.class, new MongoDeviceStateChange());
		CONVERTERS.put(ISite.class, new MongoSite());
		CONVERTERS.put(IZone.class, new MongoZone());
		CONVERTERS.put(IDeviceGroup.class, new MongoDeviceGroup());
		CONVERTERS.put(IDeviceGroupElement.class, new MongoDeviceGroupElement());
		CONVERTERS.put(IBatchOperation.class, new MongoBatchOperation());
		CONVERTERS.put(IBatchElement.class, new MongoBatchElement());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMongoConverterLookup#getConverterFor(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T> MongoConverter<T> getConverterFor(Class<T> api) {
		MongoConverter<T> result = (MongoConverter<T>) CONVERTERS.get(api);
		if (result == null) {
			throw new RuntimeException("No Mongo converter registered for " + api.getName());
		}
		return result;
	}
}