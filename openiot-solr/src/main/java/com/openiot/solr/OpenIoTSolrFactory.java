/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.solr;

import com.openiot.rest.model.device.event.DeviceAlert;
import com.openiot.rest.model.device.event.DeviceEvent;
import com.openiot.rest.model.device.event.DeviceLocation;
import com.openiot.rest.model.device.event.DeviceMeasurements;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.DeviceAssignmentType;
import com.openiot.spi.device.event.*;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Factory that creates indexable Solr objects from OpenIoT objects.
 * 
 * @author Derek
 */
public class OpenIoTSolrFactory {

	/**
	 * Create a {@link SolrInputDocument} based on information in
	 * {@link IDeviceMeasurements}.
	 * 
	 * @param measurements
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SolrInputDocument createDocumentFromMeasurements(IDeviceMeasurements measurements)
			throws OpenIoTException {
		SolrInputDocument document = new SolrInputDocument();
		document.addField(ISolrFields.EVENT_TYPE, SolrEventType.Measurements.name());
		addFieldsForEvent(document, measurements);
		for (String key : measurements.getMeasurements().keySet()) {
			document.addField(ISolrFields.MEASUREMENT_PREFIX + key, measurements.getMeasurement(key));
		}
		return document;
	}

	/**
	 * Create a {@link SolrInputDocument} based on information in {@link IDeviceLocation}.
	 * 
	 * @param location
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SolrInputDocument createDocumentFromLocation(IDeviceLocation location)
			throws OpenIoTException {
		SolrInputDocument document = new SolrInputDocument();
		document.addField(ISolrFields.EVENT_TYPE, SolrEventType.Location.name());
		addFieldsForEvent(document, location);
		String latLong = "" + location.getLatitude() + ", " + location.getLongitude();
		document.addField(ISolrFields.LOCATION, latLong);
		document.addField(ISolrFields.ELEVATION, location.getElevation());
		return document;
	}

	/**
	 * Create a {@link SolrInputDocument} based on information in {@link IDeviceAlert}.
	 * 
	 * @param alert
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SolrInputDocument createDocumentFromAlert(IDeviceAlert alert) throws OpenIoTException {
		SolrInputDocument document = new SolrInputDocument();
		document.addField(ISolrFields.EVENT_TYPE, SolrEventType.Alert.name());
		addFieldsForEvent(document, alert);
		document.addField(ISolrFields.ALERT_TYPE, alert.getType());
		document.addField(ISolrFields.ALERT_MESSAGE, alert.getMessage());
		document.addField(ISolrFields.ALERT_LEVEL, alert.getLevel().name());
		document.addField(ISolrFields.ALERT_SOURCE, alert.getSource().name());
		return document;
	}

	/**
	 * Parses a {@link SolrDocument} into a {@link IDeviceEvent}.
	 * 
	 * @param document
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceEvent parseDocument(SolrDocument document) throws OpenIoTException {
		String type = (String) document.getFieldValue(ISolrFields.EVENT_TYPE);
		if (type == null) {
			throw new OpenIoTException("Solr event does not contain an event type indicator.");
		}
		SolrEventType eventType = SolrEventType.valueOf(type);
		switch (eventType) {
		case Location: {
			return parseLocationFromDocument(document);
		}
		case Measurements: {
			return parseMeasurementsFromDocument(document);
		}
		case Alert: {
			return parseAlertFromDocument(document);
		}
		default: {
			throw new OpenIoTException("Solr docuemnt contained unknown device event type.");
		}
		}
	}

	/**
	 * Parse an {@link IDeviceLocation} record from a {@link SolrDocument}.
	 * 
	 * @param document
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@SuppressWarnings("unchecked")
	protected static IDeviceLocation parseLocationFromDocument(SolrDocument document)
			throws OpenIoTException {
		DeviceLocation location = new DeviceLocation();

		List<String> latLong = (List<String>) document.get(ISolrFields.LOCATION);
		if (latLong == null) {
			throw new OpenIoTException("Invalid location document. No location data stored.");
		}
		String[] split = latLong.get(0).split("[,]");
		location.setLatitude(Double.parseDouble(split[0].trim()));
		location.setLongitude(Double.parseDouble(split[1].trim()));

		Double elevation = (Double) document.get(ISolrFields.ELEVATION);
		if (elevation != null) {
			location.setElevation(elevation);
		}

		addFieldsFromEventDocument(document, location);
		return location;
	}

	/**
	 * Parse an {@link IDeviceMeasurements} record from a {@link SolrDocument}.
	 * 
	 * @param document
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static IDeviceMeasurements parseMeasurementsFromDocument(SolrDocument document)
			throws OpenIoTException {
		DeviceMeasurements measurements = new DeviceMeasurements();

		Iterator<String> names = document.getFieldNames().iterator();
		int mxLength = ISolrFields.MEASUREMENT_PREFIX.length();
		while (names.hasNext()) {
			String name = names.next();
			if (name.startsWith(ISolrFields.MEASUREMENT_PREFIX)) {
				String metaName = name.substring(mxLength);
				Double metaValue = (Double) document.get(name);
				measurements.addOrReplaceMeasurement(metaName, metaValue);
			}
		}

		addFieldsFromEventDocument(document, measurements);
		return measurements;
	}

	/**
	 * Parse and {@link IDeviceAlert} record from a {@link SolrDocument}.
	 * 
	 * @param document
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static IDeviceAlert parseAlertFromDocument(SolrDocument document) throws OpenIoTException {
		DeviceAlert alert = new DeviceAlert();

		String alertType = (String) document.get(ISolrFields.ALERT_TYPE);
		String alertMessage = (String) document.get(ISolrFields.ALERT_MESSAGE);
		String alertLevelStr = (String) document.get(ISolrFields.ALERT_LEVEL);
		String alertSourceStr = (String) document.get(ISolrFields.ALERT_SOURCE);

		alert.setType(alertType);
		alert.setMessage(alertMessage);
		alert.setLevel(AlertLevel.valueOf(alertLevelStr));
		alert.setSource(AlertSource.valueOf(alertSourceStr));

		addFieldsFromEventDocument(document, alert);
		return alert;
	}

	/**
	 * Add fields common to all device events.
	 * 
	 * @param document
	 * @param event
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static void addFieldsFromEventDocument(SolrDocument document, DeviceEvent event)
			throws OpenIoTException {
		String id = (String) document.get(ISolrFields.EVENT_ID);
		String assignmentToken = (String) document.get(ISolrFields.ASSIGNMENT_TOKEN);
		String assignmentTypeStr = (String) document.get(ISolrFields.ASSIGNMENT_TYPE);
		String assetId = (String) document.get(ISolrFields.ASSET_ID);
		String siteToken = (String) document.get(ISolrFields.SITE_TOKEN);
		Date eventDate = (Date) document.get(ISolrFields.EVENT_DATE);
		Date receivedDate = (Date) document.get(ISolrFields.RECEIVED_DATE);

		event.setId(id);
		event.setDeviceAssignmentToken(assignmentToken);
		event.setAssignmentType(DeviceAssignmentType.valueOf(assignmentTypeStr));
		event.setAssetId(assetId);
		event.setSiteToken(siteToken);
		event.setEventDate(eventDate);
		event.setReceivedDate(receivedDate);

		Iterator<String> names = document.getFieldNames().iterator();
		int metaLength = ISolrFields.META_PREFIX.length();
		while (names.hasNext()) {
			String name = names.next();
			if (name.startsWith(ISolrFields.META_PREFIX)) {
				String metaName = name.substring(metaLength);
				String metaValue = (String) document.get(name);
				event.addOrReplaceMetadata(metaName, metaValue);
			}
		}
	}

	/**
	 * Adds common fields from base OpenIoT {@link IDeviceEvent} object.
	 * 
	 * @param document
	 * @param event
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static void addFieldsForEvent(SolrInputDocument document, IDeviceEvent event)
			throws OpenIoTException {
		document.addField(ISolrFields.EVENT_ID, event.getId());
		document.addField(ISolrFields.ASSIGNMENT_TOKEN, event.getDeviceAssignmentToken());
		document.addField(ISolrFields.ASSIGNMENT_TYPE, event.getAssignmentType().name());
		document.addField(ISolrFields.ASSET_ID, event.getAssetId());
		document.addField(ISolrFields.SITE_TOKEN, event.getSiteToken());
		document.addField(ISolrFields.EVENT_DATE, event.getEventDate());
		document.addField(ISolrFields.RECEIVED_DATE, event.getReceivedDate());
		addMetadata(document, event.getMetadata());
	}

	/**
	 * Add metadata to the document.
	 * 
	 * @param document
	 * @param metadata
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static void addMetadata(SolrInputDocument document, Map<String, String> metadata)
			throws OpenIoTException {
		for (String key : metadata.keySet()) {
			document.addField(ISolrFields.META_PREFIX + key, metadata.get(key));
		}
	}
}