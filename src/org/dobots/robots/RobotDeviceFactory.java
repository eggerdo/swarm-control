package org.dobots.robots;

import org.dobots.robots.dotty.Dotty;
import org.dobots.robots.nxt.NXT;
import org.dobots.robots.parrot.Parrot;
import org.dobots.robots.roomba.Roomba;
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
			case RBT_ARDRONE:
				return new Parrot();
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
		case RBT_ARDRONE:
			return Parrot.class;
		default:
			throw new Exception();
		}
	}

	public static void connectToRobot(Activity context, RobotDevice oRobot,
			BluetoothDevice i_oDevice, ConnectListener oListener) throws Exception {
		switch (oRobot.getType()) {
		case RBT_NXT:
			NXTRobot.connectToNXT(context, (NXT)oRobot, i_oDevice, oListener);
			break;
		case RBT_ROOMBA:
			RoombaRobot.connectToRoomba(context, (Roomba)oRobot, i_oDevice, oListener);
			break;
		case RBT_DOTTY:
			DottyRobot.connectToDotty(context, (Dotty)oRobot, i_oDevice, oListener);
			break;
		case RBT_ARDRONE:
			ParrotRobot.connectToARDrone(context, (Parrot)oRobot, "", oListener);
		default:
			throw new Exception();
		}
	}

}
