
/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.assetmodule.magento.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for magentoInfoEntity complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="magentoInfoEntity">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="magento_version" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="magento_edition" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "magentoInfoEntity", propOrder = {
    "magentoVersion",
    "magentoEdition"
})
public class MagentoInfoEntity {

    @XmlElement(name = "magento_version", required = true)
    protected String magentoVersion;
    @XmlElement(name = "magento_edition", required = true)
    protected String magentoEdition;

    /**
     * Gets the value of the magentoVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMagentoVersion() {
        return magentoVersion;
    }

    /**
     * Sets the value of the magentoVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMagentoVersion(String value) {
        this.magentoVersion = value;
    }

    /**
     * Gets the value of the magentoEdition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMagentoEdition() {
        return magentoEdition;
    }

    /**
     * Sets the value of the magentoEdition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMagentoEdition(String value) {
        this.magentoEdition = value;
    }

}
