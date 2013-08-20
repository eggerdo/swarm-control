package org.dobots.robots.robo40;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

public class Robo40Types {
	
	private static Robo40Types INSTANCE;
	public static int TIMESTAMP = 0;

	/////////////////////////////////////////////////

	public static final UUID DOTTY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public static final String MAC_FILTER = "00:06:66";
	
	public static final int MIN_VELOCITY = 0;
	public static final int MAX_VELOCITY = 255;
	public static final int MAX_RADIUS = 1000;
	public static final int MIN_RADIUS = 1;

	public static final double AXLE_WIDTH = 100.0; // in mm

	public static final int MIN_SENSOR_INTERVAL = 100;
	public static final int DEFAULT_SENSOR_INTERVAL = 500;

	/////////////////////////////////////////////////
	
	public static final byte LOGGING = (byte)0xA6;
	
	public static final int NR_SENSORS = 11;
	public static final int DATA_PARAM_LEN = NR_SENSORS;
	public static final int CMD_PARAM_LEN = DATA_PARAM_LEN;

	public static final int HEADER_SIZE = 7; // 7 bytes
	public static final int DATA_PKG_SIZE = HEADER_SIZE + DATA_PARAM_LEN * 2; // 7 + 2 * 11 = 29 bytes
	public static final int CMD_PKG_SIZE = HEADER_SIZE + CMD_PARAM_LEN * 2; // same as data package

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
	
	// sensor positions
	public static final int LIGHT	 	= 0;
	public static final int SOUND		= 1;
	public static final int DISTANCE	= 2;
	public static final int BATTERY 	= 3;
	public static final int WHEEL_1		= 4;
	public static final int WHEEL_2		= 5;
	public static final int MOTOR_1 	= 6;
	public static final int MOTOR_2		= 7;
	public static final int LED_1		= 8;
	public static final int LED_2		= 9;
	public static final int LED_3		= 10;
	
	/////////////////////////////////////////////////

	public static final byte HEADER = (byte)0xA5;
	
	public static final int SENSOR_DATA 	= 0;
	public static final int DRIVE_COMMAND 	= 1;
	public static final int MOTOR_COMMAND 	= 2;
	public static final int CONTROL_COMMAND = 3;
	public static final int DISCONNECT		= 4;
	
	public static final int INT_T		= 0;
	public static final int DOUBLE_T	= 1;
	public static final int STRING_T	= 2;
	public static final int BOOL_T		= 3;

	/////////////////////////////////////////////////

	private static JSONObject createJsonBase(int type) throws JSONException {
		JSONObject json = new JSONObject();
		
		JSONObject header = new JSONObject();
		header.put("id", HEADER);
		header.put("timestamp", 0);
		header.put("type", type);
		json.put("header", header);
		
		return json;
	}
	
	public static JSONObject createControlCommand(boolean enable) throws JSONException {
		JSONObject json = createJsonBase(CONTROL_COMMAND);
		
		JSONObject data = new JSONObject();
		data.put("enabled", enable);
		json.put("data", data);
		
		return json;
	}
	
	public static byte[] getControlCommandPackage(boolean enable) {
		JSONObject json;
		try {
			json = createControlCommand(enable);
			return json.toString().getBytes();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static JSONObject createMotorCommand(int id, int direction, int speed) throws JSONException {
		JSONObject json = createJsonBase(MOTOR_COMMAND);
		
		JSONObject data = new JSONObject();
		data.put("motor_id", id);
		data.put("direction", direction);
		data.put("speed", speed);
		json.put("data", data);
		
		return json;
	}

	public static byte[] getMotorCommandPackage(int id, int direction, int speed) {
		JSONObject json;
		try {
			json = createMotorCommand(id, direction, speed);
			return json.toString().getBytes();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static JSONObject createDriveCommand(int left, int right) throws JSONException {
		JSONObject json = createJsonBase(DRIVE_COMMAND);
		
		JSONObject data = new JSONObject();
		data.put("left", left);
		data.put("right", right);
		json.put("data", data);
		
		return json;
	}

	public static byte[] getDriveCommandPackage(int left, int right) {
		JSONObject json;
		try {
			json = createDriveCommand(left, right);
			return json.toString().getBytes();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static JSONObject createDisconnectCommand() throws JSONException {
		return createJsonBase(DISCONNECT);
	}

	public static byte[] getDisconnectPackage() {
		JSONObject json;
		try {
			json = createDisconnectCommand();
			return json.toString().getBytes();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Robo40Types getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Robo40Types();
		}
		return INSTANCE;
	}

	
}
