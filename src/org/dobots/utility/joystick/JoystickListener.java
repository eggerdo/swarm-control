package org.dobots.utility.joystick;

public interface JoystickListener {
	
	public void onUpdate(double i_dblPercentage, double i_dblAngle);
	
	public void onJoystickTouch(boolean start);
	
}