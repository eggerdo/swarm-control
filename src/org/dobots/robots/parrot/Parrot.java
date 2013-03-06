package org.dobots.robots.parrot;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;

import org.dobots.robots.BaseRobot;
import org.dobots.robots.MessageTypes;
import org.dobots.robots.helpers.IMoveRepeaterListener;
import org.dobots.robots.helpers.MoveRepeater;
import org.dobots.robots.helpers.MoveRepeater.MoveCommand;
import org.dobots.swarmcontrol.IConnectListener;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.utility.Utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.ARDrone.VideoChannel;
import com.codeminders.ardrone.DroneStatusChangeListener;
import com.codeminders.ardrone.DroneVideoListener;
import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavData.CtrlState;
import com.codeminders.ardrone.NavData.FlyingState;
import com.codeminders.ardrone.NavDataListener;

public class Parrot extends BaseRobot implements DroneStatusChangeListener, NavDataListener, IConnectListener, IMoveRepeaterListener {

	private static String TAG = "Parrot";

	private ARDrone m_oController;

	private Handler m_oUiHandler;

	private boolean m_bConnected = false;

	private double m_dblBaseSpeed = 40.0;

	private VideoChannel m_eVideoChannel = ARDrone.VideoChannel.HORIZONTAL_ONLY;
	
	private Parrot m_oInstance;
	
	private FlyingState flyingState;
	private CtrlState controlState;
	private Object state_mutex = new Object();
	
	private MoveRepeater m_oRepeater;

	public Parrot() {
		m_oInstance = this;
		
		m_oRepeater = new MoveRepeater(this, 100);
	}

	public void setHandler(Handler i_oHandler) {
		m_oUiHandler = i_oHandler;
	}

	@Override
	public RobotType getType() {
		return RobotType.RBT_PARROT;
	}

	@Override
	public String getAddress() {
		return ParrotTypes.PARROT_IP;
	}

	@Override
	public void destroy() {
		if (isConnected()) {
			disconnect();
		}
	}

	
	private class DroneStarter implements Runnable {

		@Override
		public void run() {
			try {
				m_oController = new ARDrone(InetAddress.getByName(ParrotTypes.PARROT_IP), 10000, 60000);
				m_oController.connect();
				m_oController.clearEmergencySignal();
				m_oController.waitForReady(ParrotTypes.CONNECTION_TIMEOUT);
				m_oController.playLED(1, 10, 4);
				m_oController.selectVideoChannel(ARDrone.VideoChannel.HORIZONTAL_ONLY);
				m_oController.setCombinedYawMode(true);
				m_oController.addNavDataListener(m_oInstance);
				
				m_bConnected = true;
				Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTED, null);
				return;
			} catch (Exception e) {
				try {
					m_oController.clearEmergencySignal();
					m_oController.clearImageListeners();
					m_oController.clearNavDataListeners();
					m_oController.clearStatusChangeListeners();
					m_oController.disconnect();
				} catch (Exception e1) {
				}
	
			}
			m_bConnected = false;
			Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTERROR, null);
		}
		
	}
	
	private DroneStarter m_oDroneStarter = new DroneStarter();

	@Override
	public void connect() {
		new Thread(new DroneStarter()).start();
	}

	public void setVideoListener(DroneVideoListener i_oListener) {
		m_oController.addImageListener(i_oListener); 
	}

	public void removeVideoListener(DroneVideoListener i_oListener) {
		m_oController.removeImageListener(i_oListener);
	}

	public void setNavDataListener(NavDataListener i_oListener) {
		m_oController.addNavDataListener(i_oListener);
	}

	public void removeNavDataListener(NavDataListener i_oListener) {
		m_oController.removeNavDataListener(i_oListener);
	}

	@Override
	public void navDataReceived(NavData nd) {
		synchronized(state_mutex) {
			flyingState = nd.getFlyingState();
			controlState = nd.getControlState();
			state_mutex.notifyAll();
		}
	}
	
	public void waitForState(FlyingState i_oState, long i_lTimeout) throws TimeoutException {

        long since = System.currentTimeMillis();
        synchronized(state_mutex)
        {
            while(true)
            {
            	if (flyingState == i_oState) {
            		return; // OK, state reached
            	} else if ((System.currentTimeMillis() - since) >= i_lTimeout) {
            		// timeout
            		throw new TimeoutException();
            	}
            	
                long p = Math.min(i_lTimeout - (System.currentTimeMillis() - since), i_lTimeout);
                if(p > 0)
                {
                    try
                    {
                        state_mutex.wait(p);
                    } catch(InterruptedException e)
                    {
                        // Ignore
                    }
                }
            }
        }
	}
	

	@Override
	public void disconnect() {
		try {
			if (m_oController != null) {
				m_oController.disconnect();
			}
			m_oController = null;
			m_bConnected = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isConnected() {
		return m_bConnected;
	}

	public void sendEmergencySignal() {
		try {
			m_oController.sendEmergencySignal();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setVideoChannel(VideoChannel i_oChannel) {
		try {
			m_oController.selectVideoChannel(i_oChannel);
			m_eVideoChannel = i_oChannel;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void switchCamera() {
		switch (m_eVideoChannel) {
		case HORIZONTAL_ONLY:
			setVideoChannel(VideoChannel.VERTICAL_ONLY);
			break;
		case VERTICAL_ONLY:
			setVideoChannel(VideoChannel.HORIZONTAL_ONLY);
			break;
		}
	}

	public VideoChannel getVidoeChannel() {
		return m_eVideoChannel;
	}

	// Take Off -----------------------------------------------------------
	
	public void takeOff() {
		try {
			m_oRepeater.stopMove();
			
			synchronized (m_oRepeater.getMutex()) {
				
				m_oController.clearEmergencySignal();
				m_oController.trim();
				m_oController.takeOff();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	// Land --------------------------------------------------------------

	public void land() {
		try {
			m_oRepeater.stopMove();
			
			synchronized (m_oRepeater.getMutex()) {
				
				m_oController.land();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	// Altitude Control ------------------------------------------------

	private AltitudeControl ctrl;

	public void stopAltitudeControl() {
		ctrl.bRun = false;
		hover();
	}

	public void setAltitude(double i_dblSetpoint) {
		m_oRepeater.stopMove();
		
		ctrl = new AltitudeControl(i_dblSetpoint);
		ctrl.start();
	}

	public double Kp = 0.1, Kd = 0, Ki = 0;

	class AltitudeControl extends Thread implements NavDataListener {

		private boolean bSetpointReached = false;

		boolean bNavDataReceived = false;
		private NavData oNavData;

		double dblSetpoint;
		// static final double Kp = 1, Kd = 0, Ki = 0;
		double dblLastError;
		long lLastTime;
		double dblIntegratedError;
		double dblSpeed;

		int count = 0;

		public boolean bRun = true;

		public AltitudeControl(double i_dblAltitudeSetpoint) {
			dblSetpoint = i_dblAltitudeSetpoint;
			initPIDControl();
		}

		@Override
		public void run() {
			m_oController.addNavDataListener(this);

			while (bRun && !bSetpointReached) {
				if (bNavDataReceived) {
					double dblError = dblSetpoint - oNavData.getAltitude();

					if (Math.abs(dblError) <= 0.01) {
						if (++count == 5) {
							debug(TAG, "Setpoint Reached");
							bSetpointReached = true;
							hover();
						}
					} else {
						count = 0;

						dblSpeed = pidControl(dblError);
						debug(TAG, String.format(
								"Altitude: %f, Error:%f, Speed: %f",
								oNavData.getAltitude(), dblError, dblSpeed));
						if ((dblSpeed > 0) && (dblSpeed <= 100)) {
							executeMoveUp(dblSpeed);
						} else if ((dblSpeed < 0) && (dblSpeed >= -100)) {
							executeMoveDown(-dblSpeed);
						} else {
							debug(TAG, "Fatal Error");
						}
					}

					Utils.waitSomeTime(100);
					bNavDataReceived = false;
				}
			}

			m_oController.removeNavDataListener(this);
		}

		@Override
		public void navDataReceived(NavData nd) {
			oNavData = nd;
			bNavDataReceived = true;
		}

		private void initPIDControl() {
			dblLastError = 0.0;
			lLastTime = -1;
			dblIntegratedError = 0.0;
		}

		private double pidControl(double i_dblError) {
			double dblTermI = 0.0, dblTermD = 0.0, dblTermP = 0.0;
			long lTimeNow = SystemClock.uptimeMillis();
			long dt = lTimeNow - lLastTime;
			if (dt == 0) {
				error(TAG, "Time Interval is 0!");
				return 0;
			}

			dblTermP = Kp + Math.abs(i_dblError);

			if (lLastTime != -1) {
				dblTermD = Math.abs(i_dblError - dblLastError) / dt * Kd;

				dblIntegratedError += Math.abs(i_dblError) * dt;
				dblTermI = dblIntegratedError * Ki;
			}

			double dblResult = dblTermI + dblTermD + dblTermP;

			dblLastError = i_dblError;
			lLastTime = lTimeNow;

			return Math.signum(i_dblError) * Math.min(dblResult * 100.0, 100.0);
		}
	}

	// Increase Altitude ------------------------------------------------------

	public void increaseAltitude() {
//		increaseAltitude(m_dblBaseSpeed);
		increaseAltitude(40);
	}

	public void increaseAltitude(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.MOVE_UP, i_dblSpeed, true);
	}

	private void executeMoveUp(double i_dblSpeed) {
		try {
			m_oController.move(0f, 0f, (float) i_dblSpeed / 100f, 0f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Decrease Altitude ------------------------------------------------------

	public void decreaseAltitude() {
//		decreaseAltitude(m_dblBaseSpeed);
		decreaseAltitude(40);
	}

	public void decreaseAltitude(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.MOVE_DOWN, i_dblSpeed, true);
	}

	public void executeMoveDown(double i_dblSpeed) {
		try {
			m_oController.move(0f, 0f, -(float) i_dblSpeed / 100f, 0f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Hover ------------------------------------------------------

	public void hover() {
		try {
			m_oController.hover();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// nothing to do
	}

	private int capRadius(int io_nRadius) {
		// io_nRadius = Math.min(io_nRadius, DottyTypes.MAX_RADIUS);
		// io_nRadius = Math.max(io_nRadius, -DottyTypes.MAX_RADIUS);

		return io_nRadius;
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
		case MOVE_LEFT:
			executeMoveLeft(i_dblSpeed);
			break;
		case MOVE_RIGHT:
			executeMoveRight(i_dblSpeed);
			break;
		case MOVE_UP:
			executeMoveUp(i_dblSpeed);
			break;
		case MOVE_DOWN:
			executeMoveDown(i_dblSpeed);
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

	@Override
	public void onDoMove(MoveCommand i_eMove, double i_dblSpeed, int i_nRadius) {
		switch(i_eMove) {
		case MOVE_BWD:
			executeMoveBackward(i_dblSpeed, i_nRadius);
			break;
		case MOVE_FWD:
			executeMoveForward(i_dblSpeed, i_nRadius);
			break;
		case MOVE_LEFT:
			executeMoveLeft(i_dblSpeed, i_nRadius);
			break;
		case MOVE_RIGHT:
			executeMoveRight(i_dblSpeed, i_nRadius);
			break;
		}
	}

	// Move Forward ------------------------------------------------------

	@Override
	public void moveForward() {
//		moveForward(m_dblBaseSpeed);
		moveForward(15);
	}

	@Override
	public void moveForward(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.MOVE_FWD, i_dblSpeed, true);
	}

	private void executeMoveForward(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		try {
			m_oController.move(0f, -(float) i_dblSpeed / 100f, 0f, 0f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		m_oRepeater.startMove(MoveCommand.MOVE_FWD, i_dblSpeed, i_nRadius, true);
	}

	@Override
	public void moveForward(double i_dblSpeed, double i_dblAngle) {
		// TODO Auto-generated method stub
	}
	
	private void executeMoveForward(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		i_nRadius = capRadius(i_nRadius);

		// TODO implement movement in two directions at the same time
	}
	
	// Move Backward ------------------------------------------------------

	@Override
	public void moveBackward() {
		moveBackward(15);
//		moveBackward(m_dblBaseSpeed);
	}

	@Override
	public void moveBackward(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.MOVE_BWD, i_dblSpeed, true);
	}

	public void executeMoveBackward(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		try {
			m_oController.move(0f, (float) i_dblSpeed / 100f, 0f, 0f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		i_nRadius = capRadius(i_nRadius);
		
		m_oRepeater.startMove(MoveCommand.MOVE_BWD, i_dblSpeed, i_nRadius, true);
	}

	@Override
	public void moveBackward(double i_dblSpeed, double i_dblAngle) {
		// TODO Auto-generated method stub
	}

	private void executeMoveBackward(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		i_nRadius = capRadius(i_nRadius);

		// TODO implement movement in two directions at the same time
	}
	

	// Move Left ------------------------------------------------------

	public void moveLeft() {
//		moveLeft(m_dblBaseSpeed);
		moveLeft(15);
	}

	public void moveLeft(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.MOVE_LEFT, i_dblSpeed, true);
	}

	public void executeMoveLeft(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		try {
			m_oController.move(-(float) i_dblSpeed / 100f, 0f, 0f, 0f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void moveLeft(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		i_nRadius = capRadius(i_nRadius);

		m_oRepeater.startMove(MoveCommand.MOVE_LEFT, i_dblSpeed, i_nRadius, true);
	}

	private void executeMoveLeft(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		i_nRadius = capRadius(i_nRadius);

		// TODO implement movement in two directions at the same time
	}
	

	// Move Right ------------------------------------------------------

	public void moveRight() {
		moveRight(15);
//		moveRight(m_dblBaseSpeed);
	}

	public void moveRight(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.MOVE_RIGHT, i_dblSpeed, true);
	}

	public void executeMoveRight(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		try {
			m_oController.move((float) i_dblSpeed / 100f, 0f, 0f, 0f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void moveRight(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		i_nRadius = capRadius(i_nRadius);

		m_oRepeater.startMove(MoveCommand.MOVE_RIGHT, i_dblSpeed, i_nRadius, true);
	}

	private void executeMoveRight(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		i_nRadius = capRadius(i_nRadius);

		// TODO implement movement in two directions at the same time
	}
	
	
	// Rotate Right / Clockwise -----------------------------------------

	@Override
	public void rotateClockwise() {
		rotateClockwise(50);
//		rotateClockwise(m_dblBaseSpeed);
	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.ROTATE_RIGHT, i_dblSpeed, true);
	}

	public void executeRotateClockwise(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);

		try {
			m_oController.move(0, 0, 0, (float) i_dblSpeed / 100f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Rotate Left / Counterclockwise ------------------------------------

	@Override
	public void rotateCounterClockwise() {
//		rotateCounterClockwise(m_dblBaseSpeed);
		rotateCounterClockwise(50);
	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.ROTATE_LEFT, i_dblSpeed, true);
	}

	public void executeRotateCounterClockwise(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);

		try {
			m_oController.move(0, 0, 0, -(float) i_dblSpeed / 100f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Move Stop ------------------------------------------------------

	@Override
	public void moveStop() {
		m_oRepeater.stopMove();
		hover();
	}
	
	// Genearl Move ------------------------------------------------------ 
	
	// Note: the move is repeated until moveStop() is called!
	public void move(double i_dblLeftRightTilt, double i_dblFrontBackTilt,
			double i_dblVerticalSpeed, double i_dblAngularSpeed) {
		m_oRepeater.startMove(new GeneralMoveRunner(i_dblLeftRightTilt, i_dblFrontBackTilt, i_dblVerticalSpeed, i_dblAngularSpeed), true);
	}
	
	class GeneralMoveRunner implements Runnable {
		
		private double dblLeftRightTilt;
		private double dblFrontBackTilt;
		private double dblVerticalSpeed;
		private double dblAngularSpeed;
		
		public GeneralMoveRunner(double i_dblLeftRightTilt, double i_dblFrontBackTilt,
				double i_dblVerticalSpeed, double i_dblAngularSpeed) {
			dblAngularSpeed = i_dblAngularSpeed;
			dblFrontBackTilt = i_dblFrontBackTilt;
			dblLeftRightTilt = i_dblLeftRightTilt;
			dblVerticalSpeed = i_dblVerticalSpeed;
		}
		
		@Override
		public void run() {
			synchronized (m_oRepeater.getMutex()) {
				debug(TAG, "Move");
				executeMove(dblLeftRightTilt, dblFrontBackTilt, dblVerticalSpeed, dblAngularSpeed);
			}
		}
	}
	
	private void executeMove(double i_dblLeftRightTilt, double i_dblFrontBackTilt,
			double i_dblVerticalSpeed, double i_dblAngularSpeed) {

		try {
			m_oController.move((float) i_dblLeftRightTilt, (float) i_dblFrontBackTilt, 
					(float) i_dblVerticalSpeed, (float) i_dblAngularSpeed);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	// Execute Circle ------------------------------------------------------

	@Override
	public void executeCircle(double i_nTime, double i_nSpeed) {
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
	public void ready() {
		m_bConnected = true;
	}

	public boolean isARDrone1() {
		if (m_oController != null) {
			return m_oController.isARDrone1();
		} else {
			return false;
		}
	}

	@Override
	public void onConnect(boolean i_bConnected) {
		m_bConnected = i_bConnected;
	}

}
