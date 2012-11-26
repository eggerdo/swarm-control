package org.dobots.robots.roboscooper;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dobots.robots.MessageTypes;
import org.dobots.robots.MoveRepeater;
import org.dobots.robots.MoveRepeater.MoveCommand;
import org.dobots.robots.MoveRepeater.MoveRepeaterListener;
import org.dobots.robots.RobotDevice;
import org.dobots.robots.nxt.NXT;
import org.dobots.robots.parrot.ParrotTypes.ParrotMove;
import org.dobots.swarmcontrol.ConnectListener;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.swarmcontrol.robots.nxt.NXTBluetooth;
import org.dobots.utility.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import edu.cmu.ri.createlab.brainlink.BluetoothConnection;
import edu.cmu.ri.createlab.brainlink.BrainLink;

public class RoboScooper extends BrainlinkDevice implements RobotDevice, MoveRepeaterListener {
	
	public static final String TAG = "RoboScooper";
	
	private String m_strAddress;
	private Handler m_oUiHandler;
	
	private MoveRepeater m_oRepeater;
	
	private ExecutorService executorSerive = Executors.newCachedThreadPool();
	
	private int m_nInterval = 20;

	private double m_dblBaseSpeed = 100.0;
	
	public RoboScooper() {
		m_oRepeater = new MoveRepeater(this, m_nInterval);
	}

	@Override
	public RobotType getType() {
		// TODO Auto-generated method stub
		return RobotType.RBT_ROBOSCOOPER;
	}

	@Override
	public String getAddress() {
		// TODO Auto-generated method stub
		return m_strAddress;
	}

	@Override
	public void destroy() {
		if (isConnected()) {
			disconnect();
		}
	}

	public void setConnection(BluetoothDevice i_oDevice) {
		m_oConnection = new BluetoothConnection();
		m_oConnection.initializeBluetoothAdapter();
		m_oConnection.addDevice(i_oDevice);
		m_strAddress = i_oDevice.getAddress();
	}

	@Override
	public void connect() {
		executorSerive.submit(new Runnable() {
			
			@Override
			public void run() {
				m_bConnected = m_oConnection.socketConnect();
				
				if (m_bConnected) {
					Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTED, null);
					initializeRobot();
				} else {
					Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTERROR, null);
				}
			}
		});
	}

	private void initializeRobot() {
//		if(m_oBrainLink != null)
//			m_oBrainLink = null;
		
		// Initialize BrainLink
		try {
			m_oBrainLink = new BrainLink(m_oConnection.getInputStream(), m_oConnection.getOutputStream());
			m_oBrainLink.setFullColorLED(0, 255, 0);
			
			boolean success = m_oBrainLink.initializeDevice(RoboScooperTypes.SIGNAL_FILE_NAME, RoboScooperTypes.SIGNAL_FILE_ENCODED);
			
			if (!success) {
				Utils.sendMessage(m_oUiHandler, RoboScooperTypes.INITIALISATION_FAILED, null);
			}
			
//			mBrainLinkRobotInitial = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//			mBrainLinkRobotInitial = false;
		}
	}

	@Override
	public void disconnect() {
		m_oBrainLink.setFullColorLED(0, 0, 0);
		
		m_oConnection.cancelSocket();
		m_bConnected = false;
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return m_bConnected;
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// NOTHING TO DO
	}


	@Override
	public void onDoMove(MoveCommand i_eMove, double i_dblSpeed) {
		switch(i_eMove) {
		case MOVE_BWD:
			executeMoveBackward(i_dblSpeed);
			break;
		case MOVE_FWD:
			executeMoveForward(i_dblSpeed);
			break;
		case ROTATE_LEFT:
			executeRotateCounterClockwise(i_dblSpeed);
			break;
		case ROTATE_RIGHT:
			executeRotateClockwise(i_dblSpeed);
			break;
		default:
			Log.d(TAG, "Move not available");
			return;
		}
	}

	@Override
	public void onDoMove(MoveCommand i_eMove, double i_dblSpeed, int i_nRadius) {
		switch(i_eMove) {
		case MOVE_BWD:
			executeMoveBackward(i_dblSpeed, i_nRadius);
			break;
		case MOVE_FWD:
			executeMoveForward(i_dblSpeed, i_nRadius);
			break;
		default:
			Log.d(TAG, "Move not available");
			return;
		}
	}
	
	// Move Forward

	@Override
	public void moveForward(double i_dblSpeed) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		m_oRepeater.startMove(MoveCommand.MOVE_FWD, i_dblSpeed, true);
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		m_oRepeater.startMove(MoveCommand.MOVE_FWD, i_dblSpeed, i_nRadius, true);
	}

	@Override
	public void moveForward(double i_dblSpeed, double i_dblAngle) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		
		
		// TODO Auto-generated method stub
	}

	@Override
	public void moveForward() {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		moveForward(m_dblBaseSpeed);
	}

	public void executeMoveForward(double i_dblSpeed) {
		sendCommand(RoboScooperTypes.FORWARD);
	}

	public void executeMoveForward(double i_dblSpeed, int i_nRadius) {
		sendCommand(RoboScooperTypes.FORWARD);
		if (i_nRadius < 0) {
			sendCommand(RoboScooperTypes.RIGHT);
		} else if (i_nRadius > 0) {
			sendCommand(RoboScooperTypes.LEFT);
		}
	}
	
	// Move Backward

	@Override
	public void moveBackward(double i_dblSpeed) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		m_oRepeater.startMove(MoveCommand.MOVE_BWD, i_dblSpeed, true);
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		m_oRepeater.startMove(MoveCommand.MOVE_BWD, i_dblSpeed, i_nRadius, true);

	}

	@Override
	public void moveBackward(double i_dblSpeed, double i_dblAngle) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		
		// TODO Auto-generated method stub

	}

	@Override
	public void moveBackward() {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		moveBackward(m_dblBaseSpeed);
	}

	public void executeMoveBackward(double i_dblSpeed) {
		sendCommand(RoboScooperTypes.BACKWARD);
	}

	public void executeMoveBackward(double i_dblSpeed, int i_nRadius) {
		sendCommand(RoboScooperTypes.BACKWARD);
		if (i_nRadius < 0) {
			sendCommand(RoboScooperTypes.RIGHT);
		} else if (i_nRadius > 0) {
			sendCommand(RoboScooperTypes.LEFT);
		}
	}
	
	// Move Left

	public void moveLeft(double i_dblSpeed) {
		// NOT AVAILABLE
	}

	@Override
	public void moveLeft() {
		// NOT AVAILABLE
	}

	// Move Right

	public void moveRight(double i_dblSpeed) {
		// NOT AVAILABLE
	}

	@Override
	public void moveRight() {
		// NOT AVAILABLE
	}
	
	// Rotate Right / Clockwise

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		m_oRepeater.startMove(MoveCommand.ROTATE_RIGHT, i_dblSpeed, true);
	}

	@Override
	public void rotateClockwise() {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		rotateClockwise(m_dblBaseSpeed);
	}

	private void executeRotateClockwise(double i_dblSpeed) {
		sendCommand(RoboScooperTypes.RIGHT);
	}
	
	// Rotate Left / Counterclockwise

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		m_oRepeater.startMove(MoveCommand.ROTATE_LEFT, i_dblSpeed, true);
	}

	@Override
	public void rotateCounterClockwise() {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		rotateCounterClockwise(m_dblBaseSpeed);
	}
	
	private void executeRotateCounterClockwise(double i_dblSpeed) {
		sendCommand(RoboScooperTypes.LEFT);
	}

	// Move Stop

	@Override
	public void moveStop() {
		m_oRepeater.stopMove();
	}

	
	@Override
	public void executeCircle(double i_dblTime, double i_dblSpeed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBaseSpeed(double i_dblSpeed) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the base speed is always set to 100
	}

	@Override
	public double getBaseSped() {
		// TODO Auto-generated method stub
		return m_dblBaseSpeed;
	}

	public void pickUp() {
		sendCommand(RoboScooperTypes.PICKUP);
	}
	
	public void dump() {
		sendCommand(RoboScooperTypes.DUMP);
	}
	
	public void setTalkMode() {
		sendCommand(RoboScooperTypes.TALK);
	}
	
	public void setVisionMode() {
		sendCommand(RoboScooperTypes.VISION);
	}
	
	public void setWhackMode() {
		sendCommand(RoboScooperTypes.WHACK);
	}
	
	public void stop() {
		sendCommand(RoboScooperTypes.STOP);
	}
	
	public void setAutonomous() {
		sendCommand(RoboScooperTypes.AUTONOMOUS);
	}

	public void setHandler(Handler i_oHandler) {
		m_oUiHandler = i_oHandler;
	}

}
