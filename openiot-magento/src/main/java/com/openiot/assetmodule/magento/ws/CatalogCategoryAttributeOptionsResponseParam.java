
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
 *         &lt;element name="result" type="{urn:Magento}catalogAttributeOptionEntityArray"/>
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
    "result"
})
@XmlRootElement(name = "catalogCategoryAttributeOptionsResponseParam")
public class CatalogCategoryAttributeOptionsResponseParam {

    @XmlElement(required = true)
    protected CatalogAttributeOptionEntityArray result;

    /**
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link CatalogAttributeOptionEntityArray }
     *     
     */
    public CatalogAttributeOptionEntityArray getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link CatalogAttributeOptionEntityArray }
     *     
     */
    public void setResult(CatalogAttributeOptionEntityArray value) {
        this.result = value;
    }

}
