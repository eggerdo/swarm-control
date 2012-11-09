package org.dobots.robots.parrot;

public class ParrotTypes {

	public static final String SSID_Filter 		= "ardrone";

    public static final long CONNECTION_TIMEOUT = 10000;

    public static final String PARROT_IP		= "192.168.1.1";
	public static final int PORT 				= 80;
    public static final int VIDEO_PORT 			= 5555;
    
    public static final String VIDEO_CODEC 		= "h264";
    public final static int VIDEO_WIDTH 		= 640;
    public final static int VIDEO_HEIGHT 		= 360;
    
    public final static int SUCCESS				= 0;
    public final static int NOTHING_FOUND		= -1;
    public final static int READ_FRAME_FAILED	= -2;
    
    public enum ParrotMove {
    	MOVE_UP, MOVE_DOWN, MOVE_FWD, MOVE_BWD, MOVE_LEFT, MOVE_RIGHT, ROTATE_LEFT, ROTATE_RIGHT
    }

}
