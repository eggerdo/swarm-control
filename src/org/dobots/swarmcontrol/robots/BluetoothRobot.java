package org.dobots.swarmcontrol.robots;

import org.dobots.robots.BaseBluetooth;
import org.dobots.robots.MessageTypes;
import org.dobots.swarmcontrol.BluetoothConnectionHelper;
import org.dobots.swarmcontrol.R;

import android.app.AlertDialog;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

public abstract class BluetoothRobot extends RobotView {

	protected BluetoothConnectionHelper m_oBTHelper;

//	protected BluetoothAdapter m_oBTAdapter = null;
	protected BluetoothSocket m_oSocket = null;
	protected boolean m_bBTOnByUs = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

		m_oBTHelper = new BluetoothConnectionHelper(this, RobotViewFactory.getRobotAddressFilter(m_eRobot));
		m_oBTHelper.SetOnConnectListener(this);
    }
    
	@Override
	protected void handleUIMessage(Message msg) {
		super.handleUIMessage(msg);
		
		switch (msg.what) {
		case MessageTypes.STATE_CONNECTED:
			connectingProgressDialog.dismiss();
			onConnect();
	//			updateButtonsAndMenu();
			break;
	
		case MessageTypes.STATE_CONNECTERROR_PAIRING:
			connectingProgressDialog.dismiss();
			break;
	
		case MessageTypes.STATE_CONNECTERROR:
			connectingProgressDialog.dismiss();
		case MessageTypes.STATE_RECEIVEERROR:
		case MessageTypes.STATE_SENDERROR:
	
			if (btErrorPending == false) {
				onDisconnect();
				
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

    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);

    	// pass the result on to the BluetoothConnectionHelper in case
    	// he has something to do with the result
    	m_oBTHelper.onActivityResult(requestCode, resultCode, data);
    }
}
