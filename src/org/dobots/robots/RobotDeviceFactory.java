package org.dobots.robots;

import org.dobots.robots.dotty.Dotty;
import org.dobots.robots.nxt.NXT;
import org.dobots.robots.parrot.Parrot;
import org.dobots.robots.robo40.Robo40;
import org.dobots.robots.roboscooper.RoboScooper;
import org.dobots.robots.roomba.Roomba;
import org.dobots.robots.spykee.Spykee;

import robots.RobotType;
import robots.ctrl.IRobotDevice;
import robots.ispytank.ctrl.SpyTank;
import robots.rover.ac13.ctrl.AC13Rover;
import robots.rover.rover2.ctrl.Rover2;

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
			case RBT_ROVER2:
				return new Rover2();
			case RBT_ROBO40:
				return new Robo40();
			case RBT_SPYTANK:
				return new SpyTank();
			default: 		
				throw new Exception();
		}
	}
}
