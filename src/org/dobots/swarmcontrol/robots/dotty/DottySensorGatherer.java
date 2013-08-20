package org.dobots.swarmcontrol.robots.dotty;

import java.util.EnumMap;

import org.dobots.robots.dotty.Dotty;
import org.dobots.robots.dotty.DottyTypes;
import org.dobots.robots.dotty.DottyTypes.EDottySensors;
import org.dobots.robots.dotty.DottyTypes.SensorData;
import org.dobots.swarmcontrol.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;

import robots.gui.SensorGatherer;
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
	TextView txtMotor1Value;
	TextView txtMotor2Value;
	TextView txtWheel1Value;
	TextView txtWheel2Value;
	TextView txtLed1Value;
	TextView txtLed2Value;
	TextView txtLed3Value;
	
	LinearLayout layDistanceValue;
	LinearLayout layLightValue;
	LinearLayout laySoundValue;
	LinearLayout layBatteryValue;
	LinearLayout layMotor1Value;
	LinearLayout layMotor2Value;
	LinearLayout layWheel1Value;
	LinearLayout layWheel2Value;
	LinearLayout layLed1Value;
	LinearLayout layLed2Value;
	LinearLayout layLed3Value;
	
	public DottySensorGatherer(BaseActivity i_oActivity, Dotty i_oDotty) {
		super(i_oActivity, "DottySensorGatherer");
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
		txtMotor1Value = (TextView) m_oActivity.findViewById(R.id.txtDotty_MotorSensor1Value);
		txtMotor2Value = (TextView) m_oActivity.findViewById(R.id.txtDotty_MotorSensor2Value);
		txtWheel1Value = (TextView) m_oActivity.findViewById(R.id.txtDotty_Wheel1Value);
		txtWheel2Value = (TextView) m_oActivity.findViewById(R.id.txtDotty_Wheel2Value);
		txtLed1Value = (TextView) m_oActivity.findViewById(R.id.txtDotty_Led1Value);
		txtLed2Value = (TextView) m_oActivity.findViewById(R.id.txtDotty_Led2Value);
		txtLed3Value = (TextView) m_oActivity.findViewById(R.id.txtDotty_Led3Value);
		
		layDistanceValue = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_DistanceValue);
		layLightValue = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_LightValue);
		laySoundValue = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_SoundValue);
		layBatteryValue = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_BatteryValue);
		layMotor1Value = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_MotorSensor1Value);
		layMotor2Value = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_MotorSensor2Value);
		layWheel1Value = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_Wheel1Value);
		layWheel2Value = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_Wheel2Value);
		layLed1Value = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_Led1Value);
		layLed2Value = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_Led2Value);
		layLed3Value = (LinearLayout) m_oActivity.findViewById(R.id.layDotty_Led3Value);
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
					setText(txtMotor1Value, oData.nMotor1);
					break;
				case sensor_Motor2:
					setText(txtMotor2Value, oData.nMotor2);
					break;
				case sensor_Sound:
					setText(txtSoundValue, oData.nSound);
					break;
				case sensor_Wheel1:
					setText(txtWheel1Value, oData.nWheel1);
					break;
				case sensor_Wheel2:
					setText(txtWheel2Value, oData.nWheel2);
					break;
				case sensor_Led1:
					setOnOffText(txtLed1Value, oData.bLed1ON);
					break;
				case sensor_Led2:
					setOnOffText(txtLed2Value, oData.bLed2ON);
					break;
				case sensor_Led3:
					setOnOffText(txtLed3Value, oData.bLed3ON);
					break;
				}
			}
		}
		
		m_bSensorRequestActive = false;
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
			showLayout(layMotor1Value, i_bShow);
			break;
		case sensor_Motor2:
			showLayout(layMotor2Value, i_bShow);
			break;
		case sensor_Sound:
			showLayout(laySoundValue, i_bShow);
			break;
		case sensor_Wheel1:
			showLayout(layWheel1Value, i_bShow);
			break;
		case sensor_Wheel2:
			showLayout(layWheel2Value, i_bShow);
			break;
		case sensor_Led1:
			showLayout(layLed1Value, i_bShow);
			break;
		case sensor_Led2:
			showLayout(layLed2Value, i_bShow);
			break;
		case sensor_Led3:
			showLayout(layLed3Value, i_bShow);
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

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

}
