/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.mongodb.device;

import com.mongodb.*;
import com.openiot.core.OpenIoTPersistence;
import com.openiot.mongodb.IDeviceManagementMongoClient;
import com.openiot.mongodb.MongoPersistence;
import com.openiot.mongodb.common.MongoMetadataProvider;
import com.openiot.mongodb.common.MongoOpenIoTEntity;
import com.openiot.rest.model.device.*;
import com.openiot.rest.model.device.batch.BatchElement;
import com.openiot.rest.model.device.batch.BatchOperation;
import com.openiot.rest.model.device.command.DeviceCommand;
import com.openiot.rest.model.device.event.*;
import com.openiot.rest.model.device.group.DeviceGroup;
import com.openiot.rest.model.device.group.DeviceGroupElement;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.common.IMetadataProvider;
import com.openiot.spi.device.*;
import com.openiot.spi.device.batch.IBatchElement;
import com.openiot.spi.device.batch.IBatchOperation;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.event.*;
import com.openiot.spi.device.event.request.*;
import com.openiot.spi.device.group.IDeviceGroup;
import com.openiot.spi.device.group.IDeviceGroupElement;
import com.openiot.spi.device.request.*;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.search.IDateRangeSearchCriteria;
import com.openiot.spi.search.ISearchCriteria;
import com.openiot.spi.search.ISearchResults;
import com.openiot.spi.search.device.IBatchElementSearchCriteria;
import com.openiot.spi.search.device.IDeviceSearchCriteria;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Device management implementation that uses MongoDB for persistence.
 * 
 * @author dadams
 */
public class MongoDeviceManagement extends LifecycleComponent implements IDeviceManagement,
		ICachingDeviceManagement {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(MongoDeviceManagement.class);

	/** Injected with global OpenIoT Mongo client */
	private IDeviceManagementMongoClient mongoClient;

	/** Provides caching for device management entities */
	private IDeviceManagementCacheProvider cacheProvider;

	public MongoDeviceManagement() {
		super(LifecycleComponentType.DataStore);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	public void start() throws OpenIoTException {
		/** Ensure that collection indexes exist */
		ensureIndexes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#getLogger()
	 */
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ICachingDeviceManagement#setCacheProvider(com.openiot
	 * .spi.device.IDeviceManagementCacheProvider)
	 */
	public void setCacheProvider(IDeviceManagementCacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}

	public IDeviceManagementCacheProvider getCacheProvider() {
		return cacheProvider;
	}

	/**
	 * Ensure that expected collection indexes exist.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void ensureIndexes() throws OpenIoTException {
		getMongoClient().getSitesCollection().ensureIndex(new BasicDBObject(MongoSite.PROP_TOKEN, 1),
				new BasicDBObject("unique", true));
		getMongoClient().getDeviceSpecificationsCollection().ensureIndex(
				new BasicDBObject(MongoDeviceSpecification.PROP_TOKEN, 1), new BasicDBObject("unique", true));
		getMongoClient().getDevicesCollection().ensureIndex(
				new BasicDBObject(MongoDevice.PROP_HARDWARE_ID, 1), new BasicDBObject("unique", true));
		getMongoClient().getDeviceAssignmentsCollection().ensureIndex(
				new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, 1), new BasicDBObject("unique", true));
		getMongoClient().getEventsCollection().ensureIndex(
				new BasicDBObject(MongoDeviceEvent.PROP_DEVICE_ASSIGNMENT_TOKEN, 1).append(
						MongoDeviceEvent.PROP_EVENT_DATE, -1).append(MongoDeviceEvent.PROP_EVENT_TYPE, 1));
		getMongoClient().getEventsCollection().ensureIndex(
				new BasicDBObject(MongoDeviceEvent.PROP_SITE_TOKEN, 1).append(
						MongoDeviceEvent.PROP_EVENT_DATE, -1).append(MongoDeviceEvent.PROP_EVENT_TYPE, 1));
		getMongoClient().getDeviceGroupsCollection().ensureIndex(
				new BasicDBObject(MongoDeviceGroup.PROP_TOKEN, 1), new BasicDBObject("unique", true));
		getMongoClient().getDeviceGroupsCollection().ensureIndex(
				new BasicDBObject(MongoDeviceGroup.PROP_ROLES, 1));
		getMongoClient().getGroupElementsCollection().ensureIndex(
				new BasicDBObject(MongoDeviceGroupElement.PROP_GROUP_TOKEN, 1).append(
						MongoDeviceGroupElement.PROP_TYPE, 1).append(MongoDeviceGroupElement.PROP_ELEMENT_ID,
						1));
		getMongoClient().getGroupElementsCollection().ensureIndex(
				new BasicDBObject(MongoDeviceGroupElement.PROP_GROUP_TOKEN, 1).append(
						MongoDeviceGroupElement.PROP_ROLES, 1));
		getMongoClient().getBatchOperationsCollection().ensureIndex(
				new BasicDBObject(MongoBatchOperation.PROP_TOKEN, 1), new BasicDBObject("unique", true));
		getMongoClient().getBatchOperationElementsCollection().ensureIndex(
				new BasicDBObject(MongoBatchElement.PROP_BATCH_OPERATION_TOKEN, 1));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	public void stop() throws OpenIoTException {
		LOGGER.info("Mongo device management stopped.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#createDeviceSpecification(com.openiot
	 * .spi.device.request.IDeviceSpecificationCreateRequest)
	 */
	@Override
	public IDeviceSpecification createDeviceSpecification(IDeviceSpecificationCreateRequest request)
			throws OpenIoTException {
		String uuid = null;
		if (request.getToken() != null) {
			uuid = request.getToken();
		} else {
			uuid = UUID.randomUUID().toString();
		}

		// Use common logic so all backend implementations work the same.
		DeviceSpecification spec = OpenIoTPersistence.deviceSpecificationCreateLogic(request, uuid);

		DBCollection specs = getMongoClient().getDeviceSpecificationsCollection();
		DBObject created = MongoDeviceSpecification.toDBObject(spec);
		MongoPersistence.insert(specs, created);

		// Update cache with new data.
		if (getCacheProvider() != null) {
			getCacheProvider().getDeviceSpecificationCache().put(uuid, spec);
		}
		return MongoDeviceSpecification.fromDBObject(created);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#getDeviceSpecificationByToken(java.lang
	 * .String)
	 */
	@Override
	public IDeviceSpecification getDeviceSpecificationByToken(String token) throws OpenIoTException {
		if (getCacheProvider() != null) {
			IDeviceSpecification cached = getCacheProvider().getDeviceSpecificationCache().get(token);
			if (cached != null) {
				return cached;
			}
		}
		DBObject dbSpecification = getDeviceSpecificationDBObjectByToken(token);
		if (dbSpecification != null) {
			IDeviceSpecification result = MongoDeviceSpecification.fromDBObject(dbSpecification);
			if ((getCacheProvider() != null) && (result != null)) {
				getCacheProvider().getDeviceSpecificationCache().put(token, result);
			}
			return result;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#updateDeviceSpecification(java.lang.
	 * String, IDeviceSpecificationCreateRequest)
	 */
	@Override
	public IDeviceSpecification updateDeviceSpecification(String token,
			IDeviceSpecificationCreateRequest request) throws OpenIoTException {
		DBObject match = assertDeviceSpecification(token);
		DeviceSpecification spec = MongoDeviceSpecification.fromDBObject(match);

		// Use common update logic so that backend implemetations act the same way.
		OpenIoTPersistence.deviceSpecificationUpdateLogic(request, spec);
		DBObject updated = MongoDeviceSpecification.toDBObject(spec);

		BasicDBObject query = new BasicDBObject(MongoDeviceSpecification.PROP_TOKEN, token);
		DBCollection specs = getMongoClient().getDeviceSpecificationsCollection();
		MongoPersistence.update(specs, query, updated);

		// Update cache with new data.
		if (getCacheProvider() != null) {
			getCacheProvider().getDeviceSpecificationCache().put(token, spec);
		}
		return MongoDeviceSpecification.fromDBObject(updated);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#listDeviceSpecifications(boolean,
	 * ISearchCriteria)
	 */
	@Override
	public ISearchResults<IDeviceSpecification> listDeviceSpecifications(boolean includeDeleted,
			ISearchCriteria criteria) throws OpenIoTException {
		DBCollection specs = getMongoClient().getDeviceSpecificationsCollection();
		DBObject dbCriteria = new BasicDBObject();
		if (!includeDeleted) {
			MongoOpenIoTEntity.setDeleted(dbCriteria, false);
		}
		BasicDBObject sort = new BasicDBObject(MongoOpenIoTEntity.PROP_CREATED_DATE, -1);
		return MongoPersistence.search(IDeviceSpecification.class, specs, dbCriteria, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#deleteDeviceSpecification(java.lang.
	 * String, boolean)
	 */
	@Override
	public IDeviceSpecification deleteDeviceSpecification(String token, boolean force)
			throws OpenIoTException {
		DBObject existing = assertDeviceSpecification(token);
		DBCollection specs = getMongoClient().getDeviceSpecificationsCollection();
		if (force) {
			MongoPersistence.delete(specs, existing);
			return MongoDeviceSpecification.fromDBObject(existing);
		} else {
			MongoOpenIoTEntity.setDeleted(existing, true);
			BasicDBObject query = new BasicDBObject(MongoDeviceSpecification.PROP_TOKEN, token);
			MongoPersistence.update(specs, query, existing);
			return MongoDeviceSpecification.fromDBObject(existing);
		}
	}

	/**
	 * Return the {@link DBObject} for the device specification with the given token.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject getDeviceSpecificationDBObjectByToken(String token) throws OpenIoTException {
		DBCollection specs = getMongoClient().getDeviceSpecificationsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceSpecification.PROP_TOKEN, token);
		DBObject result = specs.findOne(query);
		return result;
	}

	/**
	 * Return the {@link DBObject} for the device specification with the given token.
	 * Throws an exception if the token is not valid.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject assertDeviceSpecification(String token) throws OpenIoTException {
		DBObject match = getDeviceSpecificationDBObjectByToken(token);
		if (match == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceSpecificationToken, ErrorLevel.ERROR);
		}
		return match;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#createDeviceCommand(com.openiot.spi
	 * .device.IDeviceSpecification,
	 * IDeviceCommandCreateRequest)
	 */
	@Override
	public IDeviceCommand createDeviceCommand(IDeviceSpecification spec, IDeviceCommandCreateRequest request)
			throws OpenIoTException {
		// Note: This allows duplicates if duplicate was marked deleted.
		List<IDeviceCommand> existing = listDeviceCommands(spec.getToken(), false);

		// Use common logic so all backend implementations work the same.
		String uuid = ((request.getToken() != null) ? request.getToken() : UUID.randomUUID().toString());
		DeviceCommand command = OpenIoTPersistence.deviceCommandCreateLogic(spec, request, uuid, existing);

		DBCollection commands = getMongoClient().getDeviceCommandsCollection();
		DBObject created = MongoDeviceCommand.toDBObject(command);
		MongoPersistence.insert(commands, created);
		return MongoDeviceCommand.fromDBObject(created);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#getDeviceCommandByToken(java.lang.String
	 * )
	 */
	@Override
	public IDeviceCommand getDeviceCommandByToken(String token) throws OpenIoTException {
		DBObject result = getDeviceCommandDBObjectByToken(token);
		if (result != null) {
			return MongoDeviceCommand.fromDBObject(result);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#updateDeviceCommand(java.lang.String,
	 * IDeviceCommandCreateRequest)
	 */
	@Override
	public IDeviceCommand updateDeviceCommand(String token, IDeviceCommandCreateRequest request)
			throws OpenIoTException {
		DBObject match = assertDeviceCommand(token);
		DeviceCommand command = MongoDeviceCommand.fromDBObject(match);

		// Note: This allows duplicates if duplicate was marked deleted.
		List<IDeviceCommand> existing = listDeviceCommands(token, false);

		// Use common update logic so that backend implemetations act the same way.
		OpenIoTPersistence.deviceCommandUpdateLogic(request, command, existing);
		DBObject updated = MongoDeviceCommand.toDBObject(command);

		BasicDBObject query = new BasicDBObject(MongoDeviceCommand.PROP_TOKEN, token);
		DBCollection commands = getMongoClient().getDeviceCommandsCollection();
		MongoPersistence.update(commands, query, updated);
		return MongoDeviceCommand.fromDBObject(updated);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceCommands(java.lang.String,
	 * boolean)
	 */
	@Override
	public List<IDeviceCommand> listDeviceCommands(String token, boolean includeDeleted)
			throws OpenIoTException {
		DBCollection commands = getMongoClient().getDeviceCommandsCollection();
		DBObject dbCriteria = new BasicDBObject();
		dbCriteria.put(MongoDeviceCommand.PROP_SPEC_TOKEN, token);
		if (!includeDeleted) {
			MongoOpenIoTEntity.setDeleted(dbCriteria, false);
		}
		BasicDBObject sort = new BasicDBObject(MongoDeviceCommand.PROP_NAME, 1);
		return MongoPersistence.list(IDeviceCommand.class, commands, dbCriteria, sort);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#deleteDeviceCommand(java.lang.String,
	 * boolean)
	 */
	@Override
	public IDeviceCommand deleteDeviceCommand(String token, boolean force) throws OpenIoTException {
		DBObject existing = assertDeviceCommand(token);
		DBCollection commands = getMongoClient().getDeviceCommandsCollection();
		if (force) {
			MongoPersistence.delete(commands, existing);
			return MongoDeviceCommand.fromDBObject(existing);
		} else {
			MongoOpenIoTEntity.setDeleted(existing, true);
			BasicDBObject query = new BasicDBObject(MongoDeviceCommand.PROP_TOKEN, token);
			MongoPersistence.update(commands, query, existing);
			return MongoDeviceCommand.fromDBObject(existing);
		}
	}

	/**
	 * Return the {@link DBObject} for the device command with the given token.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject getDeviceCommandDBObjectByToken(String token) throws OpenIoTException {
		DBCollection specs = getMongoClient().getDeviceCommandsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceCommand.PROP_TOKEN, token);
		DBObject result = specs.findOne(query);
		return result;
	}

	/**
	 * Return the {@link DBObject} for the device command with the given token. Throws an
	 * exception if the token is not valid.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject assertDeviceCommand(String token) throws OpenIoTException {
		DBObject match = getDeviceCommandDBObjectByToken(token);
		if (match == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceCommandToken, ErrorLevel.ERROR);
		}
		return match;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#createDevice(com.openiot.spi.device
	 * .request. IDeviceCreateRequest)
	 */
	@Override
	public IDevice createDevice(IDeviceCreateRequest request) throws OpenIoTException {
		IDevice existing = getDeviceByHardwareId(request.getHardwareId());
		if (existing != null) {
			throw new OpenIoTSystemException(ErrorCode.DuplicateHardwareId, ErrorLevel.ERROR,
					HttpServletResponse.SC_CONFLICT);
		}
		Device newDevice = OpenIoTPersistence.deviceCreateLogic(request);

		// Convert and save device data.
		DBCollection devices = getMongoClient().getDevicesCollection();
		DBObject created = MongoDevice.toDBObject(newDevice);
		MongoPersistence.insert(devices, created);

		// Update cache with new data.
		if (getCacheProvider() != null) {
			getCacheProvider().getDeviceCache().put(request.getHardwareId(), newDevice);
		}
		return newDevice;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#updateDevice(java.lang.String,
	 * IDeviceCreateRequest)
	 */
	@Override
	public IDevice updateDevice(String hardwareId, IDeviceCreateRequest request) throws OpenIoTException {
		DBObject existing = assertDevice(hardwareId);
		Device updatedDevice = MongoDevice.fromDBObject(existing);

		OpenIoTPersistence.deviceUpdateLogic(request, updatedDevice);
		DBObject updated = MongoDevice.toDBObject(updatedDevice);

		DBCollection devices = getMongoClient().getDevicesCollection();
		BasicDBObject query = new BasicDBObject(MongoDevice.PROP_HARDWARE_ID, hardwareId);
		MongoPersistence.update(devices, query, updated);

		// Update cache with new data.
		if (getCacheProvider() != null) {
			getCacheProvider().getDeviceCache().put(hardwareId, updatedDevice);
		}
		return MongoDevice.fromDBObject(updated);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#getDeviceByHardwareId(java
	 * .lang.String)
	 */
	@Override
	public IDevice getDeviceByHardwareId(String hardwareId) throws OpenIoTException {
		if (getCacheProvider() != null) {
			IDevice cached = getCacheProvider().getDeviceCache().get(hardwareId);
			if (cached != null) {
				return cached;
			}
		}
		DBObject dbDevice = getDeviceDBObjectByHardwareId(hardwareId);
		if (dbDevice != null) {
			IDevice result = MongoDevice.fromDBObject(dbDevice);
			if ((getCacheProvider() != null) && (result != null)) {
				getCacheProvider().getDeviceCache().put(hardwareId, result);
			}
			return result;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#getCurrentDeviceAssignment
	 * (com.openiot.spi.device .IDevice)
	 */
	@Override
	public IDeviceAssignment getCurrentDeviceAssignment(IDevice device) throws OpenIoTException {
		if (device.getAssignmentToken() == null) {
			return null;
		}
		return assertApiDeviceAssignment(device.getAssignmentToken());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#listDevices(boolean,
	 * IDeviceSearchCriteria)
	 */
	@Override
	public SearchResults<IDevice> listDevices(boolean includeDeleted, IDeviceSearchCriteria criteria)
			throws OpenIoTException {
		DBCollection devices = getMongoClient().getDevicesCollection();
		BasicDBObject dbCriteria = new BasicDBObject();
		if (!includeDeleted) {
			MongoOpenIoTEntity.setDeleted(dbCriteria, false);
		}
		if (criteria.isExcludeAssigned()) {
			dbCriteria.put(MongoDevice.PROP_ASSIGNMENT_TOKEN, null);
		}
		MongoPersistence.addDateSearchCriteria(dbCriteria, MongoOpenIoTEntity.PROP_CREATED_DATE, criteria);
		switch (criteria.getSearchType()) {
		case All: {
			break;
		}
		case UsesSpecification: {
			if (criteria.getDeviceBySpecificationParameters() != null) {
				String token = criteria.getDeviceBySpecificationParameters().getSpecificationToken();
				if (token == null) {
					throw new OpenIoTException("Invalid device search. No specification token passed.");
				}
				dbCriteria.put(MongoDevice.PROP_SPECIFICATION_TOKEN, token);
			}
			break;
		}
		}
		BasicDBObject sort = new BasicDBObject(MongoOpenIoTEntity.PROP_CREATED_DATE, -1);
		return MongoPersistence.search(IDevice.class, devices, dbCriteria, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#createDeviceElementMapping(java.lang
	 * .String, IDeviceElementMapping)
	 */
	@Override
	public IDevice createDeviceElementMapping(String hardwareId, IDeviceElementMapping mapping)
			throws OpenIoTException {
		return OpenIoTPersistence.deviceElementMappingCreateLogic(this, hardwareId, mapping);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#deleteDeviceElementMapping(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public IDevice deleteDeviceElementMapping(String hardwareId, String path) throws OpenIoTException {
		return OpenIoTPersistence.deviceElementMappingDeleteLogic(this, hardwareId, path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#deleteDevice(java.lang.String,
	 * boolean)
	 */
	@Override
	public IDevice deleteDevice(String hardwareId, boolean force) throws OpenIoTException {
		DBObject existing = assertDevice(hardwareId);
		Device device = MongoDevice.fromDBObject(existing);
		IDeviceAssignment assignment = getCurrentDeviceAssignment(device);
		if (assignment != null) {
			throw new OpenIoTSystemException(ErrorCode.DeviceCanNotBeDeletedIfAssigned, ErrorLevel.ERROR);
		}
		if (force) {
			DBCollection devices = getMongoClient().getDevicesCollection();
			MongoPersistence.delete(devices, existing);
			return MongoDevice.fromDBObject(existing);
		} else {
			MongoOpenIoTEntity.setDeleted(existing, true);
			BasicDBObject query = new BasicDBObject(MongoDevice.PROP_HARDWARE_ID, hardwareId);
			DBCollection devices = getMongoClient().getDevicesCollection();
			MongoPersistence.update(devices, query, existing);
			return MongoDevice.fromDBObject(existing);
		}
	}

	/**
	 * Get the DBObject containing site information that matches the given token.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject getDeviceDBObjectByHardwareId(String hardwareId) throws OpenIoTException {
		DBCollection devices = getMongoClient().getDevicesCollection();
		BasicDBObject query = new BasicDBObject(MongoDevice.PROP_HARDWARE_ID, hardwareId);
		DBObject result = devices.findOne(query);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#createDeviceAssignment(com.openiot
	 * .spi.device.request. IDeviceAssignmentCreateRequest)
	 */
	@Override
	public IDeviceAssignment createDeviceAssignment(IDeviceAssignmentCreateRequest request)
			throws OpenIoTException {
		DBObject deviceDb = assertDevice(request.getDeviceHardwareId());
		if (deviceDb.get(MongoDevice.PROP_ASSIGNMENT_TOKEN) != null) {
			throw new OpenIoTSystemException(ErrorCode.DeviceAlreadyAssigned, ErrorLevel.ERROR);
		}
		Device device = MongoDevice.fromDBObject(deviceDb);

		// Use common logic to load assignment from request.
		DeviceAssignment newAssignment =
				OpenIoTPersistence.deviceAssignmentCreateLogic(request, device,
                        UUID.randomUUID().toString());

		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		DBObject created = MongoDeviceAssignment.toDBObject(newAssignment);
		MongoPersistence.insert(assignments, created);

		// Update cache with new assignment data.
		if (getCacheProvider() != null) {
			getCacheProvider().getDeviceAssignmentCache().put(newAssignment.getToken(), newAssignment);
		}

		// Update device to point to created assignment.
		DBCollection devices = getMongoClient().getDevicesCollection();
		BasicDBObject query = new BasicDBObject(MongoDevice.PROP_HARDWARE_ID, request.getDeviceHardwareId());
		deviceDb.put(MongoDevice.PROP_ASSIGNMENT_TOKEN, newAssignment.getToken());
		MongoPersistence.update(devices, query, deviceDb);

		// Update cache with new device data.
		if (getCacheProvider() != null) {
			Device updated = MongoDevice.fromDBObject(deviceDb);
			getCacheProvider().getDeviceCache().put(updated.getHardwareId(), updated);
		}
		return newAssignment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#getDeviceAssignmentByToken
	 * (java.lang.String)
	 */
	@Override
	public IDeviceAssignment getDeviceAssignmentByToken(String token) throws OpenIoTException {
		if (getCacheProvider() != null) {
			IDeviceAssignment cached = getCacheProvider().getDeviceAssignmentCache().get(token);
			if (cached != null) {
				return cached;
			}
		}
		DBObject dbAssignment = getDeviceAssignmentDBObjectByToken(token);
		if (dbAssignment != null) {
			IDeviceAssignment result = MongoDeviceAssignment.fromDBObject(dbAssignment);
			if ((getCacheProvider() != null) && (result != null)) {
				getCacheProvider().getDeviceAssignmentCache().put(token, result);
			}
			return result;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#deleteDeviceAssignment(java.lang.String,
	 * boolean)
	 */
	@Override
	public IDeviceAssignment deleteDeviceAssignment(String token, boolean force) throws OpenIoTException {
		DBObject existing = assertDeviceAssignment(token);
		if (force) {
			DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
			MongoPersistence.delete(assignments, existing);
			return MongoDeviceAssignment.fromDBObject(existing);
		} else {
			MongoOpenIoTEntity.setDeleted(existing, true);
			BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
			DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
			MongoPersistence.update(assignments, query, existing);
			return MongoDeviceAssignment.fromDBObject(existing);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#getDeviceForAssignment(com
	 * .sitewhere.spi.device .IDeviceAssignment)
	 */
	@Override
	public IDevice getDeviceForAssignment(IDeviceAssignment assignment) throws OpenIoTException {
		return getDeviceByHardwareId(assignment.getDeviceHardwareId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#getSiteForAssignment(com.openiot
	 * .spi.device. IDeviceAssignment)
	 */
	@Override
	public ISite getSiteForAssignment(IDeviceAssignment assignment) throws OpenIoTException {
		return getSiteByToken(assignment.getSiteToken());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#updateDeviceAssignmentMetadata
	 * (java.lang.String, com.openiot.spi.device.IMetadataProvider)
	 */
	@Override
	public IDeviceAssignment updateDeviceAssignmentMetadata(String token, IMetadataProvider metadata)
			throws OpenIoTException {
		DBObject match = assertDeviceAssignment(token);
		MongoMetadataProvider.toDBObject(metadata, match);
		DeviceAssignment assignment = MongoDeviceAssignment.fromDBObject(match);
		OpenIoTPersistence.setUpdatedEntityMetadata(assignment);
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		MongoPersistence.update(assignments, query, MongoDeviceAssignment.toDBObject(assignment));

		// Update cache with new assignment data.
		if (getCacheProvider() != null) {
			getCacheProvider().getDeviceAssignmentCache().put(assignment.getToken(), assignment);
		}

		return MongoDeviceAssignment.fromDBObject(match);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#updateDeviceAssignmentStatus
	 * (java.lang.String, DeviceAssignmentStatus)
	 */
	@Override
	public IDeviceAssignment updateDeviceAssignmentStatus(String token, DeviceAssignmentStatus status)
			throws OpenIoTException {
		DBObject match = assertDeviceAssignment(token);
		match.put(MongoDeviceAssignment.PROP_STATUS, status.name());
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
		MongoPersistence.update(assignments, query, match);
		DeviceAssignment updated = MongoDeviceAssignment.fromDBObject(match);

		// Update cache with new assignment data.
		if (getCacheProvider() != null) {
			getCacheProvider().getDeviceAssignmentCache().put(updated.getToken(), updated);
		}

		return updated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#updateDeviceAssignmentState(java.lang
	 * .String, com.openiot.spi.device.IDeviceEventBatch)
	 */
	@Override
	public IDeviceAssignment updateDeviceAssignmentState(String token, IDeviceAssignmentState state)
			throws OpenIoTException {
		DBObject match = assertDeviceAssignment(token);
		MongoDeviceAssignment.setState(state, match);
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
		MongoPersistence.update(assignments, query, match);
		DeviceAssignment updated = MongoDeviceAssignment.fromDBObject(match);

		// Update cache with new assignment data.
		if (getCacheProvider() != null) {
			getCacheProvider().getDeviceAssignmentCache().put(updated.getToken(), updated);
		}

		return updated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#addDeviceEventBatch(java.lang.String,
	 * com.openiot.spi.device.IDeviceEventBatch)
	 */
	@Override
	public IDeviceEventBatchResponse addDeviceEventBatch(String assignmentToken, IDeviceEventBatch batch)
			throws OpenIoTException {
		return OpenIoTPersistence.deviceEventBatchLogic(assignmentToken, batch, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#endDeviceAssignment(java.lang
	 * .String)
	 */
	@Override
	public IDeviceAssignment endDeviceAssignment(String token) throws OpenIoTException {
		DBObject match = assertDeviceAssignment(token);
		match.put(MongoDeviceAssignment.PROP_RELEASED_DATE, Calendar.getInstance().getTime());
		match.put(MongoDeviceAssignment.PROP_STATUS, DeviceAssignmentStatus.Released.name());
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
		MongoPersistence.update(assignments, query, match);

		// Update cache with new assignment data.
		if (getCacheProvider() != null) {
			DeviceAssignment updated = MongoDeviceAssignment.fromDBObject(match);
			getCacheProvider().getDeviceAssignmentCache().put(updated.getToken(), updated);
		}

		// Remove device assignment reference.
		DBCollection devices = getMongoClient().getDevicesCollection();
		String hardwareId = (String) match.get(MongoDeviceAssignment.PROP_DEVICE_HARDWARE_ID);
		DBObject deviceMatch = getDeviceDBObjectByHardwareId(hardwareId);
		deviceMatch.removeField(MongoDevice.PROP_ASSIGNMENT_TOKEN);
		query = new BasicDBObject(MongoDevice.PROP_HARDWARE_ID, hardwareId);
		MongoPersistence.update(devices, query, deviceMatch);

		// Update cache with new device data.
		if (getCacheProvider() != null) {
			Device updated = MongoDevice.fromDBObject(deviceMatch);
			getCacheProvider().getDeviceCache().put(updated.getHardwareId(), updated);
		}

		DeviceAssignment assignment = MongoDeviceAssignment.fromDBObject(match);
		return assignment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#getDeviceAssignmentHistory(java.lang
	 * .String, com.openiot.spi.common.ISearchCriteria)
	 */
	@Override
	public SearchResults<IDeviceAssignment> getDeviceAssignmentHistory(String hardwareId,
			ISearchCriteria criteria) throws OpenIoTException {
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_DEVICE_HARDWARE_ID, hardwareId);
		BasicDBObject sort = new BasicDBObject(MongoDeviceAssignment.PROP_ACTIVE_DATE, -1);
		return MongoPersistence.search(IDeviceAssignment.class, assignments, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#getDeviceAssignmentsForSite(java.lang
	 * .String, com.openiot.spi.common.ISearchCriteria)
	 */
	@Override
	public SearchResults<IDeviceAssignment> getDeviceAssignmentsForSite(String siteToken,
			ISearchCriteria criteria) throws OpenIoTException {
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_SITE_TOKEN, siteToken);
		BasicDBObject sort = new BasicDBObject(MongoDeviceAssignment.PROP_ACTIVE_DATE, -1);
		return MongoPersistence.search(IDeviceAssignment.class, assignments, query, sort, criteria);
	}

	/**
	 * Find the DBObject for a device assignment based on unique token.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject getDeviceAssignmentDBObjectByToken(String token) throws OpenIoTException {
		DBCollection assignments = getMongoClient().getDeviceAssignmentsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceAssignment.PROP_TOKEN, token);
		DBObject result = assignments.findOne(query);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#getDeviceEventById(java.lang.String)
	 */
	@Override
	public IDeviceEvent getDeviceEventById(String id) throws OpenIoTException {
		DBObject searchById = new BasicDBObject("_id", new ObjectId(id));
		DBObject found = getMongoClient().getEventsCollection().findOne(searchById);
		if (found == null) {
			return null;
		}
		return MongoPersistence.unmarshalEvent(found);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#listDeviceEvents(java.lang.String,
	 * IDateRangeSearchCriteria)
	 */
	@Override
	public ISearchResults<IDeviceEvent> listDeviceEvents(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		DBCollection events = getMongoClient().getEventsCollection();
		BasicDBObject query =
				new BasicDBObject(MongoDeviceEvent.PROP_DEVICE_ASSIGNMENT_TOKEN, assignmentToken);
		MongoPersistence.addDateSearchCriteria(query, MongoDeviceEvent.PROP_EVENT_DATE, criteria);
		BasicDBObject sort =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1).append(
						MongoDeviceEvent.PROP_RECEIVED_DATE, -1);

		int offset = Math.max(0, criteria.getPageNumber() - 1) * criteria.getPageSize();
		DBCursor cursor = events.find(query).skip(offset).limit(criteria.getPageSize()).sort(sort);
		List<IDeviceEvent> matches = new ArrayList<IDeviceEvent>();
		SearchResults<IDeviceEvent> results = new SearchResults<IDeviceEvent>(matches);
		try {
			results.setNumResults(cursor.count());
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(MongoPersistence.unmarshalEvent(match));
			}
		} finally {
			cursor.close();
		}
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#addDeviceMeasurements(java.lang.String,
	 * IDeviceMeasurementsCreateRequest)
	 */
	@Override
	public IDeviceMeasurements addDeviceMeasurements(String assignmentToken,
			IDeviceMeasurementsCreateRequest request) throws OpenIoTException {
		IDeviceAssignment assignment = assertApiDeviceAssignment(assignmentToken);
		DeviceMeasurements measurements =
				OpenIoTPersistence.deviceMeasurementsCreateLogic(request, assignment);

		DBCollection events = getMongoClient().getEventsCollection();
		DBObject mObject = MongoDeviceMeasurements.toDBObject(measurements, false);
		MongoPersistence.insert(events, mObject);

		// Update assignment state if requested.
		measurements = MongoDeviceMeasurements.fromDBObject(mObject, false);
		if (request.isUpdateState()) {
			DeviceAssignmentState updated =
					OpenIoTPersistence.assignmentStateMeasurementsUpdateLogic(assignment, measurements);
			updateDeviceAssignmentState(assignmentToken, updated);
		}

		return measurements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceMeasurements(java.lang.String,
	 * com.openiot.spi.common.IDateRangeSearchCriteria)
	 */
	@Override
	public SearchResults<IDeviceMeasurements> listDeviceMeasurements(String token,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		DBCollection events = getMongoClient().getEventsCollection();
		BasicDBObject query =
				new BasicDBObject(MongoDeviceEvent.PROP_DEVICE_ASSIGNMENT_TOKEN, token).append(
						MongoDeviceEvent.PROP_EVENT_TYPE, DeviceEventType.Measurements.name());
		MongoPersistence.addDateSearchCriteria(query, MongoDeviceEvent.PROP_EVENT_DATE, criteria);
		BasicDBObject sort =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1).append(
						MongoDeviceEvent.PROP_RECEIVED_DATE, -1);
		return MongoPersistence.search(IDeviceMeasurements.class, events, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceMeasurementsForSite(java.lang
	 * .String, com.openiot.spi.common.IDateRangeSearchCriteria)
	 */
	@Override
	public SearchResults<IDeviceMeasurements> listDeviceMeasurementsForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		DBCollection events = getMongoClient().getEventsCollection();
		BasicDBObject query =
				new BasicDBObject(MongoDeviceEvent.PROP_SITE_TOKEN, siteToken).append(
						MongoDeviceEvent.PROP_EVENT_TYPE, DeviceEventType.Measurements.name());
		MongoPersistence.addDateSearchCriteria(query, MongoDeviceEvent.PROP_EVENT_DATE, criteria);
		BasicDBObject sort =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1).append(
						MongoDeviceEvent.PROP_RECEIVED_DATE, -1);
		return MongoPersistence.search(IDeviceMeasurements.class, events, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#addDeviceLocation(java.lang.String,
	 * IDeviceLocationCreateRequest)
	 */
	@Override
	public IDeviceLocation addDeviceLocation(String assignmentToken, IDeviceLocationCreateRequest request)
			throws OpenIoTException {
		IDeviceAssignment assignment = assertApiDeviceAssignment(assignmentToken);
		DeviceLocation location = OpenIoTPersistence.deviceLocationCreateLogic(assignment, request);

		DBCollection events = getMongoClient().getEventsCollection();
		DBObject locObject = MongoDeviceLocation.toDBObject(location, false);
		MongoPersistence.insert(events, locObject);

		// Update assignment state if requested.
		location = MongoDeviceLocation.fromDBObject(locObject, false);
		if (request.isUpdateState()) {
			DeviceAssignmentState updated =
					OpenIoTPersistence.assignmentStateLocationUpdateLogic(assignment, location);
			updateDeviceAssignmentState(assignment.getToken(), updated);
		}

		return location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceLocations(java.lang.String,
	 * com.openiot.spi.common.IDateRangeSearchCriteria)
	 */
	@Override
	public SearchResults<IDeviceLocation> listDeviceLocations(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		DBCollection events = getMongoClient().getEventsCollection();
		BasicDBObject query =
				new BasicDBObject(MongoDeviceEvent.PROP_DEVICE_ASSIGNMENT_TOKEN, assignmentToken).append(
						MongoDeviceEvent.PROP_EVENT_TYPE, DeviceEventType.Location.name());
		MongoPersistence.addDateSearchCriteria(query, MongoDeviceEvent.PROP_EVENT_DATE, criteria);
		BasicDBObject sort =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1).append(
						MongoDeviceEvent.PROP_RECEIVED_DATE, -1);
		return MongoPersistence.search(IDeviceLocation.class, events, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceLocationsForSite(java.lang
	 * .String, com.openiot.spi.common.IDateRangeSearchCriteria)
	 */
	@Override
	public SearchResults<IDeviceLocation> listDeviceLocationsForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		DBCollection events = getMongoClient().getEventsCollection();
		BasicDBObject query =
				new BasicDBObject(MongoDeviceEvent.PROP_SITE_TOKEN, siteToken).append(
						MongoDeviceEvent.PROP_EVENT_TYPE, DeviceEventType.Location.name());
		MongoPersistence.addDateSearchCriteria(query, MongoDeviceEvent.PROP_EVENT_DATE, criteria);
		BasicDBObject sort =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1).append(
						MongoDeviceEvent.PROP_RECEIVED_DATE, -1);
		return MongoPersistence.search(IDeviceLocation.class, events, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#listDeviceLocations(java.util.List,
	 * com.openiot.spi.common.IDateRangeSearchCriteria)
	 */
	@Override
	public SearchResults<IDeviceLocation> listDeviceLocations(List<String> assignmentTokens,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		DBCollection events = getMongoClient().getEventsCollection();
		BasicDBObject query = new BasicDBObject();
		query.put(MongoDeviceEvent.PROP_DEVICE_ASSIGNMENT_TOKEN, new BasicDBObject("$in", assignmentTokens));
		query.append(MongoDeviceEvent.PROP_EVENT_TYPE, DeviceEventType.Location.name());
		MongoPersistence.addDateSearchCriteria(query, MongoDeviceEvent.PROP_EVENT_DATE, criteria);
		BasicDBObject sort =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1).append(
						MongoDeviceEvent.PROP_RECEIVED_DATE, -1);
		return MongoPersistence.search(IDeviceLocation.class, events, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#addDeviceAlert(java.lang.String,
	 * IDeviceAlertCreateRequest)
	 */
	@Override
	public IDeviceAlert addDeviceAlert(String assignmentToken, IDeviceAlertCreateRequest request)
			throws OpenIoTException {
		IDeviceAssignment assignment = assertApiDeviceAssignment(assignmentToken);
		DeviceAlert alert = OpenIoTPersistence.deviceAlertCreateLogic(assignment, request);

		DBCollection events = getMongoClient().getEventsCollection();
		DBObject alertObject = MongoDeviceAlert.toDBObject(alert, false);
		MongoPersistence.insert(events, alertObject);

		// Update assignment state if requested.
		alert = MongoDeviceAlert.fromDBObject(alertObject, false);
		if (request.isUpdateState()) {
			DeviceAssignmentState updated =
					OpenIoTPersistence.assignmentStateAlertUpdateLogic(assignment, alert);
			updateDeviceAssignmentState(assignment.getToken(), updated);
		}

		return alert;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#listDeviceAlerts(java.lang.String,
	 * com.openiot.spi.common.IDateRangeSearchCriteria)
	 */
	@Override
	public SearchResults<IDeviceAlert> listDeviceAlerts(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		DBCollection events = getMongoClient().getEventsCollection();
		BasicDBObject query =
				new BasicDBObject(MongoDeviceEvent.PROP_DEVICE_ASSIGNMENT_TOKEN, assignmentToken).append(
						MongoDeviceEvent.PROP_EVENT_TYPE, DeviceEventType.Alert.name());
		MongoPersistence.addDateSearchCriteria(query, MongoDeviceEvent.PROP_EVENT_DATE, criteria);
		BasicDBObject sort =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1).append(
						MongoDeviceEvent.PROP_RECEIVED_DATE, -1);
		return MongoPersistence.search(IDeviceAlert.class, events, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceAlertsForSite(java.lang.String
	 * , com.openiot.spi.common.IDateRangeSearchCriteria)
	 */
	@Override
	public SearchResults<IDeviceAlert> listDeviceAlertsForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		DBCollection events = getMongoClient().getEventsCollection();
		BasicDBObject query =
				new BasicDBObject(MongoDeviceEvent.PROP_SITE_TOKEN, siteToken).append(
						MongoDeviceEvent.PROP_EVENT_TYPE, DeviceEventType.Alert.name());
		MongoPersistence.addDateSearchCriteria(query, MongoDeviceEvent.PROP_EVENT_DATE, criteria);
		BasicDBObject sort =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1).append(
						MongoDeviceEvent.PROP_RECEIVED_DATE, -1);
		return MongoPersistence.search(IDeviceAlert.class, events, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#addDeviceCommandInvocation(java.lang
	 * .String, IDeviceCommand,
	 * IDeviceCommandInvocationCreateRequest)
	 */
	@Override
	public IDeviceCommandInvocation addDeviceCommandInvocation(String assignmentToken,
			IDeviceCommand command, IDeviceCommandInvocationCreateRequest request) throws OpenIoTException {
		IDeviceAssignment assignment = assertApiDeviceAssignment(assignmentToken);
		DeviceCommandInvocation ci =
				OpenIoTPersistence.deviceCommandInvocationCreateLogic(assignment, command, request);

		DBCollection events = getMongoClient().getEventsCollection();
		DBObject ciObject = MongoDeviceCommandInvocation.toDBObject(ci);
		MongoPersistence.insert(events, ciObject);
		return MongoDeviceCommandInvocation.fromDBObject(ciObject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceCommandInvocations(java.lang
	 * .String, IDateRangeSearchCriteria)
	 */
	@Override
	public ISearchResults<IDeviceCommandInvocation> listDeviceCommandInvocations(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		DBCollection events = getMongoClient().getEventsCollection();
		BasicDBObject query =
				new BasicDBObject(MongoDeviceEvent.PROP_DEVICE_ASSIGNMENT_TOKEN, assignmentToken).append(
						MongoDeviceEvent.PROP_EVENT_TYPE, DeviceEventType.CommandInvocation.name());
		MongoPersistence.addDateSearchCriteria(query, MongoDeviceEvent.PROP_EVENT_DATE, criteria);
		BasicDBObject sort =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1).append(
						MongoDeviceEvent.PROP_RECEIVED_DATE, -1);
		return MongoPersistence.search(IDeviceCommandInvocation.class, events, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceCommandInvocationsForSite(
	 * java.lang.String, IDateRangeSearchCriteria)
	 */
	@Override
	public ISearchResults<IDeviceCommandInvocation> listDeviceCommandInvocationsForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		DBCollection events = getMongoClient().getEventsCollection();
		BasicDBObject query =
				new BasicDBObject(MongoDeviceEvent.PROP_SITE_TOKEN, siteToken).append(
						MongoDeviceEvent.PROP_EVENT_TYPE, DeviceEventType.CommandInvocation.name());
		MongoPersistence.addDateSearchCriteria(query, MongoDeviceEvent.PROP_EVENT_DATE, criteria);
		BasicDBObject sort =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1).append(
						MongoDeviceEvent.PROP_RECEIVED_DATE, -1);
		return MongoPersistence.search(IDeviceCommandInvocation.class, events, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceCommandInvocationResponses
	 * (java.lang.String)
	 */
	@Override
	public ISearchResults<IDeviceCommandResponse> listDeviceCommandInvocationResponses(String invocationId)
			throws OpenIoTException {
		DBCollection events = getMongoClient().getEventsCollection();
		BasicDBObject query =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_TYPE, DeviceEventType.CommandResponse.name()).append(
						MongoDeviceCommandResponse.PROP_ORIGINATING_EVENT_ID, invocationId);
		BasicDBObject sort =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1).append(
						MongoDeviceEvent.PROP_RECEIVED_DATE, -1);
		return MongoPersistence.search(IDeviceCommandResponse.class, events, query, sort);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#addDeviceCommandResponse(java.lang.String
	 * , IDeviceCommandResponseCreateRequest)
	 */
	@Override
	public IDeviceCommandResponse addDeviceCommandResponse(String assignmentToken,
			IDeviceCommandResponseCreateRequest request) throws OpenIoTException {
		IDeviceAssignment assignment = assertApiDeviceAssignment(assignmentToken);
		DeviceCommandResponse response =
				OpenIoTPersistence.deviceCommandResponseCreateLogic(assignment, request);

		DBCollection events = getMongoClient().getEventsCollection();
		DBObject dbresponse = MongoDeviceCommandResponse.toDBObject(response);
		MongoPersistence.insert(events, dbresponse);
		return MongoDeviceCommandResponse.fromDBObject(dbresponse);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceCommandResponses(java.lang
	 * .String, IDateRangeSearchCriteria)
	 */
	@Override
	public ISearchResults<IDeviceCommandResponse> listDeviceCommandResponses(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		DBCollection events = getMongoClient().getEventsCollection();
		BasicDBObject query =
				new BasicDBObject(MongoDeviceEvent.PROP_DEVICE_ASSIGNMENT_TOKEN, assignmentToken).append(
						MongoDeviceEvent.PROP_EVENT_TYPE, DeviceEventType.CommandResponse.name());
		MongoPersistence.addDateSearchCriteria(query, MongoDeviceEvent.PROP_EVENT_DATE, criteria);
		BasicDBObject sort =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1).append(
						MongoDeviceEvent.PROP_RECEIVED_DATE, -1);
		return MongoPersistence.search(IDeviceCommandResponse.class, events, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceCommandResponsesForSite(java
	 * .lang.String, IDateRangeSearchCriteria)
	 */
	@Override
	public ISearchResults<IDeviceCommandResponse> listDeviceCommandResponsesForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		DBCollection events = getMongoClient().getEventsCollection();
		BasicDBObject query =
				new BasicDBObject(MongoDeviceEvent.PROP_SITE_TOKEN, siteToken).append(
						MongoDeviceEvent.PROP_EVENT_TYPE, DeviceEventType.CommandResponse.name());
		MongoPersistence.addDateSearchCriteria(query, MongoDeviceEvent.PROP_EVENT_DATE, criteria);
		BasicDBObject sort =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1).append(
						MongoDeviceEvent.PROP_RECEIVED_DATE, -1);
		return MongoPersistence.search(IDeviceCommandResponse.class, events, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#addDeviceStateChange(java.lang.String,
	 * IDeviceStateChangeCreateRequest)
	 */
	@Override
	public IDeviceStateChange addDeviceStateChange(String assignmentToken,
			IDeviceStateChangeCreateRequest request) throws OpenIoTException {
		IDeviceAssignment assignment = assertApiDeviceAssignment(assignmentToken);
		DeviceStateChange state = OpenIoTPersistence.deviceStateChangeCreateLogic(assignment, request);

		DBCollection events = getMongoClient().getEventsCollection();
		DBObject dbstate = MongoDeviceStateChange.toDBObject(state);
		MongoPersistence.insert(events, dbstate);
		return MongoDeviceStateChange.fromDBObject(dbstate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceStateChanges(java.lang.String,
	 * IDateRangeSearchCriteria)
	 */
	@Override
	public ISearchResults<IDeviceStateChange> listDeviceStateChanges(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		DBCollection events = getMongoClient().getEventsCollection();
		BasicDBObject query =
				new BasicDBObject(MongoDeviceEvent.PROP_DEVICE_ASSIGNMENT_TOKEN, assignmentToken).append(
						MongoDeviceEvent.PROP_EVENT_TYPE, DeviceEventType.StateChange.name());
		MongoPersistence.addDateSearchCriteria(query, MongoDeviceEvent.PROP_EVENT_DATE, criteria);
		BasicDBObject sort =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1).append(
						MongoDeviceEvent.PROP_RECEIVED_DATE, -1);
		return MongoPersistence.search(IDeviceStateChange.class, events, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceStateChangesForSite(java.lang
	 * .String, IDateRangeSearchCriteria)
	 */
	@Override
	public ISearchResults<IDeviceStateChange> listDeviceStateChangesForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		DBCollection events = getMongoClient().getEventsCollection();
		BasicDBObject query =
				new BasicDBObject(MongoDeviceEvent.PROP_SITE_TOKEN, siteToken).append(
						MongoDeviceEvent.PROP_EVENT_TYPE, DeviceEventType.StateChange.name());
		MongoPersistence.addDateSearchCriteria(query, MongoDeviceEvent.PROP_EVENT_DATE, criteria);
		BasicDBObject sort =
				new BasicDBObject(MongoDeviceEvent.PROP_EVENT_DATE, -1).append(
						MongoDeviceEvent.PROP_RECEIVED_DATE, -1);
		return MongoPersistence.search(IDeviceStateChange.class, events, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#createSite(com.openiot.spi.device.
	 * request.ISiteCreateRequest )
	 */
	@Override
	public ISite createSite(ISiteCreateRequest request) throws OpenIoTException {
		// Use common logic so all backend implementations work the same.
		Site site = OpenIoTPersistence.siteCreateLogic(request, UUID.randomUUID().toString());

		DBCollection sites = getMongoClient().getSitesCollection();
		DBObject created = MongoSite.toDBObject(site);
		MongoPersistence.insert(sites, created);
		return MongoSite.fromDBObject(created);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#updateSite(java.lang.String,
	 * ISiteCreateRequest)
	 */
	@Override
	public ISite updateSite(String token, ISiteCreateRequest request) throws OpenIoTException {
		DBObject match = getSiteDBObjectByToken(token);
		if (match == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.ERROR);
		}
		Site site = MongoSite.fromDBObject(match);

		// Use common update logic so that backend implemetations act the same way.
		OpenIoTPersistence.siteUpdateLogic(request, site);

		DBObject updated = MongoSite.toDBObject(site);

		DBCollection sites = getMongoClient().getSitesCollection();
		BasicDBObject query = new BasicDBObject(MongoSite.PROP_TOKEN, token);
		MongoPersistence.update(sites, query, updated);
		if (getCacheProvider() != null) {
			getCacheProvider().getSiteCache().put(token, site);
		}
		return MongoSite.fromDBObject(updated);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#getSiteByToken(java.lang.String )
	 */
	@Override
	public ISite getSiteByToken(String token) throws OpenIoTException {
		DBObject dbSite = getSiteDBObjectByToken(token);
		if (dbSite != null) {
			return MongoSite.fromDBObject(dbSite);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#deleteSite(java.lang.String,
	 * boolean)
	 */
	@Override
	public ISite deleteSite(String siteToken, boolean force) throws OpenIoTException {
		DBObject existing = assertSite(siteToken);
		if (force) {
			DBCollection sites = getMongoClient().getSitesCollection();
			MongoPersistence.delete(sites, existing);
			if (getCacheProvider() != null) {
				getCacheProvider().getSiteCache().remove(siteToken);
			}
			return MongoSite.fromDBObject(existing);
		} else {
			MongoOpenIoTEntity.setDeleted(existing, true);
			BasicDBObject query = new BasicDBObject(MongoSite.PROP_TOKEN, siteToken);
			DBCollection sites = getMongoClient().getSitesCollection();
			MongoPersistence.update(sites, query, existing);
			if (getCacheProvider() != null) {
				getCacheProvider().getSiteCache().remove(siteToken);
			}
			return MongoSite.fromDBObject(existing);
		}
	}

	/**
	 * Get the DBObject containing site information that matches the given token.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject getSiteDBObjectByToken(String token) throws OpenIoTException {
		if (getCacheProvider() != null) {
			ISite cached = getCacheProvider().getSiteCache().get(token);
			if (cached != null) {
				return MongoSite.toDBObject(cached);
			}
		}
		DBCollection sites = getMongoClient().getSitesCollection();
		BasicDBObject query = new BasicDBObject(MongoSite.PROP_TOKEN, token);
		DBObject result = sites.findOne(query);
		if ((getCacheProvider() != null) && (result != null)) {
			ISite site = MongoSite.fromDBObject(result);
			getCacheProvider().getSiteCache().put(token, site);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#listSites(com.openiot.spi.common.
	 * ISearchCriteria)
	 */
	@Override
	public SearchResults<ISite> listSites(ISearchCriteria criteria) throws OpenIoTException {
		DBCollection sites = getMongoClient().getSitesCollection();
		BasicDBObject query = new BasicDBObject();
		BasicDBObject sort = new BasicDBObject(MongoSite.PROP_NAME, 1);
		return MongoPersistence.search(ISite.class, sites, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#createZone(com.openiot.spi.device.
	 * ISite, IZoneCreateRequest)
	 */
	@Override
	public IZone createZone(ISite site, IZoneCreateRequest request) throws OpenIoTException {
		Zone zone =
				OpenIoTPersistence.zoneCreateLogic(request, site.getToken(), UUID.randomUUID().toString());

		DBCollection zones = getMongoClient().getZonesCollection();
		DBObject created = MongoZone.toDBObject(zone);
		MongoPersistence.insert(zones, created);
		return MongoZone.fromDBObject(created);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#updateZone(java.lang.String,
	 * IZoneCreateRequest)
	 */
	@Override
	public IZone updateZone(String token, IZoneCreateRequest request) throws OpenIoTException {
		DBCollection zones = getMongoClient().getZonesCollection();
		DBObject match = assertZone(token);

		Zone zone = MongoZone.fromDBObject(match);
		OpenIoTPersistence.zoneUpdateLogic(request, zone);

		DBObject updated = MongoZone.toDBObject(zone);

		BasicDBObject query = new BasicDBObject(MongoSite.PROP_TOKEN, token);
		MongoPersistence.update(zones, query, updated);
		return MongoZone.fromDBObject(updated);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#getZone(java.lang.String)
	 */
	@Override
	public IZone getZone(String zoneToken) throws OpenIoTException {
		DBObject found = assertZone(zoneToken);
		return MongoZone.fromDBObject(found);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#listZones(java.lang.String,
	 * com.openiot.spi.common.ISearchCriteria)
	 */
	@Override
	public SearchResults<IZone> listZones(String siteToken, ISearchCriteria criteria)
			throws OpenIoTException {
		DBCollection zones = getMongoClient().getZonesCollection();
		BasicDBObject query = new BasicDBObject(MongoZone.PROP_SITE_TOKEN, siteToken);
		BasicDBObject sort = new BasicDBObject(MongoOpenIoTEntity.PROP_CREATED_DATE, -1);
		return MongoPersistence.search(IZone.class, zones, query, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#deleteZone(java.lang.String,
	 * boolean)
	 */
	@Override
	public IZone deleteZone(String zoneToken, boolean force) throws OpenIoTException {
		DBObject existing = assertZone(zoneToken);
		if (force) {
			DBCollection zones = getMongoClient().getZonesCollection();
			MongoPersistence.delete(zones, existing);
			return MongoZone.fromDBObject(existing);
		} else {
			MongoOpenIoTEntity.setDeleted(existing, true);
			BasicDBObject query = new BasicDBObject(MongoZone.PROP_TOKEN, zoneToken);
			DBCollection zones = getMongoClient().getZonesCollection();
			MongoPersistence.update(zones, query, existing);
			return MongoZone.fromDBObject(existing);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#createDeviceGroup(com.openiot.spi.
	 * device.request.IDeviceGroupCreateRequest)
	 */
	@Override
	public IDeviceGroup createDeviceGroup(IDeviceGroupCreateRequest request) throws OpenIoTException {
		String uuid = ((request.getToken() != null) ? request.getToken() : UUID.randomUUID().toString());
		DeviceGroup group = OpenIoTPersistence.deviceGroupCreateLogic(request, uuid);

		DBCollection groups = getMongoClient().getDeviceGroupsCollection();
		DBObject created = MongoDeviceGroup.toDBObject(group);
		MongoPersistence.insert(groups, created);
		return MongoDeviceGroup.fromDBObject(created);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#updateDeviceGroup(java.lang.String,
	 * IDeviceGroupCreateRequest)
	 */
	@Override
	public IDeviceGroup updateDeviceGroup(String token, IDeviceGroupCreateRequest request)
			throws OpenIoTException {
		DBCollection groups = getMongoClient().getDeviceGroupsCollection();
		DBObject match = assertDeviceGroup(token);

		DeviceGroup group = MongoDeviceGroup.fromDBObject(match);
		OpenIoTPersistence.deviceGroupUpdateLogic(request, group);

		DBObject updated = MongoDeviceGroup.toDBObject(group);

		// Manually copy last index since it's not copied by default.
		updated.put(MongoDeviceGroup.PROP_LAST_INDEX, match.get(MongoDeviceGroup.PROP_LAST_INDEX));

		BasicDBObject query = new BasicDBObject(MongoDeviceGroup.PROP_TOKEN, token);
		MongoPersistence.update(groups, query, updated);
		return MongoDeviceGroup.fromDBObject(updated);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#getDeviceGroup(java.lang.String)
	 */
	@Override
	public IDeviceGroup getDeviceGroup(String token) throws OpenIoTException {
		DBObject found = assertDeviceGroup(token);
		return MongoDeviceGroup.fromDBObject(found);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#listDeviceGroups(boolean,
	 * ISearchCriteria)
	 */
	@Override
	public ISearchResults<IDeviceGroup> listDeviceGroups(boolean includeDeleted, ISearchCriteria criteria)
			throws OpenIoTException {
		DBCollection groups = getMongoClient().getDeviceGroupsCollection();
		DBObject dbCriteria = new BasicDBObject();
		if (!includeDeleted) {
			MongoOpenIoTEntity.setDeleted(dbCriteria, false);
		}
		BasicDBObject sort = new BasicDBObject(MongoOpenIoTEntity.PROP_CREATED_DATE, -1);
		return MongoPersistence.search(IDeviceGroup.class, groups, dbCriteria, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceGroupsWithRole(java.lang.String
	 * , boolean, ISearchCriteria)
	 */
	@Override
	public ISearchResults<IDeviceGroup> listDeviceGroupsWithRole(String role, boolean includeDeleted,
			ISearchCriteria criteria) throws OpenIoTException {
		DBCollection groups = getMongoClient().getDeviceGroupsCollection();
		DBObject dbCriteria = new BasicDBObject(MongoDeviceGroup.PROP_ROLES, role);
		if (!includeDeleted) {
			MongoOpenIoTEntity.setDeleted(dbCriteria, false);
		}
		BasicDBObject sort = new BasicDBObject(MongoOpenIoTEntity.PROP_CREATED_DATE, -1);
		return MongoPersistence.search(IDeviceGroup.class, groups, dbCriteria, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#deleteDeviceGroup(java.lang.String,
	 * boolean)
	 */
	@Override
	public IDeviceGroup deleteDeviceGroup(String token, boolean force) throws OpenIoTException {
		DBObject existing = assertDeviceGroup(token);
		if (force) {
			DBCollection groups = getMongoClient().getDeviceGroupsCollection();
			MongoPersistence.delete(groups, existing);

			// Delete group elements as well.
			DBCollection elements = getMongoClient().getGroupElementsCollection();
			BasicDBObject match = new BasicDBObject(MongoDeviceGroupElement.PROP_GROUP_TOKEN, token);
			MongoPersistence.delete(elements, match);

			return MongoDeviceGroup.fromDBObject(existing);
		} else {
			MongoOpenIoTEntity.setDeleted(existing, true);
			BasicDBObject query = new BasicDBObject(MongoDeviceGroup.PROP_TOKEN, token);
			DBCollection groups = getMongoClient().getDeviceGroupsCollection();
			MongoPersistence.update(groups, query, existing);
			return MongoDeviceGroup.fromDBObject(existing);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#addDeviceGroupElements(java.lang.String,
	 * java.util.List)
	 */
	@Override
	public List<IDeviceGroupElement> addDeviceGroupElements(String groupToken,
			List<IDeviceGroupElementCreateRequest> elements) throws OpenIoTException {
		List<IDeviceGroupElement> results = new ArrayList<IDeviceGroupElement>();
		for (IDeviceGroupElementCreateRequest request : elements) {
			long index = MongoDeviceGroup.getNextGroupIndex(getMongoClient(), groupToken);
			DeviceGroupElement element =
					OpenIoTPersistence.deviceGroupElementCreateLogic(request, groupToken, index);
			DBObject created = MongoDeviceGroupElement.toDBObject(element);
			MongoPersistence.insert(getMongoClient().getGroupElementsCollection(), created);
			results.add(MongoDeviceGroupElement.fromDBObject(created));
		}
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#removeDeviceGroupElements(java.lang.
	 * String, java.util.List)
	 */
	@Override
	public List<IDeviceGroupElement> removeDeviceGroupElements(String groupToken,
			List<IDeviceGroupElementCreateRequest> elements) throws OpenIoTException {
		List<IDeviceGroupElement> deleted = new ArrayList<IDeviceGroupElement>();
		for (IDeviceGroupElementCreateRequest request : elements) {
			BasicDBObject match =
					new BasicDBObject(MongoDeviceGroupElement.PROP_GROUP_TOKEN, groupToken).append(
							MongoDeviceGroupElement.PROP_TYPE, request.getType().name()).append(
							MongoDeviceGroupElement.PROP_ELEMENT_ID, request.getElementId());
			DBCursor found = getMongoClient().getGroupElementsCollection().find(match);
			while (found.hasNext()) {
				DBObject current = found.next();
				WriteResult result =
						MongoPersistence.delete(getMongoClient().getGroupElementsCollection(), current);
				if (result.getN() > 0) {
					deleted.add(MongoDeviceGroupElement.fromDBObject(current));
				}
			}
		}
		return deleted;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceGroupElements(java.lang.String
	 * , ISearchCriteria)
	 */
	@Override
	public SearchResults<IDeviceGroupElement> listDeviceGroupElements(String groupToken,
			ISearchCriteria criteria) throws OpenIoTException {
		BasicDBObject match = new BasicDBObject(MongoDeviceGroupElement.PROP_GROUP_TOKEN, groupToken);
		BasicDBObject sort = new BasicDBObject(MongoDeviceGroupElement.PROP_INDEX, 1);
		return MongoPersistence.search(IDeviceGroupElement.class,
				getMongoClient().getGroupElementsCollection(), match, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#createBatchOperation(com.openiot.spi
	 * .device.request.IBatchOperationCreateRequest)
	 */
	@Override
	public IBatchOperation createBatchOperation(IBatchOperationCreateRequest request)
			throws OpenIoTException {
		String uuid = ((request.getToken() != null) ? request.getToken() : UUID.randomUUID().toString());
		BatchOperation batch = OpenIoTPersistence.batchOperationCreateLogic(request, uuid);

		DBCollection batches = getMongoClient().getBatchOperationsCollection();
		DBObject created = MongoBatchOperation.toDBObject(batch);
		MongoPersistence.insert(batches, created);

		// Insert element for each hardware id.
		long index = 0;
		DBCollection elements = getMongoClient().getBatchOperationElementsCollection();
		for (String hardwareId : request.getHardwareIds()) {
			BatchElement element =
					OpenIoTPersistence.batchElementCreateLogic(batch.getToken(), hardwareId, ++index);
			DBObject dbElement = MongoBatchElement.toDBObject(element);
			MongoPersistence.insert(elements, dbElement);
		}

		return MongoBatchOperation.fromDBObject(created);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#updateBatchOperation(java.lang.String,
	 * IBatchOperationUpdateRequest)
	 */
	@Override
	public IBatchOperation updateBatchOperation(String token, IBatchOperationUpdateRequest request)
			throws OpenIoTException {
		DBCollection batchops = getMongoClient().getBatchOperationsCollection();
		DBObject match = assertBatchOperation(token);

		BatchOperation operation = MongoBatchOperation.fromDBObject(match);
		OpenIoTPersistence.batchOperationUpdateLogic(request, operation);

		DBObject updated = MongoBatchOperation.toDBObject(operation);

		BasicDBObject query = new BasicDBObject(MongoBatchOperation.PROP_TOKEN, token);
		MongoPersistence.update(batchops, query, updated);
		return MongoBatchOperation.fromDBObject(updated);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#getBatchOperation(java.lang.String)
	 */
	@Override
	public IBatchOperation getBatchOperation(String token) throws OpenIoTException {
		DBObject found = assertBatchOperation(token);
		return MongoBatchOperation.fromDBObject(found);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#listBatchOperations(boolean,
	 * ISearchCriteria)
	 */
	@Override
	public ISearchResults<IBatchOperation> listBatchOperations(boolean includeDeleted,
			ISearchCriteria criteria) throws OpenIoTException {
		DBCollection ops = getMongoClient().getBatchOperationsCollection();
		DBObject dbCriteria = new BasicDBObject();
		if (!includeDeleted) {
			MongoOpenIoTEntity.setDeleted(dbCriteria, false);
		}
		BasicDBObject sort = new BasicDBObject(MongoOpenIoTEntity.PROP_CREATED_DATE, -1);
		return MongoPersistence.search(IBatchOperation.class, ops, dbCriteria, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#deleteBatchOperation(java.lang.String,
	 * boolean)
	 */
	@Override
	public IBatchOperation deleteBatchOperation(String token, boolean force) throws OpenIoTException {
		DBObject existing = assertBatchOperation(token);
		if (force) {
			DBCollection ops = getMongoClient().getBatchOperationsCollection();
			MongoPersistence.delete(ops, existing);

			// Delete operation elements as well.
			DBCollection elements = getMongoClient().getBatchOperationElementsCollection();
			BasicDBObject match = new BasicDBObject(MongoBatchElement.PROP_BATCH_OPERATION_TOKEN, token);
			MongoPersistence.delete(elements, match);

			return MongoBatchOperation.fromDBObject(existing);
		} else {
			MongoOpenIoTEntity.setDeleted(existing, true);
			BasicDBObject query = new BasicDBObject(MongoDeviceGroup.PROP_TOKEN, token);
			DBCollection ops = getMongoClient().getBatchOperationsCollection();
			MongoPersistence.update(ops, query, existing);
			return MongoBatchOperation.fromDBObject(existing);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#listBatchElements(java.lang.String,
	 * IBatchElementSearchCriteria)
	 */
	@Override
	public SearchResults<IBatchElement> listBatchElements(String batchToken,
			IBatchElementSearchCriteria criteria) throws OpenIoTException {
		DBCollection elements = getMongoClient().getBatchOperationElementsCollection();
		DBObject dbCriteria = new BasicDBObject(MongoBatchElement.PROP_BATCH_OPERATION_TOKEN, batchToken);
		if (criteria.getProcessingStatus() != null) {
			dbCriteria.put(MongoBatchElement.PROP_PROCESSING_STATUS, criteria.getProcessingStatus());
		}
		BasicDBObject sort = new BasicDBObject(MongoBatchElement.PROP_INDEX, 1);
		return MongoPersistence.search(IBatchElement.class, elements, dbCriteria, sort, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#updateBatchElement(java.lang.String,
	 * long, IBatchElementUpdateRequest)
	 */
	@Override
	public IBatchElement updateBatchElement(String operationToken, long index,
			IBatchElementUpdateRequest request) throws OpenIoTException {
		DBCollection elements = getMongoClient().getBatchOperationElementsCollection();
		DBObject dbElement = assertBatchElement(operationToken, index);

		BatchElement element = MongoBatchElement.fromDBObject(dbElement);
		OpenIoTPersistence.batchElementUpdateLogic(request, element);

		DBObject updated = MongoBatchElement.toDBObject(element);

		BasicDBObject query =
				new BasicDBObject(MongoBatchElement.PROP_BATCH_OPERATION_TOKEN, operationToken).append(
						MongoBatchElement.PROP_INDEX, index);
		MongoPersistence.update(elements, query, updated);
		return MongoBatchElement.fromDBObject(updated);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#createBatchCommandInvocation(com.openiot
	 * .spi.device.request.IBatchCommandInvocationRequest)
	 */
	@Override
	public IBatchOperation createBatchCommandInvocation(IBatchCommandInvocationRequest request)
			throws OpenIoTException {
		String uuid = ((request.getToken() != null) ? request.getToken() : UUID.randomUUID().toString());
		IBatchOperationCreateRequest generic =
				OpenIoTPersistence.batchCommandInvocationCreateLogic(request, uuid);
		return createBatchOperation(generic);
	}

	/**
	 * Return the {@link DBObject} for the site with the given token. Throws an exception
	 * if the token is not found.
	 * 
	 * @param hardwareId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject assertSite(String token) throws OpenIoTException {
		DBObject match = getSiteDBObjectByToken(token);
		if (match == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.INFO);
		}
		return match;
	}

	/**
	 * Return the {@link DBObject} for the device with the given hardware id. Throws an
	 * exception if the hardware id is not found.
	 * 
	 * @param hardwareId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject assertDevice(String hardwareId) throws OpenIoTException {
		if (getCacheProvider() != null) {

		}
		DBObject match = getDeviceDBObjectByHardwareId(hardwareId);
		if (match == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidHardwareId, ErrorLevel.INFO);
		}
		return match;
	}

	/**
	 * Return the {@link DBObject} for the assignment with the given token. Throws an
	 * exception if the token is not valid.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject assertDeviceAssignment(String token) throws OpenIoTException {
		DBObject match = getDeviceAssignmentDBObjectByToken(token);
		if (match == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceAssignmentToken, ErrorLevel.ERROR);
		}
		return match;
	}

	/**
	 * Return an {@link IDeviceAssignment} for the given token. Throws an exception if the
	 * token is not valid.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected IDeviceAssignment assertApiDeviceAssignment(String token) throws OpenIoTException {
		if (getCacheProvider() != null) {
			IDeviceAssignment result = getCacheProvider().getDeviceAssignmentCache().get(token);
			if (result != null) {
				return result;
			}
		}
		DBObject match = assertDeviceAssignment(token);
		IDeviceAssignment result = MongoDeviceAssignment.fromDBObject(match);
		if ((getCacheProvider() != null) && (result != null)) {
			getCacheProvider().getDeviceAssignmentCache().put(token, result);
		}
		return result;
	}

	/**
	 * Return the {@link DBObject} for the zone with the given token.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject getZoneDBObjectByToken(String token) throws OpenIoTException {
		DBCollection zones = getMongoClient().getZonesCollection();
		BasicDBObject query = new BasicDBObject(MongoZone.PROP_TOKEN, token);
		DBObject result = zones.findOne(query);
		return result;
	}

	/**
	 * Return the {@link DBObject} for the zone with the given token. Throws an exception
	 * if the token is not valid.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject assertZone(String token) throws OpenIoTException {
		DBObject match = getZoneDBObjectByToken(token);
		if (match == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidZoneToken, ErrorLevel.ERROR);
		}
		return match;
	}

	/**
	 * Returns the {@link DBObject} for the device group with the given token. Returns
	 * null if not found.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject getDeviceGroupDBObjectByToken(String token) throws OpenIoTException {
		DBCollection groups = getMongoClient().getDeviceGroupsCollection();
		BasicDBObject query = new BasicDBObject(MongoDeviceGroup.PROP_TOKEN, token);
		DBObject result = groups.findOne(query);
		return result;
	}

	/**
	 * Return the {@link DBObject} for the device group with the given token. Throws an
	 * exception if the token is not valid.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject assertDeviceGroup(String token) throws OpenIoTException {
		DBObject match = getDeviceGroupDBObjectByToken(token);
		if (match == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceGroupToken, ErrorLevel.ERROR);
		}
		return match;
	}

	/**
	 * Returns the {@link DBObject} for the batch operation with the given token. Returns
	 * null if not found.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject getBatchOperationDBObjectByToken(String token) throws OpenIoTException {
		DBCollection ops = getMongoClient().getBatchOperationsCollection();
		BasicDBObject query = new BasicDBObject(MongoBatchOperation.PROP_TOKEN, token);
		DBObject result = ops.findOne(query);
		return result;
	}

	/**
	 * Return the {@link DBObject} for the batch operation with the given token. Throws an
	 * exception if the token is not valid.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject assertBatchOperation(String token) throws OpenIoTException {
		DBObject match = getBatchOperationDBObjectByToken(token);
		if (match == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidBatchOperationToken, ErrorLevel.ERROR);
		}
		return match;
	}

	/**
	 * Return the {@link DBObject} for the batch operation element based on the token for
	 * its parent operation and its index.
	 * 
	 * @param operationToken
	 * @param index
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected DBObject getBatchElementDBObjectByIndex(String operationToken, long index)
			throws OpenIoTException {
		DBCollection ops = getMongoClient().getBatchOperationElementsCollection();
		BasicDBObject query =
				new BasicDBObject(MongoBatchElement.PROP_BATCH_OPERATION_TOKEN, operationToken).append(
						MongoBatchElement.PROP_INDEX, index);
		DBObject result = ops.findOne(query);
		return result;
	}

	protected DBObject assertBatchElement(String operationToken, long index) throws OpenIoTException {
		DBObject match = getBatchElementDBObjectByIndex(operationToken, index);
		if (match == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidBatchElement, ErrorLevel.ERROR);
		}
		return match;
	}

	public IDeviceManagementMongoClient getMongoClient() {
		return mongoClient;
	}

	public void setMongoClient(IDeviceManagementMongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}
}