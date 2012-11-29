package org.dobots.swarmcontrol.robots.nxt;

import java.io.IOException;

import org.dobots.robots.MessageTypes;
import org.dobots.robots.nxt.NXT;
import org.dobots.robots.nxt.NXTTypes;
import org.dobots.robots.nxt.NXTTypes.ENXTMotorID;
import org.dobots.robots.nxt.NXTTypes.ENXTMotorSensorType;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorID;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorType;
import org.dobots.swarmcontrol.BaseActivity;
import org.dobots.swarmcontrol.ConnectListener;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.RemoteControlHelper;
import org.dobots.swarmcontrol.RobotInventory;
import org.dobots.swarmcontrol.robots.BluetoothRobot;
import org.dobots.swarmcontrol.robots.RobotCalibration;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.swarmcontrol.robots.roboscooper.RoboScooperRobot;
import org.dobots.utility.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

public class NXTRobot extends BluetoothRobot implements BTConnectable {

	private static String TAG = "NXT";
	
	private static final int DEBUG_ID = CONNECT_ID + 1;
	private static final int INVERT_ID = DEBUG_ID + 1;
	private static final int ACCEL_ID = INVERT_ID + 1;
	private static final int ADVANCED_CONTROL_ID = ACCEL_ID + 1;
	
	private static final int REMOTE_CTRL_GRP = GENERAL_GRP + 1;

	private boolean connected;
	
	private NXT m_oNxt;

	private NXTSensorGatherer m_oSensorGatherer;

	private RemoteControlHelper m_oRemoteCtrl;

	private boolean m_bDebug;

	private Button m_btnCalibrate;
	private CheckBox m_cbSensor1;
	private CheckBox m_cbSensor2;
	private CheckBox m_cbSensor3;
	private CheckBox m_cbSensor4;
	private Spinner m_spSensor1Type;
	private Spinner m_spSensor2Type;
	private Spinner m_spSensor3Type;
	private Spinner m_spSensor4Type;
	private CheckBox m_cbMotor1;
	private CheckBox m_cbMotor2;
	private CheckBox m_cbMotor3;
	private Spinner m_spMotor1Type;
	private Spinner m_spMotor2Type;
	private Spinner m_spMotor3Type;
	private Button m_btnMotor1Reset;
	private Button m_btnMotor2Reset;
	private Button m_btnMotor3Reset;

	private double m_dblSpeed;

	public NXTRobot(BaseActivity i_oOwner) {
		super(i_oOwner);
	}
	
	public NXTRobot() {
		super();
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	int nIndex = (Integer) getIntent().getExtras().get("InventoryIndex");
    	if (nIndex == -1) {
			m_oNxt = new NXT();
	        connectToRobot();
    	} else {
    		m_oNxt = (NXT) RobotInventory.getInstance().getRobot(nIndex);
    		m_bKeepAlive = true;
    	}
		m_oNxt.setHandler(m_oUiHandler);
		
		m_oSensorGatherer = new NXTSensorGatherer(this, m_oNxt);
		m_dblSpeed = m_oNxt.getBaseSped();

		m_oRemoteCtrl = new RemoteControlHelper(m_oActivity, m_oNxt, null);
        m_oRemoteCtrl.setProperties();

        updateButtons(false);
        setDebug(false);

        if (m_oNxt.isConnected()) {
			updateButtons(true);
		}
    }
    
    public void setNXT(NXT i_oNxt) {
    	m_oNxt = i_oNxt;
    	m_oNxt.setHandler(m_oUiHandler);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();

    	shutDown();
    }
    
    protected void shutDown() {
    	m_oSensorGatherer.stopThread();
    	
    	if (m_oNxt.isConnected() && !m_bKeepAlive) {
    		m_oNxt.disconnect();
    		m_oNxt.destroy();
    	}
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	
//    	m_oSensorGatherer.pauseThread();
    	
    	if (m_oNxt.isConnected() && !m_bKeepAlive) {
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
    	
    	if (m_strMacAddress != "" && !m_bKeepAlive) {
    		connectToRobot(m_oBTHelper.getRemoteDevice(m_strMacAddress));
    	}

//    	m_oSensorGatherer.resumeThread();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(GENERAL_GRP, DEBUG_ID, 2, "Debug");

		menu.add(REMOTE_CTRL_GRP, INVERT_ID, 3, "Invert Driving");
		menu.add(REMOTE_CTRL_GRP, ACCEL_ID, 4, "Accelerometer");
		menu.add(REMOTE_CTRL_GRP, ADVANCED_CONTROL_ID, 5, "Advanced Control");
		
		return true;
	}
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.setGroupVisible(REMOTE_CTRL_GRP, m_oRemoteCtrl.isControlEnabled());
    	
    	Utils.updateOnOffMenuItem(menu.findItem(ACCEL_ID), m_bAccelerometer);
    	Utils.updateOnOffMenuItem(menu.findItem(ADVANCED_CONTROL_ID), m_oRemoteCtrl.isAdvancedControl());
    	Utils.updateOnOffMenuItem(menu.findItem(DEBUG_ID), m_bDebug);
    	Utils.updateOnOffMenuItem(menu.findItem(INVERT_ID), m_oNxt.isInverted());
    	
		return true;
    }

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case DEBUG_ID:
			setDebug(!m_bDebug);
			return true;
		case INVERT_ID:
			m_oNxt.setInverted();
			return true;
		case ACCEL_ID:
			m_bAccelerometer = !m_bAccelerometer;

			if (m_bAccelerometer) {
				m_bSetAccelerometerBase = true;
			} else {
				m_oNxt.moveStop();
			}
		case ADVANCED_CONTROL_ID:
			m_oRemoteCtrl.toggleAdvancedControl();
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void disconnect() {
		m_oNxt.disconnect();
	}

	@Override
	public void connectToRobot(BluetoothDevice i_oDevice) {
		if (m_oBTHelper.initBluetooth()) {
			m_strMacAddress = i_oDevice.getAddress();
			showConnectingDialog();
			
			if (m_oNxt.isConnected()) {
				m_oNxt.disconnect();
			}

			m_oNxt.setConnection(new NXTBluetooth(i_oDevice, getResources()));
			m_oNxt.connect();
		}
	}
	
	public static void connectToNXT(final BaseActivity m_oOwner, NXT i_oNxt, BluetoothDevice i_oDevice, final ConnectListener i_oConnectListener) {
		NXTRobot m_oRobot = new NXTRobot(m_oOwner) {
			public void onConnect() {
				i_oConnectListener.onConnect(true);
			};
			public void onDisconnect() {
				i_oConnectListener.onConnect(false);
			};
		};
		
		m_oRobot.showConnectingDialog();
		
		if (i_oNxt.isConnected()) {
			i_oNxt.disconnect();
		}

		i_oNxt.setHandler(m_oRobot.getUIHandler());
		i_oNxt.setConnection(new NXTBluetooth(i_oDevice, m_oOwner.getResources()));
		i_oNxt.connect();
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
					m_oNxt.moveForward(speed, radius);
				} else if (radius < -RADIUS_SENSITIVITY) {
					m_oNxt.moveForward(speed, radius);
				} else {
					m_oNxt.moveForward(speed);
				}
			} else if (speed < -SPEED_SENSITIVITY) {
				// remove the speed_sensitivity again
//				speed += SPEED_SENSITIVITY;

				Log.i("Speeds", "speed=" + speed + ", radius=" + radius); 

				if (radius > RADIUS_SENSITIVITY) {
					m_oNxt.moveBackward(speed, radius);
				} else if (radius < -RADIUS_SENSITIVITY) {
					m_oNxt.moveBackward(speed, radius);
				} else {
					m_oNxt.moveBackward(speed);
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
					m_oNxt.moveStop();
				}
				
			}
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case RobotCalibration.ROBOT_CALIBRATION_RESULT:
			if (resultCode == RESULT_OK) {
				m_dblSpeed = data.getExtras().getDouble(RobotCalibration.CALIBRATED_SPEED);
				m_oNxt.setBaseSpeed(m_dblSpeed);
				showToast("Calibrated speed saved", Toast.LENGTH_SHORT);
			} else {
				showToast("Calibration discarded", Toast.LENGTH_SHORT);
			}
		}
	};
	
	public void onConnect() {
		connected = true;
		updateButtons(true);
	}
	
	@Override
	public void onDisconnect() {
		connected = false;
		updateButtons(false);
		m_oRemoteCtrl.resetLayout();
	}
	
	@Override
	public void handleUIMessage(Message msg) {
		super.handleUIMessage(msg);
		
		switch (msg.what) {
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
	
	@Override
	protected void setProperties(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.nxt_main);
        
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

    	m_cbSensor1 = (CheckBox) m_oActivity.findViewById(R.id.cbSensor1);
    	m_spSensor1Type = (Spinner) m_oActivity.findViewById(R.id.spSensor1Type);
    	m_cbSensor2 = (CheckBox) m_oActivity.findViewById(R.id.cbSensor2);
    	m_spSensor2Type = (Spinner) m_oActivity.findViewById(R.id.spSensor2Type);
    	m_cbSensor3 = (CheckBox) m_oActivity.findViewById(R.id.cbSensor3);
    	m_spSensor3Type = (Spinner) m_oActivity.findViewById(R.id.spSensor3Type);
    	m_cbSensor4 = (CheckBox) m_oActivity.findViewById(R.id.cbSensor4);
    	m_spSensor4Type = (Spinner) m_oActivity.findViewById(R.id.spSensor4Type);
    	m_cbMotor1 = (CheckBox) m_oActivity.findViewById(R.id.cbMotor1);
    	m_spMotor1Type = (Spinner) m_oActivity.findViewById(R.id.spMotor1Type);
    	m_btnMotor1Reset = (Button) m_oActivity.findViewById(R.id.btnMotor1Reset);
    	m_cbMotor2 = (CheckBox) m_oActivity.findViewById(R.id.cbMotor2);
    	m_spMotor2Type = (Spinner) m_oActivity.findViewById(R.id.spMotor2Type);
    	m_btnMotor2Reset = (Button) m_oActivity.findViewById(R.id.btnMotor2Reset);
    	m_cbMotor3 = (CheckBox) m_oActivity.findViewById(R.id.cbMotor3);
    	m_spMotor3Type = (Spinner) m_oActivity.findViewById(R.id.spMotor3Type);
    	m_btnMotor3Reset = (Button) m_oActivity.findViewById(R.id.btnMotor3Reset);
    	
		m_btnCalibrate = (Button) m_oActivity.findViewById(R.id.btnCalibrate);
		m_btnCalibrate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				int nIndex = RobotInventory.getInstance().findRobot(m_oNxt);
				if (nIndex == -1) {
					nIndex = RobotInventory.getInstance().addRobot(m_oNxt);
				}
				m_bKeepAlive = true;
				RobotCalibration.createAndShow(m_oActivity, RobotType.RBT_NXT, nIndex, m_dblSpeed);
			}
		});
	}

	public void updateButtons(boolean enabled) {
		m_oRemoteCtrl.updateButtons(enabled);
		
		m_btnCalibrate.setEnabled(enabled);
		m_cbSensor1.setEnabled(enabled);
		m_spSensor1Type.setEnabled(enabled);
		m_cbSensor2.setEnabled(enabled);
		m_spSensor2Type.setEnabled(enabled);
		m_cbSensor3.setEnabled(enabled);
		m_spSensor3Type.setEnabled(enabled);
		m_cbSensor4.setEnabled(enabled);
		m_spSensor4Type.setEnabled(enabled);
		m_cbMotor1.setEnabled(enabled);
		m_spMotor1Type.setEnabled(enabled);
		m_btnMotor1Reset.setEnabled(enabled);
		m_cbMotor2.setEnabled(enabled);
		m_spMotor2Type.setEnabled(enabled);
		m_btnMotor2Reset.setEnabled(enabled);
		m_cbMotor3.setEnabled(enabled);
		m_spMotor3Type.setEnabled(enabled);
		m_btnMotor3Reset.setEnabled(enabled);
	}
	
	public void resetLayout() {
		m_oRemoteCtrl.resetLayout();
		
		m_cbSensor1.setChecked(false);
		m_spSensor1Type.setSelection(0);
		
		m_cbSensor2.setChecked(false);
		m_spSensor2Type.setSelection(0);
		
		m_cbSensor3.setChecked(false);
		m_spSensor3Type.setSelection(0);
		
		m_cbSensor4.setChecked(false);
		m_spSensor4Type.setSelection(0);
		
		m_cbMotor1.setChecked(false);
		m_spMotor1Type.setSelection(0);
		
		m_cbMotor2.setChecked(false);
		m_spMotor2Type.setSelection(0);
		
		m_cbMotor3.setChecked(false);
		m_spMotor3Type.setSelection(0);

		m_oSensorGatherer.initialize();
	}

	public void setDebug(boolean i_bDebug) {
		
		m_bDebug = i_bDebug;
		
		m_oSensorGatherer.setDebug(i_bDebug);
		
		// create a temporary layout from the nxt layout
		View oTempView = LayoutInflater.from(m_oActivity).inflate(R.layout.nxt_main, null);
		
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
	
	public static String getMacFilter() {
		return NXTTypes.MAC_FILTER;
	}

}
