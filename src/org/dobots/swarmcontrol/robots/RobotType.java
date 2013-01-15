package org.dobots.swarmcontrol.robots;

public enum RobotType {
	
	RBT_ROOMBA		("Roomba", 			true),
	RBT_NXT			("Mindstorm NXT", 	true),
	RBT_DOTTY		("Dotty", 			true), 
	RBT_PARROT		("AR Drone", 		true),
	RBT_ROBOSCOOPER	("RoboScooper", 	true),
	RBT_SPYKEE		("Spykee", 			true), 
	RBT_AC13ROVER	("AC13 Rover",		true),
	RBT_FINCH		("Finch", 			false),
	RBT_SURVEYOR	("Surveyor", 		false),
	RBT_TRAKR		("Trakr", 			false);
	
	private String strDisplayName;
	// enabled means that the robot is implemented and can be selected
	private boolean bEnabled;
	
	private RobotType(String i_strDisplayName, boolean i_bEnabled) {
		this.strDisplayName = i_strDisplayName;
		this.bEnabled = i_bEnabled;
	}
	
	@Override
	public String toString() {
		return strDisplayName;
	}
	
	public boolean isEnabled() {
		return bEnabled;
	}

}