package org.dobots.swarmcontrol.robots;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.dobots.roomba.Roomba;
import org.dobots.roomba.RoombaTypes.ERoombaSensorPackages;
import org.dobots.roomba.RoombaTypes.SensorPackage;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.FinchRobot.FinchSensorType;

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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class RoombaRobot extends RobotDevice {

	private static final String TAG = "Roomba";
	
	private static final int REQUEST_ENABLE_BT = 1;

	private Roomba oRoomba;

	private BluetoothAdapter m_oBTAdapter;
	private BluetoothDevice m_oDevice;
	private BluetoothSocket m_oSocket;
	
	private RoombaSensorGatherer oSensorGatherer;
	
	private boolean m_bShow = false;
	
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
			initBluetooth();
			
			selectDevice();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	public void initBluetooth() throws Exception {
		m_oBTAdapter = BluetoothAdapter.getDefaultAdapter();
		if (m_oBTAdapter == null) {
			throw new Exception("Roomba Connection not possible without Bluetooth!");
		}
		
		if (!m_oBTAdapter.isEnabled()) {
			Intent oEnableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			m_oActivity.startActivityForResult(oEnableBTIntent, REQUEST_ENABLE_BT);
		}
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
			oRoomba.setConnection(m_oSocket);
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
		
		Button btnS1 = (Button) m_oActivity.findViewById(R.id.button4);
		btnS1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SensorPackage sensors = oRoomba.getSensors(ERoombaSensorPackages.sensPkg_1);
				Log.d(TAG, sensors.toString());
				int i = 0;
			}
		});

		Button btnS2 = (Button) m_oActivity.findViewById(R.id.button5);
		btnS2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SensorPackage sensors = oRoomba.getSensors(ERoombaSensorPackages.sensPkg_2);
				Log.d(TAG, sensors.toString());
				int i = 0;
			}
		});
		
		Button btnS3 = (Button) m_oActivity.findViewById(R.id.button6);
		btnS3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bShow = !m_bShow;
				oSensorGatherer.showSensorPackage3(m_bShow);
//				SensorPackage sensors = oRoomba.getSensors(ERoombaSensorPackages.sensPkg_3);
//				Log.d(TAG, sensors.toString());
//				int i = 0;
			}
		});
		
		Button btnSAll = (Button) m_oActivity.findViewById(R.id.button7);
		btnSAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SensorPackage sensors = oRoomba.getSensors(ERoombaSensorPackages.sensPkg_All);
				Log.d(TAG, sensors.toString());
				int i = 0;
			}
		});
	}
	
//	public void scanForDevices() {
//		m_oBTAdapter.startDiscovery();
//		m_oDiscoveredDevices = new ArrayAdapter<CharSequence>(m_oActivity, android.R.layout.select_dialog_item);
//		IntentFilter oFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//		m_oActivity.registerReceiver(m_oDiscoveryReceiver, oFilter);
//	}
}
