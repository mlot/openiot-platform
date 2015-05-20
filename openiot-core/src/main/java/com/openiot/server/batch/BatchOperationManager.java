/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.server.batch;

import com.openiot.OpenIoT;
import com.openiot.rest.model.device.event.request.DeviceCommandInvocationCreateRequest;
import com.openiot.rest.model.device.request.BatchElementUpdateRequest;
import com.openiot.rest.model.device.request.BatchOperationUpdateRequest;
import com.openiot.rest.model.search.SearchResults;
import com.openiot.rest.model.search.device.BatchElementSearchCriteria;
import com.openiot.security.SitewhereAuthentication;
import com.openiot.server.OpenIoTServer;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.common.IMetadataProvider;
import com.openiot.spi.device.IDevice;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.batch.*;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.event.CommandInitiator;
import com.openiot.spi.device.event.CommandTarget;
import com.openiot.spi.device.event.IDeviceCommandInvocation;
import com.openiot.spi.device.request.IBatchCommandInvocationRequest;
import com.openiot.spi.device.request.IBatchOperationCreateRequest;
import com.openiot.spi.search.device.IBatchElementSearchCriteria;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import com.openiot.spi.server.lifecycle.LifecycleStatus;
import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of {@link IBatchOperationManager}. Uses multiple threads to
 * process batch operations.
 * 
 * @author Derek
 */
public class BatchOperationManager extends LifecycleComponent implements IBatchOperationManager {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(BatchOperationManager.class);

	/** Number of threads used for batch operation processing */
	private static final int BATCH_PROCESSOR_THREAD_COUNT = 10;

	/** Thread pool for processing events */
	private ExecutorService processorPool;

	/** Throttling delay in milliseconds */
	private long throttleDelayMs;

	public BatchOperationManager() {
		super(LifecycleComponentType.BatchOperationManager);
	}

	/** Used for naming batch operation processor threads */
	private class ProcessorsThreadFactory implements ThreadFactory {

		/** Counts threads */
		private AtomicInteger counter = new AtomicInteger();

		public Thread newThread(Runnable r) {
			return new Thread(r, "Batch Operation Processor " + counter.incrementAndGet());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		processorPool =
				Executors.newFixedThreadPool(BATCH_PROCESSOR_THREAD_COUNT, new ProcessorsThreadFactory());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see LifecycleComponent#canPause()
	 */
	public boolean canPause() throws OpenIoTException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
		processorPool.shutdownNow();
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
	 * @see
	 * IBatchOperationManager#process(com.openiot.spi
	 * .device.batch.IBatchOperation)
	 */
	@Override
	public void process(IBatchOperation operation) throws OpenIoTException {
		processorPool.execute(new BatchOperationProcessor(operation));
	}

	public long getThrottleDelayMs() {
		return throttleDelayMs;
	}

	public void setThrottleDelayMs(long throttleDelayMs) {
		this.throttleDelayMs = throttleDelayMs;
	}

	/**
	 * Processes a batch in a separate thread.
	 * 
	 * @author Derek
	 */
	private class BatchOperationProcessor implements Runnable {

		/** Operation being processed */
		private IBatchOperation operation;

		private SitewhereAuthentication systemUser;

		public BatchOperationProcessor(IBatchOperation operation) {
			this.operation = operation;
		}

		@Override
		public void run() {
			LOGGER.debug("Processing batch operation: " + operation.getToken());
			try {
				systemUser = OpenIoTServer.getSystemAuthentication();
				SecurityContextHolder.getContext().setAuthentication(systemUser);

				BatchOperationUpdateRequest request = new BatchOperationUpdateRequest();
				request.setProcessingStatus(BatchOperationStatus.Processing);
				request.setProcessingStartedDate(new Date());
				OpenIoT.getServer().getDeviceManagement().updateBatchOperation(operation.getToken(),
						request);

				// Process all batch elements.
				IBatchElementSearchCriteria criteria = new BatchElementSearchCriteria(1, 0);
				SearchResults<IBatchElement> matches =
						OpenIoT.getServer().getDeviceManagement().listBatchElements(operation.getToken(),
								criteria);
				BatchProcessingResults result = processBatchElements(operation, matches.getResults());

				// Update operation to reflect processing results.
				request = new BatchOperationUpdateRequest();
				request.setProcessingStatus(BatchOperationStatus.FinishedSuccessfully);
				request.setProcessingEndedDate(new Date());
				if (result.getErrorCount() > 0) {
					request.setProcessingStatus(BatchOperationStatus.FinishedWithErrors);
				}
				OpenIoT.getServer().getDeviceManagement().updateBatchOperation(operation.getToken(),
						request);
			} catch (OpenIoTException e) {
				LOGGER.error("Error processing batch operation.", e);
			}
		}

		/**
		 * Handle case where batch operation manager has been paused.
		 */
		protected void handlePauseAndThrottle() {
			while (getLifecycleStatus() == LifecycleStatus.Paused) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			if (getThrottleDelayMs() > 0) {
				try {
					Thread.sleep(getThrottleDelayMs());
				} catch (InterruptedException e) {
					LOGGER.warn("Throttle timer interrupted.");
				}
			}
		}

		/**
		 * Processes a page of batch elements.
		 * 
		 * @param operation
		 * @param elements
		 * @return
		 * @throws com.openiot.spi.OpenIoTException
		 */
		protected BatchProcessingResults processBatchElements(IBatchOperation operation,
				List<IBatchElement> elements) throws OpenIoTException {
			BatchProcessingResults results = new BatchProcessingResults();
			for (IBatchElement element : elements) {
				// Check whether manager has been paused.
				handlePauseAndThrottle();

				// Only process unprocessed elements.
				if (element.getProcessingStatus() != ElementProcessingStatus.Unprocessed) {
					continue;
				}

				// Indicate element is being processed.
				BatchElementUpdateRequest request = new BatchElementUpdateRequest();
				request.setProcessingStatus(ElementProcessingStatus.Processing);
				OpenIoT.getServer().getDeviceManagement().updateBatchElement(
						element.getBatchOperationToken(), element.getIndex(), request);

				request = new BatchElementUpdateRequest();
				try {
					switch (operation.getOperationType()) {
					case InvokeCommand: {
						processBatchCommandInvocationElement(operation, element, request);
						break;
					}
					case UpdateFirmware: {
						break;
					}
					}
					// Indicate element succeeded in processing.
					request.setProcessingStatus(ElementProcessingStatus.Succeeded);
					request.setProcessedDate(new Date());
				} catch (OpenIoTException t) {
					// Indicate element failed in processing.
					request.setProcessingStatus(ElementProcessingStatus.Failed);
				} finally {
					IBatchElement updated =
							OpenIoT.getServer().getDeviceManagement().updateBatchElement(
									element.getBatchOperationToken(), element.getIndex(), request);
					results.process(updated);
				}
			}
			return results;
		}

		/**
		 * Process a single element from a batch command invocation.
		 * 
		 * @param operation
		 * @param element
		 * @param updated
		 * @throws com.openiot.spi.OpenIoTException
		 */
		protected void processBatchCommandInvocationElement(IBatchOperation operation, IBatchElement element,
				IMetadataProvider updated) throws OpenIoTException {
			LOGGER.info("Processing command invocation: " + element.getHardwareId());

			// Find information about the command to be executed.
			String commandToken =
					operation.getParameters().get(IBatchCommandInvocationRequest.PARAM_COMMAND_TOKEN);
			if (commandToken == null) {
				throw new OpenIoTException("Command token not found in batch command invocation request.");
			}
			IDeviceCommand command =
					OpenIoT.getServer().getDeviceManagement().getDeviceCommandByToken(commandToken);
			if (command == null) {
				throw new OpenIoTException("Invalid command token referenced by batch command invocation.");
			}

			// Find information about the device to execute the command against.
			IDevice device =
					OpenIoT.getServer().getDeviceManagement().getDeviceByHardwareId(element.getHardwareId());
			if (device == null) {
				throw new OpenIoTException("Invalid device hardware id in command invocation.");
			}

			// Find the current assignment information for the device.
			IDeviceAssignment assignment =
					OpenIoT.getServer().getDeviceManagement().getCurrentDeviceAssignment(device);
			if (assignment == null) {
				throw new OpenIoTException("Device is not currently assigned. Command can not be invoked.");
			}

			// Create the request.
			DeviceCommandInvocationCreateRequest request = new DeviceCommandInvocationCreateRequest();
			request.setCommandToken(commandToken);
			request.setInitiator(CommandInitiator.BatchOperation);
			request.setInitiatorId(systemUser.getName());
			request.setTarget(CommandTarget.Assignment);
			request.setTargetId(assignment.getToken());
			request.setParameterValues(operation.getMetadata());
			request.addOrReplaceMetadata(IBatchOperationCreateRequest.META_BATCH_OPERATION_ID,
					operation.getToken());

			// Invoke the command.
			IDeviceCommandInvocation invocation =
					OpenIoT.getServer().getDeviceManagement().addDeviceCommandInvocation(
							assignment.getToken(), command, request);
			updated.addOrReplaceMetadata(IBatchCommandInvocationRequest.META_INVOCATION_EVENT_ID,
					invocation.getId());
		}
	}

	/**
	 * Used to track status of processed elements.
	 * 
	 * @author Derek
	 */
	private class BatchProcessingResults {

		// Count of successfully processed elements.
		private AtomicLong success = new AtomicLong();

		// Count of elements that failed to process.
		private AtomicLong failed = new AtomicLong();

		public void process(IBatchElement element) {
			switch (element.getProcessingStatus()) {
			case Succeeded: {
				success.incrementAndGet();
				break;
			}
			case Failed: {
				failed.incrementAndGet();
				break;
			}
			case Processing:
			case Unprocessed: {
				LOGGER.warn("Batch element was not in an expected state: " + element.getProcessingStatus());
				break;
			}
			}
		}

		public long getErrorCount() {
			return failed.get();
		}
	}
}