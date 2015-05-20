/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi;

import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.rest.model.device.*;
import com.openiot.rest.model.device.batch.BatchOperation;
import com.openiot.rest.model.device.command.DeviceCommand;
import com.openiot.rest.model.device.event.*;
import com.openiot.rest.model.device.event.request.DeviceAlertCreateRequest;
import com.openiot.rest.model.device.event.request.DeviceLocationCreateRequest;
import com.openiot.rest.model.device.event.request.DeviceMeasurementsCreateRequest;
import com.openiot.rest.model.device.request.*;
import com.openiot.rest.model.search.*;
import com.openiot.rest.model.system.Version;
import com.openiot.spi.device.request.IDeviceAssignmentCreateRequest;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Interface for OpenIoT client calls.
 * 
 * @author Derek Adams
 */
public interface IOpenIoTClient {

	/**
	 * Get OpenIoT version information.
	 * 
	 * @return
	 * @throws OpenIoTException
	 */
	public Version getOpenIoTVersion() throws OpenIoTException;

	/**
	 * Create a new site.
	 * 
	 * @param request
	 * @return
	 * @throws OpenIoTException
	 */
	public Site createSite(SiteCreateRequest request) throws OpenIoTException;

	/**
	 * Create a new device specification.
	 * 
	 * @param request
	 * @return
	 * @throws OpenIoTException
	 */
	public DeviceSpecification createDeviceSpecification(DeviceSpecificationCreateRequest request)
			throws OpenIoTException;

	/**
	 * Create a new device command for a specification.
	 * 
	 * @param specToken
	 * @param request
	 * @return
	 * @throws OpenIoTException
	 */
	public DeviceCommand createDeviceCommand(String specToken, DeviceCommandCreateRequest request)
			throws OpenIoTException;

	/**
	 * Get a device specification by token.
	 * 
	 * @param token
	 * @return
	 * @throws OpenIoTException
	 */
	public DeviceSpecification getDeviceSpecificationByToken(String token) throws OpenIoTException;

	/**
	 * Create a new device.
	 * 
	 * @param request
	 *            information about device to be created
	 * @return the created device
	 * @throws OpenIoTException
	 */
	public Device createDevice(DeviceCreateRequest request) throws OpenIoTException;

	/**
	 * Get a device by its unique hardware id.
	 * 
	 * @param hardwareId
	 *            hardware id of device to return
	 * @return device if found or null if not
	 * @throws OpenIoTException
	 */
	public Device getDeviceByHardwareId(String hardwareId) throws OpenIoTException;

	/**
	 * Update information for an existing device.
	 * 
	 * @param hardwareId
	 *            hardware id of device to update
	 * @param request
	 *            updated information
	 * @throws OpenIoTException
	 */
	public Device updateDevice(String hardwareId, DeviceCreateRequest request) throws OpenIoTException;

	/**
	 * List devices that meet the given criteria.
	 * 
	 * @param includeDeleted
	 * @param excludeAssigned
	 * @param populateSpecification
	 * @param populateAssignment
	 * @param pageNumber
	 * @param pageSize
	 * @param createDateStart
	 * @param createDateEnd
	 * @return
	 * @throws OpenIoTException
	 */
	public DeviceSearchResults listDevices(Boolean includeDeleted, Boolean excludeAssigned,
			Boolean populateSpecification, Boolean populateAssignment, Integer pageNumber, Integer pageSize,
			Calendar createDateStart, Calendar createDateEnd) throws OpenIoTException;

	/**
	 * Delete a device.
	 * 
	 * @param hardwareId
	 *            hardware id of device to delete
	 * @param force
	 *            if true, data is deleted. if false, delete flag is set to true
	 * @return
	 * @throws OpenIoTException
	 */
	public Device deleteDevice(String hardwareId, boolean force) throws OpenIoTException;

	/**
	 * Get current device assignment for a device based on hardware id.
	 * 
	 * @param hardwareId
	 *            unique hardware id of device
	 * @return device assignment information
	 * @throws OpenIoTException
	 */
	public DeviceAssignment getCurrentAssignmentForDevice(String hardwareId) throws OpenIoTException;

	/**
	 * Get the history of device assignments for a given hardware id.
	 * 
	 * @param hardwareId
	 * @return
	 * @throws OpenIoTException
	 */
	public DeviceAssignmentSearchResults listDeviceAssignmentHistory(String hardwareId)
			throws OpenIoTException;

	/**
	 * Add a batch of events to the current assignment for the given hardware id.
	 * 
	 * @param hardwareId
	 *            hardware id whose assignment will have events added
	 * @param batch
	 *            batch of events that will be added
	 * @return response of events that were created
	 * @throws OpenIoTException
	 */
	public DeviceEventBatchResponse addDeviceEventBatch(String hardwareId, DeviceEventBatch batch)
			throws OpenIoTException;

	/**
	 * Create a new device assignment based on the given inputs.
	 * 
	 * @param request
	 *            information about the new assignment
	 * @return the assignment that was created.
	 * @throws OpenIoTException
	 */
	public DeviceAssignment createDeviceAssignment(IDeviceAssignmentCreateRequest request)
			throws OpenIoTException;

	/**
	 * Get a device assignment by its unique token.
	 * 
	 * @param assignmentToken
	 *            unique assignment token
	 * @return the device assignment
	 * @throws OpenIoTException
	 */
	public DeviceAssignment getDeviceAssignmentByToken(String assignmentToken) throws OpenIoTException;

	/**
	 * Delete a device assignment based on its unique token.
	 * 
	 * @param assignmentToken
	 *            unique assignment token
	 * @param force
	 *            value of false sets deleted flag, true deletes data.
	 * @return assignment that was deleted
	 * @throws OpenIoTException
	 */
	public DeviceAssignment deleteDeviceAssignment(String assignmentToken, boolean force)
			throws OpenIoTException;

	/**
	 * Update the metadata for an existing device assignment.
	 * 
	 * @param token
	 * @param metadata
	 * @return
	 * @throws OpenIoTException
	 */
	public DeviceAssignment updateDeviceAssignmentMetadata(String token, MetadataProvider metadata)
			throws OpenIoTException;

	/**
	 * Create measurements for an assignment.
	 * 
	 * @param assignmentToken
	 * @param measurements
	 * @return
	 * @throws OpenIoTException
	 */
	public DeviceMeasurements createDeviceMeasurements(String assignmentToken,
			DeviceMeasurementsCreateRequest measurements) throws OpenIoTException;

	/**
	 * Get most recent device measurements for a given assignment.
	 * 
	 * @param assignmentToken
	 * @param maxCount
	 * @return
	 * @throws OpenIoTException
	 */
	public SearchResults<DeviceMeasurements> listDeviceMeasurements(String assignmentToken, int maxCount)
			throws OpenIoTException;

	/**
	 * Create a new device location for an assignment.
	 * 
	 * @param assignmentToken
	 * @param request
	 * @return
	 * @throws OpenIoTException
	 */
	public DeviceLocation createDeviceLocation(String assignmentToken, DeviceLocationCreateRequest request)
			throws OpenIoTException;

	/**
	 * Get most recent device locations for a given assignment.
	 * 
	 * @param assignmentToken
	 * @param maxCount
	 * @return
	 * @throws OpenIoTException
	 */
	public DeviceLocationSearchResults listDeviceLocations(String assignmentToken, int maxCount)
			throws OpenIoTException;

	/**
	 * Create a new alert for a device assignment.
	 * 
	 * @param assignmentToken
	 * @param request
	 * @return
	 * @throws OpenIoTException
	 */
	public DeviceAlert createDeviceAlert(String assignmentToken, DeviceAlertCreateRequest request)
			throws OpenIoTException;

	/**
	 * Get most recent device alerts for a given assignment.
	 * 
	 * @param assignmentToken
	 * @param maxCount
	 * @return
	 * @throws OpenIoTException
	 */
	public DeviceAlertSearchResults listDeviceAlerts(String assignmentToken, int maxCount)
			throws OpenIoTException;

	/**
	 * Create a new zone associated with a site.
	 * 
	 * @param siteToken
	 *            unique token for site
	 * @param request
	 *            information for new zone
	 * @return zone that was created.
	 * @throws OpenIoTException
	 */
	public Zone createZone(String siteToken, ZoneCreateRequest request) throws OpenIoTException;

	/**
	 * List zones associated with a given site.
	 * 
	 * @param siteToken
	 * @return
	 * @throws OpenIoTException
	 */
	public ZoneSearchResults listZonesForSite(String siteToken) throws OpenIoTException;

	/**
	 * Invokes a command on a list of devices as a batch operation.
	 * 
	 * @param batchToken
	 * @param commandToken
	 * @param parameters
	 * @param hardwareIds
	 * @return
	 * @throws OpenIoTException
	 */
	public BatchOperation createBatchCommandInvocation(String batchToken, String commandToken,
			Map<String, String> parameters, List<String> hardwareIds) throws OpenIoTException;
}