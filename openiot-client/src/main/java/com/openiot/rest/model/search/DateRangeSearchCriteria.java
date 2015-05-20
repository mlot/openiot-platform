/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.search;

import com.openiot.spi.search.IDateRangeSearchCriteria;

import java.util.Date;

/**
 * Extends search criteria with ability to specify a date range.
 * 
 * @author Derek
 */
public class DateRangeSearchCriteria extends SearchCriteria implements IDateRangeSearchCriteria {

	/** Start date for search */
	private Date startDate;

	/** End date for search */
	private Date endDate;

	public DateRangeSearchCriteria(int pageNumber, int pageSize, Date startDate, Date endDate) {
		super(pageNumber, pageSize);
		this.startDate = startDate;
		this.endDate = endDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.common.IDateRangeSearchCriteria#getStartDate()
	 */
	public Date getStartDate() {
		return startDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.spi.common.IDateRangeSearchCriteria#getEndDate()
	 */
	public Date getEndDate() {
		return endDate;
	}
}