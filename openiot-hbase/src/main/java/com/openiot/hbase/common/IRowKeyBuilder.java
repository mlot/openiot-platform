/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.hbase.common;

import com.openiot.spi.OpenIoTException;

/**
 * Abstracts the idea of building a row key based on a hierarchical scheme.
 * 
 * @author Derek
 */
public interface IRowKeyBuilder {

	/**
	 * Get number of bytes (max of 8) saved as part of the record key.
	 * 
	 * @return
	 */
	public int getKeyIdLength();

	/**
	 * Throws an exception if an invalid key is referenced.
	 * 
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void throwInvalidKey() throws OpenIoTException;

	/**
	 * Gets identifier that marks a row of the given type.
	 * 
	 * @return
	 */
	public byte getTypeIdentifier();

	/**
	 * Get identifier that marks a row as primary.
	 * 
	 * @return
	 */
	public byte getPrimaryIdentifier();

	/**
	 * Builds a primary entity row key based on a unique token.
	 * 
	 * @param token
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] buildPrimaryKey(String token) throws OpenIoTException;

	/**
	 * Builds a subordinate key based on the type indicator.
	 * 
	 * @param token
	 * @param type
	 * @return
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public byte[] buildSubkey(String token, byte type) throws OpenIoTException;

	/**
	 * Deletes a reference to a token.
	 * 
	 * @param token
	 * @throws com.openiot.spi.OpenIoTException
	 */
	public void deleteReference(String token) throws OpenIoTException;
}