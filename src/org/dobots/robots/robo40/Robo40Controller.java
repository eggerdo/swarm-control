package org.dobots.robots.robo40;

import java.io.IOException;

import org.dobots.swarmcontrol.robots.robo40.Robo40Bluetooth;

public class Robo40Controller {
	
	private Robo40Bluetooth m_oConnection;
	
	public void setConnection(Robo40Bluetooth i_oConnection) {
		m_oConnection = i_oConnection;
	}
	
	public Robo40Bluetooth getConnection() {
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
		byte[] message = Robo40Types.getDisconnectPackage();
		m_oConnection.sendMessage(message);
		m_oConnection.close();
		m_oConnection = null;
	}
		
	public void control(boolean i_bEnable) {
		byte[] message = Robo40Types.getControlCommandPackage(i_bEnable);
		m_oConnection.sendMessage(message);
	}
	
	public void drive(int i_nLeftVelocity, int i_nRightVelocity) {
		byte[] message = Robo40Types.getDriveCommandPackage(i_nLeftVelocity, i_nRightVelocity);
		m_oConnection.sendMessage(message);
	}
	
	public void driveStop() {
		byte[] message = Robo40Types.getDriveCommandPackage(0, 0);
		m_oConnection.sendMessage(message);
	}
//	
//	public void requestSensorData() {
//		byte[] message = Robo40Types.getDataRequestPackage();
//		m_oConnection.sendMessage(message);
//	}
//	
//	public void startStreaming(int i_nInterval) {
//		byte[] message = Robo40Types.getStreamingONPackage(i_nInterval);
//		m_oConnection.sendMessage(message);
//	}
//	
//	public void stopStreaming() {
//		byte[] message = Robo40Types.getStreamingOFFPackage();
//		m_oConnection.sendMessage(message);
//	}

	public void setMotor(int id, int direction, int value) {
		byte[] message = Robo40Types.getMotorCommandPackage(id, direction, value);
		m_oConnection.sendMessage(message);
	}
	
}
