package org.dobots.roomba;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
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
	}
	
//	public void setListener(Object i_oListener) {
//		m_oListener = i_oListener;
//	}

	private class BTConnectionThread extends Thread {
		private InputStream m_oInStream;
		private OutputStream m_oOutStream;
		
		private Object m_oParent;
		
		private boolean m_bStopped = false;
		
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
		
		public void startThread() {
			this.start();
		}
		
		public void stopThread() {
			m_bStopped = true;
		}
		
		public void run() {
//			byte[] buffer = new byte[1024];
			m_rgRxBuffer = new byte[1024];
//			int bytes;
			
			while (!m_bStopped) {
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
	}		

	public void open() throws IOException {
		m_oSocket.connect();
		m_oConnectionHandler.startThread();
	}
	
	public void close() {
		try {
			m_oConnectionHandler.stopThread();
			m_oSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void send(byte[] buffer) {
		m_oConnectionHandler.write(buffer);
	}
	
	public synchronized byte[] read() throws TimeoutException {
		byte[] buffer = null;
		
		try {
			wait(5000);
			if (!m_bMsgReceived) {
				// TODO error, no answer received
				throw new TimeoutException("No answer received");
			} else {
				m_bMsgReceived = false;
				
				buffer = new byte[m_nRxBytes];
				
				System.arraycopy(m_rgRxBuffer, 0, buffer, 0, m_nRxBytes);

				Arrays.fill(m_rgRxBuffer, (byte)0);
				m_nRxBytes = 0;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return buffer;
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
					Arrays.fill(m_rgRxBuffer, (byte)0);
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
