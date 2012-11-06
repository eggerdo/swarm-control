package org.dobots.swarmcontrol.robots.dotty;

import java.io.IOException;
import java.util.Arrays;

import org.dobots.robots.MessageTypes;
import org.dobots.robots.dotty.Dotty;
import org.dobots.robots.dotty.DottyTypes;
import org.dobots.robots.dotty.DottyTypes.EDottySensors;
import org.dobots.robots.nxt.msg.MsgTypes.RawDataMsg;
import org.dobots.swarmcontrol.ConnectListener;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.RemoteControlHelper;
import org.dobots.swarmcontrol.RobotInventory;
import org.dobots.swarmcontrol.robots.BluetoothRobot;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.utility.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class DottyRobot extends BluetoothRobot {

	private static String TAG = "Dotty";
	
	private static final int CONNECT_ID = Menu.FIRST;
	private static final int ADVANCED_CONTROL_ID = CONNECT_ID + 1;
	
	private Dotty m_oDotty;

	private DottySensorGatherer m_oSensorGatherer;

	private RemoteControlHelper m_oRemoteCtrl;

	private boolean m_bStreaming = false;
	
	private Button m_btnStreaming;
	
	private CheckBox m_cbAll;
	private CheckBox m_cbDistance;
	private CheckBox m_cbLight;
	private CheckBox m_cbSound;
	private CheckBox m_cbBattery;
	private CheckBox m_cbMotor1;
	private CheckBox m_cbMotor2;
	private CheckBox m_cbWheel1;
	private CheckBox m_cbWheel2;
	private CheckBox m_cbLed1;
	private CheckBox m_cbLed2;
	private CheckBox m_cbLed3;

	private EditText m_edtInterval;
	
	private double m_dblSpeed;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        
    	int nIndex = (Integer) getIntent().getExtras().get("InventoryIndex");
    	if (nIndex == -1) {
    		m_oDotty = new Dotty();
	        connectToRobot();
    	} else {
    		m_oDotty = (Dotty) RobotInventory.getInstance().getRobot(nIndex);
    		
    		m_bKeepAlive = true;
    	}
    	m_oDotty.setHandler(uiHandler);
		
		m_oSensorGatherer = new DottySensorGatherer(m_oActivity, m_oDotty);
		m_dblSpeed = m_oDotty.getBaseSped();

		m_oRemoteCtrl = new RemoteControlHelper(m_oActivity, m_oDotty, null);
        m_oRemoteCtrl.setProperties();

        updateButtons(false);
        
        if (m_oDotty.isConnected()) {
			updateButtons(true);
		}
    }
    
    public void setNXT(Dotty i_oNxt) {
    	m_oDotty = i_oNxt;
    	m_oDotty.setHandler(uiHandler);
    }
	
	@Override
	public void handleUIMessage(Message msg) {
		super.handleUIMessage(msg);
		
		switch (msg.what) {
		case DottyTypes.SENSOR_DATA:
			m_oSensorGatherer.sendMessage(DottyTypes.SENSOR_DATA, msg.obj);
			break;
		case DottyTypes.LOGGING:
			byte[] message = ((RawDataMsg)msg.obj).rgbyRawData;
			Log.i(TAG, String.format("%s", new String(Arrays.copyOfRange(message, 2, message.length))));
			break;
		}
	}
	
    @Override
	protected void setProperties(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.dotty_main);
        
        m_cbDistance = (CheckBox) m_oActivity.findViewById(R.id.cbDotty_Distance);
        m_cbLight = (CheckBox) m_oActivity.findViewById(R.id.cbDotty_Light);
        m_cbSound = (CheckBox) m_oActivity.findViewById(R.id.cbDotty_Sound);
        m_cbBattery = (CheckBox) m_oActivity.findViewById(R.id.cbDotty_Battery);
        m_cbMotor1 = (CheckBox) m_oActivity.findViewById(R.id.cbDotty_MotorSensor1);
        m_cbMotor2 = (CheckBox) m_oActivity.findViewById(R.id.cbDotty_MotorSensor2);
        m_cbWheel1 = (CheckBox) m_oActivity.findViewById(R.id.cbDotty_Wheel1);
        m_cbWheel2 = (CheckBox) m_oActivity.findViewById(R.id.cbDotty_Wheel2);
        m_cbLed1 = (CheckBox) m_oActivity.findViewById(R.id.cbDotty_Led1);
        m_cbLed2 = (CheckBox) m_oActivity.findViewById(R.id.cbDotty_Led2);
        m_cbLed3 = (CheckBox) m_oActivity.findViewById(R.id.cbDotty_Led3);
        
        CheckBox cbSensor;
        for (EDottySensors eSensor : EDottySensors.values()) {
        	switch(eSensor) {
        	case sensor_Battery:
        		cbSensor = m_cbBattery;
        		break;
        	case sensor_Dist:
        		cbSensor = m_cbDistance;
        		break;
        	case sensor_Light:
        		cbSensor = m_cbLight;
        		break;
        	case sensor_Motor1:
        		cbSensor = m_cbMotor1;
        		break;
        	case sensor_Motor2:
        		cbSensor = m_cbMotor2;
        		break;
        	case sensor_Sound:
        		cbSensor = m_cbSound;
        		break;
			case sensor_Wheel1:
				cbSensor = m_cbWheel1;
				break;
			case sensor_Wheel2:
				cbSensor = m_cbWheel2;
				break;
			case sensor_Led1:
				cbSensor = m_cbLed1;
				break;
			case sensor_Led2:
				cbSensor = m_cbLed2;
				break;
			case sensor_Led3:
				cbSensor = m_cbLed3;
				break;
    		default:
    			continue;
        	}
        	
        	cbSensor.setTag(eSensor);
        	cbSensor.setOnCheckedChangeListener(new OnCheckedChangeListener() {
    			
    			@Override
    			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    				// get the sensor id from the view object
    				m_oSensorGatherer.enableSensor((EDottySensors)buttonView.getTag(), isChecked);
    			}
    		});
        }
        
        m_cbAll = (CheckBox) m_oActivity.findViewById(R.id.cbDotty_All);
        m_cbAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        m_cbDistance.setChecked(isChecked);
		        m_cbLight.setChecked(isChecked);
		        m_cbSound.setChecked(isChecked);
		        m_cbBattery.setChecked(isChecked);
		        m_cbMotor1.setChecked(isChecked);
		        m_cbMotor2.setChecked(isChecked);
		        m_cbWheel1.setChecked(isChecked);
		        m_cbWheel2.setChecked(isChecked);
		        m_cbLed1.setChecked(isChecked);
		        m_cbLed2.setChecked(isChecked);
		        m_cbLed3.setChecked(isChecked);
			}
		});
		
		m_edtInterval = (EditText) m_oActivity.findViewById(R.id.edtInterval);
		m_edtInterval.setText(Integer.toString(DottyTypes.DEFAULT_SENSOR_INTERVAL));
		
		m_btnStreaming = (Button) m_oActivity.findViewById(R.id.btnStreaming);
		m_btnStreaming.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!m_bStreaming) {
					int nInterval = Integer.parseInt(m_edtInterval.getText().toString());
					if (nInterval >= DottyTypes.MIN_SENSOR_INTERVAL) {
						m_oDotty.startStreaming(nInterval);
					} else {
						showToast(String.format("Error: Minimun Value is %d!", DottyTypes.MIN_SENSOR_INTERVAL), Toast.LENGTH_SHORT);
						return;
					}
				} else {
					m_oDotty.stopStreaming();
				}
				m_bStreaming = !m_bStreaming;
				m_btnStreaming.setText("Streaming: " + (m_bStreaming ? "ON" : "OFF"));
			}
		});
		
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();

    	if (m_oDotty.isConnected() && !m_bKeepAlive) {
    		m_oDotty.disconnect();
    		m_oDotty.destroy();
    	}

    	m_oSensorGatherer.stopThread();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	
//    	m_oSensorGatherer.pauseThread();
    	
    	if (m_oDotty.isConnected() && !m_bKeepAlive) {
    		m_oDotty.disconnect();
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
    		connectToRobot(m_oBTHelper.getRemoteDevice(m_strMacAddress));
    	}

//    	m_oSensorGatherer.resumeThread();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, CONNECT_ID, 1, "Connect");
		return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	if (m_oRemoteCtrl.isControlEnabled()) {
    		if (menu.findItem(ADVANCED_CONTROL_ID) == null) {
				menu.add(0, ADVANCED_CONTROL_ID, 2, "Advanced Control");
    		}
		} else
			if (menu.findItem(ADVANCED_CONTROL_ID) != null) {
				menu.removeItem(ADVANCED_CONTROL_ID);
			}

    	Utils.updateOnOffMenuItem(menu.findItem(ADVANCED_CONTROL_ID), m_oRemoteCtrl.isAdvancedControl());

    	return true;
    }
    
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case ADVANCED_CONTROL_ID:
			m_oRemoteCtrl.toggleAdvancedControl();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	protected void resetLayout() {
		m_cbAll.setChecked(false);
        m_cbDistance.setChecked(false);
        m_cbLight.setChecked(false);
        m_cbSound.setChecked(false);
        m_cbBattery.setChecked(false);
        m_cbMotor1.setChecked(false);
        m_cbMotor2.setChecked(false);
        m_cbWheel1.setChecked(false);
        m_cbWheel2.setChecked(false);
        m_cbLed1.setChecked(false);
        m_cbLed2.setChecked(false);
        m_cbLed3.setChecked(false);
        
        m_oRemoteCtrl.resetLayout();
        
        m_btnStreaming.setText("Streaming: OFF");
        m_bStreaming = false;
        
        updateButtons(false);

		m_oSensorGatherer.initialize();
	}
	
	public void updateButtons(boolean enabled) {
		m_oRemoteCtrl.updateButtons(enabled);
		
		m_btnStreaming.setEnabled(enabled);
		m_edtInterval.setEnabled(enabled);
		
		m_cbAll.setEnabled(enabled);
		m_cbDistance.setEnabled(enabled);
		m_cbBattery.setEnabled(enabled);
		m_cbLight.setEnabled(enabled);
		m_cbMotor1.setEnabled(enabled);
		m_cbMotor2.setEnabled(enabled);
		m_cbSound.setEnabled(enabled);
        m_cbWheel1.setEnabled(enabled);
        m_cbWheel2.setEnabled(enabled);
        m_cbLed1.setEnabled(enabled);
        m_cbLed2.setEnabled(enabled);
        m_cbLed3.setEnabled(enabled);
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
		m_oDotty.disconnect();
	}
	
	@Override
	public void connectToRobot(BluetoothDevice i_oDevice) {
		if (m_oBTHelper.initBluetooth()) {
			m_strMacAddress = i_oDevice.getAddress();
			connectingProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.connecting_please_wait), true);
			
			if (m_oDotty.getConnection() != null) {
				try {
					m_oDotty.getConnection().destroyConnection();
				}
				catch (IOException e) { }
			}
			m_oDotty.setConnection(new DottyBluetooth(i_oDevice));
			m_oDotty.connect();
		}
	}
	
	public static void connectToDotty(final Activity m_oOwner, Dotty i_oDotty, BluetoothDevice i_oDevice, final ConnectListener i_oConnectListener) {
		final ProgressDialog connectingProgress = ProgressDialog.show(m_oOwner, "", m_oOwner.getResources().getString(R.string.connecting_please_wait), true);
		
		if (i_oDotty.getConnection() != null) {
			try {
				i_oDotty.getConnection().destroyConnection();
			}
			catch (IOException e) { }
		}
		
		i_oDotty.setConnection(new DottyBluetooth(i_oDevice));
		i_oDotty.connect();
		i_oDotty.setHandler(new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MessageTypes.DISPLAY_TOAST:
					Utils.showToast((String)msg.obj, Toast.LENGTH_SHORT);
					break;
				case MessageTypes.STATE_CONNECTED:
					connectingProgress.dismiss();
					i_oConnectListener.onConnect(true);
//					updateButtonsAndMenu();
					break;

				case MessageTypes.STATE_CONNECTERROR_PAIRING:
					connectingProgress.dismiss();
					i_oConnectListener.onConnect(false);
					break;

				case MessageTypes.STATE_CONNECTERROR:
					connectingProgress.dismiss();
				case MessageTypes.STATE_RECEIVEERROR:
				case MessageTypes.STATE_SENDERROR:
					i_oConnectListener.onConnect(false);

//					if (btErrorPending == false) {
//						btErrorPending = true;
						// inform the user of the error with an AlertDialog
						AlertDialog.Builder builder = new AlertDialog.Builder(m_oOwner);
						builder.setTitle(m_oOwner.getResources().getString(R.string.bt_error_dialog_title))
						.setMessage(m_oOwner.getResources().getString(R.string.bt_error_dialog_message)).setCancelable(false)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							//                            @Override
							public void onClick(DialogInterface dialog, int id) {
//								btErrorPending = false;
								dialog.cancel();
							}
						});
						builder.create().show();
//					}

					break;
				}
			}
		});
	}

}
