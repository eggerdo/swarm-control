package org.dobots.swarmcontrol.robots;

import org.dobots.swarmcontrol.robots.finch.FinchRobot;
import org.dobots.swarmcontrol.robots.nxt.NXTRobot;
import org.dobots.swarmcontrol.robots.roomba.RoombaRobot;


public class RobotViewFactory {
	
	public static RobotView getRobotView(RobotType robot)
	{
		switch (robot) {
			case RBT_FINCH: 
				return new FinchRobot(); 
			case RBT_ROOMBA:
				return new RoombaRobot();
			case RBT_NXT:
				return new NXTRobot();
			default: 		
				return new RobotView();
		}
	}
	
	public static Class getRobotViewClass(RobotType robot) {
		switch (robot) {
//		case RBT_FINCH: 
//			return FinchRobot.class;
		case RBT_ROOMBA:
			return RoombaRobot.class;
		case RBT_NXT:
			return NXTRobot.class;
		default:
			return RobotView.class;
		}
	}

}