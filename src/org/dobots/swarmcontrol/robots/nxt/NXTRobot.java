package org.dobots.swarmcontrol.robots.nxt;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.dobots.robots.nxt.LCPMessage;
import org.dobots.robots.nxt.NXT;
import org.dobots.robots.nxt.NXTTypes;
import org.dobots.robots.nxt.NXTTypes.ENXTMotorID;
import org.dobots.robots.nxt.NXTTypes.ENXTMotorSensorType;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorID;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorType;
import org.dobots.robots.roomba.RoombaBluetooth;
import org.dobots.robots.roomba.RoombaTypes.ERoombaSensorPackages;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.swarmcontrol.robots.RobotView;
import org.dobots.utility.AccelerometerListener;
import org.dobots.utility.AccelerometerManager;
import org.dobots.utility.DeviceListActivity;
import org.dobots.utility.ProgressDlg;
import org.dobots.utility.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class NXTRobot extends RobotView implements BTConnectable {

	private static String TAG = "NXT";
	
	private static final int CONNECT_ID = Menu.FIRST;
	private static final int DEBUG_ID = CONNECT_ID + 1;
	private static final int INVERT_ID = DEBUG_ID + 1;
	private static final int ACCEL_ID = INVERT_ID + 1;

	private ProgressDialog connectingProgressDialog;
	
	private boolean connected;
	
	private NXT m_oNxt;

	private boolean btErrorPending = false;

	private NXTSensorGatherer m_oSensorGatherer;
	
	private boolean m_bDebug;
	private boolean m_bControl;

	private Button m_btnFwd;
	private Button m_btnBwd;
	private Button m_btnLeft;
	private Button m_btnRight;
	
	private String m_strMacAddress = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
		m_strRobotMacFilter = NXTTypes.MAC_FILTER;
		
    	super.onCreate(savedInstanceState);
		
		m_oNxt = new NXT();
		m_oNxt.setHandler(uiHandler);
		
		m_oSensorGatherer = new NXTSensorGatherer(this, m_oNxt);
		
        setDebug(false);
        updateButtons(true);
        updateControlButtons(false);
		
        connectToRobot();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();

    	if (m_oNxt.isConnected()) {
    		m_oNxt.disconnect();
    		m_oNxt.destroy();
    	}

    	m_oSensorGatherer.stopThread();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	
//    	m_oSensorGatherer.pauseThread();
    	
    	if (m_oNxt.isConnected()) {
    		m_oNxt.disconnect();
    	}
    }

    @Override
    public void onPause() {
    	super.onPause();

    	m_bAccelerometer = false;
    }

    @Override
    public void onRestart() {
    	super.onRestart();
    	
    	if (m_strMacAddress != "") {
    		connectToRobot(m_strMacAddress);
    	}

//    	m_oSensorGatherer.resumeThread();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, CONNECT_ID, 1, "Connect");
		menu.add(0, DEBUG_ID, 2, "Debug ON");
		return true;
	}
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	if (m_bControl) {
    		if (menu.findItem(INVERT_ID) == null) {
				menu.add(0, INVERT_ID, 3, "Invert Driving (ON)");
				menu.add(0, ACCEL_ID, 3, "Accelerometer (ON)");
    		}
		} else
			if (menu.findItem(INVERT_ID) != null) {
				menu.removeItem(INVERT_ID);
				menu.removeItem(ACCEL_ID);
			}
    	
    	MenuItem item = menu.findItem(ACCEL_ID);
    	if (item != null) {
    		item.setTitle("Accelerometer " + (m_bAccelerometer ? "(OFF)" : "(ON)"));
    	}
		return true;
    }

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case CONNECT_ID:
			m_oNxt.disconnect();
			m_oSensorGatherer.initialize();
			resetLayout();
			updateButtons(false);
			m_oBTHelper.selectRobot();
			return true;
		case DEBUG_ID:
			setDebug(!m_bDebug);
			item.setTitle("Debug " + (m_bDebug ? "OFF" : "ON"));
			return true;
		case INVERT_ID:
			m_oNxt.setInverted();
			item.setTitle("Invert Driving " + (m_oNxt.isInverted() ? "(OFF)" : "(ON)"));
			return true;
		case ACCEL_ID:
			m_bAccelerometer = !m_bAccelerometer;

			if (m_bAccelerometer) {
				m_bSetAccelerometerBase = true;
			} else {
				m_oNxt.driveStop();
			}
		}
			

		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	protected void connectToRobot() {
		// if bluetooth is not yet enabled, initBluetooth will return false
		// and the device selection will be called in the onActivityResult
		if (m_oBTHelper.initBluetooth())
			m_oBTHelper.selectRobot();
	}
	
	@Override
	public void connectToRobot(String i_strAddr) {
		if (m_oBTHelper.initBluetooth()) {
			m_strMacAddress = i_strAddr;
			connectingProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.connecting_please_wait), true);
			createBTCommunicator(i_strAddr);
			m_oNxt.connect();
		}
	}

	/**
	 * Creates a new object for communication to the NXT robot via bluetooth and fetches the corresponding handler.
	 */
	public void createBTCommunicator(String i_strAddress) {
		Log.i(TAG, "creating BT Communicator");
		
		BTCommunicator oBTCommunicator = m_oNxt.getConnection();
		if (oBTCommunicator != null) {
			try {
				oBTCommunicator.destroyNXTconnection();
			}
			catch (IOException e) { }
		}
		// interestingly BT adapter needs to be obtained by the UI thread - so we pass it in in the constructor
		oBTCommunicator = new BTCommunicator(this, BluetoothAdapter.getDefaultAdapter(), getResources());
		oBTCommunicator.setMACAddress(i_strAddress);
		m_oNxt.setConnection(oBTCommunicator);
	}

	/**
	 * Sends a message for disconnecting to the communication thread.
	 */
	public void destroyBTCommunicator() {
		
		if (m_oNxt.getConnection() != null) {
			m_oNxt.disconnect();
			m_oNxt.setConnection(null);
		}

		connected = false;
	}
	
	@Override
	public void onAccelerationChanged(float x, float y, float z, boolean tx) {
		super.onAccelerationChanged(x, y, z, tx);
		
		if (tx && m_bAccelerometer) {
			int speed = getSpeedFromAcceleration(x, y, z, NXTTypes.MAX_VELOCITY, false);
			int radius = getRadiusFromAcceleration(x, y, z, NXTTypes.MAX_RADIUS);
			
			// if speed is negative the roomba should drive forward
			// if it is positive it should drive backward
			if (speed > SPEED_SENSITIVITY) {
				// remove the speed sensitivity again
//				speed -= SPEED_SENSITIVITY; 

				Log.i("Speeds", "speed=" + speed + ", radius=" + radius); 

				if (radius > RADIUS_SENSITIVITY) {
					m_oNxt.driveForward(speed, radius);
				} else if (radius < -RADIUS_SENSITIVITY) {
					m_oNxt.driveForward(speed, radius);
				} else {
					m_oNxt.driveForward(speed);
				}
			} else if (speed < -SPEED_SENSITIVITY) {
				// remove the speed_sensitivity again
//				speed += SPEED_SENSITIVITY;

				Log.i("Speeds", "speed=" + speed + ", radius=" + radius); 

				if (radius > RADIUS_SENSITIVITY) {
					m_oNxt.driveBackward(speed, radius);
				} else if (radius < -RADIUS_SENSITIVITY) {
					m_oNxt.driveBackward(speed, radius);
				} else {
					m_oNxt.driveBackward(speed);
				}
			} else {

				Log.i("Speeds", "speed=~0" + ", radius=" + radius); 

				if (radius > RADIUS_SENSITIVITY) {
					// if speed is small we remap the radius to 
					// speed and let it rotate on the spot 
					speed = (int) (radius / (double)NXTTypes.MAX_RADIUS * NXTTypes.MAX_VELOCITY);
					m_oNxt.rotateCounterClockwise(speed);
				} else if (radius < -RADIUS_SENSITIVITY) {
					// if speed is small we remap the radius to 
					// speed and let it rotate on the spot 
					speed = (int) (radius / (double)NXTTypes.MAX_RADIUS * NXTTypes.MAX_VELOCITY);
					m_oNxt.rotateClockwise(speed);
				} else {
					m_oNxt.driveStop();
				}
				
			}
		}
	}

	/**
	 * Receive messages from the BTCommunicator
	 */
	final Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NXTTypes.DISPLAY_TOAST:
				showToast((String)msg.obj, Toast.LENGTH_SHORT);
				break;
			case NXTTypes.STATE_CONNECTED:
				connected = true;
				connectingProgressDialog.dismiss();
				updateButtons(true);
//				updateButtonsAndMenu();
				break;

			case NXTTypes.STATE_CONNECTERROR_PAIRING:
				connectingProgressDialog.dismiss();
				break;

			case NXTTypes.STATE_CONNECTERROR:
				connectingProgressDialog.dismiss();
			case NXTTypes.STATE_RECEIVEERROR:
			case NXTTypes.STATE_SENDERROR:

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
							m_oBTHelper.selectRobot();
						}
					});
					builder.create().show();
				}

				break;
				
			case NXTTypes.GET_INPUT_VALUES:
				m_oSensorGatherer.sendMessage(NXTTypes.SENSOR_DATA_RECEIVED, msg.obj);
				break;
				
			case NXTTypes.GET_DISTANCE:
				m_oSensorGatherer.sendMessage(NXTTypes.DISTANCE_DATA_RECEIVED, msg.obj);
				break;
				
			case NXTTypes.MOTOR_STATE:
				m_oSensorGatherer.sendMessage(NXTTypes.MOTOR_DATA_RECEIVED, msg.obj);
				break;
			
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

        // adapter is the same, for each sensor we can choose the same types
		final ArrayAdapter<ENXTMotorSensorType> oMotorSensorTypeAdapter = new ArrayAdapter<ENXTMotorSensorType>(m_oActivity, 
				android.R.layout.simple_spinner_item, ENXTMotorSensorType.values());
		oMotorSensorTypeAdapter.setDropDownViewResource(android.R.layout.select_dialog_item);
        
        for (ENXTMotorID eMotorID : ENXTMotorID.values()) {
        	
        	int nCheckboxResId, nResetId, nSpinnerResId;
        	
        	// get resource id based on sensor id
        	switch (eMotorID) {
        	case motor_1:
        		nCheckboxResId 	= R.id.cbMotor1;
        		nResetId		= R.id.btnMotor1Reset;
        		nSpinnerResId	= R.id.spMotor1Type;
        		break;
        	case motor_2:
        		nCheckboxResId 	= R.id.cbMotor2;
        		nResetId		= R.id.btnMotor2Reset;
        		nSpinnerResId	= R.id.spMotor2Type;
        		break;
        	case motor_3:
        		nCheckboxResId 	= R.id.cbMotor3;
        		nResetId		= R.id.btnMotor3Reset;
        		nSpinnerResId	= R.id.spMotor3Type;
        		break;
    		default:
    			continue;
        	}
        	
        	CheckBox cbSensor = (CheckBox) m_oActivity.findViewById(nCheckboxResId);
        	// add sensor id as tag to the check box so we can access it later on
        	cbSensor.setTag(eMotorID);
    		cbSensor.setOnCheckedChangeListener(new OnCheckedChangeListener() {
    			
    			@Override
    			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    				// get the sensor id from the view object
    				m_oSensorGatherer.enableMotor((ENXTMotorID)buttonView.getTag(), isChecked);
    			}
    		});

        	Spinner spSensors = (Spinner) m_oActivity.findViewById(nSpinnerResId);
        	// add sensor id as tag to the spinner so we can access it later on
        	spSensors.setTag(eMotorID);
        	spSensors.setAdapter(oMotorSensorTypeAdapter);
        	spSensors.setOnItemSelectedListener(new OnItemSelectedListener() {

    			@Override
    			public void onItemSelected(AdapterView<?> parent, View view,
    					int position, long id) {
    				ENXTMotorSensorType eMotorSensorType = oMotorSensorTypeAdapter.getItem(position);
    				// get the sensor id from the parent object
    				m_oSensorGatherer.setMotorSensorType((ENXTMotorID)parent.getTag(), eMotorSensorType);
    			}

    			@Override
    			public void onNothingSelected(AdapterView<?> arg0) {
    				// do nothing
    			}
    			
    		});
        	
    		Button btnReset = (Button) m_oActivity.findViewById(nResetId);
    		btnReset.setTag(eMotorID);
    		btnReset.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					m_oNxt.resetMotorPosition((ENXTMotorID)v.getTag(), true);
					m_oNxt.resetMotorPosition((ENXTMotorID)v.getTag(), false);
				}
			});
        }
        
		Button btnControl = (Button) m_oActivity.findViewById(R.id.btnCtrl);
		btnControl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bControl = !m_bControl;
				updateControlButtons(m_bControl);
				((Button)v).setText("Control " + (m_bControl ? "OFF" : "ON"));
				if (m_bControl) {
//					Rect r = new Rect();
////					 ((LinearLayout)m_oActivity.findViewById(R.id.layRemoteControl)).getDrawingRect(r);
////					 ((LinearLayout)m_oActivity.findViewById(R.id.layRemoteControl)).requestRectangleOnScreen(r);
//					ScrollView sview = (ScrollView)m_oActivity.findViewById(R.id.nxt);
////					sview.scrollTo(0, m_oActivity.findViewById(R.id.layRemoteControl).getBottom());
//					View ctrl = m_oActivity.findViewById(R.id.layRemoteControl);
//					sview.recomputeViewAttributes(ctrl);
//					sview.refreshDrawableState();
//					ctrl.setFocusable(true);
//					ctrl.requestFocus();
//					ctrl.getLocalVisibleRect(r);
//					ctrl.getDrawingRect(r);
//					ctrl.getGlobalVisibleRect(r);
//					ctrl.requestRectangleOnScreen(r);
				}
			}
		});
	
		m_btnFwd = (Button) m_oActivity.findViewById(R.id.btnFwd);
		m_btnLeft = (Button) m_oActivity.findViewById(R.id.btnLeft);
		m_btnBwd = (Button) m_oActivity.findViewById(R.id.btnBwd);
		m_btnRight = (Button) m_oActivity.findViewById(R.id.btnRight);
		
		m_btnFwd.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oNxt.driveStop();
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_DOWN:
					m_oNxt.driveForward(50);
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					break;					
				case MotionEvent.ACTION_MOVE:
					break;
				}
				return true;
			}
		});
		
		m_btnBwd.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oNxt.driveStop();
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_DOWN:
					m_oNxt.driveBackward(50);
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					break;					
				case MotionEvent.ACTION_MOVE:
					break;
				}
				return true;
			}
		});

		m_btnLeft.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oNxt.driveStop();
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_DOWN:
					m_oNxt.rotateCounterClockwise(50);
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					break;					
				case MotionEvent.ACTION_MOVE:
					break;
				}
				return true;
			}
		});

		m_btnRight.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oNxt.driveStop();
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_DOWN:
					m_oNxt.rotateClockwise(50);
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					break;					
				case MotionEvent.ACTION_MOVE:
					break;
				}
				return true;
			}
		});
		
	}

	public void updateControlButtons(boolean visible) {
		Utils.showLayout((LinearLayout)m_oActivity.findViewById(R.id.layRemoteControl), visible);
	}
	
	public void updateArrowButtons(boolean enabled) {
		m_oActivity.findViewById(R.id.btnLeft).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnRight).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnFwd).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnBwd).setEnabled(enabled);
		
	}

	public void updateButtons(boolean enabled) {
		m_oActivity.findViewById(R.id.btnCtrl).setEnabled(enabled);
		m_oActivity.findViewById(R.id.cbSensor1).setEnabled(enabled);
		m_oActivity.findViewById(R.id.spSensor1Type).setEnabled(enabled);
		m_oActivity.findViewById(R.id.cbSensor2).setEnabled(enabled);
		m_oActivity.findViewById(R.id.spSensor2Type).setEnabled(enabled);
		m_oActivity.findViewById(R.id.cbSensor3).setEnabled(enabled);
		m_oActivity.findViewById(R.id.spSensor3Type).setEnabled(enabled);
		m_oActivity.findViewById(R.id.cbSensor4).setEnabled(enabled);
		m_oActivity.findViewById(R.id.spSensor4Type).setEnabled(enabled);
		m_oActivity.findViewById(R.id.cbMotor1).setEnabled(enabled);
		m_oActivity.findViewById(R.id.spMotor1Type).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnMotor1Reset).setEnabled(enabled);
		m_oActivity.findViewById(R.id.cbMotor2).setEnabled(enabled);
		m_oActivity.findViewById(R.id.spMotor2Type).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnMotor2Reset).setEnabled(enabled);
		m_oActivity.findViewById(R.id.cbMotor3).setEnabled(enabled);
		m_oActivity.findViewById(R.id.spMotor3Type).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnMotor3Reset).setEnabled(enabled);
	}
	
	public void resetLayout() {
		updateControlButtons(false);
		
		((CheckBox)m_oActivity.findViewById(R.id.cbSensor1)).setChecked(false);
		((Spinner)m_oActivity.findViewById(R.id.spSensor1Type)).setSelection(0);
		
		((CheckBox)m_oActivity.findViewById(R.id.cbSensor2)).setChecked(false);
		((Spinner)m_oActivity.findViewById(R.id.spSensor2Type)).setSelection(0);
		
		((CheckBox)m_oActivity.findViewById(R.id.cbSensor3)).setChecked(false);
		((Spinner)m_oActivity.findViewById(R.id.spSensor3Type)).setSelection(0);
		
		((CheckBox)m_oActivity.findViewById(R.id.cbSensor4)).setChecked(false);
		((Spinner)m_oActivity.findViewById(R.id.spSensor4Type)).setSelection(0);
		
		((CheckBox)m_oActivity.findViewById(R.id.cbMotor1)).setChecked(false);
		((Spinner)m_oActivity.findViewById(R.id.spMotor1Type)).setSelection(0);
		
		((CheckBox)m_oActivity.findViewById(R.id.cbMotor2)).setChecked(false);
		((Spinner)m_oActivity.findViewById(R.id.spMotor2Type)).setSelection(0);
		
		((CheckBox)m_oActivity.findViewById(R.id.cbMotor3)).setChecked(false);
		((Spinner)m_oActivity.findViewById(R.id.spMotor3Type)).setSelection(0);
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

		for (ENXTMotorID eMotorID : ENXTMotorID.values()) {

        	int nDataResId, nTachoCountResId;
        	
        	// get resource ids based on sensor id
        	switch (eMotorID) {
        	case motor_1:
        		nDataResId = R.id.tblMotor1_data;
        		nTachoCountResId = R.id.tblrMotor1_TachoCount;
        		break;
        	case motor_2:
        		nDataResId = R.id.tblMotor2_data;
        		nTachoCountResId = R.id.tblrMotor2_TachoCount;
        		break;
        	case motor_3:
        		nDataResId = R.id.tblMotor3_data;
        		nTachoCountResId = R.id.tblrMotor3_TachoCount;
        		break;
    		default:
    			continue;
        	}
        	

        	TableLayout tblData = (TableLayout) m_oActivity.findViewById(nDataResId);
        	
        	if (i_bDebug) {

        		// if the table row for the raw value already exists in the current layout
        		// then continue with the next sensor
        		if (m_oActivity.findViewById(nTachoCountResId) != null)
        			continue;
        		
        		// get the table row for the raw value from the temp layout
        		TableRow tblrwRow = (TableRow) oTempView.findViewById(nTachoCountResId);
        		// remove it from the temp layout
        		((TableLayout)tblrwRow.getParent()).removeView(tblrwRow);
        		// add it to the current layout
        		tblData.addView(tblrwRow);

        	} else {

        		TableRow tblrwRow = (TableRow) m_oActivity.findViewById(nTachoCountResId);

        		// if the table row for the raw value doesn't exist in the current layout
        		// continue with the next sensor        		
        		if (tblData.indexOfChild(tblrwRow) == -1) {
        			continue;
        		} else {
        			// otherwise remove it from the layout
        			tblData.removeView(tblrwRow);
        		}
    			
        	}
		}
	}

	@Override
	public boolean isPairing() {
		// TODO Auto-generated method stub
		return false;
	}
}
