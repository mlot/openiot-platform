/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.asset;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.openiot.rest.model.datatype.JsonDateSerializer;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.asset.IAsset;
import com.openiot.spi.asset.IAssetModuleManager;
import com.openiot.spi.device.DeviceAssignmentType;
import com.openiot.spi.device.asset.IDeviceEventWithAsset;
import com.openiot.spi.device.event.DeviceEventType;
import com.openiot.spi.device.event.IDeviceEvent;

import java.util.Date;
import java.util.Map;

/**
 * Wraps a device event and provides extra information the associated asset from its
 * assignment.
 * 
 * @author Derek
 */
public class DeviceEventWithAsset implements IDeviceEventWithAsset {

	/** Text shown when an asset is not assigned */
	public static final String UNASSOCIATED_ASSET_NAME = "Unassociated";

	/** Wrapped event */
	protected IDeviceEvent wrapped;

	/** Associated asset */
	protected IAsset asset;

	public DeviceEventWithAsset(IDeviceEvent wrapped, IAssetModuleManager assets) throws OpenIoTException {
		this.wrapped = wrapped;
		if (wrapped.getAssignmentType() == DeviceAssignmentType.Associated) {
			this.asset = assets.getAssetById(wrapped.getAssetModuleId(), wrapped.getAssetId());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceEventWithAsset#getAssetName()
	 */
	@Override
	public String getAssetName() {
		if (asset != null) {
			return asset.getName();
		}
		return UNASSOCIATED_ASSET_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IMetadataProvider#addOrReplaceMetadata(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void addOrReplaceMetadata(String name, String value) throws OpenIoTException {
		getWrapped().addOrReplaceMetadata(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMetadataProvider#removeMetadata(java.lang.String)
	 */
	@Override
	public String removeMetadata(String name) {
		return getWrapped().removeMetadata(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMetadataProvider#getMetadata(java.lang.String)
	 */
	@Override
	public String getMetadata(String name) {
		return getWrapped().getMetadata(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMetadataProvider#getMetadata()
	 */
	@Override
	public Map<String, String> getMetadata() {
		return getWrapped().getMetadata();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMetadataProvider#clearMetadata()
	 */
	@Override
	public void clearMetadata() {
		getWrapped().clearMetadata();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IDeviceEvent o) {
		return getWrapped().compareTo(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IDeviceEvent#getId()
	 */
	@Override
	public String getId() {
		return getWrapped().getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceEvent#getEventType()
	 */
	@Override
	public DeviceEventType getEventType() {
		return getWrapped().getEventType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IDeviceEvent#getSiteToken()
	 */
	@Override
	public String getSiteToken() {
		return getWrapped().getSiteToken();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IDeviceEvent#getDeviceAssignmentToken()
	 */
	@Override
	public String getDeviceAssignmentToken() {
		return getWrapped().getDeviceAssignmentToken();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IDeviceEvent#getAssignmentType()
	 */
	@Override
	public DeviceAssignmentType getAssignmentType() {
		return getWrapped().getAssignmentType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceEvent#getAssetModuleId()
	 */
	@Override
	public String getAssetModuleId() {
		return getWrapped().getAssetModuleId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IDeviceEvent#getAssetId()
	 */
	@Override
	public String getAssetId() {
		return getWrapped().getAssetId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IDeviceEvent#getEventDate()
	 */
	@Override
	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getEventDate() {
		return getWrapped().getEventDate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IDeviceEvent#getReceivedDate()
	 */
	@Override
	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getReceivedDate() {
		return getWrapped().getReceivedDate();
	}

	protected IDeviceEvent getWrapped() {
		return wrapped;
	}
}