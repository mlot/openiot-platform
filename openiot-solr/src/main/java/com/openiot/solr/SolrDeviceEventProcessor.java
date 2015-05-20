/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.solr;

import com.openiot.device.event.processor.OutboundEventProcessor;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.IDeviceAlert;
import com.openiot.spi.device.event.IDeviceLocation;
import com.openiot.spi.device.event.IDeviceMeasurements;
import com.openiot.spi.device.event.processor.IOutboundEventProcessor;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@link IOutboundEventProcessor} implementation that takes saved events and indexes them
 * in Apache Solr for advanced analytics processing.
 * 
 * @author Derek
 */
public class SolrDeviceEventProcessor extends OutboundEventProcessor {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(SolrDeviceEventProcessor.class);

	/** Number of documents to buffer before blocking calls */
	private static final int BUFFER_SIZE = 1000;

	/** Injected Solr configuration */
	private OpenIoTSolrConfiguration solr;

	/** Bounded queue that holds documents to be processed */
	private BlockingQueue<SolrInputDocument> queue = new ArrayBlockingQueue<SolrInputDocument>(BUFFER_SIZE);

	/** Used to execute Solr indexing in a separate thread */
	/** TODO: Use a better approach for scalability */
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		if (getSolr() == null) {
			throw new OpenIoTException("No Solr configuration provided to " + getClass().getName());
		}
		try {
			LOGGER.info("Attempting to ping Solr server to verify availability...");
			SolrPingResponse response = getSolr().getSolrServer().ping();
			int pingTime = response.getQTime();
			LOGGER.info("Solr server location verified. Ping responded in " + pingTime + " ms.");
		} catch (SolrServerException e) {
			throw new OpenIoTException("Ping failed. Verify that Solr server is available.", e);
		} catch (IOException e) {
			throw new OpenIoTException("Exception in ping. Verify that Solr server is available.", e);
		}
		LOGGER.info("Solr event processor indexing events to server at: " + getSolr().getSolrServerUrl());
		executor.execute(new SolrDocumentQueueProcessor());
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
	 * com.openiot.rest.model.device.event.processor.OutboundEventProcessor#onMeasurements
	 * (IDeviceMeasurements)
	 */
	@Override
	public void onMeasurements(IDeviceMeasurements measurements) throws OpenIoTException {
		SolrInputDocument document = OpenIoTSolrFactory.createDocumentFromMeasurements(measurements);
		try {
			queue.put(document);
		} catch (InterruptedException e) {
			throw new OpenIoTException("Interrupted during indexing.", e);
		}
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
		SolrInputDocument document = OpenIoTSolrFactory.createDocumentFromLocation(location);
		try {
			queue.put(document);
		} catch (InterruptedException e) {
			throw new OpenIoTException("Interrupted during indexing.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.rest.model.device.event.processor.OutboundEventProcessor#onAlert(
	 * IDeviceAlert)
	 */
	@Override
	public void onAlert(IDeviceAlert alert) throws OpenIoTException {
		SolrInputDocument document = OpenIoTSolrFactory.createDocumentFromAlert(alert);
		try {
			queue.put(document);
		} catch (InterruptedException e) {
			throw new OpenIoTException("Interrupted during indexing.", e);
		}
	}

	/**
	 * Class that processes documents in the queue asynchronously.
	 * 
	 * @author Derek
	 */
	private class SolrDocumentQueueProcessor implements Runnable {

		@Override
		public void run() {
			LOGGER.info("Started Solr indexing thread.");
			while (true) {
				try {
					SolrInputDocument document = queue.take();
					try {
						LOGGER.debug("Indexing document in Solr...");
						UpdateResponse response = getSolr().getSolrServer().add(document);
						if (response.getStatus() == 0) {
							LOGGER.debug("Indexed document successfully. " + response.toString());
							getSolr().getSolrServer().commit();
						} else {
							LOGGER.warn("Bad response code indexing document: " + response.getStatus());
						}
					} catch (SolrServerException e) {
						LOGGER.error("Exception indexing OpenIoT document.", e);
					} catch (IOException e) {
						LOGGER.error("IOException indexing OpenIoT document.", e);
					} catch (Throwable e) {
						LOGGER.error("Unhandled exception indexing OpenIoT document.", e);
					}
				} catch (InterruptedException e) {
					LOGGER.error(e);
				}
			}
		}
	}

	public OpenIoTSolrConfiguration getSolr() {
		return solr;
	}

	public void setSolr(OpenIoTSolrConfiguration solr) {
		this.solr = solr;
	}
}