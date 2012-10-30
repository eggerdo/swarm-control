package org.dobots.robots;

import java.io.IOException;

import org.dobots.swarmcontrol.robots.RobotType;

public interface RobotDevice {

	public interface OnFinishListener {
		public void onFinish();
	}
	
	public RobotType getType();
	public String getAddress();

	public void destroy();
	
	// connection methods
	public void setConnection();
	public void connect() throws IOException;
	public void disconnect();
	public boolean isConnected();
	
	// drive methods
	public void enableControl(boolean i_bEnable);
	
	public void moveForward(double i_dblSpeed);
	public void moveForward(double i_dblSpeed, int i_nRadius);
	
	public void moveBackward(double i_dblSpeed);
	public void moveBackward(double i_dblSpeed, int i_nRadius);

	public void moveBackward(double i_dblSpeed, double i_dblAngle);
	public void moveForward(double i_dblSpeed, double i_dblAngle);
	
	public void rotateClockwise(double i_dblSpeed);
	public void rotateCounterClockwise(double i_dblSpeed);
	
	public void moveStop();
	
	public void executeCircle(double i_dblTime, double i_dblSpeed);
	
	public void setBaseSpeed(double i_dblSpeed);
	public double getBaseSped();
	
	public void moveForward();
	public void moveBackward();
	public void rotateCounterClockwise();
	public void rotateClockwise();
	
}
