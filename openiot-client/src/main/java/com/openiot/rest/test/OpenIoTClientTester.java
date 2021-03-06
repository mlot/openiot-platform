/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.test;

import com.openiot.rest.client.OpenIoTClient;
import com.openiot.rest.model.device.event.request.DeviceAlertCreateRequest;
import com.openiot.rest.model.device.event.request.DeviceLocationCreateRequest;
import com.openiot.rest.model.device.event.request.DeviceMeasurementsCreateRequest;
import com.openiot.rest.test.OpenIoTClientTester.TestResults;
import com.openiot.spi.IOpenIoTClient;
import com.openiot.spi.device.event.AlertLevel;

import java.util.Date;
import java.util.concurrent.Callable;

/**
 * Used to test performance of repeated calls to the OpenIoT REST services. Randomly
 * creates a given number of events for a given device assignment.
 * 
 * @author Derek
 */
public class OpenIoTClientTester implements Callable<TestResults> {

	/** Token for assignment to receive events */
	private String assignmentToken;

	/** Number of events to generate */
	private int eventCount;

	/** Indicates whether assignment state should be updated by event */
	private boolean updateState;

	public OpenIoTClientTester(String assignmentToken, int eventCount, boolean updateState) {
		this.assignmentToken = assignmentToken;
		this.eventCount = eventCount;
		this.updateState = updateState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public TestResults call() throws Exception {
		IOpenIoTClient client =
				new OpenIoTClient("http://sw-swarm-master.cloudapp.net:8080/sitewhere/api/", "admin",
						"password");
		for (int i = 0; i < eventCount; i++) {
			int random = (int) Math.floor(Math.random() * 3);
			if (random == 0) {
				DeviceAlertCreateRequest request = new DeviceAlertCreateRequest();
				request.setEventDate(new Date());
				request.setType("test.error");
				request.setLevel(AlertLevel.Error);
				request.setMessage("This is a test alert message.");
				request.setUpdateState(updateState);
				client.createDeviceAlert(getAssignmentToken(), request);
			} else if (random == 1) {
				DeviceLocationCreateRequest request = new DeviceLocationCreateRequest();
				request.setEventDate(new Date());
				request.setLatitude(33.7550);
				request.setLongitude(-84.3900);
				request.setElevation(1000.0);
				request.setUpdateState(updateState);
				client.createDeviceLocation(getAssignmentToken(), request);
			} else if (random == 2) {
				DeviceMeasurementsCreateRequest request = new DeviceMeasurementsCreateRequest();
				request.setEventDate(new Date());
				request.addOrReplaceMeasurement("first", 123.45);
				request.addOrReplaceMeasurement("second", 987.65);
				request.setUpdateState(updateState);
				client.createDeviceMeasurements(getAssignmentToken(), request);
			}
		}
		return new TestResults();
	}

	public String getAssignmentToken() {
		return assignmentToken;
	}

	public void setAssignmentToken(String assignmentToken) {
		this.assignmentToken = assignmentToken;
	}

	public int getEventCount() {
		return eventCount;
	}

	public void setEventCount(int eventCount) {
		this.eventCount = eventCount;
	}

	/**
	 * Holds results from client test.
	 * 
	 * @author Derek
	 */
	public static class TestResults {
	}
}