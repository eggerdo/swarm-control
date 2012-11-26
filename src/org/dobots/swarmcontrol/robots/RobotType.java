package org.dobots.swarmcontrol.robots;

public enum RobotType {
	
	RBT_UNKNOWN(""),
	RBT_ROOMBA("Roomba"),
	RBT_NXT("Mindstorm NXT"),
	RBT_DOTTY("Dotty"), 
	RBT_PARROT("AR Drone"),
	RBT_ROBOSCOOPER("RoboScooper"),
	RBT_SPYKEE("Spykee"),
	RBT_FINCH("Finch"),
	RBT_SURVEYOR("Surveyor"),
	RBT_TRAKR("Trakr");
	private String strDisplayName;
	
	private RobotType(String i_strDisplayName) {
		this.strDisplayName = i_strDisplayName;
	}
	
	@Override
	public String toString() {
		return strDisplayName;
	}

}