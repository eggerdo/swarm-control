package org.dobots.robots.roomba;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import org.dobots.robots.BaseBluetooth;
import org.dobots.robots.MessageTypes;
import org.dobots.robots.nxt.LCPMessage;
import org.dobots.robots.nxt.NXTTypes;
import org.dobots.robots.nxt.msg.MsgTypes;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.BluetoothConnection;
import org.dobots.utility.Utils;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

public class RoombaBluetooth extends BaseBluetooth implements BluetoothConnection {
	
//	private Object m_oListener;
	
	private boolean m_bMsgReceived;
	private byte[] m_rgRxBuffer;
	private int m_nRxBytes;

//	private Object m_oParent;
	
	public RoombaBluetooth(BluetoothDevice i_oDevice) {
		super(i_oDevice);
		m_oUUID = RoombaTypes.ROOMBA_UUID;
		m_strRobotName = "Roomba";
	}

    /**
     * Creates the connection, waits for incoming messages and dispatches them. The thread will be terminated
     * on closing of the connection.
     */
    @Override
    public void run() {

    	startUp();

    	m_rgRxBuffer = new byte[1024];
//			int bytes;
		
		while (connected && !m_bStopped) {
			try {
				m_nRxBytes = m_oInStream.read(m_rgRxBuffer);
				m_bMsgReceived = true;
				synchronized(this) {
					this.notify();
				}
//					m_oHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//							  .sendToTarget();
			} catch (IOException e) {
				if (connected) {
                	connected = false;
                    sendState(MessageTypes.STATE_RECEIVEERROR);
                }
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
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

	public void open() {
		startThread();
	}
	
	public void close() {
		connected = false;
		
		try {
			stopThread();
			m_oSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m_oSocket = null;
		m_oInStream = null;
		m_oOutStream = null;
	}
	
	public void send(byte[] buffer) {
		write(buffer);
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
				wait(500);
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
