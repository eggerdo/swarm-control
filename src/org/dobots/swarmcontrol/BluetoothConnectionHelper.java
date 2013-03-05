package org.dobots.swarmcontrol;

import org.dobots.robots.MessageTypes;
import org.dobots.swarmcontrol.behaviours.IActivityResultListener;
import org.dobots.utility.DeviceListActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class BluetoothConnectionHelper implements IActivityResultListener {
	
	public static String TAG = "BTHelper";

	private BaseActivity m_oParent;
	
	private BluetoothAdapter m_oBTAdapter;
	
	private boolean m_bBTOnByUs;
	private String m_strMacFilter;

	public static String MAC_FILTER = "MAC_FILTER";
	public static String TITLE = "TITLE";

	private IBluetoothConnectionListener m_oListener;

	private String m_strTitle = "";
	
	public BluetoothConnectionHelper(BaseActivity i_oParent, String i_strMacFilter) {
		m_oParent = i_oParent;
		m_strMacFilter = i_strMacFilter;
	}
	
	public void SetOnConnectListener(IBluetoothConnectionListener i_oListener) {
		m_oListener = i_oListener;
	}

	public boolean initBluetooth() {
		try {
			m_oBTAdapter = BluetoothAdapter.getDefaultAdapter();
			if (m_oBTAdapter == null) {
				throw new Exception("Robot Connection not possible without Bluetooth!");
			}
			
			if (!m_oBTAdapter.isEnabled()) {
				m_bBTOnByUs = true;
				Intent oEnableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				m_oParent.startActivityForResult(oEnableBTIntent, MessageTypes.REQUEST_ENABLE_BT, this);
				return false;
			} else
				return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean disableBluetooth() {
		if (m_bBTOnByUs) {
			if (m_oBTAdapter != null) {
				// requires BLUETOOTH_ADMIN permission and is discouraged in the API Doc
	//			m_oBTAdapter.disable();
				return true;
			}
		}
		return false;
	}

	public BluetoothDevice getRemoteDevice(String i_strAddr) {
		if (m_oBTAdapter != null) {
			return m_oBTAdapter.getRemoteDevice(i_strAddr);
		} else
			return null;
	}

	public void cancelDiscovery() {
		if (m_oBTAdapter.isDiscovering()) {
			m_oBTAdapter.cancelDiscovery();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case MessageTypes.REQUEST_CONNECT_ROBOT:
			Log.d(TAG, "DeviceListActivity returns with device to connect");
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address and connect to the robot
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				
				m_oListener.connect(m_oBTAdapter.getRemoteDevice(address));
			}
			break;
		case MessageTypes.REQUEST_ENABLE_BT:
			Log.d(TAG, "SelectRobot request received");
			if (resultCode == Activity.RESULT_OK) {
				selectRobot();
			}
			break;
		}

	}

	public void selectRobot() {
		Intent serverIntent = new Intent(m_oParent, DeviceListActivity.class);
		Bundle oParam = new Bundle();
		oParam.putString(MAC_FILTER, m_strMacFilter);
		oParam.putString(TITLE, m_strTitle);
		serverIntent.putExtras(oParam);
		m_oParent.startActivityForResult(serverIntent, MessageTypes.REQUEST_CONNECT_ROBOT, this);
	}
	
	public void setTitle(String i_strTitle) {
		m_strTitle = i_strTitle;
	}
	
}
