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

package com.openiot.azure.device.provisioning;

import com.openiot.azure.device.provisioning.common.ConnectionStringBuilder;
import com.openiot.azure.device.provisioning.common.EventHubException;
import com.openiot.azure.device.provisioning.receiver.EventHubConsumerGroup;
import com.openiot.azure.device.provisioning.receiver.SelectorFilterWriter;
import com.openiot.azure.device.provisioning.sender.EventHubMessageSender;
import com.openiot.azure.device.provisioning.sender.EventHubMessageSenderImpl;
import org.apache.qpid.amqp_1_0.client.Connection;
import org.apache.qpid.amqp_1_0.client.ConnectionErrorException;
import org.apache.qpid.amqp_1_0.client.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventHubClientFactory {

    private static final String DefaultConsumerGroupName = "$default";
    private static final Logger logger = LoggerFactory.getLogger(EventHubClientFactory.class);
    private static final long ConnectionSyncTimeout = 60000L;

    private final String connectionString;
    private final String entityPath;
    private final Connection connection;

    private EventHubClientFactory(String connectionString, String entityPath) throws EventHubException {
        this.connectionString = connectionString;
        this.entityPath = entityPath;
        this.connection = this.createConnection();
    }

    /**
     * creates a new instance of EventHubClient using the supplied connection string and entity path.
     *
     * @param connectionString connection string to the namespace of event hubs. connection string format:
     *                         amqps://{userId}:{password}@{namespaceName}.servicebus.windows.net
     * @param entityPath       the name of event hub entity.
     * @return EventHubClientFactory
     * @throws EventHubException
     */
    public static EventHubClientFactory getInstance(String connectionString, String entityPath) throws EventHubException {
        return new EventHubClientFactory(connectionString, entityPath);
    }


    public EventHubMessageSender createPartitionSender(String partitionId) throws EventHubException {
        try {
            return new EventHubMessageSenderImpl(this.connection.createSession(), this.entityPath, partitionId);
        } catch (ConnectionException e) {
            throw new EventHubException(e);
        }
    }

    public EventHubConsumerGroup createDefaultConsumerGroup() {
        return new EventHubConsumerGroup(this.connection, this.entityPath, DefaultConsumerGroupName);
    }

    public void close() {
        try {
            this.connection.close();
        } catch (ConnectionErrorException e) {
            logger.error(e.toString());
        }
    }

    private Connection createConnection() throws EventHubException {
        ConnectionStringBuilder connectionStringBuilder = new ConnectionStringBuilder(this.connectionString);
        Connection clientConnection;

        try {
            clientConnection = new Connection(
                    connectionStringBuilder.getHost(),
                    connectionStringBuilder.getPort(),
                    connectionStringBuilder.getUserName(),
                    connectionStringBuilder.getPassword(),
                    connectionStringBuilder.getHost(),
                    connectionStringBuilder.getSsl());
        } catch (ConnectionException e) {
            logger.error(e.toString());
            throw new EventHubException(e);
        }
        clientConnection.getEndpoint().setSyncTimeout(ConnectionSyncTimeout);
        SelectorFilterWriter.register(clientConnection.getEndpoint().getDescribedTypeRegistry());
        return clientConnection;
    }
}
