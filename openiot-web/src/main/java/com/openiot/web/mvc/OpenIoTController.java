/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.web.mvc;

import com.openiot.OpenIoT;
import com.openiot.Tracer;
import com.openiot.common.MarshalUtils;
import com.openiot.device.marshaling.DeviceAssignmentMarshalHelper;
import com.openiot.security.LoginManager;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.*;
import com.openiot.spi.device.batch.IBatchOperation;
import com.openiot.spi.device.command.IDeviceCommand;
import com.openiot.spi.device.group.IDeviceGroup;
import com.openiot.spi.device.request.IBatchCommandInvocationRequest;
import com.openiot.spi.server.debug.TracerCategory;
import com.openiot.spi.server.lifecycle.LifecycleStatus;
import com.openiot.version.VersionHelper;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring MVC controller for OpenIoT web application.
 * 
 * @author dadams
 */
@Controller
public class OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(OpenIoTController.class);

	/**
	 * Display the "login" page.
	 * 
	 * @return
	 */
	@RequestMapping("/")
	public ModelAndView login() {
		Tracer.start(TracerCategory.AdminUserInterface, "login", LOGGER);
		try {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("version", VersionHelper.getVersion());
			if (OpenIoT.getServer().getLifecycleStatus() == LifecycleStatus.Started) {
				return new ModelAndView("login", data);
			} else {
				return new ModelAndView("noserver", data);
			}
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Display the "login" page after failed login.
	 * 
	 * @return
	 */
	@RequestMapping("/loginFailed")
	public ModelAndView loginFailed() {
		Tracer.start(TracerCategory.AdminUserInterface, "loginFailed", LOGGER);
		try {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("version", VersionHelper.getVersion());
			data.put("loginFailed", true);
			return new ModelAndView("login", data);
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * Display the "list sites" page.
	 * 
	 * @return
	 */
	@RequestMapping("/sites/list")
	public ModelAndView listSites() {
		Tracer.start(TracerCategory.AdminUserInterface, "listSites", LOGGER);
		try {
			try {
				Map<String, Object> data = createBaseData();
				return new ModelAndView("sites/list", data);
			} catch (OpenIoTException e) {
				LOGGER.error(e);
				return showError(e.getMessage());
			}
		} finally {
			Tracer.stop(LOGGER);
		}
	}

    /**
     * Display the site dashboard.
     *
     * @param siteToken
     * @return
     */
    @RequestMapping("/sites/dashboard")
    public ModelAndView siteDashboard(@RequestParam("siteToken") String siteToken) {
        if (siteToken != null) {
            try {
                Map<String, Object> data = createBaseData();
                IDeviceManagement management = OpenIoT.getServer().getDeviceManagement();
                ISite site = management.getSiteByToken(siteToken);
                if (site != null) {
                    data.put("site", site);
                    return new ModelAndView("sites/dashboard", data);
                }
                return showError("Site for token '" + siteToken + "' not found.");
            } catch (OpenIoTException e) {
                LOGGER.error(e);
                return showError(e.getMessage());
            }
        }
        return showError("No site token passed.");
    }

	/**
	 * Display the "site detail" page.
	 * 
	 * @param siteToken
	 * @return
	 */
	@RequestMapping("/sites/detail")
	public ModelAndView siteDetail(@RequestParam("siteToken") String siteToken) {
		if (siteToken != null) {
			try {
				Map<String, Object> data = createBaseData();
				IDeviceManagement management = OpenIoT.getServer().getDeviceManagement();
				ISite site = management.getSiteByToken(siteToken);
				if (site != null) {
					data.put("site", site);
					return new ModelAndView("sites/detail", data);
				}
				return showError("Site for token '" + siteToken + "' not found.");
			} catch (OpenIoTException e) {
				LOGGER.error(e);
				return showError(e.getMessage());
			}
		}
		return showError("No site token passed.");
	}

	/**
	 * Display the "assignment detail" page.
	 * 
	 * @param token
	 * @return
	 */
	@RequestMapping("/assignments/detail")
	public ModelAndView assignmentDetail(@RequestParam("token") String token) {
		if (token != null) {
			try {
				Map<String, Object> data = createBaseData();
				IDeviceManagement management = OpenIoT.getServer().getDeviceManagement();
				IDeviceAssignment assignment = management.getDeviceAssignmentByToken(token);
				if (assignment != null) {
					DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper();
					helper.setIncludeDevice(true);
					assignment = helper.convert(assignment, OpenIoT.getServer().getAssetModuleManager());
					data.put("assignment", assignment);
					return new ModelAndView("assignments/detail", data);
				}
				return showError("Assignment for token '" + token + "' not found.");
			} catch (OpenIoTException e) {
				LOGGER.error(e);
				return showError(e.getMessage());
			}
		}
		return showError("No assignment token passed.");
	}

	/**
	 * Display the "assignment emulator" page.
	 * 
	 * @param token
	 * @return
	 */
	@RequestMapping("/assignments/emulator")
	public ModelAndView assignmentEmulator(@RequestParam("token") String token) {
		if (token != null) {
			try {
				Map<String, Object> data = createBaseData();
				IDeviceManagement management = OpenIoT.getServer().getDeviceManagement();
				IDeviceAssignment assignment = management.getDeviceAssignmentByToken(token);
				if (assignment != null) {
					data.put("assignment", assignment);
					return new ModelAndView("assignments/emulator", data);
				}
				return showError("Assignment for token '" + token + "' not found.");
			} catch (OpenIoTException e) {
				LOGGER.error(e);
				return showError(e.getMessage());
			}
		}
		return showError("Assignment token was not passed.");
	}

	/**
	 * Display the "list device specifications" page.
	 * 
	 * @return
	 */
	@RequestMapping("/specifications/list")
	public ModelAndView listSpecifications() {
		try {
			Map<String, Object> data = createBaseData();
			return new ModelAndView("specifications/list", data);
		} catch (OpenIoTException e) {
			LOGGER.error(e);
			return showError(e.getMessage());
		}
	}

	/**
	 * Display the "specification detail" page.
	 * 
	 * @param siteToken
	 * @return
	 */
	@RequestMapping("/specifications/detail")
	public ModelAndView specificationDetail(@RequestParam("token") String token) {
		if (token != null) {
			try {
				Map<String, Object> data = createBaseData();
				IDeviceManagement management = OpenIoT.getServer().getDeviceManagement();
				IDeviceSpecification spec = management.getDeviceSpecificationByToken(token);
				if (spec != null) {
					data.put("specification", spec);
					return new ModelAndView("specifications/detail", data);
				}
				return showError("Specification for token '" + token + "' not found.");
			} catch (OpenIoTException e) {
				LOGGER.error(e);
				return showError(e.getMessage());
			}
		}
		return showError("No specification token passed.");
	}

	/**
	 * Display the "list devices" page.
	 * 
	 * @return
	 */
	@RequestMapping("/devices/list")
	public ModelAndView listDevices(@RequestParam(required = false) String specification,
			@RequestParam(required = false) String group,
			@RequestParam(required = false) String groupsWithRole,
			@RequestParam(required = false) String dateRange,
			@RequestParam(required = false) String beforeDate,
			@RequestParam(required = false) String afterDate,
			@RequestParam(required = false) boolean excludeAssigned) {
		try {
			Map<String, Object> data = createBaseData();

			// Look up specification that will be used for filtering.
			if (specification != null) {
				IDeviceSpecification found =
						OpenIoT.getServer().getDeviceManagement().getDeviceSpecificationByToken(
								specification);
				if (found == null) {
					throw new OpenIoTException("Specification token was not valid.");
				}
				data.put("specification", found);
			}

			// Look up device group that will be used for filtering.
			if (group != null) {
				IDeviceGroup found = OpenIoT.getServer().getDeviceManagement().getDeviceGroup(group);
				if (found == null) {
					throw new OpenIoTException("Device group token was not valid.");
				}
				data.put("group", found);
			}

			if (groupsWithRole != null) {
				data.put("groupsWithRole", groupsWithRole);
			}

			data.put("dateRange", dateRange);
			data.put("beforeDate", beforeDate);
			data.put("afterDate", afterDate);
			data.put("excludeAssigned", excludeAssigned);
			return new ModelAndView("devices/list", data);
		} catch (OpenIoTException e) {
			LOGGER.error(e);
			return showError(e.getMessage());
		}
	}

	/**
	 * Display the "device detail" page.
	 * 
	 * @param hardwareId
	 * @return
	 */
	@RequestMapping("/devices/detail")
	public ModelAndView deviceDetail(@RequestParam("hardwareId") String hardwareId) {
		if (hardwareId != null) {
			try {
				Map<String, Object> data = createBaseData();
				IDeviceManagement management = OpenIoT.getServer().getDeviceManagement();
				IDevice device = management.getDeviceByHardwareId(hardwareId);
				if (device != null) {
					IDeviceSpecification specification =
							management.getDeviceSpecificationByToken(device.getSpecificationToken());
					data.put("device", device);
					data.put("specification", specification);
					return new ModelAndView("devices/detail", data);
				}
				return showError("Device for hardware id '" + hardwareId + "' not found.");
			} catch (OpenIoTException e) {
				LOGGER.error(e);
				return showError(e.getMessage());
			}
		}
		return showError("No device hardware id passed.");
	}

	/**
	 * Display the "list device groups" page.
	 * 
	 * @return
	 */
	@RequestMapping("/groups/list")
	public ModelAndView listDeviceGroups() {
		try {
			Map<String, Object> data = createBaseData();
			return new ModelAndView("groups/list", data);
		} catch (OpenIoTException e) {
			LOGGER.error(e);
			return showError(e.getMessage());
		}
	}

	/**
	 * Display the "device group detail" page.
	 * 
	 * @param groupToken
	 * @return
	 */
	@RequestMapping("/groups/detail")
	public ModelAndView deviceGroupDetail(@RequestParam("groupToken") String groupToken) {
		if (groupToken != null) {
			try {
				Map<String, Object> data = createBaseData();
				IDeviceManagement management = OpenIoT.getServer().getDeviceManagement();
				IDeviceGroup group = management.getDeviceGroup(groupToken);
				if (group != null) {
					data.put("group", group);
					return new ModelAndView("groups/detail", data);
				}
				return showError("Device group for token '" + groupToken + "' not found.");
			} catch (OpenIoTException e) {
				LOGGER.error(e);
				return showError(e.getMessage());
			}
		}
		return showError("No group token passed.");
	}

	/**
	 * Display the "list batch operations" page.
	 * 
	 * @return
	 */
	@RequestMapping("/batch/list")
	public ModelAndView listBatchOperations() {
		Tracer.start(TracerCategory.AdminUserInterface, "listBatchOperations", LOGGER);
		try {
			try {
				Map<String, Object> data = createBaseData();
				return new ModelAndView("batch/list", data);
			} catch (OpenIoTException e) {
				LOGGER.error(e);
				return showError(e.getMessage());
			}
		} finally {
			Tracer.stop(LOGGER);
		}
	}

	/**
	 * View details about a batch command invocation.
	 * 
	 * @param batchToken
	 * @return
	 */
	@RequestMapping("/batch/command")
	public ModelAndView batchCommandInvocationDetail(@RequestParam("token") String batchToken) {
		if (batchToken != null) {
			try {
				Map<String, Object> data = createBaseData();
				IDeviceManagement management = OpenIoT.getServer().getDeviceManagement();
				IBatchOperation operation = management.getBatchOperation(batchToken);
				if (operation != null) {
					data.put("operation", operation);
					String commandToken =
							operation.getParameters().get(IBatchCommandInvocationRequest.PARAM_COMMAND_TOKEN);
					if (commandToken == null) {
						return showError("No command token set for batch operation.");
					}
					IDeviceCommand command = management.getDeviceCommandByToken(commandToken);
					data.put("command", new String(MarshalUtils.marshalJsonAsString(command)));
					return new ModelAndView("batch/command", data);
				}
				return showError("Batch operation for token '" + batchToken + "' not found.");
			} catch (OpenIoTException e) {
				LOGGER.error(e);
				return showError(e.getMessage());
			}
		}
		return showError("No batch operation token passed.");
	}

	/**
	 * Display the "list users" page.
	 * 
	 * @return
	 */
	@RequestMapping("/users/list")
	public ModelAndView listUsers() {
		try {
			Map<String, Object> data = createBaseData();
			return new ModelAndView("users/list", data);
		} catch (OpenIoTException e) {
			LOGGER.error(e);
			return showError(e.getMessage());
		}
	}

	/**
	 * Returns a {@link ModelAndView} that will display an error message.
	 * 
	 * @param message
	 * @return
	 */
	protected ModelAndView showError(String message) {
		try {
			Map<String, Object> data = createBaseData();
			data.put("message", message);
			return new ModelAndView("error", data);
		} catch (OpenIoTException e) {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("message", e.getMessage());
			return new ModelAndView("error", data);
		}
	}

	/**
	 * Create data structure and common objects passed to pages.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected Map<String, Object> createBaseData() throws OpenIoTException {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("version", VersionHelper.getVersion());
		data.put("currentUser", LoginManager.getCurrentlyLoggedInUser());
		return data;
	}
}