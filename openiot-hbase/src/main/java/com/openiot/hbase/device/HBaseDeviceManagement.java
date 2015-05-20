/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.device;

import com.openiot.core.OpenIoTPersistence;
import com.openiot.hbase.HBaseContext;
import com.openiot.hbase.IOpenIoTHBase;
import com.openiot.hbase.IOpenIoTHBaseClient;
import com.openiot.hbase.common.OpenIoTTables;
import com.openiot.hbase.encoder.IPayloadMarshaler;
import com.openiot.hbase.encoder.ProtobufPayloadMarshaler;
import com.openiot.hbase.uid.IdManager;
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
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.UUID;

/**
 * HBase implementation of OpenIoT device management.
 * 
 * @author Derek
 */
public class HBaseDeviceManagement extends LifecycleComponent implements IDeviceManagement,
		ICachingDeviceManagement {

	/** Static logger instance */
	private static final Logger LOGGER = Logger.getLogger(HBaseDeviceManagement.class);

	/** Used to communicate with HBase */
	private IOpenIoTHBaseClient client;

	/** Injected cache provider */
	private IDeviceManagementCacheProvider cacheProvider;

	/** Injected payload encoder */
	private IPayloadMarshaler payloadMarshaler = new ProtobufPayloadMarshaler();

	/** Supplies context to implementation methods */
	private HBaseContext context;

	/** Allows puts to be buffered for device events */
	private DeviceEventBuffer buffer;

	public HBaseDeviceManagement() {
		super(LifecycleComponentType.DataStore);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	public void start() throws OpenIoTException {
		ensureTablesExist();

		IdManager.getInstance().load(client);

		// Create context from configured options.
		this.context = new HBaseContext();
		context.setClient(getClient());
		context.setCacheProvider(getCacheProvider());
		context.setPayloadMarshaler(getPayloadMarshaler());

		// Start buffer for saving device events.
		buffer = new DeviceEventBuffer(context);
		buffer.start();
		context.setDeviceEventBuffer(buffer);
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

	/**
	 * Make sure that all OpenIoT tables exist, creating them if necessary.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void ensureTablesExist() throws OpenIoTException {
		OpenIoTTables.assureTable(client, IOpenIoTHBase.UID_TABLE_NAME, BloomType.ROW);
		OpenIoTTables.assureTable(client, IOpenIoTHBase.SITES_TABLE_NAME, BloomType.ROW);
		OpenIoTTables.assureTable(client, IOpenIoTHBase.EVENTS_TABLE_NAME, BloomType.ROW);
		OpenIoTTables.assureTable(client, IOpenIoTHBase.DEVICES_TABLE_NAME, BloomType.ROW);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	public void stop() throws OpenIoTException {
		buffer.stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ICachingDeviceManagement#setCacheProvider(com.openiot
	 * .spi.device.IDeviceManagementCacheProvider)
	 */
	@Override
	public void setCacheProvider(IDeviceManagementCacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}

	public IDeviceManagementCacheProvider getCacheProvider() {
		return cacheProvider;
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
		return HBaseDeviceSpecification.createDeviceSpecification(context, request);
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
		return HBaseDeviceSpecification.getDeviceSpecificationByToken(context, token);
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
		return HBaseDeviceSpecification.updateDeviceSpecification(context, token, request);
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
		return HBaseDeviceSpecification.listDeviceSpecifications(context, includeDeleted, criteria);
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
		return HBaseDeviceSpecification.deleteDeviceSpecification(context, token, force);
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
		return HBaseDeviceCommand.createDeviceCommand(context, spec, request);
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
		return HBaseDeviceCommand.getDeviceCommandByToken(context, token);
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
		return HBaseDeviceCommand.updateDeviceCommand(context, token, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceCommands(java.lang.String,
	 * boolean)
	 */
	@Override
	public List<IDeviceCommand> listDeviceCommands(String specToken, boolean includeDeleted)
			throws OpenIoTException {
		return HBaseDeviceCommand.listDeviceCommands(context, specToken, includeDeleted);
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
		return HBaseDeviceCommand.deleteDeviceCommand(context, token, force);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#createDevice(com.openiot.spi.device
	 * .request.IDeviceCreateRequest)
	 */
	public IDevice createDevice(IDeviceCreateRequest device) throws OpenIoTException {
		return HBaseDevice.createDevice(context, device);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#getDeviceByHardwareId(java.lang.String)
	 */
	public IDevice getDeviceByHardwareId(String hardwareId) throws OpenIoTException {
		return HBaseDevice.getDeviceByHardwareId(context, hardwareId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#updateDevice(java.lang.String,
	 * IDeviceCreateRequest)
	 */
	public IDevice updateDevice(String hardwareId, IDeviceCreateRequest request) throws OpenIoTException {
		return HBaseDevice.updateDevice(context, hardwareId, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#getCurrentDeviceAssignment(com.openiot
	 * .spi.device.IDevice)
	 */
	public IDeviceAssignment getCurrentDeviceAssignment(IDevice device) throws OpenIoTException {
		String token = HBaseDevice.getCurrentAssignmentId(context, device.getHardwareId());
		if (token == null) {
			return null;
		}
		return HBaseDeviceAssignment.getDeviceAssignment(context, token);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#listDevices(boolean,
	 * IDeviceSearchCriteria)
	 */
	public SearchResults<IDevice> listDevices(boolean includeDeleted, IDeviceSearchCriteria criteria)
			throws OpenIoTException {
		return HBaseDevice.listDevices(context, includeDeleted, criteria);
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
	public IDevice deleteDevice(String hardwareId, boolean force) throws OpenIoTException {
		return HBaseDevice.deleteDevice(context, hardwareId, force);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#createDeviceAssignment(com.openiot
	 * .spi.device.request.IDeviceAssignmentCreateRequest)
	 */
	public IDeviceAssignment createDeviceAssignment(IDeviceAssignmentCreateRequest request)
			throws OpenIoTException {
		return HBaseDeviceAssignment.createDeviceAssignment(context, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#getDeviceAssignmentByToken(java.lang
	 * .String)
	 */
	public IDeviceAssignment getDeviceAssignmentByToken(String token) throws OpenIoTException {
		return HBaseDeviceAssignment.getDeviceAssignment(context, token);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#deleteDeviceAssignment(java.lang.String,
	 * boolean)
	 */
	public IDeviceAssignment deleteDeviceAssignment(String token, boolean force) throws OpenIoTException {
		return HBaseDeviceAssignment.deleteDeviceAssignment(context, token, force);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#getDeviceForAssignment(com.openiot
	 * .spi.device.IDeviceAssignment)
	 */
	public IDevice getDeviceForAssignment(IDeviceAssignment assignment) throws OpenIoTException {
		return HBaseDevice.getDeviceByHardwareId(context, assignment.getDeviceHardwareId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#getSiteForAssignment(com.openiot.spi
	 * .device.IDeviceAssignment)
	 */
	public ISite getSiteForAssignment(IDeviceAssignment assignment) throws OpenIoTException {
		return HBaseSite.getSiteByToken(context, assignment.getSiteToken());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#updateDeviceAssignmentMetadata(java.
	 * lang.String, IMetadataProvider)
	 */
	public IDeviceAssignment updateDeviceAssignmentMetadata(String token, IMetadataProvider metadata)
			throws OpenIoTException {
		return HBaseDeviceAssignment.updateDeviceAssignmentMetadata(context, token, metadata);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#updateDeviceAssignmentStatus(java.lang
	 * .String, DeviceAssignmentStatus)
	 */
	public IDeviceAssignment updateDeviceAssignmentStatus(String token, DeviceAssignmentStatus status)
			throws OpenIoTException {
		return HBaseDeviceAssignment.updateDeviceAssignmentStatus(context, token, status);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#updateDeviceAssignmentState(java.lang
	 * .String, IDeviceAssignmentState)
	 */
	public IDeviceAssignment updateDeviceAssignmentState(String token, IDeviceAssignmentState state)
			throws OpenIoTException {
		return HBaseDeviceAssignment.updateDeviceAssignmentState(context, token, state);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#addDeviceEventBatch(java.lang.String,
	 * com.openiot.spi.device.IDeviceEventBatch)
	 */
	public IDeviceEventBatchResponse addDeviceEventBatch(String assignmentToken, IDeviceEventBatch batch)
			throws OpenIoTException {
		return OpenIoTPersistence.deviceEventBatchLogic(assignmentToken, batch, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#endDeviceAssignment(java.lang.String)
	 */
	public IDeviceAssignment endDeviceAssignment(String token) throws OpenIoTException {
		return HBaseDeviceAssignment.endDeviceAssignment(context, token);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#getDeviceAssignmentHistory(java.lang
	 * .String, com.openiot.spi.common.ISearchCriteria)
	 */
	public SearchResults<IDeviceAssignment> getDeviceAssignmentHistory(String hardwareId,
			ISearchCriteria criteria) throws OpenIoTException {
		return HBaseDevice.getDeviceAssignmentHistory(context, hardwareId, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#getDeviceAssignmentsForSite(java.lang
	 * .String, com.openiot.spi.common.ISearchCriteria)
	 */
	public SearchResults<IDeviceAssignment> getDeviceAssignmentsForSite(String siteToken,
			ISearchCriteria criteria) throws OpenIoTException {
		return HBaseSite.listDeviceAssignmentsForSite(context, siteToken, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#getDeviceEventById(java.lang.String)
	 */
	@Override
	public IDeviceEvent getDeviceEventById(String id) throws OpenIoTException {
		return HBaseDeviceEvent.getDeviceEvent(context, id);
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
		return HBaseDeviceEvent.listDeviceEvents(context, assignmentToken, criteria);
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
			IDeviceMeasurementsCreateRequest measurements) throws OpenIoTException {
		IDeviceAssignment assignment = assertDeviceAssignment(assignmentToken);
		return HBaseDeviceEvent.createDeviceMeasurements(context, assignment, measurements);
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
		return HBaseDeviceEvent.listDeviceMeasurements(context, token, criteria);
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
		return HBaseDeviceEvent.listDeviceMeasurementsForSite(context, siteToken, criteria);
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
		IDeviceAssignment assignment = assertDeviceAssignment(assignmentToken);
		return HBaseDeviceEvent.createDeviceLocation(context, assignment, request);
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
		return HBaseDeviceEvent.listDeviceLocations(context, assignmentToken, criteria);
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
		return HBaseDeviceEvent.listDeviceLocationsForSite(context, siteToken, criteria);
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
		throw new OpenIoTException("Not implemented yet for HBase device management.");
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
		IDeviceAssignment assignment = assertDeviceAssignment(assignmentToken);
		return HBaseDeviceEvent.createDeviceAlert(context, assignment, request);
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
		return HBaseDeviceEvent.listDeviceAlerts(context, assignmentToken, criteria);
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
		return HBaseDeviceEvent.listDeviceAlertsForSite(context, siteToken, criteria);
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
		IDeviceAssignment assignment = assertDeviceAssignment(assignmentToken);
		return HBaseDeviceEvent.createDeviceCommandInvocation(context, assignment, command, request);
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
		return HBaseDeviceEvent.listDeviceCommandInvocations(context, assignmentToken, criteria);
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
		return HBaseDeviceEvent.listDeviceCommandInvocationsForSite(context, siteToken, criteria);
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
		return HBaseDeviceEvent.listDeviceCommandInvocationResponses(context, invocationId);
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
		IDeviceAssignment assignment = assertDeviceAssignment(assignmentToken);
		return HBaseDeviceEvent.createDeviceCommandResponse(context, assignment, request);
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
		return HBaseDeviceEvent.listDeviceCommandResponses(context, assignmentToken, criteria);
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
		return HBaseDeviceEvent.listDeviceCommandResponsesForSite(context, siteToken, criteria);
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
		IDeviceAssignment assignment = assertDeviceAssignment(assignmentToken);
		return HBaseDeviceEvent.createDeviceStateChange(context, assignment, request);
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
		return HBaseDeviceEvent.listDeviceStateChanges(context, assignmentToken, criteria);
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
		return HBaseDeviceEvent.listDeviceStateChangesForSite(context, siteToken, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#createSite(com.openiot.spi.device.
	 * request.ISiteCreateRequest)
	 */
	@Override
	public ISite createSite(ISiteCreateRequest request) throws OpenIoTException {
		return HBaseSite.createSite(context, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#deleteSite(java.lang.String,
	 * boolean)
	 */
	@Override
	public ISite deleteSite(String siteToken, boolean force) throws OpenIoTException {
		return HBaseSite.deleteSite(context, siteToken, force);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#updateSite(java.lang.String,
	 * ISiteCreateRequest)
	 */
	@Override
	public ISite updateSite(String siteToken, ISiteCreateRequest request) throws OpenIoTException {
		return HBaseSite.updateSite(context, siteToken, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#getSiteByToken(java.lang.String)
	 */
	@Override
	public ISite getSiteByToken(String token) throws OpenIoTException {
		return HBaseSite.getSiteByToken(context, token);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#listSites(com.openiot.spi.common.
	 * ISearchCriteria)
	 */
	@Override
	public SearchResults<ISite> listSites(ISearchCriteria criteria) throws OpenIoTException {
		return HBaseSite.listSites(context, criteria);
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
		return HBaseZone.createZone(context, site, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#updateZone(java.lang.String,
	 * IZoneCreateRequest)
	 */
	@Override
	public IZone updateZone(String token, IZoneCreateRequest request) throws OpenIoTException {
		return HBaseZone.updateZone(context, token, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#getZone(java.lang.String)
	 */
	@Override
	public IZone getZone(String zoneToken) throws OpenIoTException {
		return HBaseZone.getZone(context, zoneToken);
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
		return HBaseSite.listZonesForSite(context, siteToken, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#deleteZone(java.lang.String,
	 * boolean)
	 */
	@Override
	public IZone deleteZone(String zoneToken, boolean force) throws OpenIoTException {
		return HBaseZone.deleteZone(context, zoneToken, force);
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
		return HBaseDeviceGroup.createDeviceGroup(context, request);
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
		return HBaseDeviceGroup.updateDeviceGroup(context, token, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#getDeviceGroup(java.lang.String)
	 */
	@Override
	public IDeviceGroup getDeviceGroup(String token) throws OpenIoTException {
		return HBaseDeviceGroup.getDeviceGroupByToken(context, token);
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
		return HBaseDeviceGroup.listDeviceGroups(context, includeDeleted, criteria);
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
		return HBaseDeviceGroup.listDeviceGroupsWithRole(context, role, includeDeleted, criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#deleteDeviceGroup(java.lang.String,
	 * boolean)
	 */
	@Override
	public IDeviceGroup deleteDeviceGroup(String token, boolean force) throws OpenIoTException {
		return HBaseDeviceGroup.deleteDeviceGroup(context, token, force);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#addDeviceGroupElements(java.lang.String,
	 * java.util.List)
	 */
	@Override
	public List<IDeviceGroupElement> addDeviceGroupElements(String networkToken,
			List<IDeviceGroupElementCreateRequest> elements) throws OpenIoTException {
		return HBaseDeviceGroupElement.createDeviceGroupElements(context, networkToken, elements);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#removeDeviceGroupElements(java.lang.
	 * String, java.util.List)
	 */
	@Override
	public List<IDeviceGroupElement> removeDeviceGroupElements(String networkToken,
			List<IDeviceGroupElementCreateRequest> elements) throws OpenIoTException {
		return HBaseDeviceGroupElement.removeDeviceGroupElements(context, networkToken, elements);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagement#listDeviceGroupElements(java.lang.String
	 * , ISearchCriteria)
	 */
	@Override
	public SearchResults<IDeviceGroupElement> listDeviceGroupElements(String networkToken,
			ISearchCriteria criteria) throws OpenIoTException {
		return HBaseDeviceGroupElement.listDeviceGroupElements(context, networkToken, criteria);
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
		return HBaseBatchOperation.createBatchOperation(context, request);
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
		return HBaseBatchOperation.updateBatchOperation(context, token, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagement#getBatchOperation(java.lang.String)
	 */
	@Override
	public IBatchOperation getBatchOperation(String token) throws OpenIoTException {
		return HBaseBatchOperation.getBatchOperationByToken(context, token);
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
		return HBaseBatchOperation.listBatchOperations(context, includeDeleted, criteria);
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
		return HBaseBatchOperation.deleteBatchOperation(context, token, force);
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
		return HBaseBatchElement.listBatchElements(context, batchToken, criteria);
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
		return HBaseBatchElement.updateBatchElement(context, operationToken, index, request);
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
	 * Verify that the given assignment exists.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected IDeviceAssignment assertDeviceAssignment(String token) throws OpenIoTException {
		IDeviceAssignment result = HBaseDeviceAssignment.getDeviceAssignment(context, token);
		if (result == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceAssignmentToken, ErrorLevel.ERROR);
		}
		return result;
	}

	public IOpenIoTHBaseClient getClient() {
		return client;
	}

	public void setClient(IOpenIoTHBaseClient client) {
		this.client = client;
	}

	public IPayloadMarshaler getPayloadMarshaler() {
		return payloadMarshaler;
	}

	public void setPayloadMarshaler(IPayloadMarshaler payloadMarshaler) {
		this.payloadMarshaler = payloadMarshaler;
	}
}