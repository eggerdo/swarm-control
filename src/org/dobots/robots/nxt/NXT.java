package org.dobots.robots.nxt;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.dobots.robots.DifferentialRobot;
import org.dobots.robots.MessageTypes;
import org.dobots.robots.msg.MsgTypes;
import org.dobots.robots.msg.MsgTypes.MotorDataRequestMsg;
import org.dobots.robots.msg.MsgTypes.MotorSpeedMsg;
import org.dobots.robots.msg.MsgTypes.RawDataMsg;
import org.dobots.robots.msg.MsgTypes.ResetMotorPositionMsg;
import org.dobots.robots.msg.MsgTypes.SensorDataRequestMsg;
import org.dobots.robots.msg.MsgTypes.SensorTypeMsg;
import org.dobots.robots.nxt.NXTTypes.ENXTMotorID;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorID;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorType;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.swarmcontrol.robots.nxt.NXTBluetooth;
import org.dobots.utility.Utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class NXT extends DifferentialRobot {

	private static String TAG = "NXT";

	private NXTBluetooth m_oConnection;

	private double m_dblBaseSpeed = 50.0;
	
	int motorLeft;
	int motorRight;

	private Handler m_oUiHandler;

	private boolean connected = false;

	private NXTReceiver m_oReceiver;
	private NXTSender m_oSender;

	private int m_nWaitID;
	private Object receiveEvent = this;
	private boolean m_bMessageReceived = false;

	private Timer m_oKeepAliveTimer;
	
	private int m_nInvertFactor = -1;	// normal = 1, inverted = -1
	
	private double m_dblAxleWidth = NXTTypes.AXLE_WIDTH;
	
	private class NXTReceiver extends Thread {
		
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
						getFirmwareVersion();
						break;

					case MessageTypes.STATE_CONNECTERROR_PAIRING:
						m_oConnection = null;
						break;

					case MessageTypes.STATE_RECEIVEERROR:
					case MessageTypes.STATE_SENDERROR:
						connected = false;
						break;

					case NXTMessageTypes.DESTROY:
						m_oConnection = null;
					case NXTMessageTypes.FIRMWARE_VERSION:

						if (m_oConnection != null) {
							byte[] firmwareMessage = ((RawDataMsg)msg.obj).rgbyRawData;
							// check if we know the firmware
//							boolean isLejosMindDroid = true;
//							for (int pos=0; pos<4; pos++) {
//								if (firmwareMessage[pos + 3] != LCPMessage.FIRMWARE_VERSION_LEJOSMINDDROID[pos]) {
//									isLejosMindDroid = false;
//									break;
//								}
//							}
//							if (isLejosMindDroid) {
//								// mRobotType = R.id.robot_type_4;
//								setUpByType();
//							}
							
						}

						break;
	
					case NXTMessageTypes.GET_INPUT_VALUES:
						
						if (m_oConnection != null) {
							byte[] sensorMessage = ((RawDataMsg)msg.obj).rgbyRawData;
							msg.obj = NXTTypes.assembleSensorData(sensorMessage);
//							Utils.sendMessage(m_oUiHandler, messageID, NXTTypes.assembleSensorData(sensorMessage));
						}
						
						break;
//						return;

					case NXTMessageTypes.MOTOR_STATE:
						
						if (m_oConnection != null) {
							byte[] sensorMessage = ((RawDataMsg)msg.obj).rgbyRawData;
							msg.obj = NXTTypes.assembleMotorData(sensorMessage);
//							Utils.sendMessage(m_oUiHandler, messageID, NXTTypes.assembleMotorData(sensorMessage));
						}
						
						break;
//						return;
				
					}
					
					// forwards new message with same data to the ui handler
					Utils.sendMessage(m_oUiHandler, messageID, msg.obj);
//					m_oUiHandler.sendMessage(msg);
				}
				
			};
			Looper.loop();
		}
		
	}
	
	private class NXTSender extends Thread {

		private Handler m_oHandler;

		public Handler getHandler() {
			return m_oHandler;
		}

		public Looper getLooper() {
			return Looper.myLooper();
		}

		@Override
		public void run() {

			Looper.prepare();
			m_oHandler = new Handler() {
				
				@Override
				public void handleMessage(Message msg) {

					if (connected) {
			            switch (msg.what) {
			                case NXTMessageTypes.GET_FIRMWARE_VERSION:
			                	m_oConnection.requestFirmwareVersion();
			                    break;
			                case NXTMessageTypes.SET_INPUT_MODE:
			                	SensorTypeMsg cmdSensorTypeMsg = (SensorTypeMsg)msg.obj;
			                	m_oConnection.setInputMode(cmdSensorTypeMsg.nPort, cmdSensorTypeMsg.byType, cmdSensorTypeMsg.byMode);
			                	break;
			                case NXTMessageTypes.GET_INPUT_VALUES:
			                	SensorDataRequestMsg cmdSensorDataRequestMsg = (SensorDataRequestMsg)msg.obj;
			                	m_oConnection.requestInputValues(cmdSensorDataRequestMsg.nPort);
			                	break;
			                case NXTMessageTypes.SET_OUTPUT_STATE:
			                	MotorSpeedMsg cmdMotorSpeedMsg = (MotorSpeedMsg)msg.obj;
			                	m_oConnection.setMotorSpeed(cmdMotorSpeedMsg.nPort, cmdMotorSpeedMsg.nSpeed);
			                	break;
			                case NXTMessageTypes.MOTOR_STATE:
			                	MotorDataRequestMsg cmdMotorDataRequestMsg = (MotorDataRequestMsg)msg.obj;
			                	m_oConnection.requestMotorState(cmdMotorDataRequestMsg.nPort);
			                	break;
			                case NXTMessageTypes.RESET_MOTOR_POSITION:
			                	ResetMotorPositionMsg cmdResetMotorPosition = (ResetMotorPositionMsg)msg.obj;
			                	m_oConnection.resetMotorPosition(cmdResetMotorPosition.nPort, cmdResetMotorPosition.bRelative);
			                	break;
			                case NXTMessageTypes.GET_BATTERY_LEVEL:
			                	m_oConnection.requestBatteryLevel();
			                	break;
			                case NXTMessageTypes.GET_DISTANCE:
			                	SensorDataRequestMsg cmdDistanceRequestMsg = (SensorDataRequestMsg)msg.obj;
			                	getDistanceSensorData(cmdDistanceRequestMsg.nPort);
			                	break;
			                case NXTMessageTypes.DISCONNECT:
			                	shutDown();
			                    break;
			                case NXTMessageTypes.KEEP_ALIVE:
			                	m_oConnection.keepAlive();
			                	break;
			            }
		            }
				}
			};
			Looper.loop();
			
		}
	}
	
	private final TimerTask m_oKeepAlive = new TimerTask() {
		
		@Override
		public void run() {
			if (connected) {
				keepAlive();
			}
		}
		
	};

	public NXT() {
		super(NXTTypes.AXLE_WIDTH, NXTTypes.MAX_VELOCITY, NXTTypes.MIN_RADIUS, NXTTypes.MAX_RADIUS);
		
		m_oReceiver = new NXTReceiver();
		m_oReceiver.start();
		
		m_oSender = new NXTSender();
		m_oSender.start();

		m_oKeepAliveTimer = new Timer("KeepAliveTimer");
		m_oKeepAliveTimer.schedule(m_oKeepAlive, 30000, 30000);
		
//		setUpByType();
	}
	
	public void destroy() {
		m_oKeepAliveTimer.cancel();
		disconnect();
	}

	public void setHandler(Handler i_oHandler) {
		m_oUiHandler = i_oHandler;
	}
	
	public RobotType getType() {
		return RobotType.RBT_NXT;
	}
	
	public String getAddress() {
		if (m_oConnection != null) {
			return m_oConnection.getAddress();
		} else {
			return "";
		}
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public void connect() {
		connected = false;       
		if (m_oConnection != null) {
			m_oConnection.start();
		}
	}
	
	@Override
	public void disconnect() {
		sendCmdMessage(NXTMessageTypes.DISCONNECT);
		while (isConnected()) {
			Utils.waitSomeTime(10);
		}
	}
	
	public void setConnection(NXTBluetooth i_oConnection) {
		m_oConnection = i_oConnection;
		m_oConnection.setReceiveHandler(m_oReceiver.getHandler());
	}
	
	public NXTBluetooth getConnection() {
		return m_oConnection;
	}
	
	@Override
	public void enableControl(boolean i_bEnable) {
		// nothing to do, control always enabled
	}
	
	private void sendCmdMessage(int i_nCmd) {
		sendCmdMessage(i_nCmd, null);
	}
	
	private void sendCmdMessage(int i_nCmd, Object i_oData) {
		Utils.sendMessage(m_oSender.getHandler(), i_nCmd, i_oData);
	}
	
	private void sendResultMessage(int i_nCmd, Object i_oData) {
		Utils.sendMessage(m_oUiHandler, i_nCmd, i_oData);
	}

	public void getFirmwareVersion() {
		sendCmdMessage(NXTMessageTypes.GET_FIRMWARE_VERSION);
	}
	
	public void requestSensorData(ENXTSensorID i_eSensorID, ENXTSensorType i_eSensorType) {
		if (i_eSensorType == ENXTSensorType.sensType_Distance) {
			sendCmdMessage(NXTMessageTypes.GET_DISTANCE, MsgTypes.assembleSensorDataRequestMsg(i_eSensorID));
		} else {
			sendCmdMessage(NXTMessageTypes.GET_INPUT_VALUES, MsgTypes.assembleSensorDataRequestMsg(i_eSensorID));
		}
	}
	
	public void setSensorType(ENXTSensorID i_eSensorID, ENXTSensorType i_eSensorType) {
		sendCmdMessage(NXTMessageTypes.SET_INPUT_MODE, MsgTypes.assembleSensorTypeMsg(i_eSensorID, i_eSensorType));
	}
	
	public void keepAlive() {
		sendCmdMessage(NXTMessageTypes.KEEP_ALIVE);
	}
	
	public void requestMotorData(ENXTMotorID i_eMotorID) {
		sendCmdMessage(NXTMessageTypes.MOTOR_STATE, MsgTypes.assembleMotorDataRequestMsg(i_eMotorID));
	}
	
	public void resetMotorPosition(ENXTMotorID i_eMotorID, boolean i_bRelative) {
		sendCmdMessage(NXTMessageTypes.RESET_MOTOR_POSITION, MsgTypes.assembleResetMotorPositionMsg(i_eMotorID, i_bRelative));
	}

//	private int calculateVelocity(double i_dblSpeed) {
//		return (int) Math.round(i_dblSpeed);
//	}
//
//	private void calculateVelocity(double i_dblSpeed, int i_nRadius, int[] io_rgnVelocity) {
//		int nBaseVelocity = calculateVelocity(i_dblSpeed);
//		int nVelocity1, nVelocity2;
//		
//		int nAbsRadius = Math.abs(i_nRadius);
//		
//		if (i_nRadius == 0) {
//			io_rgnVelocity[0] = nBaseVelocity;
//			io_rgnVelocity[1] = nBaseVelocity;
//		} else {
//			nVelocity1 = (int) Math.round(nBaseVelocity * (nAbsRadius + m_dblAxleWidth) / (nAbsRadius + m_dblAxleWidth / 2.0));
//			nVelocity2 = (int) Math.round(nBaseVelocity * nAbsRadius / (nAbsRadius + m_dblAxleWidth / 2.0));
//			
//			// we have to make sure that the higher velocity of the two wheels (velocity1) cannot be more than the MAX_VELOCITY
//			// if it is more, we need to scale both values down so that the higher velocity equals MAX_VELOCITY. if the lower
//			// velocity would fall below 0 we set it to 0
//			int nOffset = nVelocity1 - NXTTypes.MAX_VELOCITY;
//			if (nOffset > 0) {
//				nVelocity1 = 100;
//				nVelocity2 = Math.max(nVelocity2 - nOffset, 0);
//			}
//			// for the same reason we have to make sure that the lower velocity of the two wheels cannot be less than 0. if the
//			// higher velocity would go above 100 we set it to 100
//			nOffset = -nVelocity2;
//			if (nOffset > 0) {
//				nVelocity1 = Math.min(nVelocity1 + nOffset, 100);
//				nVelocity2 = 0;
//			}
//			
//			if (i_nRadius > 0) {
//				io_rgnVelocity[0] = nVelocity2;
//				io_rgnVelocity[1] = nVelocity1;
//			} else if (i_nRadius < 0) {
//				io_rgnVelocity[0] = nVelocity1;
//				io_rgnVelocity[1] = nVelocity2;
//			}
//		}
//	}

	private void setMotorSpeed(ENXTMotorID i_eMotor, int i_nVelocity) {
		sendCmdMessage(NXTMessageTypes.SET_OUTPUT_STATE, MsgTypes.assembleMotorSpeedMsg(i_eMotor, i_nVelocity * m_nInvertFactor));
	}
	
	private void drive(int i_nLeftVelocity, int i_nRightVelocity) {
		debug(TAG, "left=" + i_nLeftVelocity + ", right=" + i_nRightVelocity);
		
		setMotorSpeed(ENXTMotorID.motor_1, i_nLeftVelocity);
		setMotorSpeed(ENXTMotorID.motor_2, i_nRightVelocity);
	}
	
	@Override
	public void moveForward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		drive(nVelocity, nVelocity);
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		debug(TAG, String.format("speed=%3f, radius=%d", i_dblSpeed, i_nRadius));
		
		int velocity[] = {0, 0};
		calculateVelocity(i_dblSpeed, i_nRadius, velocity);
		
		drive(velocity[0], velocity[1]);
	}
	
	public void moveForward() {
		moveForward(m_dblBaseSpeed);
	}

	public void moveForward(double i_dblSpeed, double i_dblAngle) {
		int nRadius = angleToRadius(i_dblAngle);
		
		moveForward(i_dblSpeed, nRadius);
	}

	@Override
	public void moveBackward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);

		drive(-nVelocity, -nVelocity);
	}
	
	public void moveBackward() {
		moveBackward(m_dblBaseSpeed);
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		debug(TAG, String.format("speed=%3f, radius=%d", i_dblSpeed, i_nRadius));
		
		int velocity[] = {0, 0};
		calculateVelocity(i_dblSpeed, i_nRadius, velocity);
		
		drive(-velocity[0], -velocity[1]);
	}

	public void moveBackward(double i_dblSpeed, double i_dblAngle) {
		int nRadius = angleToRadius(i_dblAngle);
		
		moveBackward(i_dblSpeed, nRadius);
	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		drive(nVelocity, -nVelocity);
	}
	
	public void rotateClockwise() {
		rotateClockwise(m_dblBaseSpeed);
	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);

		drive(-nVelocity, nVelocity);
	}
	
	public void rotateCounterClockwise() {
		rotateCounterClockwise(m_dblBaseSpeed);
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
	
	int m_lCircleTime = 4000;

	@Override
	public void moveStop() {
		drive(0, 0);
	}
	
	/**
	 * Initialization of the motor commands for the different robot types.
	 */
//	public void setUpByType() {
//		// default
//		motorLeft = NXTTypes.MOTOR_B;
//		directionLeft = 1;
//		motorRight = NXTTypes.MOTOR_C;
//		directionRight = 1;
//		motorAction = NXTTypes.MOTOR_A;
//		directionAction = 1;
//	}

    public synchronized void getDistanceSensorData(int port) {

		try {
			byte[] data = new byte[] { 0x02, 0x42 };
			m_oConnection.LSWrite(port, data, 1);
			
			Utils.waitSomeTime(100);
			
			for (int i = 0; i < 3; i++) {
				m_oConnection.LSGetStatus(port);
				waitAnswer(NXTMessageTypes.LS_GET_STATUS, 200);
				
				byte[] reply = m_oConnection.getReturnMessage();
				if (reply == null || reply[2] != LCPMessage.SUCCESS) {
					Utils.waitSomeTime(500);
				} else {
					break;
				}
			};
			
			m_oConnection.LSRead(port);
			waitAnswer(NXTMessageTypes.LS_READ, 200);
			
			sendResultMessage(NXTMessageTypes.GET_DISTANCE, NXTTypes.assembleDistanceData(port, m_oConnection.getReturnMessage()));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private boolean waitAnswer(int id, long timeout) throws InterruptedException {
		m_nWaitID = id;
		m_bMessageReceived = false;
		receiveEvent.wait(timeout);
		m_nWaitID = -1;
		return m_bMessageReceived;
	}

	public void shutDown() {
		// turn off input ports
		for (ENXTSensorID eSensor : ENXTSensorID.values()) {
			setSensorType(eSensor, ENXTSensorType.sensType_None);
		}

		if (m_oConnection != null) {
	        // send stop messages to motors
	    	m_oConnection.setMotorSpeed(NXTTypes.MOTOR_A, 0);
	    	m_oConnection.setMotorSpeed(NXTTypes.MOTOR_B, 0);
	    	m_oConnection.setMotorSpeed(NXTTypes.MOTOR_C, 0);
	    	
	        Utils.waitSomeTime(500);
	        try {
	        	// destroy connection
	        	m_oConnection.destroyConnection();
	        }
	        catch (IOException e) { 
	        	e.printStackTrace();
	        }
		}
		connected = false;
	}

	public void setInverted() {
		m_nInvertFactor *= -1;
	}
	
	public boolean isInverted() {
		return m_nInvertFactor == -1;
	}

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
