/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.charting;

import com.openiot.spi.device.charting.IChartEntry;
import com.openiot.spi.device.charting.IChartSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * Chart series implementation.
 * 
 * @author Derek
 */
public class ChartSeries<T> implements IChartSeries<T> {

	/** Measurement id */
	private String measurementId;

	/** Entries for the chart series */
	private List<IChartEntry<T>> entries = new ArrayList<IChartEntry<T>>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see IChartSeries#getMeasurementId()
	 */
	@Override
	public String getMeasurementId() {
		return measurementId;
	}

	public void setMeasurementId(String measurementId) {
		this.measurementId = measurementId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IChartSeries#getEntries()
	 */
	@Override
	public List<IChartEntry<T>> getEntries() {
		return entries;
	}

	public void setEntries(List<IChartEntry<T>> entries) {
		this.entries = entries;
	}
}