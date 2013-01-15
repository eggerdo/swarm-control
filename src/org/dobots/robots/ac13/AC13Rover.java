package org.dobots.robots.ac13;

// Uses the AC13Communication Library created by Uceta
// http://sourceforge.net/projects/ac13javalibrary/

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dobots.robots.DifferentialRobot;
import org.dobots.robots.MessageTypes;
import org.dobots.robots.ac13.AC13RoverTypes.AC13RoverParameters;
import org.dobots.robots.ac13.AC13RoverTypes.VideoResolution;
import org.dobots.robots.helpers.IMoveRepeaterListener;
import org.dobots.robots.helpers.MoveRepeater;
import org.dobots.robots.helpers.MoveRepeater.MoveCommand;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.utility.Utils;

import android.os.Handler;

public class AC13Rover extends DifferentialRobot implements IMoveRepeaterListener {
	
	private static final String TAG = "AC13Rover";
	
//	private AC13Comunicator m_oController;
	private AC13Controller m_oController;
	
	private VideoResolution m_eResolution = VideoResolution.res_unknown;

	private Handler m_oUiHandler;

	private ExecutorService executorSerive = Executors.newCachedThreadPool();

	private Timer m_oKeepAliveTimer;

	private double m_dblBaseSpeed = 50.0;

	private MoveRepeater m_oRepeater;
	
	public AC13Rover() {
		super(AC13RoverTypes.AXLE_WIDTH, AC13RoverTypes.MAX_SPEED, AC13RoverTypes.MIN_RADIUS, AC13RoverTypes.MAX_RADIUS);
		
//		m_oController = new AC13Comunicator();
		m_oController = new AC13Controller();
		m_oController.setLogListener(m_oLogListener);

		m_oKeepAliveTimer = new Timer("KeepAliveTimer");
		m_oKeepAliveTimer.schedule(m_oKeepAliveTask, 60000, 60000);

		m_oRepeater = new MoveRepeater(this, 500);
	}

	private final TimerTask m_oKeepAliveTask = new TimerTask() {
		
		@Override
		public void run() {
			keepAlive();
		}
		
	};

	public void keepAlive() {
		if (m_oController.isConnected()) {
			m_oController.keepAlive();
		}
	}

	// Default Robot Device Functions =========================================

	public void setHandler(Handler i_oUiHandler) {
		m_oUiHandler = i_oUiHandler;
	}

	@Override
	public RobotType getType() {
		// TODO Auto-generated method stub
		return RobotType.RBT_AC13ROVER;
	}

	@Override
	public String getAddress() {
		// TODO Auto-generated method stub
		return AC13RoverTypes.ADDRESS;
	}

	@Override
	public void destroy() {
		disconnect();
	}

	@Override
	public void connect() {
		executorSerive.submit(new Runnable() {
			
			@Override
			public void run() {
				if (m_oController.connect()) {
					Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTED, null);
				} else {
					Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTERROR, null);
				}
			}
		});
	}

	@Override
	public void disconnect() {
		if (m_oController.isConnected()) {
			m_oController.disconnect();
			Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_DISCONNECTED, null);
		}
	}

	@Override
	public boolean isConnected() {
		return m_oController.isConnected();
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// nothing to do
	}

	@Override
	public void moveForward(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.MOVE_FWD, i_dblSpeed, true);
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		m_oRepeater.startMove(MoveCommand.MOVE_FWD, i_dblSpeed, i_nRadius, true);
	}

	@Override
	public void moveForward(double i_dblSpeed, double i_dblAngle) {

		debug(TAG, String.format("moveForward(%f, %f)", i_dblSpeed, i_dblAngle));
		
		if (Math.abs(i_dblAngle) < 10) {
			moveForward(i_dblSpeed);
		} else {
			int nRadius = angleToRadius(i_dblAngle);
			moveForward(i_dblSpeed, nRadius);
		}
		
	}

	@Override
	public void moveBackward(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.MOVE_BWD, i_dblSpeed, true);
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		m_oRepeater.startMove(MoveCommand.MOVE_BWD, i_dblSpeed, i_nRadius, true);
	}

	@Override
	public void moveBackward(double i_dblSpeed, double i_dblAngle) {

		debug(TAG, String.format("moveBackward(%f, %f)", i_dblSpeed, i_dblAngle));
		
		if (Math.abs(i_dblAngle) < 10) {
			moveBackward(i_dblSpeed);
		} else {
			int nRadius = angleToRadius(i_dblAngle);
			moveBackward(i_dblSpeed, nRadius);
		}
		
	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.ROTATE_LEFT, i_dblSpeed, true);
	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.ROTATE_RIGHT, i_dblSpeed, true);
	}

	@Override
	public void moveStop() {
		m_oRepeater.stopMove();
		m_oController.moveStop();
	}

	
	// ------------------------------------------------------------------------------------------

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
			error(TAG, "Move not available");
			return;
		}
	}

	private void executeMoveForward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);

		m_oController.moveForward(nVelocity);
	}
	
	private void executeMoveBackward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);

		m_oController.moveBackward(nVelocity);
	}
	
	private void executeRotateCounterClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oController.rotateLeft(nVelocity);
	}
	
	private void executeRotateClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oController.rotateRight(nVelocity);
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
			error(TAG, "Move not available");
			return;
		}
	}

	private void executeMoveForward(double i_dblSpeed, int i_nRadius) {
		int velocity[] = {0, 0};
		calculateVelocity(i_dblSpeed, i_nRadius, velocity);
		
		m_oController.moveForward(velocity[0], velocity[1]);
	}

	private void executeMoveBackward(double i_dblSpeed, int i_nRadius) {
		int velocity[] = {0, 0};
		calculateVelocity(i_dblSpeed, i_nRadius, velocity);

		m_oController.moveBackward(velocity[0], velocity[1]);
	}

	
	// ------------------------------------------------------------------------------------------

	@Override
	public void executeCircle(double i_dblTime, double i_dblSpeed) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setBaseSpeed(double i_dblSpeed) {
		m_dblBaseSpeed = i_dblSpeed;
	}

	@Override
	public double getBaseSped() {
		return m_dblBaseSpeed;
	}

	@Override
	public void moveForward() {
		moveForward(m_dblBaseSpeed);
	}

	@Override
	public void moveBackward() {
		moveBackward(m_dblBaseSpeed);
	}

	@Override
	public void rotateCounterClockwise() {
		rotateCounterClockwise(m_dblBaseSpeed);
	}

	@Override
	public void rotateClockwise() {
		rotateClockwise(m_dblBaseSpeed);
	}

	@Override
	public void moveLeft() {
		// not available
	}

	@Override
	public void moveRight() {
		// not available
	}

	// Custom AC 13 Rover Functions ====================================================
	
	public void switchInfrared() {
		m_oController.switchInfrared();
	}
	
	public boolean isInfraredEnabled() {
		return m_oController.isInfraredEnabled();
	}
	
	public boolean startStreaming() {
		return m_oController.startStreaming();
	}
	
	public void stopStreaming() {
		m_oController.stopStreaming();
	}
	
	public boolean isStreaming() {
		return m_oController.isStreaming();
	}
	
	public void setResolution(VideoResolution i_eResolution) {
		if (m_eResolution != i_eResolution) {
			switch(i_eResolution) {
			case res_320x240:
				m_oController.setResolution320x240();
				break;
			case res_640x480:
				m_oController.setResolution640x480();
				break;
			}
			m_eResolution = i_eResolution;
		}
	}
	
	public VideoResolution getResolution() {
		if (m_eResolution == VideoResolution.res_unknown) {
			// we don't know what the resolution is so we obtain it from the robot
			AC13RoverParameters param = m_oController.getParameters();
			switch (Integer.valueOf(param.resolution)) {
			case 8:
				m_eResolution = VideoResolution.res_320x240;
				break;
			case 32:
				m_eResolution = VideoResolution.res_640x480;
				break;
			}
		}
		return m_eResolution;
	}
	
	public void setVideoListener(IAC13VideoListener i_oListener) {
		m_oController.setVideoListener(i_oListener);
	}
	
	public void removeVideoListener(IAC13VideoListener i_oListener) {
		m_oController.removeVideoListener(i_oListener);
	}
	
	public byte[] getImageBuffer() {
		return null;
//		return m_oController.GetImageBuffer();
	}
	
}
