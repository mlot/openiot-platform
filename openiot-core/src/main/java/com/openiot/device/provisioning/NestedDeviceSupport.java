/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning;

import com.openiot.OpenIoT;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDevice;
import com.openiot.spi.device.IDeviceElementMapping;
import com.openiot.spi.device.IDeviceNestingContext;
import com.openiot.spi.device.util.DeviceUtils;

/**
 * Provides support logic for handling interactions with nested devices.
 * 
 * @author Derek
 */
public class NestedDeviceSupport {

	/**
	 * Perform common logic for locating device nesting information.
	 * 
	 * @param target
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static NestedDeviceInformation calculateNestedDeviceInformation(IDevice target)
			throws OpenIoTException {
		NestedDeviceInformation nested = new NestedDeviceInformation();

		// No parent set. Treat target device as gateway.
		if (target.getParentHardwareId() == null) {
			nested.setGateway(target);
			return nested;
		}

		// Resolve parent and verify it exists.
		IDevice parent =
				OpenIoT.getServer().getDeviceManagement().getDeviceByHardwareId(
						target.getParentHardwareId());
		if (parent == null) {
			throw new OpenIoTException("Parent device reference points to device that does not exist.");
		}

		// Parent should contain a mapping entry for the target device.
		IDeviceElementMapping mapping = DeviceUtils.findMappingFor(parent, target.getHardwareId());

		// Fall back to target as gateway if no mapping exists. This should not happen.
		if (mapping == null) {
			nested.setGateway(target);
			return nested;
		}

		nested.setGateway(parent);
		nested.setNested(target);
		nested.setPath(mapping.getDeviceElementSchemaPath());
		return nested;
	}

	/**
	 * Holds fields passed for addressing nested devices via a gateway.
	 * 
	 * @author Derek
	 */
	public static class NestedDeviceInformation implements IDeviceNestingContext {

		/** Primary hardware id */
		private IDevice gateway;

		/** Nested hardware id */
		private IDevice nested;

		/** Path to nested device */
		private String path;

		/*
		 * (non-Javadoc)
		 * 
		 * @see IDeviceNestingContext#getGateway()
		 */
		public IDevice getGateway() {
			return gateway;
		}

		public void setGateway(IDevice gateway) {
			this.gateway = gateway;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see IDeviceNestingContext#getNested()
		 */
		public IDevice getNested() {
			return nested;
		}

		public void setNested(IDevice nested) {
			this.nested = nested;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see IDeviceNestingContext#getPath()
		 */
		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}
	}
}