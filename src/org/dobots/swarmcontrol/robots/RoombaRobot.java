package org.dobots.swarmcontrol.robots;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.dobots.nxt.NXTTypes;
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
import org.dobots.utility.Utils;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class RoombaRobot extends RobotDevice {
	
	private static String TAG = "Roomba";

	private static final int CONNECT_ID = Menu.FIRST;
	
	private Roomba oRoomba;

	private RoombaSensorGatherer oSensorGatherer;
	
	private boolean m_bControl = false;
	private boolean m_bMainBrushEnabled = false;
	private boolean m_bSideBrushEnabled = false;
	private boolean m_bVacuumEnabled = false;

	private Button m_btnFwd;
	private Button m_btnBwd;
	private Button m_btnLeft;
	private Button m_btnRight;

	private boolean m_bAccelerometer = false;
	private boolean m_bSetAccelerometerBase = false;

	private boolean m_bMove = false;

	private float m_fXBase, m_fYBase, m_fZBase = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
		oRoomba = new Roomba();
		oSensorGatherer = new RoombaSensorGatherer(m_oActivity, oRoomba);

    	updateButtons(false);
    	updateControlButtons(false);
    	
    	m_strRobotMacFilter = RoombaTypes.MAC_FILTER;
    
		try {
			// if bluetooth is not yet enabled, initBluetooth will return false
			// and the device selection will be called in the onActivityResult
			if (initBluetooth())
				selectRobot();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, CONNECT_ID, 1, "Connect");
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case CONNECT_ID:
			close();
			selectRobot();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

    public void onDestroy() {
    	super.onDestroy();
    	
    	if (oRoomba.isConnected()) {
    		close();
    	}
    }
    
	@Override
	public void onAccelerationChanged(float x, float y, float z, boolean tx) {
		super.onAccelerationChanged(x, y, z, tx);
		
		if (tx && m_bAccelerometer) {
			
			int nSpeed = getSpeedFromAcceleration(x, y, z, RoombaTypes.MAX_SPEED);
			int nRadius = getRadiusFromAcceleration(x, y, z, RoombaTypes.MAX_RADIUS);
			
			Log.i("Speeds", "speed=" + nSpeed + ", radius=" + nRadius); 

			// if speed is negative the roomba should drive forward
			// if it is positive it should drive backward
			if (nSpeed < -SPEED_SENSITIVITY) {
				// remove the speed sensitivity again
				nSpeed -= SPEED_SENSITIVITY; 
				if (nRadius > RADIUS_SENSITIVITY) {
					oRoomba.driveForward(nSpeed, nRadius);
				} else if (nRadius < -RADIUS_SENSITIVITY) {
					oRoomba.driveForward(nSpeed, nRadius);
				} else {
					oRoomba.driveForward(nSpeed);
				}
			} else if (nSpeed > SPEED_SENSITIVITY) {
				// remove the speed_sensitivity again
				nSpeed -= SPEED_SENSITIVITY;
				if (nRadius > RADIUS_SENSITIVITY) {
					// 
					oRoomba.driveBackward(nSpeed, nRadius);
				} else if (nRadius < -RADIUS_SENSITIVITY) {
					oRoomba.driveBackward(nSpeed, nRadius);
				} else {
					oRoomba.driveBackward(nSpeed);
				}
			} else {
				if (nRadius > RADIUS_SENSITIVITY) {
					// if speed is small we remap the radius to 
					// speed and let it rotate on the spot 
					nSpeed = (int) (nRadius / RoombaTypes.MAX_RADIUS * RoombaTypes.MAX_SPEED);
					oRoomba.rotateCounterClockwise(nSpeed);
				} else if (nRadius < -RADIUS_SENSITIVITY) {
					// if speed is small we remap the radius to 
					// speed and let it rotate on the spot 
					nSpeed = (int) (nRadius / RoombaTypes.MAX_RADIUS * RoombaTypes.MAX_SPEED);
					oRoomba.rotateClockwise(nSpeed);
				} else {
					oRoomba.stop();
				}
				
			}
		}
	}

	public void updateControlButtons(boolean visible) {
		m_oActivity.findViewById(R.id.btnMainBrush).setEnabled(visible);
		m_oActivity.findViewById(R.id.btnSideBrush).setEnabled(visible);
		m_oActivity.findViewById(R.id.btnVacuum).setEnabled(visible);
		
		Utils.showLayout((LinearLayout)m_oActivity.findViewById(R.id.layRemoteControl), visible);
		Utils.showLayout((LinearLayout)m_oActivity.findViewById(R.id.accelerometer), visible);
		
//		if (visible) {
//			TableLayout tblControlButtons = (TableLayout) m_oActivity.findViewById(R.id.layRemoteControl);
//			tblControlButtons.setLayoutParams(new TableLayout.LayoutParams());
//		} else {
//			TableLayout tblControlButtons = (TableLayout) m_oActivity.findViewById(R.id.layRemoteControl);
//			tblControlButtons.setLayoutParams(new TableLayout.LayoutParams(0, 0));
//		}
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
	protected void connectToRobot(String i_strAddr) {
		BluetoothDevice oDevice = m_oBTAdapter.getRemoteDevice(i_strAddr);
		connectToRoomba(oDevice);
	}
	
	private void connectToRoomba(BluetoothDevice i_oDevice) {
		final BluetoothDevice oDevice = i_oDevice;

		if (progress == null) {
			progress = ProgressDlg.show(this, "Connecting...", "");
		}

		try {
			m_oSocket = oDevice.createRfcommSocketToServiceRecord(RoombaTypes.ROOMBA_UUID);
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
			progress = null;
			
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

				if (m_bAccelerometer) {
					m_bSetAccelerometerBase = true;
				} else {
					oRoomba.stop();
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
