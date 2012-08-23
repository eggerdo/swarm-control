package org.dobots.robots.roomba;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import org.dobots.robots.nxt.NXTTypes;
import org.dobots.robots.nxt.msg.MsgTypes;
import org.dobots.swarmcontrol.R;
import org.dobots.utility.Utils;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

public abstract class BaseBluetooth extends Thread {

    public static final int DISPLAY_TOAST = 1000;
    public static final int STATE_CONNECTED = 1001;
    public static final int STATE_CONNECTERROR = 1002;
    public static final int STATE_RECEIVEERROR = 1003;
    public static final int STATE_SENDERROR = 1004;
    public static final int STATE_CONNECTERROR_PAIRING = 1005;
	
    
	protected BluetoothDevice m_oDevice = null;
	protected BluetoothSocket m_oSocket = null;
	protected InputStream m_oInStream = null;
	protected OutputStream m_oOutStream = null;

	protected Handler m_oReceiveHandler = null;

	protected boolean connected = false;
	protected boolean m_bStopped = false;
	
	protected UUID m_oUUID = null;
	protected String m_strRobotName = "";
	protected String m_strMacAddress = "";
	
	public BaseBluetooth(BluetoothDevice i_oDevice) {
		this.m_oDevice = i_oDevice;
		this.m_strMacAddress = i_oDevice.getAddress();
	}

    public void setReceiveHandler(Handler i_oHandler) {
    	m_oReceiveHandler = i_oHandler;
    }
    
    public String getAddress() {
    	return m_strMacAddress;
    }

    /**
     * @return the current status of the connection
     */            
    public boolean isConnected() {
        return connected;
    }

	public void startThread() {
		this.start();
	}
	
	public void stopThread() {
		m_bStopped = true;
	}
	
	protected void startUp() {

        try {        
            createConnection();
            connect();
        }
        catch (IOException e) { }

	}

    public void createConnection() throws IOException {
        if (m_oDevice == null) {
            if (m_oReceiveHandler == null)
                throw new IOException();
            else {
                sendToast("No paired " + m_strRobotName + " robot found!");
                sendState(STATE_CONNECTERROR);
                return;
            }
        }
        m_oSocket = m_oDevice.createRfcommSocketToServiceRecord(m_oUUID);
    }

    /**
     * Closes the bluetooth connection. On error the method either sends a message
     * to it's owner or creates an exception in the case of no message handler.
     */
    public void destroyConnection() throws IOException {
        try {
            if (m_oSocket != null) {
                connected = false;
                m_oSocket.close();
                m_oSocket = null;
            }

            m_oInStream = null;
            m_oOutStream = null;

        } catch (IOException e) {
            if (m_oReceiveHandler == null)
                throw e;
            else
                sendToast("Problem in closing the connection!");
        }
    }

    public void connect() throws IOException {
    	if (m_oSocket == null) {
    		int i = 0;
    		return;
    	}
        try {
	    	try {
	    		m_oSocket.connect();
	        }
	        catch (IOException e) {  
//	            if (myOwner.isPairing()) {
//	                if (m_oReceiveHandler != null) {
//	                    sendToast(mResources.getString(R.string.pairing_message));
//	                    sendState(NXTTypes.STATE_CONNECTERROR_PAIRING);
//	                }
//	                else
//	                    throw e;
//	                return;
//	            }
	
	            // try another method for connection, this should work on the HTC desire, credits to Michael Biermann
	            try {
	                Method mMethod = m_oDevice.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
	                m_oSocket = (BluetoothSocket) mMethod.invoke(m_oDevice, Integer.valueOf(1));            
	                m_oSocket.connect();
	            }
	            catch (Exception e1){
	                if (m_oReceiveHandler == null)
	                    throw new IOException();
	                else
	                    sendState(STATE_CONNECTERROR);
	                return;
	            }
	        }
	    	m_oInStream = m_oSocket.getInputStream();
	    	m_oOutStream = m_oSocket.getOutputStream();
	        connected = true;
	    } catch (IOException e) {
	        if (m_oReceiveHandler == null)
	            throw e;
	        else {
//	            if (myOwner.isPairing())
//	                sendToast(mResources.getString(R.string.pairing_message));
	            sendState(STATE_CONNECTERROR);
	            return;
	        }
	    }
	    // everything was OK
	    if (m_oReceiveHandler != null) {
	        sendState(STATE_CONNECTED);
	    }
    }

    protected void sendToast(String toastText) {
    	Utils.sendMessage(m_oReceiveHandler, DISPLAY_TOAST, toastText);
    }

    protected void sendState(int i_nCmd) {
    	Utils.sendMessage(m_oReceiveHandler, i_nCmd, null);
    }

}
