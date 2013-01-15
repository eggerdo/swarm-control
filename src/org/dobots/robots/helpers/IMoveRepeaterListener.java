package org.dobots.robots.helpers;

import org.dobots.robots.helpers.MoveRepeater.MoveCommand;

public interface IMoveRepeaterListener {
	
	void onDoMove(MoveCommand i_eMove, double i_dblSpeed);
	void onDoMove(MoveCommand i_eMove, double i_dblSpeed, int i_nRadius);

}
