/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.group;

import com.openiot.OpenIoT;
import com.openiot.rest.model.search.SearchCriteria;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDevice;
import com.openiot.spi.device.group.IDeviceGroup;
import com.openiot.spi.device.group.IDeviceGroupElement;
import com.openiot.spi.search.ISearchCriteria;
import com.openiot.spi.search.ISearchResults;
import com.openiot.spi.search.device.IDeviceSearchCriteria;

import java.util.*;

/**
 * Utility methods for maniupulating device groups.
 * 
 * @author Derek
 */
public class DeviceGroupUtils {

	/**
	 * Get devices in a group that match the given criteria.
	 * 
	 * @param groupToken
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static List<IDevice> getDevicesInGroup(String groupToken, IDeviceSearchCriteria criteria)
			throws OpenIoTException {
		Collection<IDevice> devices = getDevicesInGroup(groupToken);
		List<IDevice> matches = new ArrayList<IDevice>();
		for (IDevice device : devices) {
			switch (criteria.getSearchType()) {
			case All: {
				break;
			}
			case UsesSpecification: {
				if (!device.getSpecificationToken().equals(
						criteria.getDeviceBySpecificationParameters().getSpecificationToken())) {
					continue;
				}
			}
			}
			if (criteria.isExcludeAssigned() && (device.getAssignmentToken() != null)) {
				continue;
			}
			if ((criteria.getStartDate() != null)
					&& (device.getCreatedDate().before(criteria.getStartDate()))) {
				continue;
			}
			if ((criteria.getEndDate() != null) && (device.getCreatedDate().after(criteria.getEndDate()))) {
				continue;
			}
			matches.add(device);
		}
		return matches;
	}

	/**
	 * Get the list of unique devices in a group. (Recurses into subgroups and removes
	 * duplicates)
	 * 
	 * @param groupToken
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static Collection<IDevice> getDevicesInGroup(String groupToken) throws OpenIoTException {
		Map<String, IDevice> devices = new HashMap<String, IDevice>();
		ISearchCriteria criteria = new SearchCriteria(1, 0);
		SearchResults<IDeviceGroupElement> elements =
				OpenIoT.getServer().getDeviceManagement().listDeviceGroupElements(groupToken, criteria);
		for (IDeviceGroupElement element : elements.getResults()) {
			switch (element.getType()) {
			case Device: {
				devices.put(
						element.getElementId(),
						OpenIoT.getServer().getDeviceManagement().getDeviceByHardwareId(
								element.getElementId()));
				break;
			}
			case Group: {
				Collection<IDevice> subDevices = getDevicesInGroup(element.getElementId());
				for (IDevice subDevice : subDevices) {
					devices.put(subDevice.getHardwareId(), subDevice);
				}
				break;
			}
			}
		}
		return devices.values();
	}

	/**
	 * Gets devices in all groups that have the given role. Duplicates are removed.
	 * 
	 * @param groupRole
	 * @param criteria
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static Collection<IDevice> getDevicesInGroupsWithRole(String groupRole,
			IDeviceSearchCriteria criteria) throws OpenIoTException {
		Map<String, IDevice> devices = new HashMap<String, IDevice>();
		ISearchCriteria groupCriteria = new SearchCriteria(1, 0);
		ISearchResults<IDeviceGroup> groups =
				OpenIoT.getServer().getDeviceManagement().listDeviceGroupsWithRole(groupRole, false,
						groupCriteria);
		for (IDeviceGroup group : groups.getResults()) {
			List<IDevice> groupDevices = getDevicesInGroup(group.getToken(), criteria);
			for (IDevice groupDevice : groupDevices) {
				devices.put(groupDevice.getHardwareId(), groupDevice);
			}
		}
		return devices.values();
	}
}