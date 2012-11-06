package org.dobots.robots.parrot;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.dobots.robots.MessageTypes;
import org.dobots.robots.RobotDevice;
import org.dobots.robots.parrot.ParrotTypes.ParrotMove;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.utility.Utils;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.format.Time;
import android.util.Log;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.ARDrone.VideoChannel;
import com.codeminders.ardrone.DroneStatusChangeListener;
import com.codeminders.ardrone.DroneVideoListener;
import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavDataListener;

public class Parrot implements RobotDevice, DroneStatusChangeListener {

	private static String TAG = "Parrot";

	private Handler mHandler = new Handler(Looper.getMainLooper());

	private Handler m_oRepeatMoveHandler = new Handler();
	private boolean m_bRepeat = false;

	private ARDrone m_oController;

	private Handler m_oUiHandler;

	private boolean m_bConnected = false;

	private int m_nWaitID;
	private Object receiveEvent = this;
	private boolean m_bMessageReceived = false;

	private double m_dblBaseSpeed = 40.0;

	private VideoChannel m_eVideoChannel = ARDrone.VideoChannel.HORIZONTAL_ONLY;

	public Parrot() {
		// m_oController = new
		// ARDrone(InetAddress.getByAddress(ParrotTypes.ARDRONE_IP), 10000,
		// 60000);

		// m_oReceiver = new ARDroneReceiver();
		// m_oReceiver.start();

		// m_oDroneStarter = new DroneStarter();
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
		try {
			m_oController.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setConnection() {
		// TODO Auto-generated method stub
	}

	private class DroneStarter extends AsyncTask<ARDrone, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(ARDrone... drones) {
			try {
				m_oController = new ARDrone(
						InetAddress.getByName(ParrotTypes.PARROT_IP),
						10000, 60000);
				m_oController.connect();
				m_oController.clearEmergencySignal();
				m_oController.waitForReady(ParrotTypes.CONNECTION_TIMEOUT);
				m_oController.playLED(1, 10, 4);
				m_oController.selectVideoChannel(ARDrone.VideoChannel.HORIZONTAL_ONLY);
				m_oController.setCombinedYawMode(true);
				return true;
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
			return false;
		}

		protected void onPostExecute(Boolean success) {
			if (success.booleanValue()) {
				m_bConnected = true;
				Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTED, null);
			} else {
				m_bConnected = false;
				Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTERROR, null);
			}
		}
	}

	@Override
	public void connect() {
		(new DroneStarter()).execute(m_oController);
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
	public void disconnect() {
		try {
//			if (m_oController != null) {
				m_oController.disconnect();
//			}
			m_oController = null;
			m_bConnected = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setVideoChannel(VideoChannel i_oChannel) {
		try {
			m_oController.selectVideoChannel(i_oChannel);
			m_eVideoChannel = i_oChannel;
		} catch (IOException e) {
			// TODO Auto-generated catch block
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

	private void startMove(ParrotMove i_eMove, double i_dblSpeed,
			boolean i_bRepeat) {
		Runnable runner;
		switch (i_eMove) {
		case MOVE_BWD:
			runner = new MoveBackwardsRunnable(i_dblSpeed);
			break;
		case MOVE_FWD:
			runner = new MoveForwardsRunnable(i_dblSpeed);
			break;
		case MOVE_DOWN:
			runner = new DecreaseAltitudeRunnable(i_dblSpeed);
			break;
		case MOVE_UP:
			runner = new IncreaseAltitudeRunnable(i_dblSpeed);
			break;
		case MOVE_LEFT:
			runner = new MoveLeftRunnable(i_dblSpeed);
			break;
		case MOVE_RIGHT:
			runner = new MoveRightRunnable(i_dblSpeed);
			break;
		case ROTATE_LEFT:
			runner = new RotateCounterClockwiseRunnable(i_dblSpeed);
			break;
		case ROTATE_RIGHT:
			runner = new RotateClockwiseRunnable(i_dblSpeed);
			break;
		default:
			return;
		}
		m_bRepeat = i_bRepeat;
		m_oRepeatMoveHandler.post(runner);
	}

	private void stopRepeatedMove() {
		Log.d(TAG, "done");
		m_bRepeat = false;
		hover();
	}

	public void takeOff() {
		try {
			m_oController.clearEmergencySignal();
			m_oController.trim();
			m_oController.takeOff();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void land() {
		try {
			m_oController.land();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private AltitudeControl ctrl;

	public void stopAltitudeControl() {
		ctrl.bRun = false;
		hover();
	}

	public void setAltitude(double i_dblSetpoint) {
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
							Log.d(TAG, "Setpoint Reached");
							bSetpointReached = true;
							hover();
						}
					} else {
						count = 0;

						dblSpeed = pidControl(dblError);
						Log.d(TAG, String.format(
								"Altitude: %f, Error:%f, Speed: %f",
								oNavData.getAltitude(), dblError, dblSpeed));
						if ((dblSpeed > 0) && (dblSpeed <= 100)) {
							doIncreaseAltitude(dblSpeed);
						} else if ((dblSpeed < 0) && (dblSpeed >= -100)) {
							doDecreaseAltitude(-dblSpeed);
						} else {
							Log.d(TAG, "Fatal Error");
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
				Log.e(TAG, "Time Interval is 0!");
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

	// Increase Altitude

	public void increaseAltitude(double i_dblSpeed) {
		startMove(ParrotMove.MOVE_UP, i_dblSpeed, true);
	}

	class IncreaseAltitudeRunnable implements Runnable {

		private double dblSpeed;

		public IncreaseAltitudeRunnable(double i_dblSpeed) {
			dblSpeed = i_dblSpeed;
		}

		@Override
		public void run() {
			Log.d(TAG, "increase");
			doIncreaseAltitude(dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(new IncreaseAltitudeRunnable(
						dblSpeed), 100);
			}
		}
	}

	private void doIncreaseAltitude(double i_dblSpeed) {
		try {
			m_oController.move(0f, 0f, (float) i_dblSpeed / 100f, 0f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Decrease Altitude

	public void decreaseAltitude(double i_dblSpeed) {
		startMove(ParrotMove.MOVE_DOWN, i_dblSpeed, true);
	}

	class DecreaseAltitudeRunnable implements Runnable {

		private double dblSpeed;

		public DecreaseAltitudeRunnable(double i_dblSpeed) {
			dblSpeed = i_dblSpeed;
		}

		@Override
		public void run() {
			Log.d(TAG, "decrease");
			doDecreaseAltitude(dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(new DecreaseAltitudeRunnable(
						dblSpeed), 100);
			}
		}
	};

	public void doDecreaseAltitude(double i_dblSpeed) {
		try {
			m_oController.move(0f, 0f, -(float) i_dblSpeed / 100f, 0f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Hover

	public void hover() {
		try {
			m_oController.hover();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// m_oController.control(i_bEnable);
	}

	private double capSpeed(double io_dblSpeed) {
		// if a negative value was provided as speed
		// use the absolute value of it.
		io_dblSpeed = Math.abs(io_dblSpeed);
		io_dblSpeed = Math.min(io_dblSpeed, 100);
		io_dblSpeed = Math.max(io_dblSpeed, 0);

		return io_dblSpeed;
	}

	private int capRadius(int io_nRadius) {
		// io_nRadius = Math.min(io_nRadius, DottyTypes.MAX_RADIUS);
		// io_nRadius = Math.max(io_nRadius, -DottyTypes.MAX_RADIUS);

		return io_nRadius;
	}

	// Move Forward

	@Override
	public void moveForward(double i_dblSpeed) {
		startMove(ParrotMove.MOVE_FWD, i_dblSpeed, true);
	}

	class MoveForwardsRunnable implements Runnable {

		private double dblSpeed;

		public MoveForwardsRunnable(double i_dblSpeed) {
			dblSpeed = i_dblSpeed;
		}

		@Override
		public void run() {
			Log.d(TAG, "move fwd");
			doMoveForward(dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(new MoveForwardsRunnable(
						dblSpeed), 100);
			}
		}
	}

	public void doMoveForward(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		try {
			m_oController.move(0f, -(float) i_dblSpeed / 100f, 0f, 0f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		i_nRadius = capRadius(i_nRadius);
	}

	@Override
	public void moveForward(double i_dblSpeed, double i_dblAngle) {
		// TODO Auto-generated method stub
	}
	
	// Move Backward

	@Override
	public void moveBackward(double i_dblSpeed) {
		startMove(ParrotMove.MOVE_BWD, i_dblSpeed, true);
	}

	class MoveBackwardsRunnable implements Runnable {

		private double dblSpeed;

		public MoveBackwardsRunnable(double i_dblSpeed) {
			dblSpeed = i_dblSpeed;
		}

		@Override
		public void run() {
			Log.d(TAG, "move bwd");
			doMoveBackward(dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(new MoveBackwardsRunnable(
						dblSpeed), 100);
			}
		}
	}

	public void doMoveBackward(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		try {
			m_oController.move(0f, (float) i_dblSpeed / 100f, 0f, 0f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		i_nRadius = capRadius(i_nRadius);

	}

	@Override
	public void moveBackward(double i_dblSpeed, double i_dblAngle) {
		
	}


	// Move Left

	public void moveLeft(double i_dblSpeed) {
		startMove(ParrotMove.MOVE_LEFT, i_dblSpeed, true);
	}

	class MoveLeftRunnable implements Runnable {

		private double dblSpeed;

		public MoveLeftRunnable(double i_dblSpeed) {
			dblSpeed = i_dblSpeed;
		}

		@Override
		public void run() {
			Log.d(TAG, "move left");
			doMoveLeft(dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(
						new MoveLeftRunnable(dblSpeed), 100);
			}
		}
	}

	public void doMoveLeft(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		try {
			m_oController.move(-(float) i_dblSpeed / 100f, 0f, 0f, 0f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Move Right

	public void moveRight(double i_dblSpeed) {
		startMove(ParrotMove.MOVE_RIGHT, i_dblSpeed, true);
	}

	class MoveRightRunnable implements Runnable {

		private double dblSpeed;

		public MoveRightRunnable(double i_dblSpeed) {
			dblSpeed = i_dblSpeed;
		}

		@Override
		public void run() {
			Log.d(TAG, "move right");
			doMoveRight(dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(
						new MoveRightRunnable(dblSpeed), 100);
			}
		}
	}

	public void doMoveRight(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		try {
			m_oController.move((float) i_dblSpeed / 100f, 0f, 0f, 0f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Rotate Right / Clockwise

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		startMove(ParrotMove.ROTATE_RIGHT, i_dblSpeed, true);
	}

	class RotateClockwiseRunnable implements Runnable {

		private double dblSpeed;

		public RotateClockwiseRunnable(double i_dblSpeed) {
			dblSpeed = i_dblSpeed;
		}

		@Override
		public void run() {
			Log.d(TAG, "rotate right");
			doRrotateClockwise(dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(new RotateClockwiseRunnable(
						dblSpeed), 100);
			}
		}
	}

	public void doRrotateClockwise(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);

		try {
			m_oController.move(0, 0, 0, (float) i_dblSpeed / 100f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Rotate Left / Counterclockwise

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		startMove(ParrotMove.ROTATE_LEFT, i_dblSpeed, true);
	}

	class RotateCounterClockwiseRunnable implements Runnable {

		private double dblSpeed;

		public RotateCounterClockwiseRunnable(double i_dblSpeed) {
			dblSpeed = i_dblSpeed;
		}

		@Override
		public void run() {
			Log.d(TAG, "rotate left");
			doRotateCounterClockwise(dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(
						new RotateCounterClockwiseRunnable(dblSpeed), 100);
			}
		}
	}

	public void doRotateCounterClockwise(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);

		try {
			m_oController.move(0, 0, 0, -(float) i_dblSpeed / 100f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Move Stop

	@Override
	public void moveStop() {
		stopRepeatedMove();
	}

	// Execute Circle

	@Override
	public void executeCircle(double i_nTime, double i_nSpeed) {
		// TODO Auto-generated method stub
	}

	@Override
	public void moveForward() {
		moveForward(m_dblBaseSpeed);
	}

	@Override
	public void moveBackward() {
		moveBackward(m_dblBaseSpeed);
	}

	public void moveLeft() {
		moveLeft(m_dblBaseSpeed);
	}

	public void moveRight() {
		moveRight(m_dblBaseSpeed);
	}

	@Override
	public void rotateCounterClockwise() {
		rotateCounterClockwise(m_dblBaseSpeed);
	}

	@Override
	public void rotateClockwise() {
		rotateClockwise(m_dblBaseSpeed);
	}

	public void increaseAltitude() {
		increaseAltitude(m_dblBaseSpeed);
	}

	public void decreaseAltitude() {
		decreaseAltitude(m_dblBaseSpeed);
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
		return m_oController.isARDrone1();
	}

}
