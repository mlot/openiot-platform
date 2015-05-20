/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning.websocket;

import com.openiot.spi.device.provisioning.IInboundEventReceiver;
import org.apache.log4j.Logger;

import javax.websocket.*;
import java.util.HashMap;

/**
 * Implementation of {@link WebSocketEventReceiver} that operates on String payloads.
 * 
 * @author Derek
 */
public class StringWebSocketEventReceiver extends WebSocketEventReceiver<String> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(StringWebSocketEventReceiver.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see WebSocketEventReceiver#
	 * getWebSocketClientClass()
	 */
	@Override
	public Class<? extends Endpoint> getWebSocketClientClass() {
		return StringWebSocketClient.class;
	}

	/**
	 * Implementation of {@link WebSocketClient} that operates on String payloads.
	 * 
	 * @author Derek
	 */
	public static class StringWebSocketClient extends Endpoint {

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.websocket.Endpoint#onOpen(javax.websocket.Session,
		 * javax.websocket.EndpointConfig)
		 */
		@Override
		public void onOpen(Session session, final EndpointConfig config) {
			session.addMessageHandler(new MessageHandler.Whole<String>() {

				@SuppressWarnings("unchecked")
				public void onMessage(String payload) {
					IInboundEventReceiver<String> receiver =
							(IInboundEventReceiver<String>) config.getUserProperties().get(
									WebSocketEventReceiver.PROP_EVENT_RECEIVER);
					receiver.onEventPayloadReceived(payload, new HashMap());
				}
			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.websocket.Endpoint#onClose(javax.websocket.Session,
		 * javax.websocket.CloseReason)
		 */
		@Override
		public void onClose(Session session, CloseReason closeReason) {
			LOGGER.info("Web socket closed.");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.websocket.Endpoint#onError(javax.websocket.Session,
		 * java.lang.Throwable)
		 */
		@Override
		public void onError(Session session, Throwable e) {
			LOGGER.error("Web socket error.", e);
		}
	}
}