package org.dobots.swarmcontrol.robots;

public enum RobotType {
	
	RBT_FINCH("Finch"),
	RBT_SURVEYOR("Surveyor"),
	RBT_ROOMBA("Roomba"),
	ROBOT_TRAKR("Trakr"),
	RBT_SPYKEE("Spykee");
	private String strDisplayName;
	
	private RobotType(String i_strDisplayName) {
		this.strDisplayName = i_strDisplayName;
	}
	
	@Override
	public String toString() {
		return strDisplayName;
	}

}