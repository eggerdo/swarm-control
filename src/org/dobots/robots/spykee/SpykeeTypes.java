package org.dobots.robots.spykee;


public class SpykeeTypes {

	public static final String SSID_FILTER 	= "";

	public static final int SPYKEE_DOCK_UNDOCKED = 1;
	public static final int SPYKEE_DOCK_DOCKED = 2;

	// as received from spykee
	public static final int SPYKEE_AUDIO = 1;
	public static final int SPYKEE_VIDEO_FRAME = 2;
	public static final int SPYKEE_BATTERY_LEVEL = 3;
	public static final int SPYKEE_DOCK = 16;
	
	public static final int DEFAULT_VOLUME = 50;  // volume is between [0, 100]
	// The number of characters decoded per line in the hex dump
	public static final int CHARS_PER_LINE = 32;
	
	// After this many characters in the hex dump, an extra space is inserted
	// (this must be a power of two).
	public static final int EXTRA_SPACE_FREQ = 8;
	public static final int EXTRA_SPACE_MASK = EXTRA_SPACE_FREQ - 1;

	public static final int MIN_VELOCITY	= 0;
	public static final int MAX_VELOCITY 	= 100;
	public static final int MIN_RADIUS 		= 1;
	public static final int MAX_RADIUS 		= 1000;
	
	public static final double AXLE_WIDTH 	= 180.0; // mm

	public enum SpykeeSound {
		ALARM,
		BOMB,
		LASER,
		AHAHAH,
		ENGINE,
		ROBOT,
		CUSTOM1,
		CUSTOM2
	}

    public final static int VIDEO_WIDTH 	= 320;
    public final static int VIDEO_HEIGHT 	= 240;
    
	// The following strings are used as keys for reading and writing the
	// values needed to connect to Spykee.
	public static final String SPYKEE_PREFS_ADDRESS 	= "spykee_address";
	public static final String SPYKEE_PREFS_PORT 		= "spykee_port";
	public static final String SPYKEE_PREFS_LOGIN 		= "spykee_login";
	public static final String SPYKEE_PREFS_PASSWORD 	= "spykee_password";
	
	public static final String SPYKEE_DEFAULT_ADDRESS 	= null;
	public static final String SPYKEE_DEFAULT_PORT 		= "9000";
	public static final String SPYKEE_DEFAULT_LOGIN 	= "admin";
	public static final String SPYKEE_DEFAULT_PASSWORD 	= null;

	// Login Exception is thrown when the login failed because of wrong credentials
	class LoginException extends Exception {
		private static final long serialVersionUID = 7809103573726689170L;
	};

}
