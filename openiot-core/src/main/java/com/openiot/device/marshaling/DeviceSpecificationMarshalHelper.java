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
import com.openiot.rest.model.common.MetadataProviderEntity;
import com.openiot.rest.model.device.DeviceSpecification;
import com.openiot.rest.model.device.element.DeviceElementSchema;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.asset.IAssetModuleManager;
import com.openiot.spi.device.IDeviceSpecification;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import org.apache.log4j.Logger;

/**
 * Configurable helper class that allows {@link DeviceSpecification} model objects to be
 * created from {@link IDeviceSpecification} SPI objects.
 * 
 * @author dadams
 */
public class DeviceSpecificationMarshalHelper {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(DeviceSpecificationMarshalHelper.class);

	/** Indicates whether device specification asset information is to be included */
	private boolean includeAsset = true;

	/**
	 * Convert a device specification for marshaling.
	 * 
	 * @param source
	 * @param manager
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public DeviceSpecification convert(IDeviceSpecification source, IAssetModuleManager manager)
			throws OpenIoTException {
		DeviceSpecification spec = new DeviceSpecification();
		MetadataProviderEntity.copy(source, spec);
		spec.setToken(source.getToken());
		spec.setName(source.getName());
		HardwareAsset asset =
				(HardwareAsset) OpenIoT.getServer().getAssetModuleManager().getAssetById(
						source.getAssetModuleId(), source.getAssetId());
		if (asset == null) {
			LOGGER.warn("Device specification has reference to non-existent asset.");
			throw new OpenIoTSystemException(ErrorCode.InvalidAssetReferenceId, ErrorLevel.ERROR);
		}
		spec.setAssetModuleId(source.getAssetModuleId());
		spec.setAssetId(asset.getId());
		spec.setAssetName(asset.getName());
		spec.setAssetImageUrl(asset.getImageUrl());
		if (isIncludeAsset()) {
			spec.setAsset(asset);
		}
		spec.setContainerPolicy(source.getContainerPolicy());
		spec.setDeviceElementSchema((DeviceElementSchema) source.getDeviceElementSchema());
		return spec;
	}

	public boolean isIncludeAsset() {
		return includeAsset;
	}

	public DeviceSpecificationMarshalHelper setIncludeAsset(boolean includeAsset) {
		this.includeAsset = includeAsset;
		return this;
	}
}