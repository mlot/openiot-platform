/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.activemq;

import com.openiot.configuration.TomcatConfigurationResolver;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.provisioning.IInboundEventReceiver;
import com.openiot.spi.device.provisioning.IInboundEventSource;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.log4j.Logger;

import javax.jms.*;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of {@link IInboundEventReceiver} that uses an ActiveMQ broker to listen
 * on a transport for messages.
 * 
 * @author Derek
 */
public class ActiveMQInboundEventReceiver extends LifecycleComponent implements IInboundEventReceiver<byte[]> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(ActiveMQInboundEventReceiver.class);

	/** Number of consumers reading messages from the queue */
	private static final int DEFAULT_NUM_CONSUMERS = 3;

	/** Parent event source */
	private IInboundEventSource<byte[]> eventSource;

	/** ActiveMQ broker service */
	private BrokerService brokerService;

	/** Unique name of ActiveMQ broker */
	private String brokerName;

	/** URI for configuring transport */
	private String transportUri;

	/** Queue name used for inbound event data */
	private String queueName;

	/** ActiveMQ data directory */
	private String dataDirectory;

	/** Number of consumers used to read messages from the queue */
	private int numConsumers = DEFAULT_NUM_CONSUMERS;

	/** List of consumers reading messages */
	private List<Consumer> consumers = new ArrayList<Consumer>();

	/** Thread pool for consumer processing */
	private ExecutorService consumersPool;

	public ActiveMQInboundEventReceiver() {
		super(LifecycleComponentType.InboundEventReceiver);
		this.brokerService = new BrokerService();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		if (getBrokerName() == null) {
			throw new OpenIoTException("Broker name is required.");
		}
		if (getTransportUri() == null) {
			throw new OpenIoTException("Transport URI is required.");
		}
		if (getQueueName() == null) {
			throw new OpenIoTException("Queue name is required.");
		}
		if (getDataDirectory() == null) {
			File tomcatData = TomcatConfigurationResolver.getOpenIoTDataFolder();
			setDataDirectory(tomcatData.getAbsolutePath());
		}
		try {
			brokerService.setBrokerName(getBrokerName());
			TransportConnector connector = new TransportConnector();
			connector.setUri(new URI(getTransportUri()));
			brokerService.addConnector(connector);
			brokerService.setDataDirectory(getDataDirectory());
			brokerService.setUseShutdownHook(false);
			brokerService.setUseJmx(false);
			brokerService.start();
			startConsumers();
		} catch (Exception e) {
			throw new OpenIoTException("Error starting ActiveMQ inbound event receiver.", e);
		}
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
		return getTransportUri();
	}

	/**
	 * Starts consumers for reading messages into OpenIoT.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void startConsumers() throws OpenIoTException {
		consumers.clear();
		consumersPool = Executors.newFixedThreadPool(getNumConsumers(), new ConsumersThreadFactory());
		for (int i = 0; i < getNumConsumers(); i++) {
			Consumer consumer = new Consumer();
			consumer.start();
			consumersPool.execute(consumer);
			consumers.add(consumer);
		}
		LOGGER.info("Created " + consumers.size() + " consumers for processing ActiveMQ messages.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
		if (brokerService != null) {
			try {
				brokerService.stop();
			} catch (Exception e) {
				throw new OpenIoTException("Error stopping ActiveMQ broker.", e);
			}
		}
		stopConsumers();
	}

	/**
	 * Stops all consumers.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void stopConsumers() throws OpenIoTException {
		consumersPool.shutdownNow();
		for (Consumer consumer : consumers) {
			consumer.stop();
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
		getEventSource().onEncodedEventReceived(ActiveMQInboundEventReceiver.this, payload, context);
	}

	/** Used for naming consumer threads */
	private class ConsumersThreadFactory implements ThreadFactory {

		/** Counts threads */
		private AtomicInteger counter = new AtomicInteger();

		public Thread newThread(Runnable r) {
			return new Thread(r, "OpenIoT ActiveMQ(" + getBrokerName() + ") Consumer "
					+ counter.incrementAndGet());
		}
	}

	/**
	 * Reads messages from the ActiveMQ queue and puts the binary content on a queue for
	 * OpenIoT to use.
	 * 
	 * @author Derek
	 */
	private class Consumer implements Runnable, ExceptionListener {

		/** Connection to ActiveMQ */
		private Connection connection;

		/** JMS session */
		private Session session;

		/** Consumer for reading data */
		private MessageConsumer consumer;

		public void start() throws OpenIoTException {
			try {
				// Create a VM connection to the broker.
				ActiveMQConnectionFactory connectionFactory =
						new ActiveMQConnectionFactory("vm://" + getBrokerName());
				this.connection = connectionFactory.createConnection();
				connection.setExceptionListener(this);
				connection.start();

				// Create a Session
				this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

				Destination destination = session.createQueue(getQueueName());
				this.consumer = session.createConsumer(destination);
			} catch (Exception e) {
				throw new OpenIoTException("Error starting ActiveMQ consumer.", e);
			}
		}

		public void stop() throws OpenIoTException {
			try {
				consumer.close();
				session.close();
				connection.close();
			} catch (Exception e) {
				throw new OpenIoTException("Error shutting down ActiveMQ consumer.", e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (true) {
				try {
					Message message = consumer.receive();
					if (message == null) {
						break;
					}
					if (message instanceof TextMessage) {
						TextMessage textMessage = (TextMessage) message;
						onEventPayloadReceived(textMessage.getText().getBytes(),new HashMap());
					} else if (message instanceof BytesMessage) {
						BytesMessage bytesMessage = (BytesMessage) message;
						byte[] buffer = new byte[(int) bytesMessage.getBodyLength()];
						bytesMessage.readBytes(buffer);
						onEventPayloadReceived(buffer,new HashMap());
					} else {
						LOGGER.warn("Ignoring unknown JMS message type: " + message.getClass().getName());
					}
				} catch (Throwable e) {
					LOGGER.error("Error in ActiveMQ message processing.", e);
					return;
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.jms.ExceptionListener#onException(javax.jms.JMSException)
		 */
		@Override
		public void onException(JMSException e) {
			try {
				stop();
			} catch (OpenIoTException e1) {
			}
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

	public String getBrokerName() {
		return brokerName;
	}

	public void setBrokerName(String brokerName) {
		this.brokerName = brokerName;
	}

	public String getTransportUri() {
		return transportUri;
	}

	public void setTransportUri(String transportUri) {
		this.transportUri = transportUri;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getDataDirectory() {
		return dataDirectory;
	}

	public void setDataDirectory(String dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public int getNumConsumers() {
		return numConsumers;
	}

	public void setNumConsumers(int numConsumers) {
		this.numConsumers = numConsumers;
	}
}