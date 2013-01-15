package org.dobots.swarmcontrol.robots;

import org.dobots.swarmcontrol.BaseActivity;
import org.dobots.swarmcontrol.WifiConnectionHelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public abstract class WifiRobot extends RobotView {

	protected WifiConnectionHelper m_oWifiHelper;

	public WifiRobot(BaseActivity i_oOwner) {
		super(i_oOwner);
	}
	
	public WifiRobot() {
		super();
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        m_oWifiHelper = new WifiConnectionHelper(m_oActivity, RobotViewFactory.getRobotAddressFilter(m_eRobot));
    }
    
    protected void onConnectError() {
		// inform the user of the error with an AlertDialog
		AlertDialog.Builder builder = new AlertDialog.Builder(m_oActivity);
		builder.setTitle("Wifi Connection Error")
		.setMessage("Connection could not be established. Please check your Wifi Connection and try again").setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			//                            @Override
			public void onClick(DialogInterface dialog, int id) {
				btErrorPending = false;
				dialog.cancel();
			}
		});
		builder.create().show();
    }
    
    @Override
    protected void connectToRobot() {
    	if (m_oWifiHelper.initWifi()) {
        	showConnectingDialog();
    		connect();
    	}
    }

	protected abstract void connect();
}
