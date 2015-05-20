/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device;

import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
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
import com.openiot.spi.search.IDateRangeSearchCriteria;
import com.openiot.spi.search.ISearchCriteria;
import com.openiot.spi.search.ISearchResults;
import com.openiot.spi.search.device.IBatchElementSearchCriteria;
import com.openiot.spi.search.device.IDeviceSearchCriteria;
import com.openiot.spi.server.lifecycle.ILifecycleComponent;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import com.openiot.spi.server.lifecycle.LifecycleStatus;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Allows classes to inject themselves as a facade around an existing device management
 * implementation. By default all methods just pass calls to the underlying delegate.
 * 
 * @author Derek
 */
public class DeviceManagementDecorator implements IDeviceManagement {

	/** Delegate instance */
	private IDeviceManagement delegate;

	public DeviceManagementDecorator(IDeviceManagement delegate) {
		this.delegate = delegate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#getComponentId()
	 */
	@Override
	public String getComponentId() {
		return delegate.getComponentId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#getComponentType()
	 */
	@Override
	public LifecycleComponentType getComponentType() {
		return delegate.getComponentType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see LifecycleComponent#lifecycleStart()
	 */
	@Override
	public void lifecycleStart() {
		delegate.lifecycleStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		delegate.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#lifecyclePause()
	 */
	@Override
	public void lifecyclePause() {
		delegate.lifecyclePause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#canPause()
	 */
	@Override
	public boolean canPause() throws OpenIoTException {
		return delegate.canPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#pause()
	 */
	@Override
	public void pause() throws OpenIoTException {
		delegate.pause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#getLogger()
	 */
	@Override
	public Logger getLogger() {
		return delegate.getLogger();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see LifecycleComponent#lifecycleStop()
	 */
	@Override
	public void lifecycleStop() {
		delegate.lifecycleStop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#getComponentName()
	 */
	@Override
	public String getComponentName() {
		return delegate.getComponentName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#getLifecycleStatus()
	 */
	@Override
	public LifecycleStatus getLifecycleStatus() {
		return delegate.getLifecycleStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#getLifecycleError()
	 */
	@Override
	public OpenIoTException getLifecycleError() {
		return delegate.getLifecycleError();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ILifecycleComponent#getLifecycleComponents()
	 */
	@Override
	public List<ILifecycleComponent> getLifecycleComponents() {
		return delegate.getLifecycleComponents();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
		delegate.stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ILifecycleComponent#findComponentsOfType(com
	 * .sitewhere.spi.server.lifecycle.LifecycleComponentType)
	 */
	@Override
	public List<ILifecycleComponent> findComponentsOfType(LifecycleComponentType type)
			throws OpenIoTException {
		return delegate.findComponentsOfType(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#logState()
	 */
	@Override
	public void logState() {
		delegate.logState();
	}

	@Override
	public IDeviceSpecification createDeviceSpecification(IDeviceSpecificationCreateRequest request)
			throws OpenIoTException {
		return delegate.createDeviceSpecification(request);
	}

	@Override
	public IDeviceSpecification getDeviceSpecificationByToken(String token) throws OpenIoTException {
		return delegate.getDeviceSpecificationByToken(token);
	}

	@Override
	public IDeviceSpecification updateDeviceSpecification(String token,
			IDeviceSpecificationCreateRequest request) throws OpenIoTException {
		return delegate.updateDeviceSpecification(token, request);
	}

	@Override
	public ISearchResults<IDeviceSpecification> listDeviceSpecifications(boolean includeDeleted,
			ISearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceSpecifications(includeDeleted, criteria);
	}

	@Override
	public IDeviceSpecification deleteDeviceSpecification(String token, boolean force)
			throws OpenIoTException {
		return delegate.deleteDeviceSpecification(token, force);
	}

	@Override
	public IDeviceCommand createDeviceCommand(IDeviceSpecification spec, IDeviceCommandCreateRequest request)
			throws OpenIoTException {
		return delegate.createDeviceCommand(spec, request);
	}

	@Override
	public IDeviceCommand getDeviceCommandByToken(String token) throws OpenIoTException {
		return delegate.getDeviceCommandByToken(token);
	}

	@Override
	public IDeviceCommand updateDeviceCommand(String token, IDeviceCommandCreateRequest request)
			throws OpenIoTException {
		return delegate.updateDeviceCommand(token, request);
	}

	@Override
	public List<IDeviceCommand> listDeviceCommands(String token, boolean includeDeleted)
			throws OpenIoTException {
		return delegate.listDeviceCommands(token, includeDeleted);
	}

	@Override
	public IDeviceCommand deleteDeviceCommand(String token, boolean force) throws OpenIoTException {
		return delegate.deleteDeviceCommand(token, force);
	}

	@Override
	public IDevice createDevice(IDeviceCreateRequest device) throws OpenIoTException {
		return delegate.createDevice(device);
	}

	@Override
	public IDevice getDeviceByHardwareId(String hardwareId) throws OpenIoTException {
		return delegate.getDeviceByHardwareId(hardwareId);
	}

	@Override
	public IDevice updateDevice(String hardwareId, IDeviceCreateRequest request) throws OpenIoTException {
		return delegate.updateDevice(hardwareId, request);
	}

	@Override
	public IDeviceAssignment getCurrentDeviceAssignment(IDevice device) throws OpenIoTException {
		return delegate.getCurrentDeviceAssignment(device);
	}

	@Override
	public ISearchResults<IDevice> listDevices(boolean includeDeleted, IDeviceSearchCriteria criteria)
			throws OpenIoTException {
		return delegate.listDevices(includeDeleted, criteria);
	}

	@Override
	public IDevice createDeviceElementMapping(String hardwareId, IDeviceElementMapping mapping)
			throws OpenIoTException {
		return delegate.createDeviceElementMapping(hardwareId, mapping);
	}

	@Override
	public IDevice deleteDeviceElementMapping(String hardwareId, String path) throws OpenIoTException {
		return delegate.deleteDeviceElementMapping(hardwareId, path);
	}

	@Override
	public IDevice deleteDevice(String hardwareId, boolean force) throws OpenIoTException {
		return delegate.deleteDevice(hardwareId, force);
	}

	@Override
	public IDeviceAssignment createDeviceAssignment(IDeviceAssignmentCreateRequest request)
			throws OpenIoTException {
		return delegate.createDeviceAssignment(request);
	}

	@Override
	public IDeviceAssignment getDeviceAssignmentByToken(String token) throws OpenIoTException {
		return delegate.getDeviceAssignmentByToken(token);
	}

	@Override
	public IDeviceAssignment deleteDeviceAssignment(String token, boolean force) throws OpenIoTException {
		return delegate.deleteDeviceAssignment(token, force);
	}

	@Override
	public IDevice getDeviceForAssignment(IDeviceAssignment assignment) throws OpenIoTException {
		return delegate.getDeviceForAssignment(assignment);
	}

	@Override
	public ISite getSiteForAssignment(IDeviceAssignment assignment) throws OpenIoTException {
		return delegate.getSiteForAssignment(assignment);
	}

	@Override
	public IDeviceAssignment updateDeviceAssignmentMetadata(String token, IMetadataProvider metadata)
			throws OpenIoTException {
		return delegate.updateDeviceAssignmentMetadata(token, metadata);
	}

	@Override
	public IDeviceAssignment updateDeviceAssignmentStatus(String token, DeviceAssignmentStatus status)
			throws OpenIoTException {
		return delegate.updateDeviceAssignmentStatus(token, status);
	}

	@Override
	public IDeviceAssignment updateDeviceAssignmentState(String token, IDeviceAssignmentState state)
			throws OpenIoTException {
		return delegate.updateDeviceAssignmentState(token, state);
	}

	@Override
	public IDeviceEventBatchResponse addDeviceEventBatch(String assignmentToken, IDeviceEventBatch batch)
			throws OpenIoTException {
		return delegate.addDeviceEventBatch(assignmentToken, batch);
	}

	@Override
	public IDeviceAssignment endDeviceAssignment(String token) throws OpenIoTException {
		return delegate.endDeviceAssignment(token);
	}

	@Override
	public ISearchResults<IDeviceAssignment> getDeviceAssignmentHistory(String hardwareId,
			ISearchCriteria criteria) throws OpenIoTException {
		return delegate.getDeviceAssignmentHistory(hardwareId, criteria);
	}

	@Override
	public ISearchResults<IDeviceAssignment> getDeviceAssignmentsForSite(String siteToken,
			ISearchCriteria criteria) throws OpenIoTException {
		return delegate.getDeviceAssignmentsForSite(siteToken, criteria);
	}

	@Override
	public IDeviceEvent getDeviceEventById(String id) throws OpenIoTException {
		return delegate.getDeviceEventById(id);
	}

	@Override
	public ISearchResults<IDeviceEvent> listDeviceEvents(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceEvents(assignmentToken, criteria);
	}

	@Override
	public IDeviceMeasurements addDeviceMeasurements(String assignmentToken,
			IDeviceMeasurementsCreateRequest measurements) throws OpenIoTException {
		return delegate.addDeviceMeasurements(assignmentToken, measurements);
	}

	@Override
	public ISearchResults<IDeviceMeasurements> listDeviceMeasurements(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceMeasurements(assignmentToken, criteria);
	}

	@Override
	public ISearchResults<IDeviceMeasurements> listDeviceMeasurementsForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceMeasurementsForSite(siteToken, criteria);
	}

	@Override
	public IDeviceLocation addDeviceLocation(String assignmentToken, IDeviceLocationCreateRequest request)
			throws OpenIoTException {
		return delegate.addDeviceLocation(assignmentToken, request);
	}

	@Override
	public ISearchResults<IDeviceLocation> listDeviceLocations(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceLocations(assignmentToken, criteria);
	}

	@Override
	public ISearchResults<IDeviceLocation> listDeviceLocationsForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceLocationsForSite(siteToken, criteria);
	}

	@Override
	public ISearchResults<IDeviceLocation> listDeviceLocations(List<String> assignmentTokens,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceLocations(assignmentTokens, criteria);
	}

	@Override
	public IDeviceAlert addDeviceAlert(String assignmentToken, IDeviceAlertCreateRequest request)
			throws OpenIoTException {
		return delegate.addDeviceAlert(assignmentToken, request);
	}

	@Override
	public ISearchResults<IDeviceAlert> listDeviceAlerts(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceAlerts(assignmentToken, criteria);
	}

	@Override
	public ISearchResults<IDeviceAlert> listDeviceAlertsForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceAlertsForSite(siteToken, criteria);
	}

	@Override
	public IDeviceCommandInvocation addDeviceCommandInvocation(String assignmentToken,
			IDeviceCommand command, IDeviceCommandInvocationCreateRequest request) throws OpenIoTException {
		return delegate.addDeviceCommandInvocation(assignmentToken, command, request);
	}

	@Override
	public ISearchResults<IDeviceCommandInvocation> listDeviceCommandInvocations(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceCommandInvocations(assignmentToken, criteria);
	}

	@Override
	public ISearchResults<IDeviceCommandInvocation> listDeviceCommandInvocationsForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceCommandInvocationsForSite(siteToken, criteria);
	}

	@Override
	public ISearchResults<IDeviceCommandResponse> listDeviceCommandInvocationResponses(String invocationId)
			throws OpenIoTException {
		return delegate.listDeviceCommandInvocationResponses(invocationId);
	}

	@Override
	public IDeviceCommandResponse addDeviceCommandResponse(String assignmentToken,
			IDeviceCommandResponseCreateRequest request) throws OpenIoTException {
		return delegate.addDeviceCommandResponse(assignmentToken, request);
	}

	@Override
	public ISearchResults<IDeviceCommandResponse> listDeviceCommandResponses(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceCommandResponses(assignmentToken, criteria);
	}

	@Override
	public ISearchResults<IDeviceCommandResponse> listDeviceCommandResponsesForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceCommandResponsesForSite(siteToken, criteria);
	}

	@Override
	public IDeviceStateChange addDeviceStateChange(String assignmentToken,
			IDeviceStateChangeCreateRequest request) throws OpenIoTException {
		return delegate.addDeviceStateChange(assignmentToken, request);
	}

	@Override
	public ISearchResults<IDeviceStateChange> listDeviceStateChanges(String assignmentToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceStateChanges(assignmentToken, criteria);
	}

	@Override
	public ISearchResults<IDeviceStateChange> listDeviceStateChangesForSite(String siteToken,
			IDateRangeSearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceStateChangesForSite(siteToken, criteria);
	}

	@Override
	public ISite createSite(ISiteCreateRequest request) throws OpenIoTException {
		return delegate.createSite(request);
	}

	@Override
	public ISite deleteSite(String siteToken, boolean force) throws OpenIoTException {
		return delegate.deleteSite(siteToken, force);
	}

	@Override
	public ISite updateSite(String siteToken, ISiteCreateRequest request) throws OpenIoTException {
		return delegate.updateSite(siteToken, request);
	}

	@Override
	public ISite getSiteByToken(String token) throws OpenIoTException {
		return delegate.getSiteByToken(token);
	}

	@Override
	public ISearchResults<ISite> listSites(ISearchCriteria criteria) throws OpenIoTException {
		return delegate.listSites(criteria);
	}

	@Override
	public IZone createZone(ISite site, IZoneCreateRequest request) throws OpenIoTException {
		return delegate.createZone(site, request);
	}

	@Override
	public IZone updateZone(String token, IZoneCreateRequest request) throws OpenIoTException {
		return delegate.updateZone(token, request);
	}

	@Override
	public IZone getZone(String zoneToken) throws OpenIoTException {
		return delegate.getZone(zoneToken);
	}

	@Override
	public ISearchResults<IZone> listZones(String siteToken, ISearchCriteria criteria)
			throws OpenIoTException {
		return delegate.listZones(siteToken, criteria);
	}

	@Override
	public IZone deleteZone(String zoneToken, boolean force) throws OpenIoTException {
		return delegate.deleteZone(zoneToken, force);
	}

	@Override
	public IDeviceGroup createDeviceGroup(IDeviceGroupCreateRequest request) throws OpenIoTException {
		return delegate.createDeviceGroup(request);
	}

	@Override
	public IDeviceGroup updateDeviceGroup(String token, IDeviceGroupCreateRequest request)
			throws OpenIoTException {
		return delegate.updateDeviceGroup(token, request);
	}

	@Override
	public IDeviceGroup getDeviceGroup(String token) throws OpenIoTException {
		return delegate.getDeviceGroup(token);
	}

	@Override
	public ISearchResults<IDeviceGroup> listDeviceGroups(boolean includeDeleted, ISearchCriteria criteria)
			throws OpenIoTException {
		return delegate.listDeviceGroups(includeDeleted, criteria);
	}

	@Override
	public ISearchResults<IDeviceGroup> listDeviceGroupsWithRole(String role, boolean includeDeleted,
			ISearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceGroupsWithRole(role, includeDeleted, criteria);
	}

	@Override
	public List<IDeviceGroupElement> addDeviceGroupElements(String groupToken,
			List<IDeviceGroupElementCreateRequest> elements) throws OpenIoTException {
		return delegate.addDeviceGroupElements(groupToken, elements);
	}

	@Override
	public List<IDeviceGroupElement> removeDeviceGroupElements(String groupToken,
			List<IDeviceGroupElementCreateRequest> elements) throws OpenIoTException {
		return delegate.removeDeviceGroupElements(groupToken, elements);
	}

	@Override
	public SearchResults<IDeviceGroupElement> listDeviceGroupElements(String groupToken,
			ISearchCriteria criteria) throws OpenIoTException {
		return delegate.listDeviceGroupElements(groupToken, criteria);
	}

	@Override
	public IDeviceGroup deleteDeviceGroup(String token, boolean force) throws OpenIoTException {
		return delegate.deleteDeviceGroup(token, force);
	}

	@Override
	public IBatchOperation createBatchOperation(IBatchOperationCreateRequest request)
			throws OpenIoTException {
		return delegate.createBatchOperation(request);
	}

	@Override
	public IBatchOperation updateBatchOperation(String token, IBatchOperationUpdateRequest request)
			throws OpenIoTException {
		return delegate.updateBatchOperation(token, request);
	}

	@Override
	public IBatchOperation getBatchOperation(String token) throws OpenIoTException {
		return delegate.getBatchOperation(token);
	}

	@Override
	public ISearchResults<IBatchOperation> listBatchOperations(boolean includeDeleted,
			ISearchCriteria criteria) throws OpenIoTException {
		return delegate.listBatchOperations(includeDeleted, criteria);
	}

	@Override
	public IBatchOperation deleteBatchOperation(String token, boolean force) throws OpenIoTException {
		return delegate.deleteBatchOperation(token, force);
	}

	@Override
	public SearchResults<IBatchElement> listBatchElements(String batchToken,
			IBatchElementSearchCriteria criteria) throws OpenIoTException {
		return delegate.listBatchElements(batchToken, criteria);
	}

	@Override
	public IBatchElement updateBatchElement(String operationToken, long index,
			IBatchElementUpdateRequest request) throws OpenIoTException {
		return delegate.updateBatchElement(operationToken, index, request);
	}

	@Override
	public IBatchOperation createBatchCommandInvocation(IBatchCommandInvocationRequest request)
			throws OpenIoTException {
		return delegate.createBatchCommandInvocation(request);
	}

	public IDeviceManagement getDelegate() {
		return delegate;
	}

	public void setDelegate(IDeviceManagement delegate) {
		this.delegate = delegate;
	}
}