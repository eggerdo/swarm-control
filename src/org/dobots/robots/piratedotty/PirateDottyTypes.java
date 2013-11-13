package org.dobots.robots.piratedotty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import org.dobots.utilities.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class PirateDottyTypes {
	
	private static PirateDottyTypes INSTANCE;
	public static int TIMESTAMP = 0;

	/////////////////////////////////////////////////

	public static final UUID PirateDotty_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public static final String MAC_FILTER = "00:06:66";
	
	public static final int MIN_VELOCITY = 0;
	public static final int MAX_VELOCITY = 255;
	public static final int MAX_RADIUS = 1000;
	public static final int MIN_RADIUS = 1;

	public static final double AXLE_WIDTH = 100.0; // in mm

	public static final int MIN_SENSOR_INTERVAL = 100;
	public static final int DEFAULT_SENSOR_INTERVAL = 500;

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

	public static PirateDottyTypes getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PirateDottyTypes();
		}
		return INSTANCE;
	}

	
}
