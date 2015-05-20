/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.batch;

import com.openiot.OpenIoT;
import com.openiot.device.group.DeviceGroupUtils;
import com.openiot.rest.model.search.device.DeviceSearchCriteria;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.IDevice;
import com.openiot.spi.device.request.IBatchCommandForCriteriaRequest;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import com.openiot.spi.search.device.IDeviceSearchCriteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utility methods for batch operations.
 * 
 * @author Derek
 */
public class BatchUtils {

	/**
	 * Get hardware ids based on the given criteria.
	 * 
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static List<String> getHardwareIds(IBatchCommandForCriteriaRequest criteria)
			throws OpenIoTException {
		if (criteria.getSpecificationToken() == null) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceSpecificationToken, ErrorLevel.ERROR);
		}

		boolean hasGroup = false;
		boolean hasGroupsWithRole = false;
		if ((criteria.getGroupToken() != null) && (criteria.getGroupToken().trim().length() > 0)) {
			hasGroup = true;
		}
		if ((criteria.getGroupsWithRole() != null) && (criteria.getGroupsWithRole().trim().length() > 0)) {
			hasGroupsWithRole = true;
		}
		if (hasGroup && hasGroupsWithRole) {
			throw new OpenIoTException("Only one of groupToken or groupsWithRole may be specified.");
		}

		IDeviceSearchCriteria deviceSearch =
				DeviceSearchCriteria.createDeviceBySpecificationSearch(criteria.getSpecificationToken(), 1,
						0, criteria.getStartDate(), criteria.getEndDate(), criteria.isExcludeAssigned());

		Collection<IDevice> matches;
		if (hasGroup) {
			matches = DeviceGroupUtils.getDevicesInGroup(criteria.getGroupToken(), deviceSearch);
		} else if (hasGroupsWithRole) {
			matches = DeviceGroupUtils.getDevicesInGroupsWithRole(criteria.getGroupsWithRole(), deviceSearch);
		} else {
			matches =
					OpenIoT.getServer().getDeviceManagement().listDevices(false, deviceSearch).getResults();
		}
		List<String> hardwareIds = new ArrayList<String>();
		for (IDevice match : matches) {
			hardwareIds.add(match.getHardwareId());
		}
		return hardwareIds;
	}
}