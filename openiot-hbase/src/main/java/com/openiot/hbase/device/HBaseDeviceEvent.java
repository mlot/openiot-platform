/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.device;

import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.Base58;
import com.openiot.core.OpenIoTPersistence;
import com.openiot.hbase.IHBaseContext;
import com.openiot.hbase.IOpenIoTHBase;
import com.openiot.hbase.common.HBaseUtils;
import com.openiot.hbase.common.Pager;
import com.openiot.hbase.encoder.PayloadEncoding;
import com.openiot.hbase.encoder.PayloadMarshalerResolver;
import com.openiot.hbase.uid.IdManager;
import com.openiot.rest.model.device.DeviceAssignmentState;
import com.openiot.rest.model.device.event.*;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.event.*;
import com.openiot.spi.device.event.request.*;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.search.IDateRangeSearchCriteria;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * HBase specifics for dealing with OpenIoT device events.
 * 
 * @author Derek
 */
public class HBaseDeviceEvent {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(HBaseDeviceEvent.class);

	/** Size of a row in milliseconds */
	private static final long ROW_IN_MS = (1 << 24);

	/**
	 * List measurements associated with an assignment based on the given criteria.
	 * 
	 * @param context
	 * @param assnToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceEvent> listDeviceEvents(IHBaseContext context, String assnToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		Pager<EventMatch> matches = getEventRowsForAssignment(context, assnToken, null, criteria);
		return convertMatches(context, matches);
	}

	/**
	 * Create a new device measurements entry for an assignment.
	 * 
	 * @param context
	 * @param assignment
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceMeasurements createDeviceMeasurements(IHBaseContext context,
			IDeviceAssignment assignment, IDeviceMeasurementsCreateRequest request) throws OpenIoTException {
		long time = getEventTime(request);
		byte[] assnKey = IdManager.getInstance().getAssignmentKeys().getValue(assignment.getToken());
		if (assnKey == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceAssignmentToken, ErrorLevel.ERROR);
		}
		byte[] rowkey = getRowKey(assnKey, time);
		byte[] qualifier =
				getQualifier(EventRecordType.Measurement, time, context.getPayloadMarshaler().getEncoding());

		// Create measurements object and marshal to JSON.
		DeviceMeasurements measurements =
				OpenIoTPersistence.deviceMeasurementsCreateLogic(request, assignment);
		String id = getEncodedEventId(rowkey, qualifier);
		measurements.setId(id);
		byte[] payload = context.getPayloadMarshaler().encodeDeviceMeasurements(measurements);

		Put put = new Put(rowkey);
		put.add(IOpenIoTHBase.FAMILY_ID, qualifier, payload);
		context.getDeviceEventBuffer().add(put);

		// Update state if requested.
		if (request.isUpdateState()) {
			DeviceAssignmentState updated =
					OpenIoTPersistence.assignmentStateMeasurementsUpdateLogic(assignment, measurements);
			HBaseDeviceAssignment.updateDeviceAssignmentState(context, assignment.getToken(), updated);
		}

		return measurements;
	}

	/**
	 * List measurements associated with an assignment based on the given criteria.
	 * 
	 * @param context
	 * @param assnToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceMeasurements> listDeviceMeasurements(IHBaseContext context,
			String assnToken, IDateRangeSearchCriteria criteria) throws OpenIoTException {
		Pager<EventMatch> matches =
				getEventRowsForAssignment(context, assnToken, EventRecordType.Measurement, criteria);
		return convertMatches(context, matches);
	}

	/**
	 * List device measurements associated with a site.
	 * 
	 * @param context
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceMeasurements> listDeviceMeasurementsForSite(IHBaseContext context,
			String siteToken, IDateRangeSearchCriteria criteria) throws OpenIoTException {
		Pager<EventMatch> matches =
				getEventRowsForSite(context, siteToken, EventRecordType.Measurement, criteria);
		return convertMatches(context, matches);
	}

	/**
	 * Create a new device location entry for an assignment.
	 * 
	 * @param context
	 * @param assignment
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceLocation createDeviceLocation(IHBaseContext context, IDeviceAssignment assignment,
			IDeviceLocationCreateRequest request) throws OpenIoTException {
		long time = getEventTime(request);
		byte[] rowkey = getEventRowKey(assignment, time);
		byte[] qualifier =
				getQualifier(EventRecordType.Location, time, context.getPayloadMarshaler().getEncoding());

		DeviceLocation location = OpenIoTPersistence.deviceLocationCreateLogic(assignment, request);
		String id = getEncodedEventId(rowkey, qualifier);
		location.setId(id);
		byte[] payload = context.getPayloadMarshaler().encodeDeviceLocation(location);

		Put put = new Put(rowkey);
		put.add(IOpenIoTHBase.FAMILY_ID, qualifier, payload);
		context.getDeviceEventBuffer().add(put);

		// Update state if requested.
		if (request.isUpdateState()) {
			DeviceAssignmentState updated =
					OpenIoTPersistence.assignmentStateLocationUpdateLogic(assignment, location);
			HBaseDeviceAssignment.updateDeviceAssignmentState(context, assignment.getToken(), updated);
		}

		return location;
	}

	/**
	 * List locations associated with an assignment based on the given criteria.
	 * 
	 * @param context
	 * @param assnToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceLocation> listDeviceLocations(IHBaseContext context, String assnToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		Pager<EventMatch> matches =
				getEventRowsForAssignment(context, assnToken, EventRecordType.Location, criteria);
		return convertMatches(context, matches);
	}

	/**
	 * List device locations associated with a site.
	 * 
	 * @param context
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceLocation> listDeviceLocationsForSite(IHBaseContext context,
			String siteToken, IDateRangeSearchCriteria criteria) throws OpenIoTException {
		Pager<EventMatch> matches =
				getEventRowsForSite(context, siteToken, EventRecordType.Location, criteria);
		return convertMatches(context, matches);
	}

	/**
	 * Create a new device alert entry for an assignment.
	 * 
	 * @param context
	 * @param assignment
	 * @param request
	 * @param cache
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceAlert createDeviceAlert(IHBaseContext context, IDeviceAssignment assignment,
			IDeviceAlertCreateRequest request) throws OpenIoTException {
		long time = getEventTime(request);
		byte[] rowkey = getEventRowKey(assignment, time);
		byte[] qualifier =
				getQualifier(EventRecordType.Alert, time, context.getPayloadMarshaler().getEncoding());

		// Create alert and marshal to JSON.
		DeviceAlert alert = OpenIoTPersistence.deviceAlertCreateLogic(assignment, request);
		String id = getEncodedEventId(rowkey, qualifier);
		alert.setId(id);
		byte[] payload = context.getPayloadMarshaler().encodeDeviceAlert(alert);

		Put put = new Put(rowkey);
		put.add(IOpenIoTHBase.FAMILY_ID, qualifier, payload);
		context.getDeviceEventBuffer().add(put);

		// Update state if requested.
		if (request.isUpdateState()) {
			DeviceAssignmentState updated =
					OpenIoTPersistence.assignmentStateAlertUpdateLogic(assignment, alert);
			HBaseDeviceAssignment.updateDeviceAssignmentState(context, assignment.getToken(), updated);
		}

		return alert;
	}

	/**
	 * List alerts associated with an assignment based on the given criteria.
	 * 
	 * @param context
	 * @param assnToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceAlert> listDeviceAlerts(IHBaseContext context, String assnToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		Pager<EventMatch> matches =
				getEventRowsForAssignment(context, assnToken, EventRecordType.Alert, criteria);
		return convertMatches(context, matches);
	}

	/**
	 * List device alerts associated with a site.
	 * 
	 * @param context
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceAlert> listDeviceAlertsForSite(IHBaseContext context,
			String siteToken, IDateRangeSearchCriteria criteria) throws OpenIoTException {
		Pager<EventMatch> matches = getEventRowsForSite(context, siteToken, EventRecordType.Alert, criteria);
		return convertMatches(context, matches);
	}

	/**
	 * Create a new device command invocation entry for an assignment.
	 * 
	 * @param context
	 * @param assignment
	 * @param command
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceCommandInvocation createDeviceCommandInvocation(IHBaseContext context,
			IDeviceAssignment assignment, IDeviceCommand command,
			IDeviceCommandInvocationCreateRequest request) throws OpenIoTException {
		long time = getEventTime(request);
		byte[] rowkey = getEventRowKey(assignment, time);
		byte[] qualifier =
				getQualifier(EventRecordType.CommandInvocation, time,
						context.getPayloadMarshaler().getEncoding());

		// Create a command invocation and marshal to JSON.
		DeviceCommandInvocation ci =
				OpenIoTPersistence.deviceCommandInvocationCreateLogic(assignment, command, request);
		String id = getEncodedEventId(rowkey, qualifier);
		ci.setId(id);
		byte[] payload = context.getPayloadMarshaler().encodeDeviceCommandInvocation(ci);

		Put put = new Put(rowkey);
		put.add(IOpenIoTHBase.FAMILY_ID, qualifier, payload);
		context.getDeviceEventBuffer().add(put);

		return ci;
	}

	/**
	 * Get a {@link IDeviceEvent} by unique id.
	 * 
	 * @param context
	 * @param id
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceEvent getDeviceEvent(IHBaseContext context, String id) throws OpenIoTException {
		return getEventById(context, id);
	}

	/**
	 * List command invocations associated with an assignment based on the given criteria.
	 * 
	 * @param context
	 * @param assnToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceCommandInvocation> listDeviceCommandInvocations(IHBaseContext context,
			String assnToken, IDateRangeSearchCriteria criteria) throws OpenIoTException {
		Pager<EventMatch> matches =
				getEventRowsForAssignment(context, assnToken, EventRecordType.CommandInvocation, criteria);
		return convertMatches(context, matches);
	}

	/**
	 * List device command invocations associated with a site.
	 * 
	 * @param context
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceCommandInvocation> listDeviceCommandInvocationsForSite(
			IHBaseContext context, String siteToken, IDateRangeSearchCriteria criteria)
			throws OpenIoTException {
		Pager<EventMatch> matches =
				getEventRowsForSite(context, siteToken, EventRecordType.CommandInvocation, criteria);
		return convertMatches(context, matches);
	}

	/**
	 * Create a device state change event.
	 * 
	 * @param context
	 * @param assignment
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceStateChange createDeviceStateChange(IHBaseContext context,
			IDeviceAssignment assignment, IDeviceStateChangeCreateRequest request) throws OpenIoTException {
		long time = getEventTime(request);
		byte[] rowkey = getEventRowKey(assignment, time);
		byte[] qualifier =
				getQualifier(EventRecordType.StateChange, time, context.getPayloadMarshaler().getEncoding());

		// Create a state change and marshal to JSON.
		DeviceStateChange state = OpenIoTPersistence.deviceStateChangeCreateLogic(assignment, request);
		String id = getEncodedEventId(rowkey, qualifier);
		state.setId(id);
		byte[] payload = context.getPayloadMarshaler().encodeDeviceStateChange(state);

		Put put = new Put(rowkey);
		put.add(IOpenIoTHBase.FAMILY_ID, qualifier, payload);
		context.getDeviceEventBuffer().add(put);

		return state;
	}

	/**
	 * List state changes associated with an assignment based on the given criteria.
	 * 
	 * @param context
	 * @param assnToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceStateChange> listDeviceStateChanges(IHBaseContext context,
			String assnToken, IDateRangeSearchCriteria criteria) throws OpenIoTException {
		Pager<EventMatch> matches =
				getEventRowsForAssignment(context, assnToken, EventRecordType.StateChange, criteria);
		return convertMatches(context, matches);
	}

	/**
	 * List device state changes associated with a site.
	 * 
	 * @param context
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceStateChange> listDeviceStateChangesForSite(IHBaseContext context,
			String siteToken, IDateRangeSearchCriteria criteria) throws OpenIoTException {
		Pager<EventMatch> matches =
				getEventRowsForSite(context, siteToken, EventRecordType.StateChange, criteria);
		return convertMatches(context, matches);
	}

	/**
	 * Create a device command response.
	 * 
	 * @param context
	 * @param assignment
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceCommandResponse createDeviceCommandResponse(IHBaseContext context,
			IDeviceAssignment assignment, IDeviceCommandResponseCreateRequest request)
			throws OpenIoTException {
		long time = getEventTime(request);
		byte[] rowkey = getEventRowKey(assignment, time);
		byte[] qualifier =
				getQualifier(EventRecordType.CommandResponse, time,
						context.getPayloadMarshaler().getEncoding());

		// Create a state change and marshal to JSON.
		DeviceCommandResponse cr = OpenIoTPersistence.deviceCommandResponseCreateLogic(assignment, request);
		String id = getEncodedEventId(rowkey, qualifier);
		cr.setId(id);
		byte[] payload = context.getPayloadMarshaler().encodeDeviceCommandResponse(cr);

		Put put = new Put(rowkey);
		put.add(IOpenIoTHBase.FAMILY_ID, qualifier, payload);
		context.getDeviceEventBuffer().add(put);

		linkDeviceCommandResponseToInvocation(context, cr);
		return cr;
	}

	/**
	 * Creates a link to the command response by incrementing(or creating) a counter and
	 * sequential entries under the original invocation. TODO: Note that none of this is
	 * transactional, so it is currently possible for responses not to be correctly linked
	 * back to the original invocation if the calls in this method fail.
	 * 
	 * @param context
	 * @param response
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static void linkDeviceCommandResponseToInvocation(IHBaseContext context,
			IDeviceCommandResponse response) throws OpenIoTException {
		String originator = response.getOriginatingEventId();
		if (originator == null) {
			return;
		}

		byte[][] keys = getDecodedEventId(originator);
		byte[] row = keys[0];
		byte[] qual = keys[1];

		HTableInterface events = null;
		try {
			events = context.getClient().getTableInterface(IOpenIoTHBase.EVENTS_TABLE_NAME);
			// Increment the result counter.
			qual[3] = EventRecordType.CommandResponseCounter.getType();
			long counter = events.incrementColumnValue(row, IOpenIoTHBase.FAMILY_ID, qual, 1);
			byte[] counterBytes = Bytes.toBytes(counter);

			// Add new response entry row under the invocation.
			qual[3] = EventRecordType.CommandResponseEntry.getType();
			ByteBuffer seqkey = ByteBuffer.allocate(qual.length + counterBytes.length);
			seqkey.put(qual);
			seqkey.put(counterBytes);

			Put put = new Put(row);
			put.add(IOpenIoTHBase.FAMILY_ID, seqkey.array(), response.getId().getBytes());
			events.put(put);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to link command response.", e);
		} finally {
			HBaseUtils.closeCleanly(events);
		}
	}

	/**
	 * Find responses associated with a device command invocation.
	 * 
	 * @param context
	 * @param invocationId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static SearchResults<IDeviceCommandResponse> listDeviceCommandInvocationResponses(
			IHBaseContext context, String invocationId) throws OpenIoTException {
		byte[][] keys = getDecodedEventId(invocationId);
		byte[] row = keys[0];
		byte[] qual = keys[1];

		HTableInterface events = null;
		List<IDeviceCommandResponse> responses = new ArrayList<IDeviceCommandResponse>();
		try {
			events = context.getClient().getTableInterface(IOpenIoTHBase.EVENTS_TABLE_NAME);

			Get get = new Get(row);
			Result result = events.get(get);
			Map<byte[], byte[]> cells = result.getFamilyMap(IOpenIoTHBase.FAMILY_ID);

			byte[] match = qual;
			match[3] = EventRecordType.CommandResponseEntry.getType();
			for (byte[] curr : cells.keySet()) {
				if ((curr[0] == match[0]) && (curr[1] == match[1]) && (curr[2] == match[2])
						&& (curr[3] == match[3])) {
					byte[] value = cells.get(curr);
					String responseId = new String(value);
					responses.add(getDeviceCommandResponse(context, responseId));
				}
			}
			return new SearchResults<IDeviceCommandResponse>(responses);
		} catch (IOException e) {
			throw new OpenIoTException("Unable to link command response.", e);
		} finally {
			HBaseUtils.closeCleanly(events);
		}
	}

	/**
	 * List command responses associated with an assignment based on the given criteria.
	 * 
	 * @param context
	 * @param assnToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceCommandResponse> listDeviceCommandResponses(IHBaseContext context,
			String assnToken, IDateRangeSearchCriteria criteria) throws OpenIoTException {
		Pager<EventMatch> matches =
				getEventRowsForAssignment(context, assnToken, EventRecordType.CommandResponse, criteria);
		return convertMatches(context, matches);
	}

	/**
	 * Get a {@link IDeviceCommandResponse} by unique id.
	 * 
	 * @param context
	 * @param id
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceCommandResponse getDeviceCommandResponse(IHBaseContext context, String id)
			throws OpenIoTException {
		IDeviceEvent event = getEventById(context, id);
		if (event instanceof IDeviceCommandResponse) {
			return (IDeviceCommandResponse) event;
		}
		throw new OpenIoTException("Event is not a command response.");
	}

	/**
	 * List device command responses associated with a site.
	 * 
	 * @param context
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static SearchResults<IDeviceCommandResponse> listDeviceCommandResponsesForSite(
			IHBaseContext context, String siteToken, IDateRangeSearchCriteria criteria)
			throws OpenIoTException {
		Pager<EventMatch> matches =
				getEventRowsForSite(context, siteToken, EventRecordType.CommandResponse, criteria);
		return convertMatches(context, matches);
	}

	/**
	 * Get the event row key bytes.
	 * 
	 * @param assignment
	 * @param time
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static byte[] getEventRowKey(IDeviceAssignment assignment, long time) throws OpenIoTException {
		byte[] assnKey = IdManager.getInstance().getAssignmentKeys().getValue(assignment.getToken());
		if (assnKey == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceAssignmentToken, ErrorLevel.ERROR);
		}
		return getRowKey(assnKey, time);
	}

	/**
	 * Find all event rows associated with a device assignment and return cells that match
	 * the search criteria.
	 * 
	 * @param context
	 * @param assnToken
	 * @param eventType
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static Pager<EventMatch> getEventRowsForAssignment(IHBaseContext context, String assnToken,
			EventRecordType eventType, IDateRangeSearchCriteria criteria) throws OpenIoTException {
		byte[] assnKey = IdManager.getInstance().getAssignmentKeys().getValue(assnToken);
		if (assnKey == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceAssignmentToken, ErrorLevel.ERROR);
		}

		// Note: Because time values are inverted, start and end keys are reversed.
		byte[] startKey = null, endKey = null;
		if (criteria.getEndDate() != null) {
			startKey = getRowKey(assnKey, criteria.getEndDate().getTime());
		} else {
			startKey = getAbsoluteStartKey(assnKey);
		}
		if (criteria.getStartDate() != null) {
			endKey = getRowKey(assnKey, criteria.getStartDate().getTime() - ROW_IN_MS);
		} else {
			endKey = getAbsoluteEndKey(assnKey);
		}

		HTableInterface events = null;
		ResultScanner scanner = null;
		try {
			events = context.getClient().getTableInterface(IOpenIoTHBase.EVENTS_TABLE_NAME);
			Scan scan = new Scan();
			scan.setStartRow(startKey);
			scan.setStopRow(endKey);
			scanner = events.getScanner(scan);

			List<EventMatch> matches = new ArrayList<EventMatch>();
			Iterator<Result> results = scanner.iterator();
			while (results.hasNext()) {
				Result current = results.next();
				Map<byte[], byte[]> cells = current.getFamilyMap(IOpenIoTHBase.FAMILY_ID);
				for (byte[] qual : cells.keySet()) {
					byte[] value = cells.get(qual);
					if ((qual.length > 3) && ((eventType == null) || (qual[3] == eventType.getType()))) {
						Date eventDate = getDateForEventKeyValue(current.getRow(), qual);
						if ((criteria.getStartDate() != null) && (eventDate.before(criteria.getStartDate()))) {
							continue;
						}
						if ((criteria.getEndDate() != null) && (eventDate.after(criteria.getEndDate()))) {
							continue;
						}
						EventRecordType type = EventRecordType.decode(qual[3]);
						byte[] encoding = getEncodingFromQualifier(qual);
						matches.add(new EventMatch(type, eventDate, value, encoding));
					}
				}
			}
			Collections.sort(matches, Collections.reverseOrder());
			Pager<EventMatch> pager = new Pager<EventMatch>(criteria);
			for (EventMatch match : matches) {
				pager.process(match);
			}
			return pager;
		} catch (IOException e) {
			throw new OpenIoTException("Error scanning event rows.", e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			HBaseUtils.closeCleanly(events);
		}
	}

	/**
	 * Decodes the event date encoded in the rowkey and qualifier for events.
	 * 
	 * @param kv
	 * @return
	 */
	protected static Date getDateForEventKeyValue(byte[] key, byte[] qualifier) {
		byte[] work = new byte[8];
		work[0] = (byte) ~key[7];
		work[1] = (byte) ~key[8];
		work[2] = (byte) ~key[9];
		work[3] = (byte) ~key[10];
		work[4] = (byte) ~key[11];
		work[5] = (byte) ~qualifier[0];
		work[6] = (byte) ~qualifier[1];
		work[7] = (byte) ~qualifier[2];
		long time = Bytes.toLong(work);
		return new Date(time);
	}

	/**
	 * Find all event rows associated with a site and return values that match the search
	 * criteria. TODO: This is not optimized at all and will take forever in cases where
	 * there are ton of assignments and events. It has to go through every record
	 * associated with the site. It works for now though.
	 * 
	 * @param context
	 * @param siteToken
	 * @param eventType
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static Pager<EventMatch> getEventRowsForSite(IHBaseContext context, String siteToken,
			EventRecordType eventType, IDateRangeSearchCriteria criteria) throws OpenIoTException {
		Long siteId = IdManager.getInstance().getSiteKeys().getValue(siteToken);
		if (siteId == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidSiteToken, ErrorLevel.ERROR);
		}
		byte[] startPrefix = HBaseSite.getAssignmentRowKey(siteId);
		byte[] afterPrefix = HBaseSite.getAfterAssignmentRowKey(siteId);

		HTableInterface events = null;
		ResultScanner scanner = null;
		try {
			events = context.getClient().getTableInterface(IOpenIoTHBase.EVENTS_TABLE_NAME);
			Scan scan = new Scan();
			scan.setStartRow(startPrefix);
			scan.setStopRow(afterPrefix);
			scanner = events.getScanner(scan);

			List<EventMatch> matches = new ArrayList<EventMatch>();
			Iterator<Result> results = scanner.iterator();
			while (results.hasNext()) {
				Result current = results.next();
				byte[] key = current.getRow();
				if (key.length > 7) {
					Map<byte[], byte[]> cells = current.getFamilyMap(IOpenIoTHBase.FAMILY_ID);
					for (byte[] qual : cells.keySet()) {
						byte[] value = cells.get(qual);
						if ((qual.length > 3) && (qual[3] == eventType.getType())) {
							Date eventDate = getDateForEventKeyValue(key, qual);
							if ((criteria.getStartDate() != null)
									&& (eventDate.before(criteria.getStartDate()))) {
								continue;
							}
							if ((criteria.getEndDate() != null) && (eventDate.after(criteria.getEndDate()))) {
								continue;
							}
							EventRecordType type = EventRecordType.decode(qual[3]);
							byte[] encoding = getEncodingFromQualifier(qual);
							matches.add(new EventMatch(type, eventDate, value, encoding));
						}
					}
				}
			}
			Collections.sort(matches, Collections.reverseOrder());
			Pager<EventMatch> pager = new Pager<EventMatch>(criteria);
			for (EventMatch match : matches) {
				pager.process(match);
			}
			return pager;
		} catch (IOException e) {
			throw new OpenIoTException("Error scanning event rows.", e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			HBaseUtils.closeCleanly(events);
		}
	}

	/**
	 * Used for ordering events without having to unmarshal all of the byte arrays to do
	 * it.
	 * 
	 * @author Derek
	 */
	private static class EventMatch implements Comparable<EventMatch> {

		private EventRecordType type;

		private Date date;

		private byte[] payload;

		private byte[] encoding;

		public EventMatch(EventRecordType type, Date date, byte[] payload, byte[] encoding) {
			this.type = type;
			this.date = date;
			this.payload = payload;
			this.encoding = encoding;
		}

		public EventRecordType getType() {
			return type;
		}

		public Date getDate() {
			return date;
		}

		public byte[] getPayload() {
			return payload;
		}

		public byte[] getEncoding() {
			return encoding;
		}

		public int compareTo(EventMatch other) {
			return this.getDate().compareTo(other.getDate());
		}
	}

	/**
	 * Converts matching rows to {@link SearchResults} for web service response.
	 * 
	 * @param context
	 * @param matches
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	@SuppressWarnings("unchecked")
	protected static <I extends IDeviceEvent> SearchResults<I> convertMatches(IHBaseContext context,
			Pager<EventMatch> matches) throws OpenIoTException {
		List<I> results = new ArrayList<I>();
		for (EventMatch match : matches.getResults()) {
			Class<? extends IDeviceEvent> type = getEventClassForIndicator(match.getType().getType());
			try {
				results.add((I) PayloadMarshalerResolver.getInstance().getMarshaler(match.getEncoding()).decode(
						match.getPayload(), type));
			} catch (Throwable e) {
				LOGGER.error("Unable to read payload value into event object.", e);
			}
		}
		return new SearchResults<I>(results, matches.getTotal());
	}

	/**
	 * Gets the absolute first possible event key for cases where a start timestamp is not
	 * specified.
	 * 
	 * @param assnKey
	 * @return
	 */
	protected static byte[] getAbsoluteStartKey(byte[] assnKey) {
		ByteBuffer buffer = ByteBuffer.allocate(assnKey.length + 4);
		buffer.put(assnKey);
		buffer.put((byte) 0x00);
		buffer.put((byte) 0x00);
		buffer.put((byte) 0x00);
		buffer.put((byte) 0x00);
		return buffer.array();
	}

	/**
	 * Gets the absolute first possible event key for cases where a start timestamp is not
	 * specified.
	 * 
	 * @param assnKey
	 * @return
	 */
	protected static byte[] getAbsoluteEndKey(byte[] assnKey) {
		ByteBuffer buffer = ByteBuffer.allocate(assnKey.length + 4);
		buffer.put(assnKey);
		buffer.put((byte) 0xff);
		buffer.put((byte) 0xff);
		buffer.put((byte) 0xff);
		buffer.put((byte) 0xff);
		return buffer.array();
	}

	/**
	 * Get the event time used to calculate row key and qualifier.
	 * 
	 * @param event
	 * @return
	 */
	protected static long getEventTime(IDeviceEventCreateRequest event) {
		return (event.getEventDate() != null) ? event.getEventDate().getTime() : System.currentTimeMillis();
	}

	/**
	 * Get row key for a given event type and time.
	 * 
	 * @param assnToken
	 * @param eventType
	 * @param time
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static byte[] getRowKey(byte[] assnKey, long time) throws OpenIoTException {
		byte[] bucketBytes = Bytes.toBytes(time);
		ByteBuffer buffer = ByteBuffer.allocate(assnKey.length + 5);
		buffer.put(assnKey);
		buffer.put((byte) ~bucketBytes[0]);
		buffer.put((byte) ~bucketBytes[1]);
		buffer.put((byte) ~bucketBytes[2]);
		buffer.put((byte) ~bucketBytes[3]);
		buffer.put((byte) ~bucketBytes[4]);
		return buffer.array();
	}

	/**
	 * Get column qualifier for storing the event.
	 * 
	 * @param type
	 * @param time
	 * @return
	 */
	public static byte[] getQualifier(EventRecordType eventType, long time, PayloadEncoding encoding) {
		byte[] offsetBytes = Bytes.toBytes(time);
		byte[] encodingBytes = encoding.getIndicator();
		ByteBuffer buffer = ByteBuffer.allocate(4 + encodingBytes.length);
		buffer.put((byte) ~offsetBytes[5]);
		buffer.put((byte) ~offsetBytes[6]);
		buffer.put((byte) ~offsetBytes[7]);
		buffer.put(eventType.getType());
		buffer.put(encodingBytes);
		return buffer.array();
	}

	/**
	 * Get encoding scheme from qualifier.
	 * 
	 * @param qualifier
	 * @return
	 */
	public static byte[] getEncodingFromQualifier(byte[] qualifier) {
		int encLength = qualifier.length - 4;
		return Bytes.tail(qualifier, encLength);
	}

	/**
	 * Creates a base 64 encoded String for unique event key.
	 * 
	 * @param rowkey
	 * @param qualifier
	 * @return
	 */
	public static String getEncodedEventId(byte[] rowkey, byte[] qualifier) {
		ByteBuffer buffer = ByteBuffer.allocate(rowkey.length + qualifier.length);
		buffer.put(rowkey);
		buffer.put(qualifier);
		byte[] bytes = buffer.array();
		return Base58.encode(bytes);
	}

	/**
	 * Decodes an event id into a {@link KeyValue} that can be used to access the data in
	 * HBase.
	 * 
	 * @param id
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static byte[][] getDecodedEventId(String id) throws OpenIoTException {
		int rowLength =
				HBaseSite.SITE_IDENTIFIER_LENGTH + 1 + HBaseDeviceAssignment.ASSIGNMENT_IDENTIFIER_LENGTH + 5;
		try {
			byte[] decoded = Base58.decode(id);
			byte[] row = Bytes.head(decoded, rowLength);
			byte[] qual = Bytes.tail(decoded, decoded.length - rowLength);
			return new byte[][] { row, qual };
		} catch (AddressFormatException e) {
			throw new OpenIoTException("Invalid event id: " + id);
		}
	}

	/**
	 * Gets an event by unique id.
	 * 
	 * @param context
	 * @param id
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static IDeviceEvent getEventById(IHBaseContext context, String id) throws OpenIoTException {
		byte[][] keys = getDecodedEventId(id);
		byte[] row = keys[0];
		byte[] qual = keys[1];
		HTableInterface events = null;
		try {
			events = context.getClient().getTableInterface(IOpenIoTHBase.EVENTS_TABLE_NAME);
			Get get = new Get(row);
			get.addColumn(IOpenIoTHBase.FAMILY_ID, qual);
			Result result = events.get(get);
			byte type = qual[3];
			Class<? extends IDeviceEvent> eventClass = getEventClassForIndicator(type);

			if (result != null) {
				byte[] payload = result.getValue(IOpenIoTHBase.FAMILY_ID, qual);
				if (payload != null) {
					return context.getPayloadMarshaler().decode(payload, eventClass);
				}
			}
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceEventId, ErrorLevel.ERROR,
					HttpServletResponse.SC_NOT_FOUND);
		} catch (IOException e) {
			throw new OpenIoTException(e);
		} finally {
			HBaseUtils.closeCleanly(events);
		}
	}

	/**
	 * Get the REST wrapper class that can be used to unmarshal JSON.
	 * 
	 * @param indicator
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected static Class<? extends IDeviceEvent> getEventClassForIndicator(byte indicator)
			throws OpenIoTException {
		EventRecordType eventType = EventRecordType.decode(indicator);
		switch (eventType) {
		case Measurement: {
			return DeviceMeasurements.class;
		}
		case Location: {
			return DeviceLocation.class;
		}
		case Alert: {
			return DeviceAlert.class;
		}
		case CommandInvocation: {
			return DeviceCommandInvocation.class;
		}
		case CommandResponse: {
			return DeviceCommandResponse.class;
		}
		case StateChange: {
			return DeviceStateChange.class;
		}
		default: {
			throw new OpenIoTException("Id references unknown event type.");
		}
		}
	}
}