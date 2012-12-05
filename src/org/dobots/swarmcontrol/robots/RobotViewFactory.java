package org.dobots.swarmcontrol.robots;

import org.dobots.robots.dotty.DottyTypes;
import org.dobots.robots.nxt.NXTTypes;
import org.dobots.robots.parrot.ParrotTypes;
import org.dobots.robots.roboscooper.RoboScooperTypes;
import org.dobots.robots.roomba.RoombaTypes;
import org.dobots.robots.spykee.SpykeeTypes;
import org.dobots.swarmcontrol.BaseActivity;
import org.dobots.swarmcontrol.robots.dotty.DottyRobot;
import org.dobots.swarmcontrol.robots.nxt.NXTRobot;
import org.dobots.swarmcontrol.robots.parrot.ParrotRobot;
import org.dobots.swarmcontrol.robots.roboscooper.RoboScooperRobot;
import org.dobots.swarmcontrol.robots.roomba.RoombaRobot;
import org.dobots.swarmcontrol.robots.spykee.SpykeeRobot;

public class RobotViewFactory {
	
	public static Class getRobotViewClass(RobotType i_eRobot) {
		switch (i_eRobot) {
		case RBT_ROOMBA:
			return RoombaRobot.class;
		case RBT_NXT:
			return NXTRobot.class;
		case RBT_DOTTY:
			return DottyRobot.class;
		case RBT_PARROT:
			return ParrotRobot.class;
		case RBT_ROBOSCOOPER:
			return RoboScooperRobot.class;
		case RBT_SPYKEE:
			return SpykeeRobot.class;
		default:
			return UnimplementedRobot.class;
		}
	}
	
	public static String getRobotAddressFilter(RobotType i_eRobot) {
		switch (i_eRobot) {
		case RBT_ROOMBA:
			return RoombaTypes.MAC_FILTER;
		case RBT_NXT:
			return NXTTypes.MAC_FILTER;
		case RBT_DOTTY:
			return DottyTypes.MAC_FILTER;
		case RBT_PARROT:
			return ParrotTypes.SSID_FILTER;
		case RBT_ROBOSCOOPER:
			return RoboScooperTypes.MAC_FILTER;
		case RBT_SPYKEE:
			return SpykeeTypes.SSID_FILTER;
		default:
			return null;
		}
	}

}