/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.event;

import com.openiot.spi.device.event.IMeasurementsProvider;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles creating/modifying a collection of measurements.
 * 
 * @author Derek
 */
public class MeasurementsProvider implements IMeasurementsProvider, Serializable {

	/** For Java serialization */
	private static final long serialVersionUID = 3675057693017779000L;

	/** List of measurement entries */
	private Map<String, Double> measurements = new HashMap<String, Double>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.spi.device.IMeasurementsProvider#addOrReplaceMeasurement(java.lang
	 * .String, java.lang.Double)
	 */
	@Override
	public void addOrReplaceMeasurement(String name, Double value) {
		measurements.put(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.spi.device.IMeasurementsProvider#removeMeasurement(java.lang.String)
	 */
	public Double removeMeasurement(String name) {
		return measurements.remove(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.spi.device.IMeasurementsProvider#getMeasurement(java.lang.String)
	 */
	@Override
	public Double getMeasurement(String name) {
		return measurements.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IMeasurementsProvider#getMeasurements()
	 */
	@Override
	public Map<String, Double> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(Map<String, Double> measurements) {
		this.measurements = measurements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.device.IMeasurementsProvider#clearMeasurements()
	 */
	@Override
	public void clearMeasurements() {
		measurements.clear();
	}

	/**
	 * Copy contents of one measurements provider to another.
	 * 
	 * @param source
	 * @param target
	 */
	public static void copy(IMeasurementsProvider source, MeasurementsProvider target) {
		if (source != null) {
			for (String key : source.getMeasurements().keySet()) {
				target.addOrReplaceMeasurement(key, source.getMeasurement(key));
			}
		}
	}
}