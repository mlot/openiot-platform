/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.device.util;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.IDeviceSpecification;
import com.openiot.spi.device.element.IDeviceElement;
import com.openiot.spi.device.element.IDeviceSlot;
import com.openiot.spi.device.element.IDeviceUnit;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * Helper methods for common operations on {@link IDeviceSpecification} objects.
 * 
 * @author Derek
 */
public class DeviceSpecificationUtils {

	/**
	 * Get an {@link IDeviceSlot} given relative path in {@link com.openiot.spi.device.element.IDeviceElementSchema}.
	 * 
	 * @param specification
	 * @param path
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static IDeviceSlot getDeviceSlotByPath(IDeviceSpecification specification, String path)
			throws OpenIoTException {
		IDeviceElement match = getDeviceElementByPath(specification, path);
		if ((match == null) || (!(match instanceof IDeviceSlot))) {
			throw new OpenIoTSystemException(ErrorCode.InvalidDeviceSlotPath, ErrorLevel.ERROR);
		}
		return (IDeviceSlot) match;
	}

	/**
	 * Get an {@link IDeviceElement} located at the given path. Return null if not found.
	 * 
	 * @param specification
	 * @param path
	 * @return
	 */
	public static IDeviceElement getDeviceElementByPath(IDeviceSpecification specification, String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		String[] segarray = path.split("[/]");
		Queue<String> segments = new ArrayDeque<String>(Arrays.asList(segarray));
		IDeviceUnit unit = specification.getDeviceElementSchema();
		if (unit == null) {
			return null;
		}
		while (segments.size() > 0) {
			String segment = segments.poll();
			if (segments.size() > 0) {
				for (IDeviceUnit nested : unit.getDeviceUnits()) {
					if (nested.getPath().equals(segment)) {
						unit = nested;
						break;
					}
				}
			} else {
				for (IDeviceSlot slot : unit.getDeviceSlots()) {
					if (slot.getPath().equals(segment)) {
						return slot;
					}
				}
				for (IDeviceUnit nested : unit.getDeviceUnits()) {
					if (nested.getPath().equals(segment)) {
						return nested;
					}
				}
				return null;
			}
		}
		return null;
	}
}