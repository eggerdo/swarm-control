package org.dobots.swarmcontrol.robots;

import org.dobots.swarmcontrol.BluetoothConnectionHelper;
import org.dobots.swarmcontrol.ConnectListener;
import org.dobots.swarmcontrol.R;
import org.dobots.utility.AccelerometerListener;
import org.dobots.utility.AccelerometerManager;
import org.dobots.utility.ProgressDlg;

import android.app.Activity;
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
import android.widget.Toast;

public class RobotView extends Activity implements AccelerometerListener, ConnectListener {
	
	protected static String TAG = "RobotDevice";
	
	protected String m_strRobotMacFilter;
	
	protected Activity m_oActivity;
	protected RobotType m_eRobot;

//	protected BluetoothAdapter m_oBTAdapter = null;
	protected BluetoothSocket m_oSocket = null;
	protected boolean m_bBTOnByUs = false;

	protected ProgressDlg progress;
	
	// Sensitivity towards acceleration
	protected static int SPEED_SENSITIVITY = 10;
	protected static int RADIUS_SENSITIVITY = 400;
	
	protected boolean m_bAccelerometer = false;
	protected boolean m_bSetAccelerometerBase = false;

	protected float m_fXBase, m_fYBase, m_fZBase = 0;

	protected Toast reusableToast;
	
	protected BluetoothConnectionHelper m_oBTHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
		this.m_oActivity = this;
		
		m_oBTHelper = new BluetoothConnectionHelper(this, m_strRobotMacFilter);
		m_oBTHelper.SetOnConnectListener(this);

		reusableToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		
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
				if (y > 0 && z > 0) {
					m_fXBase = x;
					m_fYBase = y;
					m_fZBase = z;
				} else {
					m_bAccelerometer = false;
					String strText = "Please hold the phone upwards and try again";
					showToast(strText, strText.length());
				}
				
				m_bSetAccelerometerBase = false;
			}
		}
	}
//
//    protected int getSpeedFromAcceleration(float x, float y, float z, float i_fMaxSpeed) {
//		
//		float speed_off = 0.0F;
//		
//		// calculate speed, we use the angle between the start position
//		// (the position in which the phone was when the acceleration was
//		// turned on) and the current position. 
//		if (x + y > 0) {
//			speed_off = (z - m_fZBase);
//			
//		} else {
//			speed_off = ((9.9F + 9.9F - z) - m_fZBase);
//		}
//
//		// instead of mapping the speed to the range 0..i_nMaxSpeed we add the speed 
//		// sensitivity so that speeds between 0..speed_sensitivity are ignored
//		// before giving the speed as parameter to the drive function we need
//		// to get rid of the speed_sensitivity again.
//		int speed = (int) (speed_off * ((i_fMaxSpeed + SPEED_SENSITIVITY) / 9.9F));
//
//		// cap the speed to [-i_nMaxSpeed,i_nMaxSpeed]
//		speed = Math.max(speed, -(int)i_fMaxSpeed - SPEED_SENSITIVITY);
//		speed = Math.min(speed, (int)i_fMaxSpeed + SPEED_SENSITIVITY);
//
//		return speed;
//    }
//    
	
    protected int getSpeedFromAcceleration(float x, float y, float z, float i_fMaxSpeed, boolean i_bIncludeSensitivity) {
		
		float speed_off = 0.0F;

		Log.i("Accel", "xb=" + m_fXBase + ", yb=" + m_fYBase + ", zb=" + m_fZBase);
		
		Log.i("Accel", "x=" + x + ", y=" + y + ", z=" + z);
		
		// calculate speed, we use the angle between the start position
		// (the position in which the phone was when the acceleration was
		// turned on) and the current position. 
		if (Math.abs(x) + y > 0) {
			speed_off = (z - m_fZBase);
		} else {
			speed_off = ((9.9F + 9.9F - z) - m_fZBase);
		}

		int speed = 0;
		
		if (i_bIncludeSensitivity) {
			// instead of mapping the speed to the range 0..i_nMaxSpeed we add the speed 
			// sensitivity so that speeds between 0..speed_sensitivity are ignored
			// before giving the speed as parameter to the drive function we need
			// to get rid of the speed_sensitivity again.
			speed = (int) (speed_off * ((i_fMaxSpeed + SPEED_SENSITIVITY) / 9.9F));
	
			// cap the speed to [-i_nMaxSpeed,i_nMaxSpeed]
			speed = Math.max(speed, -(int)i_fMaxSpeed - SPEED_SENSITIVITY);
			speed = Math.min(speed, (int)i_fMaxSpeed + SPEED_SENSITIVITY);
		} else {
			// instead of mapping the speed to the range 0..i_nMaxSpeed we add the speed 
			// sensitivity so that speeds between 0..speed_sensitivity are ignored
			// before giving the speed as parameter to the drive function we need
			// to get rid of the speed_sensitivity again.
			speed = (int) (speed_off * ((i_fMaxSpeed) / 9.9F));
	
			// cap the speed to [-i_nMaxSpeed,i_nMaxSpeed]
			speed = Math.max(speed, -(int)i_fMaxSpeed);
			speed = Math.min(speed, (int)i_fMaxSpeed);
		}
		
		return speed;
    }
    
    protected int getRadiusFromAcceleration(float x, float y, float z, float i_fMaxRadius) {

    	float radius_off;
    	
		// convert to [-i_nMaxRadius,i_nMaxRadius]
    	if (x < 0 || Math.abs(z) + y > 0) {
    		radius_off = (x - m_fXBase);
			
		} else {
			radius_off = ((9.9F + 9.9F - x) - m_fXBase);
		}

		return (int) (radius_off * (i_fMaxRadius / 9.9F) * 1.5); // factor 2 added to make it react faster

    }

    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    		//nothing to be done right now here
    	}
    	
    	// pass the result on to the BluetoothConnectionHelper in case
    	// he has something to do with the result
    	m_oBTHelper.onActivityResult(requestCode, resultCode, data);
	}

	protected void setProperties(RobotType i_eRobot) {
        m_oActivity.setContentView(R.layout.robotdevice);
        
		TextView lblRobot = (TextView) m_oActivity.findViewById(R.id.lblRobot);
		lblRobot.setText(i_eRobot.toString());
		
		Spinner spSensors = (Spinner) m_oActivity.findViewById(R.id.spSensors);
		spSensors.setVisibility(View.INVISIBLE);
	}
	
	public void shutDown() {
		// to be implemented by child class
	}
	
    protected void sendBundle(Handler i_oHandler, Bundle i_oBundle) {
        Message myMessage = new Message();
        myMessage.setData(i_oBundle);
        i_oHandler.sendMessage(myMessage);
    }

	/**
	 * Displays a message as a toast
	 * @param textToShow the message
	 * @param length the length of the toast to display
	 */
    protected void showToast(String textToShow, int length) {
		reusableToast.setText(textToShow);
		reusableToast.setDuration(length);
		reusableToast.show();
	}

	/**
	 * Displays a message as a toast
	 * @param nResID the resource ID to display
	 * @param length the length of the toast to display
	 */
    protected void showToast(int nResID, int length) {
		reusableToast.setText(nResID);
		reusableToast.setDuration(length);
		reusableToast.show();
	}

	protected void connectToRobot() {
		// has to be implemented by child class
	}

	public void connectToRobot(String i_strAddr) {
		// has to be implemented by child class
	}

}
