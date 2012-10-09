package org.dobots.swarmcontrol;

import org.dobots.utility.Utils;

import android.app.Activity;
import android.content.Context;
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
				
			WifiInfo oInfo = m_oWifiManager.getConnectionInfo();
			if (oInfo != null) {
				String strSsid = oInfo.getSSID();
				if (strSsid != null) {
					if (strSsid.contains(m_strSSID_Filter)) {
						return true;
					} else {
						Utils.showToast("You first need to be connected to the robot!\n"
								+ "It should be a connection starting with " + m_strSSID_Filter + ".\n", 
								Toast.LENGTH_LONG);
					}
				} else {
					Utils.showToast("Not possible to determine the name of your Wifi connection. "
							+ "Make sure you are connected!\n", 
							Toast.LENGTH_LONG);
				}
			}
		}
		
		return false;
	}

	public boolean checkConnection() {
		if (m_oWifiManager != null) {
			WifiInfo info = m_oWifiManager.getConnectionInfo();
			if (info != null) {
				String ssid = info.getSSID();
				if (ssid != null) 
					if (ssid.contains(m_strSSID_Filter)) 
						return true;
			}
		}
		return false;
	}
}
