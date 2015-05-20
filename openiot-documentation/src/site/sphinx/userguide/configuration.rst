====================
System Configuration
====================
OpenIoT uses a hierarchy of `Spring <http://projects.spring.io/spring-framework/>`_ XML files as
its configuration mechanism. When the OpenIoT server starts, one of the first steps is to bootstrap
the core system components by loading the *conf/openiot/openiot-server.xml* file.
Versions of OpenIoT prior to 0.9.7 only used the generic Spring beans schema for defining the core
list of beans needed to configure the server. Starting with version 0.9.7 OpenIoT has added a custom
XML schema that is more succinct and expressive. The schema provides shortcuts for many common 
configuration options while still allowing the user to extend the core architecture with custom
component implementations.

.. contents:: Contents
   :local:

--------------------------
Configuration Fundamentals
--------------------------
A valid OpenIoT configuration is based on a standard Spring beans XML file with an embedded section
that uses a schema specific to OpenIoT. The XML below is a partial configuration file illustrating some
of the key features. 

Notice the schema declarations and enclosing *<beans>* element at the top of the file. These are standard for a 
`Spring beans <http://docs.spring.io/spring-framework/docs/current/spring-framework-reference/html/beans.html>`_ 
configuration file. There is an *http://www.openiot.com/schema/openiot* namespace declared and
pointed to the OpenIoT schema for the 0.9.7 release. Often a new OpenIoT release will add
features to the schema, so it is important to point to the schema
for the version of OpenIoT being run on the server.

The *<sw:configuraton>* section contains all of the schema-based OpenIoT configuration elements. If a
schema-aware editor such as Eclipse is being used, the editor will provide syntax completion based on the 
OpenIoT schema. The OpenIoT schema contains many of the most often used building blocks for setting up
a OpenIoT server. It also allows for the introduction of user-defined component implementations. For example,
in the configuration below, the *<sw:outbound-event-processor>* contains a *ref* attribute that points to an
external Spring bean. By implementing components that conform to OpenIoT interfaces and plugging them in via
Spring beans, the system may be customized to add new behaviors. In this case, the system has been configured 
to broadcast all processed events via Hazelcast.

.. code-block:: xml

   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:context="http://www.springframework.org/schema/context" xmlns:sw="http://www.openiot.com/schema/openiot/ce"
      xsi:schemaLocation="
              http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
              http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.1.xsd
              http://www.springframework.org/schema/security http://www.springframework.org/schema/security/spring-security-3.0.xsd
              http://www.openiot.com/schema/openiot/ce http://www.openiot.org/schema/openiot/ce/1.0.1/openiot.xsd">
      
      <sw:configuration>
               
         <sw:outbound-processing-chain>
         
            <!-- Send outbound device events over Hazelcast -->
            <sw:outbound-event-processor ref="hazelcastDeviceEventProcessor"/>
   
         </sw:outbound-processing-chain>
   
      </sw:configuration>
   
      <!-- Provides access to a local Hazelcast instance for OpenIoT -->
      <bean id="hazelcastConfig" class="OpenIoTHazelcastConfiguration">
         <property name="configFileName" value="hazelcast.xml"/>
      </bean>
   
   
      <!-- Broadcasts OpenIoT state over Hazelcast -->
      <bean id="hazelcastDeviceEventProcessor" class="HazelcastEventProcessor">
         <property name="configuration" ref="hazelcastConfig"/>
      </bean>
   
   </beans>
   
Moving Sensitive Data Outside the Configuration
-----------------------------------------------
OpenIoT configuration files often contain login credentials or other information that should not
be shared with other users. Also, there are situations where settings for a system are 
environment-specific (production vs. staging vs. development) and maintaining a separate configuration 
for each creates extra work. Using Spring
`property placeholders <http://docs.spring.io/spring-framework/docs/current/spring-framework-reference/html/xsd-config.html#xsd-config-body-schemas-context-pphc>`_
allows sensitive data to be moved into an external properties file and injected at runtime.
In the following example, the hostname and port for the MongoDB datastore would be loaded from
the **openiot.properties** file in the same directory as the main configuration file.

.. code-block:: xml
   :emphasize-lines: 1, 14
   
   <context:property-placeholder location="file:${CATALINA_BASE}/conf/openiot/openiot.properties" ignore-resource-not-found="true"/>

   <!-- ########################### -->
   <!-- # SITEWHERE CONFIGURATION # -->
   <!-- ########################### -->
   <sw:configuration>
      
      <!-- ########################### -->
      <!-- # DATASTORE CONFIGURATION # -->
      <!-- ########################### -->
      <sw:datastore>
      
         <!-- Default MongoDB Datastore -->
         <sw:mongo-datastore hostname="${mongo.host}" port="${mongo.port}" databaseName="openiot"/>
 
The properties file would contain values for the placeholders as shown below:

.. code-block:: properties

   # OpenIoT configuration properties.
   mongo.host=localhost
   mongo.port=1234

-----------------------
Datastore Configuration
-----------------------
OpenIoT can use either `MongoDB <http://www.mongodb.org/>`_ or `Apache HBase <https://hbase.apache.org/>`_ for
underlying data storage. For small installations where extreme scalability is not needed, it is much quicker and 
easier to use MongoDB. For true "big data" applications, the HBase backend is the better choice. 

Configuring a MongoDB Datastore
-------------------------------
To use MongoDB as the backing datastore, edit the OpenIoT configuration *<sw:datastore>* section
and uncomment the *<sw:mongo-datastore>* element while leaving the *<sw:hbase-datastore>* element
commented as shown below:

.. code-block:: xml
   :emphasize-lines: 4, 7-9

	<sw:datastore>
	
		<!-- Default MongoDB Datastore -->
		<sw:mongo-datastore hostname="localhost" port="27017" databaseName="openiot"/>
	
		<!-- Default HBase Datastore -->
		<!--  
		<sw:hbase-datastore quorum="localhost"/>
		-->

Note that the default settings assume a local MongoDB instance running on the default port and using a database
named **openiot**.

The following attributes may be specified for the *<sw:mongo-datastore>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| hostname             | optional | Server hostname for MongoDB instance.            |
|                      |          | Defaults to *localhost*.                         |
+----------------------+----------+--------------------------------------------------+
| port                 | optional | Server port for MongoDB instance.                |
|                      |          | Defaults to *27017*.                             |
+----------------------+----------+--------------------------------------------------+
| databaseName         | optional | MongoDB database name for OpenIoT storage.     |
|                      |          | Defaults to *openiot*.                         |
+----------------------+----------+--------------------------------------------------+

Configuring an HBase Datastore
------------------------------
To use Apache HBase as the backing datastore, edit the OpenIoT configuration  *<sw:datastore>* section
and uncomment the *<sw:hbase-datastore>* element while leaving the *<sw:mongo-datastore>* element
commented as shown below:

.. code-block:: xml
   :emphasize-lines: 4-6, 9

	<sw:datastore>
	
		<!-- Default MongoDB Datastore -->
		<!--  
		<sw:mongo-datastore hostname="localhost" port="27017" databaseName="openiot"/>
		-->
	
		<!-- Default HBase Datastore -->
		<sw:hbase-datastore quorum="localhost"/>

Note that you will need to update the quorum address so that OpenIoT can locate your HBase cluster.

The following attributes may be specified for the *<sw:hbase-datastore>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| quorum               | required | Server hostname for HBase ZooKeeper quorum.      |
+----------------------+----------+--------------------------------------------------+

Populating Sample Data
----------------------
In both MongoDB and HBase installations, OpenIoT will automatically create the underlying database if it does
not already exist. After that, each time that OpenIoT server starts up, it will check whether there is data
in the database and, if data initializers are configured, will prompt to populate 
the database with sample data (for non-console startup, there are properties on the 
model initializers in the configuration file that allow you to specify whether 
to populate the sample data automatically). OpenIoT provides initializers that will
create sample data for both the user and device models. They can be configured by adding
the *<sw:default-device-model-initializer/>* and/or *<sw:default-user-model-initializer/>*
elements to the *<sw:datastore>* section as shown below:

.. code-block:: xml
   :emphasize-lines: 7, 10

		<sw:datastore>
		
			<!-- Default MongoDB Datastore -->
			<sw:mongo-datastore hostname="localhost" port="27017" databaseName="openiot"/>
			
			<!-- Initializes device model with sample data if datastore is empty -->
			<sw:default-device-model-initializer/>
			
			<!-- Initializes user model with sample data if datastore is empty -->
			<sw:default-user-model-initializer/>
 
It is usually a good choice to allow the user model to be populated since a valid user and permissions 
are required to log in to the management application. Populating the sample device data gives a nice 
starting point for understanding OpenIoT in the context of a real application.

Device Management Cache Providers
---------------------------------
Many elements of the device data model do not change often and can benefit from a caching implementation.
OpenIoT offers a service provider interface
`IDeviceManagementCacheProvider <../apidocs/com/openiot/spi/device/IDeviceManagementCacheProvider.html>`_
which may be implemented to provide caching capabilities that use an external cache provider.
OpenIoT offers a default device management cache implementation based on `Ehcache <http://ehcache.org/>`_
which can be configured as shown below:

.. code-block:: xml
   :emphasize-lines: 7

	<sw:datastore>
	
		<!-- Default MongoDB Datastore -->
		<sw:mongo-datastore hostname="localhost" port="27017" databaseName="openiot"/>
		
		<!-- Improves performance by using EHCache to store device management entities -->
		<sw:ehcache-device-management-cache/>

Note that removing the cache will result in noticeably slower performance since the underlying
service provider implementations will load all data from the datastore.

-------------------
Device Provisioning
-------------------
In OpenIoT, the term **provisioning** refers to the subsystem that communicates with devices.
On the inbound side, device data is brought in to the system via **event sources**. The inbound 
data is converted into OpenIoT events and passed in to the **inbound processing chain** by
the **inbound processing strategy**. On the outbound side (as part of the outbound processing 
chain) commands are sent to external devices via **command destinations**. An **outbound 
command router** makes the choice of which command destination will be used to deliver the 
command payload.

Event Sources
-------------
Event sources are responsible for bringing data into OpenIoT. All event sources implement the
`IInboundEventSource <../apidocs/com/openiot/spi/device/provisioning/IInboundEventSource.html>`_
interface and are composed of one or more **event receivers** (implementing 
`IInboundEventReceiver <../apidocs/com/openiot/spi/device/provisioning/IInboundEventReceiver.html>`_)
and a single **event decoder** (implementing 
`IDeviceEventDecoder <../apidocs/com/openiot/spi/device/provisioning/IDeviceEventDecoder.html>`_).
Event receivers take care of dealing with protocols for gathering data. The data is then processed
by the event decoder in order to create OpenIoT events which provide a common representation of
the device data so it can be processed by the inbound processing chain.

MQTT Event Source
*****************
Since consuming MQTT data is common in IoT applications, OpenIoT includes a component that
streamlines the process. In the example below, an event source is configured to listen for messages
on the given topic, then use the *<sw:protobuf-event-decoder/>* to decode the message payload 
using the standard OpenIoT Google Protocol Buffers message format.

.. code-block:: xml
   :emphasize-lines: 7-10

   <sw:provisioning>
	
      <!-- Inbound event sources -->
      <sw:event-sources>

         <!-- Event source for protobuf messages over MQTT -->
         <sw:mqtt-event-source sourceId="protobuf" hostname="localhost"
            port="1883" topic="OpenIoT/input/protobuf">
            <sw:protobuf-event-decoder/>
        </sw:mqtt-event-source>

The following attributes may be specified for the *<sw:mqtt-event-source>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| sourceId             | required | Unique event source id.                          |
+----------------------+----------+--------------------------------------------------+
| hostname             | required | MQTT broker server hostname or IP address.       |
+----------------------+----------+--------------------------------------------------+
| port                 | required | MQTT broker server port.                         |
+----------------------+----------+--------------------------------------------------+
| topic                | required | MQTT topic where devices will post events.       |
+----------------------+----------+--------------------------------------------------+

ActiveMQ Event Source
*********************
`Apache ActiveMQ <http://activemq.apache.org/>`_ is an open source messaging platform
that supports many wire formats such as AMQP, OpenWire, XMPP, and MQTT. It also supports
the standard Java JMS APIs for message processing. OpenIoT includes an event source
that creates an embedded ActiveMQ broker that listens on a configured transport. A
multithreaded pool of consumers listen on a configured topic and hand off the binary
payload to the configured decoder.

.. code-block:: xml
   :emphasize-lines: 7-10

   <sw:provisioning>
   
      <!-- Inbound event sources -->
      <sw:event-sources>

         <!-- Event source for protobuf messages over ActiveMQ queue -->
         <sw:activemq-event-source sourceId="activemq" transportUri="tcp://localhost:1234"
            queueName="SITEWHERE.IN" numConsumers="150">
            <sw:protobuf-event-decoder/>
         </sw:activemq-event-source>
         
The example above listens for JMS connections over TCP/IP with 150 consumer threads that 
read data from the configured queue, decode the data using OpenIoT Google Protocol Buffers
format, then send the decoded events to be processed.

The following attributes may be specified for the *<sw:activemq-event-source>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| sourceId             | required | Unique event source id.                          |
+----------------------+----------+--------------------------------------------------+
| transportUri         | required | Configures the ActiveMQ transport that will be   |
|                      |          | made available for clients to connect to.        |
+----------------------+----------+--------------------------------------------------+
| queueName            | required | Queue that external clients post events to.      |
+----------------------+----------+--------------------------------------------------+
| numConsumers         | optional | Number of threaded consumers used to process     |
|                      |          | data from the queue. Defaults to *3*.            |
+----------------------+----------+--------------------------------------------------+

Socket Event Source
*******************
Many devices connect over direct socket connections to report events. For instance, many
GPS trackers have cellular connectivity and report location or other events over GPRS.
The *<sw:socket-event-source/>* can be used to create a server socket which listens
on a given port, receiving client connections and processing them using a multithreaded
approach. Socket interactions are often complex and stateful, so the processing is
delegated to an implementation of 
`ISocketInteractionHandler <../apidocs/com/openiot/spi/device/provisioning/socket/ISocketInteractionHandler.html>`_
which handles the conversation between device and server. The socket interaction handler
returns a payload which is passed to the configured decoder to build OpenIoT events.

.. code-block:: xml
   :emphasize-lines: 7-10

   <sw:provisioning>
   
      <!-- Inbound event sources -->
      <sw:event-sources>

         <!-- Event source for protobuf messages from socket connections -->
         <sw:socket-event-source port="8585" numThreads="10" sourceId="socket">
            <sw:read-all-interaction-handler-factory/>
            <sw:protobuf-event-decoder/>
         </sw:socket-event-source>

Configuring the *<sw:read-all-interaction-handler-factory/>* reads all of the input from
the client socket and passes the binary information to the configured decoder. In some cases
(such as sending payloads in the standard OpenIoT Google Protocol Buffers format) this
is sufficient. However, in most cases, the user will need to create an interaction handler that
understands the conversational logic between the device and server. A custom implementation
can be referenced by using the *<sw:interaction-handler-factory/>* element
which references a Spring bean that contains the socket interaction handler factory. The factory implements the
`ISocketInteractionHandlerFactory <../apidocs/com/openiot/spi/device/provisioning/socket/ISocketInteractionHandlerFactory.html>`_
interface and creates instances of the socket interaction handler that manages device 
conversation.

The following attributes may be specified for the *<sw:socket-event-source>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| sourceId             | required | Unique event source id.                          |
+----------------------+----------+--------------------------------------------------+
| port                 | optional | Server port to listen on. Defaults to *8484*.    |
+----------------------+----------+--------------------------------------------------+
| numThreads           | required | Number of threads used to process client         |
|                      |          | requests. Defaults to *5*.                       |
+----------------------+----------+--------------------------------------------------+

Custom Event Source
*******************
In cases where a custom protocol is needed to support inbound events for devices, OpenIoT makes
it easy to plug in a custom event source. The custom event source class must implement the
`IInboundEventSource <../apidocs/com/openiot/spi/device/provisioning/IInboundEventSource.html>`_
interface. OpenIoT provides base classes that provide much of the common event source
functionality. For instance the BinaryInboundEventSource found
in openiot-core provides an event source that deals with binary data. By creating an instance
of BinaryInboundEventSource and plugging in a custom 
`IInboundEventReceiver <../apidocs/com/openiot/spi/device/provisioning/IInboundEventReceiver.html>`_
and `IDeviceEventDecoder <../apidocs/com/openiot/spi/device/provisioning/IDeviceEventDecoder.html>`_
implementation, the behavior can be completely customized. The event receiver takes care of receiving
binary data from the device and the decoder converts the data into OpenIoT events that can be
processed.

.. code-block:: xml
   :emphasize-lines: 7

   <sw:provisioning>
   
      <!-- Inbound event sources -->
      <sw:event-sources>

         <!-- Custom event source referencing a Spring bean -->
         <sw:event-source ref="customEventSourceBean"/>

The following attributes may be specified for the *<sw:event-source>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| ref                  | required | Reference to externally defined Spring bean      |
+----------------------+----------+--------------------------------------------------+

Inbound Processing Strategy
---------------------------
The inbound processing strategy is responsible for moving events from event sources into the
inbound processing chain. It is responsible for handling threading and reliably delivering
events for processing. An inbound processing strategy must implement the 
`IInboundProcessingStrategy <../apidocs/com/openiot/spi/device/provisioning/IInboundProcessingStrategy.html>`_
interface.

Default Inbound Processing Strategy
***********************************
The default inbound processing strategy for OpenIoT CE uses a bounded queue to hold events
being delivered from event sources. It creates a thread pool that consumes the queue to 
deliver events to the inbound processing chain. If events are delivered faster than the thread
pool can process them, the queue will eventually start blocking the event receiver threads.
Increasing the number of threads for event processing takes load from the queue but increases
processing load on the core system. OpenIoT CE does not persist the inbound queue, so shutting
down the server may result in data loss. OpenIoT EE offers a more advanced inbound processing
strategy implementation with persistent queues and transactional semantics.

.. code-block:: xml
   :emphasize-lines: 5-6

   <sw:provisioning>
   
         <!-- Inbound Processing Strategy -->
         <sw:inbound-processing-strategy>
            <sw:default-inbound-processing-strategy
               numEventProcessorThreads="150" enableMonitoring="true" monitoringIntervalSec="1"/>
         </sw:inbound-processing-strategy>

The following attributes may be specified for the *<sw:default-inbound-processing-strategy>* element.
      
+--------------------------+----------+----------------------------------------------------+
| Attribute                | Required | Description                                        |
+==========================+==========+====================================================+
| numEventProcessorThreads | optional | Number of threads used to process incoming events. |
|                          |          | Defaults to *100*.                                 |
+--------------------------+----------+----------------------------------------------------+
| enableMonitoring         | optional | Enables monitoring of event processing in the log. |
|                          |          | Defaults to *false*.                               |
+--------------------------+----------+----------------------------------------------------+
| monitoringIntervalSec    | optional | Interval (in seconds) at which monitoring messages |
|                          |          | are posted. Defaults to *5*.                       |
+--------------------------+----------+----------------------------------------------------+

Batch Operation Manager
-----------------------
The batch operation manager is responsible for asynchronously processing operations that 
are applied to many devices. Batch operations can be submitted via the administrative
console or via the REST services. The batch operation manager cycles through the list 
of batch operation elements, executing each and keeping state regarding progress of
execution. The default batch operation manager can be configured by using the
*<sw:default-batch-operation-manager>* element as shown below.

.. code-block:: xml
   :emphasize-lines: 5

   <sw:provisioning>
               
      <!-- Batch operation management -->
      <sw:batch-operations>
         <sw:default-batch-operation-manager throttleDelayMs="10000"/>
      </sw:batch-operations>

The throttle delay value can be used to slow down the rate that elements are processed
so that the system is not overloaded by large operations.
      
A custom batch operation manager can be added by creating a class that implements
`IBatchOperationManager <../apidocs/com/openiot/spi/device/batch/IBatchOperationManager.html>`_
and adding a reference to it using the *<sw:batch-operation-manager>* element.

The following attributes may be specified for the *<sw:default-batch-operation-manager>* element.
      
+--------------------------+----------+----------------------------------------------------+
| Attribute                | Required | Description                                        |
+==========================+==========+====================================================+
| throttleDelayMs          | optional | Number of milliseconds to wait between processing  |
|                          |          | batch operation elements. Defaults to *0*.         |
+--------------------------+----------+----------------------------------------------------+

Command Destinations
--------------------
Command destinations are responsible for delivering commands to devices. All command destinations implement the
`ICommandDestination <../apidocs/com/openiot/spi/device/provisioning/ICommandDestination.html>`_
interface and are composed of a **command encoder** (implementing 
`ICommandExecutionEncoder <../apidocs/com/openiot/spi/device/provisioning/ICommandExecutionEncoder.html>`_),
a **parameter extractor** (implementing
`ICommandDeliveryParameterExtractor <../apidocs/com/openiot/spi/device/provisioning/ICommandDeliveryParameterExtractor.html>`_),
and a **delivery provider** (implementing 
`ICommandDeliveryProvider <../apidocs/com/openiot/spi/device/provisioning/ICommandDeliveryProvider.html>`_).
The command encoder is used to convert the command payload into a format understood by the device. The parameter
extractor pulls information needed for delivering the message to the delivery provider (e.g. for an SMS provider,
the extractor may pull the SMS phone number for the device from device metadata). The delivery provider takes 
the encoded payload and extracted parameters, then delivers the message to the device.

MQTT Command Destination
************************
For devices that listen on an MQTT topic for commands, the *<sw:mqtt-command-destination>* element can 
be used to easily configure a destination. An encoder and parameter extractor should be configured
based on the expected command format and location of MQTT routing information. The 
*<sw:hardware-id-topic-extractor>* element configures the MQTT topics for delivery based
on an expression that includes the hardware id of the device to be addressed. In cases where this
is not appropriate, a custom parameter extractor can be injected instead.

.. code-block:: xml
   :emphasize-lines: 7-12

   <sw:provisioning>
					
      <!-- Outbound command destinations -->
      <sw:command-destinations>

         <!-- Delivers commands via MQTT -->
         <sw:mqtt-command-destination destinationId="default"
            hostname="localhost" port="1883">
            <sw:protobuf-command-encoder/>
            <sw:hardware-id-topic-extractor commandTopicExpr="OpenIoT/commands/%s"
               systemTopicExpr="OpenIoT/system/%s"/>
         </sw:mqtt-command-destination>

The following attributes may be specified for the *<sw:mqtt-command-destination>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| destinationId        | required | Unique id for destination.                       |
+----------------------+----------+--------------------------------------------------+
| hostname             | required | MQTT broker hostname.                            |
+----------------------+----------+--------------------------------------------------+
| port                 | required | MQTT broker port.                                |
+----------------------+----------+--------------------------------------------------+

Twilio Command Destination
**************************
For devices that receive commands via SMS messages, the *<sw:twilio-command-destination>* may be used to
deliver the command via the `Twilio <https://www.twilio.com/>`_ online service. To use the service you will
need to create a Twilio account and pay for the outbound SMS service (including a phone number that
messages will be sent from).

.. code-block:: xml
   :emphasize-lines: 7-12

   <sw:provisioning>
					
      <!-- Outbound command destinations -->
      <sw:command-destinations>

         <!-- Delivers commands via Twilio SMS messages -->
         <sw:twilio-command-destination destinationId="laipac"
            accountSid="${twilio.account.sid}" authToken="${twilio.auth.token}" 
            fromPhoneNumber="${twilio.from.phone.number}">
            <sw:protobuf-command-encoder/>
            <sw:parameter-extractor ref="laipacExtractor"/>
         </sw:twilio-command-destination>
				
The account SID, auth token, and sending phone number are all pieces of data related to the Twilio account.
The parameter extractor implementation should be one that supplies parameters of type 
SmsParameters which is used by the delivery provider to determine the SMS phone number 
to deliver the command to.

The following attributes may be specified for the *<sw:twilio-command-destination>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| destinationId        | required | Unique id for destination.                       |
+----------------------+----------+--------------------------------------------------+
| accountSid           | required | Twilio account SID (from Twilio website).        |
+----------------------+----------+--------------------------------------------------+
| authToken            | required | Twilio account auth token (from Twilio website). |
+----------------------+----------+--------------------------------------------------+
| fromPhoneNumber      | required | Twilio phone number used to originate SMS.       |
+----------------------+----------+--------------------------------------------------+

------------------------
Inbound Processing Chain
------------------------
After data has been decoded into OpenIoT device events by event sources, the
inbound processing strategy queues up events to be processed by the 
**inbound processing chain**. The chain is a series of **inbound event processors** (implementing 
`IInboundEventProcessor <../apidocs/com/openiot/spi/device/event/processor/IInboundEventProcessor.html>`_)
that each handle the inbound events in series. New inbound event processors can be added to the chain to augment
the existing functionality. For instance, a metrics processor could keep count of events processed per second. 

**Since REST calls (or other calls that directly invoke the device management APIs) do not enter the system via event sources, 
they are not processed by the inbound processing chain.**


Default Event Storage Processor
-------------------------------
By default, an instance of *<sw:default-event-storage-processor/>* is configured in the chain. This processor
takes care of persisting device events via the device management service provider interfaces. If this 
processor is removed, events will not be stored and devices will not be registered. The default configuration
is shown below:

.. code-block:: xml
   :emphasize-lines: 6

		<sw:provisioning>
					
			<sw:inbound-processing-chain>
				
				<!-- Store events and delegate to registration manager -->
				<sw:default-event-storage-processor/>
	
			</sw:inbound-processing-chain>

-------------------------
Outbound Processing Chain
-------------------------
In the default provisioning implementation, each time an event is saved via the device management 
service provider interfaces, the outbound event processing chain is invoked. In the same way the 
inbound processing chain acts on unsaved inbound event data, the oubound processing chain acts on 
data that has been successfully persisted to the datastore. Each **outbound event processor** (implementing 
`IOutboundEventProcessor <../apidocs/com/openiot/spi/device/event/processor/IOutboundEventProcessor.html>`_)
is executed in series. New outbound event processors can be added to the chain to augment existing
functionality. For instance, OpenIoT has an event processor for sending all outbound events to
Hazelcast subscribers, allowing external clients to act on the events.

**REST calls (or other calls that directly invoke the device management APIs) are processed by the
outbound processing chain in the same manner as events from event sources.**

Provisioning Event Processor
----------------------------
By default, an instance of *<sw:provisioning-event-processor/>* is configured in the outbound chain. This
processor hands off device command invocations to the provisioning subsystem for processing. If this 
processor is removed, device command invocations will be persisted, but will never be processed. The
default configuration is shown below:

.. code-block:: xml
   :emphasize-lines: 6

   <sw:provisioning>
					
      <sw:outbound-processing-chain>
			
         <!-- Routes commands for provisioning -->
         <sw:provisioning-event-processor/>
				
         <!-- Send outbound device events over Hazelcast -->
         <sw:outbound-event-processor ref="hazelcastDeviceEventProcessor"/>
	
      </sw:outbound-processing-chain>

This example also shows the addition of a custom outbound event processor which references a Spring bean
defined elsewhere in the configuration. Events will be passed to the custom processor after they have
been processed by the provisioning processor.

Zone Test Event Processor
-------------------------
The *<sw:zone-test-event-processor/>* outbound event processor is used to test location events against
a list of predefined zones to verify if they fall within the zone boundaries. Each location event is
tested against the conditions defined in the list of *<sw:zone-test/>* elements. The zone tests
specify the unique token of the zone to test against (defined via the admin interface or REST services)
and the test condition (inside or outside the zone). If the condition is met, a new alert event is 
created based on the alert attributes in the test. The alert event can be processed like any other
alert entering the system, allowing other outbound processing components to handle reaction to the
zone condition.

.. code-block:: xml
   :emphasize-lines: 9-12
 
   <sw:provisioning>
   
      <sw:outbound-processing-chain>
      
         <!-- Routes commands for provisioning -->
         <sw:provisioning-event-processor/>
         
         <!-- Performs zone checking for locations -->
         <sw:zone-test-event-processor>
            <sw:zone-test zoneToken="777fa4e5-bc2f-458b-9968-b598b2e2d2ca" condition="outside"
               alertLevel="error" alertType="off.site" alertMessage="Asset has left the worksite."/>
         </sw:zone-test-event-processor>

In the example above, each location will be checked against the zone defined by the given zone token.
If the location is outside the given zone (in this case the worksite where an asset is deployed), an
alert is fired. The alert is an error of type 'off.site' an includes an alert message. If an asset 
goes offsite, the alert event can be used for reactions such as firing an SMS message or sending 
an audible alarm to a device on the worksite.
 
The following attributes may be specified for the *<sw:zone-test>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| zoneToken            | required | Unique token for zone to test.                   |
+----------------------+----------+--------------------------------------------------+
| condition            | required | Condition for test.                              |
|                      |          | Either *inside* or *outside*.                    |
+----------------------+----------+--------------------------------------------------+
| alertType            | required | Alert type for generated alert.                  |
+----------------------+----------+--------------------------------------------------+
| alertLevel           | optional | Alert level for generated alert.                 |
|                      |          | Defaults to *error*.                             |
+----------------------+----------+--------------------------------------------------+
| alertMessage         | required | Alert message for generated alert.               |
+----------------------+----------+--------------------------------------------------+

Sending Events to an Azure Event Hub
------------------------------------
The *<sw:azure-eventhub-event-processor/>* outbound event processor connects to an 
`Azure Event Hub <http://azure.microsoft.com/en-us/services/event-hubs/>`_ and forwards
device events to it. The current implementation sends all events in JSON format. Future
implementations will allow for filtering which events are sent and choosing the wire 
format of the event data. An Azure Event Hub outbound event
processor can be figured as shown below:

.. code-block:: xml
   :emphasize-lines: 3-4
   
   <sw:outbound-processing-chain>
      
      <sw:azure-eventhub-event-processor sasKey="{azure.sas.key}"
         sasName="default" serviceBusName="openiot.servicebus.windows.net" entityPath="openiot"/>

   </sw:outbound-processing-chain>

Note that a SAS name and key are required in order to connect to the Event Hub. See
`this <https://msdn.microsoft.com/en-us/library/azure/dn170477.aspx>`_ article to find
more information about Shared Access Signatures.

The following attributes may be specified for the *<sw:azure-eventhub-event-processor>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| serviceBusName       | required | Name of the service bus where the event hub      |
|                      |          | is configured.                                   |
+----------------------+----------+--------------------------------------------------+
| entityPath         | required | Name of the event hub to connect to.             |
+----------------------+----------+--------------------------------------------------+
| sasName              | required | Name of SAS entity to connect with.              |
+----------------------+----------+--------------------------------------------------+
| sasKey               | required | Key for SAS entity to connect with.              |
+----------------------+----------+--------------------------------------------------+

Broadcasting Events via Hazelcast
---------------------------------
OpenIoT has support for broadcasting events over `Hazelcast <http://hazelcast.com/>`_ topics, making it
easy to share events with external agents. To enable Hazelcast broadcasting, first add the configuration
information to the *<sw:globals>* section as shown below:

.. code-block:: xml
   :emphasize-lines: 4
   
   <sw:configuration>

      <sw:globals>
         <sw:hazelcast-configuration configFileLocation="${CATALINA_BASE}/conf/openiot/hazelcast.xml"/>
      </sw:globals>

Note that the *configFileLocation* attribute specifies the full path to a Hazelcast configuration file.
The configuration above is the default which assumes OpenIoT is running inside a Tomcat container.
Once the configuration has been declared, it may be referenced as part of the outbound processing chain to
enable broadcasting of events.

.. code-block:: xml
   :emphasize-lines: 7
   
   <sw:outbound-processing-chain>
      
      <!-- Routes commands for provisioning -->
      <sw:provisioning-event-processor/>

      <!-- Send outbound device events over Hazelcast -->
      <sw:hazelcast-event-processor/>

   </sw:outbound-processing-chain>

To consume events from the Hazelcast topics, listen on the topic names as defined in 
`IOpenIoTHazelcast <../apidocs/com/openiot/spi/server/hazelcast/IOpenIoTHazelcast.html>`_.

Sending Events to Apache Solr
-----------------------------
OpenIoT supports forwarding events to `Apache Solr <http://lucene.apache.org/solr/>`_ to leverage
the sophisticated search and analytics features it provides. The Solr outbound event processor uses
the `Solrj <https://cwiki.apache.org/confluence/display/solr/Using+SolrJ>`_ library to send each
outbound event to a Solr instance. The events are stored using a custom OpenIoT document schema,
allowing event data to be indexed based on its type. For instance, location events are stored with
geospatial indexes to allow efficient location searches. To enable the Solr event processor, first add the configuration
information to the *<sw:globals>* section as shown below:

.. code-block:: xml
   :emphasize-lines: 5
   
   <sw:configuration>

      <sw:globals>
         <sw:hazelcast-configuration configFileLocation="${CATALINA_BASE}/conf/openiot/hazelcast.xml"/>
         <sw:solr-configuration solrServerUrl="http://localhost:8983/solr/OpenIoT"/>
      </sw:globals>

The **solrServerUrl** attribute needs to point to the Solr core being used for OpenIoT data. To
add the outbound event processor to the chain, reference it as shown below:

.. code-block:: xml
   :emphasize-lines: 7
   
   <sw:outbound-processing-chain>
		
      <!-- Routes commands for provisioning -->
      <sw:provisioning-event-processor/>
			
      <!-- Index events in Solr -->
      <sw:solr-event-processor/>

   </sw:outbound-processing-chain>

Note that on system startup, the event processor attempts to ping the Solr server to verify the 
settings are correct. If the ping fails, server startup will fail.

-------------------
Configuring Logging
-------------------
OpenIoT uses `Apache Log4j <http://logging.apache.org/log4j/1.2/>`_ for logging information about the running system.
The logging output is configured by the **log4j.xml** file which is found in the lib folder of the default server
distributions. For users running OpenIoT on their own application server instance, the default logging configuration
file can be found on `GitHub <https://github.com/openiot/openiot/blob/master/openiot-core/config/log4j.xml>`_.
The file must be available on the server classpath in order to be used.

The default logging configuration file logs to the console output and also creates a separate log file named
**openiot.log** which contains the same content.

Enabling Server Debug Output
----------------------------
By default, most debugging output is not logged for OpenIoT. To turn debugging on for all aspects of the server,
scroll down to the following block:

.. code-block:: xml
   
   <category name="com.openiot">
      <priority value="INFO" />
   </category>

Change the **INFO** value to **DEBUG** and restart the server. All debug information will be now be available. This is
discouraged in production environments because logging takes system resources and will degrade performance.

Debugging Device Provisioning
-----------------------------
Debugging can also be enabled just for certain areas of the system. A common area where users require detailed
debugging information is in the provisioning of device data. It is often helpful to see exactly what OpenIoT is
doing to handle inbound and outbound data. To turn on provisioning debugging, scroll down to the following block in
the **log4j.xml** file:

.. code-block:: xml
   
   <category name="com.openiot.device.provisioning">
      <priority value="INFO" />
   </category>

Update the **INFO** value to **DEBUG** and restart the server to see more detailed provisioning information.
