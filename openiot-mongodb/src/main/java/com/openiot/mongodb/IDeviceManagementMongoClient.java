/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.mongodb;

import com.mongodb.DBCollection;

/**
 * Mongo client that provides device management collections.
 * 
 * @author Derek
 */
public interface IDeviceManagementMongoClient {

	/** Default collection name for OpenIoT sites */
	public static final String DEFAULT_SITES_COLLECTION_NAME = "sites";

	/** Default collection name for OpenIoT zones */
	public static final String DEFAULT_ZONES_COLLECTION_NAME = "zones";

	/** Default collection name for OpenIoT device specifications */
	public static final String DEFAULT_DEVICE_SPECIFICATIONS_COLLECTION_NAME = "specifications";

	/** Default collection name for OpenIoT device commands */
	public static final String DEFAULT_DEVICE_COMMANDS_COLLECTION_NAME = "commands";

	/** Default collection name for OpenIoT devices */
	public static final String DEFAULT_DEVICES_COLLECTION_NAME = "devices";

	/** Default collection name for OpenIoT device groups */
	public static final String DEFAULT_DEVICE_GROUPS_COLLECTION_NAME = "devicegroups";

	/** Default collection name for OpenIoT device group elements */
	public static final String DEFAULT_DEVICE_GROUP_ELEMENTS_COLLECTION_NAME = "groupelements";

	/** Default collection name for OpenIoT device assignments */
	public static final String DEFAULT_DEVICE_ASSIGNMENTS_COLLECTION_NAME = "assignments";

	/** Default collection name for OpenIoT events */
	public static final String DEFAULT_EVENTS_COLLECTION_NAME = "events";

	/** Default collection name for OpenIoT batch operations */
	public static final String DEFAULT_BATCH_OPERATIONS_COLLECTION_NAME = "batchoperations";

	/** Default collection name for OpenIoT batch operation elements */
	public static final String DEFAULT_BATCH_OPERATION_ELEMENTS_COLLECTION_NAME = "batchopelements";

	public DBCollection getDeviceSpecificationsCollection();

	public DBCollection getDeviceCommandsCollection();

	public DBCollection getDevicesCollection();

	public DBCollection getDeviceAssignmentsCollection();

	public DBCollection getSitesCollection();

	public DBCollection getZonesCollection();

	public DBCollection getDeviceGroupsCollection();

	public DBCollection getGroupElementsCollection();

	public DBCollection getEventsCollection();

	public DBCollection getBatchOperationsCollection();

	public DBCollection getBatchOperationElementsCollection();
}
