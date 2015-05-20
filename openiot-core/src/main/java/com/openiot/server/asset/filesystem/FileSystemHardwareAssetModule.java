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
 * Module that loads a list of hardware assets from an XML file on the filesystem.
 * 
 * @author Derek Adams
 */
public class FileSystemHardwareAssetModule extends FileSystemAssetModule<HardwareAsset> implements
		IAssetModule<HardwareAsset> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(FileSystemHardwareAssetModule.class);

	/** Module id */
	public static final String MODULE_ID = "fs-hardware";

	/** Module name */
	public static final String MODULE_NAME = "Default Hardware Management";

	/** Filename in OpenIoT config folder that contains hardware assets */
	public static final String HARDWARE_CONFIG_FILENAME = "hardware-assets.xml";

	public FileSystemHardwareAssetModule() {
		super(HARDWARE_CONFIG_FILENAME, MODULE_ID, MODULE_NAME);
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
		return AssetType.Hardware;
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