package org.dobots.swarmcontrol.robots.robo40;

import java.io.IOException;
import java.util.Arrays;

import org.dobots.robots.msg.MsgTypes.RawDataMsg;
import org.dobots.robots.robo40.Robo40;
import org.dobots.robots.robo40.Robo40Types;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.BluetoothRobot;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;

import robots.RobotType;
import robots.ctrl.RemoteControlHelper;
import robots.gui.IConnectListener;
import robots.gui.RobotDriveCommandListener;
import robots.gui.SensorGatherer;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Robo40Robot extends BluetoothRobot {

	private static String TAG = "Robo40";
	
	private static final int CONNECT_ID = Menu.FIRST;
	private static final int ACCEL_ID = CONNECT_ID + 1;
	
	private static final int REMOTE_CTRL_GRP = GENERAL_GRP + 1;
	
	private Robo40 m_oRobo40;

	private Robo40SensorGatherer m_oSensorGatherer;

	private RemoteControlHelper m_oRemoteCtrl;

	private double m_dblSpeed;

	private SeekBar m_sbMotor1;

	private SeekBar m_sbMotor2;

	private SeekBar m_sbMotor3;

	private TextView m_edtMotor1;
	private TextView m_edtMotor2;
	private TextView m_edtMotor3;

	private RobotDriveCommandListener m_oRemoteListener;

	public Robo40Robot(BaseActivity i_oOwner) {
		super(i_oOwner);
	}
	
	public Robo40Robot() {
		super();
	}

	protected SensorGatherer getSensorGatherer() {
		return m_oSensorGatherer;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        
    	m_oRobo40 = (Robo40) getRobot();
    	m_oRobo40.setHandler(m_oUiHandler);
		
		m_oSensorGatherer = new Robo40SensorGatherer(m_oActivity, m_oRobo40);
		m_dblSpeed = m_oRobo40.getBaseSped();

		m_oRemoteListener = new RobotDriveCommandListener(m_oRobo40);
		m_oRemoteCtrl = new RemoteControlHelper(m_oActivity);
		m_oRemoteCtrl.setDriveControlListener(m_oRemoteListener);

        updateButtons(false);
        
        if (m_oRobo40.isConnected()) {
			updateButtons(true);
		} else {
			connectToRobot();
		}
    }
    	
	@Override
	public void handleUIMessage(Message msg) {
		super.handleUIMessage(msg);
		
		switch (msg.what) {
		case Robo40Types.SENSOR_DATA:
			m_oSensorGatherer.sendMessage(Robo40Types.SENSOR_DATA, msg.obj);
			break;
		case Robo40Types.LOGGING:
			byte[] message = ((RawDataMsg)msg.obj).rgbyRawData;
			Log.i(TAG, String.format("%s", new String(Arrays.copyOfRange(message, 2, message.length))));
			break;
		}
	}
	
    @Override
	protected void setProperties(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.robo40_main);
        
        m_edtMotor1 = (TextView) findViewById(R.id.edtRobo40Motor1);
        m_edtMotor1.setText("0");
        m_edtMotor2 = (TextView) findViewById(R.id.edtRobo40Motor2);
        m_edtMotor2.setText("0");
        m_edtMotor3 = (TextView) findViewById(R.id.edtRobo40Motor3); 
        m_edtMotor3.setText("0");

        m_sbMotor1 = (SeekBar) findViewById(R.id.sbRobo40Motor1);
        m_sbMotor1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress();
				updateMotor(1, progress);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				updateMotor(1, progress);
			}
		});
        
        m_sbMotor2 = (SeekBar) findViewById(R.id.sbRobo40Motor2);
        m_sbMotor2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress();
				updateMotor(2, progress);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				updateMotor(2, progress);
			}
		});

        m_sbMotor3 = (SeekBar) findViewById(R.id.sbRobo40Motor3);
        m_sbMotor3.setEnabled(false);
        
    }

    long last_time = 0;
    private void updateMotor(int id, int progress) {
    	int direction = 0;
		m_edtMotor1.setText(Integer.toString(progress - 255));
		if (progress >= 255) {
			direction = 1;
		} else {
			direction = 0;
		}
		long this_time = System.currentTimeMillis();
		if (this_time - last_time > 1000) {
			m_oRobo40.setMotor(id, direction, progress - 255);
			last_time = this_time;
		}
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
				m_oRobo40.moveStop();
			}
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	protected void resetLayout() {
        m_oRemoteCtrl.resetLayout();

        updateButtons(false);

		m_oSensorGatherer.initialize();
	}
	
	public void updateButtons(boolean enabled) {
		m_oRemoteCtrl.setControlEnabled(enabled);
	}

	@Override
	protected void onConnect() {
		updateButtons(true);
	}
	
	@Override
	protected void onDisconnect() {
		updateButtons(false);
		m_oRemoteCtrl.resetLayout();
	}

	@Override
	protected void disconnect() {
		m_oRobo40.disconnect();
	}
	
	@Override
	public void connect(BluetoothDevice i_oDevice) {
//		if (m_oBTHelper.initBluetooth()) {
			m_strAddress = i_oDevice.getAddress();
			showConnectingDialog();
			
			if (m_oRobo40.getConnection() != null) {
				try {
					m_oRobo40.getConnection().destroyConnection();
				}
				catch (IOException e) { }
			}
			m_oRobo40.setConnection(new Robo40Bluetooth(i_oDevice));
			m_oRobo40.connect();
//		}
	}

	public static void connectToRobo40(final BaseActivity m_oOwner, Robo40 i_oRobo40, BluetoothDevice i_oDevice, final IConnectListener i_oConnectListener) {
		Robo40Robot m_oRobot = new Robo40Robot(m_oOwner) {
			public void onConnect() {
				i_oConnectListener.onConnect(true);
			};
			public void onDisconnect() {
				i_oConnectListener.onConnect(false);
			};
		};
		
		m_oRobot.showConnectingDialog();
		
		if (i_oRobo40.isConnected()) {
			i_oRobo40.disconnect();
		}

		i_oRobo40.setHandler(m_oRobot.getUIHandler());
		i_oRobo40.setConnection(new Robo40Bluetooth(i_oDevice));
		i_oRobo40.connect();
	}

}
