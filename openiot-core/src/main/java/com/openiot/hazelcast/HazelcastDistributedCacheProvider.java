/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hazelcast;

import com.hazelcast.core.IMap;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.cache.CacheType;
import com.openiot.spi.cache.ICache;
import com.openiot.spi.device.*;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Implements {@link IDeviceManagementCacheProvider} using Hazelcast as a distributed
 * cache.
 * 
 * @author Derek
 */
public class HazelcastDistributedCacheProvider extends LifecycleComponent implements
		IDeviceManagementCacheProvider {

	public HazelcastDistributedCacheProvider() {
		super(LifecycleComponentType.CacheProvider);
	}

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(HazelcastDistributedCacheProvider.class);

	/** Name of site cache */
	private static final String SITE_CACHE = "siteCache";

	/** Name of device specification cache */
	private static final String SPECIFICATION_CACHE = "specificationCache";

	/** Name of device cache */
	private static final String DEVICE_CACHE = "deviceCache";

	/** Name of assignment cache */
	private static final String ASSIGNMENT_CACHE = "assignmentCache";

	/** Hazelcast configuration */
	private OpenIoTHazelcastConfiguration configuration;

	/** Cache for sites */
	private HazelcastCache<ISite> siteCache;

	/** Cache for device specifications */
	private HazelcastCache<IDeviceSpecification> specificationCache;

	/** Cache for devices */
	private HazelcastCache<IDevice> deviceCache;

	/** Cache for device assignments */
	private HazelcastCache<IDeviceAssignment> assignmentCache;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		this.siteCache = new HazelcastCache<ISite>(SITE_CACHE, CacheType.SiteCache);
		this.specificationCache =
				new HazelcastCache<IDeviceSpecification>(SPECIFICATION_CACHE,
						CacheType.DeviceSpecificationCache);
		this.deviceCache = new HazelcastCache<IDevice>(DEVICE_CACHE, CacheType.DeviceCache);
		this.assignmentCache =
				new HazelcastCache<IDeviceAssignment>(ASSIGNMENT_CACHE, CacheType.DeviceAssignmentCache);
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
	 * @see ILifecycleComponent#getLogger()
	 */
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagementCacheProvider#getSiteCache()
	 */
	@Override
	public ICache<String, ISite> getSiteCache() throws OpenIoTException {
		return siteCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagementCacheProvider#getDeviceSpecificationCache
	 * ()
	 */
	@Override
	public ICache<String, IDeviceSpecification> getDeviceSpecificationCache() throws OpenIoTException {
		return specificationCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IDeviceManagementCacheProvider#getDeviceCache()
	 */
	@Override
	public ICache<String, IDevice> getDeviceCache() throws OpenIoTException {
		return deviceCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IDeviceManagementCacheProvider#getDeviceAssignmentCache()
	 */
	@Override
	public ICache<String, IDeviceAssignment> getDeviceAssignmentCache() throws OpenIoTException {
		return assignmentCache;
	}

	public OpenIoTHazelcastConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(OpenIoTHazelcastConfiguration configuration) {
		this.configuration = configuration;
	}

	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	private class HazelcastCache<T> implements ICache<String, T> {

		/** Name of Hazelcast map */
		private String name;

		/** Cache type */
		private CacheType type;

		/** Hazelcast map used as cache */
		private IMap hMap;

		/** Count of total cache requests */
		private AtomicLong requestCount = new AtomicLong();

		/** Count of total cache hits */
		private AtomicLong hitCount = new AtomicLong();

		public HazelcastCache(String name, CacheType type) {
			this.name = name;
			this.type = type;
			this.hMap = configuration.getHazelcastInstance().getMap(name);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ICache#getType()
		 */
		@Override
		public CacheType getType() {
			return type;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ICache#get(java.lang.Object)
		 */
		@Override
		public T get(String key) throws OpenIoTException {
			T result = (T) hMap.get(key);
			requestCount.incrementAndGet();
			if (result != null) {
				hitCount.incrementAndGet();
			}
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ICache#put(java.lang.Object, java.lang.Object)
		 */
		@Override
		public void put(String key, T value) throws OpenIoTException {
			hMap.put(key, value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ICache#remove(java.lang.Object)
		 */
		@Override
		public void remove(String key) throws OpenIoTException {
			hMap.remove(key);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ICache#clear()
		 */
		@Override
		public void clear() throws OpenIoTException {
			hMap.clear();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ICache#getElementCount()
		 */
		@Override
		public int getElementCount() throws OpenIoTException {
			return hMap.size();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ICache#getRequestCount()
		 */
		@Override
		public long getRequestCount() throws OpenIoTException {
			return requestCount.get();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ICache#getHitCount()
		 */
		@Override
		public long getHitCount() throws OpenIoTException {
			return hitCount.get();
		}
	}
}