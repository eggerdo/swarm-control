package org.dobots.swarmcontrol.robots.nxt;

import java.util.EnumMap;

import org.dobots.robots.nxt.LCPMessage;
import org.dobots.robots.nxt.NXT;
import org.dobots.robots.nxt.NXTTypes;
import org.dobots.robots.nxt.NXTTypes.DistanceData;
import org.dobots.robots.nxt.NXTTypes.ENXTMotorID;
import org.dobots.robots.nxt.NXTTypes.ENXTMotorSensorType;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorID;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorType;
import org.dobots.robots.nxt.NXTTypes.MotorData;
import org.dobots.robots.nxt.NXTTypes.SensorData;
import org.dobots.robots.roomba.RoombaTypes.ERoombaSensorPackages;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.SensorGatherer;
import org.dobots.utility.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;

public class NXTSensorGatherer extends SensorGatherer {

	private NXT m_oNxt;
	
	private boolean m_bDebug;
	
	private EnumMap<ENXTSensorID, ENXTSensorType> m_oSensorTypes;
	private EnumMap<ENXTSensorID, Boolean> m_oSensorEnabled;
	private EnumMap<ENXTSensorID, Boolean> m_oSensorRequestActive; // TODO should be solved with timeouts
	
	private EnumMap<ENXTMotorID, Boolean> m_oMotorEnabled;
	private EnumMap<ENXTMotorID, ENXTMotorSensorType> m_oMotorSensorTypes;
	private EnumMap<ENXTMotorID, Boolean> m_oMotorRequestActive; // TODO should be solved with timeouts
	
	
	public NXTSensorGatherer(Activity i_oActivity, NXT i_oNxt) {
		super(i_oActivity);
		m_oNxt = i_oNxt;
		
//		m_oGUIUpdater = new UpdateSensorDataTask();
		m_oSensorTypes = new EnumMap<ENXTSensorID, ENXTSensorType>(ENXTSensorID.class);
		m_oSensorEnabled = new EnumMap<ENXTSensorID, Boolean>(ENXTSensorID.class);
		m_oSensorRequestActive = new EnumMap<ENXTSensorID, Boolean>(ENXTSensorID.class);
		
		m_oMotorEnabled = new EnumMap<NXTTypes.ENXTMotorID, Boolean>(ENXTMotorID.class);
		m_oMotorRequestActive = new EnumMap<NXTTypes.ENXTMotorID, Boolean>(ENXTMotorID.class);
		m_oMotorSensorTypes = new EnumMap<NXTTypes.ENXTMotorID, NXTTypes.ENXTMotorSensorType>(ENXTMotorID.class);
		
		// set up the maps
		initialize();
		
		start();
	}
		
	public void initialize() {
		// set up the maps
		for (ENXTSensorID sensor : ENXTSensorID.values()) {
			m_oSensorTypes.put(sensor, ENXTSensorType.sensType_None);
			m_oSensorEnabled.put(sensor, false);
			m_oSensorRequestActive.put(sensor, false);
		}
		
		for (ENXTMotorID motor : ENXTMotorID.values()) {
			m_oMotorEnabled.put(motor, false);
			m_oMotorRequestActive.put(motor, false);
			m_oMotorSensorTypes.put(motor, ENXTMotorSensorType.motor_degreee);
		}
	}
	
	@Override
	protected void execute() {

		if (m_oNxt.isConnected()) {
			for (ENXTSensorID sensor : m_oSensorEnabled.keySet()) {
				if (m_oSensorEnabled.get(sensor) && 
					!m_oSensorRequestActive.get(sensor) &&
					m_oSensorTypes.get(sensor) != ENXTSensorType.sensType_None) {
						ENXTSensorType eType = m_oSensorTypes.get(sensor);
						m_oNxt.requestSensorData(sensor, eType);
						m_oSensorRequestActive.put(sensor, true);
				}
			}
			
			for (ENXTMotorID motor : m_oMotorEnabled.keySet()) {
				if (m_oMotorEnabled.get(motor) &&
					!m_oMotorRequestActive.get(motor)) {
						m_oNxt.requestMotorData(motor);
						m_oMotorRequestActive.put(motor, true);
				}
			}
		}
	}
	
	public void sendMessage(int message, Object data) {
		Utils.sendMessage(m_oSensorDataUiUpdater, message, data);
	}

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

	public void setMotorSensorType(ENXTMotorID i_eMotor,
			ENXTMotorSensorType eMotorSensorType) {
		m_oMotorSensorTypes.put(i_eMotor, eMotorSensorType);
	}
	
	public void enableMotor(ENXTMotorID i_eMotor, boolean i_bEnabled) {
		m_oMotorEnabled.put(i_eMotor, i_bEnabled);
		showMotor(i_eMotor, i_bEnabled);
	}

	/**
	 * Receive messages from the BTCommunicator
	 */
	final Handler m_oSensorDataUiUpdater = new Handler() {
		@Override
		public void handleMessage(Message myMessage) {
			switch(myMessage.what) {
			case NXTTypes.SENSOR_DATA_RECEIVED:
//				SensorData oSensorData = m_oNxt.getReceivedSensorData();
				SensorData oSensorData = (SensorData) myMessage.obj;
				updateGUI(oSensorData);
				break;
			case NXTTypes.DISTANCE_DATA_RECEIVED:
//				DistanceData oDistanceData = m_oNxt.getReceivedDistanceData();
				DistanceData oDistanceData = (DistanceData) myMessage.obj;
				updateGUI(oDistanceData);
				break;
			case NXTTypes.MOTOR_DATA_RECEIVED:
//				MotorData oMotorData = m_oNxt.getReceivedMotorData();
				MotorData oMotorData = (MotorData) myMessage.obj;
				updateGUI(oMotorData);
				break;
			}
		}
	};
	
	private void updateGUI(MotorData i_oMotorData) {
		int nOutputPort = i_oMotorData.nOutputPort;
		
		ENXTMotorID eMotor;
		int nResPowerSetpointID, nResTachoCountID, nResRotationCountID;
		
		int nInvertedFactor = m_oNxt.isInverted() ? -1 : 1;
		
		switch (nOutputPort) {
		case 0:
			eMotor = ENXTMotorID.motor_1;
			nResPowerSetpointID = R.id.txtMotor1PowerSetpoint;
			nResTachoCountID 	= R.id.txtMotor1TachoCount;
//			nResBlockTachoCountID = R.id.txtMotor1BlockTachoCount;
			nResRotationCountID = R.id.txtMotor1RotationCount;
			break;
		case 1:
			eMotor = ENXTMotorID.motor_2;
			nResPowerSetpointID = R.id.txtMotor2PowerSetpoint;
			nResTachoCountID 	= R.id.txtMotor2TachoCount;
//			nResBlockTachoCountID = R.id.txtMotor2BlockTachoCount;
			nResRotationCountID = R.id.txtMotor2RotationCount;
			break;
		case 2:
			eMotor = ENXTMotorID.motor_3;
			nResPowerSetpointID = R.id.txtMotor3PowerSetpoint;
			nResTachoCountID 	= R.id.txtMotor3TachoCount;
//			nResBlockTachoCountID = R.id.txtMotor3BlockTachoCount;
			nResRotationCountID = R.id.txtMotor3RotationCount;
			break;
		default:
			return;
		}
		
    	TextView txtPowerSetpoint = (TextView) m_oActivity.findViewById(nResPowerSetpointID);
    	txtPowerSetpoint.setText(String.valueOf(i_oMotorData.nPowerSetpoint));

    	TextView txtRotationCount = (TextView) m_oActivity.findViewById(nResRotationCountID);
    	if (m_oMotorSensorTypes.get(eMotor) == ENXTMotorSensorType.motor_degreee) {
	    	txtRotationCount.setText(String.valueOf(nInvertedFactor * i_oMotorData.nRotationCount));
    	} else {
	    	txtRotationCount.setText(String.format("%.2f", nInvertedFactor * i_oMotorData.nRotationCount / 360.0));
    	}

    	if (m_bDebug) {
	    	TextView txtTachoCount = (TextView) m_oActivity.findViewById(nResTachoCountID);
	    	txtTachoCount.setText(String.valueOf(nInvertedFactor * i_oMotorData.nTachoCount));
    	}

		m_oMotorRequestActive.put(eMotor, false);
	}
	
	private void updateGUI(DistanceData i_oDistanceData) {
		int nInputPort = i_oDistanceData.nInputPort;

		ENXTSensorID eSensor;
    	int nResRawID, nResNormID, nResScaleID, nResCalibID;
    	
    	// get resource id based on sensor id
    	switch (nInputPort) {
    	case 0:
    		eSensor = ENXTSensorID.sens_sensor1;
    		nResScaleID = R.id.txtSensor1ScaleValue;
    		nResRawID 	= R.id.txtSensor1RawValue;
    		nResNormID 	= R.id.txtSensor1NormValue;
    		nResCalibID = R.id.txtSensor1CalibValue;
    		break;
    	case 1:
    		eSensor = ENXTSensorID.sens_sensor2;
    		nResRawID 	= R.id.txtSensor2RawValue;
    		nResNormID 	= R.id.txtSensor2NormValue;
    		nResScaleID = R.id.txtSensor2ScaleValue;
    		nResCalibID = R.id.txtSensor2CalibValue;
    		break;
    	case 2:
    		eSensor = ENXTSensorID.sens_sensor3;
    		nResRawID 	= R.id.txtSensor3RawValue;
    		nResNormID 	= R.id.txtSensor3NormValue;
    		nResScaleID = R.id.txtSensor3ScaleValue;
    		nResCalibID = R.id.txtSensor3CalibValue;
    		break;
    	case 3:
    		eSensor = ENXTSensorID.sens_sensor4;
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

		m_oSensorRequestActive.put(eSensor, false);
	}
	
	private void updateGUI(SensorData i_oSensorData) {
		int nInputPort = i_oSensorData.nInputPort;
		
		ENXTSensorID eSensor;
    	int nResRawID, nResNormID, nResScaleID, nResCalibID;
    	
    	// get resource id based on sensor id
    	switch (nInputPort) {
    	case 0:
    		eSensor = ENXTSensorID.sens_sensor1;
    		nResScaleID = R.id.txtSensor1ScaleValue;
    		nResRawID 	= R.id.txtSensor1RawValue;
    		nResNormID 	= R.id.txtSensor1NormValue;
    		nResCalibID = R.id.txtSensor1CalibValue;
    		break;
    	case 1:
    		eSensor = ENXTSensorID.sens_sensor2;
    		nResRawID 	= R.id.txtSensor2RawValue;
    		nResNormID 	= R.id.txtSensor2NormValue;
    		nResScaleID = R.id.txtSensor2ScaleValue;
    		nResCalibID = R.id.txtSensor2CalibValue;
    		break;
    	case 2:
    		eSensor = ENXTSensorID.sens_sensor3;
    		nResRawID 	= R.id.txtSensor3RawValue;
    		nResNormID 	= R.id.txtSensor3NormValue;
    		nResScaleID = R.id.txtSensor3ScaleValue;
    		nResCalibID = R.id.txtSensor3CalibValue;
    		break;
    	case 3:
    		eSensor = ENXTSensorID.sens_sensor4;
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

		m_oSensorRequestActive.put(eSensor, false);
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

	public void showMotor(ENXTMotorID i_eMotor, boolean i_bShow) {

    	int nResID;
    	
    	// get resource id based on sensor id
    	switch (i_eMotor) {
    	case motor_1:
    		nResID = R.id.tblMotor1_data;
    		break;
    	case motor_2:
    		nResID = R.id.tblMotor2_data;
    		break;
    	case motor_3:
    		nResID = R.id.tblMotor3_data;
    		break;
		default:
			return;
    	}
    	
    	Utils.showLayout((TableLayout)m_oActivity.findViewById(nResID), i_bShow);
	}

	public void setDebug(boolean i_bDebug) {
		m_bDebug = i_bDebug;
	}

}
