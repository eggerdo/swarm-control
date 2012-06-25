package org.dobots.roomba;

import java.util.concurrent.TimeoutException;

import org.dobots.roomba.RoombaTypes.ERoombaBaudRates;
import org.dobots.roomba.RoombaTypes.SensorPackage;

import android.bluetooth.BluetoothSocket;

public class Roomba {
	
	RoombaController oRoombaCtrl; 
	
	byte m_byMotorState;
	byte m_byLEDState;
	byte m_byPowerColor;
	byte m_byPowerIntensity;
	
	public Roomba() {
		oRoombaCtrl = new RoombaController();
		
		// create bluetooth connection object and add it to the controller
		// oRoombaCtrl.setConnection(oConnection);
	}
	
	public void setConnection(BluetoothSocket i_oSocket) {
		oRoombaCtrl.setConnection(i_oSocket);
	}
	
	public boolean isConnected() {
		return oRoombaCtrl.isConnected();
	}
	
	/*
	 * Initialise Robot. sends the start command, then sets the roomba to the safe mode
	 */
	public void init() {
		try {
			oRoombaCtrl.start();
			Thread.sleep(20);
			oRoombaCtrl.control();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Set the Baudrate of the Roomba (default is 57600)
	 */
	public void setBaud(RoombaTypes.ERoombaBaudRates i_eBaudRate) {
		oRoombaCtrl.baud((byte)i_eBaudRate.getID());
	}
	
	/*
	 * Sets the Roomba to safe control (cliff sensors are checked
	 * and prevent the Roomba to fall down stairs etc.
	 */
	public void startSafeControl() {
		oRoombaCtrl.safe();
	}
	
	/* 
	 * Sets the Roomba to full control (cliff sensors ar NOT checked!)
	 */
	public void startFullControl() {
		oRoombaCtrl.full();
	}
	
	/*
	 * 
	 */
	public void powerOff() {
		oRoombaCtrl.power();
	}
	
	public void startSpotMode() {
		oRoombaCtrl.spot();
	}
	
	public void startCleanMode() {
		oRoombaCtrl.clean();
	}
	
	private void capSpeed(double io_dblSpeed) {
		io_dblSpeed = Math.min(io_dblSpeed, 100);
		io_dblSpeed = Math.max(io_dblSpeed, 0);
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
		return (int) i_dblSpeed * RoombaTypes.MAX_VELOCITY;
	}
	
	public void driveForward(double i_dblSpeed) {
		capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		oRoombaCtrl.drive(nVelocity, RoombaTypes.STRAIGHT);
	}
	
	public void driveForward(double i_dblSpeed, int i_nRadius) {
		capSpeed(i_dblSpeed);
		capRadius(i_nRadius);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		oRoombaCtrl.drive(nVelocity, i_nRadius);
	}
	
	public void driveBackward(double i_dblSpeed) {
		capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		oRoombaCtrl.drive(-nVelocity, RoombaTypes.STRAIGHT);
	}

	public void driveBackward(double i_dblSpeed, int i_nRadius) {
		capSpeed(i_dblSpeed);
		capRadius(i_nRadius);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		oRoombaCtrl.drive(-nVelocity, i_nRadius);
	}
	
	public void rotateClockwise(double i_dblSpeed) {
		capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		oRoombaCtrl.drive(nVelocity, RoombaTypes.CLOCKWISE);
	}
	
	public void rotateCounterClockwise(double i_dblSpeed) {
		capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		oRoombaCtrl.drive(nVelocity, RoombaTypes.COUNTER_CLOCKWISE);
	}
	
	public void stop() {
		oRoombaCtrl.drive(0, 0);
	}
	
	public void setMotors(boolean i_bOn, RoombaTypes.ERoombaMotors ... i_eMotors) {
		int nBit;
		for (RoombaTypes.ERoombaMotors eMotor : i_eMotors) {
			nBit = eMotor.getID();
			if (i_bOn) {
				SetBit(m_byMotorState, nBit);
			} else {
				ClearBit(m_byMotorState, nBit);
			}
		}
		
		oRoombaCtrl.motors(m_byMotorState);
	}
	
	private void SetBit(byte io_byValue, int i_nBit) {
		io_byValue |= (1 << i_nBit);
	}
	
	private void ClearBit(byte io_byValue, int i_nBit) {
		io_byValue &= (0 << i_nBit);
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
			return RoombaTypes.assembleSensorPackage(i_ePackage, byResult);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public void seekDocking() {
		oRoombaCtrl.dock();
	}
}
