package org.dobots.nxt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadFactory;

import org.dobots.nxt.NXTTypes.DistanceData;
import org.dobots.nxt.NXTTypes.ENXTMotorID;
import org.dobots.nxt.NXTTypes.ENXTSensorID;
import org.dobots.nxt.NXTTypes.ENXTSensorType;
import org.dobots.nxt.NXTTypes.MotorData;
import org.dobots.nxt.NXTTypes.SensorData;
import org.dobots.nxt.msg.RawDataMsg;
import org.dobots.roomba.RoombaTypes;
import org.dobots.swarmcontrol.R;
import org.dobots.utility.Utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
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

	private List<String> programList;
	private static final int MAX_PROGRAMS = 20;

	// experimental TTS support
	private TextToSpeech mTts;
	private final int TTS_CHECK_CODE = 9991;
	
	private SensorData m_oSensorData;
	private DistanceData m_oDistanceData;
	private MotorData m_oMotorData;

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

		@Override
		public void run() {
		
			Looper.prepare();
			m_oHandler = new Handler() {
				
				@Override
				public void handleMessage(Message msg) {

					int messageID = msg.getData().getInt("message");
					
					if (messageID == m_nWaitID) {
						m_bMessageReceived = true;
						synchronized(receiveEvent) {
							receiveEvent.notify();
						}
					}
					
					switch (messageID) {
					case NXTTypes.DESTROY:
						destroyBTCommunicator();
					case NXTTypes.DISPLAY_TOAST:
//						showToast(myMessage.getData().getString("toastText"), Toast.LENGTH_SHORT);
						break;
					case NXTTypes.STATE_CONNECTED:
						connected = true;
						programList = new ArrayList<String>();
//						updateButtonsAndMenu();
						getFirmwareVersion();
						break;
//					case NXTTypes.MOTOR_STATE:
//
//						if (m_oConnection != null) {
//							byte[] motorMessage = m_oConnection.getReturnMessage();
//							int position = Utils.byteToInt(motorMessage[21]) + (Utils.byteToInt(motorMessage[22]) << 8) + (Utils.byteToInt(motorMessage[23]) << 16)
//							+ (Utils.byteToInt(motorMessage[24]) << 24);
////							showToast(getResources().getString(R.string.current_position) + position, Toast.LENGTH_SHORT);
//						}
//
//						break;

					case NXTTypes.STATE_CONNECTERROR_PAIRING:
						destroyBTCommunicator();
						break;

					case NXTTypes.STATE_RECEIVEERROR:
					case NXTTypes.STATE_SENDERROR:
						connected = false;
						break;

					case NXTTypes.FIRMWARE_VERSION:

						if (m_oConnection != null) {
							byte[] firmwareMessage = m_oConnection.getReturnMessage();
							// check if we know the firmware
							boolean isLejosMindDroid = true;
							for (int pos=0; pos<4; pos++) {
								if (firmwareMessage[pos + 3] != LCPMessage.FIRMWARE_VERSION_LEJOSMINDDROID[pos]) {
									isLejosMindDroid = false;
									break;
								}
							}
							if (isLejosMindDroid) {
								//                            mRobotType = R.id.robot_type_4;
								setUpByType();
							}
							
							// afterwards we search for all files on the robot
							findFiles(0, 0);
						}

						break;
						
					case NXTTypes.FIND_FILES:
						
						if (m_oConnection != null) {
//							byte[] fileNameMessage = m_oConnection.getReturnMessage();
							byte[] fileNameMessage = ((RawDataMsg)msg.obj).rgbyRawData;
							String strName = new String(Arrays.copyOfRange(fileNameMessage, 4, 23));
							String str2 = strName;
						}
						
						break;
						
					case NXTTypes.GET_INPUT_VALUES:
						
						if (m_oConnection != null) {
//							byte[] sensorMessage = m_oConnection.getReturnMessage();
							byte[] sensorMessage = ((RawDataMsg)msg.obj).rgbyRawData;
							m_oSensorData =  NXTTypes.assembleSensorData(sensorMessage);

							Utils.sendDataBundle(m_oUiHandler, msg.getData(), m_oSensorData);
						}
						
						return;

					case NXTTypes.MOTOR_STATE:
						
						if (m_oConnection != null) {
//							byte[] sensorMessage = m_oConnection.getReturnMessage();
							byte[] sensorMessage = ((RawDataMsg)msg.obj).rgbyRawData;
							m_oMotorData =  NXTTypes.assembleMotorData(sensorMessage);

							Utils.sendDataBundle(m_oUiHandler, msg.getData(), m_oMotorData);
						}
						
						return;
						
					case NXTTypes.GET_DISTANCE:

						if (m_oConnection != null) {
//							byte[] sensorMessage = m_oConnection.getReturnMessage();
							byte[] sensorMessage = ((RawDataMsg)msg.obj).rgbyRawData;
							int port = msg.getData().getInt("value");
							m_oDistanceData =  NXTTypes.assembleDistanceData(port, sensorMessage);

							Utils.sendDataBundle(m_oUiHandler, msg.getData(), m_oDistanceData);
						}
						
						return;
					}
					
					// forwards new message with same data to the ui handler
					Utils.sendBundle(m_oUiHandler, msg.getData());
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

		@Override
		public void run() {

			Looper.prepare();
			m_oHandler = new Handler() {
				
				@Override
				public void handleMessage(Message myMessage) {

					if (connected) {

			            int message;
	
			            switch (message = myMessage.getData().getInt("message")) {
			                case NXTTypes.MOTOR_A:
			                case NXTTypes.MOTOR_B:
			                case NXTTypes.MOTOR_C:
			                    m_oConnection.setMotorSpeed(message, myMessage.getData().getInt("value1"));
			                    break;
			                case NXTTypes.MOTOR_B_ACTION:
			                	m_oConnection.rotateTo(NXTTypes.MOTOR_B, myMessage.getData().getInt("value1"));
			                    break;
			                case NXTTypes.START_PROGRAM:
			                	m_oConnection.startProgram(myMessage.getData().getString("name"));
			                    break;
			                case NXTTypes.STOP_PROGRAM:
			                	m_oConnection.stopProgram();
			                    break;
			                case NXTTypes.GET_PROGRAM_NAME:
			                	m_oConnection.requestProgramName();
			                    break;    
			                case NXTTypes.DO_BEEP:
			                	m_oConnection.doBeep(myMessage.getData().getInt("value1"), myMessage.getData().getInt("value2"));
			                    break;
			                case NXTTypes.DO_ACTION:
			                	m_oConnection.doAction(0);
			                    break;
			                case NXTTypes.READ_MOTOR_STATE:
			                	m_oConnection.requestMotorState(myMessage.getData().getInt("value1"));
			                    break;
			                case NXTTypes.GET_FIRMWARE_VERSION:
			                	m_oConnection.requestFirmwareVersion();
			                    break;
			                case NXTTypes.FIND_FILES:
			                	m_oConnection.findFiles(myMessage.getData().getInt("value1") == 0, myMessage.getData().getInt("value2"));
			                    break;
			                case NXTTypes.SET_INPUT_MODE:
			                	m_oConnection.setInputMode(myMessage.getData().getInt("value1"), myMessage.getData().getByte("value2"), myMessage.getData().getByte("value3"));
			                	break;
			                case NXTTypes.GET_INPUT_VALUES:
			                	m_oConnection.requestInputValues(myMessage.getData().getInt("value1"));
			                	break;
			                case NXTTypes.SET_OUTPUT_STATE:
			                	m_oConnection.setMotorSpeed(myMessage.getData().getInt("value1"), myMessage.getData().getInt("value2"));
			                	break;
			                case NXTTypes.MOTOR_STATE:
			                	m_oConnection.requestMotorState(myMessage.getData().getInt("value1"));
			                	break;
			                case NXTTypes.RESET_MOTOR_POSITION:
			                	m_oConnection.resetMotorPosition(myMessage.getData().getInt("value1"), myMessage.getData().getBoolean("value2"));
			                	break;
			                case NXTTypes.GET_BATTERY_LEVEL:
			                	m_oConnection.requestBatteryLevel();
			                	break;
			                case NXTTypes.GET_DISTANCE:
			                	getDistanceSensorData(myMessage.getData().getInt("value1"));
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
    
	public SensorData getReceivedSensorData() {
		return m_oSensorData;
	}
	
	public DistanceData getReceivedDistanceData() {
		return m_oDistanceData;
	}
	
	public MotorData getReceivedMotorData() {
		return m_oMotorData;
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
	
	public void disconnect() {
		sendBTCmessage(NXTTypes.NO_DELAY, NXTTypes.DISCONNECT, 0, 0);
	}
	
	public void getFirmwareVersion() {
		sendBTCmessage(NXTTypes.NO_DELAY, NXTTypes.GET_FIRMWARE_VERSION, 0, 0);
	}
	
	public void requestSensorData(ENXTSensorID i_eSensorID, ENXTSensorType i_eSensorType) {
		if (i_eSensorType == ENXTSensorType.sensType_Distance) {
			sendBTCmessage(NXTTypes.NO_DELAY, NXTTypes.GET_DISTANCE, i_eSensorID.getValue(), 0);
		} else {
			sendBTCmessage(NXTTypes.NO_DELAY, NXTTypes.GET_INPUT_VALUES, i_eSensorID.getValue(), 0);
		}
	}
	
	public void setSensorType(ENXTSensorID i_eSensorID, ENXTSensorType i_eSensorType) {
		sendBTCmessage(NXTTypes.NO_DELAY, NXTTypes.SET_INPUT_MODE, (byte)i_eSensorID.getValue(), i_eSensorType.getValue(), i_eSensorType.getDefaultMode());
	}
	
	public void keepAlive() {
		sendBTCmessage(NXTTypes.NO_DELAY, NXTTypes.KEEP_ALIVE, 0, 0);
	}
	
	public void requestMotorData(ENXTMotorID i_eSensorID) {
		sendBTCmessage(NXTTypes.NO_DELAY, NXTTypes.MOTOR_STATE, i_eSensorID.getValue(), 0);
	}
	
	public void resetMotorPosition(ENXTMotorID i_eMotorID, boolean i_bRelative) {
		sendBTCmessage(NXTTypes.NO_DELAY, NXTTypes.RESET_MOTOR_POSITION, i_eMotorID.getValue(), i_bRelative);
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
		sendBTCmessage(NXTTypes.NO_DELAY, NXTTypes.SET_OUTPUT_STATE, i_eMotor.getValue(), i_nVelocity * m_nInvert);
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

	public void findFiles(int par1, int par2) {
		sendBTCmessage(NXTTypes.NO_DELAY, NXTTypes.FIND_FILES, par1, par2);
	}

	/**
	 * Depending on the status (whether the program runs already) we stop it, wait and restart it again.
	 * @param status The current status, 0x00 means that the program is already running.
	 */   
	public void startRXEprogram(byte status) {
		if (status == 0x00) {
			sendBTCmessage(NXTTypes.NO_DELAY, NXTTypes.STOP_PROGRAM, 0, 0);
			sendBTCmessage(1000, NXTTypes.START_PROGRAM, programToStart);
		}    
		else {
			sendBTCmessage(NXTTypes.NO_DELAY, NXTTypes.START_PROGRAM, programToStart);
		}
	}        

	/**
	 * Starts a program on the NXT robot.
	 * @param name The program name to start. Has to end with .rxe on the LEGO firmware and with .nxj on the 
	 *             leJOS NXJ firmware.
	 */   
	public void startProgram(String name) {
		// for .rxe programs: get program name, eventually stop this and start the new one delayed
		// is handled in startRXEprogram()
		if (name.endsWith(".rxe")) {
			programToStart = name;        
			sendBTCmessage(NXTTypes.NO_DELAY, NXTTypes.GET_PROGRAM_NAME, 0, 0);
			return;
		}

		// for .nxj programs: stop bluetooth communication after starting the program
		if (name.endsWith(".nxj")) {
			sendBTCmessage(NXTTypes.NO_DELAY, NXTTypes.START_PROGRAM, name);
			Message myMessage = new Message();
			Bundle myBundle = new Bundle();
			myBundle.putInt("message", NXTTypes.DESTROY);
			myMessage.setData(myBundle);
			m_oUiHandler.dispatchMessage(myMessage);
			return;
		}        

		// for all other programs: just start the program
		sendBTCmessage(NXTTypes.NO_DELAY, NXTTypes.START_PROGRAM, name);
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

	        Bundle myBundle = new Bundle();
	        myBundle.putInt("message", NXTTypes.GET_DISTANCE);
	        myBundle.putInt("value", port);
	        Utils.sendBundle(m_oReceiver.getHandler(), myBundle);

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

	/**
	 * Sends the message via the BTCommunicator to the robot.
	 * @param delay time to wait before sending the message.
	 * @param message the message type (as defined in BTCommucator)
	 * @param value1 first parameter
	 * @param value2 second parameter
	 */   
	private void sendBTCmessage(int delay, int message, int value1, int value2) {
		Bundle myBundle = new Bundle();
		myBundle.putInt("message", message);
		myBundle.putInt("value1", value1);
		myBundle.putInt("value2", value2);
		Message myMessage = m_oUiHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			m_oSender.getHandler().sendMessage(myMessage);

		else
			m_oSender.getHandler().sendMessageDelayed(myMessage, delay);
	}
	
	private void sendBTCmessage(int delay, int message, int value1, boolean value2) {
		Bundle myBundle = new Bundle();
		myBundle.putInt("message", message);
		myBundle.putInt("value1", value1);
		myBundle.putBoolean("value2", value2);
		Message myMessage = m_oUiHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			m_oSender.getHandler().sendMessage(myMessage);

		else
			m_oSender.getHandler().sendMessageDelayed(myMessage, delay);
	}
	
	private void sendBTCmessage(int delay, int message, int value1, byte value2, byte value3) {
		Bundle myBundle = new Bundle();
		myBundle.putInt("message", message);
		myBundle.putInt("value1", value1);
		myBundle.putByte("value2", value2);
		myBundle.putByte("value3", value3);
		Message myMessage = m_oUiHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			m_oSender.getHandler().sendMessage(myMessage);

		else
			m_oSender.getHandler().sendMessageDelayed(myMessage, delay);
	}

	/**
	 * Sends the message via the BTCommuncator to the robot.
	 * @param delay time to wait before sending the message.
	 * @param message the message type (as defined in BTCommucator)
	 * @param String a String parameter
	 */       
	void sendBTCmessage(int delay, int message, String name) {
		Bundle myBundle = new Bundle();
		myBundle.putInt("message", message);
		myBundle.putString("name", name);
		Message myMessage = m_oUiHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			m_oSender.getHandler().sendMessage(myMessage);
		else
			m_oSender.getHandler().sendMessageDelayed(myMessage, delay);
	}

	public void setInverted() {
		m_nInvert *= -1;
	}
	
	public boolean isInverted() {
		return m_nInvert == -1;
	}

}
