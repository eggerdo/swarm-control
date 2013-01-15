package org.dobots.swarmcontrol.robots.ac13;

import org.dobots.robots.ac13.AC13Rover;
import org.dobots.robots.ac13.AC13RoverTypes.VideoResolution;
import org.dobots.swarmcontrol.BaseActivity;
import org.dobots.swarmcontrol.IConnectListener;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.RemoteControlHelper;
import org.dobots.swarmcontrol.RobotInventory;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.swarmcontrol.robots.WifiRobot;
import org.dobots.utility.Utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public class AC13RoverRobot extends WifiRobot {

	private static final String TAG = "AC13Rover";

	private static final int DIALOG_SETTINGS_ID = 1;
	
	private static final int ACCEL_ID = CONNECT_ID + 1;
	private static final int ADVANCED_CONTROL_ID = ACCEL_ID + 1;
	private static final int VIDEO_ID = ADVANCED_CONTROL_ID + 1;
	private static final int VIDEO_SETTINGS_ID = VIDEO_ID + 1;

	private static final int REMOTE_CTRL_GRP = GENERAL_GRP + 1;
	private static final int SENSOR_GRP = REMOTE_CTRL_GRP + 1;
	private static final int VIDEO_GRP = SENSOR_GRP + 1	;
	
	private AC13Rover m_oRover;
	
	private AC13RoverSensorGatherer m_oSensorGatherer;

	private RemoteControlHelper m_oRemoteCtrl;

	private boolean connected;
	private double m_dblSpeed;

	private ToggleButton m_btnInfrared;

	private AlertDialog m_dlgSettingsDialog;

	public AC13RoverRobot(BaseActivity i_oOwner) {
		super(i_oOwner);
	}
	
	public AC13RoverRobot() {
		super();
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	// TODO make generic
    	int nIndex = (Integer) getIntent().getExtras().get("InventoryIndex");
    	if (nIndex == -1) {
    		m_oRover = new AC13Rover();
	        connectToRobot();
    	} else {
    		m_oRover = (AC13Rover) RobotInventory.getInstance().getRobot(nIndex);
    		m_bKeepAlive = true;
    	}
    	m_oRover.setHandler(m_oUiHandler);
		
		m_oSensorGatherer = new AC13RoverSensorGatherer(this, m_oRover);
		m_dblSpeed = m_oRover.getBaseSped();

		m_oRemoteCtrl = new RemoteControlHelper(m_oActivity, m_oRover, null);
        m_oRemoteCtrl.setProperties();

        updateButtons(false);

        if (m_oRover.isConnected()) {
			updateButtons(true);
		}
        
    }

	@Override
	protected void setProperties(RobotType i_eRobot) {
    	m_oActivity.setContentView(R.layout.ac13rover_main);
    	
    	m_btnInfrared = (ToggleButton) m_oActivity.findViewById(R.id.btnInfrared);
    	m_btnInfrared.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oRover.switchInfrared();
			}
		});
    	
	}

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	shutDown();
    }

	@Override
	protected void shutDown() {
		m_oSensorGatherer.stopThread();

    	if (m_oRover.isConnected() && !m_bKeepAlive) {
    		m_oRover.destroy();
    	}
	}

    @Override
    public void onStop() {
    	super.onStop();
    	
    	if (m_oRover.isStreaming()) {
    		m_oRover.stopStreaming();
    	}
    	
    	if (m_oRover.isConnected() && !m_bKeepAlive) {
    		m_oRover.disconnect();
    	}
    }

    @Override
    public void onRestart() {
    	super.onRestart();
    	
    	if (m_strAddress != "" && !m_bKeepAlive) {
    		connectToRobot();
    	}

    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(REMOTE_CTRL_GRP, ACCEL_ID, ACCEL_ID, "Accelerometer");
		menu.add(REMOTE_CTRL_GRP, ADVANCED_CONTROL_ID, ADVANCED_CONTROL_ID, "Advanced Control");
		
		menu.add(SENSOR_GRP, VIDEO_ID, VIDEO_ID, "Video");

		menu.add(VIDEO_GRP, VIDEO_SETTINGS_ID, VIDEO_SETTINGS_ID, "Video Settings");

		return true;
	}
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.setGroupVisible(REMOTE_CTRL_GRP, m_oRover.isConnected() && m_oRemoteCtrl.isControlEnabled());
    	menu.setGroupVisible(SENSOR_GRP, m_oRover.isConnected());
    	menu.setGroupVisible(VIDEO_GRP, m_oRover.isConnected() && m_oRover.isStreaming());
    	
    	Utils.updateOnOffMenuItem(menu.findItem(ACCEL_ID), m_bAccelerometer);
    	Utils.updateOnOffMenuItem(menu.findItem(ADVANCED_CONTROL_ID), m_oRemoteCtrl.isAdvancedControl());
    	Utils.updateOnOffMenuItem(menu.findItem(VIDEO_ID), m_oRover.isStreaming());
    	
		return true;
    }

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case VIDEO_SETTINGS_ID:
    		showDialog(DIALOG_SETTINGS_ID);
    		break;
		case ACCEL_ID:
			m_bAccelerometer = !m_bAccelerometer;

			if (m_bAccelerometer) {
				m_bSetAccelerometerBase = true;
			} else {
				m_oRover.moveStop();
			}
			break;
		case ADVANCED_CONTROL_ID:
			m_oRemoteCtrl.toggleAdvancedControl();
			break;
		case VIDEO_ID:
			m_oSensorGatherer.setVideoEnabled(!m_oRover.isStreaming());
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onConnect() {
		connected = true;
		updateButtons(true);
		m_oSensorGatherer.onConnect();
	}

	@Override
	protected void onDisconnect() {
		connected = false;
		updateButtons(false);
		m_oRemoteCtrl.resetLayout();
	}

	@Override
	protected void connect() {
		m_oRover.connect();
	}

	public static void connectToAC13Rover(final BaseActivity m_oOwner, AC13Rover i_oRover, final IConnectListener i_oConnectListener) {
		AC13RoverRobot m_oRobot = new AC13RoverRobot(m_oOwner) {
			public void onConnect() {
				i_oConnectListener.onConnect(true);
			};
			public void onDisconnect() {
				i_oConnectListener.onConnect(false);
			};
		};
		
		m_oRobot.showConnectingDialog();
		
		if (i_oRover.isConnected()) {
			i_oRover.disconnect();
		}

		i_oRover.setHandler(m_oRobot.getUIHandler());
		i_oRover.connect();
	}

	@Override
	protected void disconnect() {
		m_oRover.disconnect();
	}

	@Override
	protected void resetLayout() {
		m_oRemoteCtrl.resetLayout();
		
		m_oSensorGatherer.resetLayout();
	}

	@Override
	protected void updateButtons(boolean i_bEnabled) {
		m_oRemoteCtrl.updateButtons(i_bEnabled);
		
		m_btnInfrared.setEnabled(i_bEnabled);
	}

    /**
     * This is called when a dialog is created for the first time.  The given
     * "id" is the same value that is passed to showDialog().
     */
    @Override
    protected Dialog onCreateDialog(int id) {
    	LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    	View layout;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	switch (id) {
    	case DIALOG_SETTINGS_ID:
        	layout = inflater.inflate(R.layout.ac13rover_videosettings, null);
        	builder.setTitle("Video Resolution");
        	builder.setView(layout);
        	builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface arg0, int arg1) {
    				adjustVideoResolution();
    			}
    		});
        	m_dlgSettingsDialog = builder.create();
        	return m_dlgSettingsDialog;
    	}
    	return null;
    }

    /**
     * This is called each time a dialog is shown.
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	if (id == DIALOG_SETTINGS_ID) {
    		switch (m_oRover.getResolution()) {
    		case res_320x240:
    			((RadioButton) dialog.findViewById(R.id.rb320x240)).setChecked(true);
    			break;
    		case res_640x480:
    			((RadioButton) dialog.findViewById(R.id.rb640x480)).setChecked(true);
    			break;
    		default:
    			((RadioGroup) dialog.findViewById(R.id.rgVideoResolution)).clearCheck();
    			break;
    		}
    	}
    }
    
    private void adjustVideoResolution() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// in order for the resolution change to take effect we need to disconnect
				// and reconnect to the robot.
		    	disconnect();
		    	// wait for a moment to give the threads time to shut down
		    	Utils.waitSomeTime(500);
		    	// because the settings are changed over http requests we don't need to have
		    	// the tcp sockets connected in order to change the settings!
		    	switch (((RadioGroup) m_dlgSettingsDialog.findViewById(R.id.rgVideoResolution)).getCheckedRadioButtonId()) {
		    	case R.id.rb320x240:
		    		m_oSensorGatherer.setResolution(VideoResolution.res_320x240);
		    		break;
		    	case R.id.rb640x480:
		    		m_oSensorGatherer.setResolution(VideoResolution.res_640x480);
		    		break;
		    	}
		    	// connect again to receive the new video stream
		    	connect();
			}
		}).start();
    }
    
}
