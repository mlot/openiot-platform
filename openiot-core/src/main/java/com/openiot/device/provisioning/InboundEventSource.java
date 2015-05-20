/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning;

import com.openiot.OpenIoT;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.event.request.*;
import com.openiot.spi.device.provisioning.*;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link IInboundEventSource}.
 * 
 * @author Derek
 * 
 * @param <T>
 */
public class InboundEventSource<T> extends LifecycleComponent implements IInboundEventSource<T> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(InboundEventSource.class);

	/** Unique id for referencing source */
	private String sourceId;

	/** Indicates if assignment state should be updated with event data */
	private boolean updateAssignmentState = false;

	/** Device event decoder */
	private IDeviceEventDecoder<T> deviceEventDecoder;

	/** Inbound event processing strategy */
	private IInboundProcessingStrategy inboundProcessingStrategy;

	/** List of {@link IInboundEventReceiver} that supply this processor */
	private List<IInboundEventReceiver<T>> inboundEventReceivers = new ArrayList<IInboundEventReceiver<T>>();

	public InboundEventSource() {
		super(LifecycleComponentType.InboundEventSource);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		getLifecycleComponents().clear();

		LOGGER.debug("Starting event source '" + getSourceId() + "'.");
		if (getInboundProcessingStrategy() == null) {
			setInboundProcessingStrategy(OpenIoT.getServer().getDeviceProvisioning().getInboundProcessingStrategy());
		}
		if ((getInboundEventReceivers() == null) || (getInboundEventReceivers().size() == 0)) {
			throw new OpenIoTException("No inbound event receivers registered for event source.");
		}
		startEventReceivers();
		LOGGER.debug("Started event source '" + getSourceId() + "'.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see LifecycleComponent#getComponentName()
	 */
	@Override
	public String getComponentName() {
		return "Event Source (" + getSourceId() + ")";
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

	/**
	 * Start event receivers for this event source.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void startEventReceivers() throws OpenIoTException {
		if (getInboundEventReceivers().size() > 0) {
			for (IInboundEventReceiver<T> receiver : getInboundEventReceivers()) {
				receiver.setEventSource(this);
				startNestedComponent(receiver, true);
			}
		} else {
			LOGGER.warn("No device event receivers configured for event source!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IInboundEventSource#onEncodedEventReceived
	 * (IInboundEventReceiver, java.lang.Object)
	 */
	@Override
	public void onEncodedEventReceived(IInboundEventReceiver<T> receiver, T encodedPayload, Map eventContext) {
		try {
			LOGGER.debug("Device event receiver thread picked up event.");
			List<IDecodedDeviceEventRequest> requests = decodePayload(encodedPayload, eventContext);
			if (requests != null) {
				for (IDecodedDeviceEventRequest decoded : requests) {
					decoded.getRequest().setUpdateState(isUpdateAssignmentState());
					if (decoded.getRequest() instanceof IDeviceRegistrationRequest) {
						getInboundProcessingStrategy().processRegistration(decoded);
					} else if (decoded.getRequest() instanceof IDeviceCommandResponseCreateRequest) {
						getInboundProcessingStrategy().processDeviceCommandResponse(decoded);
					} else if (decoded.getRequest() instanceof IDeviceMeasurementsCreateRequest) {
						getInboundProcessingStrategy().processDeviceMeasurements(decoded);
					} else if (decoded.getRequest() instanceof IDeviceLocationCreateRequest) {
						getInboundProcessingStrategy().processDeviceLocation(decoded);
					} else if (decoded.getRequest() instanceof IDeviceAlertCreateRequest) {
						getInboundProcessingStrategy().processDeviceAlert(decoded);
					} else {
						LOGGER.error("Decoded device event request could not be routed: "
								+ decoded.getRequest().getClass().getName());
					}
				}
			}
		} catch (OpenIoTException e) {
			onEventDecodeFailed(encodedPayload, e);
		} catch (Throwable e) {
			onEventDecodeFailed(encodedPayload, e);
		}
	}

	/**
	 * Decode a payload into individual events.
	 * 
	 * @param encodedPayload
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected List<IDecodedDeviceEventRequest> decodePayload(T encodedPayload, Map context) throws OpenIoTException {
		return getDeviceEventDecoder().decode(encodedPayload, context);
	}

	/**
	 * Handler for case where decoder throws an exception.
	 * 
	 * @param encodedEvent
	 * @param t
	 */
	protected void onEventDecodeFailed(T encodedEvent, Throwable t) {
		LOGGER.error("Event receiver thread unable to decode event request.", t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
		LOGGER.info("Stopping inbound event source '" + getSourceId() + "'.");
		if (getInboundEventReceivers().size() > 0) {
			for (IInboundEventReceiver<T> receiver : getInboundEventReceivers()) {
				receiver.lifecycleStop();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IInboundEventSource#getSourceId()
	 */
	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IInboundEventSource#isUpdateAssignmentState()
	 */
	public boolean isUpdateAssignmentState() {
		return updateAssignmentState;
	}

	public void setUpdateAssignmentState(boolean updateAssignmentState) {
		this.updateAssignmentState = updateAssignmentState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IInboundEventSource#setDeviceEventDecoder
	 * (IDeviceEventDecoder)
	 */
	public void setDeviceEventDecoder(IDeviceEventDecoder<T> deviceEventDecoder) {
		this.deviceEventDecoder = deviceEventDecoder;
	}

	public IDeviceEventDecoder<T> getDeviceEventDecoder() {
		return deviceEventDecoder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IInboundEventSource#setInboundProcessingStrategy
	 * (IInboundProcessingStrategy)
	 */
	public void setInboundProcessingStrategy(IInboundProcessingStrategy inboundProcessingStrategy) {
		this.inboundProcessingStrategy = inboundProcessingStrategy;
	}

	public IInboundProcessingStrategy getInboundProcessingStrategy() {
		return inboundProcessingStrategy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IInboundEventSource#setInboundEventReceivers
	 * (java.util.List)
	 */
	public void setInboundEventReceivers(List<IInboundEventReceiver<T>> inboundEventReceivers) {
		this.inboundEventReceivers = inboundEventReceivers;
	}

	public List<IInboundEventReceiver<T>> getInboundEventReceivers() {
		return inboundEventReceivers;
	}
}