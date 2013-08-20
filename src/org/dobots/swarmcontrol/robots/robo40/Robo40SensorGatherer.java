package org.dobots.swarmcontrol.robots.robo40;

import org.dobots.robots.robo40.Robo40;
import org.dobots.robots.robo40.Robo40Types;
import org.dobots.swarmcontrol.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import robots.gui.SensorGatherer;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Robo40SensorGatherer extends SensorGatherer {

	private Robo40 m_oRobo40;
	
//	private EnumMap<ERobo40Sensors, Boolean> m_oSensorEnabled;
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

	private TextView[] txtSensorValues = new TextView[4];
	private TextView[] lblSensorValues = new TextView[4];
	
	public Robo40SensorGatherer(BaseActivity i_oActivity, Robo40 i_oRobo40) {
		super(i_oActivity, "Robo40SensorGatherer");
		m_oRobo40 = i_oRobo40;
		
//		m_oSensorEnabled = new EnumMap<ERobo40Sensors, Boolean>(ERobo40Sensors.class);
	
		setProperties();
		
		// set up the maps
		initialize();
		
		start();
	}
	
	public void setProperties() {
		txtSensorValues[0] = (TextView) m_oActivity.findViewById(R.id.txtRobo40_Sensor1Value);
		txtSensorValues[1] = (TextView) m_oActivity.findViewById(R.id.txtRobo40_Sensor2Value);
		txtSensorValues[2] = (TextView) m_oActivity.findViewById(R.id.txtRobo40_Sensor3Value);
		txtSensorValues[3] = (TextView) m_oActivity.findViewById(R.id.txtRobo40_Sensor4Value);

		lblSensorValues[0] = (TextView) m_oActivity.findViewById(R.id.lblRobo40_Sensor1Value);
		lblSensorValues[1] = (TextView) m_oActivity.findViewById(R.id.lblRobo40_Sensor2Value);
		lblSensorValues[2] = (TextView) m_oActivity.findViewById(R.id.lblRobo40_Sensor3Value);
		lblSensorValues[3] = (TextView) m_oActivity.findViewById(R.id.lblRobo40_Sensor4Value);
	}

	public void initialize() {
		// set up the maps
		m_bSensorRequestActive = false;
	}
	
	protected void execute() {

		if (m_oRobo40.isConnected()) {
			if (m_nEnableBitMask != 0 && !m_bSensorRequestActive) {
//				m_oRobo40.requestSensorData();
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
			case Robo40Types.SENSOR_DATA:
				JSONObject oJson = (JSONObject) myMessage.obj;
				JSONObject header;
				try {
					header = oJson.getJSONObject("header");
					if (header.getInt("type") != Robo40Types.SENSOR_DATA) {
						return;
					}
					updateGUI(oJson.getJSONArray("data"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	public void sendMessage(int message, Object data) {
		Utils.sendMessage(m_oSensorDataUiUpdater, message, data);
	}

	private void updateGUI(JSONArray oData) {
		
		for (int i = 0; i < 4; i++) {
			
			if (i < oData.length()) {
				try {
					JSONObject item = oData.getJSONObject(i);
					lblSensorValues[i].setText(item.getString("name") + ':');
					txtSensorValues[i].setText(getValue(item));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					lblSensorValues[i].setText("");
					txtSensorValues[i].setText("");
				}
			} else {
				lblSensorValues[i].setText("");
				txtSensorValues[i].setText("");
			}
			
		}
		
		m_bSensorRequestActive = false;
	}
	
	private String getValue(JSONObject item) throws JSONException {
		int type = item.getInt("type");
		switch(type) {
		case Robo40Types.INT_T:
			return Integer.toString(item.getInt("value"));
		case Robo40Types.DOUBLE_T:
			return Double.toString(item.getInt("value"));
		case Robo40Types.STRING_T:
			return item.getString("value");
		case Robo40Types.BOOL_T:
			return Boolean.toString(item.getBoolean("value"));
		default:
			return "";
		}
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}
	
}
