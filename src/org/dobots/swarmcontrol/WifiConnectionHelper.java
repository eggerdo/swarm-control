package org.dobots.swarmcontrol;

import org.dobots.utility.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class WifiConnectionHelper{

	private Activity m_oActivity;
	
	private WifiManager m_oWifiManager;
	
	private String m_strSSID_Filter;
	
	public WifiConnectionHelper(Activity i_oParent, String i_strAddressFilter) {
		m_oActivity = i_oParent;
		m_strSSID_Filter = i_strAddressFilter;
	}

	public boolean initWifi() {
		m_oWifiManager = (WifiManager) m_oActivity.getSystemService(Context.WIFI_SERVICE);
		
		if (m_oWifiManager == null) {
			Utils.showToast("Robot Connection not possible without Wifi!", Toast.LENGTH_LONG);
		} else {
			if (!m_oWifiManager.isWifiEnabled()) {
				m_oWifiManager.setWifiEnabled(true);
			}
				
			return checkConnection();
		}
		
		return false;
	}
	
	private void showAlertDialog(String i_strTitle, String i_strMessage) {
		// inform the user of the error with an AlertDialog
		AlertDialog.Builder builder = new AlertDialog.Builder(m_oActivity);
		builder.setTitle(i_strTitle)
		.setMessage(i_strMessage).setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			//                            @Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.create().show();
	}

	private boolean checkConnection() {
		WifiInfo oInfo = m_oWifiManager.getConnectionInfo();
		if (oInfo != null) {
			String strSsid = oInfo.getSSID();
			if (strSsid != null) {
				if (strSsid.contains(m_strSSID_Filter)) {
					return true;
				} else {
					showAlertDialog("WiFi Error", 
							"Wrong WiFi connection!\n"
							+ "It should be a connection starting with " + m_strSSID_Filter + ".");
				}
			} else {
				showAlertDialog("Wifi Error", 
						"Not possible to determine the name of your Wifi connection. "
						+ "Make sure you are connected!");
			}
		}
		return false;
	}
}
