/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.test;

import com.openiot.rest.client.OpenIoTClient;
import com.openiot.rest.model.common.Location;
import com.openiot.rest.model.device.Device;
import com.openiot.rest.model.device.DeviceAssignment;
import com.openiot.rest.model.device.Zone;
import com.openiot.rest.model.device.batch.BatchOperation;
import com.openiot.rest.model.device.request.DeviceAssignmentCreateRequest;
import com.openiot.rest.model.device.request.DeviceCreateRequest;
import com.openiot.rest.model.device.request.ZoneCreateRequest;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.rest.model.system.Version;
import com.openiot.spi.IOpenIoTClient;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.DeviceAssignmentType;
import com.openiot.spi.error.ErrorCode;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test cases for client API calls.
 * 
 * @author dadams
 */
@SuppressWarnings("deprecation")
public class ApiTests {

	/** Device specification id used in tests */
	public static final String TEST_SPECIFICATION_TOKEN = "293749827342243827349";

	/** Hardware id used for test cases */
	public static final String TEST_HARDWARE_ID = "12356789-TEST-123";

	/** Asset id for testing */
	public static final String TEST_ASSET_ID = "174";

	/** Site token used in tests */
	public static final String TEST_SITE_TOKEN = "22223793-3028-4114-86ba-aefc7d05369f";

	/** Android device specification token */
	public static final String ANDROID_SPEC_TOKEN = "d2604433-e4eb-419b-97c7-88efe9b2cd41";

	/** OpenIoT client */
	private IOpenIoTClient client;

	@Before
	public void setup() {
		this.client = new OpenIoTClient();
	}

	@Test
	public void testConnectivity() throws OpenIoTException {
		OpenIoTClient client = new OpenIoTClient();
		Version version = client.getOpenIoTVersion();
		System.out.println("OpenIoT version is " + version.getVersionIdentifier() + ".");
	}

	@Test
	public void testDeviceCRUD() throws OpenIoTException {
		// Delete device if it already exists.
		try {
			client.getDeviceByHardwareId(TEST_HARDWARE_ID);
			client.deleteDevice(TEST_HARDWARE_ID, true);
		} catch (OpenIoTException e) {
			// Ignore missing device since we wanted it deleted.
		}

		// Test initial create.
		DeviceCreateRequest request = new DeviceCreateRequest();
		request.setHardwareId(TEST_HARDWARE_ID);
		request.setSpecificationToken(TEST_SPECIFICATION_TOKEN);
		request.setComments("This is a test device.");
		request.addOrReplaceMetadata("name1", "value1");
		request.addOrReplaceMetadata("name2", "value2");
		Device device = client.createDevice(request);
		Assert.assertNotNull("Device create returned null.", device);
		Assert.assertEquals("Metadata not stored properly.", 2, device.getMetadata().size());

		// Test get by hardware id.
		try {
			device = client.getDeviceByHardwareId(TEST_HARDWARE_ID);
		} catch (OpenIoTException e) {
			Assert.fail("Device should exist, but not found by handware id.");
		}

		// Test update.
		DeviceCreateRequest update = new DeviceCreateRequest();
		update.setComments("Updated.");
		update.addOrReplaceMetadata("name1", "value1");
		device = client.updateDevice(TEST_HARDWARE_ID, update);
		Assert.assertEquals("Updated.", device.getComments());
		Assert.assertEquals("Metadata not updated properly.", 1, device.getMetadata().size());
		Assert.assertNotNull("Updated date not set.", device.getUpdatedDate());

		// Should not allow hardware id to be updated.
		try {
			update = new DeviceCreateRequest();
			update.setHardwareId("xxx");
			client.updateDevice(TEST_HARDWARE_ID, update);
			Assert.fail("Device update allowed update of hardware id.");
		} catch (OpenIoTSystemException e) {
			verifyErrorCode(e, ErrorCode.DeviceHardwareIdCanNotBeChanged);
		}

		// Test duplicate.
		try {
			device = client.createDevice(request);
			Assert.fail("Create device allowed duplicate.");
		} catch (OpenIoTException e) {
			verifyErrorCode(e, ErrorCode.DuplicateHardwareId);
		}

		// Create a device assignment.
		DeviceAssignmentCreateRequest assnRequest = new DeviceAssignmentCreateRequest();
		assnRequest.setAssignmentType(DeviceAssignmentType.Associated);
		assnRequest.setAssetModuleId("testAssetModuleId");
		assnRequest.setAssetId(TEST_ASSET_ID);
		assnRequest.setDeviceHardwareId(device.getHardwareId());
		assnRequest.addOrReplaceMetadata("name1", "value1");
		assnRequest.addOrReplaceMetadata("name2", "value2");
		assnRequest.addOrReplaceMetadata("name1", "value2");
		DeviceAssignment assignment = client.createDeviceAssignment(assnRequest);
		Assert.assertNotNull("Assignment token was null.", assignment.getToken());
		Assert.assertEquals("Assignment metadata count incorrect.", 2, assignment.getMetadata().size());

		// Test get assignment by token.
		assignment = client.getDeviceAssignmentByToken(assignment.getToken());
		Assert.assertNotNull("Assignment by token returned null.", assignment);

		// Test getting current assignment for a device.
		DeviceAssignment currAssignment = client.getCurrentAssignmentForDevice(TEST_HARDWARE_ID);
		Assert.assertEquals("Current device assignment is incorrect.", assignment.getToken(),
				currAssignment.getToken());

		// Verify that an assignment can not be created for a device if one is already
		// assigned.
		try {
			assignment = client.createDeviceAssignment(assnRequest);
		} catch (OpenIoTException e) {
			verifyErrorCode(e, ErrorCode.DeviceAlreadyAssigned);
		}

		// Delete device.
		device = client.deleteDevice(TEST_HARDWARE_ID, true);
		Assert.assertNotNull(device);
	}

	@Test
	public void testCreateZone() throws OpenIoTException {
		OpenIoTClient client = new OpenIoTClient();
		ZoneCreateRequest request = new ZoneCreateRequest();
		request.setName("My Test Zone");
		List<Location> coords = new ArrayList<Location>();
		coords.add(new Location(30.0, -85.0));
		coords.add(new Location(30.0, -90.0));
		coords.add(new Location(35.0, -90.0));
		coords.add(new Location(35.0, -85.0));
		request.setCoordinates(coords);
		Zone results = client.createZone(TEST_SITE_TOKEN, request);
		System.out.println("Created zone: " + results.getName());
		SearchResults<Zone> search = client.listZonesForSite(TEST_SITE_TOKEN);
		System.out.println("Found " + search.getNumResults() + " results.");
	}

	@Test
	public void testListDevices() throws OpenIoTException {
		OpenIoTClient client = new OpenIoTClient();
		SearchResults<Device> devices = client.listDevices(false, true, true, true, 1, 100, null, null);
		System.out.println("Found " + devices.getNumResults() + " devices.");
	}

	@Test
	public void sendBatchCommandInvocation() throws OpenIoTException {
		OpenIoTClient client = new OpenIoTClient();
		List<Device> androids = getDevicesForSpecification(ANDROID_SPEC_TOKEN);
		List<String> hwids = new ArrayList<String>();
		for (Device device : androids) {
			hwids.add(device.getHardwareId());
		}
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("color", "#ff0000");
		BatchOperation op =
				client.createBatchCommandInvocation(null, "17340bb1-8673-4fc9-8ed0-4f818acedaa5", parameters,
						hwids);
		System.out.println("Created operation: " + op.getToken());
	}

	/**
	 * Get all devices for a given specification. NOTE: Logic only looks at the first 100
	 * devices.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected List<Device> getDevicesForSpecification(String token) throws OpenIoTException {
		OpenIoTClient client = new OpenIoTClient();
		SearchResults<Device> devices = client.listDevices(false, true, true, true, 1, 100, null, null);
		List<Device> results = new ArrayList<Device>();
		for (Device device : devices.getResults()) {
			if (device.getSpecificationToken().equals(token)) {
				results.add(device);
			}
		}
		return results;
	}

	/**
	 * Verifies that
	 * 
	 * @param e
	 */
	protected void verifyErrorCode(OpenIoTException e, ErrorCode code) {
		if (e instanceof OpenIoTSystemException) {
			OpenIoTSystemException sw = (OpenIoTSystemException) e;
			if (code != sw.getCode()) {
				Assert.fail("Unexpected error code returned. Expected " + code.getCode() + " but got: "
						+ sw.getCode());
			}
		} else {
			Assert.fail("Unexpected exception: " + e.getMessage());
		}
	}
}