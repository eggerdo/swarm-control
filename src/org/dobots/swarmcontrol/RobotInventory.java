package org.dobots.swarmcontrol;

import java.util.ArrayList;

import org.dobots.robots.RobotDevice;

public class RobotInventory {
	
	private static RobotInventory m_oInstance;

	private ArrayList<RobotDevice> m_oRobotList = new ArrayList<RobotDevice>();

	public static RobotInventory getInstance() {
		if (m_oInstance == null) {
			m_oInstance = new RobotInventory();
		}
		return m_oInstance;
	}
	
	public int addRobot(RobotDevice i_oRobot) {
		m_oRobotList.add(i_oRobot);
		return m_oRobotList.indexOf(i_oRobot);
	}
	
	public RobotDevice getRobot(int i_nIndex) {
		return m_oRobotList.get(i_nIndex);
	}

	public void removeRobot(RobotDevice oRobot) {
		m_oRobotList.remove(oRobot);
	}
	
}
