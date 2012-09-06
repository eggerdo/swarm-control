package org.dobots.swarmcontrol.robots;

import org.dobots.robots.dotty.DottyTypes;
import org.dobots.robots.nxt.NXTTypes;
import org.dobots.robots.roomba.RoombaTypes;
import org.dobots.swarmcontrol.robots.dotty.DottyRobot;
import org.dobots.swarmcontrol.robots.finch.FinchRobot;
import org.dobots.swarmcontrol.robots.nxt.NXTRobot;
import org.dobots.swarmcontrol.robots.roomba.RoombaRobot;


public class RobotViewFactory {
	
//	public static RobotView getRobotView(RobotType i_eRobot)
//	{
//		switch (i_eRobot) {
//			case RBT_FINCH: 
//				return new FinchRobot(); 
//			case RBT_ROOMBA:
//				return new RoombaRobot();
//			case RBT_NXT:
//				return new NXTRobot();
//			case RBT_DOTTY:
//				return new DottyRobot();
//			default: 		
//				return null;
//		}
//	}
	
	public static Class getRobotViewClass(RobotType i_eRobot) {
		switch (i_eRobot) {
//		case RBT_FINCH: 
//			return FinchRobot.class;
		case RBT_ROOMBA:
			return RoombaRobot.class;
		case RBT_NXT:
			return NXTRobot.class;
		case RBT_DOTTY:
			return DottyRobot.class;
		default:
			return RobotView.class;
		}
	}
	
	public static String getRobotMacFilter(RobotType i_eRobot) {
		switch (i_eRobot) {
		case RBT_ROOMBA:
			return RoombaTypes.MAC_FILTER;
		case RBT_NXT:
			return NXTTypes.MAC_FILTER;
		case RBT_DOTTY:
			return DottyTypes.MAC_FILTER;
		default:
			return "";
		}
	}

}