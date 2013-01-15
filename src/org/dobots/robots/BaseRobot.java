package org.dobots.robots;

import org.dobots.utility.log.Loggable;

public abstract class BaseRobot extends Loggable implements IRobotDevice {

	protected double capSpeed(double io_dblSpeed) {
		// if a negative value was provided as speed
		// use the absolute value of it.
		io_dblSpeed = Math.abs(io_dblSpeed);
		io_dblSpeed = Math.min(io_dblSpeed, 100);
		io_dblSpeed = Math.max(io_dblSpeed, 0);
		
		return io_dblSpeed;
	}

}
