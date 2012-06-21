package org.dobots.roomba;

import org.dobots.roomba.RoombaTypes.ERoombaBaudRates;

import android.text.TextUtils.TruncateAt;

public class Roomba {
	
	RoombaController oRoombaCtrl; 
	
	byte m_byMotorState;
	byte m_byLEDState;
	
	public Roomba() {
		oRoombaCtrl = new RoombaController();
		
		// create bluetooth connection object and add it to the controller
		// oRoombaCtrl.setConnection(oConnection);
	}
	
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
	
	public void setBaud(ERoombaBaudRates i_eBaudRate) {
		oRoombaCtrl.baud((byte)i_eBaudRate.getID());
	}
	
	public void startSafeControl() {
		oRoombaCtrl.safe();
	}
	
	public void startFullControl() {
		oRoombaCtrl.full();
	}
	
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
		
		oRoombaCtrl.leds(m_byLEDState, m_byPowerColor, m_byPowerIntensity)
	}

}
