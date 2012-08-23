package org.dobots.swarmcontrol.behaviours.dancing;

import java.util.ArrayList;

import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.behaviours.dancing.DanceList.DanceEntry;
import org.dobots.swarmcontrol.behaviours.dancing.RobotList.RobotEntry;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;

public class DancingMain extends TabActivity {

	private ArrayList<RobotEntry> m_oRobotList;
	private ArrayList<DanceEntry> m_oDanceList;

	public static DancingMain INSTANCE;
	
	public static DancingMain getInstance() {
		return INSTANCE;
	}
	
	public ArrayList<RobotEntry> getRobotList() {
		return m_oRobotList;
	}
	
	public ArrayList<DanceEntry> getDanceList() {
		return m_oDanceList;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dancing_main);
		
		INSTANCE = this;

		m_oRobotList = new ArrayList<RobotEntry>();
		m_oDanceList = new ArrayList<DanceEntry>();

		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		TabHost tabHost = getTabHost();
		
		TabSpec robotListSpec = tabHost.newTabSpec("Robot List");
		robotListSpec.setIndicator("Robot List");
		Intent robotListIntent = new Intent(this, RobotList.class);
		robotListSpec.setContent(robotListIntent);
		
		TabSpec remoteControlSpec = tabHost.newTabSpec("Remote Control");
		remoteControlSpec.setIndicator("Remote Control");
		Intent remoteControlIntent = new Intent(this, MultiRobotControl.class);
		remoteControlSpec.setContent(remoteControlIntent);
//		
		TabSpec danceListSpec = tabHost.newTabSpec("Dance List");
		danceListSpec.setIndicator("Dance List");
		Intent danceListIntent = new Intent(this, DanceList.class);
		danceListSpec.setContent(danceListIntent);
		
		tabHost.addTab(robotListSpec);
		tabHost.addTab(remoteControlSpec);
		tabHost.addTab(danceListSpec);
		
		TabWidget tabWidget = tabHost.getTabWidget();
		for (int i=0; i < tabWidget.getChildCount(); i++) {
			tabWidget.getChildAt(i).getLayoutParams().height = 50;
		}
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		for (RobotEntry entry : m_oRobotList) {
			entry.destroy();
		}
	}

}
