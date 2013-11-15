package org.dobots.robots.dotty;

import java.io.IOException;

import org.dobots.swarmcontrol.robots.dotty.DottyBluetooth;
import org.dobots.utilities.Utils;

import android.os.Handler;

import robots.ctrl.ProtocolHandler;
import robots.ctrl.ProtocolHandler.ICommHandler;
import robots.gui.BluetoothConnection;
import robots.nxt.MsgTypes;

public class DottyController implements ICommHandler<byte[]> {
	
	private BluetoothConnection m_oConnection;

	private Handler mUiHandler;

	private class DottyProtocolHandler extends ProtocolHandler {

		public DottyProtocolHandler(BluetoothConnection connection, ICommHandler handler) {
			super(connection, handler);
		}

		@Override
		public void execute() {
			byte[] returnMessage = receiveMessage();
			if (returnMessage != null) {
				if (mMessageHandler != null) {
					mMessageHandler.onMessage(returnMessage);
				}
			}
		}
		
		protected byte[] receiveMessage() {
			int nHeader = mConnection.read();
			byte[] receiveMessage;
			int nBytesExpected;
			int nReceivedBytes;
			
			if (nHeader == 0xa5) {
				receiveMessage = new byte[DottyTypes.DATA_PKG_SIZE];

				nBytesExpected = receiveMessage.length;
				nReceivedBytes = 1;
				
			} else if (nHeader == 0xa6) {
				nBytesExpected = mConnection.read() + 2; // + 1 for header + 1 for length byte
				nReceivedBytes = 2;
				receiveMessage = new byte[nBytesExpected];
				
				receiveMessage[1] = (byte)(nBytesExpected - 2);
				
			} else {
				return null;
			}

			receiveMessage[0] = (byte)nHeader;
			while (nReceivedBytes != nBytesExpected) { 
				int nReadBytes = mConnection.read(receiveMessage, nReceivedBytes, nBytesExpected - nReceivedBytes);
				if (nReadBytes == -1) {
					return null;
				} else {
					nReceivedBytes += nReadBytes;
				}
			}
			return receiveMessage;
			
		}
	}
	
	private DottyProtocolHandler mProtocolHandler;

	public void setHandler(Handler handler) {
		mUiHandler = handler;
	}
	
	public void setConnection(BluetoothConnection i_oConnection) {
		m_oConnection = i_oConnection;
		mProtocolHandler = new DottyProtocolHandler(m_oConnection, this);
	}
	
	public BluetoothConnection getConnection() {
		return m_oConnection;
	}
	
	public void destroyConnection() {
		if (mProtocolHandler != null) {
			mProtocolHandler.close();
			mProtocolHandler = null;
		}
		
		if (m_oConnection != null) {
			try {
				m_oConnection.close();
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
		mProtocolHandler.start();
	}
	
	public void disconnect() {
		byte[] message = DottyTypes.getDisconnectPackage();
		m_oConnection.send(message);
		destroyConnection();
	}
	
	
	public void control(boolean i_bEnable) {
		byte[] message = DottyTypes.getControlPackage(i_bEnable);
		m_oConnection.send(message);
	}
	
	public void drive(int i_nLeftVelocity, int i_nRightVelocity) {
		byte[] message = DottyTypes.getDrivePackage(i_nLeftVelocity, i_nRightVelocity);
		m_oConnection.send(message);
	}
	
	public void driveStop() {
		byte[] message = DottyTypes.getDriveStopPackage();
		m_oConnection.send(message);
	}
	
	public void requestSensorData() {
		byte[] message = DottyTypes.getDataRequestPackage();
		m_oConnection.send(message);
	}
	
	public void startStreaming(int i_nInterval) {
		byte[] message = DottyTypes.getStreamingONPackage(i_nInterval);
		m_oConnection.send(message);
	}
	
	public void stopStreaming() {
		byte[] message = DottyTypes.getStreamingOFFPackage();
		m_oConnection.send(message);
	}

	@Override
	public void onMessage(byte[] message) {
		switch (message[0]) {
    	case DottyTypes.HEADER:
    		switch (message[3]) {
            case DottyTypes.SENSOR_DATA:
            	sendStateAndData(DottyTypes.SENSOR_DATA, message);
            	break;
            }
    		break;
    	case DottyTypes.LOGGING:
    		sendStateAndData(DottyTypes.LOGGING, message);
        	break;
    	}
        
    }

    private void sendStateAndData(int i_nCmd, byte[] i_rgbyData) {
    	Utils.sendMessage(mUiHandler, i_nCmd, MsgTypes.assembleRawDataMsg(i_rgbyData));
    }
	
}
