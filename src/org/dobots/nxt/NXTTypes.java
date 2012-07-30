package org.dobots.nxt;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.dobots.nxt.LCPMessage;
import org.dobots.nxt.NXTTypes.DistanceData;
import org.dobots.utility.Utils;

public class NXTTypes {

	public static String MAC_FILTER = "00:16:53";
	
	public static final int SENSOR_DATA_RECEIVED = 1000;
	public static final int DISTANCE_DATA_RECEIVED = 1001;
	
	public enum ENXTSensorType {
		sensType_None("None",						LCPMessage.NO_SENSOR,		LCPMessage.RAWMODE),
		sensType_Sound_DB("Sound_DB", 				LCPMessage.SOUND_DB,		LCPMessage.PCTFULLSCALEMODE),
		sensType_Sound_DBA("Sound_DBA", 			LCPMessage.SOUND_DBA,		LCPMessage.PCTFULLSCALEMODE),
		sensType_Push("Push Button", 				LCPMessage.SWITCH,			LCPMessage.BOOLEANMODE),
//		sensType_Distance1("Distance1", 			LCPMessage.LOWSPEED,	LCPMessage.RAWMODE),
//		sensType_Distance2("Distance2", 			LCPMessage.LOWSPEED_9V,		LCPMessage.RAWMODE),
		sensType_Distance("Distance",				LCPMessage.LOWSPEED_9V,		LCPMessage.RAWMODE),
		sensType_Reflected_Light("Reflected Light", LCPMessage.LIGHT_ACTIVE,	LCPMessage.PCTFULLSCALEMODE),
		sensType_Ambient_Light("Ambient Light", 	LCPMessage.LIGHT_INACTIVE,	LCPMessage.PCTFULLSCALEMODE);
		private String strName;
		private byte byValue;
		private byte byDefaultMode;
		
		private ENXTSensorType(String name, byte value, byte mode) {
			this.strName = name;
			this.byValue = value;
			this.byDefaultMode = mode;
		}
		
		public String toString() {
			return strName;
		}

		public byte getValue() {
			return byValue;
		}
		
		public byte getDefaultMode() {
			return byDefaultMode;
		}
		
	}
	
	public enum ENXTSensorID {
		sens_unknown("Unknown", -1),
		sens_sensor1("Sensor 1", 0),
		sens_sensor2("Sensor 2", 1),
		sens_sensor3("Sensor 3", 2),
		sens_sensor4("Sensor 4", 3),
		sens_motor1("Motor 1", 0),
		sens_motor2("Motor 2", 1),
		sens_motor3("Motor 3", 2);
		private String strName;
		private int nValue;
		
		private ENXTSensorID(String name, int id) {
			this.strName = name;
			this.nValue = id;
		}
		
		public String toString() {
			return strName;
		}
		
		public int getValue() {
			return nValue;
		}
		
		public static ENXTSensorID fromValue(int id) {
			for (ENXTSensorID sensor : ENXTSensorID.values()) {
				if (sensor.getValue() == id) {
					return sensor;
				}
			}
			return sens_unknown;
		}
	}
	
	public class SensorData {
		public int nTelegramType;
		public int nCommand;
		public int nStatus;
		public int nInputPort;
		public boolean bValid;
		public boolean bCalibrated;
		public int nSensorType;
		public int nSensorMode;
		public int nRawValue;
		public int nNormalizedValue;
		public int nScaledValue;
		public int nCalibratedValue;
		
		public SensorData(byte[] rgbyData) {
			ByteArrayInputStream byte_in = new ByteArrayInputStream(rgbyData);
			DataInputStream data_in = new DataInputStream(byte_in);
			try {
				nTelegramType 		= data_in.readUnsignedByte();
				nCommand 			= data_in.readUnsignedByte();
				nStatus				= data_in.readUnsignedByte();
				nInputPort			= data_in.readUnsignedByte();
				bValid				= data_in.readBoolean();
				bCalibrated			= data_in.readBoolean();
				nSensorType			= data_in.readUnsignedByte();
				nSensorMode			= data_in.readUnsignedByte();
				nRawValue			= Utils.LittleEndianToBigEndian(data_in.readShort());
				nNormalizedValue	= Utils.LittleEndianToBigEndian(data_in.readShort());
				nScaledValue		= Utils.LittleEndianToBigEndian(data_in.readShort());
				nCalibratedValue	= Utils.LittleEndianToBigEndian(data_in.readShort());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static SensorData assembleSensorData(byte[] rgbyData) {
		NXTTypes types = new NXTTypes();
		return types.new SensorData(rgbyData);
	}
	
	public class DistanceData {
		public int nTelegramType;
		public int nCommand;
		public int nStatus;
		public int nInputPort;
		public int nDistance;
		
		public DistanceData(int port, byte[] rgbyData) {
			ByteArrayInputStream byte_in = new ByteArrayInputStream(rgbyData);
			DataInputStream data_in = new DataInputStream(byte_in);
			try {
				nTelegramType 		= data_in.readUnsignedByte();
				nCommand 			= data_in.readUnsignedByte();
				nStatus				= data_in.readUnsignedByte();
				nInputPort 			= port;
				data_in.readUnsignedByte(); // discard the number of bytes read, for the distance data it is always 1
				nDistance 			= data_in.readUnsignedByte();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static DistanceData assembleDistanceData(int port, byte[] rgbyData) {
		NXTTypes types = new NXTTypes();
		return types.new DistanceData(port, rgbyData);
	}
	
}
