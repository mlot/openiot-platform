/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning.socket;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.provisioning.IInboundEventReceiver;
import com.openiot.spi.device.provisioning.socket.ISocketInteractionHandler;
import com.openiot.spi.device.provisioning.socket.ISocketInteractionHandlerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 * Implementation of {@link ISocketInteractionHandler} that reads everything from the
 * socket and sends the resulting byte array to the parent event source.
 * 
 * @author Derek
 */
public class ReadAllInteractionHandler implements ISocketInteractionHandler<byte[]> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ISocketInteractionHandler#process(
	 * java.net.Socket, IInboundEventReceiver)
	 */
	@Override
	public void process(Socket socket, IInboundEventReceiver<byte[]> receiver) throws OpenIoTException {
		try {
			InputStream input = socket.getInputStream();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			int value;
			while ((value = input.read()) != -1) {
				output.write(value);
			}
			input.close();
			receiver.onEventPayloadReceived(output.toByteArray(), new HashMap());
		} catch (IOException e) {
			throw new OpenIoTException("Exception processing request in socket interaction handler.", e);
		}
	}

	/**
	 * Factory class that produces {@link ReadAllInteractionHandler} instances.
	 * 
	 * @author Derek
	 */
	public static class Factory implements ISocketInteractionHandlerFactory<byte[]> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * ISocketInteractionHandlerFactory
		 * #newInstance()
		 */
		@Override
		public ISocketInteractionHandler<byte[]> newInstance() {
			return new ReadAllInteractionHandler();
		}
	}
}