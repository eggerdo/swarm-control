package org.dobots.robots.spykee;

import org.dobots.robots.MessageTypes;

public class SpykeeTypes {

	public static final String SSID_FILTER 	= "";

	public static final int MAX_VELOCITY 	= 100;
	public static final int MIN_RADIUS 		= 1;
	public static final int MAX_RADIUS 		= 1000;
	
	public static final double AXLE_WIDTH 	= 180.0;

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

	
	public static final int LOGIN_ERROR = MessageTypes.USER;

	// Login Exception is thrown when the login failed because of wrong credentials
	class LoginException extends Exception {
		private static final long serialVersionUID = 7809103573726689170L;
	};

}
