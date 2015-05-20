/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.asset;

import com.openiot.spi.asset.AssetType;
import com.openiot.spi.asset.IHardwareAsset;

/**
 * Model class for a hardware asset.
 * 
 * @author dadams
 */
public class HardwareAsset extends Asset implements IHardwareAsset {

	/** SKU */
	private String sku;

	/** Asset description */
	private String description;

	public HardwareAsset() {
		setType(AssetType.Hardware);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IHardwareAsset#getSku()
	 */
	@Override
	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IHardwareAsset#getDescription()
	 */
	@Override
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}