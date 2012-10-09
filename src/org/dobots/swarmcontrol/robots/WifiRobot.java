package org.dobots.swarmcontrol.robots;

import org.dobots.robots.MessageTypes;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.WifiConnectionHelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

public abstract class WifiRobot extends RobotView {

	protected WifiConnectionHelper m_oWifiHelper;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        m_oWifiHelper = new WifiConnectionHelper(m_oActivity, RobotViewFactory.getRobotAddressFilter(RobotType.RBT_ARDRONE));
    }
    
	@Override
	protected void handleUIMessage(Message msg) {
		super.handleUIMessage(msg);
		
		switch (msg.what) {
		case MessageTypes.STATE_CONNECTED:
			connectingProgressDialog.dismiss();
			showToast("Connection OK", Toast.LENGTH_SHORT);
			onConnect();
			break;

		case MessageTypes.STATE_CONNECTERROR:
			connectingProgressDialog.dismiss();
			showToast("Connection Failed", Toast.LENGTH_SHORT);
		case MessageTypes.STATE_RECEIVEERROR:
		case MessageTypes.STATE_SENDERROR:
			onDisconnect();
			break;
		}
	}

}
