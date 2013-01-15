package org.dobots.robots.ac13;

import java.util.ArrayList;

public class AC13RoverTypes {
	
	public static final String ADDRESS 	= "192.168.1.100";
	public static final int PORT		= 80;
	
	public static final String SSID_FILTER = "AC13_";

	public enum VideoResolution {
		res_unknown,
		res_320x240,
		res_640x480
	}
	
	public static final int MAX_SPEED 		= 10;
	public static final int MIN_RADIUS 		= 1;
	public static final int MAX_RADIUS 		= 1000;
	public static final double AXLE_WIDTH  	= 230.0; // mm

	//Robot Parameters
	public class AC13RoverParameters
	{
		public String id;
		public String sys_ver;
		public String app_ver;
		public String alias;
		public String adhoc_ssid;
		public String username;
		public String userpwd;
		public String resolution;
		public String ip;
		public String mask;
		public String gateway;
		public String port;
		public String wifi_ssid;
		public String wifi_encrypt;
		public String wifi_defkey;
		public String wifi_key1;
		public String wifi_key2;
		public String wifi_key3;
		public String wifi_key4;
		public String wifi_authtype;
		public String wifi_keyformat;
		public String wifi_key1_bits;
		public String wifi_key2_bits;
		public String wifi_key3_bits;
		public String wifi_key4_bits;
		public String wifi_wpa_psk;
		
		public void fillParameters(ArrayList<String> parameters){	
			
			id = parameters.get(0);
			sys_ver = parameters.get(1);
			app_ver = parameters.get(2);
			alias = parameters.get(3);
			adhoc_ssid = parameters.get(4);
			username = parameters.get(5);
			userpwd = parameters.get(6);
			resolution = parameters.get(7);
			ip = parameters.get(8);
			mask = parameters.get(9);
			gateway = parameters.get(10);
			port = parameters.get(11);
			wifi_ssid = parameters.get(12);
			wifi_encrypt = parameters.get(13);
			wifi_defkey = parameters.get(14);
			wifi_key1 = parameters.get(15);
			wifi_key2 = parameters.get(16);
			wifi_key3 = parameters.get(17);
			wifi_key4 = parameters.get(18);
			wifi_authtype = parameters.get(19);
			wifi_keyformat = parameters.get(20);
			wifi_key1_bits = parameters.get(21);
			wifi_key2_bits = parameters.get(22);
			wifi_key3_bits = parameters.get(23);
			wifi_key4_bits = parameters.get(24);
			wifi_wpa_psk = parameters.get(25);
		}
		
	}
	
}
