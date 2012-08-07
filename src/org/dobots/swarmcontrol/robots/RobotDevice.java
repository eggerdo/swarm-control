package org.dobots.swarmcontrol.robots;

import org.dobots.roomba.RoombaTypes;
import org.dobots.swarmcontrol.R;
import org.dobots.utility.AccelerometerListener;
import org.dobots.utility.AccelerometerManager;
import org.dobots.utility.DeviceListActivity;
import org.dobots.utility.ProgressDlg;

import android.app.Activity;
import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

public class RobotDevice extends Activity implements AccelerometerListener {
	
	protected static String TAG = "RobotDevice";
	
	public static String MAC_FILTER = "MAC_FILTER";
	protected String m_strRobotMacFilter;
	
	protected static final int REQUEST_CONNECT_ROBOT = 1000;
	protected static final int REQUEST_ENABLE_BT = 1001;

	protected static final int CONNECT_ROBOT = 100;
	protected static final int SELECT_ROBOT = 101;
	
	protected Activity m_oActivity;
	protected RobotType m_eRobot;

	protected BluetoothAdapter m_oBTAdapter = null;
	protected BluetoothSocket m_oSocket = null;
	protected boolean m_bBTOnByUs = false;

	protected ProgressDlg progress;
	
	protected Intent serverIntent;
	
	// Sensitivity towards acceleration
	protected static int SPEED_SENSITIVITY = 20;
	protected static int RADIUS_SENSITIVITY = 200;
	
	protected boolean m_bAccelerometer = false;
	protected boolean m_bSetAccelerometerBase = false;

	protected float m_fXBase, m_fYBase, m_fZBase = 0;

	final Handler connectionHandler	= new Handler() {
		@Override
		public void handleMessage(Message myMessage) {
			switch (myMessage.getData().getInt("message")) {
			case CONNECT_ROBOT: 
				connectToRobot(myMessage.getData().getString("address"));
				break;
			case SELECT_ROBOT:
				selectRobot();
				break;
			}
		}
	};;
	
//	public void show(Activity myActivity, RobotType i_eRobot) {

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
		this.m_oActivity = this;

		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		m_eRobot = (RobotType) getIntent().getExtras().get("RobotType");
		
        setProperties(m_eRobot);
	}
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
		if (AccelerometerManager.isListening()) {
			AccelerometerManager.stopListening();
		}
    }

	@Override
	protected void onResume() {
		super.onResume();
		if (AccelerometerManager.isSupported()) {
			AccelerometerManager.startListening(this);
		}
	}

	@Override
	public void onAccelerationChanged(float x, float y, float z, boolean tx) {
		if (tx && m_bAccelerometer) {
			
			if (m_bSetAccelerometerBase) {
				m_fXBase = x;
				m_fYBase = y;
				m_fZBase = z;
				
				m_bSetAccelerometerBase = false;
			}
		}
	}

    protected int getSpeedFromAcceleration(float x, float y, float z, float i_fMaxSpeed) {
		
		float speed_off = 0.0F;
		
		// calculate speed, we use the angle between the start position
		// (the position in which the phone was when the acceleration was
		// turned on) and the current position. 
		if (z > 0) {
			speed_off = (y - m_fYBase);
			
		} else {
			speed_off = ((9.9F + 9.9F - y) - m_fYBase);
		}

		// instead of mapping the speed to the range 0..i_nMaxSpeed we add the speed 
		// sensitivity so that speeds between 0..speed_sensitivity are ignored
		// before giving the speed as parameter to the drive function we need
		// to get rid of the speed_sensitivity again.
		int speed = (int) (speed_off * ((i_fMaxSpeed + SPEED_SENSITIVITY) / 9.9F));

		// cap the speed to [-i_nMaxSpeed,i_nMaxSpeed]
		speed = Math.max(speed, -(int)i_fMaxSpeed);
		speed = Math.min(speed, (int)i_fMaxSpeed);

		return speed;
    }
    
    protected int getRadiusFromAcceleration(float x, float y, float z, float i_fMaxRadius) {

		// convert to [-i_nMaxRadius,i_nMaxRadius]
		return (int) (x * (i_fMaxRadius / 9.9F));

    }

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_ROBOT:

			Log.i(TAG, "DeviceListActivity returns with device to connect");
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address and start a new bt communicator thread
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
//				pairing = data.getExtras().getBoolean(DeviceListActivity.PAIRING);
				
		        Bundle myBundle = new Bundle();
		        myBundle.putInt("message", CONNECT_ROBOT);
		        myBundle.putString("address", address);
		        sendBundle(connectionHandler, myBundle);
			}

			break;
		}

		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
	        Bundle myBundle = new Bundle();
	        myBundle.putInt("message", SELECT_ROBOT);
	        sendBundle(connectionHandler, myBundle);
		}
	}

	protected void setProperties(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.robotdevice);
        
		TextView lblRobot = (TextView) m_oActivity.findViewById(R.id.lblRobot);
		lblRobot.setText(i_eRobot.toString());
		
		Spinner spSensors = (Spinner) m_oActivity.findViewById(R.id.spSensors);
		spSensors.setVisibility(View.INVISIBLE);
	}
	
	public void close() {
		// to be implemented by child class
	}
	
	protected boolean initBluetooth() throws Exception {
		m_oBTAdapter = BluetoothAdapter.getDefaultAdapter();
		if (m_oBTAdapter == null) {
			throw new Exception("Robot Connection not possible without Bluetooth!");
		}
		
		if (!m_oBTAdapter.isEnabled()) {
			m_bBTOnByUs = true;
			Intent oEnableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			m_oActivity.startActivityForResult(oEnableBTIntent, REQUEST_ENABLE_BT);
			return false;
		} else
			return true;
	}
	
	protected boolean disableBluetooth() {
		if (m_oBTAdapter != null) {
			// requires BLUETOOTH_ADMIN permission and is discouraged in the API Doc
//			m_oBTAdapter.disable();
			return true;
		}
		return false;
	}

    protected void sendBundle(Handler i_oHandler, Bundle i_oBundle) {
        Message myMessage = new Message();
        myMessage.setData(i_oBundle);
        i_oHandler.sendMessage(myMessage);
    }

	protected void selectRobot() {
		serverIntent = new Intent(m_oActivity, DeviceListActivity.class);
		Bundle oParam = new Bundle();
		oParam.putString(MAC_FILTER, m_strRobotMacFilter);
		serverIntent.putExtras(oParam);
		m_oActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_ROBOT);
	}

	protected void connectToRobot(String i_strAddr) {
		// has to be implemented by child class
	}

}
