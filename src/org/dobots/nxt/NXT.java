package org.dobots.nxt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.dobots.nxt.NXTTypes.DistanceData;
import org.dobots.nxt.NXTTypes.ENXTSensorID;
import org.dobots.nxt.NXTTypes.ENXTSensorType;
import org.dobots.nxt.NXTTypes.SensorData;
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
import android.os.Message;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class NXT implements BTConnectable {

	private static String TAG = "NXT";

	private BTCommunicator m_oConnection;
	private Handler m_oBtcHandler;

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
	private Handler m_oHandler;

	private boolean connected = false;

	private List<String> programList;
	private static final int MAX_PROGRAMS = 20;

	// experimental TTS support
	private TextToSpeech mTts;
	private final int TTS_CHECK_CODE = 9991;
	
	private SensorData m_oSensorData;
	private DistanceData m_oDistanceData;
	
	/**
	 * Receive messages from the BTCommunicator
	 */
	final Handler nxtHandler = new Handler() {
		@Override
		public void handleMessage(Message myMessage) {
			
			int messageID = myMessage.getData().getInt("message");
			
			switch (messageID) {
			case BTCommunicator.DESTROY:
				destroyBTCommunicator();
			case BTCommunicator.DISPLAY_TOAST:
//				showToast(myMessage.getData().getString("toastText"), Toast.LENGTH_SHORT);
				break;
			case BTCommunicator.STATE_CONNECTED:
				connected = true;
				programList = new ArrayList<String>();
//				updateButtonsAndMenu();
				getFirmwareVersion();
				break;
			case BTCommunicator.MOTOR_STATE:

				if (m_oConnection != null) {
					byte[] motorMessage = m_oConnection.getReturnMessage();
					int position = Utils.byteToInt(motorMessage[21]) + (Utils.byteToInt(motorMessage[22]) << 8) + (Utils.byteToInt(motorMessage[23]) << 16)
					+ (Utils.byteToInt(motorMessage[24]) << 24);
//					showToast(getResources().getString(R.string.current_position) + position, Toast.LENGTH_SHORT);
				}

				break;

			case BTCommunicator.STATE_CONNECTERROR_PAIRING:
				destroyBTCommunicator();
				break;

			case BTCommunicator.FIRMWARE_VERSION:

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
				
			case BTCommunicator.FIND_FILES:
				
				if (m_oConnection != null) {
					byte[] fileNameMessage = m_oConnection.getReturnMessage();
					String strName = new String(Arrays.copyOfRange(fileNameMessage, 4, 23));
					String str2 = strName;
				}
				
				break;
				
			case BTCommunicator.GET_INPUT_VALUES:
				
				if (m_oConnection != null) {
					byte[] sensorMessage = m_oConnection.getReturnMessage();
					m_oSensorData =  NXTTypes.assembleSensorData(sensorMessage);
				}
				
				break;
				
			case BTCommunicator.GET_DISTANCE:

				if (m_oConnection != null) {
					byte[] sensorMessage = m_oConnection.getReturnMessage();
					int port = myMessage.getData().getInt("value");
					m_oDistanceData =  NXTTypes.assembleDistanceData(port, sensorMessage);
				}
				
				break;


//			case BTCommunicator.SAY_TEXT:
//				if (m_oConnection != null) {
//					byte[] textMessage = m_oConnection.getReturnMessage();
//					// evaluate control byte 
//					byte controlByte = textMessage[2];
//					// BIT7: Language
//					if ((controlByte & 0x80) == 0x00) 
//						mTts.setLanguage(Locale.US);
//					else
//						mTts.setLanguage(Locale.getDefault());
//					// BIT6: Pitch
//					if ((controlByte & 0x40) == 0x00)
//						mTts.setPitch(1.0f);
//					else
//						mTts.setPitch(0.75f);
//					// BIT0-3: Speech Rate    
//					switch (controlByte & 0x0f) {
//					case 0x01: 
//						mTts.setSpeechRate(1.5f);
//						break;                                 
//					case 0x02: 
//						mTts.setSpeechRate(0.75f);
//						break;
//
//					default: mTts.setSpeechRate(1.0f);
//					break;
//					}
//
//					String ttsText = new String(textMessage, 3, 19);
//					ttsText = ttsText.replaceAll("\0","");
////					showToast(ttsText, Toast.LENGTH_SHORT);
//					mTts.speak(ttsText, TextToSpeech.QUEUE_FLUSH, null);
//				}
//
//				break;                    
//
//			case BTCommunicator.VIBRATE_PHONE:
//				if (m_oConnection != null) {
//					byte[] vibrateMessage = m_oConnection.getReturnMessage();
//					Vibrator myVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//					myVibrator.vibrate(vibrateMessage[2]*10);
//				}
//
//				break;
			}
			
			m_oHandler.dispatchMessage(myMessage);
		}
	};

	public NXT(Handler i_oHandler, Resources i_oResources) {
		m_oHandler = i_oHandler;
		m_oResources = i_oResources;

		setUpByType();
	}
    
	public SensorData getReceivedSensorData() {
		return m_oSensorData;
	}
	
	public DistanceData getReceivedDistanceData() {
		return m_oDistanceData;
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
		sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DISCONNECT, 0, 0);
	}
	
	public void getFirmwareVersion() {
		sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.GET_FIRMWARE_VERSION, 0, 0);
	}
	
	public void requestSensorData(ENXTSensorID i_eSensorID, ENXTSensorType i_eSensorType) {
		if (i_eSensorType == ENXTSensorType.sensType_Distance) {
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.GET_DISTANCE, i_eSensorID.getValue(), 0);
		} else {
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.GET_INPUT_VALUES, i_eSensorID.getValue(), 0);
		}
	}
	
	public void setSensorType(ENXTSensorID i_eSensorID, ENXTSensorType i_eSensorType) {
		sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.SET_INPUT_MODE, (byte)i_eSensorID.getValue(), i_eSensorType.getValue(), i_eSensorType.getDefaultMode());
	}

	/**
	 * Initialization of the motor commands for the different robot types.
	 */
	public void setUpByType() {
		// default
		motorLeft = BTCommunicator.MOTOR_B;
		directionLeft = 1;
		motorRight = BTCommunicator.MOTOR_C;
		directionRight = 1;
		motorAction = BTCommunicator.MOTOR_A;
		directionAction = 1;
	}

	/**
	 * Creates a new object for communication to the NXT robot via bluetooth and fetches the corresponding handler.
	 */
	public void createBTCommunicator() {
		Log.i(TAG, "BT Communicator created");
		// interestingly BT adapter needs to be obtained by the UI thread - so we pass it in in the constructor
		m_oConnection = new BTCommunicator(this, nxtHandler, BluetoothAdapter.getDefaultAdapter(), m_oResources);
		m_oBtcHandler = m_oConnection.getHandler();
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
		sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.FIND_FILES, par1, par2);
	}

	/**
	 * Depending on the status (whether the program runs already) we stop it, wait and restart it again.
	 * @param status The current status, 0x00 means that the program is already running.
	 */   
	public void startRXEprogram(byte status) {
		if (status == 0x00) {
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.STOP_PROGRAM, 0, 0);
			sendBTCmessage(1000, BTCommunicator.START_PROGRAM, programToStart);
		}    
		else {
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.START_PROGRAM, programToStart);
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
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.GET_PROGRAM_NAME, 0, 0);
			return;
		}

		// for .nxj programs: stop bluetooth communication after starting the program
		if (name.endsWith(".nxj")) {
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.START_PROGRAM, name);
			Message myMessage = new Message();
			Bundle myBundle = new Bundle();
			myBundle.putInt("message", BTCommunicator.DESTROY);
			myMessage.setData(myBundle);
			m_oHandler.dispatchMessage(myMessage);
			return;
		}        

		// for all other programs: just start the program
		sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.START_PROGRAM, name);
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
		Message myMessage = m_oHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			m_oBtcHandler.sendMessage(myMessage);

		else
			m_oBtcHandler.sendMessageDelayed(myMessage, delay);
	}
	
	private void sendBTCmessage(int delay, int message, int value1, byte value2, byte value3) {
		Bundle myBundle = new Bundle();
		myBundle.putInt("message", message);
		myBundle.putInt("value1", value1);
		myBundle.putByte("value2", value2);
		myBundle.putByte("value3", value3);
		Message myMessage = m_oHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			m_oBtcHandler.sendMessage(myMessage);

		else
			m_oBtcHandler.sendMessageDelayed(myMessage, delay);
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
		Message myMessage = m_oHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			m_oBtcHandler.sendMessage(myMessage);
		else
			m_oBtcHandler.sendMessageDelayed(myMessage, delay);
	}

	public void shutDown() {
		for (ENXTSensorID eSensor : ENXTSensorID.values()) {
			setSensorType(eSensor, ENXTSensorType.sensType_None);
		}
	}

}
