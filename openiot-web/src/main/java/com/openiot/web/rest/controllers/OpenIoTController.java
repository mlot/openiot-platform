/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.web.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openiot.rest.IOpenIoTWebConstants;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.OpenIoTSystemException;
import org.apache.log4j.Logger;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Base class for common controller functionality.
 * 
 * @author Derek Adams
 */
public class OpenIoTController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(OpenIoTController.class);

	/**
	 * Send message back to called indicating successful add.
	 * 
	 * @param response
	 */
	protected void handleSuccessfulAdd(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_CREATED);
		try {
			response.flushBuffer();
		} catch (IOException e) {
			// Ignore failed flush.
		}
	}

	/**
	 * Handles a system exception by setting the HTML response code and response headers.
	 * 
	 * @param e
	 * @param response
	 */
	@ExceptionHandler
	protected void handleSystemException(OpenIoTException e, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			String flexMode = request.getHeader("X-OpenIoT-Error-Mode");
			if (flexMode != null) {
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(response.getOutputStream(), e);
				response.flushBuffer();
			} else {
				if (e instanceof OpenIoTSystemException) {
					OpenIoTSystemException sse = (OpenIoTSystemException) e;
					String combined = sse.getCode() + ":" + e.getMessage();
					response.setHeader(IOpenIoTWebConstants.HEADER_OPENIOT_ERROR, e.getMessage());
					response.setHeader(IOpenIoTWebConstants.HEADER_OPENIOT_ERROR_CODE,
							String.valueOf(sse.getCode()));
					if (sse.hasHttpResponseCode()) {
						response.sendError(sse.getHttpResponseCode(), combined);
					} else {
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, combined);
					}
				} else {
					response.setHeader(IOpenIoTWebConstants.HEADER_OPENIOT_ERROR, e.getMessage());
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
				}
				LOGGER.error("Exeception thrown during REST processing.", e);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Handles uncaught runtime exceptions such as null pointers.
	 * 
	 * @param e
	 * @param response
	 */
	@ExceptionHandler
	protected void handleRuntimeException(RuntimeException e, HttpServletResponse response) {
		LOGGER.error("Unhandled runtime exception.", e);
		try {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			LOGGER.error("Unhandled runtime exception.", e);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Handles exceptions generated if {@link Secured} annotations are not satisfied.
	 * 
	 * @param e
	 * @param response
	 */
	@ExceptionHandler
	protected void handleAccessDenied(AccessDeniedException e, HttpServletResponse response) {
		try {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			LOGGER.error("Access denied.", e);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Handles situations where user does not pass exprected content for a POST.
	 * 
	 * @param e
	 * @param response
	 */
	@ExceptionHandler
	protected void handleMissingContent(HttpMessageNotReadableException e, HttpServletResponse response) {
		try {
			LOGGER.error("Error handling REST request..", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No body content passed for POST request.");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}