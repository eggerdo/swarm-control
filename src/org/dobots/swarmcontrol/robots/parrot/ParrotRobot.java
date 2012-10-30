package org.dobots.swarmcontrol.robots.parrot;

import java.net.UnknownHostException;

import org.dobots.robots.parrot.Parrot;
import org.dobots.robots.parrot.ParrotTypes;
import org.dobots.robots.parrot.ParrotVideoProcessor;
import org.dobots.swarmcontrol.ConnectListener;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.RemoteControlHelper;
import org.dobots.swarmcontrol.RobotInventory;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.swarmcontrol.robots.WifiRobot;
import org.dobots.utility.OnButtonPress;
import org.dobots.utility.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codeminders.ardrone.ARDrone.VideoChannel;

public class ParrotRobot extends WifiRobot {

	private static String TAG = "Parrot";
	
	private static final int CONNECT_ID = Menu.FIRST;

	private boolean connected;
	
	private Parrot m_oParrot;

	private ParrotSensorGatherer m_oSensorGatherer;

//	private RemoteControlHelper m_oRemoteCtrl;
	
//	private Button m_btnCalibrate;
	
	private double m_dblSpeed;
	
	private Button m_btnLand;
	private Button m_btnTakeOff;
	private Button m_btnUp;
	private Button m_btnDown;
	private Button m_btnRotateLeft;
	private Button m_btnRotateRight;
	
	private Button m_btnCamera;
	private Button m_btnSensors;
	
	private boolean m_bSensorsEnabled = false;
	private boolean m_bControl = false;

	private Button m_btnSetAltitude;

	private EditText m_edtAltitude;

    private EditText edtKp, edtKd, edtKi;

	private Button m_btnEmergency;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	int nIndex = (Integer) getIntent().getExtras().get("InventoryIndex");
    	if (nIndex == -1) {
			m_oParrot = new Parrot();
	    	m_oParrot.setHandler(uiHandler);
	        connectToRobot();
    	} else {
    		m_oParrot = (Parrot) RobotInventory.getInstance().getRobot(nIndex);
        	m_oParrot.setHandler(uiHandler);
    		if (m_oParrot.isConnected()) {
    			updateButtons(true);
    		}
    		m_bKeepAlive = true;
    	}

		m_oSensorGatherer = new ParrotSensorGatherer(this, m_oParrot);
		m_dblSpeed = m_oParrot.getBaseSped();

//		m_oRemoteCtrl = new RemoteControlHelper(m_oActivity, m_oParrot, null);
//        m_oRemoteCtrl.setProperties();

        updateButtons(false);
        
//        m_oRemoteCtrl.setControlPressListener(new OnButtonPress() {
//			
//			@Override
//			public void buttonPressed(boolean i_bDown) {
//				m_bControl = !m_bControl;
//				m_oParrot.enableControl(m_bControl);
//			}
//		});
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();

    	if (m_oParrot.isConnected() && !m_bKeepAlive) {
    		m_oParrot.disconnect();
    		m_oParrot.destroy();
    	}
    	
    	m_oSensorGatherer.close();
    }
    
    @Override
    public void onStop() {
    	super.onStop();

    	m_oSensorGatherer.close();
    	
    	if (m_oParrot.isConnected() && !m_bKeepAlive) {
    		m_oParrot.disconnect();
    	}
    	
    }

    @Override
    public void onPause() {
    	super.onPause();

    	m_oSensorGatherer.pauseThread();
    }

    @Override
    public void onRestart() {
    	super.onRestart();
    	
    	if (!m_bKeepAlive) {
    		connectToRobot();
    	}
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();

    	m_oSensorGatherer.resumeThread();
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
			m_oParrot.disconnect();
			resetLayout();
			updateButtons(false);
			connectToRobot(2000);
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}
	
	public void connectToRobot(int delay) {
		if (m_oWifiHelper.initWifi()) {
			connectingProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.connecting_please_wait), true);
			
			uiHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					m_oParrot.connect();
				}
			}, delay);
		}
	}
	
	@Override
	public void connectToRobot() {
		connectToRobot(0);
	}

	public static void connectToARDrone(final Activity m_oOwner, Parrot i_oARDrone, String i_strAddress, final ConnectListener i_oConnectListener) {
//		final ProgressDialog connectingProgress = ProgressDialog.show(m_oOwner, "", m_oOwner.getResources().getString(R.string.connecting_please_wait), true);
//		
//		if (i_oARDrone.getConnection() != null) {
//			try {
//				i_oARDrone.getConnection().destroyConnection();
//			}
//			catch (IOException e) { }
//		}
//		
//		i_oARDrone.setConnection(new ARDroneWifi());
//		i_oARDrone.connect();
//		i_oARDrone.setHandler(new Handler() {
//			@Override
//			public void handleMessage(Message msg) {
//				switch (msg.what) {
//				case BaseWifi.DISPLAY_TOAST:
//					Utils.showToast((String)msg.obj, Toast.LENGTH_SHORT);
//					break;
//				case BaseWifi.STATE_CONNECTED:
//					connectingProgress.dismiss();
//					i_oConnectListener.onConnect(true);
////					updateButtonsAndMenu();
//					break;
//
//				case BaseWifi.STATE_CONNECTERROR_PAIRING:
//					connectingProgress.dismiss();
//					i_oConnectListener.onConnect(false);
//					break;
//
//				case BaseWifi.STATE_CONNECTERROR:
//					connectingProgress.dismiss();
//				case BaseWifi.STATE_RECEIVEERROR:
//				case BaseWifi.STATE_SENDERROR:
//					i_oConnectListener.onConnect(false);
//
////					if (btErrorPending == false) {
////						btErrorPending = true;
//						// inform the user of the error with an AlertDialog
//						AlertDialog.Builder builder = new AlertDialog.Builder(m_oOwner);
//						builder.setTitle(m_oOwner.getResources().getString(R.string.bt_error_dialog_title))
//						.setMessage(m_oOwner.getResources().getString(R.string.bt_error_dialog_message)).setCancelable(false)
//						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//							//                            @Override
//							public void onClick(DialogInterface dialog, int id) {
////								btErrorPending = false;
//								dialog.cancel();
//							}
//						});
//						builder.create().show();
////					}
//
//					break;
//				}
//			}
//		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	};

	@Override
	protected void onConnect() {
		connected = true;
		m_oSensorGatherer.onConnect();

		updateButtons(true);
//		m_oRemoteCtrl.updateButtons(true);
	}
	
	@Override
	protected void onDisconnect() {
		connected = false;
		m_oSensorGatherer.onDisconnect();
		
		updateButtons(false);
//		m_oRemoteCtrl.updateButtons(false);
	}
	
	@Override
	public void handleUIMessage(Message msg) {
		super.handleUIMessage(msg);
	}

	@Override
	protected void setProperties(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.parrot_main);
        
//		m_btnCalibrate = (Button) m_oActivity.findViewById(R.id.btnCalibrate);
//		m_btnCalibrate.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//
//				int nIndex = RobotInventory.getInstance().findRobot(m_oARDrone);
//				if (nIndex == -1) {
//					nIndex = RobotInventory.getInstance().addRobot(m_oARDrone);
//				}
//				m_bKeepAlive = true;
//				RobotCalibration.createAndShow(m_oActivity, RobotType.RBT_NXT, nIndex, m_dblSpeed);
//			}
//		});
        
        m_btnEmergency = (Button) findViewById(R.id.btnEmergency);
        m_btnEmergency.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oParrot.sendEmergencySignal();
			}
		});
        
        m_edtAltitude = (EditText) findViewById(R.id.edtAltitude);
        
        edtKp = (EditText) findViewById(R.id.edtKp);
        edtKd = (EditText) findViewById(R.id.edtKd);
        edtKi = (EditText) findViewById(R.id.edtKi);
        
        Button btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oParrot.stopAltitudeControl();
			}
		});
        
        m_btnSetAltitude = (Button) findViewById(R.id.btnSetAltitude);
        m_btnSetAltitude.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				double altitude = (new Double(m_edtAltitude.getText().toString())).doubleValue();
				m_oParrot.Kp = (new Double(edtKp.getText().toString())).doubleValue();
				m_oParrot.Kd = (new Double(edtKd.getText().toString())).doubleValue();
				m_oParrot.Ki = (new Double(edtKi.getText().toString())).doubleValue();
				m_oParrot.setAltitude(altitude);
			}
		});
   
        m_btnCamera = (Button) findViewById(R.id.btnCamera);
        m_btnCamera.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oParrot.switchCamera();
		        updateCameraButton();
			}
		});
        updateCameraButton();
        
        m_btnSensors = (Button) findViewById(R.id.btnSensors);
        m_btnSensors.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bSensorsEnabled = !m_bSensorsEnabled;
				m_oSensorGatherer.enableSensors(m_bSensorsEnabled);
				m_btnSensors.setText("Sensors: " + (m_bSensorsEnabled ? "ON" : "OFF"));
			}
		});

        m_btnLand = (Button) findViewById(R.id.btnLand);
        m_btnLand.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oParrot.land();
			}
		});
        
        m_btnTakeOff = (Button) findViewById(R.id.btnTakeOff);
        m_btnTakeOff.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oParrot.takeOff();
			}
		});
        
        m_btnUp = (Button) findViewById(R.id.btnUp);
        m_btnUp.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oParrot.moveStop();
					break;
				case MotionEvent.ACTION_DOWN:
					m_oParrot.increaseAltitude();
					break;
				}
				return true;
			}
		});
        
        m_btnDown = (Button) findViewById(R.id.btnDown);
        m_btnDown.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oParrot.moveStop();
					break;
				case MotionEvent.ACTION_DOWN:
					m_oParrot.decreaseAltitude();
					break;
				}
				return true;
			}
		});
        
        m_btnRotateLeft = (Button) findViewById(R.id.btnRotateLeft);
        m_btnRotateLeft.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oParrot.moveStop();
					break;
				case MotionEvent.ACTION_DOWN:
					m_oParrot.rotateCounterClockwise();
					break;
				}
				return true;
			}
		});

        m_btnRotateRight = (Button) findViewById(R.id.btnRotateRight);
        m_btnRotateRight.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oParrot.moveStop();
					break;
				case MotionEvent.ACTION_DOWN:
					m_oParrot.rotateClockwise();
					break;
				}
				return true;
			}
		});
        
	}
	
	private void updateCameraButton() {
		if (m_oParrot != null && (m_oParrot.getVidoeChannel() == VideoChannel.VERTICAL_ONLY)) {
			m_btnCamera.setText("Camera: Bottom");
		} else {
			m_btnCamera.setText("Camera: Front");
		}
	}

	private void resetLayout() {
//        m_oRemoteCtrl.resetLayout();
        
        updateButtons(false);
	}

	public void updateButtons(boolean enabled) {
//		m_oRemoteCtrl.updateButtons(enabled);
		
		m_btnCamera.setEnabled(enabled);
		m_btnSensors.setEnabled(enabled);
	}
	
}
