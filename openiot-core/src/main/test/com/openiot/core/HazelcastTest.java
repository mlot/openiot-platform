/*
 * Copyright (c) OpenIoT, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.core;

import org.junit.Before;
import org.junit.Test;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.openiot.rest.model.device.event.DeviceLocation;
import com.openiot.spi.server.hazelcast.IOpenIoTHazelcast;

/**
 * Used to test various aspects of Hazelcast connectivity.
 * 
 * @author Derek
 */
public class HazelcastTest {

	/** OpenIoT Hazelcast username */
	private static final String SITEWHERE_USERNAME = "sitewhere";

	/** OpenIoT Hazelcast password */
	private static final String SITEWHERE_PASSWORD = "sitewhere";

	/** Hazelcast client for OpenIoT */
	private HazelcastInstance hazelcast;

	@Before
	public void setup() throws Exception {

		try {
	        Config config = new Config();
	        config.getGroupConfig().setName(SITEWHERE_USERNAME);
	        config.getGroupConfig().setPassword(SITEWHERE_PASSWORD);
	        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
	        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
			hazelcast = Hazelcast.newHazelcastInstance(config);
		} catch (Exception e) {
			throw new Exception("Unable to connect to OpenIoT Hazelcast cluster.", e);
		}
	}

	@Test
	public void test() {
		ITopic<DeviceLocation> locationsTopic = hazelcast.getTopic(IOpenIoTHazelcast.TOPIC_LOCATION_ADDED);
		locationsTopic.getName();
		try {
			Thread.sleep(120000);
		} catch (InterruptedException e) {
		}
	}
}