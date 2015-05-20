/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.server.asset.filesystem;

import com.openiot.rest.model.asset.LocationAsset;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.asset.AssetType;
import com.openiot.spi.asset.IAssetModule;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * Module that loads a list of location assets from an XML file on the filesystem.
 * 
 * @author Derek Adams
 */
public class FileSystemLocationAssetModule extends FileSystemAssetModule<LocationAsset> implements
		IAssetModule<LocationAsset> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(FileSystemLocationAssetModule.class);

	/** Module id */
	public static final String MODULE_ID = "fs-locations";

	/** Module name */
	public static final String MODULE_NAME = "Default Location Management";

	/** Filename in OpenIoT config folder that contains location assets */
	public static final String CONFIG_FILENAME = "location-assets.xml";

	public FileSystemLocationAssetModule() {
		super(CONFIG_FILENAME, MODULE_ID, MODULE_NAME);
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
	 * @see IAssetModule#getAssetType()
	 */
	public AssetType getAssetType() {
		return AssetType.Location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * FileSystemAssetModule#unmarshal(java.io.File)
	 */
	@Override
	protected List<LocationAsset> unmarshal(File file) throws OpenIoTException {
		return MarshalUtils.loadLocationAssets(file);
	}
}
