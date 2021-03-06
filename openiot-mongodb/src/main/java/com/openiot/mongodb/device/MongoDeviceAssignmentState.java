/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.mongodb.device;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.openiot.mongodb.MongoConverter;
import com.openiot.rest.model.device.DeviceAssignmentState;
import com.openiot.spi.device.IDeviceAssignmentState;
import com.openiot.spi.device.event.IDeviceAlert;
import com.openiot.spi.device.event.IDeviceMeasurement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Used to load or save assignment state to MongoDB.
 * 
 * @author dadams
 */
public class MongoDeviceAssignmentState implements MongoConverter<IDeviceAssignmentState> {

	/** Property for last interaction date */
	public static final String PROP_LAST_INTERACTION_DATE = "lastInteractionDate";

	/** Property for last location */
	public static final String PROP_LAST_LOCATION = "lastLocation";

	/** Property for latest measurements */
	public static final String PROP_LATEST_MEASUREMENTS = "latestMeasurements";

	/** Property for latest measurements */
	public static final String PROP_LATEST_ALERTS = "latestAlerts";

	/** Property for last 'reply to' location */
	public static final String PROP_LAST_REPLY_TO = "lastReplyTo";

	/*
	 * (non-Javadoc)
	 * 
	 * @see MongoConverter#convert(java.lang.Object)
	 */
	@Override
	public BasicDBObject convert(IDeviceAssignmentState source) {
		return MongoDeviceAssignmentState.toDBObject(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MongoConverter#convert(com.mongodb.DBObject)
	 */
	@Override
	public IDeviceAssignmentState convert(DBObject source) {
		return MongoDeviceAssignmentState.fromDBObject(source);
	}

	/**
	 * Copy information from SPI into Mongo DBObject.
	 * 
	 * @param source
	 * @param target
	 */
	public static void toDBObject(IDeviceAssignmentState source, BasicDBObject target) {
		if (source.getLastInteractionDate() != null) {
			target.append(PROP_LAST_INTERACTION_DATE, source.getLastInteractionDate());
		}

		if (source.getLastLocation() != null) {
			target.append(PROP_LAST_LOCATION, MongoDeviceLocation.toDBObject(source.getLastLocation(), true));
		}

		if (!source.getLatestMeasurements().isEmpty()) {
			List<BasicDBObject> measurements = new ArrayList<BasicDBObject>();
			for (IDeviceMeasurement sm : source.getLatestMeasurements()) {
				measurements.add(MongoDeviceMeasurement.toDBObject(sm, true));
			}
			target.append(PROP_LATEST_MEASUREMENTS, measurements);
		}

		if (!source.getLatestAlerts().isEmpty()) {
			List<BasicDBObject> alerts = new ArrayList<BasicDBObject>();
			for (IDeviceAlert sa : source.getLatestAlerts()) {
				alerts.add(MongoDeviceAlert.toDBObject(sa, true));
			}
			target.append(PROP_LATEST_ALERTS, alerts);
		}

		if (source.getLastReplyTo() != null) {
			target.append(PROP_LAST_REPLY_TO, source.getLastReplyTo());
		}
	}

	/**
	 * Copy information from Mongo DBObject to model object.
	 * 
	 * @param source
	 * @param target
	 */
	@SuppressWarnings("unchecked")
	public static void fromDBObject(DBObject source, DeviceAssignmentState target) {
		target.setLastInteractionDate((Date) source.get(PROP_LAST_INTERACTION_DATE));
		DBObject lastLocation = (DBObject) source.get(PROP_LAST_LOCATION);
		if (lastLocation != null) {
			target.setLastLocation(MongoDeviceLocation.fromDBObject(lastLocation, true));
		}
		List<DBObject> latestMeasurements = (List<DBObject>) source.get(PROP_LATEST_MEASUREMENTS);
		if (latestMeasurements != null) {
			for (DBObject sm : latestMeasurements) {
				target.getLatestMeasurements().add(MongoDeviceMeasurement.fromDBObject(sm, true));
			}
		}
		List<DBObject> latestAlerts = (List<DBObject>) source.get(PROP_LATEST_ALERTS);
		if (latestAlerts != null) {
			for (DBObject sa : latestAlerts) {
				target.getLatestAlerts().add(MongoDeviceAlert.fromDBObject(sa, true));
			}
		}
		target.setLastReplyTo((String) source.get(PROP_LAST_REPLY_TO));
	}

	/**
	 * Convert SPI object to Mongo DBObject.
	 * 
	 * @param source
	 * @return
	 */
	public static BasicDBObject toDBObject(IDeviceAssignmentState source) {
		BasicDBObject result = new BasicDBObject();
		MongoDeviceAssignmentState.toDBObject(source, result);
		return result;
	}

	/**
	 * Convert a DBObject into the SPI equivalent.
	 * 
	 * @param source
	 * @return
	 */
	public static DeviceAssignmentState fromDBObject(DBObject source) {
		DeviceAssignmentState result = new DeviceAssignmentState();
		MongoDeviceAssignmentState.fromDBObject(source, result);
		return result;
	}
}