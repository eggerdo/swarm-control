package org.dobots.swarmcontrol;

import org.dobots.swarmcontrol.RemoteControlHelper.Move;

public interface RemoteControlListener {
	
	void onMove(Move i_oMove, double i_dblSpeed, double i_dblAngle);
	
	void onMove(Move i_oMove);
	
	void enableControl(boolean i_bEnable);
	
}
