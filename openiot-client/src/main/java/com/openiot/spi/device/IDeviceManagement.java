/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device;

import com.openiot.rest.model.device.batch.BatchOperation;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.common.IMetadataProvider;
import com.openiot.spi.device.batch.IBatchElement;
import com.openiot.spi.device.batch.IBatchOperation;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.event.*;
import com.openiot.spi.device.event.request.*;
import com.openiot.spi.device.group.IDeviceGroup;
import com.openiot.spi.device.group.IDeviceGroupElement;
import com.openiot.spi.device.request.*;
import com.openiot.spi.search.IDateRangeSearchCriteria;
import com.openiot.spi.search.ISearchCriteria;
import com.openiot.spi.search.ISearchResults;
import com.openiot.spi.search.device.IBatchElementSearchCriteria;
import com.openiot.spi.search.device.IDeviceSearchCriteria;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;

import java.util.List;

/**
 * Interface for device operations.
 * 
 * @author Derek
 */
public interface IDeviceManagement extends ILifecycleComponent {

	/**
	 * Create a new device specification.
	 * 
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceSpecification createDeviceSpecification(IDeviceSpecificationCreateRequest request)
			throws OpenIoTException;

	/**
	 * Get a device specification by unique token.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceSpecification getDeviceSpecificationByToken(String token) throws OpenIoTException;

	/**
	 * Update an existing device specification.
	 * 
	 * @param token
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceSpecification updateDeviceSpecification(String token,
			IDeviceSpecificationCreateRequest request) throws OpenIoTException;

	/**
	 * List device specifications that match the search criteria.
	 * 
	 * @param includeDeleted
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceSpecification> listDeviceSpecifications(boolean includeDeleted,
			ISearchCriteria criteria) throws OpenIoTException;

	/**
	 * Delete an existing device specification.
	 * 
	 * @param token
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceSpecification deleteDeviceSpecification(String token, boolean force)
			throws OpenIoTException;

	/**
	 * Creates a device command associated with an existing device specification.
	 * 
	 * @param spec
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceCommand createDeviceCommand(IDeviceSpecification spec, IDeviceCommandCreateRequest request)
			throws OpenIoTException;

	/**
	 * Get a device command by unique token.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceCommand getDeviceCommandByToken(String token) throws OpenIoTException;

	/**
	 * Update an existing device command.
	 * 
	 * @param token
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceCommand updateDeviceCommand(String token, IDeviceCommandCreateRequest request)
			throws OpenIoTException;

	/**
	 * List device command objects associated with a device specification.
	 * 
	 * @param specToken
	 * @param includeDeleted
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<IDeviceCommand> listDeviceCommands(String specToken, boolean includeDeleted)
			throws OpenIoTException;

	/**
	 * Delete an existing device command.
	 * 
	 * @param token
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceCommand deleteDeviceCommand(String token, boolean force) throws OpenIoTException;

	/**
	 * Create a new device.
	 * 
	 * @param device
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDevice createDevice(IDeviceCreateRequest device) throws OpenIoTException;

	/**
	 * Gets a device by unique hardware id.
	 * 
	 * @param hardwareId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDevice getDeviceByHardwareId(String hardwareId) throws OpenIoTException;

	/**
	 * Update device information.
	 * 
	 * @param hardwareId
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDevice updateDevice(String hardwareId, IDeviceCreateRequest request) throws OpenIoTException;

	/**
	 * Gets the current assignment for a device. Null if none.
	 * 
	 * @param device
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceAssignment getCurrentDeviceAssignment(IDevice device) throws OpenIoTException;

	/**
	 * List devices that meet the given criteria.
	 * 
	 * @param includeDeleted
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDevice> listDevices(boolean includeDeleted, IDeviceSearchCriteria criteria)
			throws OpenIoTException;

	/**
	 * Create an {@link IDeviceElementMapping} for a nested device.
	 * 
	 * @param hardwareId
	 * @param mapping
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDevice createDeviceElementMapping(String hardwareId, IDeviceElementMapping mapping)
			throws OpenIoTException;

	/**
	 * Delete an exising {@link IDeviceElementMapping} from a device.
	 * 
	 * @param hardwareId
	 * @param path
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDevice deleteDeviceElementMapping(String hardwareId, String path) throws OpenIoTException;

	/**
	 * Delete an existing device.
	 * 
	 * @param hardwareId
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDevice deleteDevice(String hardwareId, boolean force) throws OpenIoTException;

	/**
	 * Create a new device assignment.
	 * 
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceAssignment createDeviceAssignment(IDeviceAssignmentCreateRequest request)
			throws OpenIoTException;

	/**
	 * Get a device assignment by unique token.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceAssignment getDeviceAssignmentByToken(String token) throws OpenIoTException;

	/**
	 * Delete a device assignment. Depending on 'force' flag the assignment will be marked
	 * for delete or actually be deleted.
	 * 
	 * @param token
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceAssignment deleteDeviceAssignment(String token, boolean force) throws OpenIoTException;

	/**
	 * Get the device associated with an assignment.
	 * 
	 * @param assignment
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDevice getDeviceForAssignment(IDeviceAssignment assignment) throws OpenIoTException;

	/**
	 * Get the site associated with an assignment.
	 * 
	 * @param assignment
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISite getSiteForAssignment(IDeviceAssignment assignment) throws OpenIoTException;

	/**
	 * Update metadata associated with a device assignment.
	 * 
	 * @param token
	 * @param metadata
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceAssignment updateDeviceAssignmentMetadata(String token, IMetadataProvider metadata)
			throws OpenIoTException;

	/**
	 * Update the status of an existing device assignment.
	 * 
	 * @param token
	 * @param status
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceAssignment updateDeviceAssignmentStatus(String token, DeviceAssignmentStatus status)
			throws OpenIoTException;

	/**
	 * Updates the current state of a device assignment.
	 * 
	 * @param token
	 * @param state
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceAssignment updateDeviceAssignmentState(String token, IDeviceAssignmentState state)
			throws OpenIoTException;

	/**
	 * Add a batch of events for the given assignment.
	 * 
	 * @param assignmentToken
	 * @param batch
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceEventBatchResponse addDeviceEventBatch(String assignmentToken, IDeviceEventBatch batch)
			throws OpenIoTException;

	/**
	 * Ends a device assignment.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceAssignment endDeviceAssignment(String token) throws OpenIoTException;

	/**
	 * Get the device assignment history for a given device.
	 * 
	 * @param hardwareId
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceAssignment> getDeviceAssignmentHistory(String hardwareId,
			ISearchCriteria criteria) throws OpenIoTException;

	/**
	 * Get a list of device assignments for a site.
	 * 
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceAssignment> getDeviceAssignmentsForSite(String siteToken,
			ISearchCriteria criteria) throws OpenIoTException;

	/**
	 * Get a device event by unique id.
	 * 
	 * @param id
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceEvent getDeviceEventById(String id) throws OpenIoTException;

	/**
	 * List all events for the given assignment that meet the search criteria.
	 * 
	 * @param assignmentToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceEvent> listDeviceEvents(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException;

	/**
	 * Add measurements for a given device assignment.
	 * 
	 * @param assignmentToken
	 * @param measurements
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceMeasurements addDeviceMeasurements(String assignmentToken,
			IDeviceMeasurementsCreateRequest measurements) throws OpenIoTException;

	/**
	 * Gets device measurement entries for an assignment based on criteria.
	 * 
	 * @param assignmentToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceMeasurements> listDeviceMeasurements(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException;

	/**
	 * List device measurements for a site.
	 * 
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceMeasurements> listDeviceMeasurementsForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException;

	/**
	 * Add location for a given device assignment.
	 * 
	 * @param assignmentToken
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceLocation addDeviceLocation(String assignmentToken, IDeviceLocationCreateRequest request)
			throws OpenIoTException;

	/**
	 * Gets device location entries for an assignment.
	 * 
	 * @param assignmentToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceLocation> listDeviceLocations(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException;

	/**
	 * List device locations for a site.
	 * 
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceLocation> listDeviceLocationsForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException;

	/**
	 * List device locations for the given tokens within the given time range.
	 * 
	 * @param assignmentTokens
	 * @param start
	 * @param end
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceLocation> listDeviceLocations(List<String> assignmentTokens,
			IDateRangeSearchCriteria criteria) throws OpenIoTException;

	/**
	 * Add alert for a given device assignment.
	 * 
	 * @param assignmentToken
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceAlert addDeviceAlert(String assignmentToken, IDeviceAlertCreateRequest request)
			throws OpenIoTException;

	/**
	 * Gets the most recent device alert entries for an assignment.
	 * 
	 * @param assignmentToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceAlert> listDeviceAlerts(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException;

	/**
	 * List device alerts for a site.
	 * 
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceAlert> listDeviceAlertsForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException;

	/**
	 * Add a device command invocation event for the given assignment.
	 * 
	 * @param assignmentToken
	 * @param command
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceCommandInvocation addDeviceCommandInvocation(String assignmentToken,
			IDeviceCommand command, IDeviceCommandInvocationCreateRequest request) throws OpenIoTException;

	/**
	 * Gets device command invocations for an assignment based on criteria.
	 * 
	 * @param assignmentToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceCommandInvocation> listDeviceCommandInvocations(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException;

	/**
	 * List device command invocations for a site.
	 * 
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceCommandInvocation> listDeviceCommandInvocationsForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException;

	/**
	 * List responses associated with a command invocation.
	 * 
	 * @param invocationId
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceCommandResponse> listDeviceCommandInvocationResponses(String invocationId)
			throws OpenIoTException;

	/**
	 * Adds a new device command response event.
	 * 
	 * @param assignmentToken
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceCommandResponse addDeviceCommandResponse(String assignmentToken,
			IDeviceCommandResponseCreateRequest request) throws OpenIoTException;

	/**
	 * Gets the most recent device command response entries for an assignment.
	 * 
	 * @param assignmentToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceCommandResponse> listDeviceCommandResponses(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException;

	/**
	 * List device command responses for a site.
	 * 
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceCommandResponse> listDeviceCommandResponsesForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException;

	/**
	 * Adds a new device state change event.
	 * 
	 * @param assignmentToken
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceStateChange addDeviceStateChange(String assignmentToken,
			IDeviceStateChangeCreateRequest request) throws OpenIoTException;

	/**
	 * Gets the most recent device state change entries for an assignment.
	 * 
	 * @param assignmentToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceStateChange> listDeviceStateChanges(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException;

	/**
	 * List device state changes for a site.
	 * 
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceStateChange> listDeviceStateChangesForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException;

	/**
	 * Create a site based on the given input.
	 * 
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISite createSite(ISiteCreateRequest request) throws OpenIoTException;

	/**
	 * Delete a site based on unique site token. If 'force' is specified, the database
	 * object will be deleted, otherwise the deleted flag will be set to true.
	 * 
	 * @param siteToken
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISite deleteSite(String siteToken, boolean force) throws OpenIoTException;

	/**
	 * Update information for a site.
	 * 
	 * @param siteToken
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISite updateSite(String siteToken, ISiteCreateRequest request) throws OpenIoTException;

	/**
	 * Get a site by unique token.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISite getSiteByToken(String token) throws OpenIoTException;

	/**
	 * Get a list of all sites.
	 * 
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<ISite> listSites(ISearchCriteria criteria) throws OpenIoTException;

	/**
	 * Create a new zone.
	 * 
	 * @param site
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IZone createZone(ISite site, IZoneCreateRequest request) throws OpenIoTException;

	/**
	 * Update an existing zone.
	 * 
	 * @param token
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IZone updateZone(String token, IZoneCreateRequest request) throws OpenIoTException;

	/**
	 * Get a zone by its unique token.
	 * 
	 * @param zoneToken
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IZone getZone(String zoneToken) throws OpenIoTException;

	/**
	 * Get a list of all zones associated with a Site.
	 * 
	 * @param siteToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IZone> listZones(String siteToken, ISearchCriteria criteria)
			throws OpenIoTException;

	/**
	 * Delete a zone given its unique token.
	 * 
	 * @param zoneToken
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IZone deleteZone(String zoneToken, boolean force) throws OpenIoTException;

	/**
	 * Create a new device group.
	 * 
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceGroup createDeviceGroup(IDeviceGroupCreateRequest request) throws OpenIoTException;

	/**
	 * Update an existing device group.
	 * 
	 * @param token
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceGroup updateDeviceGroup(String token, IDeviceGroupCreateRequest request)
			throws OpenIoTException;

	/**
	 * Get a device network by unique token.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceGroup getDeviceGroup(String token) throws OpenIoTException;

	/**
	 * List device groups.
	 * 
	 * @param includeDeleted
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceGroup> listDeviceGroups(boolean includeDeleted, ISearchCriteria criteria)
			throws OpenIoTException;

	/**
	 * Lists all device groups that have the given role.
	 * 
	 * @param role
	 * @param includeDeleted
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IDeviceGroup> listDeviceGroupsWithRole(String role, boolean includeDeleted,
			ISearchCriteria criteria) throws OpenIoTException;

	/**
	 * Delete a device group.
	 * 
	 * @param token
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IDeviceGroup deleteDeviceGroup(String token, boolean force) throws OpenIoTException;

	/**
	 * Add elements to a device group.
	 * 
	 * @param groupToken
	 * @param elements
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<IDeviceGroupElement> addDeviceGroupElements(String groupToken,
			List<IDeviceGroupElementCreateRequest> elements) throws OpenIoTException;

	/**
	 * Remove selected elements from a device group.
	 * 
	 * @param groupToken
	 * @param elements
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<IDeviceGroupElement> removeDeviceGroupElements(String groupToken,
			List<IDeviceGroupElementCreateRequest> elements) throws OpenIoTException;

	/**
	 * List device group elements that meet the given criteria.
	 * 
	 * @param groupToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public SearchResults<IDeviceGroupElement> listDeviceGroupElements(String groupToken,
			ISearchCriteria criteria) throws OpenIoTException;

	/**
	 * Creates an {@link com.openiot.spi.device.batch.IBatchOperation} to perform an operation on multiple devices.
	 * 
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IBatchOperation createBatchOperation(IBatchOperationCreateRequest request)
			throws OpenIoTException;

	/**
	 * Update an existing {@link IBatchOperation}.
	 * 
	 * @param token
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IBatchOperation updateBatchOperation(String token, IBatchOperationUpdateRequest request)
			throws OpenIoTException;

	/**
	 * Get an {@link IBatchOperation} by unique token.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IBatchOperation getBatchOperation(String token) throws OpenIoTException;

	/**
	 * List batch operations based on the given criteria.
	 * 
	 * @param includeDeleted
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public ISearchResults<IBatchOperation> listBatchOperations(boolean includeDeleted,
			ISearchCriteria criteria) throws OpenIoTException;

	/**
	 * Deletes a batch operation and its elements.
	 * 
	 * @param token
	 * @param force
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IBatchOperation deleteBatchOperation(String token, boolean force) throws OpenIoTException;

	/**
	 * Lists elements for an {@link IBatchOperation} that meet the given criteria.
	 * 
	 * @param batchToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public SearchResults<IBatchElement> listBatchElements(String batchToken,
			IBatchElementSearchCriteria criteria) throws OpenIoTException;

	/**
	 * Updates an existing batch operation element.
	 * 
	 * @param operationToken
	 * @param index
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IBatchElement updateBatchElement(String operationToken, long index,
			IBatchElementUpdateRequest request) throws OpenIoTException;

	/**
	 * Creates a {@link BatchOperation} that will invoke a command on multiple devices.
	 * 
	 * @param request
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public IBatchOperation createBatchCommandInvocation(IBatchCommandInvocationRequest request)
			throws OpenIoTException;
}