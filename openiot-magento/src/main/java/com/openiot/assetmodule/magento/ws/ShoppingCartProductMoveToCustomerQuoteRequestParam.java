
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
 *         &lt;element name="quoteId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="productsData" type="{urn:Magento}shoppingCartProductEntityArray"/>
 *         &lt;element name="store" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "quoteId",
    "productsData",
    "store"
})
@XmlRootElement(name = "shoppingCartProductMoveToCustomerQuoteRequestParam")
public class ShoppingCartProductMoveToCustomerQuoteRequestParam {

    @XmlElement(required = true)
    protected String sessionId;
    protected int quoteId;
    @XmlElement(required = true)
    protected ShoppingCartProductEntityArray productsData;
    protected String store;

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
     * Gets the value of the quoteId property.
     * 
     */
    public int getQuoteId() {
        return quoteId;
    }

    /**
     * Sets the value of the quoteId property.
     * 
     */
    public void setQuoteId(int value) {
        this.quoteId = value;
    }

    /**
     * Gets the value of the productsData property.
     * 
     * @return
     *     possible object is
     *     {@link ShoppingCartProductEntityArray }
     *     
     */
    public ShoppingCartProductEntityArray getProductsData() {
        return productsData;
    }

    /**
     * Sets the value of the productsData property.
     * 
     * @param value
     *     allowed object is
     *     {@link ShoppingCartProductEntityArray }
     *     
     */
    public void setProductsData(ShoppingCartProductEntityArray value) {
        this.productsData = value;
    }

    /**
     * Gets the value of the store property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStore() {
        return store;
    }

    /**
     * Sets the value of the store property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStore(String value) {
        this.store = value;
    }

}
