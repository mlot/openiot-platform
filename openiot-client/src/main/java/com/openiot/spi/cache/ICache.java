/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.spi.cache;

import com.openiot.spi.OpenIoTException;

/**
 * Interface for a cache that stores objects by id.
 * 
 * @author Derek
 * 
 * @param <K>
 * @param <V>
 */
public interface ICache<K, V> {

	/**
	 * Get the cache type.
	 * 
	 * @return
	 */
	public CacheType getType();

	/**
	 * Get value based on a given key.
	 * 
	 * @param key
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public V get(K key) throws OpenIoTException;

	/**
	 * Add or replace value for the given key.
	 * 
	 * @param key
	 * @param Value
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void put(K key, V value) throws OpenIoTException;

	/**
	 * Remove an element from the cache.
	 * 
	 * @param key
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void remove(K key) throws OpenIoTException;

	/**
	 * Clear all elements from cache.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void clear() throws OpenIoTException;

	/**
	 * Get count of elements currently in cache.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public int getElementCount() throws OpenIoTException;

	/**
	 * Get the number of requests made to the cache.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public long getRequestCount() throws OpenIoTException;

	/**
	 * Get the number of cache hits.
	 * 
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public long getHitCount() throws OpenIoTException;
}