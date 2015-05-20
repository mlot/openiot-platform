/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.marshaling;

import com.openiot.OpenIoT;
import com.openiot.rest.model.asset.HardwareAsset;
import com.openiot.rest.model.asset.LocationAsset;
import com.openiot.rest.model.asset.PersonAsset;
import com.openiot.rest.model.common.MetadataProviderEntity;
import com.openiot.rest.model.device.DeviceAssignment;
import com.openiot.rest.model.device.DeviceAssignmentState;
import com.openiot.rest.model.device.Site;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.asset.IAsset;
import com.openiot.spi.asset.IAssetModuleManager;
import com.openiot.spi.device.DeviceAssignmentType;
import com.openiot.spi.device.IDevice;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.ISite;
import org.apache.log4j.Logger;

/**
 * Configurable helper class that allows DeviceAssignment model objects to be created from
 * IDeviceAssignment SPI objects.
 * 
 * @author dadams
 */
public class DeviceAssignmentMarshalHelper {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(DeviceAssignmentMarshalHelper.class);

	/** Indicates whether device asset information is to be included */
	private boolean includeAsset = true;

	/** Indicates whether to include device information */
	private boolean includeDevice = false;

	/** Indicates whether to include site information */
	private boolean includeSite = false;

	/** Used to control marshaling of devices */
	private DeviceMarshalHelper deviceHelper;

	/**
	 * Convert the SPI object into a model object for marshaling.
	 * 
	 * @param source
	 * @param manager
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public DeviceAssignment convert(IDeviceAssignment source, IAssetModuleManager manager)
			throws OpenIoTException {
		DeviceAssignment result = new DeviceAssignment();
		result.setToken(source.getToken());
		result.setActiveDate(source.getActiveDate());
		result.setReleasedDate(source.getReleasedDate());
		result.setStatus(source.getStatus());
		result.setAssignmentType(source.getAssignmentType());
		result.setAssetModuleId(source.getAssetModuleId());
		result.setAssetId(source.getAssetId());
		MetadataProviderEntity.copy(source, result);
		if (source.getState() != null) {
			result.setState(DeviceAssignmentState.copy(source.getState()));
		}
		if (source.getAssignmentType() != DeviceAssignmentType.Unassociated) {
			IAsset asset = manager.getAssetById(source.getAssetModuleId(), source.getAssetId());
			if (isIncludeAsset() || (asset == null)) {
				if (asset instanceof HardwareAsset) {
					result.setAssociatedHardware((HardwareAsset) asset);
				} else if (asset instanceof PersonAsset) {
					result.setAssociatedPerson((PersonAsset) asset);
				} else if (asset instanceof LocationAsset) {
					result.setAssociatedLocation((LocationAsset) asset);
				}
			} else {
				result.setAssetName(asset.getName());
				result.setAssetImageUrl(asset.getImageUrl());
			}
		}
		result.setSiteToken(source.getSiteToken());
		if (isIncludeSite()) {
			ISite site = OpenIoT.getServer().getDeviceManagement().getSiteForAssignment(source);
			result.setSite(Site.copy(site));
		}
		result.setDeviceHardwareId(source.getDeviceHardwareId());
		if (isIncludeDevice()) {
			IDevice device = OpenIoT.getServer().getDeviceManagement().getDeviceForAssignment(source);
			if (device != null) {
				result.setDevice(getDeviceHelper().convert(device, manager));
			} else {
				LOGGER.error("Assignment references invalid hardware id.");
			}
		}
		return result;
	}

	/**
	 * Get the helper for marshaling device information.
	 * 
	 * @return
	 */
	protected DeviceMarshalHelper getDeviceHelper() {
		if (deviceHelper == null) {
			deviceHelper = new DeviceMarshalHelper();
			deviceHelper.setIncludeAsset(false);
			deviceHelper.setIncludeAssignment(false);
			deviceHelper.setIncludeSpecification(false);
		}
		return deviceHelper;
	}

	public boolean isIncludeAsset() {
		return includeAsset;
	}

	public DeviceAssignmentMarshalHelper setIncludeAsset(boolean includeAsset) {
		this.includeAsset = includeAsset;
		return this;
	}

	public boolean isIncludeDevice() {
		return includeDevice;
	}

	public DeviceAssignmentMarshalHelper setIncludeDevice(boolean includeDevice) {
		this.includeDevice = includeDevice;
		return this;
	}

	public boolean isIncludeSite() {
		return includeSite;
	}

	public DeviceAssignmentMarshalHelper setIncludeSite(boolean includeSite) {
		this.includeSite = includeSite;
		return this;
	}
}