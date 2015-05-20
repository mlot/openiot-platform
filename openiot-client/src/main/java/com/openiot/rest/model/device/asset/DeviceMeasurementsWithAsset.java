/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.asset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.openiot.rest.model.device.event.DeviceMeasurements;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.asset.IAssetModuleManager;
import com.openiot.spi.device.event.IDeviceMeasurements;

import java.util.Map;

/**
 * Wraps a {@link DeviceMeasurements} so that information about the asset associated with
 * its assignment is available.
 * 
 * @author Derek
 */
@JsonIgnoreProperties
@JsonInclude(Include.NON_NULL)
public class DeviceMeasurementsWithAsset extends DeviceEventWithAsset implements IDeviceMeasurements {

	public DeviceMeasurementsWithAsset(IDeviceMeasurements wrapped, IAssetModuleManager assets)
			throws OpenIoTException {
		super(wrapped, assets);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.spi.device.IMeasurementsProvider#addOrReplaceMeasurement(java.lang
	 * .String, java.lang.Double)
	 */
	@Override
	public void addOrReplaceMeasurement(String name, Double value) {
		((IDeviceMeasurements) getWrapped()).addOrReplaceMeasurement(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.spi.device.IMeasurementsProvider#removeMeasurement(java.lang.String)
	 */
	@Override
	public Double removeMeasurement(String name) {
		return ((IDeviceMeasurements) getWrapped()).removeMeasurement(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.spi.device.IMeasurementsProvider#getMeasurement(java.lang.String)
	 */
	@Override
	public Double getMeasurement(String name) {
		return ((IDeviceMeasurements) getWrapped()).getMeasurement(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IMeasurementsProvider#getMeasurements()
	 */
	@Override
	public Map<String, Double> getMeasurements() {
		return ((IDeviceMeasurements) getWrapped()).getMeasurements();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IMeasurementsProvider#clearMeasurements()
	 */
	@Override
	public void clearMeasurements() {
		((IDeviceMeasurements) getWrapped()).clearMeasurements();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IDeviceMeasurements#getMeasurementsSummary()
	 */
	public String getMeasurementsSummary() {
		String result = "";
		boolean isFirst = true;
		for (String key : getMeasurements().keySet()) {
			if (!isFirst) {
				result += ", ";
			} else {
				isFirst = false;
			}
			result += key + ": " + getMeasurement(key);
		}
		return result;
	}

	/**
	 * For Jackson marshalling.
	 * 
	 * @param value
	 */
	public void setMeasurementsSummary(String value) {
	}
}