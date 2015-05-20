/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning.mqtt;

import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.provisioning.IInboundEventReceiver;
import com.openiot.spi.device.provisioning.IInboundEventSource;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;
import org.fusesource.mqtt.client.*;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of {@link IInboundEventReceiver} that subscribes to an MQTT topic and
 * pulls the message contents into OpenIoT for processing.
 * 
 * @author Derek
 */
public class MqttInboundEventReceiver extends LifecycleComponent implements IInboundEventReceiver<byte[]> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(MqttInboundEventReceiver.class);

	/** Default hostname if not set via Spring */
	public static final String DEFAULT_HOSTNAME = "localhost";

	/** Default port if not set from Spring */
	public static final int DEFAULT_PORT = 1883;

	/** Default subscribed topic name */
	public static final String DEFAULT_TOPIC = "OpenIoT/input/protobuf";

	/** Parent event source */
	private IInboundEventSource<byte[]> eventSource;

	/** Host name */
	private String hostname = DEFAULT_HOSTNAME;

	/** Port */
	private int port = DEFAULT_PORT;

	/** Topic name */
	private String topic = DEFAULT_TOPIC;

	/** MQTT client */
	private MQTT mqtt;

	/** Shared MQTT connection */
	private BlockingConnection connection;

	/** Used to execute MQTT subscribe in separate thread */
	private ExecutorService executor = Executors.newSingleThreadExecutor(new SubscribersThreadFactory());

	public MqttInboundEventReceiver() {
		super(LifecycleComponentType.InboundEventReceiver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		this.mqtt = new MQTT();
		try {
			mqtt.setHost(getHostname(), getPort());
		} catch (URISyntaxException e) {
			throw new OpenIoTException("Invalid hostname for MQTT server.", e);
		}
		LOGGER.info("Receiver connecting to MQTT broker at '" + getHostname() + ":" + getPort() + "'...");
		connection = mqtt.blockingConnection();
		try {
			connection.connect();
		} catch (Exception e) {
			throw new OpenIoTException("Unable to establish MQTT connection.", e);
		}
		LOGGER.info("Receiver connected to MQTT broker.");

		// Subscribe to chosen topic.
		Topic[] topics = { new Topic(getTopic(), QoS.AT_LEAST_ONCE) };
		try {
			connection.subscribe(topics);
			LOGGER.info("Subscribed to events on MQTT topic: " + getTopic());
		} catch (Exception e) {
			throw new OpenIoTException("Exception while attempting to subscribe to MQTT topic: "
					+ getTopic(), e);
		}

		// Handle message processing in separate thread.
		executor.execute(new MqttSubscriptionProcessor());
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
	 * @see IInboundEventReceiver#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return getHostname() + ":" + getPort() + "/" + getTopic();
	}

	/** Used for naming consumer threads */
	private class SubscribersThreadFactory implements ThreadFactory {

		/** Counts threads */
		private AtomicInteger counter = new AtomicInteger();

		public Thread newThread(Runnable r) {
			return new Thread(r, "OpenIoT MQTT(" + getEventSource().getSourceId() + " - " + getTopic()
					+ ") Receiver " + counter.incrementAndGet());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IInboundEventReceiver#onEventPayloadReceived
	 * (java.lang.Object)
	 */
	@Override
	public void onEventPayloadReceived(byte[] payload, Map context) {
		getEventSource().onEncodedEventReceived(MqttInboundEventReceiver.this, payload, context);
	}

	/**
	 * Pulls messages from the MQTT topic and puts them on the queue for this receiver.
	 * 
	 * @author Derek
	 */
	private class MqttSubscriptionProcessor implements Runnable {

		@Override
		public void run() {
			LOGGER.info("Started MQTT subscription processing thread.");
			while (true) {
				try {
					Message message = connection.receive();
					message.ack();
					onEventPayloadReceived(message.getPayload(), new HashMap());
				} catch (InterruptedException e) {
					break;
				} catch (Throwable e) {
					LOGGER.error(e);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
		executor.shutdownNow();
		try {
			connection.disconnect();
			connection.kill();
		} catch (Exception e) {
			LOGGER.error("Error shutting down MQTT device event receiver.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IInboundEventReceiver#getEventSource()
	 */
	public IInboundEventSource<byte[]> getEventSource() {
		return eventSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IInboundEventReceiver#setEventSource(com.
	 * sitewhere.spi.device.provisioning.IInboundEventSource)
	 */
	public void setEventSource(IInboundEventSource<byte[]> eventSource) {
		this.eventSource = eventSource;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
}