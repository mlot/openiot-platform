/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.model.device;

import com.openiot.rest.model.common.Location;
import com.openiot.rest.model.common.MetadataProviderEntity;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.common.ILocation;
import com.openiot.spi.device.IZone;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model object for a zone.
 * 
 * @author dadams
 */
public class Zone extends MetadataProviderEntity implements IZone, Serializable {

	/** Serialization version identifier */
	private static final long serialVersionUID = -5108019932881896046L;

	/** Unique zone token */
	private String token;

	/** Token for associated site */
	private String siteToken;

	/** Displayed name */
	private String name;

	/** Zone coordinates */
	private List<Location> coordinates = new ArrayList<Location>();

	/** Border color */
	private String borderColor;

	/** Fill color */
	private String fillColor;

	/** Opacity */
	private Double opacity;

	/*
	 * (non-Javadoc)
	 * 
	 * @see IZone#getToken()
	 */
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IZone#getSiteToken()
	 */
	public String getSiteToken() {
		return siteToken;
	}

	public void setSiteToken(String siteToken) {
		this.siteToken = siteToken;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IZone#getName()
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IZone#getCoordinates()
	 */
	@SuppressWarnings("unchecked")
	public List<ILocation> getCoordinates() {
		return (List<ILocation>) (List<? extends ILocation>) coordinates;
	}

	public void setCoordinates(List<Location> coordinates) {
		this.coordinates = coordinates;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IZone#getBorderColor()
	 */
	public String getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IZone#getFillColor()
	 */
	public String getFillColor() {
		return fillColor;
	}

	public void setFillColor(String fillColor) {
		this.fillColor = fillColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IZone#getOpacity()
	 */
	public Double getOpacity() {
		return opacity;
	}

	public void setOpacity(Double opacity) {
		this.opacity = opacity;
	}

	/**
	 * Create a copy of an SPI object. Used by web services for marshaling.
	 * 
	 * @param input
	 * @return
	 */
	public static Zone copy(IZone input) throws OpenIoTException {
		Zone result = new Zone();
		result.setToken(input.getToken());
		result.setSiteToken(input.getSiteToken());
		result.setName(input.getName());
		result.setCreatedDate(input.getCreatedDate());
		result.setBorderColor(input.getBorderColor());
		result.setFillColor(input.getFillColor());
		result.setOpacity(input.getOpacity());

		List<Location> coords = new ArrayList<Location>();
		for (ILocation location : input.getCoordinates()) {
			coords.add(Location.copy(location));
		}
		result.setCoordinates(coords);

		MetadataProviderEntity.copy(input, result);
		return result;
	}
}