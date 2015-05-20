/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.ehcache;

import com.openiot.spi.OpenIoTException;
import com.openiot.spi.cache.CacheType;
import com.openiot.spi.cache.ICache;
import com.openiot.spi.device.IDeviceManagementCacheProvider;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Wraps {@link Cache} with support for generics and {@link ICache} interface for use in
 * {@link IDeviceManagementCacheProvider} implementation.
 * 
 * @author Derek
 * 
 * @param <K>
 * @param <V>
 */
public class CacheAdapter<K, V> implements ICache<K, V> {

	/** Static logger instance */
	// private static Logger LOGGER = Logger.getLogger(CacheAdapter.class);

	/** Cache type */
	private CacheType type;

	/** Wrapped cache */
	private Ehcache cache;

	/** Counts to number of requests */
	private AtomicLong requestCount;

	/** Counts the number of hits */
	private AtomicLong hitCount;

	public CacheAdapter(CacheType type, Ehcache cache) {
		this.type = type;
		this.cache = cache;
		this.requestCount = new AtomicLong();
		this.hitCount = new AtomicLong();
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
	@SuppressWarnings("unchecked")
	public V get(K key) throws OpenIoTException {
		requestCount.incrementAndGet();
		Element match = cache.get(key);
		if (match == null) {
			return null;
		}
		hitCount.incrementAndGet();
		return (V) match.getObjectValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ICache#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void put(K key, V value) throws OpenIoTException {
		cache.put(new Element(key, value));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ICache#remove(java.lang.Object)
	 */
	@Override
	public void remove(K key) throws OpenIoTException {
		cache.remove(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ICache#clear()
	 */
	@Override
	public void clear() throws OpenIoTException {
		cache.removeAll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ICache#getElementCount()
	 */
	@Override
	public int getElementCount() throws OpenIoTException {
		return cache.getSize();
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