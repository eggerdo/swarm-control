package org.dobots.robots.roboscooper;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dobots.robots.BrainlinkDevice;
import org.dobots.robots.MessageTypes;
import org.dobots.robots.MoveRepeater;
import org.dobots.robots.MoveRepeater.MoveCommand;
import org.dobots.robots.RobotDevice;
import org.dobots.robots.roboscooper.RoboScooperMoveRunner.SubMoveCommand;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.utility.Utils;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import edu.cmu.ri.createlab.brainlink.BluetoothConnection;
import edu.cmu.ri.createlab.brainlink.BrainLink;

/*
 * Note: The RoboScooper is a particular case. firstly it is not possible to
 * provide a speed parameter for the move commands because it is a robot
 * controlled by IR. there is only one signal for each direction. nevertheless
 * the robot can drive with different "speeds". If an ir signal, e.g. forward, is only
 * sent once, the robot will just make a small step. if the signal is sent continuously
 * the robot will increase it's speed and drive fast.
 * to reflect this with the brainlink was a bit tricky. repeatedly sending the signal
 * by calling transmitIRSignal did not always work and the robot started to stutter.
 * However the brainlink provides us with the possibility to define repeat times for
 * the signal itself which will then be continuously sent by the brainlink until the
 * turnOffIR function is called. this works well to move the RoboScooper with fast speed.
 * But if we only want to move the robot step by step then most of the times the IR signal
 * with the automatic repeat did not have any effect.
 * In order to provide both functionality (fast move and step by step move) we had to
 * adjust the moving pattern a bit and define two signals for each move, the normal signal
 * without repeat times and a "fast" signal with repeat times of 100ms. 
 * The Move Runnables which are provided to the MoveRepeater count how often they are
 * being executed. The first time a Runnable is executed it calls the normal signal 
 * (without repeat times). the second time a Runnable is executed it calls the fast
 * signal (and the brainlink will automatically repeat the signal). Every further iteration
 * of the runnable won't have any effect anymore (because the brainlink is repeating
 * the signal we don't have to do anything anymore. In order to stop the Move, the moveStop
 * function has to be called which turns off the IR signal and stopps the Runnable from
 * being executed.
 * Additionally, moving forward left and forward right (or backward resp.) requires sending a
 * different command to the brainlink than a normal move forward (or backward resp.) thus
 * we had to add SubMoveCommands to distinguish between left, straight and right while
 * moving forward or backward.
 */

public class RoboScooper extends BrainlinkDevice implements RobotDevice {
	
	public static final String TAG = "RoboScooper";
	
	private String m_strAddress;
	private Handler m_oUiHandler;
	
	private MoveRepeater m_oRepeater;
	
	private ExecutorService executorSerive = Executors.newCachedThreadPool();
	
	private int m_nInterval = 200;

	private double m_dblBaseSpeed = 100.0;
	
	private RoboScooperMoveRunner m_eCurrentMove = null;
	
	public RoboScooper() {
		// we supply our own move runnables so we don't have to
		// provide a move listener
		m_oRepeater = new MoveRepeater(null, m_nInterval);
	}

	@Override
	public RobotType getType() {
		return RobotType.RBT_ROBOSCOOPER;
	}

	@Override
	public String getAddress() {
		return m_strAddress;
	}

	@Override
	public void destroy() {
		if (isConnected()) {
			disconnect();
		}
	}

	public void setHandler(Handler i_oHandler) {
		m_oUiHandler = i_oHandler;
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
		// Initialize BrainLink
		try {
			if (m_oBrainLink != null) {
				m_oBrainLink = null;
			}
			
			m_oBrainLink = new BrainLink(m_oConnection.getInputStream(), m_oConnection.getOutputStream());
//			m_oBrainLink.setFullColorLED(0, 255, 0);
			
			boolean success = m_oBrainLink.initializeDevice(RoboScooperTypes.SIGNAL_FILE_NAME, RoboScooperTypes.SIGNAL_FILE_ENCODED);
			
			if (!success) {
				Utils.sendMessage(m_oUiHandler, RoboScooperTypes.INITIALISATION_FAILED, null);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void disconnect() {
		m_oBrainLink.setFullColorLED(0, 0, 0);
		close();
		
		m_oConnection.cancelSocket();
		m_bConnected = false;
	}

	@Override
	public boolean isConnected() {
		return m_bConnected;
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// NOTHING TO DO
	}
	
	private SubMoveCommand radiusToSubMove(int i_nRadius) {
		if (i_nRadius > 30) {
			return SubMoveCommand.LEFT;
		} else if (i_nRadius < -30) {
			return SubMoveCommand.RIGHT;
		} else {
			return SubMoveCommand.STRAIGHT;
		}
	}

	private void startMove(MoveCommand i_eMove, int i_nRadius) {
		// convert the radius to the SubMoveCommands LEFT, RIGHT, STRAIGHT
		SubMoveCommand eCmd = radiusToSubMove(i_nRadius);
		
		if (m_eCurrentMove == null || m_eCurrentMove.eMove != i_eMove) {
			// only start a new move if we are not currently running this move
			m_eCurrentMove = new RoboScooperMoveRunner(this, i_eMove, eCmd);
			m_oRepeater.startMove(m_eCurrentMove, true);
		} else if (m_eCurrentMove.eCmd != eCmd){
			// if the same move is already running and the sub move command
			// is different we change only that. this way we will directly go
			// to the fast move and skip the normal move command
			m_eCurrentMove.setSubMove(eCmd);
		} else {
			// no need to change anything, we are already executing this move
		}
	}
	
	private int angleToRadius(double i_dblAngle) {
		// just use the integer part of the angle as radius for now
		return (int) i_dblAngle;
	}

	// Move Forward ---------------------------------------------------
	
	@Override
	public void moveForward(double i_dblSpeed) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		
		moveForward(i_dblSpeed, 0);
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored

		startMove(MoveCommand.MOVE_FWD, i_nRadius);
	}

	@Override
	public void moveForward(double i_dblSpeed, double i_dblAngle) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored

		int nRadius = angleToRadius(i_dblAngle);
		moveForward(i_dblSpeed, nRadius);
	}

	@Override
	public void moveForward() {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		moveForward(m_dblBaseSpeed);
	}

	// Move Backward ---------------------------------------------------

	@Override
	public void moveBackward(double i_dblSpeed) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored

		moveBackward(i_dblSpeed, 0);
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored

		startMove(MoveCommand.MOVE_BWD, i_nRadius);
	}

	@Override
	public void moveBackward(double i_dblSpeed, double i_dblAngle) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored

		int nRadius = angleToRadius(i_dblAngle);
		moveBackward(i_dblSpeed, nRadius);
	}

	@Override
	public void moveBackward() {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		moveBackward(m_dblBaseSpeed);
	}

	// Move Left ---------------------------------------------------

	public void moveLeft(double i_dblSpeed) {
		// NOT AVAILABLE
	}

	@Override
	public void moveLeft() {
		// NOT AVAILABLE
	}

	// Move Right ---------------------------------------------------

	public void moveRight(double i_dblSpeed) {
		// NOT AVAILABLE
	}

	@Override
	public void moveRight() {
		// NOT AVAILABLE
	}
	
	// Rotate Right / Clockwise --------------------------------------

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored

		startMove(MoveCommand.ROTATE_RIGHT, 0);
	}

	@Override
	public void rotateClockwise() {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		rotateClockwise(m_dblBaseSpeed);
	}

	// Rotate Left / Counterclockwise --------------------------------

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored

		startMove(MoveCommand.ROTATE_LEFT, 0);
	}

	@Override
	public void rotateCounterClockwise() {
		// SPEED PARAMETER NOT AVAILABLE
		// the roboscooper sets the speed itself, depending on how
		// long the signal is sent. it is not possible to set a speed
		// thus the speed parameter will be ignored
		rotateCounterClockwise(m_dblBaseSpeed);
	}

	// Move Stop ---------------------------------------------------

	@Override
	public void moveStop() {
		// give some time for a command to be executed
		// before turning off the IR signals, otherwise we might turn
		// of the IR before the signal is completely sent
		Utils.waitSomeTime(100);
		m_oRepeater.stopMove();
		m_oBrainLink.turnOffIR();
		m_eCurrentMove = null;
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
	
	// RoboScooper specific commands --------------------------------

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

}
