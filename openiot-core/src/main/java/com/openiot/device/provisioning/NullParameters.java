/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.IDeviceNestingContext;
import com.openiot.spi.device.command.IDeviceCommandExecution;
import com.openiot.spi.device.provisioning.ICommandDeliveryParameterExtractor;
import com.openiot.spi.device.provisioning.ICommandDeliveryProvider;

/**
 * Placeholder object for {@link ICommandDeliveryProvider} that do not require parameters.
 * 
 * @author Derek
 */
public class NullParameters {

	/**
	 * Implementation of {@link ICommandDeliveryParameterExtractor} that returns
	 * {@link NullParameters}.
	 * 
	 * @author Derek
	 */
	public static class Extractor implements ICommandDeliveryParameterExtractor<NullParameters> {

		/** Value to be returned */
		private NullParameters parameters = new NullParameters();

		/*
		 * (non-Javadoc)
		 * 
		 * @see ICommandDeliveryParameterExtractor#
		 * extractDeliveryParameters(IDeviceNestingContext,
		 * IDeviceAssignment,
		 * IDeviceCommandExecution)
		 */
		@Override
		public NullParameters extractDeliveryParameters(IDeviceNestingContext nesting,
				IDeviceAssignment assignment, IDeviceCommandExecution execution) throws OpenIoTException {
			return parameters;
		}
	}
}