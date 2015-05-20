/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.geospatial;

import com.openiot.OpenIoT;
import com.openiot.device.event.processor.OutboundEventProcessor;
import com.openiot.rest.model.device.event.request.DeviceAlertCreateRequest;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IZone;
import com.openiot.spi.device.event.IDeviceLocation;
import com.openiot.spi.device.event.processor.IOutboundEventProcessor;
import com.openiot.spi.geospatial.ZoneContainment;
import com.vividsolutions.jts.geom.Polygon;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Implementation of {@link IOutboundEventProcessor} that performs a series of tests for
 * whether a location is inside or outside of zones, firing alerts if the criteria is met.
 * 
 * @author Derek
 */
public class ZoneTestEventProcessor extends OutboundEventProcessor {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(ZoneTestEventProcessor.class);

	/** Map of polygons by zone token */
	private Map<String, Polygon> zoneMap = new HashMap<String, Polygon>();

	/** List of tests to perform */
	private List<ZoneTest> zoneTests = new ArrayList<ZoneTest>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		LOGGER.info("Starting zone test processor with " + zoneTests.size() + " tests.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#getLogger()
	 */
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.rest.model.device.event.processor.OutboundEventProcessor#onLocation
	 * (IDeviceLocation)
	 */
	@Override
	public void onLocation(IDeviceLocation location) throws OpenIoTException {
		for (ZoneTest test : zoneTests) {
			Polygon poly = getZonePolygon(test.getZoneToken());
			ZoneContainment containment =
					(poly.contains(GeoUtils.createPointForLocation(location))) ? ZoneContainment.Inside
							: ZoneContainment.Outside;
			if (test.getCondition() == containment) {
				DeviceAlertCreateRequest alert = new DeviceAlertCreateRequest();
				alert.setType(test.getAlertType());
				alert.setLevel(test.getAlertLevel());
				alert.setMessage(test.getAlertMessage());
				alert.setUpdateState(false);
				alert.setEventDate(new Date());
				OpenIoT.getServer().getDeviceManagement().addDeviceAlert(
						location.getDeviceAssignmentToken(), alert);
			}
		}
	}

	/**
	 * Get cached zone polygon or try to load from datastore.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected Polygon getZonePolygon(String token) throws OpenIoTException {
		Polygon poly = zoneMap.get(token);
		if (poly != null) {
			return poly;
		}
		IZone zone = OpenIoT.getServer().getDeviceManagement().getZone(token);
		if (zone != null) {
			poly = GeoUtils.createPolygonForZone(zone);
			zoneMap.put(token, poly);
			return poly;
		}
		throw new OpenIoTException("Invalid zone token in " + ZoneTestEventProcessor.class.getName() + ": "
				+ token);
	}

	public List<ZoneTest> getZoneTests() {
		return zoneTests;
	}

	public void setZoneTests(List<ZoneTest> zoneTests) {
		this.zoneTests = zoneTests;
	}
}