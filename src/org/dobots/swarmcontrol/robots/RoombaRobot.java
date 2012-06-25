package org.dobots.swarmcontrol.robots;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.dobots.roomba.Roomba;
import org.dobots.roomba.RoombaBluetooth;
import org.dobots.roomba.RoombaTypes.ERoombaSensorPackages;
import org.dobots.roomba.RoombaTypes.SensorPackage;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.SwarmControlActivity;
import org.dobots.swarmcontrol.robots.FinchRobot.FinchSensorType;
import org.dobots.utility.AccelerometerListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
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

public class RoombaRobot extends RobotDevice implements AccelerometerListener {

	private static final String TAG = "Roomba";
	
	private static final int REQUEST_ENABLE_BT = 1;

	private Roomba oRoomba;

	private BluetoothAdapter m_oBTAdapter;
	private BluetoothDevice m_oDevice;
	private BluetoothSocket m_oSocket;
	
	private RoombaSensorGatherer oSensorGatherer;
	
	private boolean m_bShow = false;
	
	private boolean m_bControl = false;
	
	private Button m_btnFwd;
	private Button m_btnBwd;
	private Button m_btnLeft;
	private Button m_btnRight;
	
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
//	protected enum RoombaSensorType {
//		SENS_LIGHT("Light"),
//		SENS_TEMPERATURE("Temperature"),
//		SENS_ACCELERATION("Acceleration"),
//		SENS_OBSTACLE("Obstacle Detection");
//		private String strDisplayName;
//		
//		FinchSensorType(String i_strDisplayName) {
//			this.strDisplayName = i_strDisplayName;
//		}
//
//		@Override
//		public String toString() {
//			return strDisplayName;
//		}
//	}
	
//	private ArrayAdapter<CharSequence> m_oDiscoveredDevices;
	
//	private final BroadcastReceiver m_oDiscoveryReceiver = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			String strAction = intent.getAction();
//			if (BluetoothDevice.ACTION_FOUND.equals(strAction)) {
//				BluetoothDevice oDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
////				m_oDiscoveredDevices.add(oDevice);
//				m_oDiscoveredDevices.add(oDevice.getName() + "\n" + oDevice.getAddress());
//			}
//		}
//		
//	};
	
	
	@Override
	public void show(Activity i_oActivity, RobotType i_eRobot) {
		m_oActivity = i_oActivity;
        m_oActivity.setContentView(R.layout.roomba);
        setProperties(i_eRobot);
		
		oRoomba = new Roomba();
		oSensorGatherer = new RoombaSensorGatherer(i_oActivity, oRoomba);
		
		try {
			// if bluetooth is not yet enabled, initBluetooth will return false
			// and the device selection will be called in the onActivityResult
			if (initBluetooth())
				selectDevice();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode) {
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
			selectDevice();
		}
	}

	@Override
	public void onAccelerationChanged(float x, float y, float z, boolean tx) {
//		if (tx && streaming && !controls) {
//			// convert to [-127,127]
//			int speed = (int) (x * (127.0F / 9.9F));
//
//			// convert to [-127,127]
//			int offset = (int) (y * (127.0F / 9.9F));
//
//			Log.i("Speeds", "speed=" + speed + ", offset=" + offset); 
//
//			if (speed < -sensitivity) {
//				if (offset > sensitivity) {
//					Drive(1, 0);
//				} else if (offset < -sensitivity) {
//					Drive(2, 0);
//				} else {
//					Drive(3, 0);
//				}
//			}
//
//			if (speed > sensitivity) {
//				if (offset > sensitivity) {
//					Drive(0, 1);
//				} else if (offset < -sensitivity) {
//					Drive(0, 2);
//				} else {
//					Drive(0, 3);
//				}
//			}
//		}
	}

	public void SetButtonVisible(boolean visible) {
		if (visible) {
			TableLayout tblControlButtons = (TableLayout) m_oActivity.findViewById(R.id.tblControlButtons);
			tblControlButtons.setLayoutParams(new TableLayout.LayoutParams());
		} else {
			TableLayout tblControlButtons = (TableLayout) m_oActivity.findViewById(R.id.tblControlButtons);
			tblControlButtons.setLayoutParams(new TableLayout.LayoutParams(0, 0));
		}
	}

	public boolean initBluetooth() throws Exception {
		m_oBTAdapter = BluetoothAdapter.getDefaultAdapter();
		if (m_oBTAdapter == null) {
			throw new Exception("Roomba Connection not possible without Bluetooth!");
		}
		
		if (!m_oBTAdapter.isEnabled()) {
			Intent oEnableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			m_oActivity.startActivityForResult(oEnableBTIntent, REQUEST_ENABLE_BT);
			return false;
		} else
			return true;
	}
	
	private class BTDevice {
		private BluetoothDevice oDevice;
		
		public BTDevice(BluetoothDevice i_oDevice) {
			oDevice = i_oDevice;
		}
		
		public String toString() {
			return oDevice.getName() + "\n" + oDevice.getAddress();
		}
		
		public BluetoothDevice getDevice() {
			return oDevice;
		}
		
	}
	
	public void selectDevice() {
		
		if (m_oBTAdapter.isDiscovering()) {
			m_oBTAdapter.cancelDiscovery();
		}
		
		Set<BluetoothDevice> sePairedDevices = m_oBTAdapter.getBondedDevices();
		
		
		if (sePairedDevices.size() > 0) {
			final ArrayAdapter<BTDevice> oArrayAdapter = new ArrayAdapter<BTDevice>(m_oActivity, android.R.layout.select_dialog_item);
			for (BluetoothDevice oDevice : sePairedDevices) {
				if (oDevice.getAddress().startsWith("00:06:66")) {
					oArrayAdapter.add(new BTDevice(oDevice));
				}
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(m_oActivity);
			builder.setTitle("Choose a device");
			builder.setAdapter(oArrayAdapter, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					BluetoothDevice oDevice = oArrayAdapter.getItem(which).getDevice();
					connectToDevice(oDevice);
				}
			});
//			builder.setNeutralButton("Scan for devices", new DialogInterface.OnClickListener() {
//				
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					scanForDevices();
////					selectDevice();
//				}
//			});
			builder.show();
		}
	}
	
	private void connectToDevice(BluetoothDevice i_oDevice) {
		final BluetoothDevice oDevice = i_oDevice;
		try {
			m_oSocket = oDevice.createRfcommSocketToServiceRecord(MY_UUID);
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
//				oRoomba.init();
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
					m_oActivity.setContentView(R.layout.main);
				}
			});
			builder.show();
		}
	}

	@Override
	public void close() {
		try {
			m_oSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void setProperties(RobotType i_eRobot) {
		
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

		Button btnClean = (Button) m_oActivity.findViewById(R.id.button1);
		btnClean.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				oRoomba.startCleanMode();
			}
		});
		
		Button btnStop = (Button) m_oActivity.findViewById(R.id.button2);
		btnStop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				oRoomba.stop();
			}
		});
		
		Button btnDock = (Button) m_oActivity.findViewById(R.id.button3);
		btnDock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				oRoomba.seekDocking();
			}
		});

		Button btnInit = (Button) m_oActivity.findViewById(R.id.button8);
		btnInit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				oRoomba.init();
			}
		});
		

		Button btnControl = (Button) m_oActivity.findViewById(R.id.btnCtrl);
		btnControl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bControl = !m_bControl;
				SetButtonVisible(m_bControl);
				if (m_bControl) {
					oRoomba.init();
					oRoomba.startSafeControl();
				} else {
					oRoomba.powerOff();
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
		
//		Button btnS1 = (Button) m_oActivity.findViewById(R.id.button4);
//		btnS1.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				SensorPackage sensors = oRoomba.getSensors(ERoombaSensorPackages.sensPkg_1);
//				Log.d(TAG, sensors.toString());
//				int i = 0;
//			}
//		});
//
//		Button btnS2 = (Button) m_oActivity.findViewById(R.id.button5);
//		btnS2.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				SensorPackage sensors = oRoomba.getSensors(ERoombaSensorPackages.sensPkg_2);
//				Log.d(TAG, sensors.toString());
//				int i = 0;
//			}
//		});
//		
//		Button btnS3 = (Button) m_oActivity.findViewById(R.id.button6);
//		btnS3.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				m_bShow = !m_bShow;
//				oSensorGatherer.showSensorPackage3(m_bShow);
////				SensorPackage sensors = oRoomba.getSensors(ERoombaSensorPackages.sensPkg_3);
////				Log.d(TAG, sensors.toString());
////				int i = 0;
//			}
//		});
//		
//		Button btnSAll = (Button) m_oActivity.findViewById(R.id.button7);
//		btnSAll.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				SensorPackage sensors = oRoomba.getSensors(ERoombaSensorPackages.sensPkg_All);
//				Log.d(TAG, sensors.toString());
//				int i = 0;
//			}
//		});
	}
	
//	public void scanForDevices() {
//		m_oBTAdapter.startDiscovery();
//		m_oDiscoveredDevices = new ArrayAdapter<CharSequence>(m_oActivity, android.R.layout.select_dialog_item);
//		IntentFilter oFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//		m_oActivity.registerReceiver(m_oDiscoveryReceiver, oFilter);
//	}
}
