package org.dobots.robots.dotty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.dobots.utility.Utils;

public class DottyTypes {
	
	private static DottyTypes INSTANCE;
	public static int TIMESTAMP = 0;

	/////////////////////////////////////////////////

	public static final UUID DOTTY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public static final String MAC_FILTER = "00:06:66";
	
	public static final int MAX_VELOCITY = 255;
	public static final int MAX_RADIUS = 1;

	public static final int MIN_SENSOR_INTERVAL = 100;
	public static final int DEFAULT_SENSOR_INTERVAL = 500;

	/////////////////////////////////////////////////
	
	public static final byte HEADER = (byte)0xA5;
	public static final byte LOGGING = (byte)0xA6;
	
	public static final int NR_SENSORS = 11;
	public static final int DATA_PARAM_LEN = NR_SENSORS;
	public static final int CMD_PARAM_LEN = DATA_PARAM_LEN;

	public static final int HEADER_SIZE = 7; // 7 bytes
	public static final int DATA_PKG_SIZE = HEADER_SIZE + DATA_PARAM_LEN * 2; // 7 + 2 * 11 = 29 bytes
	public static final int CMD_PKG_SIZE = HEADER_SIZE + CMD_PARAM_LEN * 2; // same as data package

	public static final byte SENSOR_DATA = 0x00;
	
	// streaming
	public static final byte STREAM_ON = 0x01;
	public static final byte STREAM_OFF = 0x02;

	public static final int STREAM_CMD_LEN = 2;

	// remote control
	public static final byte DRIVE = 0x03;
	public static final byte DRIVE_STOP = 0x04;

	public static final int DRIVE_CMD_LEN = 4;

	// logging
	public static final byte BT_LOGGING = 0x05;
	public static final byte LOG_DATA_SINGLE = 0x06;
	public static final byte LOG_DATA_MULTI_START = 0x07;
	public static final byte LOG_DATA_MULTI_MIDDLE = 0x08;
	public static final byte LOG_DATA_MULTI_END = 0x09;

	public static final int BT_LOGGING_CMD_LEN = 2;

	// sensor data
	public static final byte SENSOR_DATA_REQ = 0x0A;

	public static final int SENSOR_CMD_LEN = 0;

	// control command
	public static final byte CONTROL_ENABLE = 0x0B;

	public static final int CONTROL_CMD_LEN = 2;
	
	// disconnect command
	public static final byte DISCONNECT = 0x0C;
	
	/////////////////////////////////////////////////
	

	public class CmdPackage {
		int nHeader;
		int nTimestamp;
		int nType;
		int nLength;
		int rgnParameter[] = new int[CMD_PARAM_LEN];
		boolean bDirtyBitSet;
		int nCRC;
		
		public CmdPackage() {
			nHeader = (byte)0xA5;
			nTimestamp = TIMESTAMP++;
		}
		
		public CmdPackage(byte[] rgbyData) {
			ByteArrayInputStream byte_in = new ByteArrayInputStream(rgbyData);
			DataInputStream data_in = new DataInputStream(byte_in);
			try {
				nHeader 		= data_in.readUnsignedByte();
				nTimestamp 		= Utils.ConvertEndian((short)data_in.readUnsignedShort());
				nType			= data_in.readUnsignedByte();
				nLength			= data_in.readUnsignedByte();
				for (int i = 0; i < CMD_PARAM_LEN; i++) {
					rgnParameter[i] = Utils.ConvertEndian((short)data_in.readUnsignedShort());
				}
				// the last 16 bits consist of 1 bit for the DirtyBit and 15 bits for the CRC
				// we read in the 16 bits together, then parse it into DirtyBit and CRC
				short sTmp = (short) data_in.readUnsignedShort();
				// the first bit in the sequence (bit number 15) is the DirtyBit. We convert that into a boolean
				// for easier handling
				bDirtyBitSet	= Utils.IsBitSet(sTmp, 15); 
				// next we clear the DirtyBit from the short and use the result as CRC
//				sTmp = (short) Utils.clearBit(sTmp, 15);
//				nCRC			= Utils.LittleEndianToBigEndian(sTmp);
				nCRC			= (short) Utils.clearBit(sTmp, 15);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public byte[] toByteArray() {
			ByteArrayOutputStream byte_out = new ByteArrayOutputStream();
			DataOutputStream data_out = new DataOutputStream(byte_out);
			try {
				data_out.writeByte(nHeader);
				data_out.writeShort(Utils.ConvertEndian((short)nTimestamp));
				data_out.writeByte(nType);
				data_out.writeByte(nLength);
				for (int i=0; i < CMD_PARAM_LEN; i++) {
					data_out.writeShort(Utils.ConvertEndian((short)rgnParameter[i]));
				}
				// the last 16 bits consist of 1 bit for the DirtyBit and 15 bits for the CRC
				// we concatenate them and write them out together
				short sTmp = (short)nCRC;
				if (bDirtyBitSet) {
					Utils.setBit(sTmp, 15);
				} // clearing is not necessary since the bit 15 should be 0 by default
				data_out.writeShort(sTmp);
				return byte_out.toByteArray();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public static CmdPackage assembleCmdPackage() {
		CmdPackage cmd = getInstance().new CmdPackage();
		return cmd;
	}
	
	public static byte[] getControlPackage(boolean i_bEnable) {
		CmdPackage cmd = assembleCmdPackage();
		cmd.nType = CONTROL_ENABLE;
		cmd.nLength = CONTROL_CMD_LEN;
		cmd.rgnParameter[0] = i_bEnable ? 1 : 0;
		cmd.nCRC = 1;
		return cmd.toByteArray();
	}

	public static byte[] getDrivePackage(int i_nLeftVelocity, int i_nRightVelocity) {
		CmdPackage cmd = assembleCmdPackage();
		cmd.nType = DRIVE;
		cmd.nLength = DRIVE_CMD_LEN;
		cmd.rgnParameter[0] = i_nLeftVelocity;
		cmd.rgnParameter[1] = i_nRightVelocity;
		cmd.nCRC = 1;
		return cmd.toByteArray();
	}

	public static byte[] getDriveStopPackage() {
		CmdPackage cmd = assembleCmdPackage();
		cmd.nType = DRIVE_STOP;
		cmd.nLength = 0;
		cmd.nCRC = 1;
		return cmd.toByteArray();
	}
	
	public static byte[] getDataRequestPackage() {
		CmdPackage cmd = assembleCmdPackage();
		cmd.nType = SENSOR_DATA_REQ;
		cmd.nLength = SENSOR_CMD_LEN;
		cmd.nCRC = 1;
		return cmd.toByteArray();
	}
	
	public static byte[] getStreamingONPackage(int i_nInterval) {
		CmdPackage cmd = assembleCmdPackage();
		cmd.nType = STREAM_ON;
		cmd.nLength = STREAM_CMD_LEN;
		cmd.rgnParameter[0] = i_nInterval;
		cmd.nCRC = 1;
		return cmd.toByteArray();
	}

	public static byte[] getStreamingOFFPackage() {
		CmdPackage cmd = assembleCmdPackage();
		cmd.nType = STREAM_OFF;
		cmd.nLength = 0;
		cmd.nCRC = 1;
		return cmd.toByteArray();
	}

	public static byte[] getDisconnectPackage() {
		CmdPackage cmd = assembleCmdPackage();
		cmd.nType = DISCONNECT;
		cmd.nLength = 0;
		cmd.nCRC = 1;
		return cmd.toByteArray();
	}

	public class DataPackage {
		int nHeader;
		int nTimestamp;
		int nType;
		int nLength;
		int rgnSensor[] = new int[DATA_PARAM_LEN];
		int nCRC;
		
		public DataPackage(byte[] rgbyData) {
			ByteArrayInputStream byte_in = new ByteArrayInputStream(rgbyData);
			DataInputStream data_in = new DataInputStream(byte_in);
			try {
				nHeader 		= data_in.readUnsignedByte();
				nTimestamp 		= Utils.ConvertEndian((short)data_in.readUnsignedShort());
				nType			= data_in.readUnsignedByte();
				nLength			= data_in.readUnsignedByte();
				for (int i = 0; i < DATA_PARAM_LEN; i++) {
					rgnSensor[i] = Utils.ConvertEndian((short)data_in.readUnsignedShort());
				}
				nCRC			= Utils.ConvertEndian((short)data_in.readUnsignedByte());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public byte[] toByteArray() {
			ByteArrayOutputStream byte_out = new ByteArrayOutputStream();
			DataOutputStream data_out = new DataOutputStream(byte_out);
			try {
				data_out.writeByte(nHeader);
				data_out.writeShort(Utils.LittleEndianToBigEndian(nTimestamp));
				data_out.writeByte(nType);
				data_out.writeByte(nLength);
				for (int i=0; i < DATA_PARAM_LEN; i++) {
					data_out.writeShort(Utils.LittleEndianToBigEndian(rgnSensor[i]));
				}
				data_out.writeShort(Utils.LittleEndianToBigEndian(nCRC));
				return byte_out.toByteArray();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

	public static DataPackage assembleDataPackage(byte[] i_rgbyData) {
		return getInstance().new DataPackage(i_rgbyData);
	}
	
	public class SensorData {
		public int nSound;
		public int nBattery;
		public int nLight;
		public int nDistance;
		public int nMotor1;
		public int nMotor2;
		public int nWheel1;
		public int nWheel2;
		public boolean bLed1ON;
		public boolean bLed2ON;
		public boolean bLed3ON;
		
		public SensorData(int[] i_rgnData) {
			nSound			= i_rgnData[0];
			nBattery		= i_rgnData[1];
			nLight			= i_rgnData[2];
			nDistance		= i_rgnData[3];
			nMotor1			= i_rgnData[4];
			nMotor2			= i_rgnData[5];
			nWheel1			= i_rgnData[6];
			nWheel2			= i_rgnData[7];
			bLed1ON			= readBoolean(i_rgnData[8]);
			bLed2ON			= readBoolean(i_rgnData[9]);
			bLed3ON			= readBoolean(i_rgnData[10]);
		}
		
		private boolean readBoolean(int i_nValue) {
			return i_nValue == 0 ? false : true;
		}
	}

	public static SensorData assembleSensorData(int[] i_rgnData) {
		return getInstance().new SensorData(i_rgnData);
	}
	
	public enum EDottySensors {
		sensor_Dist("Distance"),
		sensor_Sound("Sound"),
		sensor_Light("Light"),
		sensor_Battery("Battery"),
		sensor_Motor1("Motor 1"),
		sensor_Motor2("Motor 2"),
		sensor_Wheel1("Wheel 1"),
		sensor_Wheel2("Wheel 2");
		String strName;
		
		EDottySensors(String i_strName) {
			strName = i_strName;
		}
		
		public String toString() {
			return strName;
		}
	}
	
	public static DottyTypes getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DottyTypes();
		}
		return INSTANCE;
	}

	
}
