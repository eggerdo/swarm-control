package org.dobots.swarmcontrol.robots;

import org.dobots.robots.MessageTypes;
import org.dobots.swarmcontrol.BluetoothConnectionHelper;
import org.dobots.swarmcontrol.IBluetoothConnectionListener;
import org.dobots.swarmcontrol.R;
import org.dobots.utilities.BaseActivity;

import robots.gui.RobotView;
import android.app.AlertDialog;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

public abstract class BluetoothRobot extends RobotView implements IBluetoothConnectionListener {

	protected BluetoothConnectionHelper m_oBTHelper;

//	protected BluetoothAdapter m_oBTAdapter = null;
	protected BluetoothSocket m_oSocket = null;
	protected boolean m_bBTOnByUs = false;

	public BluetoothRobot(BaseActivity i_oOwner) {
		super(i_oOwner);
	}

    public BluetoothRobot() {
		super();
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

		m_oBTHelper = new BluetoothConnectionHelper(this, RobotViewFactory.getRobotAddressFilter(m_eRobot));
		m_oBTHelper.SetOnConnectListener(this);
    }

    @Override
    public void onRestart() {
    	super.onRestart();
    	
    	// when activity is restarted, connect to the robot again if it's not already connected, but only
    	// if we have a valid address
    	if (m_strAddress != "" && !getRobot().isConnected()) {
    		if (m_oBTHelper.initBluetooth()) {
    			connect(m_oBTHelper.getRemoteDevice(m_strAddress));
    		}
    	}
    }

    protected void onConnectError() {
		// inform the user of the error with an AlertDialog
		AlertDialog.Builder builder = new AlertDialog.Builder(m_oActivity);
		builder.setTitle(m_oActivity.getResources().getString(R.string.bt_error_dialog_title))
		.setMessage(m_oActivity.getResources().getString(R.string.bt_error_dialog_message)).setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			//                            @Override
			public void onClick(DialogInterface dialog, int id) {
				btErrorPending = false;
				dialog.cancel();
//				m_oBTHelper.selectRobot();
			}
		});
		builder.create().show();
    }
    
	@Override
	protected void handleUIMessage(Message msg) {
		super.handleUIMessage(msg);
		
		switch (msg.what) {
		case MessageTypes.STATE_CONNECTERROR_PAIRING:
			connectingProgressDialog.dismiss();
			onDisconnect();
			break;
		}
	}

    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);

    	// pass the result on to the BluetoothConnectionHelper in case
    	// he has something to do with the result
    	m_oBTHelper.onActivityResult(requestCode, resultCode, data);
    }

	protected void connectToRobot() {
		// if bluetooth is not yet enabled, initBluetooth will return false
		// and the device selection will be called in the onActivityResult
		if (m_oBTHelper.initBluetooth())
			m_oBTHelper.selectRobot();
	}

}
