/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.device.provisioning;

import com.openiot.OpenIoT;
import com.openiot.rest.model.device.command.RegistrationAckCommand;
import com.openiot.rest.model.device.command.RegistrationFailureCommand;
import com.openiot.rest.model.device.request.DeviceAssignmentCreateRequest;
import com.openiot.rest.model.device.request.DeviceCreateRequest;
import com.openiot.rest.model.search.SearchCriteria;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.DeviceAssignmentType;
import com.openiot.spi.device.IDevice;
import com.openiot.spi.device.IDeviceSpecification;
import com.openiot.spi.device.ISite;
import com.openiot.spi.device.command.RegistrationFailureReason;
import com.openiot.spi.device.command.RegistrationSuccessReason;
import com.openiot.spi.device.event.request.IDeviceRegistrationRequest;
import com.openiot.spi.device.provisioning.IRegistrationManager;
import com.openiot.spi.search.ISearchResults;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;

/**
 * Base logic for {@link IRegistrationManager} implementations.
 * 
 * @author Derek
 */
public class RegistrationManager extends LifecycleComponent implements IRegistrationManager {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(RegistrationManager.class);

	/** Indicates if new devices can register with the system */
	private boolean allowNewDevices = true;

	/** Indicates if devices can be auto-assigned if no site token is passed */
	private boolean autoAssignSite = true;

	/** Token used if autoAssignSite is enabled */
	private String autoAssignSiteToken = null;

	public RegistrationManager() {
		super(LifecycleComponentType.RegistrationManger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IRegistrationManager#handleDeviceRegistration
	 * (IDeviceRegistrationRequest)
	 */
	@Override
	public void handleDeviceRegistration(IDeviceRegistrationRequest request) throws OpenIoTException {
		LOGGER.debug("Handling device registration request.");
		IDevice device =
				OpenIoT.getServer().getDeviceManagement().getDeviceByHardwareId(request.getHardwareId());
		IDeviceSpecification specification =
				OpenIoT.getServer().getDeviceManagement().getDeviceSpecificationByToken(
						request.getSpecificationToken());

		// If a site token is passed, verify it is valid.
		if (request.getSiteToken() != null) {
			if (OpenIoT.getServer().getDeviceManagement().getSiteByToken(request.getSiteToken()) == null) {
				LOGGER.warn("Ignoring device registration request because of invalid site token.");
				return;
			}
		}
		// Create device if it does not already exist.
		if (device == null) {
			if (!isAllowNewDevices()) {
				LOGGER.warn("Ignoring device registration request since new devices are not allowed.");
				return;
			}
			if (specification == null) {
				sendInvalidSpecification(request.getHardwareId());
				return;
			}
			if ((!isAutoAssignSite()) && (request.getSiteToken() == null)) {
				sendSiteTokenRequired(request.getHardwareId());
				return;
			}
			if (isAutoAssignSite() && (getAutoAssignSiteToken() == null)) {
				updateAutoAssignToFirstSite();
				if (getAutoAssignSiteToken() == null) {
					throw new OpenIoTException("Unable to register device. No sites are configured.");
				}
			}
			String siteToken =
					(request.getSiteToken() != null) ? request.getSiteToken() : getAutoAssignSiteToken();
			LOGGER.debug("Creating new device as part of registration.");
			DeviceCreateRequest deviceCreate = new DeviceCreateRequest();
			deviceCreate.setHardwareId(request.getHardwareId());
			deviceCreate.setSpecificationToken(request.getSpecificationToken());
			deviceCreate.setSiteToken(siteToken);
			deviceCreate.setComments("Device created by on-demand registration.");
			for (String key : request.getMetadata().keySet()) {
				String value = request.getMetadata(key);
				deviceCreate.addOrReplaceMetadata(key, value);
			}
			device = OpenIoT.getServer().getDeviceManagement().createDevice(deviceCreate);
		} else if (!device.getSpecificationToken().equals(request.getSpecificationToken())) {
			sendInvalidSpecification(request.getHardwareId());
			return;
		}
		// Make sure device is assigned.
		if (device.getAssignmentToken() == null) {
			LOGGER.debug("Handling unassigned device for registration.");
			DeviceAssignmentCreateRequest assnCreate = new DeviceAssignmentCreateRequest();
			assnCreate.setDeviceHardwareId(device.getHardwareId());
			assnCreate.setAssignmentType(DeviceAssignmentType.Unassociated);
			OpenIoT.getServer().getDeviceManagement().createDeviceAssignment(assnCreate);
		}
		boolean isNewRegistration = (device != null);
		sendRegistrationAck(request.getHardwareId(), isNewRegistration);
	}

	/**
	 * Send a registration ack message.
	 * 
	 * @param hardwareId
	 * @param newRegistration
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void sendRegistrationAck(String hardwareId, boolean newRegistration) throws OpenIoTException {
		RegistrationAckCommand command = new RegistrationAckCommand();
		command.setReason((newRegistration) ? RegistrationSuccessReason.NewRegistration
				: RegistrationSuccessReason.AlreadyRegistered);
		OpenIoT.getServer().getDeviceProvisioning().deliverSystemCommand(hardwareId, command);
	}

	/**
	 * Send a message indicating that the registration manager does not allow registration
	 * of new devices.
	 * 
	 * @param hardwareId
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void sendNoNewDevicesAllowed(String hardwareId) throws OpenIoTException {
		RegistrationFailureCommand command = new RegistrationFailureCommand();
		command.setReason(RegistrationFailureReason.NewDevicesNotAllowed);
		command.setErrorMessage("Registration manager does not allow new devices to be created.");
		OpenIoT.getServer().getDeviceProvisioning().deliverSystemCommand(hardwareId, command);
	}

	/**
	 * Send a message indicating invalid specification id or one that does not match
	 * existing device.
	 * 
	 * @param hardwareId
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void sendInvalidSpecification(String hardwareId) throws OpenIoTException {
		RegistrationFailureCommand command = new RegistrationFailureCommand();
		command.setReason(RegistrationFailureReason.InvalidSpecificationToken);
		command.setErrorMessage("Specification token passed in registration was invalid.");
		OpenIoT.getServer().getDeviceProvisioning().deliverSystemCommand(hardwareId, command);
	}

	/**
	 * Send information indicating a site token must be passed (if not auto-assigned).
	 * 
	 * @param hardwareId
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void sendSiteTokenRequired(String hardwareId) throws OpenIoTException {
		RegistrationFailureCommand command = new RegistrationFailureCommand();
		command.setReason(RegistrationFailureReason.SiteTokenRequired);
		command.setErrorMessage("Automatic site assignment disabled. Site token required.");
		OpenIoT.getServer().getDeviceProvisioning().deliverSystemCommand(hardwareId, command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		if (isAutoAssignSite()) {
			if (getAutoAssignSiteToken() == null) {
				updateAutoAssignToFirstSite();
			} else {
				ISite site =
						OpenIoT.getServer().getDeviceManagement().getSiteByToken(getAutoAssignSiteToken());
				if (site == null) {
					throw new OpenIoTException(
							"Registration manager auto assignment site token is invalid.");
				}
			}
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
	 * Update token for auto-assigned site to first site in list.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void updateAutoAssignToFirstSite() throws OpenIoTException {
		ISearchResults<ISite> sites =
				OpenIoT.getServer().getDeviceManagement().listSites(new SearchCriteria(1, 1));
		if (sites.getResults().isEmpty()) {
			LOGGER.warn("Registration manager configured for auto-assign site, but no sites were found.");
			setAutoAssignSiteToken(null);
		} else {
			setAutoAssignSiteToken(sites.getResults().get(0).getToken());
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

	public boolean isAllowNewDevices() {
		return allowNewDevices;
	}

	public void setAllowNewDevices(boolean allowNewDevices) {
		this.allowNewDevices = allowNewDevices;
	}

	public boolean isAutoAssignSite() {
		return autoAssignSite;
	}

	public void setAutoAssignSite(boolean autoAssignSite) {
		this.autoAssignSite = autoAssignSite;
	}

	public String getAutoAssignSiteToken() {
		return autoAssignSiteToken;
	}

	public void setAutoAssignSiteToken(String autoAssignSiteToken) {
		this.autoAssignSiteToken = autoAssignSiteToken;
	}
}