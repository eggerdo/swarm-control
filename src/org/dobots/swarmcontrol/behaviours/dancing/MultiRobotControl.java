package org.dobots.swarmcontrol.behaviours.dancing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.dobots.robots.RobotDevice;
import org.dobots.robots.parrot.Parrot;
import org.dobots.swarmcontrol.BaseActivity;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.RemoteControlHelper;
import org.dobots.swarmcontrol.RemoteControlHelper.Move;
import org.dobots.swarmcontrol.RemoteControlListener;
import org.dobots.swarmcontrol.behaviours.dancing.RobotList.RobotEntry;
import org.dobots.utility.OnButtonPress;
import org.dobots.utility.Utils;

import com.codeminders.ardrone.NavData.FlyingState;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class MultiRobotControl extends BaseActivity implements
		RemoteControlListener {

	private static final String TAG = "MultiRobotControl";

	private static MultiRobotControl INSTANCE;

	private BaseActivity m_oActivity;

	private RemoteControlHelper m_oRemoteCtrl;

	public static MultiRobotControl getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new MultiRobotControl();
		}
		return INSTANCE;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		INSTANCE = this;

		m_oActivity = this;
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

		m_oRemoteCtrl = new RemoteControlHelper(m_oActivity, null, this);

		setProperties();
	}

	public void setProperties() {
		setContentView(R.layout.dancing_remotecontrol);

		m_oRemoteCtrl.setProperties();
		m_oRemoteCtrl.updateButtons(true);
	}
	
	public void enableControl(boolean i_bEnable) {
		Log.d(TAG, "Enable Control");
		for (RobotEntry entry : DancingMain.getInstance().getRobotList()) {
			entry.oRobot.enableControl(i_bEnable);
		}
	}

	public static void moveForward() {

		Log.d(TAG, "Move Forward");
		for (RobotEntry entry : DancingMain.getInstance().getRobotList()) {
			entry.oRobot.moveForward();
		}

	}

	public static void moveBackward() {

		Log.d(TAG, "Move Backward");
		for (RobotEntry entry : DancingMain.getInstance().getRobotList()) {
			entry.oRobot.moveBackward();
		}

	}

	public static void rotateCounterClockwise() {

		Log.d(TAG, "Rotate Counter Clockwise");
		for (RobotEntry entry : DancingMain.getInstance().getRobotList()) {
			entry.oRobot.rotateCounterClockwise();
		}

	}

	public static void rotateClockwise() {

		Log.d(TAG, "Rotate Clockwise");
		for (RobotEntry entry : DancingMain.getInstance().getRobotList()) {
			entry.oRobot.rotateClockwise();
		}

	}

	public static void moveStop() {

		Log.d(TAG, "Move Stop");
		for (RobotEntry entry : DancingMain.getInstance().getRobotList()) {
			entry.oRobot.moveStop();
		}

	}
	
	public static void moveLeft() {

		Log.d(TAG, "Move Left");
		for (RobotEntry entry : DancingMain.getInstance().getRobotList()) {
			entry.oRobot.moveLeft();
		}

	}

	public static void moveRight() {

		Log.d(TAG, "Move Right");
		for (RobotEntry entry : DancingMain.getInstance().getRobotList()) {
			entry.oRobot.moveRight();
		}

	}


	@Override
	public void onMove(Move i_oMove, double i_dblSpeed, double i_dblAngle) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMove(Move i_oMove) {

		// execute this move
		switch (i_oMove) {
		case NONE:
			moveStop();
			break;
		case BACKWARD:
			moveBackward();
			break;
		case FORWARD:
			moveForward();
			break;
		case LEFT:
			rotateCounterClockwise();
			break;
		case RIGHT:
			rotateClockwise();
			break;
		}
	}

}
