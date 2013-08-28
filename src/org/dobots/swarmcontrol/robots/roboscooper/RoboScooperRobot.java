package org.dobots.swarmcontrol.robots.roboscooper;

import org.dobots.robots.BrainlinkDevice;
import org.dobots.robots.BrainlinkDevice.BrainlinkSensors;
import org.dobots.robots.roboscooper.RoboScooper;
import org.dobots.robots.roboscooper.RoboScooperMessageTypes;
import org.dobots.robots.roboscooper.RoboScooperTypes;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.BluetoothRobot;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;

import robots.RobotType;
import robots.ctrl.IRemoteControlListener;
import robots.ctrl.RemoteControlHelper;
import robots.ctrl.RemoteControlHelper.Move;
import robots.gui.IConnectListener;
import robots.gui.RobotRemoteListener;
import robots.gui.SensorGatherer;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Toast;

public class RoboScooperRobot extends BluetoothRobot {
	
	private static String TAG = "RoboScooper";
	
	private static final int ACCEL_ID = CONNECT_ID + 1;
	
	private static final int REMOTE_CTRL_GRP = GENERAL_GRP + 1;

	private boolean connected;
	
	private RoboScooper m_oRoboScooper;

	private RoboScooperSensorGatherer m_oSensorGatherer;

	private RemoteControlHelper m_oRemoteCtrl;

	private Button m_btnTalkMode;
	private Button m_btnWhackMode;
	private Button m_btnVision;
	private Button m_btnStopMode;
	private Button m_btnCleanSweepMode;
	private Button m_btnPickUpMode;
	private Button m_btnPickup;
	private Button m_btnDump;
	
	private CheckBox m_cbAccelerometer;
	private CheckBox m_cbLight;
	private CheckBox m_cbBattery;
	
	private LinearLayout m_layControls;
	private LinearLayout m_layPlayModes;

	private RobotRemoteListener m_oRemoteListener;

	
	public RoboScooperRobot(BaseActivity m_oOwner) {
		super(m_oOwner);
	}
	
	public RoboScooperRobot() {
		super();
	}

	protected SensorGatherer getSensorGatherer() {
		return m_oSensorGatherer;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	m_oRoboScooper = (RoboScooper) getRobot();
    	m_oRoboScooper.setHandler(m_oUiHandler);
		
		m_oSensorGatherer = new RoboScooperSensorGatherer(this, m_oRoboScooper);

		m_oRemoteListener = new RobotRemoteListener(m_oRoboScooper) {
			
			@Override
			public void enableControl(boolean i_bEnable) {
				super.enableControl(i_bEnable);
				
				// we also need to update buttons
				Utils.showLayout(m_layControls, i_bEnable);
				Utils.showLayout(m_layPlayModes, i_bEnable);
			}
		};
		m_oRemoteCtrl = new RemoteControlHelper(m_oActivity, m_oRemoteListener);

        updateButtons(false);

        if (m_oRoboScooper.isConnected()) {
			updateButtons(true);
		} else {
			connectToRobot();
		}
        
        if (!BrainlinkDevice.checkForConfigFile(getResources(), RoboScooperTypes.SIGNAL_FILE_NAME, RoboScooperTypes.SIGNAL_FILE_ENCODED)) {
        	showToast("Failed to install device config file", Toast.LENGTH_LONG);
        }
    }

	@Override
	protected void setProperties(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.roboscooper_main);

        m_layControls = (LinearLayout) findViewById(R.id.layControls);
        m_layPlayModes = (LinearLayout) findViewById(R.id.layPlayModes);
     
    	m_btnTalkMode = (Button) findViewById(R.id.btnTalkMode);
    	m_btnTalkMode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oRoboScooper.setTalkMode();
			}
		});
    	
    	m_btnWhackMode = (Button) findViewById(R.id.btnWhackMode);
    	m_btnWhackMode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oRoboScooper.setWhackMode();
			}
		});
    	
    	m_btnVision = (Button) findViewById(R.id.btnVision);
    	m_btnVision.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oRoboScooper.setVision();
			}
		});
    	
    	m_btnStopMode = (Button) findViewById(R.id.btnStopMode);
    	m_btnStopMode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oRoboScooper.stop();
			}
		});
    	
    	m_btnCleanSweepMode = (Button) findViewById(R.id.btnCleanSweepMode);
    	m_btnCleanSweepMode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oRoboScooper.setCleanSweepMode();
			}
		});

    	m_btnPickUpMode = (Button) findViewById(R.id.btnPickUpMode);
    	m_btnPickUpMode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oRoboScooper.setPickUpMode();
			}
		});
    	
    	m_btnPickup = (Button) findViewById(R.id.btnPickUp);
    	m_btnPickup.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oRoboScooper.pickUp();
			}
		});
    	
    	m_btnDump = (Button) findViewById(R.id.btnDump);
    	m_btnDump.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oRoboScooper.dump();
			}
		});
    	
    	m_cbAccelerometer = (CheckBox) findViewById(R.id.cbAccelerometer);
    	m_cbAccelerometer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				m_oSensorGatherer.enableSensor(BrainlinkSensors.ACCELEROMETER, isChecked);
			}
    	});
    	
    	m_cbLight = (CheckBox) findViewById(R.id.cbLight);
    	m_cbLight.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				m_oSensorGatherer.enableSensor(BrainlinkSensors.LIGHT, isChecked);
			}
    	});
    	
    	m_cbBattery = (CheckBox) findViewById(R.id.cbBattery);
    	m_cbBattery.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				m_oSensorGatherer.enableSensor(BrainlinkSensors.BATTERY, isChecked);
			}
    	});
    	
	}
	
	protected void handleUIMessage(Message msg) {
		switch (msg.what) {
		case RoboScooperMessageTypes.INITIALISATION_FAILED:
			showToast("Brainlink initialisation failed, make sure that the signal definition file was copied to ...", Toast.LENGTH_LONG);
			updateButtons(false);
			break;
		}
		
		super.handleUIMessage(msg);
	}

	public void resetLayout() {
		m_oRemoteCtrl.resetLayout();
		
		m_cbAccelerometer.setChecked(false);
		m_cbBattery.setChecked(false);
		m_cbLight.setChecked(false);

		Utils.showLayout(m_layControls, false);
		Utils.showLayout(m_layPlayModes, false);

		m_oSensorGatherer.initialize();
	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(REMOTE_CTRL_GRP, ACCEL_ID, 4, "Accelerometer");
		
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
				m_oRoboScooper.moveStop();
			}
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void disconnect() {
		if (m_oRoboScooper.isConnected()) {
			m_oRoboScooper.disconnect();
		}
	}

	@Override
	public void connect(BluetoothDevice i_oDevice) {
//		if (m_oBTHelper.initBluetooth()) {
			m_strAddress = i_oDevice.getAddress();
			showConnectingDialog();
			
			if (m_oRoboScooper.isConnected()) {
				m_oRoboScooper.disconnect();
			}

			m_oRoboScooper.setConnection(i_oDevice);
			m_oRoboScooper.connect();
//		}
	}

	public static void connectToRoboScooper(final BaseActivity m_oOwner, RoboScooper i_oRoboScooper, BluetoothDevice i_oDevice, final IConnectListener i_oConnectListener) {
		RoboScooperRobot m_oRobot = new RoboScooperRobot(m_oOwner) {
			public void onConnect() {
				i_oConnectListener.onConnect(true);
			};
			public void onDisconnect() {
				i_oConnectListener.onConnect(false);
			};
		};
		m_oRobot.showConnectingDialog();
		
		if (i_oRoboScooper.isConnected()) {
			i_oRoboScooper.disconnect();
		}
		
		i_oRoboScooper.setConnection(i_oDevice);
		i_oRoboScooper.connect();
		i_oRoboScooper.setHandler(m_oRobot.getUIHandler());
	}

	@Override
	protected void onConnect() {
		connected = true;
		updateButtons(true);
	}

	@Override
	protected void onDisconnect() {
		connected = false;
		updateButtons(false);
		m_oRemoteCtrl.resetLayout();
		m_oSensorGatherer.initialize();
	}

	@Override
	protected void updateButtons(boolean i_bEnabled) {
		m_oRemoteCtrl.setControlEnabled(i_bEnabled);
		
		m_btnCleanSweepMode.setEnabled(i_bEnabled);
		m_btnPickUpMode.setEnabled(i_bEnabled);
		m_btnTalkMode.setEnabled(i_bEnabled);
		m_btnWhackMode.setEnabled(i_bEnabled);
		
		m_btnDump.setEnabled(i_bEnabled);
		m_btnPickup.setEnabled(i_bEnabled);
		m_btnStopMode.setEnabled(i_bEnabled);
		m_btnVision.setEnabled(i_bEnabled);
		
		m_cbAccelerometer.setEnabled(i_bEnabled);
		m_cbBattery.setEnabled(i_bEnabled);
		m_cbLight.setEnabled(i_bEnabled);
	}

}
