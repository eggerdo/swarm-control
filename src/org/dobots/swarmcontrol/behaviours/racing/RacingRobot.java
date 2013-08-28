package org.dobots.swarmcontrol.behaviours.racing;

import java.io.IOException;

import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.SwarmControlActivity;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.joystick.IJoystickListener;
import org.dobots.utilities.joystick.Joystick;

import robots.ctrl.IRobotDevice;
import robots.ctrl.RemoteControlHelper.Move;
import robots.gui.RobotInventory;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;

public class RacingRobot extends BaseActivity implements IJoystickListener {

	private static final String TAG = "RacingRobot";
	
	private static final int CONNECT_ID = Menu.FIRST;
	private static final int ROBOT_ID = CONNECT_ID + 1;
	
	private IRobotDevice m_oRobot;
	private BaseActivity m_oActivity;
	
	private Move lastMove = Move.NONE;

	private long lastTime = SystemClock.uptimeMillis();
	private double updateFrequency = 5.0; // Hz
	private int threshold = 20;

	private Joystick m_oJoystick;
	
	private String m_strRobotID;

	private boolean m_bKeepAlive = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		m_oActivity = this;

        m_strRobotID = (String) getIntent().getExtras().get("RobotID");
        m_oRobot = RobotInventory.getInstance().getRobot(m_strRobotID);
		
		setProperties();
		
		m_oRobot.enableControl(true);
	}
	
	private void setProperties() {
		setContentView(R.layout.joystick_remote);

		m_oJoystick = (Joystick) m_oActivity.findViewById(R.id.oJoystick);
		m_oJoystick.setUpdateListener(this);
		m_oJoystick.getLayoutParams().height = LayoutParams.MATCH_PARENT;
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, CONNECT_ID, 1, "Connect");
		menu.add(0, ROBOT_ID, 2, "Show Robot");
		return true;
	}
	

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case CONNECT_ID:
			m_oRobot.disconnect();
			try {
				m_oRobot.connect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case ROBOT_ID:
			showRobot();
			break;
		}
		return true;
	}

	final static int ROTATE_THRESHOLD = 40;
	final static int DIRECTION_THRESHOLD_1 = 10;
	final static int DIRECTION_THRESHOLD_2 = 30;
	
	@Override
	public void onUpdate(double i_dblPercentage, double i_dblAngle) {
		
		if (i_dblPercentage == 0 && i_dblAngle == 0) {
			// if percentage and angle is 0 this means the joystick was released
			// so we stop the robot
			onMove(Move.NONE, 0, 0);
			lastMove = Move.NONE;
		} else {

			// only allow a rate of updateFrequency to send drive commands
			// otherwise it will overload the robot's command queue
			if ((SystemClock.uptimeMillis() - lastTime) < 1/updateFrequency * 1000)
				return;

			lastTime = SystemClock.uptimeMillis();
			double dblAbsAngle = Math.abs(i_dblAngle);
			
			// determine which move should be executed based on the
			// last move and the angle of the joystick
			Move thisMove = Move.NONE;
			switch(lastMove) {
			case NONE:
				// for a low percentage (close to the center of the joystick) the
				// angle is too sensitive, so we only start once the percentage
				// is over the threshold
				if (i_dblPercentage < threshold) {
					return;
				}
			case LEFT:
			case RIGHT:
				// if the last move was left (or right respectively) we use a window
				// of +- 30 degrees, otherwise we switch to moving forward or backward
				// depending on the angle
				if (dblAbsAngle < ROTATE_THRESHOLD) {
					thisMove = Move.RIGHT;
				} else if ((180 - dblAbsAngle) < ROTATE_THRESHOLD) {
					thisMove = Move.LEFT;
				} else if (i_dblAngle > 0) {
					thisMove = Move.FORWARD;
				} else if (i_dblAngle < 0) {
					thisMove = Move.BACKWARD;
				}
				break;
			case BACKWARD:
				// if the last move was backward and the angle is within
				// 10 degrees of 0 or 180 degrees we still move backward
				// and cap the degree to 0 or 180 respectively
				if (i_dblAngle < 0) {
					thisMove = Move.BACKWARD;
				} else if (i_dblAngle < DIRECTION_THRESHOLD_1) {
					dblAbsAngle = 0;
					thisMove = Move.BACKWARD;
				} else if (i_dblAngle > 180 - DIRECTION_THRESHOLD_1) {
					dblAbsAngle = 180;
					thisMove = Move.BACKWARD;
				} else if (i_dblAngle < DIRECTION_THRESHOLD_2) {
					thisMove = Move.RIGHT;
				} else if (i_dblAngle > 180 - DIRECTION_THRESHOLD_2) {
					thisMove = Move.LEFT;
				} else {
					thisMove = Move.FORWARD;
				}
				break;
			case FORWARD:
				// if the last move was forward and the angle is within
				// 10 degrees of 0 or 180 degrees we still move forward
				// and cap the degree to 0 or 180 respectively
				if (i_dblAngle > 0) {
					thisMove = Move.FORWARD;
				} else if (i_dblAngle > -DIRECTION_THRESHOLD_1) {
					dblAbsAngle = 0;
					thisMove = Move.FORWARD;
				} else if (i_dblAngle < -(180 - DIRECTION_THRESHOLD_1)) {
					dblAbsAngle = 180;
					thisMove = Move.FORWARD;
				} else if (i_dblAngle > -DIRECTION_THRESHOLD_2) {
					thisMove = Move.RIGHT;
				} else if (i_dblAngle < -(180 - DIRECTION_THRESHOLD_2)) {
					thisMove = Move.LEFT;
				} else {
					thisMove = Move.BACKWARD;
				}
				break;
			}
			
			onMove(thisMove, i_dblPercentage, dblAbsAngle);
			lastMove = thisMove;
		}
	}

	public void onMove(Move i_oMove, double i_dblSpeed, double i_dblAngle) {

		// make sure it is listening for remote control commands
		m_oRobot.enableControl(true);
		
		// modify the angle so that it is between -90 and +90
		// where -90 is left and +90 is right
		i_dblAngle -= 90.0;
		
		// execute this move
		switch(i_oMove) {
		case NONE:
			m_oRobot.moveStop();
			Log.i(TAG, "stop()");
			break;
		case BACKWARD:
			m_oRobot.moveBackward(i_dblSpeed, i_dblAngle);
			Log.i(TAG, String.format("bwd(%f, %f)", i_dblSpeed, i_dblAngle));
			break;
		case FORWARD:
			m_oRobot.moveForward(i_dblSpeed, i_dblAngle);
			Log.i(TAG, String.format("fwd(%f, %f)", i_dblSpeed, i_dblAngle));
			break;
		case LEFT:
			m_oRobot.rotateCounterClockwise(i_dblSpeed);
			Log.i(TAG, String.format("c cw(%f)", i_dblSpeed));
			break;
		case RIGHT:
			m_oRobot.rotateClockwise(i_dblSpeed);
			Log.i(TAG, String.format("cw(%f)", i_dblSpeed));
			break;
		}
	}

	@Override
	public void onJoystickTouch(boolean start) {
		// TODO Auto-generated method stub
	}
	
	private void showRobot() {
		m_bKeepAlive = true;
		((SwarmControlActivity)SwarmControlActivity.getContext()).showRobot(m_oRobot.getType(), m_strRobotID);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		m_bKeepAlive = false;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (!m_bKeepAlive) {
			m_oRobot.disconnect();
		}
	}

}
