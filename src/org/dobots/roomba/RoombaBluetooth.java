package org.dobots.roomba;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;

public class RoombaBluetooth implements RoombaConnection {
	
	private BTConnectionThread m_oConnectionHandler; 

	private class BTConnectionThread extends Thread {
		private BluetoothSocket m_oSocket;
		private InputStream m_oInStream;
		private OutputStream m_oOutStream;
		
		private Object m_oParent;
		
		public BTConnectionThread(Object i_oParent, BluetoothSocket i_oSocket) {
			m_oSocket = i_oSocket;
			m_oParent = i_oParent;
			
			try {
				m_oInStream = m_oSocket.getInputStream();
				m_oOutStream = m_oSocket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void run() {
//			byte[] buffer = new byte[1024];
			m_rgRxBuffer = new byte[1024];
//			int bytes;
			
			while (true) {
				try {
					m_nRxBytes = m_oInStream.read(m_rgRxBuffer);
					m_bMsgReceived = true;
					synchronized(m_oParent) {
						m_oParent.notify();
					}
//					m_oHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//							  .sendToTarget();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
			}
		}
		
		public void write(byte[] buffer) {
			try {
				m_oOutStream.write(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void close() {
			try {
				m_oSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
