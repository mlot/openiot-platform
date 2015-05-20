/*
 * Copyright (c) Microsoft Open Technologies (Shanghai) Company Limited.  All rights reserved.
 *
 *  The MIT License (MIT)
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package com.openiot.azure.test;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URLEncoder;
import java.util.Hashtable;

/**
 * Tests for EventHub messaging.
 * 
 * @author Derek
 */
public class EventHubTest {

	// @Test
	@SuppressWarnings("deprecation")
	public void sendEvent() throws Exception {
		String key = URLEncoder.encode("SAS_KEY_GOES_HERE");
		String connectionString = "amqps://user:" + key + "@sitewhere.servicebus.windows.net";
		File file = File.createTempFile("qpid", "props");
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write("connectionfactory.SBCF = " + connectionString);
		writer.newLine();
		writer.write("queue.EVENTHUB = sitewhere");
		writer.newLine();
		writer.close();

		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.apache.qpid.amqp_1_0.jms.jndi.PropertiesFileInitialContextFactory");
		env.put(Context.PROVIDER_URL, file.getAbsolutePath());
		Context context = new InitialContext(env);

		// Lookup ConnectionFactory and Queue
		ConnectionFactory cf = (ConnectionFactory) context.lookup("SBCF");
		Destination eventhub = (Destination) context.lookup("EVENTHUB");

		// Create Connection
		Connection connection = cf.createConnection();

		// Create sender-side Session and MessageProducer
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer sender = session.createProducer(eventhub);

		for (int i = 0; i < 100; i++) {
			String messageId = "ID:bubba" + i;
			BytesMessage message = session.createBytesMessage();
			byte[] body = "Test".getBytes();
			message.writeBytes(body);
			message.setJMSMessageID(messageId);
			sender.send(message);
			System.out.println("Sending " + messageId);
		}

		sender.close();
		session.close();
		connection.close();
	}
}