package org.dobots.swarmcontrol.robots.dotty;

import java.util.EnumMap;

import org.dobots.robots.dotty.Dotty;
import org.dobots.robots.dotty.DottyTypes;
import org.dobots.robots.dotty.DottyTypes.EDottySensors;
import org.dobots.robots.dotty.DottyTypes.SensorData;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.SensorGatherer;
import org.dobots.utility.Utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DottySensorGatherer extends SensorGatherer {

	private Dotty m_oDotty;
	
	private EnumMap<EDottySensors, Boolean> m_oSensorEnabled;
	private boolean m_bSensorRequestActive = false;
	private int m_nEnableBitMask = 0;
	
	TextView txtDistanceValue;
	TextView txtLightValue;
	TextView txtSoundValue;
	TextView txtBatteryValue;
	TextView txtMotorAValue;
	TextView txtMotorBValue;
	
	LinearLayout layDistanceValue;
	LinearLayout layLightValue;
	LinearLayout laySoundValue;
	LinearLayout layBatteryValue;
	LinearLayout layMotorAValue;
	LinearLayout layMotorBValue;
	
	public DottySensorGatherer(Activity i_oActivity, Dotty i_oDotty) {
		super(i_oActivity);
		m_oDotty = i_oDotty;
		
		m_oSensorEnabled = new EnumMap<EDottySensors, Boolean>(EDottySensors.class);
	
		setProperties();
		
		// set up the maps
		initialize();
		
		start();
	}
	
	public void setProperties() {
		txtDistanceValue = (TextView) m_oActivity.findViewById(R.id.txtDotty_DistanceValue);
		txtLightValue = (TextView) m_oActivity.findViewById(R.id.txtDotty_LightValue);
		txtSoundValue = (TextView) m_oActivity.findViewById(R.id.txtDotty_SoundValue);
		txtBatteryValue = (TextView) m_oActivity.findViewById(R.id.txtDotty_BatteryValue);
		txtMotorAValue = (TextView) m_oActivity.findViewById(R.id.txtDotty_MotorSensorAValue);
		txtMotorBValue = (TextView) m_oActivity.findViewById(R.id.txtDotty_MotorSensorBValue);
		
		layDistanceValue = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_DistanceValue);
		layLightValue = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_LightValue);
		laySoundValue = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_SoundValue);
		layBatteryValue = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_BatteryValue);
		layMotorAValue = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_MotorSensorAValue);
		layMotorBValue = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_MotorSensorBValue);
	}

	public void initialize() {
		// set up the maps
		for (EDottySensors sensor : EDottySensors.values()) {
			m_oSensorEnabled.put(sensor, false);
		}
		m_bSensorRequestActive = false;
	}
	
	protected void execute() {

		if (m_oDotty.isConnected()) {
			if (m_nEnableBitMask != 0 && !m_bSensorRequestActive) {
//				m_oDotty.requestSensorData();
			}
		}
		
		Utils.waitSomeTime(500);
	}

	/**
	 * Receive messages from the BTCommunicator
	 */
	final Handler m_oSensorDataUiUpdater = new Handler() {
		@Override
		public void handleMessage(Message myMessage) {
			switch(myMessage.what) {
			case DottyTypes.SENSOR_DATA:
				SensorData oData = (SensorData) myMessage.obj;
				updateGUI(oData);
			}
		}
	};
	
	public void sendMessage(int message, Object data) {
		Utils.sendMessage(m_oSensorDataUiUpdater, message, data);
	}

	private void updateGUI(SensorData oData) {
		
		for (EDottySensors eSensor : EDottySensors.values()) {
			if (m_oSensorEnabled.get(eSensor)) {
				switch(eSensor) {
				case sensor_Battery:
					setText(txtBatteryValue, oData.nBattery);
					break;
				case sensor_Dist:
					setText(txtDistanceValue, oData.nDistance);
					break;
				case sensor_Light:
					setText(txtLightValue, oData.nLight);
					break;
				case sensor_Motor1:
					setText(txtMotorAValue, oData.nMotor1);
					break;
				case sensor_Motor2:
					setText(txtMotorBValue, oData.nMotor2);
					break;
				case sensor_Sound:
					setText(txtSoundValue, oData.nSound);
					break;
				}
			}
		}
		
		m_bSensorRequestActive = false;
	}
	
	private void setText(TextView i_oView, int i_nValue) {
		i_oView.setText(String.valueOf(i_nValue));
	}

	public void enableSensor(EDottySensors i_eSensor, boolean i_bEnabled) {
		m_oSensorEnabled.put(i_eSensor, i_bEnabled);
		if (i_bEnabled) {
			m_nEnableBitMask = Utils.setBit(m_nEnableBitMask, i_eSensor.ordinal());
		} else {
			m_nEnableBitMask = Utils.clearBit(m_nEnableBitMask, i_eSensor.ordinal());
		}
		
		showSensor(i_eSensor, i_bEnabled);
	}
	
	public void showSensor(EDottySensors i_eSensor, boolean i_bShow) {
		switch(i_eSensor) {
		case sensor_Battery:
			showLayout(layBatteryValue, i_bShow);
			break;
		case sensor_Dist:
			showLayout(layDistanceValue, i_bShow);
			break;
		case sensor_Light:
			showLayout(layLightValue, i_bShow);
			break;
		case sensor_Motor1:
			showLayout(layMotorAValue, i_bShow);
			break;
		case sensor_Motor2:
			showLayout(layMotorBValue, i_bShow);
			break;
		case sensor_Sound:
			showLayout(laySoundValue, i_bShow);
			break;
		}
	}
	
	private void showLayout(View v, boolean i_bShow) {
		if (i_bShow) {
			v.setVisibility(View.VISIBLE);
		} else {
			v.setVisibility(View.GONE);
		}
	}

}
