package org.dobots.robots.spykee;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.security.auth.login.LoginException;

import org.dobots.robots.helpers.IMoveRepeaterListener;
import org.dobots.robots.helpers.MoveRepeater;
import org.dobots.robots.helpers.MoveRepeater.MoveCommand;
import org.dobots.robots.spykee.SpykeeController.DockState;
import org.dobots.robots.spykee.SpykeeTypes.SpykeeSound;
import org.dobots.utilities.Utils;

import robots.RobotType;
import robots.ctrl.DifferentialRobot;
import robots.gui.MessageTypes;
import android.os.Handler;
import android.os.Message;

public class Spykee extends DifferentialRobot implements IMoveRepeaterListener {
	
	private static final String TAG = "Spykee";
	
	private SpykeeController m_oController;
	
	private String m_strAddress;
	private int m_nPort;
	private String m_strLogin;
	private String m_strPassword;

	private boolean m_bConnected;

	private double m_dblBaseSpeed = 50.0;
	
	private boolean m_bVideoEnabled;
	private boolean m_bAudioEnabled;
	
	private int m_nBatteryLevel = -1;

	private int m_nInvertFactor = -1;	// normal = 1, inverted = -1
	
	private Handler m_oUiHandler;

	private ExecutorService executorSerive = Executors.newCachedThreadPool();
	
	private MoveRepeater m_oRepeater;
	
	
	public Spykee() {
		super(SpykeeTypes.AXLE_WIDTH, SpykeeTypes.MIN_VELOCITY, SpykeeTypes.MAX_VELOCITY, SpykeeTypes.MIN_RADIUS, SpykeeTypes.MAX_RADIUS);
		
		m_oController = new SpykeeController();
		m_oController.setHandler(m_oReceiveHandler);
		
		m_oRepeater = new MoveRepeater(this, 500);
	}
	
	@Override
	public RobotType getType() {
		return RobotType.RBT_SPYKEE;
	}

	@Override
	public String getAddress() {
		return m_strAddress;
	}
	
	public void setHandler(Handler i_oUiHandler) {
		m_oUiHandler = i_oUiHandler;
		m_oController.setHandler(i_oUiHandler);
	}

	private Handler m_oReceiveHandler = new Handler() {
		
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			// we handle the battery level message so that we can provide the
			// current battery level in the function getBatteryLevel since there
			// is no way to poll Spykee for the battery level
			case SpykeeMessageTypes.BATTERY_LEVEL_RECEIVED:
				m_nBatteryLevel = msg.arg1;
				break;
			}
			
			// we forward all messages to the UI handler
			m_oUiHandler.dispatchMessage(msg);
		}
	};

	@Override
	public void destroy() {
		disconnect();
		m_oController = null;
	}
	
	public void setConnection(String i_strAddress, String i_strPort, String i_strLogin, String i_strPassword) {
		m_strAddress = i_strAddress;
		m_nPort = Integer.valueOf(i_strPort);
		m_strLogin = i_strLogin;
		m_strPassword = i_strPassword;
	}

	@Override
	public void connect() {
		executorSerive.submit(new Runnable() {
			
			@Override
			public void run() {
				m_bConnected = false;
				try {
					m_oController.connect(m_strAddress, m_nPort, m_strLogin, m_strPassword);
					m_bConnected = true;
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LoginException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Utils.sendMessage(m_oUiHandler, SpykeeMessageTypes.LOGIN_ERROR, null);
					return;
				}
				
				if (m_bConnected) {
					Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTED, null);
				} else {
					Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTERROR, null);
				}
			}
		});
	}

	@Override
	public void disconnect() {
		m_bConnected = false;
		m_oController.close();
	}

	@Override
	public boolean isConnected() {
		return m_bConnected;
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		if (i_bEnable) {
			m_oController.activate();
		}
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
		
		if (Math.abs(i_dblAngle) < 2) {
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
		
		if (i_dblAngle == 0) {
			moveBackward(i_dblSpeed);
		} else {
			int nRadius = angleToRadius(i_dblAngle);
			
			moveBackward(i_dblSpeed, nRadius);
		}
	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.ROTATE_RIGHT, i_dblSpeed, true);
	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.ROTATE_LEFT, i_dblSpeed, true);
	}

	@Override
	public void moveStop() {
		m_oRepeater.stopMove();
		
		if (m_oController.getDockState() == DockState.DOCKING) {
			cancelDock();
		} else {
			m_oController.stopMotor();
		}
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
			error(TAG, "Move not available");
			return;
		}
	}

	private void executeMoveForward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);

		m_oController.moveForward(nVelocity * m_nInvertFactor);
	}

	private void executeMoveBackward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oController.moveBackward(nVelocity * m_nInvertFactor);
		
	}

	private void executeRotateClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oController.moveRight(nVelocity);
	}

	private void executeRotateCounterClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oController.moveLeft(nVelocity);
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
		debug(TAG, String.format("fwd (s=%f, r=%d)", i_dblSpeed, i_nRadius));
		
		DriveVelocityLR oVelocity = calculateVelocity(i_dblSpeed, i_nRadius);

		m_oController.moveForward(oVelocity.left * m_nInvertFactor, oVelocity.right * m_nInvertFactor);
	}

	private void executeMoveBackward(double i_dblSpeed, int i_nRadius) {
		debug(TAG, String.format("fwd (s=%f, r=%d)", i_dblSpeed, i_nRadius));
		
		DriveVelocityLR oVelocity = calculateVelocity(i_dblSpeed, i_nRadius);

		m_oController.moveBackward(oVelocity.left * m_nInvertFactor, oVelocity.right * m_nInvertFactor);
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
		// NOT AVAILABLE
	}

	@Override
	public void moveRight() {
		// NOT AVAILABLE
	}
	
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
		// TODO Auto-generated method stub
		return m_dblBaseSpeed;
	}

	public void dock() {
		m_oController.dock();
	}
	
	public void undock() {
		m_oController.undock();
	}
	
	public void cancelDock() {
		m_oController.cancelDock();
	}
	
	public void setVideoEnabled(boolean i_bEnabled) {
		if (i_bEnabled) {
			m_oController.startVideo();
		} else {
			m_oController.stopVideo();
		}
		m_bVideoEnabled = i_bEnabled;
	}
	
	public boolean isVideoEnabled() {
		return m_bVideoEnabled;
	}
	
	public void setAudioEnabled(boolean i_bEnabled) {
		if (i_bEnabled) {
			m_oController.startAudio();
		} else {
			m_oController.stopAudio();
		}
		m_bAudioEnabled = i_bEnabled;
	}
	
	public boolean isAudioEnabled() {
		return m_bAudioEnabled;
	}
	
	public void playSound(SpykeeSound i_eSound) {
		switch(i_eSound) {
		case AHAHAH:
			m_oController.playSoundAhAhAh();
			break;
		case ALARM:
			m_oController.playSoundAlarm();
			break;
		case BOMB:
			m_oController.playSoundBomb();
			break;
		case CUSTOM1:
			m_oController.playSoundCustom1();
			break;
		case CUSTOM2:
			m_oController.playSoundCustom2();
			break;
		case ENGINE:
			m_oController.playSoundEngine();
			break;
		case LASER:
			m_oController.playSoundLazer();
			break;
		case ROBOT:
			m_oController.playSoundRobot();
			break;
		}
	}

	public boolean isInverted() {
		return m_nInvertFactor == -1;
	}

	public void setInverted(boolean i_bInverted) {
		if (i_bInverted) {
			m_nInvertFactor = -1;
		} else {
			m_nInvertFactor = 1;
		}
	}

	public DockState getDockState() {
		return m_oController.getDockState();
	}
	
	public void setLed(int i_nLed, boolean i_bOn) {
		m_oController.setLed(i_nLed, i_bOn);
	}
	
	public int getBatteryLevel() {
		return m_nBatteryLevel;
	}

	public boolean isCharging() {
		return getBatteryLevel() > 100;
	}

	@Override
	public boolean toggleInvertDrive() {
		// TODO Auto-generated method stub
		return false;
	}

}
