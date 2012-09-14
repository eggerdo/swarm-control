package org.dobots.swarmcontrol.behaviours.dancing;

import java.util.ArrayList;

import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.RemoteControlHelper;
import org.dobots.swarmcontrol.behaviours.dancing.RobotList.RobotEntry;
import org.dobots.utility.OnButtonPress;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;

public class MultiRobotControl extends Activity {

	private static MultiRobotControl INSTANCE;
	
	private Activity m_oActivity;

	private RemoteControlHelper m_oRemoteCtrl;

	public static MultiRobotControl getInstance() {
		return INSTANCE;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		INSTANCE = this;
		
		this.m_oActivity = this;
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

		m_oRemoteCtrl = new RemoteControlHelper(m_oActivity);
		
		setProperties();
	}
	
	public void setProperties() {
		setContentView(R.layout.dancing_remotecontrol);
		

        m_oRemoteCtrl.setProperties();
        
        m_oRemoteCtrl.setControlPressListener(new OnButtonPress() {
        	
			@Override
			public void buttonPressed(boolean i_bDown) {
				enableControl(i_bDown);
			}
		});
        
        m_oRemoteCtrl.setFwdPressListener(new OnButtonPress() {
			
			@Override
			public void buttonPressed(boolean i_bDown) {
				if (i_bDown) {
					driveForward();
				} else {
					driveStop();
				}
			}
		});
        
		m_oRemoteCtrl.setBwdPressListener(new OnButtonPress() {
			
			@Override
			public void buttonPressed(boolean i_bDown) {
				if (i_bDown) {
					driveBackward();
				} else {
					driveStop();
				}
			}
		});
		
		m_oRemoteCtrl.setLeftPressListener(new OnButtonPress() {
			
			@Override
			public void buttonPressed(boolean i_bDown) {
				if (i_bDown) {
					rotateCounterClockwise();
				} else {
					driveStop();
				}
			}
		});
		
		m_oRemoteCtrl.setRightPressListener(new OnButtonPress() {
			
			@Override
			public void buttonPressed(boolean i_bDown) {
				if (i_bDown) {
					rotateClockwise();
				} else {
					driveStop();
				}
			}
		});

	}
	
	public static void enableControl(boolean i_bEnable) {
		for (RobotEntry entry : DancingMain.getInstance().getRobotList()) {
			entry.oRobot.enableControl(i_bEnable);
		}
	}
	
	public static void driveForward() {
		
		int nSpeed = 50;
		for (RobotEntry entry : DancingMain.getInstance().getRobotList()) {
			entry.oRobot.driveForward();
		}
		
	}
	
	public static void driveBackward() {

		int nSpeed = 50;
		for (RobotEntry entry : DancingMain.getInstance().getRobotList()) {
			entry.oRobot.driveBackward();
		}
		
	}
	
	public static void rotateCounterClockwise() {

		int nSpeed = 50;
		for (RobotEntry entry : DancingMain.getInstance().getRobotList()) {
			entry.oRobot.rotateCounterClockwise();
		}
		
	}
	
	public static void rotateClockwise() {

		int nSpeed = 50;
		for (RobotEntry entry : DancingMain.getInstance().getRobotList()) {
			entry.oRobot.rotateClockwise();
		}
		
	}
	
	public static void driveStop() {

		for (RobotEntry entry : DancingMain.getInstance().getRobotList()) {
			entry.oRobot.driveStop();
		}
		
	}
	
}
