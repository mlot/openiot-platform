/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device.charting;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.openiot.rest.model.datatype.JsonDateSerializer;
import com.openiot.spi.device.charting.IChartEntry;

import java.util.Date;

/**
 * Chart entry implementation.
 * 
 * @author Derek
 */
public class ChartEntry<T> implements IChartEntry<T> {

	/** Entry value */
	private T value;

	/** Date of measurement */
	private Date measurementDate;

	/*
	 * (non-Javadoc)
	 * 
	 * @see IChartEntry#getValue()
	 */
	@Override
	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IChartEntry#getMeasurementDate()
	 */
	@Override
	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getMeasurementDate() {
		return measurementDate;
	}

	public void setMeasurementDate(Date measurementDate) {
		this.measurementDate = measurementDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IChartEntry<T> other) {
		return this.getMeasurementDate().compareTo(other.getMeasurementDate());
	}
}