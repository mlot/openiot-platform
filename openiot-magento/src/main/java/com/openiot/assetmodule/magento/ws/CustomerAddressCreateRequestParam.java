
/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.openiot.assetmodule.magento.ws;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sessionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="customerId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="addressData" type="{urn:Magento}customerAddressEntityCreate"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sessionId",
    "customerId",
    "addressData"
})
@XmlRootElement(name = "customerAddressCreateRequestParam")
public class CustomerAddressCreateRequestParam {

    @XmlElement(required = true)
    protected String sessionId;
    protected int customerId;
    @XmlElement(required = true)
    protected CustomerAddressEntityCreate addressData;

    /**
     * Gets the value of the sessionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the value of the sessionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionId(String value) {
        this.sessionId = value;
    }

    /**
     * Gets the value of the customerId property.
     * 
     */
    public int getCustomerId() {
        return customerId;
    }

    /**
     * Sets the value of the customerId property.
     * 
     */
    public void setCustomerId(int value) {
        this.customerId = value;
    }

    /**
     * Gets the value of the addressData property.
     * 
     * @return
     *     possible object is
     *     {@link CustomerAddressEntityCreate }
     *     
     */
    public CustomerAddressEntityCreate getAddressData() {
        return addressData;
    }

    /**
     * Sets the value of the addressData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CustomerAddressEntityCreate }
     *     
     */
    public void setAddressData(CustomerAddressEntityCreate value) {
        this.addressData = value;
    }

}
