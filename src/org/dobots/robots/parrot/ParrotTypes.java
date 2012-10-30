package org.dobots.robots.parrot;

public class ParrotTypes {

	public static final int PORT = 80;
	
	public static final String SSID_Filter = "ardrone";

    public static final long CONNECTION_TIMEOUT = 10000;

//    final static byte[] ARDRONE_IP  = { (byte) 192, (byte) 168, (byte) 1, (byte) 1 };
    public static final String ARDRONE_IP = "192.168.1.1";
    
    public static final int VIDEO_PORT = 5555;
    
    public static final String VIDEO_CODEC = "h264";
    
    public enum ParrotMove {
    	MOVE_UP, MOVE_DOWN, MOVE_FWD, MOVE_BWD, MOVE_LEFT, MOVE_RIGHT, ROTATE_LEFT, ROTATE_RIGHT
    }

}
