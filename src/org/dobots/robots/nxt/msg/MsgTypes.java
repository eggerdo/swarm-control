package org.dobots.robots.nxt.msg;

import java.util.Arrays;

import org.dobots.robots.nxt.NXTTypes.ENXTMotorID;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorID;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorType;

public class MsgTypes {
	
	private static MsgTypes instance;
	
	public static MsgTypes getInstance() {
		if (instance == null) {
			instance = new MsgTypes();
		}
		return instance;
	}

	public class SensorDataRequestMsg {
		
		public int nPort;
		
		public SensorDataRequestMsg(int i_nPort) {
			nPort = i_nPort;
		}
	}
	
	public static SensorDataRequestMsg assembleSensorDataRequestMsg(ENXTSensorID eSensor) {
		return getInstance().new SensorDataRequestMsg(eSensor.getValue());
	}
	
	public class SensorTypeMsg {
		
		public int nPort;
		public byte byType;
		public byte byMode;
		
		public SensorTypeMsg(int i_nPort, byte i_byType, byte i_byMode) {
			nPort = i_nPort;
			byType = i_byType;
			byMode = i_byMode;
		}
		
	}

	public static SensorTypeMsg assembleSensorTypeMsg(ENXTSensorID i_eSensorID, 
											   ENXTSensorType i_eSensorType) {
		return getInstance().new SensorTypeMsg(i_eSensorID.getValue(), i_eSensorType.getValue(), i_eSensorType.getDefaultMode());
	}
	
	
	public class MotorDataRequestMsg {
		
		public int nPort;
		
		public MotorDataRequestMsg(int i_nPort) {
			nPort = i_nPort;
		}
	}

	public static MotorDataRequestMsg assembleMotorDataRequestMsg(ENXTMotorID i_eMotorID) {
		return getInstance().new MotorDataRequestMsg(i_eMotorID.getValue());
	}

	
	public class ResetMotorPositionMsg {
		
		public int nPort;
		public boolean bRelative;
		
		public ResetMotorPositionMsg(int i_nPort, boolean i_bRelative) {
			nPort = i_nPort;
			bRelative = i_bRelative;
		}
	}

	public static ResetMotorPositionMsg assembleResetMotorPositionMsg(ENXTMotorID i_eMotorID,
			boolean i_bRelative) {
		return getInstance().new ResetMotorPositionMsg(i_eMotorID.getValue(), i_bRelative);
	}
	
	
	public class MotorSpeedMsg {
		
		public int nPort;
		public int nSpeed;
		
		public MotorSpeedMsg(int i_nPort, int i_nSpeed) {
			nPort = i_nPort;
			nSpeed = i_nSpeed;
		}
	}

	public static MotorSpeedMsg assembleMotorSpeedMsg(ENXTMotorID i_eMotor, int i_nSpeed) {
		return getInstance().new MotorSpeedMsg(i_eMotor.getValue(), i_nSpeed);
	}
	
	
	public class RawDataMsg {
		
		public byte[] rgbyRawData;
		
		public RawDataMsg(byte[] i_rgbyData) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
				rgbyRawData = Arrays.copyOf(i_rgbyData, i_rgbyData.length);
			} else {
				rgbyRawData = new byte[i_rgbyData.length];
				for (int i = 0; i < i_rgbyData.length; i++) {
					rgbyRawData[i] = i_rgbyData[i];
				}
			}
		}
		
	}
	
	public static RawDataMsg assembleRawDataMsg(byte[] i_rgbyData) {
		return getInstance().new RawDataMsg(i_rgbyData);
	}
	
	
}
