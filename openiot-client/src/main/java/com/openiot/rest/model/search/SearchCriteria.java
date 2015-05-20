/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.search;

import com.openiot.spi.search.ISearchCriteria;

/**
 * Common criteria used in searches that return a list of results. Includes parameters for
 * paging of results.
 * 
 * @author Derek
 */
public class SearchCriteria implements ISearchCriteria {

	/** Page number to view */
	private int pageNumber;

	/** Number of records in a page of results */
	private int pageSize;

	public SearchCriteria(int pageNumber, int pageSize) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
}