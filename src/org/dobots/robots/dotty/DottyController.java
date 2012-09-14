package org.dobots.robots.dotty;

import java.io.IOException;

import org.dobots.robots.dotty.DottyTypes.CmdPackage;
import org.dobots.swarmcontrol.robots.dotty.DottyBluetooth;

public class DottyController {
	
	private DottyBluetooth m_oConnection;
	
	public void setConnection(DottyBluetooth i_oConnection) {
		m_oConnection = i_oConnection;
	}
	
	public DottyBluetooth getConnection() {
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
		byte[] message = DottyTypes.getDisconnectPackage();
		m_oConnection.sendMessage(message);
		m_oConnection.close();
		m_oConnection = null;
	}
	
	
	public void control(boolean i_bEnable) {
		byte[] message = DottyTypes.getControlPackage(i_bEnable);
		m_oConnection.sendMessage(message);
	}
	
	public void drive(int i_nLeftVelocity, int i_nRightVelocity) {
		byte[] message = DottyTypes.getDrivePackage(i_nLeftVelocity, i_nRightVelocity);
		m_oConnection.sendMessage(message);
	}
	
	public void driveStop() {
		byte[] message = DottyTypes.getDriveStopPackage();
		m_oConnection.sendMessage(message);
	}
	
	public void requestSensorData() {
		byte[] message = DottyTypes.getDataRequestPackage();
		m_oConnection.sendMessage(message);
	}
	
	public void startStreaming(int i_nInterval) {
		byte[] message = DottyTypes.getStreamingONPackage(i_nInterval);
		m_oConnection.sendMessage(message);
	}
	
	public void stopStreaming() {
		byte[] message = DottyTypes.getStreamingOFFPackage();
		m_oConnection.sendMessage(message);
	}
	
}
