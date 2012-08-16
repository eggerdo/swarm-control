package org.dobots.robots;

import java.io.IOException;

public interface RobotDevice {
	
	// connection methods
	public void setConnection();
	public void connect() throws IOException;
	public void disconnect();
	public boolean isConnected();
	
	// drive methods
	public void driveForward(double i_nSpeed);
	public void driveForward(double i_nSpeed, int i_nRadius);
	
	public void driveBackward(double i_nSpeed);
	public void driveBackward(double i_nSpeed, int i_nRadius);
	
	public void rotateClockwise(double i_nSpeed);
	public void rotateCounterClockwise(double i_nSpeed);
	
	public void driveStop();
	
	

}
