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
import com.openiot.mongodb.IDeviceManagementMongoClient;
import com.openiot.mongodb.MongoConverter;
import com.openiot.mongodb.common.MongoMetadataProvider;
import com.openiot.mongodb.common.MongoOpenIoTEntity;
import com.openiot.rest.model.device.group.DeviceGroup;
import com.openiot.spi.device.group.IDeviceGroup;

import java.util.List;

/**
 * Used to load or save device group data to MongoDB.
 * 
 * @author dadams
 */
public class MongoDeviceGroup implements MongoConverter<IDeviceGroup> {

	/** Property for unique token */
	public static final String PROP_TOKEN = "token";

	/** Property for name */
	public static final String PROP_NAME = "name";

	/** Property for description */
	public static final String PROP_DESCRIPTION = "description";

	/** Property for list of roles */
	public static final String PROP_ROLES = "roles";

	/** Property for element list */
	public static final String PROP_LAST_INDEX = "lastIndex";

	/*
	 * (non-Javadoc)
	 * 
	 * @see MongoConverter#convert(java.lang.Object)
	 */
	@Override
	public BasicDBObject convert(IDeviceGroup source) {
		return MongoDeviceGroup.toDBObject(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MongoConverter#convert(com.mongodb.DBObject)
	 */
	@Override
	public IDeviceGroup convert(DBObject source) {
		return MongoDeviceGroup.fromDBObject(source);
	}

	/**
	 * Copy information from SPI into Mongo DBObject.
	 * 
	 * @param source
	 * @param target
	 */
	public static void toDBObject(IDeviceGroup source, BasicDBObject target) {
		target.append(PROP_TOKEN, source.getToken());
		target.append(PROP_NAME, source.getName());
		target.append(PROP_DESCRIPTION, source.getDescription());
		target.append(PROP_ROLES, source.getRoles());
		MongoOpenIoTEntity.toDBObject(source, target);
		MongoMetadataProvider.toDBObject(source, target);
	}

	/**
	 * Copy information from Mongo DBObject to model object.
	 * 
	 * @param source
	 * @param target
	 */
	@SuppressWarnings("unchecked")
	public static void fromDBObject(DBObject source, DeviceGroup target) {
		String token = (String) source.get(PROP_TOKEN);
		String name = (String) source.get(PROP_NAME);
		String desc = (String) source.get(PROP_DESCRIPTION);
		List<String> roles = (List<String>) source.get(PROP_ROLES);

		target.setToken(token);
		target.setName(name);
		target.setDescription(desc);
		target.setRoles(roles);
		MongoOpenIoTEntity.fromDBObject(source, target);
		MongoMetadataProvider.fromDBObject(source, target);
	}

	/**
	 * Convert SPI object to Mongo DBObject.
	 * 
	 * @param source
	 * @return
	 */
	public static BasicDBObject toDBObject(IDeviceGroup source) {
		BasicDBObject result = new BasicDBObject();
		MongoDeviceGroup.toDBObject(source, result);
		return result;
	}

	/**
	 * Convert a DBObject into the SPI equivalent.
	 * 
	 * @param source
	 * @return
	 */
	public static DeviceGroup fromDBObject(DBObject source) {
		DeviceGroup result = new DeviceGroup();
		MongoDeviceGroup.fromDBObject(source, result);
		return result;
	}

	/**
	 * Get the next available index for the group.
	 * 
	 * @param mongo
	 * @param token
	 * @return
	 */
	public static long getNextGroupIndex(IDeviceManagementMongoClient mongo, String token) {
		BasicDBObject query = new BasicDBObject(MongoDeviceGroup.PROP_TOKEN, token);
		BasicDBObject update = new BasicDBObject(MongoDeviceGroup.PROP_LAST_INDEX, (long) 1);
		BasicDBObject increment = new BasicDBObject("$inc", update);
		DBObject updated =
				mongo.getDeviceGroupsCollection().findAndModify(query, new BasicDBObject(),
						new BasicDBObject(), false, increment, true, true);
		return (Long) updated.get(PROP_LAST_INDEX);
	}
}