package org.dobots.swarmcontrol.robots;

import java.lang.reflect.Type;

import org.dobots.swarmcontrol.robots.FinchRobot;


public class RobotDeviceFactory {
	
	public static RobotDevice getRobotDevice(RobotType robot)
	{
		switch (robot) {
			case RBT_FINCH: 
				return new FinchRobot(); 
			case RBT_ROOMBA:
				return new RoombaRobot();
			case RBT_NXT:
				return new NXTRobot();
			default: 		
				return new RobotDevice();
		}
	}
	
	public static Class getRobotDeviceClass(RobotType robot) {
		switch (robot) {
//		case RBT_FINCH: 
//			return FinchRobot.class;
		case RBT_ROOMBA:
			return RoombaRobot.class;
		case RBT_NXT:
			return NXTRobot.class;
		default:
			return RobotDevice.class;
		}
	}

}