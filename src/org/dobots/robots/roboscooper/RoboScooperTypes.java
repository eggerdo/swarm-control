package org.dobots.robots.roboscooper;

import org.dobots.robots.MessageTypes;

public class RoboScooperTypes {
	
	public static final int INITIALISATION_FAILED = MessageTypes.USER;

	public static final String MAC_FILTER 		= "00:06:66";
	
	// name of the device file containing the IR signal definitions
	// necessary for the brainlink to operate. the file has to be stored in
	//   /sdcard/BirdBrainTechnologies/BrainLink/devices/
	public static String SIGNAL_FILE_NAME 		= "RoboScooper";
	// true if the definition file is encoded, false if it is raw
	public static boolean SIGNAL_FILE_ENCODED 	= true;
	
	// definition of the names given to each signal
	public static String LEFT 					= "left";
	public static String RIGHT 					= "right";
	public static String FORWARD 				= "forward";
	public static String BACKWARD 				= "backward";
	public static String PICKUP 				= "pickup";
	public static String DUMP 					= "dump";
	public static String STOP 					= "stop";
	public static String WHACK 					= "whack";
	public static String VISION 				= "vision";
	public static String TALK 					= "talk";
	public static String AUTONOMOUS 			= "autonomous";
	
	
}
