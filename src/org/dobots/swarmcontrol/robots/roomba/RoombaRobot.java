package org.dobots.swarmcontrol.robots.roomba;

import java.io.IOException;

import org.dobots.robots.BaseBluetooth;
import org.dobots.robots.nxt.NXT;
import org.dobots.robots.nxt.NXTTypes;
import org.dobots.robots.roomba.Roomba;
import org.dobots.robots.roomba.RoombaBluetooth;
import org.dobots.robots.roomba.RoombaTypes;
import org.dobots.robots.roomba.RoombaTypes.ERoombaSensorPackages;
import org.dobots.swarmcontrol.ConnectListener;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.RobotInventory;
import org.dobots.swarmcontrol.robots.RobotCalibration;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.swarmcontrol.robots.RobotView;
import org.dobots.swarmcontrol.robots.nxt.NXTBluetooth;
import org.dobots.utility.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
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

	private boolean m_bControl = false;
	private boolean m_bMainBrushEnabled = false;
	private boolean m_bSideBrushEnabled = false;
	private boolean m_bVacuumEnabled = false;

	private Button m_btnFwd;
	private Button m_btnBwd;
	private Button m_btnLeft;
	private Button m_btnRight;
	
	private boolean m_bMove;

	private boolean btErrorPending = false;

	private Spinner m_spSensors;
	private Button m_btnClean;
	private Button m_btnStop;
	private Button m_btnDock;
	private Button m_btnControl;
	private Button m_btnMainBrush;
	private Button m_btnSideBrush;
	private Button m_btnVacuum;
	private Button m_btnPower;
	private Button m_btnAccelerometer;
	private Button m_btnMove;
	private Button m_btnCalibrate;

	private double m_dblSpeed;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	updateButtons(false);
    	updateControlButtons(false);
    	updatePowerButton(false);
    	
    	int nIndex = (Integer) getIntent().getExtras().get("InventoryIndex");
    	if (nIndex == -1) {
    		m_oRoomba = new Roomba();
    		connectToRobot();
    	} else {
    		m_oRoomba = (Roomba) RobotInventory.getInstance().getRobot(nIndex);
    		if (m_oRoomba.isConnected()) {
    			updatePowerButton(true);
				if (m_oRoomba.isPowerOn()) {
					updateButtons(true);
				}
    		}
    		m_bKeepAlive = true;
    	}
		
		oSensorGatherer = new RoombaSensorGatherer(m_oActivity, m_oRoomba);

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
    	
    	shutDown();
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case RobotCalibration.ROBOT_CALIBRATION_RESULT:
			if (resultCode == RESULT_OK) {
				m_dblSpeed = data.getExtras().getDouble(RobotCalibration.CALIBRATED_SPEED);
				m_oRoomba.setBaseSpeed(m_dblSpeed);
				showToast("Calibrated speed saved", Toast.LENGTH_SHORT);
			} else {
				showToast("Calibration discarded", Toast.LENGTH_SHORT);
			}
		}
	};
    
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
	
	@Override
	public void onConnect() {
		m_oRoomba.init();
		updatePowerButton(true);
		if (m_oRoomba.isPowerOn()) {
			updateButtons(true);
		}
	}
	
	@Override
	public void onDisconnect() {
		updatePowerButton(false);
		updateButtons(false);
	}

	public void updateControlButtons(boolean visible) {
//		m_oActivity.findViewById(R.id.btnMainBrush).setEnabled(visible);
//		m_oActivity.findViewById(R.id.btnSideBrush).setEnabled(visible);
//		m_oActivity.findViewById(R.id.btnVacuum).setEnabled(visible);
		m_btnCalibrate.setEnabled(visible);
		
		Utils.showLayout((LinearLayout)m_oActivity.findViewById(R.id.layBrushes), visible);
		
		Utils.showLayout((LinearLayout)m_oActivity.findViewById(R.id.layRemoteControl), visible);
	}
	
	public void updateArrowButtons(boolean enabled) {
		m_btnLeft.setEnabled(enabled);
		m_btnRight.setEnabled(enabled);
		m_btnFwd.setEnabled(enabled);
		m_btnBwd.setEnabled(enabled);
		
	}
	
	public void updateButtons(boolean enabled) {
		m_btnClean.setEnabled(enabled);
		m_btnStop.setEnabled(enabled);
		m_btnDock.setEnabled(enabled);
		m_btnControl.setEnabled(enabled);
//		m_btnPower.setEnabled(enabled);
		m_spSensors.setEnabled(enabled);
	}

	protected void connectToRobot() {
		// if bluetooth is not yet enabled, initBluetooth will return false
		// and the device selection will be called in the onActivityResult
		if (m_oBTHelper.initBluetooth())
			m_oBTHelper.selectRobot();

	}

	public void connectToRobot(BluetoothDevice i_oDevice) {
		m_strMacAddress = i_oDevice.getAddress();
		connectingProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.connecting_please_wait), true);
		
		if (m_oRoomba.isConnected()) {
			m_oRoomba.destroyConnection();
		}
		RoombaBluetooth oRoombaBluetooth = new RoombaBluetooth(i_oDevice);
		oRoombaBluetooth.setReceiveHandler(uiHandler);
		m_oRoomba.setConnection(oRoombaBluetooth);
		m_oRoomba.connect();
	}
	
	public void updatePowerButton(boolean enabled) {
		m_btnPower.setEnabled(enabled);
		if (enabled) {
			if (m_oRoomba.isConnected()) {
				m_btnPower.setText("Power: " + (m_oRoomba.isPowerOn() ? "ON" : "OFF"));
			}
		}
	}

	/**
	 * Receive messages from the BTCommunicator
	 */
	final Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BaseBluetooth.DISPLAY_TOAST:
				showToast((String)msg.obj, Toast.LENGTH_SHORT);
				break;
			case BaseBluetooth.STATE_CONNECTED:
				connectingProgressDialog.dismiss();
				m_oRoomba.init();
				updatePowerButton(true);
				if (m_oRoomba.isPowerOn()) {
					updateButtons(true);
				}
//				updateButtonsAndMenu();
				break;

			case BaseBluetooth.STATE_CONNECTERROR_PAIRING:
				connectingProgressDialog.dismiss();
				break;

			case BaseBluetooth.STATE_CONNECTERROR:
				connectingProgressDialog.dismiss();
			case BaseBluetooth.STATE_RECEIVEERROR:
			case BaseBluetooth.STATE_SENDERROR:

				if (btErrorPending == false) {
					btErrorPending = true;
					// inform the user of the error with an AlertDialog
					AlertDialog.Builder builder = new AlertDialog.Builder(m_oActivity);
					builder.setTitle(getResources().getString(R.string.bt_error_dialog_title))
					.setMessage(getResources().getString(R.string.bt_error_dialog_message)).setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						//                            @Override
						public void onClick(DialogInterface dialog, int id) {
							btErrorPending = false;
							dialog.cancel();
							m_oBTHelper.selectRobot();
						}
					});
					builder.create().show();
				}

				break;
			}
		}
	};

	public static void connectToRoomba(final Activity m_oOwner, final Roomba i_oRoomba, BluetoothDevice i_oDevice, final ConnectListener i_oConnectListener) {
		final ProgressDialog connectingProgress = ProgressDialog.show(m_oOwner, "", m_oOwner.getResources().getString(R.string.connecting_please_wait), true);
		
		if (i_oRoomba.isConnected()) {
			i_oRoomba.destroyConnection();
		}
		RoombaBluetooth oRoombaBluetooth = new RoombaBluetooth(i_oDevice);
		oRoombaBluetooth.setReceiveHandler(new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case BaseBluetooth.DISPLAY_TOAST:
					Utils.showToast((String)msg.obj, Toast.LENGTH_SHORT);
					break;
				case BaseBluetooth.STATE_CONNECTED:
					connectingProgress.dismiss();
					i_oConnectListener.onConnect(true);
					i_oRoomba.init();
//					updateButtonsAndMenu();
					break;

				case BaseBluetooth.STATE_CONNECTERROR_PAIRING:
					connectingProgress.dismiss();
					i_oConnectListener.onConnect(false);
					break;

				case BaseBluetooth.STATE_CONNECTERROR:
					connectingProgress.dismiss();
				case BaseBluetooth.STATE_RECEIVEERROR:
				case BaseBluetooth.STATE_SENDERROR:
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
		i_oRoomba.setConnection(oRoombaBluetooth);
		i_oRoomba.connect();
	}

	@Override
	public void shutDown() {
    	updateButtons(false);
    	updateControlButtons(false);

		oSensorGatherer.setSensor(ERoombaSensorPackages.sensPkg_None);
		oSensorGatherer.stopThread();
		
		if (m_oRoomba.isConnected() && !m_bKeepAlive) {
			m_oRoomba.disconnect();
		}
		
		m_oBTHelper.disableBluetooth();
	}

	@Override
	protected void setProperties(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.roomba_main);
		
        m_spSensors = (Spinner) m_oActivity.findViewById(R.id.spSensors);
		final ArrayAdapter<ERoombaSensorPackages> adapter = new ArrayAdapter<ERoombaSensorPackages>(m_oActivity, 
				android.R.layout.simple_spinner_item, ERoombaSensorPackages.values());
        adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
		m_spSensors.setAdapter(adapter);
		m_spSensors.setOnItemSelectedListener(new OnItemSelectedListener() {

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

		m_btnClean = (Button) m_oActivity.findViewById(R.id.btnClean);
		m_btnClean.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oRoomba.startCleanMode();
			}
		});
		
		m_btnStop = (Button) m_oActivity.findViewById(R.id.btnStop);
		m_btnStop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// we can stop any active action by setting the roomba to safe mode
				m_oRoomba.setSafeMode();
			}
		});
		
		m_btnDock = (Button) m_oActivity.findViewById(R.id.btnDock);
		m_btnDock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oRoomba.seekDocking();
			}
		});

		m_btnControl = (Button) m_oActivity.findViewById(R.id.btnCtrl);
		m_btnControl.setText("Control: OFF");
		m_btnControl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bControl = !m_bControl;
				updateControlButtons(m_bControl);
				m_oRoomba.enableControl(m_bControl);
				m_btnControl.setText("Control: " + (m_bControl ? "ON" : "OFF"));
			}
		});

		m_btnCalibrate = (Button) m_oActivity.findViewById(R.id.btnCalibrate);
		m_btnCalibrate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				int nIndex = RobotInventory.getInstance().findRobot(m_oRoomba);
				if (nIndex == -1) {
					nIndex = RobotInventory.getInstance().addRobot(m_oRoomba);
				}
				m_bKeepAlive = true;
				RobotCalibration.createAndShow(m_oActivity, RobotType.RBT_ROOMBA, nIndex);
			}
		});
	
		m_btnMainBrush = (Button) m_oActivity.findViewById(R.id.btnMainBrush);
		m_btnMainBrush.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bMainBrushEnabled = !m_bMainBrushEnabled;
				m_oRoomba.setMainBrush(m_bMainBrushEnabled);
			}
		});

		m_btnSideBrush = (Button) m_oActivity.findViewById(R.id.btnSideBrush);
		m_btnSideBrush.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bSideBrushEnabled = !m_bSideBrushEnabled;
				m_oRoomba.setSideBrush(m_bSideBrushEnabled);
			}
		});

		m_btnVacuum = (Button) m_oActivity.findViewById(R.id.btnVacuum);
		m_btnVacuum.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bVacuumEnabled = !m_bVacuumEnabled;
				m_oRoomba.setVacuum(m_bVacuumEnabled);
			}
		});
		
		m_btnPower = (Button) m_oActivity.findViewById(R.id.btnPower);
		m_btnPower.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (m_oRoomba.isPowerOn()) {
					m_oRoomba.powerOff();
				} else {
					m_oRoomba.powerOn();
				}
				updatePowerButton(true);
				if (m_oRoomba.isPowerOn()) {
					updateButtons(true);
				}
			}
		});
		
		m_btnAccelerometer = (Button) m_oActivity.findViewById(R.id.btnAccelerometer);
		m_btnAccelerometer.setOnClickListener(new OnClickListener() {
			
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
		
		m_btnMove = (Button) m_oActivity.findViewById(R.id.btnMove);
		m_btnMove.setOnClickListener(new OnClickListener() {
			
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

	public static String getMacFilter() {
		return RoombaTypes.MAC_FILTER;
	}
	
}
