/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.openiot.rest.model.asset.HardwareAsset;
import com.openiot.rest.model.asset.LocationAsset;
import com.openiot.rest.model.asset.PersonAsset;
import com.openiot.rest.model.common.MetadataProviderEntity;
import com.openiot.rest.model.datatype.JsonDateSerializer;
import com.openiot.spi.device.DeviceAssignmentStatus;
import com.openiot.spi.device.DeviceAssignmentType;
import com.openiot.spi.device.IDeviceAssignment;

import java.io.Serializable;
import java.util.Date;

/**
 * Device assignment value object used for marshaling.
 * 
 * @author dadams
 */
@JsonInclude(Include.NON_NULL)
public class DeviceAssignment extends MetadataProviderEntity implements IDeviceAssignment, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = 4053925804888464375L;

	/** Unique assignment token */
	private String token;

	/** Device hardware id */
	private String deviceHardwareId;

	/** Type of associated asset */
	private DeviceAssignmentType assignmentType;

	/** Id of asset module */
	private String assetModuleId;

	/** Id of associated asset */
	private String assetId;

	/** Associated asset name */
	private String assetName;

	/** Associated asset image */
	private String assetImageUrl;

	/** Assigned site */
	private Site site;

	/** Site token */
	private String siteToken;

	/** Assignment status */
	private DeviceAssignmentStatus status;

	/** Assignment start date */
	private Date activeDate;

	/** Assignment end date */
	private Date releasedDate;

	/** FIELDS BELOW DEPEND ON MARSHALING PARAMETERS */

	/** Device being assigned */
	private Device device;

	/** Last known location */
	private DeviceAssignmentState state;

	/** Associated person asset */
	private PersonAsset associatedPerson;

	/** Associated hardware asset */
	private HardwareAsset associatedHardware;

	/** Associated location asset */
	private LocationAsset associatedLocation;

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceAssignment#getToken()
	 */
	@Override
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceAssignment#getDeviceHardwareId()
	 */
	@Override
	public String getDeviceHardwareId() {
		return deviceHardwareId;
	}

	public void setDeviceHardwareId(String deviceHardwareId) {
		this.deviceHardwareId = deviceHardwareId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceAssignment#getAssignmentType()
	 */
	@Override
	public DeviceAssignmentType getAssignmentType() {
		return assignmentType;
	}

	public void setAssignmentType(DeviceAssignmentType assignmentType) {
		this.assignmentType = assignmentType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceAssignment#getAssetModuleId()
	 */
	public String getAssetModuleId() {
		return assetModuleId;
	}

	public void setAssetModuleId(String assetModuleId) {
		this.assetModuleId = assetModuleId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceAssignment#getAssetId()
	 */
	@Override
	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	public String getAssetName() {
		return assetName;
	}

	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public String getAssetImageUrl() {
		return assetImageUrl;
	}

	public void setAssetImageUrl(String assetImageUrl) {
		this.assetImageUrl = assetImageUrl;
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceAssignment#getSiteToken()
	 */
	@Override
	public String getSiteToken() {
		return siteToken;
	}

	public void setSiteToken(String siteToken) {
		this.siteToken = siteToken;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceAssignment#getStatus()
	 */
	@Override
	public DeviceAssignmentStatus getStatus() {
		return status;
	}

	public void setStatus(DeviceAssignmentStatus status) {
		this.status = status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceAssignment#getActiveDate()
	 */
	@JsonSerialize(using = JsonDateSerializer.class)
	@Override
	public Date getActiveDate() {
		return activeDate;
	}

	public void setActiveDate(Date activeDate) {
		this.activeDate = activeDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceAssignment#getReleasedDate()
	 */
	@JsonSerialize(using = JsonDateSerializer.class)
	@Override
	public Date getReleasedDate() {
		return releasedDate;
	}

	public void setReleasedDate(Date releasedDate) {
		this.releasedDate = releasedDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceAssignment#getState()
	 */
	@Override
	public DeviceAssignmentState getState() {
		return state;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public void setState(DeviceAssignmentState state) {
		this.state = state;
	}

	public PersonAsset getAssociatedPerson() {
		return associatedPerson;
	}

	public void setAssociatedPerson(PersonAsset associatedPerson) {
		this.associatedPerson = associatedPerson;
	}

	public HardwareAsset getAssociatedHardware() {
		return associatedHardware;
	}

	public void setAssociatedHardware(HardwareAsset associatedHardware) {
		this.associatedHardware = associatedHardware;
	}

	public LocationAsset getAssociatedLocation() {
		return associatedLocation;
	}

	public void setAssociatedLocation(LocationAsset associatedLocation) {
		this.associatedLocation = associatedLocation;
	}
}