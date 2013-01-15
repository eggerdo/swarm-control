package org.dobots.robots;

import org.dobots.robots.ac13.AC13Rover;
import org.dobots.robots.dotty.Dotty;
import org.dobots.robots.nxt.NXT;
import org.dobots.robots.parrot.Parrot;
import org.dobots.robots.roboscooper.RoboScooper;
import org.dobots.robots.roomba.Roomba;
import org.dobots.robots.spykee.Spykee;
import org.dobots.swarmcontrol.robots.RobotType;

public class RobotDeviceFactory {
		
	public static IRobotDevice getRobotDevice(RobotType robot) throws Exception
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
			case RBT_AC13ROVER:
				return new AC13Rover();
			default: 		
				throw new Exception();
		}
	}
}
