package org.dobots.swarmcontrol.behaviours;

import java.util.ArrayList;

import org.dobots.robots.RobotDevice;
import org.dobots.robots.RobotDeviceFactory;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.SwarmControlActivity;
import org.dobots.swarmcontrol.robots.RobotView;
import org.dobots.swarmcontrol.robots.RobotViewFactory;
import org.dobots.swarmcontrol.robots.RobotType;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Dancing extends Activity {
	
	private Activity m_oActivity;
	
	private ArrayList<LinearLayout> m_oRobotViews;
	private ArrayList<RobotDevice>  m_oRobots;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
		this.m_oActivity = this;
		
		m_oRobotViews = new ArrayList<LinearLayout>();
		m_oRobots = new ArrayList<RobotDevice>();

		setProperties();
	}
	
	private void setProperties() {
        m_oActivity.setContentView(R.layout.dancing);
    	

		Button btnControl = (Button) m_oActivity.findViewById(R.id.btnCtrl);
		btnControl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(m_oActivity);
				builder.setTitle("Choose a robot");
				final ArrayAdapter<RobotType> adapter = new ArrayAdapter<RobotType>(m_oActivity, android.R.layout.select_dialog_item,
						RobotType.values());
				builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						RobotType eRobot = adapter.getItem(which);
						dialog.dismiss();
						addRobot(eRobot);
					}
				});
				builder.create().show();
				
			}
		});
		
	}
	
	private void addRobot(RobotType i_eRobot) {
		
		LinearLayout layDancing_AddedRobots = (LinearLayout) m_oActivity.findViewById(R.id.layDancing_AddedRobots);
		LinearLayout layRobot = (LinearLayout) LayoutInflater.from(m_oActivity).inflate(R.layout.dancing_robot, layDancing_AddedRobots);
		
		TextView lblRobotName = (TextView) layRobot.findViewById(R.id.lblDancing_RobotName);
		lblRobotName.setText(i_eRobot.toString());
		
		m_oRobotViews.add(layRobot);
		
		RobotDevice oRobot = RobotDeviceFactory.getRobotDevice(i_eRobot);
		m_oRobots.add(oRobot);
		
		Button btnGoto = (Button)layRobot.findViewById(R.id.btnDancing_goto);
		btnGoto.setTag(i_eRobot);
		btnGoto.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((SwarmControlActivity)SwarmControlActivity.getContext()).showRobot((RobotType)v.getTag());
			}
		});
		
		

		
	}
	
}
