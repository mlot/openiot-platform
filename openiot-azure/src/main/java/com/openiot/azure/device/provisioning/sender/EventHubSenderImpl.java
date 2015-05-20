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

package com.openiot.azure.device.provisioning.sender;

import com.openiot.azure.device.provisioning.EventHubClientFactory;
import com.openiot.azure.device.provisioning.common.Constants;
import com.openiot.azure.device.provisioning.common.EventHubException;
import org.apache.qpid.amqp_1_0.client.Message;
import org.apache.qpid.amqp_1_0.type.Section;
import org.apache.qpid.amqp_1_0.type.messaging.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EventHubSenderImpl implements EventHubSender {
    private static final Logger logger = LoggerFactory.getLogger(EventHubSenderImpl.class);

    private final String connectionString;
    private final String entityPath;
    private final String partitionId;

    private EventHubMessageSender _sender;

    public EventHubSenderImpl(String connectionString, String entityName, String partitionId) {
        this.connectionString = connectionString;
        this.entityPath = entityName;
        this.partitionId = partitionId;
    }

    @Override
    public void open() throws EventHubException {
        logger.info("creating eventhub sender");
        long start = System.currentTimeMillis();

        EventHubClientFactory eventHubFactory = EventHubClientFactory.getInstance(connectionString, entityPath);
        _sender = eventHubFactory.createPartitionSender(this.partitionId);

        long end = System.currentTimeMillis();
        logger.info("created eventhub sender, time taken(ms): " + (end - start));
    }

    @Override
    public void close() {
        if (_sender != null) {
            _sender.close();
            logger.info("closed eventhub sender.");
            _sender = null;
        }
    }

    @Override
    public boolean isOpen() {
        return (_sender != null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void send(String data) throws EventHubException {

        Map value = new HashMap();
        value.put(Constants.AmqpPayloadKey, data);
        List<Section> sections = new ArrayList<>();
        sections.add( new ApplicationProperties( value ) );
        Message message = new Message(sections);

        boolean SWITCH = true;
        while (SWITCH){

            if (!isOpen()){ //retry in 5 seconds
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignore) {}
                continue;
            }
            try {
                _sender.send(message);
                break;
            } catch (EventHubException e) {
                logger.warn("Send message failed, trying to re-create sender.", e);
                reCreateSender();
            }
            try {Thread.sleep(5000);} catch (InterruptedException ignore) {}
        }
    }

    long lastRecreationTime = 0L;
    private synchronized void reCreateSender(){

        //No re-creation in 10s
        if (lastRecreationTime > 0 && System.currentTimeMillis() - lastRecreationTime < 10000)
            return;

        int MAX_RETRY_COUNT = 10;
        int retries = 0;
        while (retries < MAX_RETRY_COUNT){
            try {
                close();
            } catch (Exception ignore) {}

            try {
                open();
                break;
            } catch (EventHubException e) {
                logger.warn("re-creation failed.");
                retries ++;
                try {Thread.sleep(5000);} catch (InterruptedException ignore) {}
            }

        }

    }

}
