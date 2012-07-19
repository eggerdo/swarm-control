package org.dobots.swarmcontrol.robots;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.dobots.roomba.Roomba;
import org.dobots.roomba.RoombaBluetooth;
import org.dobots.roomba.RoombaTypes;
import org.dobots.roomba.RoombaTypes.ERoombaSensorPackages;
import org.dobots.roomba.RoombaTypes.SensorPackage;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.SwarmControlActivity;
import org.dobots.swarmcontrol.robots.FinchRobot.FinchSensorType;
import org.dobots.utility.AccelerometerListener;
import org.dobots.utility.AccelerometerManager;
import org.dobots.utility.DeviceListActivity;
import org.dobots.utility.ProgressDlg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class RoombaRobot extends RobotDevice implements AccelerometerListener {
	
	private static final String TAG = "Roomba";
	
	private static final int REQUEST_ENABLE_BT = 1;
	
	private static final int CONNECT_ROOMBA = 100;
	private static final int SELECT_ROOMBA = 101;
	
	private Roomba oRoomba;

	private BluetoothAdapter m_oBTAdapter;
	private BluetoothSocket m_oSocket;
	
	private RoombaSensorGatherer oSensorGatherer;
	
	private boolean m_bControl = false;
	private boolean m_bMainBrushEnabled = false;
	private boolean m_bSideBrushEnabled = false;
	private boolean m_bVacuumEnabled = false;

	private boolean m_bBTOnByUs = false;
	
	private Button m_btnFwd;
	private Button m_btnBwd;
	private Button m_btnLeft;
	private Button m_btnRight;
	
	public Intent serverIntent;
	
	private ProgressDlg progress;

	// Sensitivity towards acceleration
	private int speed_sensitivity = 19;
	private int radius_sensitivity = 380;
	
	private boolean m_bAccelerometer = false;

	private static final UUID ROOMBA_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
//	@Override
//	public void show(Activity i_oActivity, RobotType i_eRobot) {
//		super.show(i_oActivity, i_eRobot);
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
		m_oActivity = this;

		oRoomba = new Roomba();
		oSensorGatherer = new RoombaSensorGatherer(m_oActivity, oRoomba);

    	updateButtons(false);
    	updateControlButtons(false);
    	
		try {
			// if bluetooth is not yet enabled, initBluetooth will return false
			// and the device selection will be called in the onActivityResult
			if (initBluetooth())
				selectRoomba();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    public void onDestroy() {
    	super.onDestroy();
    	
		if (AccelerometerManager.isListening()) {
			AccelerometerManager.stopListening();
		}
    	
    	if (oRoomba.isConnected()) {
    		close();
    	}
    }
    
    @Override
    public void onStart() {
    	super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (AccelerometerManager.isSupported()) {
			AccelerometerManager.startListening(this);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:

			Log.i(TAG, "DeviceListActivity returns with device to connect");
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address and start a new bt communicator thread
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
//				pairing = data.getExtras().getBoolean(DeviceListActivity.PAIRING);

				progress = ProgressDlg.show(this, "Connecting...", "");
				
		        Bundle myBundle = new Bundle();
		        myBundle.putInt("message", CONNECT_ROOMBA);
		        myBundle.putString("address", address);
		        sendBundle(myBundle);
		        
			}

			break;
		}
		
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
	        Bundle myBundle = new Bundle();
	        myBundle.putInt("message", SELECT_ROOMBA);
	        sendBundle(myBundle);
		}
	}

	@Override
	public void onAccelerationChanged(float x, float y, float z, boolean tx) {
		if (tx && m_bAccelerometer) {
			// convert to [-100,100]
			int speed = (int) (y * (50.0F / 9.9F));

			// convert to [-2000,2000]
			int offset = (int) (x * (2000.0F / 9.9F));

			Log.i("Speeds", "speed=" + speed + ", offset=" + offset); 

//			if (speed < -speed_sensitivity) {
//				if (offset > radius_sensitivity) {
////					Drive(1, 0);
//					oRoomba.driveForward(speed, offset);
//				} else if (offset < -radius_sensitivity) {
////					Drive(2, 0);
//					oRoomba.driveForward(speed, offset);
//				} else {
////					Drive(3, 0);
//					oRoomba.driveForward(50);
//				}
//			}
//
//			if (speed > speed_sensitivity) {
//				if (offset > radius_sensitivity) {
////					Drive(0, 1);
//					oRoomba.driveBackward(50, offset);
//				} else if (offset < -radius_sensitivity) {
////					Drive(0, 2);
//					oRoomba.driveBackward(50, offset);
//				} else {
////					Drive(0, 3);
//					oRoomba.driveBackward(50);
//				}
//			}
		}
	}
	
	final Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message myMessage) {
			switch (myMessage.getData().getInt("message")) {
			case CONNECT_ROOMBA: 
				connectToDevice(myMessage.getData().getString("address"));
				break;
			case SELECT_ROOMBA:
				selectRoomba();
				break;
			}
		}
	};

    private void sendBundle(Bundle myBundle) {
        Message myMessage = new Message();
        myMessage.setData(myBundle);
        uiHandler.sendMessage(myMessage);
    }

	private void selectRoomba() {
		serverIntent = new Intent(m_oActivity, DeviceListActivity.class);
		Bundle oParam = new Bundle();
		oParam.putString(MAC_FILTER, RoombaTypes.MAC_FILTER);
		serverIntent.putExtras(oParam);
		m_oActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}

	public void updateControlButtons(boolean visible) {
		m_oActivity.findViewById(R.id.btnMainBrush).setEnabled(visible);
		m_oActivity.findViewById(R.id.btnSideBrush).setEnabled(visible);
		m_oActivity.findViewById(R.id.btnVacuum).setEnabled(visible);
		
		if (visible) {
			TableLayout tblControlButtons = (TableLayout) m_oActivity.findViewById(R.id.tblControlButtons);
			tblControlButtons.setLayoutParams(new TableLayout.LayoutParams());
		} else {
			TableLayout tblControlButtons = (TableLayout) m_oActivity.findViewById(R.id.tblControlButtons);
			tblControlButtons.setLayoutParams(new TableLayout.LayoutParams(0, 0));
		}
	}
	
	public void updateButtons(boolean enabled) {
		m_oActivity.findViewById(R.id.btnClean).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnStop).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnDock).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnCtrl).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnPower).setEnabled(enabled);
		m_oActivity.findViewById(R.id.spSensors).setEnabled(enabled);
	}

	public boolean initBluetooth() throws Exception {
		m_oBTAdapter = BluetoothAdapter.getDefaultAdapter();
		if (m_oBTAdapter == null) {
			throw new Exception("Roomba Connection not possible without Bluetooth!");
		}
		
		if (!m_oBTAdapter.isEnabled()) {
			m_bBTOnByUs = true;
			Intent oEnableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			m_oActivity.startActivityForResult(oEnableBTIntent, REQUEST_ENABLE_BT);
			return false;
		} else
			return true;
	}
	
	public boolean disableBluetooth() {
		if (m_oBTAdapter != null) {
			// requires BLUETOOTH_ADMIN permission and is discouraged in the API Doc
//			m_oBTAdapter.disable();
			return true;
		}
		return false;
	}
	
	private void connectToDevice(String i_strAddr) {
		BluetoothDevice oDevice = m_oBTAdapter.getRemoteDevice(i_strAddr);
		connectToDevice(oDevice);
	}
	
	private void connectToDevice(BluetoothDevice i_oDevice) {
		final BluetoothDevice oDevice = i_oDevice;

		try {
			m_oSocket = oDevice.createRfcommSocketToServiceRecord(ROOMBA_UUID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (m_oBTAdapter.isDiscovering()) {
			m_oBTAdapter.cancelDiscovery();
		}
		
		try {
			m_oSocket.connect();
			
			RoombaBluetooth m_oConnection = new RoombaBluetooth(m_oSocket); 
			
			oRoomba.setConnection(m_oConnection);
			
			Toast.makeText(this, "Connection OK", Toast.LENGTH_SHORT).show();
			
			// initalize the robot
			if (!oRoomba.init()) {
			
				// if the init failed it might be because the robot is not powered on
				// in which case we try to power it on now
				if (!oRoomba.isPowerOn()) {
					oRoomba.powerOn();
				}
			}
			
			progress.dismiss();
			
			updateButtons(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				m_oSocket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(m_oActivity);
			builder.setTitle("Connection failed");
			builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					connectToDevice(oDevice);
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			builder.show();
		}
	}

	@Override
	public void close() {
		try {

	    	updateButtons(false);
	    	updateControlButtons(false);
	    	
			if (m_oSocket != null) {
				oSensorGatherer.setSensor(ERoombaSensorPackages.sensPkg_None);
				
				// before closing the connection we set the roomba to passive mode
				// which consumes less power
				oRoomba.setPassiveMode();
			
				m_oSocket.close();
				m_oSocket = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (m_bBTOnByUs) {
			disableBluetooth();
		}
	}

	@Override
	protected void setProperties(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.roomba);
		
		Spinner spSensors = (Spinner) m_oActivity.findViewById(R.id.spSensors);
		final ArrayAdapter<ERoombaSensorPackages> adapter = new ArrayAdapter<ERoombaSensorPackages>(m_oActivity, 
				android.R.layout.simple_spinner_item, ERoombaSensorPackages.values());
        adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
		spSensors.setAdapter(adapter);
		spSensors.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				ERoombaSensorPackages eSensorPkg = adapter.getItem(position);
				oSensorGatherer.showSensorPackage(eSensorPkg);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// do nothing
			}
			
		});

		Button btnClean = (Button) m_oActivity.findViewById(R.id.btnClean);
		btnClean.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				oRoomba.startCleanMode();
			}
		});
		
		Button btnStop = (Button) m_oActivity.findViewById(R.id.btnStop);
		btnStop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// we can stop any active action by setting the roomba to safe mode
				oRoomba.setSafeMode();
			}
		});
		
		Button btnDock = (Button) m_oActivity.findViewById(R.id.btnDock);
		btnDock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				oRoomba.seekDocking();
			}
		});

		Button btnControl = (Button) m_oActivity.findViewById(R.id.btnCtrl);
		btnControl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bControl = !m_bControl;
				updateControlButtons(m_bControl);
				if (m_bControl) {
					oRoomba.setSafeMode();
				} else {
					oRoomba.setPassiveMode();
				}
			}
		});

		Button btnMainBrush = (Button) m_oActivity.findViewById(R.id.btnMainBrush);
		btnMainBrush.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bMainBrushEnabled = !m_bMainBrushEnabled;
				oRoomba.setMainBrush(m_bMainBrushEnabled);
			}
		});

		Button btnSideBrush = (Button) m_oActivity.findViewById(R.id.btnSideBrush);
		btnSideBrush.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bSideBrushEnabled = !m_bSideBrushEnabled;
				oRoomba.setSideBrush(m_bSideBrushEnabled);
			}
		});

		Button btnVacuum = (Button) m_oActivity.findViewById(R.id.btnVacuum);
		btnVacuum.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bVacuumEnabled = !m_bVacuumEnabled;
				oRoomba.setVacuum(m_bVacuumEnabled);
			}
		});
		
		Button btnPower = (Button) m_oActivity.findViewById(R.id.btnPower);
		btnPower.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (oRoomba.isPowerOn()) {
					oRoomba.powerOff();
				} else {
					oRoomba.powerOn();
				}
				((Button)v).setText("Power " + (oRoomba.isPowerOn() ? "OFF" : "ON"));
			}
		});
		
		Button btnAccelerometer = (Button) m_oActivity.findViewById(R.id.btnAccelerometer);
		btnAccelerometer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bAccelerometer = !m_bAccelerometer;
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
					oRoomba.stop();
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_DOWN:
					oRoomba.driveForward(50);
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
					oRoomba.stop();
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_DOWN:
					oRoomba.driveBackward(50);
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
					oRoomba.stop();
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_DOWN:
					oRoomba.rotateCounterClockwise(50);
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
					oRoomba.stop();
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_DOWN:
					oRoomba.rotateClockwise(50);
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
}
