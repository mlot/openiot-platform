/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.server.lifecycle;

import com.openiot.spi.OpenIoTException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Lifecycle methods used in OpenIoT components.
 * 
 * @author Derek
 */
public interface ILifecycleComponent {

	/**
	 * Get the unique component id.
	 * 
	 * @return
	 */
	public String getComponentId();

	/**
	 * Get human-readable name shown for component.
	 * 
	 * @return
	 */
	public String getComponentName();

	/**
	 * Get component type.
	 * 
	 * @return
	 */
	public LifecycleComponentType getComponentType();

	/**
	 * Get current lifecycle status.
	 * 
	 * @return
	 */
	public LifecycleStatus getLifecycleStatus();

	/**
	 * Gets the last lifecycle error that occurred.
	 * 
	 * @return
	 */
	public OpenIoTException getLifecycleError();

	/**
	 * Get the list of contained {@link ILifecycleComponent} elements.
	 * 
	 * @return
	 */
	public List<ILifecycleComponent> getLifecycleComponents();

	/**
	 * Starts the component while keeping up with lifecycle information.
	 */
	public void lifecycleStart();

	/**
	 * Start the component.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void start() throws OpenIoTException;

	/**
	 * Pauses the component while keeping up with lifecycle information.
	 */
	public void lifecyclePause();

	/**
	 * Indicates to framework whether component can be paused.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public boolean canPause() throws OpenIoTException;

	/**
	 * Pause the component.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void pause() throws OpenIoTException;

	/**
	 * Stops the component while keeping up with lifecycle information.
	 */
	public void lifecycleStop();

	/**
	 * Stop the component.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void stop() throws OpenIoTException;

	/**
	 * Find components (including this component and nested components) that are of the
	 * given type.
	 * 
	 * @param type
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public List<ILifecycleComponent> findComponentsOfType(LifecycleComponentType type)
			throws OpenIoTException;

	/**
	 * Get component logger.
	 * 
	 * @return
	 */
	public Logger getLogger();

	/**
	 * Logs the state of this component and all nested components.
	 */
	public void logState();
}