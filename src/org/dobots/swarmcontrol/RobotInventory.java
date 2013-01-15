package org.dobots.swarmcontrol;

import java.util.ArrayList;

import org.dobots.robots.IRobotDevice;

public class RobotInventory {
	
	private static RobotInventory m_oInstance;

	private ArrayList<IRobotDevice> m_oRobotList = new ArrayList<IRobotDevice>();

	public static RobotInventory getInstance() {
		if (m_oInstance == null) {
			m_oInstance = new RobotInventory();
		}
		return m_oInstance;
	}
	
	public int addRobot(IRobotDevice i_oRobot) {
		m_oRobotList.add(i_oRobot);
		return findRobot(i_oRobot);
	}
	
	public IRobotDevice getRobot(int i_nIndex) {
		return m_oRobotList.get(i_nIndex);
	}

	public void removeRobot(IRobotDevice oRobot) {
		m_oRobotList.remove(oRobot);
	}
	
	public int findRobot(IRobotDevice i_oRobot) {
		return m_oRobotList.indexOf(i_oRobot);
	}
	
}
