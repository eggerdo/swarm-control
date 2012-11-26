package org.dobots.robots;

import org.dobots.robots.dotty.Dotty;
import org.dobots.robots.nxt.NXT;
import org.dobots.robots.parrot.Parrot;
import org.dobots.robots.roboscooper.RoboScooper;
import org.dobots.robots.roomba.Roomba;
import org.dobots.robots.spykee.Spykee;
import org.dobots.swarmcontrol.ConnectListener;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.swarmcontrol.robots.dotty.DottyRobot;
import org.dobots.swarmcontrol.robots.nxt.NXTRobot;
import org.dobots.swarmcontrol.robots.parrot.ParrotRobot;
import org.dobots.swarmcontrol.robots.roomba.RoombaRobot;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;

public class RobotDeviceFactory {
		
	public static RobotDevice getRobotDevice(RobotType robot) throws Exception
	{
		switch (robot) {
			case RBT_ROOMBA:
				return new Roomba();
			case RBT_NXT:
				return new NXT();
			case RBT_DOTTY:
				return new Dotty();
			case RBT_PARROT:
				return new Parrot();
			case RBT_ROBOSCOOPER:
				return new RoboScooper();
			case RBT_SPYKEE:
				return new Spykee();
			default: 		
				throw new Exception();
		}
	}
	
	public static Class getRobotDeviceClass(RobotType robot) throws Exception {
		switch (robot) {
		case RBT_ROOMBA:
			return Roomba.class;
		case RBT_NXT:
			return NXT.class;
		case RBT_DOTTY:
			return Dotty.class;
		case RBT_PARROT:
			return Parrot.class;
		case RBT_ROBOSCOOPER:
			return RoboScooper.class;
		case RBT_SPYKEE:
			return Spykee.class;
		default:
			throw new Exception();
		}
	}

}
