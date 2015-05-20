/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning;

import com.openiot.OpenIoT;
import com.openiot.server.OpenIoTServer;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.batch.IBatchOperation;
import com.openiot.spi.device.event.*;
import com.openiot.spi.device.event.processor.IOutboundEventProcessorChain;
import com.openiot.spi.device.provisioning.IOutboundProcessingStrategy;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of {@link IOutboundProcessingStrategy} that uses an
 * {@link ArrayBlockingQueue} to hold events that are submitted into the
 * {@link IOutboundEventProcessorChain}.
 * 
 * @author Derek
 */
public class BlockingQueueOutboundProcessingStrategy extends LifecycleComponent implements
		IOutboundProcessingStrategy {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(BlockingQueueOutboundProcessingStrategy.class);

	/** Maximum size of queues */
	private static final int MAX_QUEUE_SIZE = 1000;

	/** Number of threads used for event processing */
	private static final int EVENT_PROCESSOR_THREAD_COUNT = 10;

	/** Blocking queue of pending create requests from receivers */
	private BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(MAX_QUEUE_SIZE);

	/** Thread pool for processing events */
	private ExecutorService processorPool;

	public BlockingQueueOutboundProcessingStrategy() {
		super(LifecycleComponentType.OutboundProcessingStrategy);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		processorPool =
				Executors.newFixedThreadPool(EVENT_PROCESSOR_THREAD_COUNT, new ProcessorsThreadFactory());
		for (int i = 0; i < EVENT_PROCESSOR_THREAD_COUNT; i++) {
			processorPool.execute(new BlockingDeviceEventProcessor(queue));
		}
		LOGGER.info("Started blocking queue outbound processing strategy with queue size of "
				+ MAX_QUEUE_SIZE + " and " + EVENT_PROCESSOR_THREAD_COUNT + " threads.");
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

	/** Used for naming processor threads */
	private class ProcessorsThreadFactory implements ThreadFactory {

		/** Counts threads */
		private AtomicInteger counter = new AtomicInteger();

		public Thread newThread(Runnable r) {
			return new Thread(r, "OpenIoT BlockingQueueOutboundProcessingStrategy Processor "
					+ counter.incrementAndGet());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
		if (processorPool != null) {
			processorPool.shutdownNow();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundEventProcessor#onMeasurements
	 * (IDeviceMeasurements)
	 */
	@Override
	public void onMeasurements(IDeviceMeasurements measurements) throws OpenIoTException {
		queue.offer(measurements);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundEventProcessor#onLocation(com
	 * .sitewhere.spi.device.event.IDeviceLocation)
	 */
	@Override
	public void onLocation(IDeviceLocation location) throws OpenIoTException {
		queue.offer(location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundEventProcessor#onAlert(com.openiot
	 * .spi.device.event.IDeviceAlert)
	 */
	@Override
	public void onAlert(IDeviceAlert alert) throws OpenIoTException {
		queue.offer(alert);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundEventProcessor#onCommandInvocation
	 * (IDeviceCommandInvocation)
	 */
	@Override
	public void onCommandInvocation(IDeviceCommandInvocation invocation) throws OpenIoTException {
		queue.offer(invocation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundEventProcessor#onCommandResponse
	 * (IDeviceCommandResponse)
	 */
	@Override
	public void onCommandResponse(IDeviceCommandResponse response) throws OpenIoTException {
		queue.offer(response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOutboundEventProcessor#onBatchOperation
	 * (IBatchOperation)
	 */
	@Override
	public void onBatchOperation(IBatchOperation operation) throws OpenIoTException {
		queue.offer(operation);
	}

	/**
	 * Blocking thread that processes {@link IDeviceEvent} messages from a queue.
	 * 
	 * @author Derek
	 * 
	 * @param <T>
	 */
	private class BlockingDeviceEventProcessor implements Runnable {

		/** Queue where messages are placed */
		private BlockingQueue<Object> queue;

		public BlockingDeviceEventProcessor(BlockingQueue<Object> queue) {
			this.queue = queue;
		}

		@Override
		public void run() {
			// Event creation APIs expect an authenticated user in order to check
			// permissions and log who creates events. When called in this context, the
			// authenticated user will always be 'system'.
			//
			// TODO: Alternatively, we may want the client to authenticate on registration
			// and pass a token on each request.
			try {
				SecurityContextHolder.getContext().setAuthentication(
						OpenIoTServer.getSystemAuthentication());
			} catch (OpenIoTException e) {
				throw new RuntimeException("Unable to use system authentication for outbound device "
						+ " event processor thread.", e);
			}
			while (true) {
				try {
					Object event = queue.take();
					if (event instanceof IDeviceMeasurements) {
						OpenIoT.getServer().getOutboundEventProcessorChain().onMeasurements(
								(IDeviceMeasurements) event);
					} else if (event instanceof IDeviceLocation) {
						OpenIoT.getServer().getOutboundEventProcessorChain().onLocation(
								(IDeviceLocation) event);
					} else if (event instanceof IDeviceAlert) {
						OpenIoT.getServer().getOutboundEventProcessorChain().onAlert((IDeviceAlert) event);
					} else if (event instanceof IDeviceCommandInvocation) {
						OpenIoT.getServer().getOutboundEventProcessorChain().onCommandInvocation(
								(IDeviceCommandInvocation) event);
					} else if (event instanceof IDeviceCommandResponse) {
						OpenIoT.getServer().getOutboundEventProcessorChain().onCommandResponse(
								(IDeviceCommandResponse) event);
					} else if (event instanceof IBatchOperation) {
						OpenIoT.getServer().getOutboundEventProcessorChain().onBatchOperation(
								(IBatchOperation) event);
					} else {
						throw new RuntimeException("Unknown device event type in outbound processing: "
								+ event.getClass().getName());
					}
				} catch (OpenIoTException e) {
					LOGGER.error("Error processing outbound device event.", e);
				} catch (InterruptedException e) {
					break;
				} catch (Throwable e) {
					LOGGER.error("Unhandled exception in outbound event processing.", e);
				}
			}
		}
	}
}