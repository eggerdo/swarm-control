package org.dobots.nxt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.dobots.nxt.NXTTypes.ENXTMotorID;
import org.dobots.nxt.NXTTypes.ENXTSensorID;
import org.dobots.nxt.NXTTypes.ENXTSensorType;
import org.dobots.nxt.msg.MsgTypes;
import org.dobots.nxt.msg.MsgTypes.MotorDataRequestMsg;
import org.dobots.nxt.msg.MsgTypes.MotorSpeedMsg;
import org.dobots.nxt.msg.MsgTypes.RawDataMsg;
import org.dobots.nxt.msg.MsgTypes.ResetMotorPositionMsg;
import org.dobots.nxt.msg.MsgTypes.SensorDataRequestMsg;
import org.dobots.nxt.msg.MsgTypes.SensorTypeMsg;
import org.dobots.utility.Utils;

import android.bluetooth.BluetoothAdapter;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class NXT implements BTConnectable {

	private static String TAG = "NXT";

	private BTCommunicator m_oConnection;

	private boolean m_bPairing;

	int motorLeft;
	private int directionLeft; // +/- 1
	int motorRight;
	private boolean stopAlreadySent = false;
	private int directionRight; // +/- 1
	private int motorAction;
	private int directionAction; // +/- 1
	
	private String programToStart;

	private Resources m_oResources;
	private Handler m_oUiHandler;

	private boolean connected = false;

	private NXTReceiver m_oReceiver;
	private NXTSender m_oSender;

	private int m_nWaitID;
	private Object receiveEvent = this;
	private boolean m_bMessageReceived = false;

	private Timer m_oKeepAliveTimer;
	
	private int m_nInvert = 1;	// normal = 1, inverted = -1
	
	private class NXTReceiver extends Thread {
		
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

					int messageID = msg.what;
					
					if (messageID == m_nWaitID) {
						m_bMessageReceived = true;
						synchronized(receiveEvent) {
							receiveEvent.notify();
						}
					}
					
					switch (messageID) {
					case NXTTypes.DESTROY:
						destroyBTCommunicator();
					case NXTTypes.STATE_CONNECTED:
						connected = true;
						getFirmwareVersion();
						break;

					case NXTTypes.STATE_CONNECTERROR_PAIRING:
						destroyBTCommunicator();
						break;

					case NXTTypes.STATE_RECEIVEERROR:
					case NXTTypes.STATE_SENDERROR:
						connected = false;
						break;

					case NXTTypes.FIRMWARE_VERSION:

						if (m_oConnection != null) {
							byte[] firmwareMessage = ((RawDataMsg)msg.obj).rgbyRawData;
							// check if we know the firmware
							boolean isLejosMindDroid = true;
							for (int pos=0; pos<4; pos++) {
								if (firmwareMessage[pos + 3] != LCPMessage.FIRMWARE_VERSION_LEJOSMINDDROID[pos]) {
									isLejosMindDroid = false;
									break;
								}
							}
							if (isLejosMindDroid) {
								// mRobotType = R.id.robot_type_4;
								setUpByType();
							}
							
						}

						break;
	
					case NXTTypes.GET_INPUT_VALUES:
						
						if (m_oConnection != null) {
							byte[] sensorMessage = ((RawDataMsg)msg.obj).rgbyRawData;
							msg.obj = NXTTypes.assembleSensorData(sensorMessage);
//							Utils.sendMessage(m_oUiHandler, messageID, NXTTypes.assembleSensorData(sensorMessage));
						}
						
						break;
//						return;

					case NXTTypes.MOTOR_STATE:
						
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
			                case NXTTypes.GET_FIRMWARE_VERSION:
			                	m_oConnection.requestFirmwareVersion();
			                    break;
			                case NXTTypes.SET_INPUT_MODE:
			                	SensorTypeMsg cmdSensorTypeMsg = (SensorTypeMsg)msg.obj;
			                	m_oConnection.setInputMode(cmdSensorTypeMsg.nPort, cmdSensorTypeMsg.byType, cmdSensorTypeMsg.byMode);
			                	break;
			                case NXTTypes.GET_INPUT_VALUES:
			                	SensorDataRequestMsg cmdSensorDataRequestMsg = (SensorDataRequestMsg)msg.obj;
			                	m_oConnection.requestInputValues(cmdSensorDataRequestMsg.nPort);
			                	break;
			                case NXTTypes.SET_OUTPUT_STATE:
			                	MotorSpeedMsg cmdMotorSpeedMsg = (MotorSpeedMsg)msg.obj;
			                	m_oConnection.setMotorSpeed(cmdMotorSpeedMsg.nPort, cmdMotorSpeedMsg.nSpeed);
			                	break;
			                case NXTTypes.MOTOR_STATE:
			                	MotorDataRequestMsg cmdMotorDataRequestMsg = (MotorDataRequestMsg)msg.obj;
			                	m_oConnection.requestMotorState(cmdMotorDataRequestMsg.nPort);
			                	break;
			                case NXTTypes.RESET_MOTOR_POSITION:
			                	ResetMotorPositionMsg cmdResetMotorPosition = (ResetMotorPositionMsg)msg.obj;
			                	m_oConnection.resetMotorPosition(cmdResetMotorPosition.nPort, cmdResetMotorPosition.bRelative);
			                	break;
			                case NXTTypes.GET_BATTERY_LEVEL:
			                	m_oConnection.requestBatteryLevel();
			                	break;
			                case NXTTypes.GET_DISTANCE:
			                	SensorDataRequestMsg cmdDistanceRequestMsg = (SensorDataRequestMsg)msg.obj;
			                	getDistanceSensorData(cmdDistanceRequestMsg.nPort);
			                	break;
			                case NXTTypes.DISCONNECT:
			                	shutDown();
			                    break;
			                case NXTTypes.KEEP_ALIVE:
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

	public NXT(Handler i_oHandler, Resources i_oResources) {
		m_oUiHandler = i_oHandler;
		m_oResources = i_oResources;
		
		m_oReceiver = new NXTReceiver();
		m_oReceiver.start();
		
		m_oSender = new NXTSender();
		m_oSender.start();

		m_oKeepAliveTimer = new Timer("KeepAliveTimer");
		m_oKeepAliveTimer.schedule(m_oKeepAlive, 30000, 30000);
		
		setUpByType();
	}
	
	public void destroy() {
//		m_oReceiver.getLooper().quit();
//		m_oSender.getLooper().quit();
		m_oKeepAliveTimer.cancel();
	}
    
	/**
	 * @return true, when currently pairing 
	 */
	@Override
	public boolean isPairing() {
		return m_bPairing;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnection(BTCommunicator i_oConnection) {
		m_oConnection = i_oConnection;
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
	
	public void disconnect() {
		sendCmdMessage(NXTTypes.DISCONNECT);
	}
	
	public void getFirmwareVersion() {
		sendCmdMessage(NXTTypes.GET_FIRMWARE_VERSION);
	}
	
	public void requestSensorData(ENXTSensorID i_eSensorID, ENXTSensorType i_eSensorType) {
		if (i_eSensorType == ENXTSensorType.sensType_Distance) {
			sendCmdMessage(NXTTypes.GET_DISTANCE, MsgTypes.assembleSensorDataRequestMsg(i_eSensorID));
		} else {
			sendCmdMessage(NXTTypes.GET_INPUT_VALUES, MsgTypes.assembleSensorDataRequestMsg(i_eSensorID));
		}
	}
	
	public void setSensorType(ENXTSensorID i_eSensorID, ENXTSensorType i_eSensorType) {
		sendCmdMessage(NXTTypes.SET_INPUT_MODE, MsgTypes.assembleSensorTypeMsg(i_eSensorID, i_eSensorType));
	}
	
	public void keepAlive() {
		sendCmdMessage(NXTTypes.KEEP_ALIVE);
	}
	
	public void requestMotorData(ENXTMotorID i_eMotorID) {
		sendCmdMessage(NXTTypes.MOTOR_STATE, MsgTypes.assembleMotorDataRequestMsg(i_eMotorID));
	}
	
	public void resetMotorPosition(ENXTMotorID i_eMotorID, boolean i_bRelative) {
		sendCmdMessage(NXTTypes.RESET_MOTOR_POSITION, MsgTypes.assembleResetMotorPositionMsg(i_eMotorID, i_bRelative));
	}

	private double capSpeed(double io_dblSpeed) {
		// if a negative value was provided as speed
		// use the absolute value of it.
		io_dblSpeed = Math.abs(io_dblSpeed);
		io_dblSpeed = Math.min(io_dblSpeed, 100);
		io_dblSpeed = Math.max(io_dblSpeed, 0);
		
		return io_dblSpeed;
	}
	
//	private void capRadius(int io_nRadius) {
//		io_nRadius = Math.min(io_nRadius, NXTTypes.MAX_RADIUS);
//		io_nRadius = Math.max(io_nRadius, -NXTTypes.MAX_RADIUS);
//		
//		// exclude the special cases
//		if (io_nRadius == 0) {
//			io_nRadius = NXTTypes.STRAIGHT;
//		}
//		
//		if (io_nRadius == -1) {
//			io_nRadius = -2;
//		}
//		
//		if (io_nRadius == 1) {
//			io_nRadius = 2;
//		}
//	}
	
	private int calculateVelocity(double i_dblSpeed) {
		return (int) Math.round(i_dblSpeed);
	}
	
	public void driveForward(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		setMotorSpeed(ENXTMotorID.motor_1, nVelocity);
		setMotorSpeed(ENXTMotorID.motor_2, nVelocity);
	}
	
	private void setMotorSpeed(ENXTMotorID i_eMotor, int i_nVelocity) {
		sendCmdMessage(NXTTypes.SET_OUTPUT_STATE, MsgTypes.assembleMotorSpeedMsg(i_eMotor, i_nVelocity * m_nInvert));
	}
	
//	public void driveForward(double i_dblSpeed, int i_nRadius) {
//		i_dblSpeed = capSpeed(i_dblSpeed);
//		capRadius(i_nRadius);
//		int nVelocity = calculateVelocity(i_dblSpeed);
//		
//		oRoombaCtrl.drive(nVelocity, i_nRadius);
//	}
	
	public void driveBackward(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);

		setMotorSpeed(ENXTMotorID.motor_1, -nVelocity);
		setMotorSpeed(ENXTMotorID.motor_2, -nVelocity);
	}

//	public void driveBackward(double i_dblSpeed, int i_nRadius) {
//		i_dblSpeed = capSpeed(i_dblSpeed);
//		capRadius(i_nRadius);
//		int nVelocity = calculateVelocity(i_dblSpeed);
//		
//		oRoombaCtrl.drive(-nVelocity, i_nRadius);
//	}
	
	public void rotateClockwise(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		setMotorSpeed(ENXTMotorID.motor_1, nVelocity);
		setMotorSpeed(ENXTMotorID.motor_2, -nVelocity);
	}
	
	public void rotateCounterClockwise(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		int nVelocity = calculateVelocity(i_dblSpeed);

		setMotorSpeed(ENXTMotorID.motor_1, -nVelocity);
		setMotorSpeed(ENXTMotorID.motor_2, nVelocity);
	}
	
	public void stop() {
		setMotorSpeed(ENXTMotorID.motor_1, 0);
		setMotorSpeed(ENXTMotorID.motor_2, 0);
	}
	
	/**
	 * Initialization of the motor commands for the different robot types.
	 */
	public void setUpByType() {
		// default
		motorLeft = NXTTypes.MOTOR_B;
		directionLeft = 1;
		motorRight = NXTTypes.MOTOR_C;
		directionRight = 1;
		motorAction = NXTTypes.MOTOR_A;
		directionAction = 1;
	}

	/**
	 * Creates a new object for communication to the NXT robot via bluetooth and fetches the corresponding handler.
	 */
	public void createBTCommunicator() {
		Log.i(TAG, "BT Communicator created");
		// interestingly BT adapter needs to be obtained by the UI thread - so we pass it in in the constructor
		m_oConnection = new BTCommunicator(this, m_oReceiver.getHandler(), BluetoothAdapter.getDefaultAdapter(), m_oResources);
	}

	/**
	 * Creates and starts the a thread for communication via bluetooth to the NXT robot.
	 * @param mac_address The MAC address of the NXT robot.
	 */
	public void startBTCommunicator(String mac_address) {
		connected = false;        
		
		if (m_oConnection != null) {
			try {
				m_oConnection.destroyNXTconnection();
			}
			catch (IOException e) { }
		}
		createBTCommunicator();
		m_oConnection.setMACAddress(mac_address);
		m_oConnection.start();
	}

	/**
	 * Sends a message for disconnecting to the communication thread.
	 */
	public void destroyBTCommunicator() {

		if (m_oConnection != null) {
			disconnect();
			m_oConnection = null;
		}

		connected = false;
	}

    public synchronized void getDistanceSensorData(int port) {

		try {
			byte[] data = new byte[] { 0x02, 0x42 };
			m_oConnection.LSWrite(port, data, 1);
			
			Thread.sleep(100);
			
			for (int i = 0; i < 3; i++) {
				m_oConnection.LSGetStatus(port);
				waitAnswer(NXTTypes.LS_GET_STATUS, 200);
				
				if (m_oConnection.getReturnMessage()[2] != LCPMessage.SUCCESS) {
					Thread.sleep(500);
				} else {
					break;
				}
			};
			
			m_oConnection.LSRead(port);
			waitAnswer(NXTTypes.LS_READ, 200);
			
			sendResultMessage(NXTTypes.GET_DISTANCE, NXTTypes.assembleDistanceData(port, m_oConnection.getReturnMessage()));
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void waitAnswer(int id, long timeout) throws InterruptedException {
		m_nWaitID = id;
		m_bMessageReceived = false;
		receiveEvent.wait(timeout);
		m_nWaitID = -1;
	}

	public void shutDown() {
		// turn off input ports
		for (ENXTSensorID eSensor : ENXTSensorID.values()) {
			setSensorType(eSensor, ENXTSensorType.sensType_None);
		}

        // send stop messages to motors
    	m_oConnection.setMotorSpeed(NXTTypes.MOTOR_A, 0);
    	m_oConnection.setMotorSpeed(NXTTypes.MOTOR_B, 0);
    	m_oConnection.setMotorSpeed(NXTTypes.MOTOR_C, 0);
    	
        Utils.waitSomeTime(500);
        try {
        	// destroy connection
        	m_oConnection.destroyNXTconnection();
        }
        catch (IOException e) { 
        	e.printStackTrace();
        }
	}

	public void setInverted() {
		m_nInvert *= -1;
	}
	
	public boolean isInverted() {
		return m_nInvert == -1;
	}

}
