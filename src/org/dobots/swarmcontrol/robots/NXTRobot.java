package org.dobots.swarmcontrol.robots;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.dobots.nxt.BTCommunicator;
import org.dobots.nxt.BTConnectable;
import org.dobots.nxt.LCPMessage;
import org.dobots.nxt.NXT;
import org.dobots.nxt.NXTTypes;
import org.dobots.swarmcontrol.R;
import org.dobots.utility.DeviceListActivity;
import org.dobots.utility.Utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class NXTRobot extends RobotDevice implements BTConnectable {

	private final static String TAG = "NXT";

	private BTCommunicator myBTCommunicator = null;
	private Handler btcHandler;

	private ProgressDialog connectingProgressDialog;
	
	private boolean m_bPairing;

	private boolean connected;
	
	private NXT m_oNXT;

	private boolean btErrorPending = false;

	private List<String> programList;
	private static final int MAX_PROGRAMS = 20;

	// experimental TTS support
	private TextToSpeech mTts;
	private final int TTS_CHECK_CODE = 9991;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
		m_oActivity = this;
		
    }
    
		
	/**
	 * @return true, when currently pairing 
	 */
	@Override
	public boolean isPairing() {
		return m_bPairing;
	}

	/**
	 * Creates a new object for communication to the NXT robot via bluetooth and fetches the corresponding handler.
	 */
	private void createBTCommunicator() {
		Log.i(TAG, "BT Communicator created");
		// interestingly BT adapter needs to be obtained by the UI thread - so we pass it in in the constructor
		myBTCommunicator = new BTCommunicator(this, myHandler, BluetoothAdapter.getDefaultAdapter(), m_oActivity.getResources());
		btcHandler = myBTCommunicator.getHandler();
	}

	/**
	 * Creates and starts the a thread for communication via bluetooth to the NXT robot.
	 * @param mac_address The MAC address of the NXT robot.
	 */
	private void startBTCommunicator(String mac_address) {
		connected = false;        
		connectingProgressDialog = ProgressDialog.show(this, "", m_oActivity.getResources().getString(R.string.connecting_please_wait), true);

		if (myBTCommunicator != null) {
			try {
				myBTCommunicator.destroyNXTconnection();
			}
			catch (IOException e) { }
		}
		createBTCommunicator();
		myBTCommunicator.setMACAddress(mac_address);
		myBTCommunicator.start();
	}

	/**
	 * Sends a message for disconnecting to the communication thread.
	 */
	public void destroyBTCommunicator() {

		if (myBTCommunicator != null) {
			m_oNXT.disconnect();
			myBTCommunicator = null;
		}

		connected = false;
	}


	void selectNXT() {
		Intent serverIntent = new Intent(this, DeviceListActivity.class);
		Bundle oParam = new Bundle();
		oParam.putString(MAC_FILTER, NXTTypes.MAC_FILTER);
		serverIntent.putExtras(oParam);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}

	/**
	 * Receive messages from the BTCommunicator
	 */
	final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message myMessage) {
			switch (myMessage.getData().getInt("message")) {
			case BTCommunicator.DESTROY:
				destroyBTCommunicator();
			case BTCommunicator.DISPLAY_TOAST:
//				showToast(myMessage.getData().getString("toastText"), Toast.LENGTH_SHORT);
				break;
			case BTCommunicator.STATE_CONNECTED:
				connected = true;
				programList = new ArrayList<String>();
				connectingProgressDialog.dismiss();
//				updateButtonsAndMenu();
				m_oNXT.getFirmwareVersion();
				break;
			case BTCommunicator.MOTOR_STATE:

				if (myBTCommunicator != null) {
					byte[] motorMessage = myBTCommunicator.getReturnMessage();
					int position = Utils.byteToInt(motorMessage[21]) + (Utils.byteToInt(motorMessage[22]) << 8) + (Utils.byteToInt(motorMessage[23]) << 16)
					+ (Utils.byteToInt(motorMessage[24]) << 24);
//					showToast(getResources().getString(R.string.current_position) + position, Toast.LENGTH_SHORT);
				}

				break;

			case BTCommunicator.STATE_CONNECTERROR_PAIRING:
				connectingProgressDialog.dismiss();
				destroyBTCommunicator();
				break;

			case BTCommunicator.STATE_CONNECTERROR:
				connectingProgressDialog.dismiss();
			case BTCommunicator.STATE_RECEIVEERROR:
			case BTCommunicator.STATE_SENDERROR:

				destroyBTCommunicator();
				if (btErrorPending == false) {
					btErrorPending = true;
					// inform the user of the error with an AlertDialog
					AlertDialog.Builder builder = new AlertDialog.Builder(m_oActivity);
					builder.setTitle(getResources().getString(R.string.bt_error_dialog_title))
					.setMessage(getResources().getString(R.string.bt_error_dialog_message)).setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						//                            @Override
						public void onClick(DialogInterface dialog, int id) {
							btErrorPending = false;
							dialog.cancel();
							selectNXT();
						}
					});
					builder.create().show();
				}

				break;

			case BTCommunicator.FIRMWARE_VERSION:

				if (myBTCommunicator != null) {
					byte[] firmwareMessage = myBTCommunicator.getReturnMessage();
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
						m_oNXT.setUpByType();
					}
					
					// afterwards we search for all files on the robot
					m_oNXT.findFiles(0, 0);
				}

				break;

			case BTCommunicator.FIND_FILES:

				if (myBTCommunicator != null) {
					byte[] fileMessage = myBTCommunicator.getReturnMessage();
					String fileName = new String(fileMessage, 4, 20);
					fileName = fileName.replaceAll("\0","");

					//                        if (mRobotType == R.id.robot_type_4 || fileName.endsWith(".nxj") || fileName.endsWith(".rxe")) {
					//                            programList.add(fileName);
					//                        }

					// find next entry with appropriate handle, 
					// limit number of programs (in case of error (endless loop))
					if (programList.size() <= MAX_PROGRAMS) {
						m_oNXT.findFiles(1, Utils.byteToInt(fileMessage[3]));
					}
				}

				break;

			case BTCommunicator.PROGRAM_NAME:
				if (myBTCommunicator != null) {
					byte[] returnMessage = myBTCommunicator.getReturnMessage();
					m_oNXT.startRXEprogram(returnMessage[2]);
				}

				break;

			case BTCommunicator.SAY_TEXT:
				if (myBTCommunicator != null) {
					byte[] textMessage = myBTCommunicator.getReturnMessage();
					// evaluate control byte 
					byte controlByte = textMessage[2];
					// BIT7: Language
					if ((controlByte & 0x80) == 0x00) 
						mTts.setLanguage(Locale.US);
					else
						mTts.setLanguage(Locale.getDefault());
					// BIT6: Pitch
					if ((controlByte & 0x40) == 0x00)
						mTts.setPitch(1.0f);
					else
						mTts.setPitch(0.75f);
					// BIT0-3: Speech Rate    
					switch (controlByte & 0x0f) {
					case 0x01: 
						mTts.setSpeechRate(1.5f);
						break;                                 
					case 0x02: 
						mTts.setSpeechRate(0.75f);
						break;

					default: mTts.setSpeechRate(1.0f);
					break;
					}

					String ttsText = new String(textMessage, 3, 19);
					ttsText = ttsText.replaceAll("\0","");
//					showToast(ttsText, Toast.LENGTH_SHORT);
					mTts.speak(ttsText, TextToSpeech.QUEUE_FLUSH, null);
				}

				break;                    

			case BTCommunicator.VIBRATE_PHONE:
				if (myBTCommunicator != null) {
					byte[] vibrateMessage = myBTCommunicator.getReturnMessage();
					Vibrator myVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					myVibrator.vibrate(vibrateMessage[2]*10);
				}

				break;
			}
		}
	};
}
