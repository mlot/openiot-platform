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
import com.openiot.mongodb.common.MongoMetadataProvider;
import com.openiot.rest.model.device.event.DeviceEvent;
import com.openiot.spi.device.DeviceAssignmentType;
import com.openiot.spi.device.event.DeviceEventType;
import com.openiot.spi.device.event.IDeviceEvent;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Used to load or save device event data to MongoDB.
 * 
 * @author dadams
 */
public class MongoDeviceEvent {

	/** Event id for events nested in other objects */
	public static final String PROP_NESTED_ID = "eventId";

	/** Event type indicator */
	public static final String PROP_EVENT_TYPE = "eventType";

	/** Property for site token */
	public static final String PROP_SITE_TOKEN = "siteToken";

	/** Property for device assignment token */
	public static final String PROP_DEVICE_ASSIGNMENT_TOKEN = "deviceAssignmentToken";

	/** Property for device assignment type */
	public static final String PROP_DEVICE_ASSIGNMENT_TYPE = "deviceAssignmentType";

	/** Property for asset module id */
	public static final String PROP_ASSET_MODULE_ID = "assetModuleId";

	/** Property for asset id */
	public static final String PROP_ASSET_ID = "assetId";

	/** Property for time measurements were taken */
	public static final String PROP_EVENT_DATE = "eventDate";

	/** Property for time measurements were received */
	public static final String PROP_RECEIVED_DATE = "receivedDate";

	/**
	 * Copy information from SPI into Mongo DBObject.
	 * 
	 * @param source
	 * @param target
	 * @param isNested
	 */
	public static void toDBObject(IDeviceEvent source, BasicDBObject target, boolean isNested) {
		if (isNested) {
			target.append(PROP_NESTED_ID, source.getId());
		}
		target.append(PROP_EVENT_TYPE, source.getEventType().name());
		target.append(PROP_SITE_TOKEN, source.getSiteToken());
		target.append(PROP_DEVICE_ASSIGNMENT_TOKEN, source.getDeviceAssignmentToken());
		target.append(PROP_DEVICE_ASSIGNMENT_TYPE, source.getAssignmentType().name());
		target.append(PROP_ASSET_MODULE_ID, source.getAssetModuleId());
		target.append(PROP_ASSET_ID, source.getAssetId());
		target.append(PROP_EVENT_DATE, source.getEventDate());
		target.append(PROP_RECEIVED_DATE, source.getReceivedDate());

		MongoMetadataProvider.toDBObject(source, target);
	}

	/**
	 * Copy information from Mongo DBObject to model object.
	 * 
	 * @param source
	 * @param target
	 * @param isNested
	 */
	public static void fromDBObject(DBObject source, DeviceEvent target, boolean isNested) {
		ObjectId id = (ObjectId) source.get("_id");
		String eventType = (String) source.get(PROP_EVENT_TYPE);
		String siteToken = (String) source.get(PROP_SITE_TOKEN);
		String assignmentToken = (String) source.get(PROP_DEVICE_ASSIGNMENT_TOKEN);
		String assignmentType = (String) source.get(PROP_DEVICE_ASSIGNMENT_TYPE);
		String assetModuleId = (String) source.get(PROP_ASSET_MODULE_ID);
		String assetId = (String) source.get(PROP_ASSET_ID);
		Date eventDate = (Date) source.get(PROP_EVENT_DATE);
		Date receivedDate = (Date) source.get(PROP_RECEIVED_DATE);

		if (id != null) {
			target.setId(id.toString());
		}
		if (isNested) {
			target.setId((String) source.get(PROP_NESTED_ID));
		}
		if (eventType != null) {
			target.setEventType(DeviceEventType.valueOf(eventType));
		}
		target.setSiteToken(siteToken);
		target.setDeviceAssignmentToken(assignmentToken);
		target.setAssetModuleId(assetModuleId);
		target.setAssetId(assetId);
		target.setEventDate(eventDate);
		target.setReceivedDate(receivedDate);

		if (assignmentType != null) {
			target.setAssignmentType(DeviceAssignmentType.valueOf(assignmentType));
		}

		MongoMetadataProvider.fromDBObject(source, target);
	}
}