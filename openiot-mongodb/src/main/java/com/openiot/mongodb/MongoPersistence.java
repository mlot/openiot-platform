/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.mongodb;

import com.mongodb.*;
import com.openiot.mongodb.device.*;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.DeviceEventType;
import com.openiot.spi.device.event.IDeviceEvent;
import com.openiot.spi.search.IDateRangeSearchCriteria;
import com.openiot.spi.search.ISearchCriteria;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * Common handlers for persisting Mongo data.
 * 
 * @author Derek
 */
public class MongoPersistence {

	/** Static logger instance */
	@SuppressWarnings("unused")
	private static Logger LOGGER = Logger.getLogger(MongoPersistence.class);

	/** Default lookup */
	private static IMongoConverterLookup LOOKUP = new MongoConverters();

	/**
	 * Common handler for creating new objects. Assures that errors are handled in a
	 * consistent way.
	 * 
	 * @param collection
	 * @param object
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static void insert(DBCollection collection, DBObject object) throws OpenIoTException {
		WriteResult result = collection.insert(object);
		if (!result.getLastError().ok()) {
			throw new OpenIoTException("Error during insert: " + result.getLastError().toString());
		}
	}

	/**
	 * Common handler for updating existing objects. Assures that errors are handled in a
	 * consistent way.
	 * 
	 * @param collection
	 * @param object
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static void update(DBCollection collection, DBObject query, DBObject object)
			throws OpenIoTException {
		WriteResult result = collection.update(query, object);
		if (!result.getLastError().ok()) {
			throw new OpenIoTException("Error during update: " + result.getLastError().toString());
		}
	}

	/**
	 * Common handler for deleting objects. Assures that errors are handled in a
	 * consistent way.
	 * 
	 * @param collection
	 * @param object
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static WriteResult delete(DBCollection collection, DBObject object) throws OpenIoTException {
		WriteResult result = collection.remove(object);
		if (!result.getLastError().ok()) {
			throw new OpenIoTException("Error during delete: " + result.getLastError().toString());
		}
		return result;
	}

	/**
	 * Perform a get using the default lookup.s
	 * 
	 * @param id
	 * @param api
	 * @param collection
	 * @return
	 */
	public static <T> T get(String id, Class<T> api, DBCollection collection) {
		return get(id, api, collection, LOOKUP);
	}

	/**
	 * Get a single entity by unique id.
	 * 
	 * @param id
	 * @param api
	 * @param collection
	 * @param lookup
	 * @return
	 */
	public static <T> T get(String id, Class<T> api, DBCollection collection, IMongoConverterLookup lookup) {
		DBObject searchById = new BasicDBObject("_id", new ObjectId(id));
		DBObject found = collection.findOne(searchById);
		if (found != null) {
			MongoConverter<T> converter = lookup.getConverterFor(api);
			return converter.convert(found);
		}
		return null;
	}

	/**
	 * Search using the default lookup.
	 * 
	 * @param api
	 * @param collection
	 * @param query
	 * @param sort
	 * @param criteria
	 * @return
	 */
	public static <T> SearchResults<T> search(Class<T> api, DBCollection collection, DBObject query,
			DBObject sort, ISearchCriteria criteria) {
		return search(api, collection, query, sort, criteria, LOOKUP);
	}

	/**
	 * Search the given collection using the provided query and sort. Return the paged
	 * seaerch results.
	 * 
	 * @param api
	 * @param collection
	 * @param query
	 * @param sort
	 * @param criteria
	 * @param lookup
	 * @return
	 */
	public static <T> SearchResults<T> search(Class<T> api, DBCollection collection, DBObject query,
			DBObject sort, ISearchCriteria criteria, IMongoConverterLookup lookup) {
		DBCursor cursor;
		if (criteria.getPageSize() == 0) {
			cursor = collection.find(query).sort(sort);
		} else {
			int offset = Math.max(0, criteria.getPageNumber() - 1) * criteria.getPageSize();
			cursor = collection.find(query).skip(offset).limit(criteria.getPageSize()).sort(sort);
		}
		List<T> matches = new ArrayList<T>();
		SearchResults<T> results = new SearchResults<T>(matches);
		MongoConverter<T> converter = lookup.getConverterFor(api);
		try {
			results.setNumResults(cursor.count());
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(converter.convert(match));
			}
		} finally {
			cursor.close();
		}
		return results;
	}

	/**
	 * Search using the default lookup.
	 * 
	 * @param api
	 * @param collection
	 * @param query
	 * @param sort
	 * @return
	 */
	public static <T> SearchResults<T> search(Class<T> api, DBCollection collection, DBObject query,
			DBObject sort) {
		return search(api, collection, query, sort, LOOKUP);
	}

	/**
	 * Search the given collection using the provided query and sort.
	 * 
	 * @param api
	 * @param collection
	 * @param query
	 * @param sort
	 * @param looku
	 * @return
	 */
	public static <T> SearchResults<T> search(Class<T> api, DBCollection collection, DBObject query,
			DBObject sort, IMongoConverterLookup lookup) {
		DBCursor cursor = collection.find(query).sort(sort);
		List<T> matches = new ArrayList<T>();
		SearchResults<T> results = new SearchResults<T>(matches);
		MongoConverter<T> converter = lookup.getConverterFor(api);
		try {
			results.setNumResults(cursor.count());
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(converter.convert(match));
			}
		} finally {
			cursor.close();
		}
		return results;
	}

	/**
	 * List using the default lookup.
	 * 
	 * @param api
	 * @param collection
	 * @param query
	 * @param sort
	 * @return
	 */
	public static <T> List<T> list(Class<T> api, DBCollection collection, DBObject query, DBObject sort) {
		return list(api, collection, query, sort, LOOKUP);
	}

	/**
	 * List all items in the collection that match the qiven query.
	 * 
	 * @param api
	 * @param collection
	 * @param query
	 * @param sort
	 * @param lookup
	 * @return
	 */
	public static <T> List<T> list(Class<T> api, DBCollection collection, DBObject query, DBObject sort,
			IMongoConverterLookup lookup) {
		DBCursor cursor = collection.find(query);
		List<T> matches = new ArrayList<T>();
		MongoConverter<T> converter = lookup.getConverterFor(api);
		try {
			while (cursor.hasNext()) {
				DBObject match = cursor.next();
				matches.add(converter.convert(match));
			}
		} finally {
			cursor.close();
		}
		return matches;
	}

	/**
	 * Appends filter criteria onto exiting query based on the given date range.
	 * 
	 * @param query
	 * @param criteria
	 */
	public static void addDateSearchCriteria(BasicDBObject query, String dateField,
			IDateRangeSearchCriteria criteria) {
		if ((criteria.getStartDate() == null) && (criteria.getEndDate() == null)) {
			return;
		}
		BasicDBObject dateClause = new BasicDBObject();
		if (criteria.getStartDate() != null) {
			dateClause.append("$gte", criteria.getStartDate());
		}
		if (criteria.getEndDate() != null) {
			dateClause.append("$lte", criteria.getEndDate());
		}
		query.put(dateField, dateClause);
	}

	/**
	 * Given a {@link DBObject} that contains event information, unmarhal it to the
	 * correct type.
	 * 
	 * @param found
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceEvent unmarshalEvent(DBObject found) throws OpenIoTException {
		String type = (String) found.get(MongoDeviceEvent.PROP_EVENT_TYPE);
		if (type == null) {
			throw new OpenIoTException("Event matched but did not contain event type field.");
		}
		DeviceEventType eventType = DeviceEventType.valueOf(type);
		if (eventType == null) {
			throw new OpenIoTException("Event type not recognized: " + type);
		}

		switch (eventType) {
		case Measurements: {
			return MongoDeviceMeasurements.fromDBObject(found, false);
		}
		case Measurement: {
			return MongoDeviceMeasurement.fromDBObject(found, false);
		}
		case Location: {
			return MongoDeviceLocation.fromDBObject(found, false);
		}
		case Alert: {
			return MongoDeviceAlert.fromDBObject(found, false);
		}
		case CommandInvocation: {
			return MongoDeviceCommandInvocation.fromDBObject(found);
		}
		case CommandResponse: {
			return MongoDeviceCommandResponse.fromDBObject(found);
		}
		case StateChange: {
			return MongoDeviceStateChange.fromDBObject(found);
		}
		default: {
			throw new OpenIoTException("Event type not handled: " + type);
		}
		}
	}
}