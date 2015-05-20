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
import com.openiot.spi.device.event.processor.IInboundEventProcessorChain;
import com.openiot.spi.device.event.request.*;
import com.openiot.spi.device.provisioning.IDecodedDeviceEventRequest;
import com.openiot.spi.device.provisioning.IInboundProcessingStrategy;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of {@link IInboundProcessingStrategy} that uses an
 * {@link ArrayBlockingQueue} to hold decoded events that are submitted into the
 * {@link IInboundEventProcessorChain}.
 * 
 * @author Derek
 */
public class BlockingQueueInboundProcessingStrategy extends LifecycleComponent implements
		IInboundProcessingStrategy {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(BlockingQueueInboundProcessingStrategy.class);

	/** Maximum size of queues */
	private static final int MAX_QUEUE_SIZE = 10000;

	/** Number of threads used for event processing */
	private static final int EVENT_PROCESSOR_THREAD_COUNT = 100;

	/** Interval between monitoring log output messages */
	private static final int MONITORING_INTERVAL_SEC = 5;

	/** Number of thread used for event processing */
	private int eventProcessorThreadCount = EVENT_PROCESSOR_THREAD_COUNT;

	/** Indicates whether monitoring messages should be logged */
	private boolean enableMonitoring = false;

	/** Number of seconds between monitoring messages */
	private int monitoringIntervalSec = MONITORING_INTERVAL_SEC;

	/** Counter for number of events */
	private AtomicLong eventCount = new AtomicLong();

	/** Counter for number of errors */
	private AtomicLong errorCount = new AtomicLong();

	/** Total wait time */
	private AtomicLong totalWaitTime = new AtomicLong();

	/** Total processing time */
	private AtomicLong totalProcessingTime = new AtomicLong();

	/** Blocking queue of pending event create requests from event sources */
	private BlockingQueue<PerformanceWrapper> queue = new ArrayBlockingQueue<PerformanceWrapper>(
			MAX_QUEUE_SIZE);

	/** Thread pool for processing events */
	private ExecutorService processorPool;

	/** Pool for monitoring thread */
	private ExecutorService monitorPool = Executors.newSingleThreadExecutor();

	public BlockingQueueInboundProcessingStrategy() {
		super(LifecycleComponentType.InboundProcessingStrategy);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		processorPool =
				Executors.newFixedThreadPool(getEventProcessorThreadCount(), new ProcessorsThreadFactory());
		for (int i = 0; i < getEventProcessorThreadCount(); i++) {
			processorPool.execute(new BlockingMessageProcessor(queue));
		}
		LOGGER.info("Started blocking queue inbound processing strategy with queue size of " + MAX_QUEUE_SIZE
				+ " and " + getEventProcessorThreadCount() + " threads.");

		// Only show monitoring data if enabled.
		if (isEnableMonitoring()) {
			monitorPool.execute(new MonitorOutput());
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

	/** Used for naming processor threads */
	private class ProcessorsThreadFactory implements ThreadFactory {

		/** Counts threads */
		private AtomicInteger counter = new AtomicInteger();

		public Thread newThread(Runnable r) {
			return new Thread(r, "OpenIoT BlockingQueueInboundProcessingStrategy Processor "
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
		if (monitorPool != null) {
			monitorPool.shutdownNow();
		}
		LOGGER.info("Stopped blocking queue inbound processing strategy.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IInboundProcessingStrategy#processRegistration
	 * (IDecodedDeviceEventRequest)
	 */
	@Override
	public void processRegistration(IDecodedDeviceEventRequest request) throws OpenIoTException {
		addRequestToQueue(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IInboundProcessingStrategy#
	 * processDeviceCommandResponse
	 * (IDecodedDeviceEventRequest)
	 */
	@Override
	public void processDeviceCommandResponse(IDecodedDeviceEventRequest request) throws OpenIoTException {
		addRequestToQueue(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IInboundProcessingStrategy#
	 * processDeviceMeasurements
	 * (IDecodedDeviceEventRequest)
	 */
	@Override
	public void processDeviceMeasurements(IDecodedDeviceEventRequest request) throws OpenIoTException {
		addRequestToQueue(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IInboundProcessingStrategy#processDeviceLocation
	 * (IDecodedDeviceEventRequest)
	 */
	@Override
	public void processDeviceLocation(IDecodedDeviceEventRequest request) throws OpenIoTException {
		addRequestToQueue(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IInboundProcessingStrategy#processDeviceAlert
	 * (IDecodedDeviceEventRequest)
	 */
	@Override
	public void processDeviceAlert(IDecodedDeviceEventRequest request) throws OpenIoTException {
		addRequestToQueue(request);
	}

	/**
	 * Adds an {@link IDecodedDeviceEventRequest} to the queue, blocking if no space is
	 * available.
	 * 
	 * @param request
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void addRequestToQueue(IDecodedDeviceEventRequest request) throws OpenIoTException {
		try {
			eventCount.incrementAndGet();

			PerformanceWrapper wrapper = new PerformanceWrapper();
			wrapper.setRequest(request);
			wrapper.setStartTime(System.currentTimeMillis());
			queue.put(wrapper);
		} catch (InterruptedException e) {
			errorCount.incrementAndGet();
			throw new OpenIoTException(e);
		}
	}

	/**
	 * Get the number of events processed.
	 * 
	 * @return
	 */
	public long getEventCount() {
		return eventCount.get();
	}

	/**
	 * Get the number of errors in processing.
	 * 
	 * @return
	 */
	public long getErrorCount() {
		return errorCount.get();
	}

	/**
	 * Get the number of backlogged requests.
	 * 
	 * @return
	 */
	public long getBacklog() {
		return queue.size();
	}

	/**
	 * Get the average wait time in milliseconds.
	 * 
	 * @return
	 */
	public long getAverageProcessingWaitTime() {
		long total = totalWaitTime.get();
		long count = eventCount.get();
		if (count == 0) {
			return 0;
		}
		return total / count;
	}

	/**
	 * Get the average processing time in milliseconds.
	 * 
	 * @return
	 */
	public long getAverageProcessingTime() {
		return 0;
	}

	/**
	 * Get the average processing time of downstream components.
	 * 
	 * @return
	 */
	public long getAverageDownstreamProcessingTime() {
		long total = totalProcessingTime.get();
		long count = eventCount.get();
		if (count == 0) {
			return 0;
		}
		return total / count;
	}

	public int getEventProcessorThreadCount() {
		return eventProcessorThreadCount;
	}

	public void setEventProcessorThreadCount(int eventProcessorThreadCount) {
		this.eventProcessorThreadCount = eventProcessorThreadCount;
	}

	public boolean isEnableMonitoring() {
		return enableMonitoring;
	}

	public void setEnableMonitoring(boolean enableMonitoring) {
		this.enableMonitoring = enableMonitoring;
	}

	public int getMonitoringIntervalSec() {
		return monitoringIntervalSec;
	}

	public void setMonitoringIntervalSec(int monitoringIntervalSec) {
		this.monitoringIntervalSec = monitoringIntervalSec;
	}

	public class PerformanceWrapper {

		/** Start time for event processing */
		private long startTime;

		/** Event request */
		private IDecodedDeviceEventRequest request;

		public long getStartTime() {
			return startTime;
		}

		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}

		public IDecodedDeviceEventRequest getRequest() {
			return request;
		}

		public void setRequest(IDecodedDeviceEventRequest request) {
			this.request = request;
		}
	}

	/**
	 * Logs monitor output at a given time interval.
	 * 
	 * @author Derek
	 */
	public class MonitorOutput implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					long eventCount = getEventCount();
					long errorCount = getErrorCount();
					long backlog = getBacklog();
					long avgWaitTime = getAverageProcessingWaitTime();
					long avgProcessingTime = getAverageProcessingTime();
					long avgDownstreamTime = getAverageDownstreamProcessingTime();
					String message =
							String.format(
									"Count(%5d) Errors(%5d) Backlog(%5d) AvgWait(%5d ms) AvgProc(%5d ms) AvgDS(%5d ms)",
									eventCount, errorCount, backlog, avgWaitTime, avgProcessingTime,
									avgDownstreamTime);
					LOGGER.info(message);
				} catch (Throwable e) {
					LOGGER.error(e);
				}
				try {
					Thread.sleep(getMonitoringIntervalSec() * 1000);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * Blocking thread that processes {@link IDecodedDeviceEventRequest} from a queue.
	 * 
	 * @author Derek
	 * 
	 * @param <T>
	 */
	private class BlockingMessageProcessor implements Runnable {

		/** Queue where messages are placed */
		private BlockingQueue<PerformanceWrapper> queue;

		public BlockingMessageProcessor(BlockingQueue<PerformanceWrapper> queue) {
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
				throw new RuntimeException("Unable to use system authentication for inbound device "
						+ " event processor thread.", e);
			}
			while (true) {
				try {
					PerformanceWrapper wrapper = queue.take();
					long wait = System.currentTimeMillis() - wrapper.getStartTime();
					totalWaitTime.addAndGet(wait);

					long processingStart = System.currentTimeMillis();

					IDecodedDeviceEventRequest decoded = wrapper.getRequest();
					if (decoded.getRequest() instanceof IDeviceRegistrationRequest) {
						OpenIoT.getServer().getInboundEventProcessorChain().onRegistrationRequest(
								decoded.getHardwareId(), decoded.getOriginator(),
								((IDeviceRegistrationRequest) decoded.getRequest()));
					} else if (decoded.getRequest() instanceof IDeviceCommandResponseCreateRequest) {
						OpenIoT.getServer().getInboundEventProcessorChain().onDeviceCommandResponseRequest(
								decoded.getHardwareId(), decoded.getOriginator(),
								((IDeviceCommandResponseCreateRequest) decoded.getRequest()));
					} else if (decoded.getRequest() instanceof IDeviceMeasurementsCreateRequest) {
						OpenIoT.getServer().getInboundEventProcessorChain().onDeviceMeasurementsCreateRequest(
								decoded.getHardwareId(), decoded.getOriginator(),
								((IDeviceMeasurementsCreateRequest) decoded.getRequest()));
					} else if (decoded.getRequest() instanceof IDeviceLocationCreateRequest) {
						OpenIoT.getServer().getInboundEventProcessorChain().onDeviceLocationCreateRequest(
								decoded.getHardwareId(), decoded.getOriginator(),
								((IDeviceLocationCreateRequest) decoded.getRequest()));
					} else if (decoded.getRequest() instanceof IDeviceAlertCreateRequest) {
						OpenIoT.getServer().getInboundEventProcessorChain().onDeviceAlertCreateRequest(
								decoded.getHardwareId(), decoded.getOriginator(),
								((IDeviceAlertCreateRequest) decoded.getRequest()));
					} else {
						throw new RuntimeException("Unknown device event type: "
								+ decoded.getRequest().getClass().getName());
					}

					long processingTime = System.currentTimeMillis() - processingStart;
					totalProcessingTime.addAndGet(processingTime);
				} catch (OpenIoTException e) {
					errorCount.incrementAndGet();
					LOGGER.error("Error processing inbound device event.", e);
				} catch (InterruptedException e) {
					break;
				} catch (Throwable e) {
					errorCount.incrementAndGet();
					LOGGER.error("Unhandled exception in inbound event processing.", e);
				}
			}
		}
	}
}