package org.dobots.robots.piratedotty;

import java.io.IOException;

import org.dobots.swarmcontrol.robots.piratedotty.PirateDottyBluetooth;
import org.dobots.utilities.log.Loggable;

import android.util.Log;

public class PirateDottyController extends Loggable {
	
	private static final String TAG = "PirateDottyController";
	
	private PirateDottyBluetooth m_oConnection;
	
	public void setConnection(PirateDottyBluetooth i_oConnection) {
		m_oConnection = i_oConnection;
	}
	
	public PirateDottyBluetooth getConnection() {
		return m_oConnection;
	}
	
	public void destroyConnection() {
		if (m_oConnection != null) {
			try {
				m_oConnection.destroyConnection();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		m_oConnection = null;
	}
	
	public boolean isConnected() {
		if (m_oConnection != null) {
			return m_oConnection.isConnected();
		} else
			return false;
	}
	
	public void connect() {
		m_oConnection.open();
	}
	
	public void disconnect() {
		byte[] message = PirateDottyTypes.getDisconnectPackage();
		m_oConnection.sendMessage(message);
		m_oConnection.close();
		m_oConnection = null;
	}
	
	
	public void control(boolean i_bEnable) {
		byte[] message = PirateDottyTypes.getControlCommandPackage(i_bEnable);
		m_oConnection.sendMessage(message);
	}
	
	public void drive(int i_nLeftVelocity, int i_nRightVelocity) {
		debug(TAG, String.format("drive(%d, %d)", i_nLeftVelocity, i_nRightVelocity));
		byte[] message = PirateDottyTypes.getDriveCommandPackage(i_nLeftVelocity, i_nRightVelocity);
		m_oConnection.sendMessage(message);
	}
	
	public void driveStop() {
		byte[] message = PirateDottyTypes.getDriveCommandPackage(0, 0);
		m_oConnection.sendMessage(message);
	}
	
//	public void requestSensorData() {
//		byte[] message = PirateDottyTypes.getSensorRequestPackage();
//		m_oConnection.sendMessage(message);
//	}
	
//	public void startStreaming(int i_nInterval) {
//		byte[] message = PirateDottyTypes.getStreamingONPackage(i_nInterval);
//		m_oConnection.sendMessage(message);
//	}
	
//	public void stopStreaming() {
//		byte[] message = PirateDottyTypes.getStreamingOFFPackage();
//		m_oConnection.sendMessage(message);
//	}
	
}
