package org.dobots.swarmcontrol.robots.piratedotty;

import java.io.IOException;

import org.dobots.robots.piratedotty.PirateDotty;
import org.dobots.robots.piratedotty.PirateDottyTypes;
import org.dobots.swarmcontrol.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;
import org.dobots.utilities.log.AndroidLogListener;

import robots.RobotType;
import robots.ctrl.RemoteControlHelper;
import robots.gui.BluetoothRobot;
import robots.gui.IConnectListener;
import robots.gui.RobotDriveCommandListener;
import robots.gui.SensorGatherer;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class PirateDottyRobot extends BluetoothRobot {

	private static String TAG = "PirateDotty";
	
	private static final int CONNECT_ID = Menu.FIRST;
	private static final int ACCEL_ID = CONNECT_ID + 1;
	
	private static final int REMOTE_CTRL_GRP = GENERAL_GRP + 1;
	
	private PirateDotty m_oPirateDotty;

	private PirateDottySensorGatherer m_oSensorGatherer;

	private RemoteControlHelper m_oRemoteCtrl;

//	private boolean m_bStreaming = false;
	
//	private Button m_btnStreaming;
	
//	private CheckBox m_cbAll;
//	private CheckBox m_cbDistance;
//	private CheckBox m_cbLight;
//	private CheckBox m_cbSound;
//	private CheckBox m_cbBattery;
//	private CheckBox m_cbMotor1;
//	private CheckBox m_cbMotor2;
//	private CheckBox m_cbWheel1;
//	private CheckBox m_cbWheel2;
//	private CheckBox m_cbLed1;
//	private CheckBox m_cbLed2;
//	private CheckBox m_cbLed3;

//	private EditText m_edtInterval;
	
	private double m_dblSpeed;

	private RobotDriveCommandListener m_oRemoteListener;

	public PirateDottyRobot(BaseActivity i_oOwner) {
		super(i_oOwner);
	}
	
	public PirateDottyRobot() {
		super();
	}
	
	protected SensorGatherer getSensorGatherer() {
		return m_oSensorGatherer;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        
    	m_oPirateDotty = (PirateDotty) getRobot();
    	m_oPirateDotty.setHandler(m_oUiHandler);
    	m_oPirateDotty.setLogListener(new AndroidLogListener());

    	m_oSensorGatherer = new PirateDottySensorGatherer(m_oActivity, m_oPirateDotty);
		m_dblSpeed = m_oPirateDotty.getBaseSped();

		m_oRemoteListener = new RobotDriveCommandListener(m_oPirateDotty);
		m_oRemoteCtrl = new RemoteControlHelper(m_oActivity);
		m_oRemoteCtrl.setDriveControlListener(m_oRemoteListener);

        updateButtons(false);
        
        if (m_oPirateDotty.isConnected()) {
			updateButtons(true);
	        m_oRemoteCtrl.enableRobotControl(true);
		} else {
			connectToRobot();
		}
    }
    
    public void setNXT(PirateDotty i_oNxt) {
    	m_oPirateDotty = i_oNxt;
    	m_oPirateDotty.setHandler(m_oUiHandler);
    }
	
	@Override
	public void handleUIMessage(Message msg) {
		super.handleUIMessage(msg);
		
		switch (msg.what) {
		case PirateDottyTypes.SENSOR_DATA:
			m_oSensorGatherer.sendMessage(PirateDottyTypes.SENSOR_DATA, msg.obj);
			break;
		}
	}
	
    @Override
	protected void setProperties(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.piratedotty_main);

//        m_cbDistance = (CheckBox) m_oActivity.findViewById(R.id.cbPirateDotty_Distance);
//        m_cbLight = (CheckBox) m_oActivity.findViewById(R.id.cbPirateDotty_Light);
//        m_cbSound = (CheckBox) m_oActivity.findViewById(R.id.cbPirateDotty_Sound);
//        m_cbBattery = (CheckBox) m_oActivity.findViewById(R.id.cbPirateDotty_Battery);
//        m_cbMotor1 = (CheckBox) m_oActivity.findViewById(R.id.cbPirateDotty_MotorSensor1);
//        m_cbMotor2 = (CheckBox) m_oActivity.findViewById(R.id.cbPirateDotty_MotorSensor2);
//        m_cbWheel1 = (CheckBox) m_oActivity.findViewById(R.id.cbPirateDotty_Wheel1);
//        m_cbWheel2 = (CheckBox) m_oActivity.findViewById(R.id.cbPirateDotty_Wheel2);
//        m_cbLed1 = (CheckBox) m_oActivity.findViewById(R.id.cbPirateDotty_Led1);
//        m_cbLed2 = (CheckBox) m_oActivity.findViewById(R.id.cbPirateDotty_Led2);
//        m_cbLed3 = (CheckBox) m_oActivity.findViewById(R.id.cbPirateDotty_Led3);
        
//        CheckBox cbSensor;
//        for (EPirateDottySensors eSensor : EPirateDottySensors.values()) {
//        	switch(eSensor) {
//        	case sensor_Battery:
//        		cbSensor = m_cbBattery;
//        		break;
//        	case sensor_Dist:
//        		cbSensor = m_cbDistance;
//        		break;
//        	case sensor_Light:
//        		cbSensor = m_cbLight;
//        		break;
//        	case sensor_Motor1:
//        		cbSensor = m_cbMotor1;
//        		break;
//        	case sensor_Motor2:
//        		cbSensor = m_cbMotor2;
//        		break;
//        	case sensor_Sound:
//        		cbSensor = m_cbSound;
//        		break;
//			case sensor_Wheel1:
//				cbSensor = m_cbWheel1;
//				break;
//			case sensor_Wheel2:
//				cbSensor = m_cbWheel2;
//				break;
//			case sensor_Led1:
//				cbSensor = m_cbLed1;
//				break;
//			case sensor_Led2:
//				cbSensor = m_cbLed2;
//				break;
//			case sensor_Led3:
//				cbSensor = m_cbLed3;
//				break;
//    		default:
//    			continue;
//        	}
//        	
//        	cbSensor.setTag(eSensor);
//        	cbSensor.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//    			
//    			@Override
//    			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//    				// get the sensor id from the view object
//    				m_oSensorGatherer.enableSensor((EPirateDottySensors)buttonView.getTag(), isChecked);
//    			}
//    		});
//        }
//        
//        m_cbAll = (CheckBox) m_oActivity.findViewById(R.id.cbPirateDotty_All);
//        m_cbAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//		        m_cbDistance.setChecked(isChecked);
//		        m_cbLight.setChecked(isChecked);
//		        m_cbSound.setChecked(isChecked);
//		        m_cbBattery.setChecked(isChecked);
//		        m_cbMotor1.setChecked(isChecked);
//		        m_cbMotor2.setChecked(isChecked);
//		        m_cbWheel1.setChecked(isChecked);
//		        m_cbWheel2.setChecked(isChecked);
//		        m_cbLed1.setChecked(isChecked);
//		        m_cbLed2.setChecked(isChecked);
//		        m_cbLed3.setChecked(isChecked);
//			}
//		});
//		
//		m_edtInterval = (EditText) m_oActivity.findViewById(R.id.edtInterval);
//		m_edtInterval.setText(Integer.toString(PirateDottyTypes.DEFAULT_SENSOR_INTERVAL));
//		
//		m_btnStreaming = (Button) m_oActivity.findViewById(R.id.btnStreaming);
//		m_btnStreaming.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if (!m_bStreaming) {
//					int nInterval = Integer.parseInt(m_edtInterval.getText().toString());
//					if (nInterval >= PirateDottyTypes.MIN_SENSOR_INTERVAL) {
//						m_oPirateDotty.startStreaming(nInterval);
//					} else {
//						showToast(String.format("Error: Minimun Value is %d!", PirateDottyTypes.MIN_SENSOR_INTERVAL), Toast.LENGTH_SHORT);
//						return;
//					}
//				} else {
//					m_oPirateDotty.stopStreaming();
//				}
//				m_bStreaming = !m_bStreaming;
//				m_btnStreaming.setText("Streaming: " + (m_bStreaming ? "ON" : "OFF"));
//			}
//		});
		
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(REMOTE_CTRL_GRP, ACCEL_ID, 3, "Accelerometer");
		
		return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	
    	menu.setGroupVisible(REMOTE_CTRL_GRP, m_oRemoteCtrl.isControlEnabled());

    	Utils.updateOnOffMenuItem(menu.findItem(ACCEL_ID), m_bAccelerometer);

    	return true;
    }
    
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case ACCEL_ID:
			m_bAccelerometer = !m_bAccelerometer;

			if (m_bAccelerometer) {
				m_bSetAccelerometerBase = true;
			} else {
				m_oPirateDotty.moveStop();
			}
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	protected void resetLayout() {
//		m_cbAll.setChecked(false);
//        m_cbDistance.setChecked(false);
//        m_cbLight.setChecked(false);
//        m_cbSound.setChecked(false);
//        m_cbBattery.setChecked(false);
//        m_cbMotor1.setChecked(false);
//        m_cbMotor2.setChecked(false);
//        m_cbWheel1.setChecked(false);
//        m_cbWheel2.setChecked(false);
//        m_cbLed1.setChecked(false);
//        m_cbLed2.setChecked(false);
//        m_cbLed3.setChecked(false);
        
        m_oRemoteCtrl.resetLayout();
        
//        m_btnStreaming.setText("Streaming: OFF");
//        m_bStreaming = false;
        
        updateButtons(false);

		m_oSensorGatherer.initialize();
	}
	
	public void updateButtons(boolean enabled) {
		m_oRemoteCtrl.setControlEnabled(enabled);
		
//		m_btnStreaming.setEnabled(enabled);
//		m_edtInterval.setEnabled(enabled);
//		
//		m_cbAll.setEnabled(enabled);
//		m_cbDistance.setEnabled(enabled);
//		m_cbBattery.setEnabled(enabled);
//		m_cbLight.setEnabled(enabled);
//		m_cbMotor1.setEnabled(enabled);
//		m_cbMotor2.setEnabled(enabled);
//		m_cbSound.setEnabled(enabled);
//        m_cbWheel1.setEnabled(enabled);
//        m_cbWheel2.setEnabled(enabled);
//        m_cbLed1.setEnabled(enabled);
//        m_cbLed2.setEnabled(enabled);
//        m_cbLed3.setEnabled(enabled);
	}

	@Override
	protected void onConnect() {
		updateButtons(true);
        m_oRemoteCtrl.setRemoteControl(true);
	}
	
	@Override
	protected void onDisconnect() {
		updateButtons(false);
		m_oRemoteCtrl.resetLayout();
	}

	@Override
	protected void disconnect() {
		m_oPirateDotty.disconnect();
	}
	
	@Override
	public void connect(BluetoothDevice i_oDevice) {
//		if (m_oBTHelper.initBluetooth()) {
			m_strAddress = i_oDevice.getAddress();
			showConnectingDialog();
			
			if (m_oPirateDotty.getConnection() != null) {
				try {
					m_oPirateDotty.getConnection().destroyConnection();
				}
				catch (IOException e) { }
			}
			m_oPirateDotty.setConnection(new PirateDottyBluetooth(i_oDevice));
			m_oPirateDotty.connect();
//		}
	}

	public static void connectToPirateDotty(final BaseActivity m_oOwner, PirateDotty i_oPirateDotty, BluetoothDevice i_oDevice, final IConnectListener i_oConnectListener) {
		PirateDottyRobot m_oRobot = new PirateDottyRobot(m_oOwner) {
			public void onConnect() {
				i_oConnectListener.onConnect(true);
			};
			public void onDisconnect() {
				i_oConnectListener.onConnect(false);
			};
		};
		
		m_oRobot.showConnectingDialog();
		
		if (i_oPirateDotty.isConnected()) {
			i_oPirateDotty.disconnect();
		}

		i_oPirateDotty.setHandler(m_oRobot.getUIHandler());
		i_oPirateDotty.setConnection(new PirateDottyBluetooth(i_oDevice));
		i_oPirateDotty.connect();
	}

}
