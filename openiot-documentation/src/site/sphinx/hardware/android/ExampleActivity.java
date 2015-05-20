package com.swandroid;

import android.app.Activity;
import android.app.Service;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.openiot.android.messaging.OpenIoTMessagingException;
import com.openiot.android.mqtt.MqttService;
import com.openiot.android.mqtt.preferences.MqttServicePreferences;
import com.openiot.android.protobuf.OpenIoTProtobufActivity;
import com.openiot.device.provisioning.protobuf.proto.Sitewhere.Device.Header;
import com.openiot.device.provisioning.protobuf.proto.Sitewhere.Device.RegistrationAck;

/**
 * Example of {@link Activity} that can communicate with OpenIoT.
 * 
 * @author Derek
 */
public class ExampleActivity extends OpenIoTProtobufActivity {

	/** Tag for logging */
	private static final String TAG = "ExampleActivity";

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Manually set up MQTT information.
		MqttServicePreferences mqtt = new MqttServicePreferences();
		mqtt.setBrokerHostname("54.237.72.168");
		mqtt.setBrokerPort(1883);
		mqtt.setDeviceHardwareId(getUniqueDeviceId());
		MqttServicePreferences.update(mqtt, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.android.OpenIoTActivity#getServiceClass()
	 */
	@Override
	protected Class<? extends Service> getServiceClass() {
		return MqttService.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.android.OpenIoTActivity#getServiceConfiguration()
	 */
	@Override
	protected Parcelable getServiceConfiguration() {
		return MqttServicePreferences.read(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		Log.d(TAG, "About to connect to OpenIoT.");
		connectToOpenIoT();
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		Log.d(TAG, "About to disconnect from OpenIoT.");
		disconnectFromOpenIoT();
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.android.OpenIoTActivity#onConnectedToOpenIoT()
	 */
	@Override
	protected void onConnectedToOpenIoT() {
		Log.d(TAG, "Connected to OpenIoT.");
		try {
			registerDevice(getUniqueDeviceId(), "d2604433-e4eb-419b-97c7-88efe9b2cd41", null);
		} catch (OpenIoTMessagingException e) {
			Log.e(TAG, "Unable to send device registration.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openiot.android.protobuf.OpenIoTProtobufActivity#handleRegistrationAck(com.openiot.device
	 * .provisioning.protobuf.proto.Sitewhere.Device.Header,
	 * com.openiot.device.provisioning.protobuf.proto.Sitewhere.Device.RegistrationAck)
	 */
	@Override
	public void handleRegistrationAck(Header header, RegistrationAck ack) {
		switch (ack.getState()) {
		case ALREADY_REGISTERED: {
			Log.d(TAG, "Device was already registered.");
			break;
		}
		case NEW_REGISTRATION: {
			Log.d(TAG, "Device was registered successfully.");
			break;
		}
		case REGISTRATION_ERROR: {
			Log.d(TAG,
					"Error registering device. " + ack.getErrorType().name() + ": " + ack.getErrorMessage());
			break;
		}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openiot.android.OpenIoTActivity#onDisconnectedFromOpenIoT()
	 */
	@Override
	protected void onDisconnectedFromOpenIoT() {
		Log.d(TAG, "Disconnected from OpenIoT.");
	}
}