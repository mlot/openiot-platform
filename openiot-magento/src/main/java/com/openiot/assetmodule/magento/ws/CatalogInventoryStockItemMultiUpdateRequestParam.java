
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
 *         &lt;element name="productIds" type="{urn:Magento}ArrayOfString"/>
 *         &lt;element name="productData" type="{urn:Magento}catalogInventoryStockItemUpdateEntityArray"/>
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
    "productIds",
    "productData"
})
@XmlRootElement(name = "catalogInventoryStockItemMultiUpdateRequestParam")
public class CatalogInventoryStockItemMultiUpdateRequestParam {

    @XmlElement(required = true)
    protected String sessionId;
    @XmlElement(required = true)
    protected ArrayOfString productIds;
    @XmlElement(required = true)
    protected CatalogInventoryStockItemUpdateEntityArray productData;

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
     * Gets the value of the productIds property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getProductIds() {
        return productIds;
    }

    /**
     * Sets the value of the productIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setProductIds(ArrayOfString value) {
        this.productIds = value;
    }

    /**
     * Gets the value of the productData property.
     * 
     * @return
     *     possible object is
     *     {@link CatalogInventoryStockItemUpdateEntityArray }
     *     
     */
    public CatalogInventoryStockItemUpdateEntityArray getProductData() {
        return productData;
    }

    /**
     * Sets the value of the productData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CatalogInventoryStockItemUpdateEntityArray }
     *     
     */
    public void setProductData(CatalogInventoryStockItemUpdateEntityArray value) {
        this.productData = value;
    }

}
