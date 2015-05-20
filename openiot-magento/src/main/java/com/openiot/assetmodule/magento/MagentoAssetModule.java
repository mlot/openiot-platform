/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.assetmodule.magento;

import com.openiot.assetmodule.magento.ws.*;
import com.openiot.rest.model.asset.HardwareAsset;
import com.openiot.rest.model.command.CommandResponse;
import com.openiot.server.asset.AssetMatcher;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.asset.AssetType;
import com.openiot.spi.asset.IAssetModule;
import com.openiot.spi.command.CommandResult;
import com.openiot.spi.command.ICommandResponse;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.log4j.Logger;

import javax.xml.ws.soap.SOAPFaultException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Asset module that interacts with an external Magento server.
 * 
 * @author dadams
 */
public class MagentoAssetModule extends LifecycleComponent implements IAssetModule<HardwareAsset> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(MagentoAssetModule.class);

	/** Module id */
	private static final String MODULE_ID = "magento";

	/** Module name */
	private static final String MODULE_NAME = "Magento Identity Management";

	/** Default base URL for Magento SOAP v2 web service */
	private static final String DEFAULT_URL = "http://locahost/magento/index.php/api/v2_soap?wsdl";

	/** Default Magento username */
	private static final String DEFAULT_USERNAME = "magento";

	/** Default Magento password */
	private static final String DEFAULT_PASSWORD = "magento";

	/** Number of threads used to make calls to Magento */
	private static final int DEFAULT_THREAD_POOL_SIZE = 5;

	/** Base URL for Magento SOAP v2 web service */
	private String magentoUrl = DEFAULT_URL;

	/** Username used to log in to Magento */
	private String magentoUsername = DEFAULT_USERNAME;

	/** Password used to log in to Magento */
	private String magentoPassword = DEFAULT_PASSWORD;

	/** Indicates whether SOAP messages should be sent to the log */
	private boolean debugSoap = false;

	/** Cached asset map */
	private Map<String, HardwareAsset> assetCache = new HashMap<String, HardwareAsset>();

	/** Matcher used for searches */
	protected AssetMatcher matcher = new AssetMatcher();

	/** Magento web service */
	protected MagentoService magento;

	/** Port for accessing Magento service */
	protected MageApiModelServerWsiHandlerPortType port;

	/** Unique session id */
	protected String sessionId;

	/** Map of attribute sets by unique id */
	private Map<Integer, ArrayOfString> attributeSets = new HashMap<Integer, ArrayOfString>();

	public MagentoAssetModule() {
		super(LifecycleComponentType.AssetModule);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		LOGGER.info("Connecting to Magento web service as user '" + getMagentoUsername() + "'.");
		try {
			magento = new MagentoService(new URL(getMagentoUrl()));
			port = magento.getMageApiModelServerWsiHandlerPort();

			if (isDebugSoap()) {
				Client client = ClientProxy.getClient(port);
				client.getInInterceptors().add(new LoggingInInterceptor());
				client.getOutInterceptors().add(new LoggingOutInterceptor());
			}

			login();
			cacheAssetData();
		} catch (MalformedURLException e) {
			throw new OpenIoTException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#getLogger()
	 */
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	/**
	 * Logs in to Magento and saves the session id.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void login() throws OpenIoTException {
		LoginParam loginParams = new LoginParam();
		loginParams.setUsername(getMagentoUsername());
		loginParams.setApiKey(getMagentoPassword());
		try {
			LoginResponseParam loginResponse = port.login(loginParams);
			sessionId = loginResponse.getResult();
		} catch (SOAPFaultException e) {
			throw new OpenIoTException("Magento login failed.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModule#getId()
	 */
	public String getId() {
		return MODULE_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModule#getName()
	 */
	public String getName() {
		return MODULE_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModule#getAssetType()
	 */
	public AssetType getAssetType() {
		return AssetType.Hardware;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModule#getAssetById(java.lang.String)
	 */
	public HardwareAsset getAssetById(String id) throws OpenIoTException {
		return assetCache.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModule#search(java.lang.String)
	 */
	public List<HardwareAsset> search(String criteria) throws OpenIoTException {
		criteria = criteria.toLowerCase();
		List<HardwareAsset> results = new ArrayList<HardwareAsset>();
		if (criteria.length() == 0) {
			results.addAll(assetCache.values());
			return results;
		}
		for (HardwareAsset asset : assetCache.values()) {
			if (matcher.isHardwareMatch(asset, criteria)) {
				results.add(asset);
			}
		}
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAssetModule#refresh()
	 */
	public ICommandResponse refresh() throws OpenIoTException {
		try {
			return cacheAssetData();
		} catch (OpenIoTException e) {
			return new CommandResponse(CommandResult.Failed, e.getMessage());
		}
	}

	/**
	 * Make calls to the Magento server to get all products and cache them locally for
	 * fast searches.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected ICommandResponse cacheAssetData() throws OpenIoTException {
		assetCache.clear();
		attributeSets.clear();

		LOGGER.info("Caching search data.");
		int totalAssets = 0;
		long startTime = System.currentTimeMillis();

		// Get attribute set list using multiple threads.
		ExecutorService attrExec = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
		CatalogProductAttributeSetListRequestParam aslReq = new CatalogProductAttributeSetListRequestParam();
		aslReq.setSessionId(sessionId);
		CatalogProductAttributeSetListResponseParam aslResp = port.catalogProductAttributeSetList(aslReq);
		List<Future<ArrayOfString>> attrResults = new ArrayList<Future<ArrayOfString>>();
		for (CatalogProductAttributeSetEntity set : aslResp.getResult().getComplexObjectArray()) {
			attrResults.add(attrExec.submit(new AttributeSetLoader(set.getSetId())));
		}
		for (Future<ArrayOfString> attrResult : attrResults) {
			try {
				attrResult.get();
			} catch (Throwable e) {
				throw new OpenIoTException(e);
			}
		}

		// Load product list using multiple threads.
		ExecutorService prodExec = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
		CatalogProductListRequestParam listReq = new CatalogProductListRequestParam();
		listReq.setSessionId(sessionId);
		CatalogProductListResponseParam listResp = port.catalogProductList(listReq);
		List<CatalogProductEntity> products = listResp.getResult().getComplexObjectArray();
		List<Future<HardwareAsset>> prodResults = new ArrayList<Future<HardwareAsset>>();
		for (CatalogProductEntity product : products) {
			prodResults.add(prodExec.submit(new ProductLoader(product)));
		}
		for (Future<HardwareAsset> prodResult : prodResults) {
			try {
				prodResult.get();
				totalAssets++;
			} catch (Throwable e) {
				throw new OpenIoTException(e);
			}
		}

		long totalTime = System.currentTimeMillis() - startTime;
		String message = "Cached " + totalAssets + " assets in " + totalTime + "ms.";
		LOGGER.info(message);
		return new CommandResponse(CommandResult.Successful, message);
	}

	public String getMagentoUrl() {
		return magentoUrl;
	}

	public void setMagentoUrl(String magentoUrl) {
		this.magentoUrl = magentoUrl;
	}

	public String getMagentoUsername() {
		return magentoUsername;
	}

	public void setMagentoUsername(String magentoUsername) {
		this.magentoUsername = magentoUsername;
	}

	public String getMagentoPassword() {
		return magentoPassword;
	}

	public void setMagentoPassword(String magentoPassword) {
		this.magentoPassword = magentoPassword;
	}

	public boolean isDebugSoap() {
		return debugSoap;
	}

	public void setDebugSoap(boolean debugSoap) {
		this.debugSoap = debugSoap;
	}

	/**
	 * Loads attribute sets in a separate thread.
	 * 
	 * @author Derek
	 */
	protected class AttributeSetLoader implements Callable<ArrayOfString> {

		/** Attribute set id */
		private Integer setId;

		public AttributeSetLoader(Integer setId) {
			this.setId = setId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		public ArrayOfString call() throws Exception {
			CatalogProductAttributeListRequestParam request = new CatalogProductAttributeListRequestParam();
			request.setSessionId(sessionId);
			request.setSetId(setId);
			CatalogProductAttributeListResponseParam response = port.catalogProductAttributeList(request);
			List<CatalogAttributeEntity> entities = response.getResult().getComplexObjectArray();
			ArrayOfString converted = new ArrayOfString();
			for (CatalogAttributeEntity entity : entities) {
				converted.getComplexObjectArray().add(entity.getCode());
			}
			attributeSets.put(setId, converted);
			return converted;
		}
	}

	/**
	 * Loads product information from Magento in a separate thread.
	 * 
	 * @author Derek
	 */
	protected class ProductLoader implements Callable<HardwareAsset> {

		/** Unique product id */
		private CatalogProductEntity product;

		public ProductLoader(CatalogProductEntity product) {
			this.product = product;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		public HardwareAsset call() throws Exception {
			HardwareAsset asset = new HardwareAsset();
			Integer setId = Integer.parseInt(product.getSet());
			ArrayOfString attrSet = attributeSets.get(setId);

			// Load product attribute details.
			CatalogProductRequestAttributes attr = new CatalogProductRequestAttributes();
			attr.setAttributes(new ArrayOfString());
			attr.setAdditionalAttributes(attrSet);
			CatalogProductInfoRequestParam infoRequest = new CatalogProductInfoRequestParam();
			infoRequest.setSessionId(sessionId);
			infoRequest.setProductId(product.getProductId());
			infoRequest.setAttributes(attr);
			CatalogProductInfoResponseParam infoResponse = port.catalogProductInfo(infoRequest);
			CatalogProductReturnEntity entity = infoResponse.getResult();

			// Load all attributes.
			AssociativeArray addAttr = entity.getAdditionalAttributes();
			if (addAttr != null) {
				for (AssociativeEntity assoc : addAttr.getComplexObjectArray()) {
					asset.setProperty(assoc.getKey(), assoc.getValue());
				}
			}
			asset.setId(entity.getProductId());
			asset.setSku(entity.getSku());
			asset.setName(asset.getProperty(IMagentoFields.PROP_NAME));
			asset.setDescription(asset.getProperty(IMagentoFields.PROP_DESCRIPTION));

			// Load product image data.
			CatalogProductAttributeMediaListRequestParam mediaReq =
					new CatalogProductAttributeMediaListRequestParam();
			mediaReq.setSessionId(sessionId);
			mediaReq.setProductId(product.getProductId());
			CatalogProductAttributeMediaListResponseParam mediaResp =
					port.catalogProductAttributeMediaList(mediaReq);
			List<CatalogProductImageEntity> mediaList = mediaResp.getResult().getComplexObjectArray();
			for (CatalogProductImageEntity media : mediaList) {
				asset.setImageUrl(media.getUrl());
				break;
			}

			assetCache.put(asset.getId(), asset);
			return asset;
		}
	}
}