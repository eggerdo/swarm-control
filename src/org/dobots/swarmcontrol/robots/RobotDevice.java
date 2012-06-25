package org.dobots.swarmcontrol.robots;

import org.dobots.swarmcontrol.R;

import android.app.Activity;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

public class RobotDevice {

	Activity m_oActivity;
    
	public void show(Activity myActivity, RobotType i_eRobot) {
		this.m_oActivity = myActivity;
		
        m_oActivity.setContentView(R.layout.robotdevice);
        setProperties(i_eRobot);
	}
	
	protected void setProperties(RobotType i_eRobot) {
		TextView lblRobot = (TextView) m_oActivity.findViewById(R.id.lblRobot);
		lblRobot.setText(i_eRobot.toString());
		
		Spinner spSensors = (Spinner) m_oActivity.findViewById(R.id.spSensors);
		spSensors.setVisibility(View.INVISIBLE);
	}
	
	public void close() {
		// to be implemented
	}
	
}
