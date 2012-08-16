package org.dobots.robots;

import org.dobots.nxt.NXT;
import org.dobots.roomba.Roomba;
import org.dobots.swarmcontrol.robots.RobotType;

public class RobotDeviceFactory {
		
	public static RobotDevice getRobotDevice(RobotType robot)
	{
		switch (robot) {
//				case RBT_FINCH: 
//					return new FinchRobot(); 
			case RBT_ROOMBA:
				return new Roomba();
			case RBT_NXT:
				return new NXT();
			default: 		
				return null;
		}
	}
	
	public static Class getRobotDeviceClass(RobotType robot) {
		switch (robot) {
//			case RBT_FINCH: 
//				return FinchRobot.class;
		case RBT_ROOMBA:
			return Roomba.class;
		case RBT_NXT:
			return NXT.class;
		default:
			return null;
		}
	}

}
