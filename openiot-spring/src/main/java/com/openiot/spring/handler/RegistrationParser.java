/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spring.handler;

import com.openiot.device.provisioning.RegistrationManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Parse elements related to device registration.
 * 
 * @author Derek
 */
public class RegistrationParser {

	/**
	 * Parse elements in the device registration section.
	 * 
	 * @param element
	 * @param context
	 * @return
	 */
	protected Object parse(Element element, ParserContext context) {
		List<Element> children = DomUtils.getChildElements(element);
		for (Element child : children) {
			Elements type = Elements.getByLocalName(child.getLocalName());
			if (type == null) {
				throw new RuntimeException("Unknown registration element: " + child.getLocalName());
			}
			switch (type) {
			case DefaultRegistrationManager: {
				return parseDefaultRegistrationManager(child, context);
			}
			case RegistrationManager: {
				return parseRegistrationManager(child, context);
			}
			}
		}
		return null;
	}

	/**
	 * Parse information for the default registration manager.
	 * 
	 * @param element
	 * @param context
	 * @return
	 */
	protected BeanDefinition parseDefaultRegistrationManager(Element element, ParserContext context) {
		BeanDefinitionBuilder manager = BeanDefinitionBuilder.rootBeanDefinition(RegistrationManager.class);

		Attr allowNewDevices = element.getAttributeNode("allowNewDevices");
		if (allowNewDevices != null) {
			manager.addPropertyValue("allowNewDevices", allowNewDevices.getValue());
		}

		Attr autoAssignSite = element.getAttributeNode("autoAssignSite");
		if (autoAssignSite != null) {
			manager.addPropertyValue("autoAssignSite", autoAssignSite.getValue());
		}

		Attr autoAssignToken = element.getAttributeNode("autoAssignToken");
		if (autoAssignToken != null) {
			manager.addPropertyValue("autoAssignToken", autoAssignToken.getValue());
		}

		return manager.getBeanDefinition();
	}

	/**
	 * Parse a registration manager reference.
	 * 
	 * @param element
	 * @param context
	 * @return
	 */
	protected RuntimeBeanReference parseRegistrationManager(Element element, ParserContext context) {
		Attr ref = element.getAttributeNode("ref");
		if (ref != null) {
			return new RuntimeBeanReference(ref.getValue());
		}
		throw new RuntimeException("Registration manager reference does not have ref defined.");
	}

	/**
	 * Expected child elements.
	 * 
	 * @author Derek
	 */
	public static enum Elements {

		/** Default registration manager */
		DefaultRegistrationManager("default-registration-manager"),

		/** Registration manager reference */
		RegistrationManager("registration-manager");

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