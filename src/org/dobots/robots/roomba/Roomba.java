package org.dobots.robots.roomba;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.dobots.robots.RobotDevice;
import org.dobots.robots.roomba.RoombaTypes.ERoombaModes;
import org.dobots.robots.roomba.RoombaTypes.ERoombaSensorPackages;
import org.dobots.robots.roomba.RoombaTypes.SensorPackage;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.utility.Utils;

import android.os.Handler;

public class Roomba implements RobotDevice {
	
	RoombaController oRoombaCtrl; 
	
	byte m_byMotorState;
	byte m_byLEDState;
	byte m_byPowerColor;
	byte m_byPowerIntensity;
	
	ERoombaModes m_eMode;

	private double m_dblBaseSpeed = 50.0;
	
	public Roomba() {
		oRoombaCtrl = new RoombaController();
		
		m_eMode = ERoombaModes.mod_Unknown;
		
		// create bluetooth connection object and add it to the controller
		// oRoombaCtrl.setConnection(oConnection);
	}
	
	public void destroy() {
		if (isConnected()) {
			disconnect();
		}
		destroyConnection();
		oRoombaCtrl = null;
	}
	
	public RobotType getType() {
		return RobotType.RBT_ROOMBA;
	}
	
	public String getAddress() {
		if (oRoombaCtrl.getConnection() != null) {
			return oRoombaCtrl.getConnection().getAddress();
		} else {
			return "";
		}
	}

	@Override
	public void setConnection() {
		
	}

	public void setConnection(RoombaBluetooth i_oConnection) {
		oRoombaCtrl.setConnection(i_oConnection);
	}
	
	public RoombaBluetooth getConnection() {
		return oRoombaCtrl.getConnection();
	}
	
	public void destroyConnection() {
		oRoombaCtrl.destroyConnection();
	}

	public boolean isConnected() {
		return oRoombaCtrl.isConnected();
	}

	@Override
	public void connect() {
		oRoombaCtrl.connect();
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

		// before closing the connection we set the roomba to passive mode
		// which consumes less power
		setPassiveMode();

		oRoombaCtrl.disconnect();
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
			oRoombaCtrl.start();
			Thread.sleep(20);
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
		oRoombaCtrl.baud((byte)i_eBaudRate.getID());

		m_eMode = ERoombaModes.mod_Passive;
	}
	
	/*
	 * Sets the Roomba back to passive mode
	 */
	public void setPassiveMode() {
		try {
			oRoombaCtrl.start();

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
			oRoombaCtrl.control();

			// Allow 20 milliseconds between sending commands that change the SCI mode. 
			Thread.sleep(20);

			oRoombaCtrl.safe();

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
			oRoombaCtrl.control();

			// Allow 20 milliseconds between sending commands that change the SCI mode. 
			Thread.sleep(20);

			oRoombaCtrl.full();

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
			oRoombaCtrl.power();

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
			if (!oRoombaCtrl.powerOn()) {
				return;
			}

			// Allow 20 milliseconds between sending commands that change the SCI mode. 
			Thread.sleep(20);
			
			// set up roomba for control
			oRoombaCtrl.start();

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
			oRoombaCtrl.spot();

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
			oRoombaCtrl.clean();
			
			// Allow 20 milliseconds between sending commands that change the SCI mode. 
			Thread.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m_eMode = ERoombaModes.mod_Passive;
	}
	
	private double capSpeed(double io_dblSpeed) {
		// if a negative value was provided as speed
		// use the absolute value of it.
		io_dblSpeed = Math.abs(io_dblSpeed);
		io_dblSpeed = Math.min(io_dblSpeed, 100);
		io_dblSpeed = Math.max(io_dblSpeed, 0);
		
		return io_dblSpeed;
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
		return (int) Math.round(i_dblSpeed / 100.0 * RoombaTypes.MAX_VELOCITY);
	}
	
	public void moveForward(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		oRoombaCtrl.drive(nVelocity, RoombaTypes.STRAIGHT);
	}
	
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		capRadius(i_nRadius);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		oRoombaCtrl.drive(nVelocity, i_nRadius);
	}
	
	public void moveBackward(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		oRoombaCtrl.drive(-nVelocity, RoombaTypes.STRAIGHT);
	}

	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		capRadius(i_nRadius);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		oRoombaCtrl.drive(-nVelocity, i_nRadius);
	}
	
	public void rotateClockwise(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		oRoombaCtrl.drive(nVelocity, RoombaTypes.CLOCKWISE);
	}
	
	public void rotateCounterClockwise(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		oRoombaCtrl.drive(nVelocity, RoombaTypes.COUNTER_CLOCKWISE);
	}
	
	public void moveStop() {
		oRoombaCtrl.drive(0, 0);
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
		
		oRoombaCtrl.motors(m_byMotorState);
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
		
		oRoombaCtrl.leds(m_byLEDState, m_byPowerColor, m_byPowerIntensity);
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
		
		oRoombaCtrl.leds(m_byLEDState, m_byPowerColor, m_byPowerIntensity);
	}
	
	public void setPowerLED(int i_nColor, int i_nIntensity) {
		m_byPowerColor = (byte)i_nColor;
		m_byPowerIntensity = (byte)i_nIntensity;
		
		oRoombaCtrl.leds(m_byLEDState, m_byPowerColor, m_byPowerIntensity);
	}

	public SensorPackage getSensors(RoombaTypes.ERoombaSensorPackages i_ePackage) {
		byte nPackage = (byte)i_ePackage.getID();
		int nResultLength = 0;
		
		byte[] byResult;
		try {
			byResult = oRoombaCtrl.sensors(nPackage, nResultLength);
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
			oRoombaCtrl.dock();

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
	public void moveBackward(double i_dblSpeed, double i_dblAngle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveForward(double i_dblSpeed, double i_dblAngle) {
		// TODO Auto-generated method stub
		
	}

}
