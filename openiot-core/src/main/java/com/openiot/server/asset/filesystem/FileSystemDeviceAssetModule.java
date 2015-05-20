/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.server.asset.filesystem;

import com.openiot.rest.model.asset.HardwareAsset;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.asset.AssetType;
import com.openiot.spi.asset.IAssetModule;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * Module that loads a list of device assets from an XML file on the filesystem.
 * 
 * @author Derek
 */
public class FileSystemDeviceAssetModule extends FileSystemAssetModule<HardwareAsset> implements
		IAssetModule<HardwareAsset> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(FileSystemDeviceAssetModule.class);

	/** Module id */
	public static final String MODULE_ID = "fs-devices";

	/** Module name */
	public static final String MODULE_NAME = "Default Device Management";

	/** Filename in OpenIoT config folder that contains device assets */
	public static final String DEVICE_CONFIG_FILENAME = "device-assets.xml";

	public FileSystemDeviceAssetModule() {
		super(DEVICE_CONFIG_FILENAME, MODULE_ID, MODULE_NAME);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IAssetModule#isAssetTypeSupported(com.openiot.spi.asset
	 * .AssetType)
	 */
	public AssetType getAssetType() {
		return AssetType.Device;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * FileSystemAssetModule#unmarshal(java.io.File)
	 */
	@Override
	protected List<HardwareAsset> unmarshal(File file) throws OpenIoTException {
		return MarshalUtils.loadHardwareAssets(file, getAssetType());
	}
}