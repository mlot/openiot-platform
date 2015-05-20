/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.twilio;

import com.openiot.device.provisioning.sms.SmsParameters;
import com.openiot.server.lifecycle.LifecycleComponent;
import com.openiot.spi.OpenIoTException;
import com.openiot.spi.device.IDeviceAssignment;
import com.openiot.spi.device.IDeviceNestingContext;
import com.openiot.spi.device.command.IDeviceCommandExecution;
import com.openiot.spi.device.provisioning.ICommandDeliveryProvider;
import com.openiot.spi.server.lifecycle.LifecycleComponentType;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Account;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link ICommandDeliveryProvider} that sends an SMS message via
 * Twilio.
 * 
 * @author Derek
 */
public class TwilioCommandDeliveryProvider extends LifecycleComponent implements
		ICommandDeliveryProvider<String, SmsParameters> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(TwilioCommandDeliveryProvider.class);

	/** Account SID */
	private String accountSid;

	/** Auth token */
	private String authToken;

	/** Phone number to send command from */
	private String fromPhoneNumber;

	/** Client for Twilio REST calls */
	private TwilioRestClient twilio;

	/** Twilio account */
	private Account account;

	public TwilioCommandDeliveryProvider() {
		super(LifecycleComponentType.CommandDeliveryProvider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILifecycleComponent#start()
	 */
	@Override
	public void start() throws OpenIoTException {
		if (getAccountSid() == null) {
			throw new OpenIoTException("Twilio command delivery provider missing account SID.");
		}
		if (getAuthToken() == null) {
			throw new OpenIoTException("Twilio command delivery provider missing auth token.");
		}
		this.twilio = new TwilioRestClient(getAccountSid(), getAuthToken());
		this.account = twilio.getAccount();
		LOGGER.info("Twilio delivery provider started. Calls will originate from " + getFromPhoneNumber()
				+ ".");
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
	 * @see ILifecycleComponent#stop()
	 */
	@Override
	public void stop() throws OpenIoTException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ICommandDeliveryProvider#deliver(com.openiot
	 * .spi.device.IDeviceNestingContext, IDeviceAssignment,
	 * IDeviceCommandExecution, java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public void deliver(IDeviceNestingContext nested, IDeviceAssignment assignment,
			IDeviceCommandExecution execution, String encoded, SmsParameters params)
			throws OpenIoTException {
		LOGGER.info("Delivering SMS command to " + params.getSmsPhoneNumber() + ".");
		sendSms(encoded, getFromPhoneNumber(), params.getSmsPhoneNumber());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ICommandDeliveryProvider#deliverSystemCommand
	 * (IDeviceNestingContext,
	 * IDeviceAssignment, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void deliverSystemCommand(IDeviceNestingContext nested, IDeviceAssignment assignment,
			String encoded, SmsParameters params) throws OpenIoTException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Send an SMS message.
	 * 
	 * @param message
	 * @param from
	 * @param to
	 * @throws com.openiot.spi.OpenIoTException
	 */
	protected void sendSms(String message, String from, String to) throws OpenIoTException {
		MessageFactory messageFactory = account.getMessageFactory();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("To", to));
		params.add(new BasicNameValuePair("From", from));
		params.add(new BasicNameValuePair("Body", message));
		try {
			messageFactory.create(params);
		} catch (TwilioRestException e) {
			throw new OpenIoTException("Unable to send Twilio SMS message.", e);
		}
	}

	public String getAccountSid() {
		return accountSid;
	}

	public void setAccountSid(String accountSid) {
		this.accountSid = accountSid;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getFromPhoneNumber() {
		return fromPhoneNumber;
	}

	public void setFromPhoneNumber(String fromPhoneNumber) {
		this.fromPhoneNumber = fromPhoneNumber;
	}
}