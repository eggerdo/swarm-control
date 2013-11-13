package org.dobots.swarmcontrol.robots.piratedotty;

import org.dobots.robots.piratedotty.PirateDotty;
import org.dobots.robots.piratedotty.PirateDottyTypes;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;

import robots.gui.SensorGatherer;
import android.os.Handler;
import android.os.Message;
import android.view.View;

public class PirateDottySensorGatherer extends SensorGatherer {

	private PirateDotty m_oPirateDotty;
	
//	private EnumMap<EPirateDottySensors, Boolean> m_oSensorEnabled;
//	private boolean m_bSensorRequestActive = false;
//	private int m_nEnableBitMask = 0;
//	
//	TextView txtDistanceValue;
//	TextView txtLightValue;
//	TextView txtSoundValue;
//	TextView txtBatteryValue;
//	TextView txtMotor1Value;
//	TextView txtMotor2Value;
//	TextView txtWheel1Value;
//	TextView txtWheel2Value;
//	TextView txtLed1Value;
//	TextView txtLed2Value;
//	TextView txtLed3Value;
//	
//	LinearLayout layDistanceValue;
//	LinearLayout layLightValue;
//	LinearLayout laySoundValue;
//	LinearLayout layBatteryValue;
//	LinearLayout layMotor1Value;
//	LinearLayout layMotor2Value;
//	LinearLayout layWheel1Value;
//	LinearLayout layWheel2Value;
//	LinearLayout layLed1Value;
//	LinearLayout layLed2Value;
//	LinearLayout layLed3Value;
	
	public PirateDottySensorGatherer(BaseActivity i_oActivity, PirateDotty i_oPirateDotty) {
		super(i_oActivity, "PirateDottySensorGatherer");
		m_oPirateDotty = i_oPirateDotty;
		
//		m_oSensorEnabled = new EnumMap<EPirateDottySensors, Boolean>(EPirateDottySensors.class);
	
		setProperties();
		
		// set up the maps
		initialize();
		
		start();
	}
	
	public void setProperties() {
//		txtDistanceValue = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_DistanceValue);
//		txtLightValue = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_LightValue);
//		txtSoundValue = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_SoundValue);
//		txtBatteryValue = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_BatteryValue);
//		txtMotor1Value = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_MotorSensor1Value);
//		txtMotor2Value = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_MotorSensor2Value);
//		txtWheel1Value = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_Wheel1Value);
//		txtWheel2Value = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_Wheel2Value);
//		txtLed1Value = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_Led1Value);
//		txtLed2Value = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_Led2Value);
//		txtLed3Value = (TextView) m_oActivity.findViewById(R.id.txtPirateDotty_Led3Value);
//		
//		layDistanceValue = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_DistanceValue);
//		layLightValue = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_LightValue);
//		laySoundValue = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_SoundValue);
//		layBatteryValue = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_BatteryValue);
//		layMotor1Value = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_MotorSensor1Value);
//		layMotor2Value = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_MotorSensor2Value);
//		layWheel1Value = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_Wheel1Value);
//		layWheel2Value = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_Wheel2Value);
//		layLed1Value = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_Led1Value);
//		layLed2Value = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_Led2Value);
//		layLed3Value = (LinearLayout) m_oActivity.findViewById(R.id.layPirateDotty_Led3Value);
	}

	public void initialize() {
		// set up the maps
//		for (EPirateDottySensors sensor : EPirateDottySensors.values()) {
//			m_oSensorEnabled.put(sensor, false);
//		}
//		m_bSensorRequestActive = false;
	}
	
	protected void execute() {

//		if (m_oPirateDotty.isConnected()) {
//			if (m_nEnableBitMask != 0 && !m_bSensorRequestActive) {
//			}
//		}
		
		Utils.waitSomeTime(500);
	}

	/**
	 * Receive messages from the BTCommunicator
	 */
	final Handler m_oSensorDataUiUpdater = new Handler() {
		@Override
		public void handleMessage(Message myMessage) {
			switch(myMessage.what) {
			case PirateDottyTypes.SENSOR_DATA:
//				SensorData oData = (SensorData) myMessage.obj;
//				updateGUI(oData);
			}
		}
	};
	
	public void sendMessage(int message, Object data) {
		Utils.sendMessage(m_oSensorDataUiUpdater, message, data);
	}

//	private void updateGUI(SensorData oData) {
//		
//		for (EPirateDottySensors eSensor : EPirateDottySensors.values()) {
//			if (m_oSensorEnabled.get(eSensor)) {
//				switch(eSensor) {
//				case sensor_Battery:
//					setText(txtBatteryValue, oData.nBattery);
//					break;
//				case sensor_Dist:
//					setText(txtDistanceValue, oData.nDistance);
//					break;
//				case sensor_Light:
//					setText(txtLightValue, oData.nLight);
//					break;
//				case sensor_Motor1:
//					setText(txtMotor1Value, oData.nMotor1);
//					break;
//				case sensor_Motor2:
//					setText(txtMotor2Value, oData.nMotor2);
//					break;
//				case sensor_Sound:
//					setText(txtSoundValue, oData.nSound);
//					break;
//				case sensor_Wheel1:
//					setText(txtWheel1Value, oData.nWheel1);
//					break;
//				case sensor_Wheel2:
//					setText(txtWheel2Value, oData.nWheel2);
//					break;
//				case sensor_Led1:
//					setOnOffText(txtLed1Value, oData.bLed1ON);
//					break;
//				case sensor_Led2:
//					setOnOffText(txtLed2Value, oData.bLed2ON);
//					break;
//				case sensor_Led3:
//					setOnOffText(txtLed3Value, oData.bLed3ON);
//					break;
//				}
//			}
//		}
//		
//		m_bSensorRequestActive = false;
//	}
	
//	public void enableSensor(EPirateDottySensors i_eSensor, boolean i_bEnabled) {
//		m_oSensorEnabled.put(i_eSensor, i_bEnabled);
//		if (i_bEnabled) {
//			m_nEnableBitMask = Utils.setBit(m_nEnableBitMask, i_eSensor.ordinal());
//		} else {
//			m_nEnableBitMask = Utils.clearBit(m_nEnableBitMask, i_eSensor.ordinal());
//		}
//		
//		showSensor(i_eSensor, i_bEnabled);
//	}
	
//	public void showSensor(EPirateDottySensors i_eSensor, boolean i_bShow) {
//		switch(i_eSensor) {
//		case sensor_Battery:
//			showLayout(layBatteryValue, i_bShow);
//			break;
//		case sensor_Dist:
//			showLayout(layDistanceValue, i_bShow);
//			break;
//		case sensor_Light:
//			showLayout(layLightValue, i_bShow);
//			break;
//		case sensor_Motor1:
//			showLayout(layMotor1Value, i_bShow);
//			break;
//		case sensor_Motor2:
//			showLayout(layMotor2Value, i_bShow);
//			break;
//		case sensor_Sound:
//			showLayout(laySoundValue, i_bShow);
//			break;
//		case sensor_Wheel1:
//			showLayout(layWheel1Value, i_bShow);
//			break;
//		case sensor_Wheel2:
//			showLayout(layWheel2Value, i_bShow);
//			break;
//		case sensor_Led1:
//			showLayout(layLed1Value, i_bShow);
//			break;
//		case sensor_Led2:
//			showLayout(layLed2Value, i_bShow);
//			break;
//		case sensor_Led3:
//			showLayout(layLed3Value, i_bShow);
//			break;
//		}
//	}
	
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
