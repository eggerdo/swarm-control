package org.dobots.swarmcontrol.robots;

import org.dobots.swarmcontrol.R;

import android.widget.TextView;

public class UnimplementedRobot extends RobotView {

	@Override
	protected void onConnect() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDisconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onConnectError() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setProperties(RobotType i_eRobot) {
		setContentView(R.layout.robotdevice);
		
		TextView lblTitle = (TextView) findViewById(R.id.lblRobot);
		lblTitle.setText(i_eRobot.toString());
	}

	@Override
	protected void shutDown() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void connectToRobot() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void disconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void resetLayout() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateButtons(boolean i_bEnabled) {
		// TODO Auto-generated method stub

	}

}
