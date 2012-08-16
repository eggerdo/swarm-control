package org.dobots.swarmcontrol.robots;

import java.io.IOException;

import org.dobots.roomba.Roomba;
import org.dobots.roomba.RoombaBluetooth;
import org.dobots.roomba.RoombaTypes;
import org.dobots.roomba.RoombaTypes.ERoombaSensorPackages;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.swarmcontrol.robots.RobotView;
import org.dobots.utility.Utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class RoombaRobot extends RobotView {
	
	private static String TAG = "Roomba";

	private static final int CONNECT_ID = Menu.FIRST;
	private static final int ACCEL_ID = CONNECT_ID + 1;
	
	private Roomba m_oRoomba;

	private RoombaSensorGatherer oSensorGatherer;

	private ProgressDialog connectingProgressDialog;
	
	private String m_strMacAddress = "";
	
	private boolean m_bControl = false;
	private boolean m_bMainBrushEnabled = false;
	private boolean m_bSideBrushEnabled = false;
	private boolean m_bVacuumEnabled = false;

	private Button m_btnFwd;
	private Button m_btnBwd;
	private Button m_btnLeft;
	private Button m_btnRight;
	
	private boolean m_bMove;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	m_strRobotMacFilter = RoombaTypes.MAC_FILTER;
        
    	super.onCreate(savedInstanceState);
    	
		m_oRoomba = new Roomba();
		oSensorGatherer = new RoombaSensorGatherer(m_oActivity, m_oRoomba);

    	updateButtons(false);
    	updateControlButtons(false);
    	
		// if bluetooth is not yet enabled, initBluetooth will return false
		// and the device selection will be called in the onActivityResult
		if (m_oBTHelper.initBluetooth())
			m_oBTHelper.selectRobot();

    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, CONNECT_ID, 1, "Connect");
		return true;
	}
	   
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	if (m_bControl) {
    		if (menu.findItem(ACCEL_ID) == null) {
				menu.add(0, ACCEL_ID, 2, "Accelerometer (ON)");
    		}
		} else
			if (menu.findItem(ACCEL_ID) != null) {
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
			shutDown();
			m_oBTHelper.selectRobot();
			return true;
		case ACCEL_ID:
			m_bAccelerometer = !m_bAccelerometer;
			item.setTitle("Accelerometer " + (m_bAccelerometer ? "(OFF)" : "(ON)"));

			if (m_bAccelerometer) {
				m_bSetAccelerometerBase = true;
			} else {
				m_oRoomba.driveStop();
			}
		}

		return super.onMenuItemSelected(featureId, item);
	}

    public void onDestroy() {
    	super.onDestroy();
    	
    	if (m_oRoomba.isConnected()) {
    		shutDown();
    	}
    }
    
	@Override
	public void onAccelerationChanged(float x, float y, float z, boolean tx) {
		super.onAccelerationChanged(x, y, z, tx);
		
		if (tx && m_bAccelerometer) {
			Log.i("Accel", "x=" + x + ", y=" + y + ", z=" + z); 
			
			int nSpeed = getSpeedFromAcceleration(x, y, z, RoombaTypes.MAX_SPEED, true);
			int nRadius = getRadiusFromAcceleration(x, y, z, RoombaTypes.MAX_RADIUS);

			// if speed is negative the roomba should drive forward
			// if it is positive it should drive backward
			if (nSpeed > SPEED_SENSITIVITY) {
				// remove the speed sensitivity again
				nSpeed -= SPEED_SENSITIVITY; 

				Log.i("Speeds", "speed=" + nSpeed + ", radius=" + nRadius); 

				if (nRadius > RADIUS_SENSITIVITY) {
					m_oRoomba.driveForward(nSpeed, nRadius);
				} else if (nRadius < -RADIUS_SENSITIVITY) {
					m_oRoomba.driveForward(nSpeed, nRadius);
				} else {
					m_oRoomba.driveForward(nSpeed);
				}
			} else if (nSpeed < -SPEED_SENSITIVITY) {
				// remove the speed_sensitivity again
				nSpeed += SPEED_SENSITIVITY;

				Log.i("Speeds", "speed=" + nSpeed + ", radius=" + nRadius); 

				if (nRadius > RADIUS_SENSITIVITY) {
					// 
					m_oRoomba.driveBackward(nSpeed, nRadius);
				} else if (nRadius < -RADIUS_SENSITIVITY) {
					m_oRoomba.driveBackward(nSpeed, nRadius);
				} else {
					m_oRoomba.driveBackward(nSpeed);
				}
			} else {
				
				Log.i("Speeds", "speed=~0" + ", radius=" + nRadius); 

				if (nRadius > RADIUS_SENSITIVITY) {
					// if speed is small we remap the radius to 
					// speed and let it rotate on the spot 
					nSpeed = (int) (nRadius / RoombaTypes.MAX_RADIUS * RoombaTypes.MAX_SPEED);
					m_oRoomba.rotateCounterClockwise(nSpeed);
				} else if (nRadius < -RADIUS_SENSITIVITY) {
					// if speed is small we remap the radius to 
					// speed and let it rotate on the spot 
					nSpeed = (int) (nRadius / RoombaTypes.MAX_RADIUS * RoombaTypes.MAX_SPEED);
					m_oRoomba.rotateClockwise(nSpeed);
				} else {
					m_oRoomba.driveStop();
				}

			}
		}
	}

	public void updateControlButtons(boolean visible) {
//		m_oActivity.findViewById(R.id.btnMainBrush).setEnabled(visible);
//		m_oActivity.findViewById(R.id.btnSideBrush).setEnabled(visible);
//		m_oActivity.findViewById(R.id.btnVacuum).setEnabled(visible);
		Utils.showLayout((LinearLayout)m_oActivity.findViewById(R.id.layBrushes), visible);
		
		Utils.showLayout((LinearLayout)m_oActivity.findViewById(R.id.layRemoteControl), visible);
	}
	
	public void updateArrowButtons(boolean enabled) {
		m_oActivity.findViewById(R.id.btnLeft).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnRight).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnFwd).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnBwd).setEnabled(enabled);
		
	}
	
	public void updateButtons(boolean enabled) {
		m_oActivity.findViewById(R.id.btnClean).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnStop).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnDock).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnCtrl).setEnabled(enabled);
		m_oActivity.findViewById(R.id.btnPower).setEnabled(enabled);
		m_oActivity.findViewById(R.id.spSensors).setEnabled(enabled);
	}

	@Override
	public void connectToRobot(String i_strAddr) {
		m_strMacAddress = i_strAddr;
		connectingProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.connecting_please_wait), true);
		BluetoothDevice oDevice = m_oBTHelper.getRemoteDevice(i_strAddr);
		connectToRoomba(oDevice);
	}
	
	private void connectToRoomba(BluetoothDevice i_oDevice) {
		final BluetoothDevice oDevice = i_oDevice;

//		if (progress == null) {
//			progress = ProgressDlg.show(this, "Connecting...", "");
//		}

		try {
			m_oSocket = oDevice.createRfcommSocketToServiceRecord(RoombaTypes.ROOMBA_UUID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m_oBTHelper.cancelDiscovery();
		
		try {
			RoombaBluetooth m_oConnection = new RoombaBluetooth(m_oSocket); 
			
			m_oRoomba.setConnection(m_oConnection);
			m_oRoomba.connect();
			
			Toast.makeText(this, "Connection OK", Toast.LENGTH_SHORT).show();
			
			// initalize the robot
			if (!m_oRoomba.init()) {
			
				// if the init failed it might be because the robot is not powered on
				// in which case we try to power it on now
				if (!m_oRoomba.isPowerOn()) {
					m_oRoomba.powerOn();
				}
			}
//			
//			progress.dismiss();
//			progress = null;
			connectingProgressDialog.dismiss();
			
			updateButtons(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			connectingProgressDialog.dismiss();
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
					connectToRoomba(oDevice);
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
	public void shutDown() {

    	updateButtons(false);
    	updateControlButtons(false);
    	
		if (m_oRoomba.isConnected()) {
			oSensorGatherer.setSensor(ERoombaSensorPackages.sensPkg_None);
			
			m_oRoomba.disconnect();
		}
		
		m_oBTHelper.disableBluetooth();
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
				m_oRoomba.startCleanMode();
			}
		});
		
		Button btnStop = (Button) m_oActivity.findViewById(R.id.btnStop);
		btnStop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// we can stop any active action by setting the roomba to safe mode
				m_oRoomba.setSafeMode();
			}
		});
		
		Button btnDock = (Button) m_oActivity.findViewById(R.id.btnDock);
		btnDock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oRoomba.seekDocking();
			}
		});

		Button btnControl = (Button) m_oActivity.findViewById(R.id.btnCtrl);
		btnControl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bControl = !m_bControl;
				updateControlButtons(m_bControl);
				if (m_bControl) {
					m_oRoomba.setSafeMode();
				} else {
					m_oRoomba.setPassiveMode();
				}
				((Button)v).setText("Control " + (m_bControl ? "OFF" : "ON"));
			}
		});

		Button btnMainBrush = (Button) m_oActivity.findViewById(R.id.btnMainBrush);
		btnMainBrush.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bMainBrushEnabled = !m_bMainBrushEnabled;
				m_oRoomba.setMainBrush(m_bMainBrushEnabled);
			}
		});

		Button btnSideBrush = (Button) m_oActivity.findViewById(R.id.btnSideBrush);
		btnSideBrush.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bSideBrushEnabled = !m_bSideBrushEnabled;
				m_oRoomba.setSideBrush(m_bSideBrushEnabled);
			}
		});

		Button btnVacuum = (Button) m_oActivity.findViewById(R.id.btnVacuum);
		btnVacuum.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bVacuumEnabled = !m_bVacuumEnabled;
				m_oRoomba.setVacuum(m_bVacuumEnabled);
			}
		});
		
		Button btnPower = (Button) m_oActivity.findViewById(R.id.btnPower);
		btnPower.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (m_oRoomba.isPowerOn()) {
					m_oRoomba.powerOff();
				} else {
					m_oRoomba.powerOn();
				}
				((Button)v).setText("Power " + (m_oRoomba.isPowerOn() ? "OFF" : "ON"));
			}
		});
		
		Button btnAccelerometer = (Button) m_oActivity.findViewById(R.id.btnAccelerometer);
		btnAccelerometer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bAccelerometer = !m_bAccelerometer;

				if (m_bAccelerometer) {
					m_bSetAccelerometerBase = true;
				} else {
					m_oRoomba.driveStop();
				}
				
				if (m_bAccelerometer && m_bMove) {
					((Button) m_oActivity.findViewById(R.id.btnMove)).performClick();
				} else {
					updateArrowButtons(!m_bAccelerometer);
				}
			}
		});
		
		Button btnMove = (Button) m_oActivity.findViewById(R.id.btnMove);
		btnMove.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bMove = !m_bMove;
				
				if (m_bMove && m_bAccelerometer) {
					((Button) m_oActivity.findViewById(R.id.btnAccelerometer)).performClick();
				} else {
					updateArrowButtons(!m_bMove);
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
					m_oRoomba.driveStop();
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_DOWN:
					m_oRoomba.driveForward(50);
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
					m_oRoomba.driveStop();
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_DOWN:
					m_oRoomba.driveBackward(50);
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
					m_oRoomba.driveStop();
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_DOWN:
					m_oRoomba.rotateCounterClockwise(50);
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
					m_oRoomba.driveStop();
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_DOWN:
					m_oRoomba.rotateClockwise(50);
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
