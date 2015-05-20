/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.rest.client;

import com.openiot.rest.IOpenIoTWebConstants;
import com.openiot.spi.OpenIoTSystemException;
import com.openiot.spi.error.ErrorCode;
import com.openiot.spi.error.ErrorLevel;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.List;

/**
 * Uses extra information passed by OpenIoT in headers to provide more information about errors.
 * 
 * @author Derek
 */
public class OpenIoTErrorHandler implements ResponseErrorHandler {

	/** Delegate to default error handler */
	private ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.client.ResponseErrorHandler#handleError(org.springframework.http.client.
	 * ClientHttpResponse)
	 */
	public void handleError(ClientHttpResponse response) throws IOException {
		String errorCode = null;
		List<String> codeList = response.getHeaders().get(IOpenIoTWebConstants.HEADER_OPENIOT_ERROR_CODE);
		if ((codeList != null) && (codeList.size() > 0)) {
			errorCode = codeList.get(0);
		}
		try {
			errorHandler.handleError(response);
		} catch (RestClientException e) {
			if (errorCode != null) {
				ErrorCode code = ErrorCode.valueOf(errorCode);
				throw new OpenIoTSystemException(code, ErrorLevel.ERROR, response.getRawStatusCode());
			} else {
				throw new OpenIoTSystemException(ErrorCode.Unknown, ErrorLevel.ERROR,
						response.getRawStatusCode());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.client.ResponseErrorHandler#hasError(org.springframework.http.client.
	 * ClientHttpResponse)
	 */
	public boolean hasError(ClientHttpResponse response) throws IOException {
		return errorHandler.hasError(response);
	}
}