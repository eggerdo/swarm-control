package org.dobots.swarmcontrol.robots;

import java.util.EnumMap;

import org.dobots.nxt.LCPMessage;
import org.dobots.nxt.NXT;
import org.dobots.nxt.NXTTypes;
import org.dobots.nxt.NXTTypes.DistanceData;
import org.dobots.nxt.NXTTypes.ENXTSensorID;
import org.dobots.nxt.NXTTypes.ENXTSensorType;
import org.dobots.nxt.NXTTypes.SensorData;
import org.dobots.roomba.RoombaTypes.ERoombaSensorPackages;
import org.dobots.swarmcontrol.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;

public class NXTSensorGatherer extends SensorGatherer {

	private NXT m_oNxt;
	
	private boolean m_bDebug;
	
	private EnumMap<ENXTSensorID, ENXTSensorType> m_oSensorTypes;
	private EnumMap<ENXTSensorID, Boolean> m_oSensorEnabled;
	private EnumMap<ENXTSensorID, Boolean> m_oRequestActive;
	
	
	public NXTSensorGatherer(Activity i_oActivity, NXT i_oNxt) {
		super(i_oActivity);
		m_oNxt = i_oNxt;
		
//		m_oGUIUpdater = new UpdateSensorDataTask();
		m_oSensorTypes = new EnumMap<ENXTSensorID, ENXTSensorType>(ENXTSensorID.class);
		m_oSensorEnabled = new EnumMap<ENXTSensorID, Boolean>(ENXTSensorID.class);
		m_oRequestActive = new EnumMap<ENXTSensorID, Boolean>(ENXTSensorID.class);
		
		// set up the maps
		for (ENXTSensorID sensor : ENXTSensorID.values()) {
			m_oSensorTypes.put(sensor, ENXTSensorType.sensType_None);
			m_oSensorEnabled.put(sensor, false);
			m_oRequestActive.put(sensor, false);
		}
		
		start();
	}
	
	@Override
	protected void execute() {
//		if (m_bEnabled && m_oNxt.isConnected()) {
//			
//		}
		for (ENXTSensorID sensor : m_oSensorEnabled.keySet()) {
			if (m_oSensorEnabled.get(sensor) && 
				!m_oRequestActive.get(sensor) &&
				m_oSensorTypes.get(sensor) != ENXTSensorType.sensType_None) {
				
				ENXTSensorType eType = m_oSensorTypes.get(sensor);
				m_oNxt.requestSensorData(sensor, eType);
				m_oRequestActive.put(sensor, true);
			}
		}
		
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
	public void sendMessage(int message) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", message);
        sendBundle(myBundle);
	}

    private void sendBundle(Bundle myBundle) {
        Message myMessage = new Message();
        myMessage.setData(myBundle);
        uiHandler.sendMessage(myMessage);
    }

//	private class UpdateSensorDataTask implements Runnable {
//
//		public void run() {
//			
//		}
//		
//	}

	public void setSensorType(ENXTSensorID i_eSensor, ENXTSensorType i_eSensorType) {
		if (m_oNxt.isConnected()) {
			m_oNxt.setSensorType(i_eSensor, i_eSensorType);
			m_oSensorTypes.put(i_eSensor, i_eSensorType);
		}
	}
	
	public void enableSensor(ENXTSensorID i_eSensor, boolean i_bEnabled) {
		m_oSensorEnabled.put(i_eSensor, i_bEnabled);
		showSensor(i_eSensor, i_bEnabled);
	}

	/**
	 * Receive messages from the BTCommunicator
	 */
	final Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message myMessage) {
			switch(myMessage.getData().getInt("message")) {
			case NXTTypes.SENSOR_DATA_RECEIVED:
				SensorData oSensorData = m_oNxt.getReceivedSensorData();
				updateGUI(oSensorData);
				break;
			case NXTTypes.DISTANCE_DATA_RECEIVED:
				DistanceData oDistanceData = m_oNxt.getReceivedDistanceData();
				updateGUI(oDistanceData);
				break;
			}
		}
	};
	
	private void updateGUI(DistanceData i_oDistanceData) {
		int nInputPort = i_oDistanceData.nInputPort;

    	int nResRawID, nResNormID, nResScaleID, nResCalibID;
    	
    	// get resource id based on sensor id
    	switch (nInputPort) {
    	case 0:
    		nResScaleID = R.id.txtSensor1ScaleValue;
    		nResRawID 	= R.id.txtSensor1RawValue;
    		nResNormID 	= R.id.txtSensor1NormValue;
    		nResCalibID = R.id.txtSensor1CalibValue;
    		break;
    	case 1:
    		nResRawID 	= R.id.txtSensor2RawValue;
    		nResNormID 	= R.id.txtSensor2NormValue;
    		nResScaleID = R.id.txtSensor2ScaleValue;
    		nResCalibID = R.id.txtSensor2CalibValue;
    		break;
    	case 2:
    		nResRawID 	= R.id.txtSensor3RawValue;
    		nResNormID 	= R.id.txtSensor3NormValue;
    		nResScaleID = R.id.txtSensor3ScaleValue;
    		nResCalibID = R.id.txtSensor3CalibValue;
    		break;
    	case 3:
    		nResRawID 	= R.id.txtSensor4RawValue;
    		nResNormID 	= R.id.txtSensor4NormValue;
    		nResScaleID = R.id.txtSensor4ScaleValue;
    		nResCalibID = R.id.txtSensor4CalibValue;
    		break;
		default:
			return;
    	}

    	TextView txtScaleValue = (TextView) m_oActivity.findViewById(nResScaleID);
    	
    	if (i_oDistanceData.nStatus == LCPMessage.SUCCESS) {
	    	txtScaleValue.setText(String.valueOf(i_oDistanceData.nDistance));
    	} else {
    		txtScaleValue.setText("????");
    	}

    	if (m_bDebug) {
	    	TextView txtNormValue = (TextView) m_oActivity.findViewById(nResNormID);
	    	txtNormValue.setText("-");
	
	    	TextView txtRawValue = (TextView) m_oActivity.findViewById(nResRawID);
	    	txtRawValue.setText("-");
	
	    	TextView txtCalibValue = (TextView) m_oActivity.findViewById(nResCalibID);
	    	txtCalibValue.setText("-");
    	}

		m_oRequestActive.put(ENXTSensorID.fromValue(nInputPort), false);
	}
	
	private void updateGUI(SensorData i_oSensorData) {
		int nInputPort = i_oSensorData.nInputPort;
		
    	int nResRawID, nResNormID, nResScaleID, nResCalibID;
    	
    	// get resource id based on sensor id
    	switch (nInputPort) {
    	case 0:
    		nResScaleID = R.id.txtSensor1ScaleValue;
    		nResRawID 	= R.id.txtSensor1RawValue;
    		nResNormID 	= R.id.txtSensor1NormValue;
    		nResCalibID = R.id.txtSensor1CalibValue;
    		break;
    	case 1:
    		nResRawID 	= R.id.txtSensor2RawValue;
    		nResNormID 	= R.id.txtSensor2NormValue;
    		nResScaleID = R.id.txtSensor2ScaleValue;
    		nResCalibID = R.id.txtSensor2CalibValue;
    		break;
    	case 2:
    		nResRawID 	= R.id.txtSensor3RawValue;
    		nResNormID 	= R.id.txtSensor3NormValue;
    		nResScaleID = R.id.txtSensor3ScaleValue;
    		nResCalibID = R.id.txtSensor3CalibValue;
    		break;
    	case 3:
    		nResRawID 	= R.id.txtSensor4RawValue;
    		nResNormID 	= R.id.txtSensor4NormValue;
    		nResScaleID = R.id.txtSensor4ScaleValue;
    		nResCalibID = R.id.txtSensor4CalibValue;
    		break;
		default:
			return;
    	}
    	
    	String strScaledValue, strNormalizedValue, strCalibratedValue, strRawValue;
    	
    	if (i_oSensorData.nStatus == LCPMessage.SUCCESS) {
    		strScaledValue = String.valueOf(i_oSensorData.nScaledValue);
    		strNormalizedValue = String.valueOf(i_oSensorData.nNormalizedValue);
    		strCalibratedValue = String.valueOf(i_oSensorData.nCalibratedValue);
    		strRawValue = String.valueOf(i_oSensorData.nRawValue);
    	} else {
    		String unknown = "????";
    		strScaledValue = unknown;
    		strNormalizedValue = unknown;
    		strCalibratedValue = unknown;
    		strRawValue = unknown;
    	}

    	TextView txtScaleValue = (TextView) m_oActivity.findViewById(nResScaleID);
        txtScaleValue.setText(strScaledValue);

    	if (m_bDebug) {
	    	TextView txtNormValue = (TextView) m_oActivity.findViewById(nResNormID);
	    	txtNormValue.setText(strNormalizedValue);
	
	    	TextView txtRawValue = (TextView) m_oActivity.findViewById(nResRawID);
	    	txtRawValue.setText(strRawValue);
	
	    	TextView txtCalibValue = (TextView) m_oActivity.findViewById(nResCalibID);
	    	txtCalibValue.setText(strCalibratedValue);
    	}

		m_oRequestActive.put(ENXTSensorID.fromValue(nInputPort), false);
	}
	

	public void showSensor(ENXTSensorID i_eSensor, boolean i_bShow) {

    	int nResID;
    	
    	// get resource id based on sensor id
    	switch (i_eSensor) {
    	case sens_sensor1:
    		nResID = R.id.tblSensor1_data;
    		break;
    	case sens_sensor2:
    		nResID = R.id.tblSensor2_data;
    		break;
    	case sens_sensor3:
    		nResID = R.id.tblSensor3_data;
    		break;
    	case sens_sensor4:
    		nResID = R.id.tblSensor4_data;
    		break;
		default:
			return;
    	}
    	
    	TableLayout oSensorData = (TableLayout) m_oActivity.findViewById(nResID);
    	
    	if (i_bShow) {
    		oSensorData.setLayoutParams(new TableLayout.LayoutParams());
    	} else {
    		oSensorData.setLayoutParams(new TableLayout.LayoutParams(0,0));
    	}
	}

	public void setDebug(boolean i_bDebug) {
		m_bDebug = i_bDebug;
	}
}
