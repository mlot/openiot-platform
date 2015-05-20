/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning;

import com.openiot.OpenIoT;
import com.openiot.device.event.processor.OutboundEventProcessor;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.batch.IBatchOperation;
import com.openiot.spi.device.event.IDeviceCommandInvocation;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Event processor that hands off {@link IDeviceCommandInvocation} events after they have
 * been saved so that provisioning can process them.
 * 
 * @author Derek
 */
public class ProvisioningEventProcessor extends OutboundEventProcessor {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(ProvisioningEventProcessor.class);

	/** Number of invocations to buffer before blocking calls */
	private static final int DEFAULT_NUM_THREADS = 10;

	/** Number of threads used for processing provisioning requests */
	private int numThreads = DEFAULT_NUM_THREADS;

	/** Used to execute Solr indexing in a separate thread */
	private ExecutorService executor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.rest.model.device.event.processor.OutboundEventProcessor#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		LOGGER.info("Provisioning event processor using " + getNumThreads() + " threads to process requests.");
		executor = Executors.newFixedThreadPool(getNumThreads(), new ProcessorsThreadFactory());
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
	 * @see com.openiot.rest.model.device.event.processor.OutboundEventProcessor#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
		executor.shutdownNow();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.rest.model.device.event.processor.OutboundEventProcessor#
	 * onCommandInvocation(IDeviceCommandInvocation)
	 */
	@Override
	public void onCommandInvocation(IDeviceCommandInvocation invocation) throws OpenIoTException {
		executor.execute(new CommandInvocationProcessor(invocation));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * OutboundEventProcessor#onBatchOperation(com
	 * .sitewhere.spi.device.batch.IBatchOperation)
	 */
	@Override
	public void onBatchOperation(IBatchOperation operation) throws OpenIoTException {
		executor.execute(new BatchOperationProcessor(operation));
	}

	/**
	 * Processes command invocations asynchronously.
	 */
	private class CommandInvocationProcessor implements Runnable {

		private IDeviceCommandInvocation command;

		public CommandInvocationProcessor(IDeviceCommandInvocation command) {
			this.command = command;
		}

		@Override
		public void run() {
			try {
				LOGGER.debug("Provisioning processor thread processing command invocation.");
				OpenIoT.getServer().getDeviceProvisioning().deliverCommand(command);
			} catch (OpenIoTException e) {
				LOGGER.error("Exception thrown in provisioning operation.", e);
			} catch (Throwable e) {
				LOGGER.error("Unhandled exception in provisioning operation.", e);
			}
		}
	}

	/**
	 * Processes batch operations asynchronously.
	 */
	private class BatchOperationProcessor implements Runnable {

		private IBatchOperation operation;

		public BatchOperationProcessor(IBatchOperation operation) {
			this.operation = operation;
		}

		@Override
		public void run() {
			try {
				LOGGER.debug("Provisioning processor thread processing batch operation.");
				OpenIoT.getServer().getDeviceProvisioning().getBatchOperationManager().process(operation);
			} catch (OpenIoTException e) {
				LOGGER.error("Exception thrown in provisioning operation.", e);
			} catch (Throwable e) {
				LOGGER.error("Unhandled exception in provisioning operation.", e);
			}
		}
	}

	/** Used for naming processor threads */
	private class ProcessorsThreadFactory implements ThreadFactory {

		/** Counts threads */
		private AtomicInteger counter = new AtomicInteger();

		public Thread newThread(Runnable r) {
			return new Thread(r, "Provisioning processor " + counter.incrementAndGet());
		}
	}

	public int getNumThreads() {
		return numThreads;
	}

	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}
}