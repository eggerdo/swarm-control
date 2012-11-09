package org.dobots.robots.dotty;

import java.io.IOException;

import org.dobots.robots.BaseBluetooth;
import org.dobots.robots.MessageTypes;
import org.dobots.robots.RobotDevice;
import org.dobots.robots.dotty.DottyTypes.DataPackage;
import org.dobots.robots.dotty.DottyTypes.SensorData;
import org.dobots.robots.nxt.NXTTypes;
import org.dobots.robots.nxt.msg.MsgTypes.RawDataMsg;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.swarmcontrol.robots.dotty.DottyBluetooth;
import org.dobots.utility.Utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class Dotty implements RobotDevice {

	private DottyController m_oController;

	private Handler m_oUiHandler;

	private boolean connected = false;

	private int m_nWaitID;
	private Object receiveEvent = this;
	private boolean m_bMessageReceived = false;

	private DottyReceiver m_oReceiver;

	private double m_dblAxleWidth = 1.0;
	
	private double m_dblBaseSpeed = 50.0;
	
	private class DottyReceiver extends Thread {
		
		private Handler m_oHandler;
		
		public Handler getHandler() {
			return m_oHandler;
		}

		@Override
		public void run() {
		
			Looper.prepare();
			m_oHandler = new Handler() {
				
				@Override
				public void handleMessage(Message msg) {

					int messageID = msg.what;
					
					if (messageID == m_nWaitID) {
						m_bMessageReceived = true;
						synchronized(receiveEvent) {
							receiveEvent.notify();
						}
					}
					
					switch (messageID) {
					case MessageTypes.STATE_CONNECTED:
						connected = true;
						break;

					case MessageTypes.STATE_CONNECTERROR_PAIRING:
						m_oController.destroyConnection();
						break;

					case MessageTypes.STATE_RECEIVEERROR:
					case MessageTypes.STATE_SENDERROR:
						connected = false;
						break;
					
					case DottyTypes.SENSOR_DATA:
						byte[] sensorMessage = ((RawDataMsg)msg.obj).rgbyRawData;
						DataPackage oData = DottyTypes.assembleDataPackage(sensorMessage);
						msg.obj = DottyTypes.assembleSensorData(oData.rgnSensor);
					}

					// forwards new message with same data to the ui handler
					Utils.sendMessage(m_oUiHandler, messageID, msg.obj);
//					m_oUiHandler.sendMessage(msg);
				}
				
			};
			Looper.loop();
		}
		
	}
	
	public Dotty() {
		m_oReceiver = new DottyReceiver();
		m_oReceiver.start();
		
		m_oController = new DottyController();
	}

	public void setHandler(Handler i_oHandler) {
		m_oUiHandler = i_oHandler;
	}
		
	@Override
	public RobotType getType() {
		// TODO Auto-generated method stub
		return RobotType.RBT_DOTTY;
	}

	@Override
	public String getAddress() {
		if (m_oController.getConnection() != null) {
			return m_oController.getConnection().getAddress();
		} else {
			return "";
		}
	}

	@Override
	public void destroy() {
		m_oController.destroyConnection();
	}

	@Override
	public void setConnection() {
		// TODO Auto-generated method stub
	}
	
	public void setConnection(DottyBluetooth i_oConnection) {
		i_oConnection.setReceiveHandler(m_oReceiver.getHandler());
		m_oController.setConnection(i_oConnection);
	}
	
	public DottyBluetooth getConnection() {
		return m_oController.getConnection();
	}

	@Override
	public void connect() {
		m_oController.connect();
	}

	@Override
	public void disconnect() {
		m_oController.disconnect();
	}

	@Override
	public boolean isConnected() {
		return m_oController.isConnected();
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		m_oController.control(i_bEnable);
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
		io_nRadius = Math.min(io_nRadius, DottyTypes.MAX_RADIUS);
		io_nRadius = Math.max(io_nRadius, -DottyTypes.MAX_RADIUS);

		return io_nRadius;
	}
	
	private int calculateVelocity(double i_dblSpeed) {
		return (int) Math.round(i_dblSpeed / 100.0 * DottyTypes.MAX_VELOCITY);
	}
	
	private void calculateVelocity(double i_dblSpeed, int i_nRadius, int[] io_rgnVelocity) {
		int nBaseVelocity = calculateVelocity(i_dblSpeed);
		int nVelocity1, nVelocity2;
		
		// a high radius value received means that the robot should make a small/short turn
		// a low radius value received means that the robot should make a big/long turn
		// that means that the actual radius from which we calculate the velocities has to be
		// the opposite of the radius we receive
		int nCorrectedRadius = DottyTypes.MAX_RADIUS - Math.abs(i_nRadius);
		
		if (i_nRadius == 0) {
			io_rgnVelocity[0] = nBaseVelocity;
			io_rgnVelocity[1] = nBaseVelocity;
		} else {
			nVelocity1 = (int) Math.round(nBaseVelocity * (nCorrectedRadius + m_dblAxleWidth) / (nCorrectedRadius + m_dblAxleWidth / 2.0));
			nVelocity2 = (int) Math.round(nBaseVelocity * nCorrectedRadius / (nCorrectedRadius + m_dblAxleWidth / 2.0));
			
			// we have to make sure that the higher velocity of the two wheels (velocity1) cannot be more than the MAX_VELOCITY
			// if it is more, we need to scale both values down so that the higher velocity equals MAX_VELOCITY. if the lower
			// velocity would fall below 0 we set it to 0
			int nOffset = nVelocity1 - DottyTypes.MAX_VELOCITY;
			if (nOffset > 0) {
				nVelocity1 = 100;
				nVelocity2 = Math.max(nVelocity2 - nOffset, 0);
			}
			// for the same reason we have to make sure that the lower velocity of the two wheels cannot be less than 0. if the
			// higher velocity would go above 100 we set it to 100
			nOffset = -nVelocity2;
			if (nOffset > 0) {
				nVelocity1 = Math.min(nVelocity1 + nOffset, 100);
				nVelocity2 = 0;
			}
			
			if (i_nRadius > 0) {
				io_rgnVelocity[0] = nVelocity2;
				io_rgnVelocity[1] = nVelocity1;
			} else if (i_nRadius < 0) {
				io_rgnVelocity[0] = nVelocity1;
				io_rgnVelocity[1] = nVelocity2;
			}
		}
	}

	
	
	@Override
	public void moveForward(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oController.drive(nVelocity, nVelocity);
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		i_nRadius = capRadius(i_nRadius);

		int velocity[] = {0, 0};
		calculateVelocity(i_dblSpeed, i_nRadius, velocity);

		m_oController.drive(velocity[0], velocity[1]);
	}
	
	public void moveForward(double i_dblSpeed, double i_dblAngle) {
		double dblAngle = i_dblAngle - 90.0;
		int nRadius = (int)(DottyTypes.MAX_RADIUS / 90.0 * dblAngle);
		
		moveForward(i_dblSpeed, nRadius);
	}

	@Override
	public void moveBackward(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oController.drive(-nVelocity, -nVelocity);
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		i_nRadius = capRadius(i_nRadius);

		int velocity[] = {0, 0};
		calculateVelocity(i_dblSpeed, i_nRadius, velocity);

		m_oController.drive(-velocity[0], -velocity[1]);
	}
	
	public void moveBackward(double i_dblSpeed, double i_dblAngle) {
		double dblAngle = i_dblAngle - 90.0;
		int nRadius = (int)(DottyTypes.MAX_RADIUS / 90.0 * dblAngle);
		
		moveBackward(i_dblSpeed, nRadius);
	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);

		m_oController.drive(nVelocity, -nVelocity);
	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);

		m_oController.drive(-nVelocity, nVelocity);
	}

	@Override
	public void moveStop() {
		m_oController.driveStop();
	}
	
	public void requestSensorData() {
		m_oController.requestSensorData();
	}
	
	public void startStreaming(int i_nInterval) {
		m_oController.startStreaming(i_nInterval);
	}
	
	public void stopStreaming() {
		m_oController.stopStreaming();
	}

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
		// TODO Auto-generated method stub
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

}
