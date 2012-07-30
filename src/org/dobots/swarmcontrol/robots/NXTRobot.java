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
import org.dobots.nxt.NXTTypes.ENXTSensorID;
import org.dobots.nxt.NXTTypes.ENXTSensorType;
import org.dobots.roomba.RoombaBluetooth;
import org.dobots.roomba.RoombaTypes.ERoombaSensorPackages;
import org.dobots.swarmcontrol.R;
import org.dobots.utility.AccelerometerManager;
import org.dobots.utility.DeviceListActivity;
import org.dobots.utility.ProgressDlg;
import org.dobots.utility.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class NXTRobot extends RobotDevice {

	private static String TAG = "NXT";
	
	private static final int DEBUG_ID = Menu.FIRST;

	private ProgressDialog connectingProgressDialog;
	
	private boolean connected;
	
	private NXT m_oNxt;

	private boolean btErrorPending = false;

	private Toast reusableToast;
	
	private NXTSensorGatherer m_oSensorGatherer;
	
	private boolean m_bDebug;

	private Button m_btnFwd;
	private Button m_btnBwd;
	private Button m_btnLeft;
	private Button m_btnRight;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
		reusableToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		
		m_strRobotMacFilter = NXTTypes.MAC_FILTER;
		
		m_oNxt = new NXT(uiHandler, getResources());
		m_oSensorGatherer = new NXTSensorGatherer(this, m_oNxt);
		
        setDebug(false);
		
		try {
			// if bluetooth is not yet enabled, initBluetooth will return false
			// and the device selection will be called in the onActivityResult
			if (initBluetooth())
				selectRobot();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();

    	if (m_oNxt.isConnected()) {
    		m_oNxt.shutDown();
    		m_oNxt.disconnect();
    	}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, DEBUG_ID, 1, "Debug ON");
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case DEBUG_ID:
			setDebug(!m_bDebug);
			item.setTitle("Debug " + (m_bDebug ? "OFF" : "ON"));
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void connectToRobot(String i_strAddr) {
		connectingProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.connecting_please_wait), true);
		m_oNxt.startBTCommunicator(i_strAddr);
	}

	/**
	 * Displays a message as a toast
	 * @param textToShow the message
	 * @param length the length of the toast to display
	 */
	private void showToast(String textToShow, int length) {
		reusableToast.setText(textToShow);
		reusableToast.setDuration(length);
		reusableToast.show();
	}

	/**
	 * Displays a message as a toast
	 * @param nResID the resource ID to display
	 * @param length the length of the toast to display
	 */
	private void showToast(int nResID, int length) {
		reusableToast.setText(nResID);
		reusableToast.setDuration(length);
		reusableToast.show();
	}

	/**
	 * Receive messages from the BTCommunicator
	 */
	final Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message myMessage) {
			switch (myMessage.getData().getInt("message")) {
			case BTCommunicator.DISPLAY_TOAST:
				showToast(myMessage.getData().getString("toastText"), Toast.LENGTH_SHORT);
				break;
			case BTCommunicator.STATE_CONNECTED:
				connected = true;
				connectingProgressDialog.dismiss();
//				updateButtonsAndMenu();
				break;

			case BTCommunicator.STATE_CONNECTERROR_PAIRING:
				connectingProgressDialog.dismiss();
				break;

			case BTCommunicator.STATE_CONNECTERROR:
				connectingProgressDialog.dismiss();
			case BTCommunicator.STATE_RECEIVEERROR:
			case BTCommunicator.STATE_SENDERROR:

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
							selectRobot();
						}
					});
					builder.create().show();
				}

				break;
				
			case BTCommunicator.GET_INPUT_VALUES:
				m_oSensorGatherer.sendMessage(NXTTypes.SENSOR_DATA_RECEIVED);
				break;
				
			case BTCommunicator.GET_DISTANCE:
				m_oSensorGatherer.sendMessage(NXTTypes.DISTANCE_DATA_RECEIVED);
				break;
				
//			case BTCommunicator.SAY_TEXT:
//				if (myBTCommunicator != null) {
//					byte[] textMessage = myBTCommunicator.getReturnMessage();
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
//				if (myBTCommunicator != null) {
//					byte[] vibrateMessage = myBTCommunicator.getReturnMessage();
//					Vibrator myVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//					myVibrator.vibrate(vibrateMessage[2]*10);
//				}
//
//				break;
			}
		}
	};
	
	
	@Override
	protected void setProperties(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.nxt);
        
        // adapter is the same, for each sensor we can choose the same types
		final ArrayAdapter<ENXTSensorType> oSensorTypeAdapter = new ArrayAdapter<ENXTSensorType>(m_oActivity, 
				android.R.layout.simple_spinner_item, ENXTSensorType.values());
        oSensorTypeAdapter.setDropDownViewResource(android.R.layout.select_dialog_item);
        
        for (ENXTSensorID eSensorID : ENXTSensorID.values()) {
        	
        	int nSpinnerResId;
        	int nCheckboxResId;
        	
        	// get resource id based on sensor id
        	switch (eSensorID) {
        	case sens_sensor1:
        		nSpinnerResId = R.id.spSensor1Type;
        		nCheckboxResId = R.id.cbSensor1;
        		break;
        	case sens_sensor2:
        		nSpinnerResId = R.id.spSensor2Type;
        		nCheckboxResId = R.id.cbSensor2;
        		break;
        	case sens_sensor3:
        		nSpinnerResId = R.id.spSensor3Type;
        		nCheckboxResId = R.id.cbSensor3;
        		break;
        	case sens_sensor4:
        		nSpinnerResId = R.id.spSensor4Type;
        		nCheckboxResId = R.id.cbSensor4;
        		break;
    		default:
    			continue;
        	}
        	
        	Spinner spSensors = (Spinner) m_oActivity.findViewById(nSpinnerResId);
        	// add sensor id as tag to the spinner so we can access it later on
        	spSensors.setTag(eSensorID);
        	spSensors.setAdapter(oSensorTypeAdapter);
        	spSensors.setOnItemSelectedListener(new OnItemSelectedListener() {

    			@Override
    			public void onItemSelected(AdapterView<?> parent, View view,
    					int position, long id) {
    				ENXTSensorType eSensorType = oSensorTypeAdapter.getItem(position);
    				// get the sensor id from the parent object
    				m_oSensorGatherer.setSensorType((ENXTSensorID)parent.getTag(), eSensorType);
    			}

    			@Override
    			public void onNothingSelected(AdapterView<?> arg0) {
    				// do nothing
    			}
    			
    		});
        	
        	CheckBox cbSensor = (CheckBox) m_oActivity.findViewById(nCheckboxResId);
        	// add sensor id as tag to the check box so we can access it later on
        	cbSensor.setTag(eSensorID);
    		cbSensor.setOnCheckedChangeListener(new OnCheckedChangeListener() {
    			
    			@Override
    			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    				// get the sensor id from the view object
    				m_oSensorGatherer.enableSensor((ENXTSensorID)buttonView.getTag(), isChecked);
    			}
    		});
        	
        }
        
//        Button test = (Button) m_oActivity.findViewById(R.id.test);
//        test.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				m_oNxt.setSensorType(ENXTSensorID.sens_sensor1, ENXTSensorType.sensType_Distance);
//				try {
//					Thread.sleep(500);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				m_oNxt.requestSensorData(ENXTSensorID.sens_sensor1, ENXTSensorType.sensType_Distance);
//			}
//		});
		
		m_btnFwd = (Button) m_oActivity.findViewById(R.id.btnFwd);
		m_btnLeft = (Button) m_oActivity.findViewById(R.id.btnLeft);
		m_btnBwd = (Button) m_oActivity.findViewById(R.id.btnBwd);
		m_btnRight = (Button) m_oActivity.findViewById(R.id.btnRight);
		
//		m_btnFwd.setOnTouchListener(new OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent e) {
//				int action = e.getAction();
//				switch (action & MotionEvent.ACTION_MASK) {
//				case MotionEvent.ACTION_CANCEL:
//				case MotionEvent.ACTION_UP:
//					oRoomba.stop();
//					break;
//				case MotionEvent.ACTION_POINTER_UP:
//					break;
//				case MotionEvent.ACTION_DOWN:
//					oRoomba.driveForward(50);
//					break;
//				case MotionEvent.ACTION_POINTER_DOWN:
//					break;					
//				case MotionEvent.ACTION_MOVE:
//					break;
//				}
//				return true;
//			}
//		});
//		
//		m_btnBwd.setOnTouchListener(new OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent e) {
//				int action = e.getAction();
//				switch (action & MotionEvent.ACTION_MASK) {
//				case MotionEvent.ACTION_CANCEL:
//				case MotionEvent.ACTION_UP:
//					oRoomba.stop();
//					break;
//				case MotionEvent.ACTION_POINTER_UP:
//					break;
//				case MotionEvent.ACTION_DOWN:
//					oRoomba.driveBackward(50);
//					break;
//				case MotionEvent.ACTION_POINTER_DOWN:
//					break;					
//				case MotionEvent.ACTION_MOVE:
//					break;
//				}
//				return true;
//			}
//		});
//
//		m_btnLeft.setOnTouchListener(new OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent e) {
//				int action = e.getAction();
//				switch (action & MotionEvent.ACTION_MASK) {
//				case MotionEvent.ACTION_CANCEL:
//				case MotionEvent.ACTION_UP:
//					oRoomba.stop();
//					break;
//				case MotionEvent.ACTION_POINTER_UP:
//					break;
//				case MotionEvent.ACTION_DOWN:
//					oRoomba.rotateCounterClockwise(50);
//					break;
//				case MotionEvent.ACTION_POINTER_DOWN:
//					break;					
//				case MotionEvent.ACTION_MOVE:
//					break;
//				}
//				return true;
//			}
//		});
//
//		m_btnRight.setOnTouchListener(new OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent e) {
//				int action = e.getAction();
//				switch (action & MotionEvent.ACTION_MASK) {
//				case MotionEvent.ACTION_CANCEL:
//				case MotionEvent.ACTION_UP:
//					oRoomba.stop();
//					break;
//				case MotionEvent.ACTION_POINTER_UP:
//					break;
//				case MotionEvent.ACTION_DOWN:
//					oRoomba.rotateClockwise(50);
//					break;
//				case MotionEvent.ACTION_POINTER_DOWN:
//					break;					
//				case MotionEvent.ACTION_MOVE:
//					break;
//				}
//				return true;
//			}
//		});
		
	}

	public void setDebug(boolean i_bDebug) {
		
		m_bDebug = i_bDebug;
		
		m_oSensorGatherer.setDebug(i_bDebug);
		
		// create a temporary layout from the nxt layout
		View oTempView = LayoutInflater.from(m_oActivity).inflate(R.layout.nxt, null);
		
		for (ENXTSensorID eSensorID : ENXTSensorID.values()) {

        	int nDataResId;
        	int nRawValueResId;
        	int nCalibValueResId;
        	int nNormValueResId;
        	
        	// get resource ids based on sensor id
        	switch (eSensorID) {
        	case sens_sensor1:
        		nDataResId = R.id.tblSensor1_data;
        		nRawValueResId = R.id.tblrwSensor1_raw;
        		nCalibValueResId = R.id.tblrwSensor1_calib;
        		nNormValueResId = R.id.tblrwSensor1_norm;
        		break;
        	case sens_sensor2:
        		nDataResId = R.id.tblSensor2_data;
        		nRawValueResId = R.id.tblrwSensor2_raw;
        		nCalibValueResId = R.id.tblrwSensor2_calib;
        		nNormValueResId = R.id.tblrwSensor2_norm;
        		break;
        	case sens_sensor3:
        		nDataResId = R.id.tblSensor3_data;
        		nRawValueResId = R.id.tblrwSensor3_raw;
        		nCalibValueResId = R.id.tblrwSensor3_calib;
        		nNormValueResId = R.id.tblrwSensor3_norm;
        		break;
        	case sens_sensor4:
        		nDataResId = R.id.tblSensor4_data;
        		nRawValueResId = R.id.tblrwSensor4_raw;
        		nCalibValueResId = R.id.tblrwSensor4_calib;
        		nNormValueResId = R.id.tblrwSensor4_norm;
        		break;
    		default:
    			continue;
        	}
        	
        	TableLayout tblData = (TableLayout) m_oActivity.findViewById(nDataResId);
        	
        	if (i_bDebug) {

        		// if the table row for the raw value already exists in the current layout
        		// then continue with the next sensor
        		if (m_oActivity.findViewById(nRawValueResId) != null)
        			continue;
        		
        		// get the table row for the raw value from the temp layout
        		TableRow tblrwRow = (TableRow) oTempView.findViewById(nRawValueResId);
        		// remove it from the temp layout
        		((TableLayout)tblrwRow.getParent()).removeView(tblrwRow);
        		// add it to the current layout
        		tblData.addView(tblrwRow);

        		// add the calibrated value row to the current layout
        		tblrwRow = (TableRow) oTempView.findViewById(nCalibValueResId);
        		((TableLayout)tblrwRow.getParent()).removeView(tblrwRow);
        		tblData.addView(tblrwRow);

        		// add the normalised value row to the current layout
        		tblrwRow = (TableRow) oTempView.findViewById(nNormValueResId);
        		((TableLayout)tblrwRow.getParent()).removeView(tblrwRow);
        		tblData.addView(tblrwRow);
        		
        	} else {

        		TableRow tblrwRow = (TableRow) m_oActivity.findViewById(nRawValueResId);

        		// if the table row for the raw value doesn't exist in the current layout
        		// continue with the next sensor        		
        		if (tblData.indexOfChild(tblrwRow) == -1) {
        			continue;
        		} else {
        			// otherwise remove it from the layout
        			tblData.removeView(tblrwRow);
        		}
    			
        		// remove the calibrated value row from the layout
    			tblrwRow = (TableRow) m_oActivity.findViewById(nCalibValueResId);
    			tblData.removeView(tblrwRow);

    			// remove the normalised value row from the layout
    			tblrwRow = (TableRow) m_oActivity.findViewById(nNormValueResId);
    			tblData.removeView(tblrwRow);
    			
        	}
		}
	}
}
