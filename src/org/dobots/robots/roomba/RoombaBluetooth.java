package org.dobots.robots.roomba;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;


import robots.ctrl.ProtocolHandler;
import robots.ctrl.ProtocolHandler.ICommHandler;
import robots.gui.BluetoothConnection;
import robots.gui.IRobotConnection;
import robots.gui.MessageTypes;
import robots.nxt.ctrl.LCPMessage;
import android.bluetooth.BluetoothDevice;

public class RoombaBluetooth extends BluetoothConnection implements IRobotConnection, ICommHandler<byte[]> {
	
//	private Object m_oListener;
	
	private boolean m_bMsgReceived;
	private byte[] m_rgRxBuffer;
	private int m_nRxBytes;

//	private Object m_oParent;

    private class RoombaProtocolHandler extends ProtocolHandler {

		public RoombaProtocolHandler(BluetoothConnection connection,	ICommHandler handler) {
			super(connection, handler);
		}

	    /**
	     * Creates the connection, waits for incoming messages and dispatches them. The thread will be terminated
	     * on closing of the connection.
	     * @throws IOException 
	     */
		@Override
		public void execute() {
			try {
				receiveMessage();
				synchronized(this) {
					this.notify();
				}
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
    
    private RoombaProtocolHandler mProtocolHandler = new RoombaProtocolHandler(this, this);
    
	public RoombaBluetooth(BluetoothDevice i_oDevice) {
		super(i_oDevice, RoombaTypes.ROOMBA_UUID);
		m_rgRxBuffer = new byte[1024];
	}
	  
    @Override
    public boolean open() {
    	if (super.open()) {
    		mProtocolHandler.start();
    		return true;
    	}
    	return false;
    }
    
    @Override
    public void close() throws IOException {
    	mProtocolHandler.close();
    	super.close();
    }
    
	public void write(byte[] buffer) {
		try {
			m_oOutStream.write(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void send(byte[] buffer) {
		if (connected) {
			write(buffer);
		}
	}
	
	protected byte[] receiveMessage() throws IOException {
		m_nRxBytes = m_oInStream.read(m_rgRxBuffer);
		m_bMsgReceived = true;
		return null;
	}
	
	public synchronized byte[] getReply() throws TimeoutException {
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
		
		return buffer;
	}

	@Override
	public void onMessage(byte[] message) {
		// TODO Auto-generated method stub
	}
	

}
