package org.dobots.utility.joystick;

public interface IJoystickListener {
	
	public void onUpdate(double i_dblPercentage, double i_dblAngle);
	
	public void onJoystickTouch(boolean start);
	
}