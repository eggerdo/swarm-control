package org.dobots.swarmcontrol.robots;

import org.dobots.swarmcontrol.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

public class RobotDevice extends Activity {
	
	public static String MAC_FILTER = "MAC_FILTER";
	
	protected static final int REQUEST_CONNECT_DEVICE = 1000;

	Activity m_oActivity;
	RobotType m_eRobot;

//	public void show(Activity myActivity, RobotType i_eRobot) {

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
		this.m_oActivity = this;

		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		m_eRobot = (RobotType) getIntent().getExtras().get("RobotType");
		
        setProperties(m_eRobot);
	}
   
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// do nothing
	}
	
	protected void setProperties(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.robotdevice);
        
		TextView lblRobot = (TextView) m_oActivity.findViewById(R.id.lblRobot);
		lblRobot.setText(i_eRobot.toString());
		
		Spinner spSensors = (Spinner) m_oActivity.findViewById(R.id.spSensors);
		spSensors.setVisibility(View.INVISIBLE);
	}
	
	public void close() {
		// to be implemented by child class
	}
	
}
