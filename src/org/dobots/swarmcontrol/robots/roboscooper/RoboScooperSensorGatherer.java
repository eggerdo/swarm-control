package org.dobots.swarmcontrol.robots.roboscooper;

import org.dobots.robots.roboscooper.RoboScooper;
import org.dobots.swarmcontrol.robots.BrainlinkSensorGatherer;
import org.dobots.utilities.BaseActivity;

public class RoboScooperSensorGatherer extends BrainlinkSensorGatherer {

	public RoboScooperSensorGatherer(BaseActivity i_oActivity, RoboScooper i_oRobot) {
		super(i_oActivity, i_oRobot.getBrainlink());
	}

}
