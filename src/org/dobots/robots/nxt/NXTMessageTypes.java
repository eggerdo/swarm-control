package org.dobots.robots.nxt;

import org.dobots.robots.MessageTypes;

public class NXTMessageTypes {

	public static final int SENSOR_DATA_RECEIVED 	= MessageTypes.USER;
	public static final int DISTANCE_DATA_RECEIVED 	= SENSOR_DATA_RECEIVED + 1;
	public static final int MOTOR_DATA_RECEIVED 	= DISTANCE_DATA_RECEIVED + 1;

    public static final int DISCONNECT 				= MOTOR_DATA_RECEIVED + 1;
    public static final int GET_FIRMWARE_VERSION 	= DISCONNECT + 1;
    public static final int FIRMWARE_VERSION 		= GET_FIRMWARE_VERSION + 1;
    public static final int FIND_FILES 				= FIRMWARE_VERSION + 1;
    public static final int START_PROGRAM 			= FIND_FILES + 1;
    public static final int STOP_PROGRAM 			= START_PROGRAM + 1;
    public static final int GET_PROGRAM_NAME 		= STOP_PROGRAM + 1;
    public static final int PROGRAM_NAME 			= GET_PROGRAM_NAME + 1;
    public static final int SET_INPUT_MODE 			= PROGRAM_NAME + 1;
    public static final int GET_INPUT_VALUES 		= SET_INPUT_MODE + 1;
    public static final int SET_OUTPUT_STATE 		= GET_INPUT_VALUES + 1;
    public static final int GET_OUTPUT_STATE 		= SET_OUTPUT_STATE + 1;
    public static final int RESET_MOTOR_POSITION 	= GET_OUTPUT_STATE + 1;
    public static final int GET_BATTERY_LEVEL 		= RESET_MOTOR_POSITION + 1;
    public static final int RESET_INPUT_SCALED 		= GET_BATTERY_LEVEL + 1;
    public static final int LS_GET_STATUS 			= RESET_INPUT_SCALED + 1;
    public static final int LS_READ 				= LS_GET_STATUS + 1;
    public static final int LS_WRITE 				= LS_READ + 1;
    public static final int MOTOR_STATE				= LS_WRITE + 1;
    public static final int GET_DISTANCE 			= MOTOR_STATE + 1;
    public static final int KEEP_ALIVE 				= GET_DISTANCE + 1;
    public static final int MAKE_CIRLCE 			= KEEP_ALIVE + 1;
    public static final int SAY_TEXT 				= MAKE_CIRLCE + 1;
    public static final int VIBRATE_PHONE 			= SAY_TEXT + 1;
    public static final int DO_BEEP 				= VIBRATE_PHONE + 1;
    public static final int DO_ACTION 				= DO_BEEP + 1;
    public static final int DESTROY 				= VIBRATE_PHONE + 1;

}
