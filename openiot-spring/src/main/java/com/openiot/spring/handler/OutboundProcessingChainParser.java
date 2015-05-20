/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spring.handler;

import com.openiot.azure.device.provisioning.EventHubOutboundEventProcessor;
import com.openiot.device.event.processor.DefaultOutboundEventProcessorChain;
import com.openiot.device.provisioning.ProvisioningEventProcessor;
import com.openiot.geospatial.ZoneTest;
import com.openiot.geospatial.ZoneTestEventProcessor;
import com.openiot.hazelcast.HazelcastEventProcessor;
import com.openiot.hazelcast.OpenIoTHazelcastConfiguration;
import com.openiot.server.OpenIoTServerBeans;
import com.openiot.solr.OpenIoTSolrConfiguration;
import com.openiot.solr.SolrDeviceEventProcessor;
import com.openiot.spi.device.event.AlertLevel;
import com.openiot.spi.geospatial.ZoneContainment;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Parses configuration data from OpenIoT outbound processing chain section.
 * 
 * @author Derek
 */
public class OutboundProcessingChainParser extends AbstractBeanDefinitionParser {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.xml.AbstractBeanDefinitionParser#parseInternal
	 * (org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
	 */
	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext context) {
		BeanDefinitionBuilder chain =
				BeanDefinitionBuilder.rootBeanDefinition(DefaultOutboundEventProcessorChain.class);
		List<Element> dsChildren = DomUtils.getChildElements(element);
		List<Object> processors = new ManagedList<Object>();
		for (Element child : dsChildren) {
			Elements type = Elements.getByLocalName(child.getLocalName());
			if (type == null) {
				throw new RuntimeException("Unknown inbound processing chain element: "
						+ child.getLocalName());
			}
			switch (type) {
			case OutboundEventProcessor: {
				processors.add(parseOutboundEventProcessor(child, context));
				break;
			}
			case ZoneTestEventProcessor: {
				processors.add(parseZoneTestEventProcessor(child, context));
				break;
			}
			case HazelcastEventProcessor: {
				processors.add(parseHazelcastEventProcessor(child, context));
				break;
			}
			case SolrEventProcessor: {
				processors.add(parseSolrEventProcessor(child, context));
				break;
			}
			case AzureEventHubEventProcessor: {
				processors.add(parseAzureEventHubEventProcessor(child, context));
				break;
			}
			case ProvisioningEventProcessor: {
				processors.add(parseProvisioningEventProcessor(child, context));
				break;
			}
			}
		}
		chain.addPropertyValue("processors", processors);
		context.getRegistry().registerBeanDefinition(OpenIoTServerBeans.BEAN_OUTBOUND_PROCESSOR_CHAIN,
				chain.getBeanDefinition());
		return null;
	}

	/**
	 * Parse configuration for custom outbound event processor.
	 * 
	 * @param element
	 * @param context
	 * @return
	 */
	protected RuntimeBeanReference parseOutboundEventProcessor(Element element, ParserContext context) {
		Attr ref = element.getAttributeNode("ref");
		if (ref != null) {
			return new RuntimeBeanReference(ref.getValue());
		}
		throw new RuntimeException("Outbound event processor does not have ref defined.");
	}

	/**
	 * Parse configuration for event processor that tests location events against zone
	 * boundaries for firing alert conditions.
	 * 
	 * @param element
	 * @param context
	 * @return
	 */
	protected AbstractBeanDefinition parseZoneTestEventProcessor(Element element, ParserContext context) {
		BeanDefinitionBuilder processor =
				BeanDefinitionBuilder.rootBeanDefinition(ZoneTestEventProcessor.class);
		List<Element> children = DomUtils.getChildElementsByTagName(element, "zone-test");
		List<Object> tests = new ManagedList<Object>();
		for (Element testElm : children) {
			ZoneTest test = new ZoneTest();

			Attr zoneToken = testElm.getAttributeNode("zoneToken");
			if (zoneToken == null) {
				throw new RuntimeException("Zone test missing 'zoneToken' attribute.");
			}
			test.setZoneToken(zoneToken.getValue());

			Attr condition = testElm.getAttributeNode("condition");
			if (condition == null) {
				throw new RuntimeException("Zone test missing 'condition' attribute.");
			}
			ZoneContainment containment =
					(condition.getValue().equalsIgnoreCase("inside") ? ZoneContainment.Inside
							: ZoneContainment.Outside);
			test.setCondition(containment);

			Attr alertType = testElm.getAttributeNode("alertType");
			if (alertType == null) {
				throw new RuntimeException("Zone test missing 'alertType' attribute.");
			}
			test.setAlertType(alertType.getValue());

			Attr alertMessage = testElm.getAttributeNode("alertMessage");
			if (alertMessage == null) {
				throw new RuntimeException("Zone test missing 'alertMessage' attribute.");
			}
			test.setAlertMessage(alertMessage.getValue());

			Attr alertLevel = testElm.getAttributeNode("alertLevel");
			AlertLevel level = AlertLevel.Error;
			if (alertLevel != null) {
				level = convertAlertLevel(alertLevel.getValue());
			}
			test.setAlertLevel(level);

			tests.add(test);
		}
		processor.addPropertyValue("zoneTests", tests);
		return processor.getBeanDefinition();
	}

	protected AlertLevel convertAlertLevel(String input) {
		if (input.equalsIgnoreCase("info")) {
			return AlertLevel.Info;
		}
		if (input.equalsIgnoreCase("warning")) {
			return AlertLevel.Warning;
		}
		if (input.equalsIgnoreCase("error")) {
			return AlertLevel.Error;
		}
		if (input.equalsIgnoreCase("critical")) {
			return AlertLevel.Critical;
		}
		throw new RuntimeException("Invalid alert level value: " + input);
	}

	/**
	 * Parse configuration for Hazelcast outbound event processor.
	 * 
	 * @param element
	 * @param context
	 * @return
	 */
	protected AbstractBeanDefinition parseHazelcastEventProcessor(Element element, ParserContext context) {
		BeanDefinitionBuilder processor =
				BeanDefinitionBuilder.rootBeanDefinition(HazelcastEventProcessor.class);
		processor.addPropertyReference("configuration",
				OpenIoTHazelcastConfiguration.HAZELCAST_CONFIGURATION_BEAN);
		return processor.getBeanDefinition();
	}

	/**
	 * Parse configuration for Solr outbound event processor.
	 * 
	 * @param element
	 * @param context
	 * @return
	 */
	protected AbstractBeanDefinition parseSolrEventProcessor(Element element, ParserContext context) {
		BeanDefinitionBuilder processor =
				BeanDefinitionBuilder.rootBeanDefinition(SolrDeviceEventProcessor.class);
		processor.addPropertyReference("solr", OpenIoTSolrConfiguration.SOLR_CONFIGURATION_BEAN);
		return processor.getBeanDefinition();
	}

	/**
	 * Parses configuration for Azure EventHub event processor.
	 * 
	 * @param element
	 * @param context
	 * @return
	 */
	protected AbstractBeanDefinition parseAzureEventHubEventProcessor(Element element, ParserContext context) {
		BeanDefinitionBuilder processor =
				BeanDefinitionBuilder.rootBeanDefinition(EventHubOutboundEventProcessor.class);

		Attr sasKey = element.getAttributeNode("sasKey");
		if (sasKey == null) {
			throw new RuntimeException("SAS key required for Azure EventHub event processor.");
		}
		processor.addPropertyValue("sasKey", sasKey.getValue());

		Attr sasName = element.getAttributeNode("sasName");
		if (sasName == null) {
			throw new RuntimeException("SAS name required for Azure EventHub event processor.");
		}
		processor.addPropertyValue("sasName", sasName.getValue());

		Attr serviceBusName = element.getAttributeNode("serviceBusName");
		if (serviceBusName == null) {
			throw new RuntimeException("Service bus name required for Azure EventHub event processor.");
		}
		processor.addPropertyValue("serviceBusName", serviceBusName.getValue());

		Attr entityPath = element.getAttributeNode("entityPath");
		if (entityPath == null) {
			throw new RuntimeException("EntityPath name required for Azure EventHub event processor.");
		}
		processor.addPropertyValue("entityPath", entityPath.getValue());

		return processor.getBeanDefinition();
	}

	/**
	 * Parse configuration for event processor that routes traffic to provisioning
	 * subsystem.
	 * 
	 * @param element
	 * @param context
	 * @return
	 */
	protected AbstractBeanDefinition parseProvisioningEventProcessor(Element element, ParserContext context) {
		BeanDefinitionBuilder processor =
				BeanDefinitionBuilder.rootBeanDefinition(ProvisioningEventProcessor.class);

		Attr numThreads = element.getAttributeNode("numThreads");
		if (numThreads != null) {
			processor.addPropertyValue("numThreads", Integer.parseInt(numThreads.getValue()));
		}

		return processor.getBeanDefinition();
	}

	/**
	 * Expected child elements.
	 * 
	 * @author Derek
	 */
	public static enum Elements {

		/** Reference to custom inbound event processor */
		OutboundEventProcessor("outbound-event-processor"),

		/** Tests location values against zones */
		ZoneTestEventProcessor("zone-test-event-processor"),

		/** Sends outbound events over Hazelcast topics */
		HazelcastEventProcessor("hazelcast-event-processor"),

		/** Indexes outbound events in Apache Solr */
		SolrEventProcessor("solr-event-processor"),

		/** Sends outbound events to an Azure EventHub */
		AzureEventHubEventProcessor("azure-eventhub-event-processor"),

		/** Reference to custom inbound event processor */
		ProvisioningEventProcessor("provisioning-event-processor");

		/** Event code */
		private String localName;

		private Elements(String localName) {
			this.localName = localName;
		}

		public static Elements getByLocalName(String localName) {
			for (Elements value : Elements.values()) {
				if (value.getLocalName().equals(localName)) {
					return value;
				}
			}
			return null;
		}

		public String getLocalName() {
			return localName;
		}

		public void setLocalName(String localName) {
			this.localName = localName;
		}
	}
}
