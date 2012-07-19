package org.dobots.nxt;

public class NXTTypes {

	public static String MAC_FILTER = "00:16:53";
	
	public enum ENXTSensors {
		sens_Sonic("Sonic"),
		sens_Colour("Colour"),
		sens_Sound("Sound"),
		sens_Push("Push Button"),
		sens_Distance("Distance");
		private String strName;
		
		private ENXTSensors(String name) {
			this.strName = name;
		}
		
		public String toString() {
			return strName;
		}
	}
	
	// response status
	public static final int SUCCESS = 0x00;
	
	
	
	
}
