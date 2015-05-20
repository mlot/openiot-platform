/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.client;

import com.openiot.rest.model.common.MetadataProvider;
import com.openiot.rest.model.device.*;
import com.openiot.rest.model.device.batch.BatchOperation;
import com.openiot.rest.model.device.command.DeviceCommand;
import com.openiot.rest.model.device.event.*;
import com.openiot.rest.model.device.event.request.DeviceAlertCreateRequest;
import com.openiot.rest.model.device.event.request.DeviceLocationCreateRequest;
import com.openiot.rest.model.device.event.request.DeviceMeasurementsCreateRequest;
import com.openiot.rest.model.device.request.*;
import com.openiot.rest.model.search.*;
import com.openiot.rest.model.system.Version;
import com.openiot.spi.IOpenIoTClient;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.device.request.IDeviceAssignmentCreateRequest;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Client for interacting with OpenIoT REST services.
 * 
 * @author dadams
 */
public class OpenIoTClient implements IOpenIoTClient {

	/** Default base url for calling REST services */
	private static final String DEFAULT_BASE_URL = "http://localhost:8080/sitewhere/api/";

	/** Default REST username */
	private static final String DEFAULT_USERNAME = "admin";

	/** Default REST password */
	private static final String DEFAULT_PASSWORD = "password";

	/** Default connection timeout in milliseconds */
	private static final int DEFAULT_CONNECT_TIMEOUT = 3 * 1000;

	/** Indicates whether to write debug information to the console */
	private static final boolean DEBUG_ENABLED = true;

	/** Use CXF web client to send requests */
	private RestTemplate client;

	/** Base URL used for REST calls */
	private String baseUrl = DEFAULT_BASE_URL;

	/** Username used for REST calls */
	private String username = DEFAULT_USERNAME;

	/** Password used for REST calls */
	private String password = DEFAULT_PASSWORD;

	public OpenIoTClient() {
		this(DEFAULT_BASE_URL, DEFAULT_USERNAME, DEFAULT_PASSWORD, DEFAULT_CONNECT_TIMEOUT);
	}

	public OpenIoTClient(String url, String username, String password) {
		if (DEBUG_ENABLED) {
			enableDebugging();
		}
		this.client = new RestTemplate();
		this.username = username;
		this.password = password;
		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
		addMessageConverters(converters);
		client.setMessageConverters(converters);
		client.setErrorHandler(new OpenIoTErrorHandler());
		this.baseUrl = url;
	}

	public OpenIoTClient(String url, String username, String password, int connectTimeoutMs) {
		if (DEBUG_ENABLED) {
			enableDebugging();
		}
		this.client = new RestTemplate();
		this.username = username;
		this.password = password;
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(connectTimeoutMs);
		client.setRequestFactory(factory);
		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
		addMessageConverters(converters);
		client.setMessageConverters(converters);
		client.setErrorHandler(new OpenIoTErrorHandler());
		this.baseUrl = url;
	}

	/**
	 * Allow subclasses to override converters used for the {@link RestTemplate}.
	 * 
	 * @param converters
	 */
	protected void addMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new MappingJackson2HttpMessageConverter());
	}

	/**
	 * Enable console debugging for messages sent by the client.
	 */
	protected void enableDebugging() {
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache", "debug");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#getOpenIoTVersion()
	 */
	@Override
	public Version getOpenIoTVersion() throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		return sendRest(getBaseUrl() + "system/version", HttpMethod.GET, null, Version.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOpenIoTClient#createSite(com.openiot.rest.model.device.request
	 * .SiteCreateRequest)
	 */
	@Override
	public Site createSite(SiteCreateRequest request) throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		return sendRest(getBaseUrl() + "sites", HttpMethod.POST, request, Site.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOpenIoTClient#createDeviceSpecification(com.openiot.rest
	 * .model.device.request.DeviceSpecificationCreateRequest)
	 */
	@Override
	public DeviceSpecification createDeviceSpecification(DeviceSpecificationCreateRequest request)
			throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		return sendRest(getBaseUrl() + "specifications", HttpMethod.POST, request, DeviceSpecification.class,
				vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOpenIoTClient#getDeviceSpecificationByToken(java.lang.String)
	 */
	@Override
	public DeviceSpecification getDeviceSpecificationByToken(String token) throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("token", token);
		return sendRest(getBaseUrl() + "specifications/{token}", HttpMethod.GET, null,
				DeviceSpecification.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#createDeviceCommand(java.lang.String,
	 * DeviceCommandCreateRequest)
	 */
	@Override
	public DeviceCommand createDeviceCommand(String specToken, DeviceCommandCreateRequest request)
			throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("token", specToken);
		return sendRest(getBaseUrl() + "specifications/{token}/commands", HttpMethod.POST, request,
				DeviceCommand.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOpenIoTClient#createDevice(com.openiot.rest.model.device
	 * .request.DeviceCreateRequest )
	 */
	@Override
	public Device createDevice(DeviceCreateRequest request) throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		return sendRest(getBaseUrl() + "devices", HttpMethod.POST, request, Device.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#getDeviceByHardwareId(java.lang.String)
	 */
	@Override
	public Device getDeviceByHardwareId(String hardwareId) throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("hardwareId", hardwareId);
		return sendRest(getBaseUrl() + "devices/{hardwareId}", HttpMethod.GET, null, Device.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#updateDevice(java.lang.String,
	 * DeviceCreateRequest)
	 */
	@Override
	public Device updateDevice(String hardwareId, DeviceCreateRequest request) throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("hardwareId", hardwareId);
		return sendRest(getBaseUrl() + "devices/{hardwareId}", HttpMethod.PUT, request, Device.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#listDevices(java.lang.Boolean,
	 * java.lang.Boolean, java.lang.Boolean, java.lang.Boolean, java.lang.Integer,
	 * java.lang.Integer, java.util.Calendar, java.util.Calendar)
	 */
	@Override
	public DeviceSearchResults listDevices(Boolean includeDeleted, Boolean excludeAssigned,
			Boolean populateSpecification, Boolean populateAssignment, Integer pageNumber, Integer pageSize,
			Calendar createDateStart, Calendar createDateEnd) throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		if (includeDeleted != null) {
			vars.put("includeDeleted", String.valueOf(includeDeleted));
		}
		if (excludeAssigned != null) {
			vars.put("excludeAssigned", String.valueOf(excludeAssigned));
		}
		if (populateSpecification != null) {
			vars.put("includeSpecification", String.valueOf(populateSpecification));
		}
		if (populateAssignment != null) {
			vars.put("includeAssignment", String.valueOf(populateAssignment));
		}
		if (pageNumber != null) {
			vars.put("page", String.valueOf(pageNumber));
		}
		if (pageSize != null) {
			vars.put("pageSize", String.valueOf(pageSize));
		}
		return sendRest(getBaseUrl() + "devices", HttpMethod.GET, null, DeviceSearchResults.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#deleteDevice(java.lang.String, boolean)
	 */
	@Override
	public Device deleteDevice(String hardwareId, boolean force) throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("hardwareId", hardwareId);
		String url = getBaseUrl() + "devices/{hardwareId}";
		if (force) {
			url += "?force=true";
		}
		return sendRest(url, HttpMethod.DELETE, null, Device.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOpenIoTClient#getCurrentAssignmentForDevice(java.lang.String)
	 */
	@Override
	public DeviceAssignment getCurrentAssignmentForDevice(String hardwareId) throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("hardwareId", hardwareId);
		return sendRest(getBaseUrl()
				+ "devices/{hardwareId}/assignment?includeAsset=false&includeDevice=false&includeSite=false",
				HttpMethod.GET, null, DeviceAssignment.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOpenIoTClient#createDeviceAssignment(com.openiot.spi.device
	 * .request. IDeviceAssignmentCreateRequest)
	 */
	@Override
	public DeviceAssignment createDeviceAssignment(IDeviceAssignmentCreateRequest request)
			throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		return sendRest(getBaseUrl() + "assignments", HttpMethod.POST, request, DeviceAssignment.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOpenIoTClient#getDeviceAssignmentByToken(java.lang.String)
	 */
	@Override
	public DeviceAssignment getDeviceAssignmentByToken(String assignmentToken) throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("assignmentToken", assignmentToken);
		return sendRest(getBaseUrl() + "assignments/{assignmentToken}", HttpMethod.GET, null,
				DeviceAssignment.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#deleteDeviceAssignment(java.lang.String,
	 * boolean)
	 */
	@Override
	public DeviceAssignment deleteDeviceAssignment(String assignmentToken, boolean force)
			throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("assignmentToken", assignmentToken);
		String url = getBaseUrl() + "assignments/{assignmentToken}";
		if (force) {
			url += "?force=true";
		}
		return sendRest(url, HttpMethod.DELETE, null, DeviceAssignment.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOpenIoTClient#listDeviceAssignmentHistory(java.lang.String)
	 */
	@Override
	public DeviceAssignmentSearchResults listDeviceAssignmentHistory(String hardwareId)
			throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("hardwareId", hardwareId);
		return sendRest(getBaseUrl() + "devices/{hardwareId}/assignments", HttpMethod.GET, null,
				DeviceAssignmentSearchResults.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#addDeviceEventBatch(java.lang.String,
	 * com.openiot.rest.model.device.DeviceEventBatch)
	 */
	@Override
	public DeviceEventBatchResponse addDeviceEventBatch(String hardwareId, DeviceEventBatch batch)
			throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("hardwareId", hardwareId);
		return sendRest(getBaseUrl() + "devices/{hardwareId}/batch", HttpMethod.POST, batch,
				DeviceEventBatchResponse.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOpenIoTClient#updateDeviceAssignmentMetadata(java.lang.String,
	 * com.openiot.rest.model.device.MetadataProvider)
	 */
	@Override
	public DeviceAssignment updateDeviceAssignmentMetadata(String token, MetadataProvider metadata)
			throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("token", token);
		return sendRest(getBaseUrl() + "assignments/{token}/metadata", HttpMethod.POST, metadata,
				DeviceAssignment.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#createDeviceMeasurements(java.lang.String,
	 * com.openiot.rest.model.device.request.DeviceMeasurementsCreateRequest)
	 */
	@Override
	public DeviceMeasurements createDeviceMeasurements(String assignmentToken,
			DeviceMeasurementsCreateRequest request) throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("token", assignmentToken);
		return sendRest(getBaseUrl() + "assignments/{token}/measurements", HttpMethod.POST, request,
				DeviceMeasurements.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#listDeviceMeasurements(java.lang.String,
	 * int)
	 */
	@Override
	public SearchResults<DeviceMeasurements> listDeviceMeasurements(String assignmentToken, int maxCount)
			throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("token", assignmentToken);
		vars.put("count", String.valueOf(maxCount));
		String url = getBaseUrl() + "assignments/{token}/measurements?count={count}";
		return sendRest(url, HttpMethod.GET, null, DeviceMeasurementsSearchResults.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#createDeviceLocation(java.lang.String,
	 * com.openiot.rest.model.device.request.DeviceLocationCreateRequest)
	 */
	@Override
	public DeviceLocation createDeviceLocation(String assignmentToken, DeviceLocationCreateRequest request)
			throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("token", assignmentToken);
		return sendRest(getBaseUrl() + "assignments/{token}/locations", HttpMethod.POST, request,
				DeviceLocation.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#listDeviceLocations(java.lang.String, int)
	 */
	@Override
	public DeviceLocationSearchResults listDeviceLocations(String assignmentToken, int maxCount)
			throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("token", assignmentToken);
		vars.put("count", String.valueOf(maxCount));
		String url = getBaseUrl() + "assignments/{token}/locations?count={count}";
		return sendRest(url, HttpMethod.GET, null, DeviceLocationSearchResults.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#createDeviceAlert(java.lang.String,
	 * com.openiot.rest.model.device.request.DeviceAlertCreateRequest)
	 */
	@Override
	public DeviceAlert createDeviceAlert(String assignmentToken, DeviceAlertCreateRequest request)
			throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("token", assignmentToken);
		return sendRest(getBaseUrl() + "assignments/{token}/alerts", HttpMethod.POST, request,
				DeviceAlert.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#listDeviceAlerts(java.lang.String, int)
	 */
	@Override
	public DeviceAlertSearchResults listDeviceAlerts(String assignmentToken, int maxCount)
			throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("token", assignmentToken);
		vars.put("count", String.valueOf(maxCount));
		String url = getBaseUrl() + "assignments/{token}/alerts?count={count}";
		return sendRest(url, HttpMethod.GET, null, DeviceAlertSearchResults.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#createZone(java.lang.String,
	 * ZoneCreateRequest)
	 */
	@Override
	public Zone createZone(String siteToken, ZoneCreateRequest request) throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("siteToken", siteToken);
		return sendRest(getBaseUrl() + "sites/{siteToken}/zones", HttpMethod.POST, request, Zone.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IOpenIoTClient#listZonesForSite(java.lang.String)
	 */
	@Override
	public ZoneSearchResults listZonesForSite(String siteToken) throws OpenIoTException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("siteToken", siteToken);
		String url = getBaseUrl() + "sites/{siteToken}/zones";
		return sendRest(url, HttpMethod.GET, null, ZoneSearchResults.class, vars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IOpenIoTClient#createBatchCommandInvocation(java.lang.String,
	 * java.lang.String, java.util.Map, java.util.List)
	 */
	@Override
	public BatchOperation createBatchCommandInvocation(String batchToken, String commandToken,
			Map<String, String> parameters, List<String> hardwareIds) throws OpenIoTException {
		BatchCommandInvocationRequest request = new BatchCommandInvocationRequest();
		request.setToken(batchToken);
		request.setCommandToken(commandToken);
		request.setParameterValues(parameters);
		request.setHardwareIds(hardwareIds);
		Map<String, String> vars = new HashMap<String, String>();
		return sendRest(getBaseUrl() + "batch/command", HttpMethod.POST, request, BatchOperation.class, vars);
	}

	/**
	 * Send a REST request and handle the response.
	 * 
	 * @param url
	 * @param method
	 * @param input
	 * @param clazz
	 * @param vars
	 * @return
	 * @throws com.openiot.spi.OpenIoTSystemException
	 */
	protected <S, T> S sendRest(String url, HttpMethod method, T input, Class<S> clazz,
			Map<String, String> vars) throws OpenIoTSystemException {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", getAuthHeader());
			HttpEntity<T> entity = new HttpEntity<T>(input, headers);
			ResponseEntity<S> response = getClient().exchange(url, method, entity, clazz, vars);
			return response.getBody();
		} catch (ResourceAccessException e) {
			if (e.getCause() instanceof OpenIoTSystemException) {
				throw (OpenIoTSystemException) e.getCause();
			}
			throw new RuntimeException(e);
		}
	}

	/**
	 * Encode the username and password to make the authorization header.
	 * 
	 * @return
	 */
	protected String getAuthHeader() {
		String token = getUsername() + ":" + getPassword();
		String encoded = new String(Base64.encodeBase64(token.getBytes()));
		return "Basic " + encoded;
	}

	public RestTemplate getClient() {
		return client;
	}

	public void setClient(RestTemplate client) {
		this.client = client;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}