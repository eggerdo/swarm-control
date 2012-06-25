package org.dobots.roomba;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

import android.bluetooth.BluetoothSocket;

public class RoombaBluetooth implements RoombaConnection {
	
	private BTConnectionThread m_oConnectionHandler; 

	private BluetoothSocket m_oSocket;
	
//	private Object m_oListener;
	
	private boolean m_bMsgReceived;
	private byte[] m_rgRxBuffer;
	private int m_nRxBytes;
	
	public RoombaBluetooth(BluetoothSocket i_oSocket) {
		m_oSocket = i_oSocket;
		m_oConnectionHandler = new BTConnectionThread(this);
		m_oConnectionHandler.start();
	}
	
//	public void setListener(Object i_oListener) {
//		m_oListener = i_oListener;
//	}

	private class BTConnectionThread extends Thread {
		private InputStream m_oInStream;
		private OutputStream m_oOutStream;
		
		private Object m_oParent;
		
		public BTConnectionThread(Object i_oParent) {
//			m_oSocket = i_oSocket;
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
	
	public void send(byte[] buffer) {
		m_oConnectionHandler.write(buffer);
	}
	
	public byte[] read() {
		return null;
	}
	
	public synchronized byte[] read(int i_nBytes) throws TimeoutException {
		int nRequiredBytes = i_nBytes;
		int nReceivedBytes = 0;
		byte[] buffer = new byte[i_nBytes];
		
		while (nReceivedBytes != nRequiredBytes) {
			try {
				wait(5000);
				if (!m_bMsgReceived) {
					// TODO error, no answer received
					throw new TimeoutException("No answer received");
				} else {
					m_bMsgReceived = false;
					
					System.arraycopy(m_rgRxBuffer, 0, buffer, nReceivedBytes, m_nRxBytes);

					nReceivedBytes += m_nRxBytes;
					m_nRxBytes = 0;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//		for (int i = 0; i < nReceivedBytes; i++) {
//			results[i] = buffer[i];
//		}
		return buffer;
	}
	
}
