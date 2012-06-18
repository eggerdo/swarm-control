package org.dobots.swarmcontrol.robots;

import org.dobots.swarmcontrol.robots.FinchRobot;


public class RobotDeviceFactory {
	
	public static RobotDevice getRobotDevice(RobotType robot)
	{
		switch (robot) {
			case RBT_FINCH: return new FinchRobot(); 
			default: 		return new RobotDevice();
		}
	}

}