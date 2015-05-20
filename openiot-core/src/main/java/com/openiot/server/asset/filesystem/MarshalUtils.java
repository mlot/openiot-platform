/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.server.asset.filesystem;

import com.openiot.rest.model.asset.HardwareAsset;
import com.openiot.rest.model.asset.LocationAsset;
import com.openiot.rest.model.asset.PersonAsset;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.asset.AssetType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for marshaling XML configuration files for assets.
 * 
 * @author Derek
 */
public class MarshalUtils {

	/**
	 * Loads a list of hardware assets from a file.
	 * 
	 * @param config
	 * @param type
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static List<HardwareAsset> loadHardwareAssets(File config, AssetType type)
			throws OpenIoTException {
		try {
			List<HardwareAsset> assets = new ArrayList<HardwareAsset>();
			JAXBContext jaxbContext = JAXBContext.newInstance(FileSystemHardwareAssets.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			FileSystemHardwareAssets xmlAssets =
					(FileSystemHardwareAssets) jaxbUnmarshaller.unmarshal(config);
			for (FileSystemHardwareAsset xmlAsset : xmlAssets.getHardwareAssets()) {
				HardwareAsset asset = new HardwareAsset();
				asset.setId(xmlAsset.getId());
				asset.setName(xmlAsset.getName());
				asset.setDescription(xmlAsset.getDescription());
				asset.setSku(xmlAsset.getSku());
				asset.setImageUrl(xmlAsset.getImageUrl());
				for (FileSystemAssetProperty xmlProperty : xmlAsset.getProperties()) {
					asset.setProperty(xmlProperty.getName(), xmlProperty.getValue());
				}
				if ((type == AssetType.Hardware) && (!xmlAsset.isDevice())) {
					assets.add(asset);
				}
				if ((type == AssetType.Device) && (xmlAsset.isDevice())) {
					assets.add(asset);
				}
			}
			return assets;
		} catch (Exception e) {
			throw new OpenIoTException("Unable to unmarshal assets file.", e);
		}
	}

	/**
	 * Loads a list of person assets from a file.
	 * 
	 * @param config
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static List<PersonAsset> loadPersonAssets(File config) throws OpenIoTException {
		List<PersonAsset> assets = new ArrayList<PersonAsset>();
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(FileSystemPersonAssets.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			FileSystemPersonAssets xmlAssets = (FileSystemPersonAssets) jaxbUnmarshaller.unmarshal(config);
			for (FileSystemPersonAsset xmlAsset : xmlAssets.getPersonAssets()) {
				PersonAsset asset = new PersonAsset();
				asset.setId(xmlAsset.getId());
				asset.setName(xmlAsset.getName());
				asset.setUserName(xmlAsset.getUserName());
				asset.setEmailAddress(xmlAsset.getEmailAddress());
				asset.setImageUrl(xmlAsset.getPhotoUrl());
				for (FileSystemAssetProperty xmlProperty : xmlAsset.getProperties()) {
					asset.setProperty(xmlProperty.getName(), xmlProperty.getValue());
				}
				if (xmlAsset.getRoles() != null) {
					List<String> roles = xmlAsset.getRoles().getRoles();
					for (String role : roles) {
						asset.getRoles().add(role);
					}
				}
				assets.add(asset);
			}
			return assets;
		} catch (Exception e) {
			throw new OpenIoTException("Unable to unmarshal person assets file.", e);
		}
	}

	/**
	 * Loads a list of location assets from a file.
	 * 
	 * @param config
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public static List<LocationAsset> loadLocationAssets(File config) throws OpenIoTException {
		List<LocationAsset> assets = new ArrayList<LocationAsset>();
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(FileSystemLocationAssets.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			FileSystemLocationAssets xmlAssets =
					(FileSystemLocationAssets) jaxbUnmarshaller.unmarshal(config);
			for (FileSystemLocationAsset xmlAsset : xmlAssets.getLocationAssets()) {
				LocationAsset asset = new LocationAsset();
				asset.setId(xmlAsset.getId());
				asset.setName(xmlAsset.getName());
				asset.setLatitude(xmlAsset.getLat());
				asset.setLongitude(xmlAsset.getLon());
				asset.setImageUrl(xmlAsset.getImageUrl());
				for (FileSystemAssetProperty xmlProperty : xmlAsset.getProperties()) {
					asset.setProperty(xmlProperty.getName(), xmlProperty.getValue());
				}
				assets.add(asset);
			}
			return assets;
		} catch (Exception e) {
			throw new OpenIoTException("Unable to unmarshal location assets file.", e);
		}
	}
}
