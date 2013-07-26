package org.dobots.robots.roomba;

import java.util.concurrent.TimeoutException;

import org.dobots.robots.roomba.RoombaTypes.ERoombaModes;
import org.dobots.robots.roomba.RoombaTypes.ERoombaSensorPackages;
import org.dobots.robots.roomba.RoombaTypes.SensorPackage;
import org.dobots.utilities.Utils;

import robots.RobotType;
import robots.ctrl.BaseRobot;
import android.os.Handler;

public class Roomba extends BaseRobot {
	
	RoombaController m_oRoombaCtrl; 
	
	byte m_byMotorState;
	byte m_byLEDState;
	byte m_byPowerColor;
	byte m_byPowerIntensity;
	
	ERoombaModes m_eMode;

	private double m_dblBaseSpeed = 50.0;

	private Handler m_oUiHandler;
	
	Handler m_oReceiveHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			m_oUiHandler.dispatchMessage(msg);
		};
	};

	public Roomba() {
		m_oRoombaCtrl = new RoombaController();
		m_oRoombaCtrl.setLogListener(m_oLogListener);
		
		m_eMode = ERoombaModes.mod_Unknown;
		
		// create bluetooth connection object and add it to the controller
		// oRoombaCtrl.setConnection(oConnection);
	}
	
	public void destroy() {
		if (isConnected()) {
			disconnect();
		}
		destroyConnection();
		m_oRoombaCtrl = null;
	}
	
	public RobotType getType() {
		return RobotType.RBT_ROOMBA;
	}
	
	public String getAddress() {
		if (m_oRoombaCtrl.getConnection() != null) {
			return m_oRoombaCtrl.getConnection().getAddress();
		} else {
			return "";
		}
	}

	public void setHandler(Handler i_oHandler) {
		m_oUiHandler = i_oHandler;
	}
	
	public void setConnection(RoombaBluetooth i_oConnection) {
		i_oConnection.setReceiveHandler(m_oReceiveHandler);
		m_oRoombaCtrl.setConnection(i_oConnection);
	}
	
	public RoombaBluetooth getConnection() {
		return m_oRoombaCtrl.getConnection();
	}
	
	public void destroyConnection() {
		m_oRoombaCtrl.destroyConnection();
	}

	public boolean isConnected() {
		if (m_oRoombaCtrl != null) {
			return m_oRoombaCtrl.isConnected();
		} else {
			return false;
		}
	}

	@Override
	public void connect() {
		m_oRoombaCtrl.connect();
	}

	@Override
	public void disconnect() {
		if (isConnected()) {
			// before closing the connection we set the roomba to passive mode
			// which consumes less power
			setPassiveMode();
	
			m_oRoombaCtrl.disconnect();
		}
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		if (!isPowerOn()) {
			powerOn();
		}
		if (i_bEnable) {
			setSafeMode();
		} else {
			setPassiveMode();
		}
	}

	/*
	 * Initialise Robot. sends the start command, then sets the roomba to the safe mode
	 */
	public boolean init() {
		try {
			m_oRoombaCtrl.start();
			Thread.sleep(200);
//			oRoombaCtrl.control();
			
			if (getSensors(ERoombaSensorPackages.sensPkg_1) == null) {
				m_eMode = ERoombaModes.mod_PowerOff;
			} else {
				m_eMode = ERoombaModes.mod_Passive;
				return true;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	/*
	 * Set the Baudrate of the Roomba (default is 57600)
	 */
	public void setBaud(RoombaTypes.ERoombaBaudRates i_eBaudRate) {
		m_oRoombaCtrl.baud((byte)i_eBaudRate.getID());

		m_eMode = ERoombaModes.mod_Passive;
	}
	
	/*
	 * Sets the Roomba back to passive mode
	 */
	public void setPassiveMode() {
		try {
			m_oRoombaCtrl.start();

			// Allow 20 milliseconds between sending commands that change the SCI mode. 
			Thread.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_eMode = ERoombaModes.mod_Passive;
	}
	
	/*
	 * Sets the Roomba to safe mode (cliff sensors are checked
	 * and prevent the Roomba to fall down stairs etc.
	 */
	public void setSafeMode() {
		try {
			m_oRoombaCtrl.control();

			// Allow 20 milliseconds between sending commands that change the SCI mode. 
			Thread.sleep(20);

			m_oRoombaCtrl.safe();

			// Allow 20 milliseconds between sending commands that change the SCI mode. 
			Thread.sleep(20);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_eMode = ERoombaModes.mod_Safe;
	}
	
	/* 
	 * Sets the Roomba to full mode (cliff sensors ar NOT checked!)
	 */
	public void setFullMode() {
		try {
			m_oRoombaCtrl.control();

			// Allow 20 milliseconds between sending commands that change the SCI mode. 
			Thread.sleep(20);

			m_oRoombaCtrl.full();

			// Allow 20 milliseconds between sending commands that change the SCI mode. 
			Thread.sleep(20);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m_eMode = ERoombaModes.mod_Full;
	}
	
	/*
	 * 
	 */
	public void powerOff() {
		try {
			m_oRoombaCtrl.power();

			// Allow 20 milliseconds between sending commands that change the SCI mode. 
			Thread.sleep(20);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m_eMode = ERoombaModes.mod_PowerOff;
	}
	
	public void powerOn() {
		try {
			// wake up roomba
			if (!m_oRoombaCtrl.powerOn()) {
				return;
			}

			// Allow 20 milliseconds between sending commands that change the SCI mode. 
			Thread.sleep(20);
			
			// set up roomba for control
			m_oRoombaCtrl.start();

			// Allow 20 milliseconds between sending commands that change the SCI mode. 
			Thread.sleep(20);
			
			m_eMode = ERoombaModes.mod_Passive;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isPowerOn() {
		return m_eMode != ERoombaModes.mod_PowerOff;
	}
	
	public void startSpotMode() {
		try {
			m_oRoombaCtrl.spot();

			// Allow 20 milliseconds between sending commands that change the SCI mode. 
			Thread.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m_eMode = ERoombaModes.mod_Passive;
	}
	
	public void startCleanMode() {
		try {
			m_oRoombaCtrl.clean();
			
			// Allow 20 milliseconds between sending commands that change the SCI mode. 
			Thread.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m_eMode = ERoombaModes.mod_Passive;
	}
	
	private void capRadius(int io_nRadius) {
		io_nRadius = Math.min(io_nRadius, RoombaTypes.MAX_RADIUS);
		io_nRadius = Math.max(io_nRadius, -RoombaTypes.MAX_RADIUS);
		
		// exclude the special cases
		if (io_nRadius == 0) {
			io_nRadius = RoombaTypes.STRAIGHT;
		}
		
		if (io_nRadius == -1) {
			io_nRadius = -2;
		}
		
		if (io_nRadius == 1) {
			io_nRadius = 2;
		}
	}
	
	private int calculateVelocity(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		return (int) Math.round(i_dblSpeed / 100.0 * RoombaTypes.MAX_VELOCITY);
	}
	
	private int angleToRadius(double i_dblAngle) {
		double dblAngle = (90 - Math.abs(i_dblAngle));
		int nRadius = (int) (Math.signum(i_dblAngle) * (RoombaTypes.MAX_RADIUS / 90.0 * dblAngle));
		
		return nRadius;
	}
	
	public void moveForward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oRoombaCtrl.drive(nVelocity, RoombaTypes.STRAIGHT);
	}
	
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		capRadius(i_nRadius);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oRoombaCtrl.drive(nVelocity, i_nRadius);
	}
	
	static final int STRAIGHT_THRESHOLD = 10;

	@Override
	public void moveForward(double i_dblSpeed, double i_dblAngle) {
		
		if (90 - Math.abs(i_dblAngle) < STRAIGHT_THRESHOLD) {
			moveForward(i_dblSpeed);
		} else {
			int nRadius = angleToRadius(i_dblAngle);
			moveForward(i_dblSpeed, nRadius);
		}
	}

	public void moveBackward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oRoombaCtrl.drive(-nVelocity, RoombaTypes.STRAIGHT);
	}

	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		capRadius(i_nRadius);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oRoombaCtrl.drive(-nVelocity, i_nRadius);
	}

	@Override
	public void moveBackward(double i_dblSpeed, double i_dblAngle) {

		if (90 - Math.abs(i_dblAngle) < STRAIGHT_THRESHOLD) {
			moveBackward(i_dblSpeed);
		} else {
			int nRadius = angleToRadius(i_dblAngle);
			moveBackward(i_dblSpeed, nRadius);
		}
	}

	public void rotateClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oRoombaCtrl.drive(nVelocity, RoombaTypes.CLOCKWISE);
	}
	
	public void rotateCounterClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oRoombaCtrl.drive(nVelocity, RoombaTypes.COUNTER_CLOCKWISE);
	}
	
	public void moveStop() {
		m_oRoombaCtrl.drive(0, 0);
	}
	
	public void setVacuum(boolean i_bOn) {
		setMotors(i_bOn, RoombaTypes.ERoombaMotors.motor_Vacuum);
	}

	public void setMainBrush(boolean i_bOn) {
		setMotors(i_bOn, RoombaTypes.ERoombaMotors.motor_MainBrush);
	}

	public void setSideBrush(boolean i_bOn) {
		setMotors(i_bOn, RoombaTypes.ERoombaMotors.motor_SideBrush);
	}
	
	private void setMotors(boolean i_bOn, RoombaTypes.ERoombaMotors ... i_eMotors) {
		int nBit;
		for (RoombaTypes.ERoombaMotors eMotor : i_eMotors) {
			nBit = eMotor.getID();
			if (i_bOn) {
				m_byMotorState = SetBit(m_byMotorState, nBit);
			} else {
				m_byMotorState = ClearBit(m_byMotorState, nBit);
			}
		}
		
		m_oRoombaCtrl.motors(m_byMotorState);
	}
	
	private byte SetBit(byte io_byValue, int i_nBit) {
		return io_byValue |= (1 << i_nBit);
	}
	
	private byte ClearBit(byte io_byValue, int i_nBit) {
		return io_byValue &= ~(1 << i_nBit);
	}
	
	public void setLEDs(boolean i_bOn, RoombaTypes.ERoombaOnOffLEDs ... i_eLEDs) {
		int nBit;
		for (RoombaTypes.ERoombaOnOffLEDs eLED : i_eLEDs) {
			nBit = eLED.getID();
			if (i_bOn) {
				SetBit(m_byLEDState, nBit);
			} else {
				ClearBit(m_byLEDState, nBit);
			}
		}
		
		m_oRoombaCtrl.leds(m_byLEDState, m_byPowerColor, m_byPowerIntensity);
	}
	
	public void setStatusLED(boolean i_bOn, RoombaTypes.ERoombaStatusLEDColours i_eLEDColour) {
		if (i_bOn) {
			switch (i_eLEDColour) {
				case ledCol_Green:
					SetBit(m_byLEDState, RoombaTypes.STATUS_LED_HIGH_BIT);
					ClearBit(m_byLEDState, RoombaTypes.STATUS_LED_LOW_BIT);
					break;
				case ledCol_Red:
					ClearBit(m_byLEDState, RoombaTypes.STATUS_LED_HIGH_BIT);
					SetBit(m_byLEDState, RoombaTypes.STATUS_LED_LOW_BIT);
					break;
				case ledCol_Amber:
					SetBit(m_byLEDState, RoombaTypes.STATUS_LED_HIGH_BIT);
					SetBit(m_byLEDState, RoombaTypes.STATUS_LED_LOW_BIT);
					break;
			}
		} else {
			ClearBit(m_byLEDState, RoombaTypes.STATUS_LED_LOW_BIT);
			ClearBit(m_byLEDState, RoombaTypes.STATUS_LED_HIGH_BIT);
		}
		
		m_oRoombaCtrl.leds(m_byLEDState, m_byPowerColor, m_byPowerIntensity);
	}
	
	public void setPowerLED(int i_nColor, int i_nIntensity) {
		m_byPowerColor = (byte)i_nColor;
		m_byPowerIntensity = (byte)i_nIntensity;
		
		m_oRoombaCtrl.leds(m_byLEDState, m_byPowerColor, m_byPowerIntensity);
	}

	public SensorPackage getSensors(RoombaTypes.ERoombaSensorPackages i_ePackage) {
		byte nPackage = (byte)i_ePackage.getID();
		int nResultLength = 0;
		
		byte[] byResult;
		try {
			byResult = m_oRoombaCtrl.sensors(nPackage, nResultLength);
			if (byResult != null) {
				return RoombaTypes.assembleSensorPackage(i_ePackage, byResult);
			} else
				return null;
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public void seekDocking() {
		try {
			m_oRoombaCtrl.dock();

			// Allow 20 milliseconds between sending commands that change the SCI mode. 
			Thread.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_eMode = ERoombaModes.mod_Passive;
	}

	Handler executor = new Handler();
	@Override
	public void executeCircle(final double i_dblTime, final double i_dblSpeed) {
		executor.post(new Runnable() {
			
			@Override
			public void run() {
				rotateClockwise(i_dblSpeed);
			}
		});
		executor.post(new Runnable() {
			
			@Override
			public void run() {
				Utils.waitSomeTime((int)i_dblTime);
			}
		});
		executor.post(new Runnable() {
			
			@Override
			public void run() {
				moveStop();
			}
		});
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
	public void setBaseSpeed(double i_dblSpeed) {
		m_dblBaseSpeed = i_dblSpeed;
	}

	@Override
	public double getBaseSped() {
		return m_dblBaseSpeed;
	}

	@Override
	public void moveLeft() {
		// not available
	}

	@Override
	public void moveRight() {
		// not available
	}

	@Override
	public boolean toggleInvertDrive() {
		// TODO Auto-generated method stub
		return false;
	}

}
