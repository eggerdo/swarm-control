package org.dobots.robots;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.dobots.utilities.Utils;

import robots.gui.MessageTypes;
import android.os.Handler;

public abstract class BaseWifi extends Thread {

    protected Socket m_oSocket = null;
	protected InputStream m_oInStream = null;
	protected OutputStream m_oOutStream = null;

	protected Handler m_oReceiveHandler = null;

	protected boolean connected = false;
	protected boolean m_bStopped = false;
	
	protected String m_strRobotName = "";
	protected String m_strSSID_Filter = "";
	
	protected String m_strAddress = "";
	protected int m_nPort = 80;
	
	public BaseWifi(String i_strAddress, int i_nPort) {
		this.m_strAddress = i_strAddress;
		this.m_nPort = i_nPort;
	}

    public void setReceiveHandler(Handler i_oHandler) {
    	m_oReceiveHandler = i_oHandler;
    }
    
    public String getAddress() {
    	return m_strAddress;
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
        catch (IOException e) { 
        	e.printStackTrace();
            sendState(MessageTypes.STATE_CONNECTERROR);
        }

	}

    public void createConnection() throws UnknownHostException, IOException {
        m_oSocket = new Socket();
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
        try {
	    	try {
	    		m_oSocket.connect(new InetSocketAddress(m_strAddress, m_nPort));
	        }
	        catch (IOException e) {  
                if (m_oReceiveHandler == null)
                    throw new IOException();
                else
                    sendState(MessageTypes.STATE_CONNECTERROR);
                return;
            }
	    	
	    	m_oInStream = m_oSocket.getInputStream();
	    	m_oOutStream = m_oSocket.getOutputStream();
	        connected = true;
	    } catch (IOException e) {
	        if (m_oReceiveHandler == null)
	            throw e;
	        else {
	            sendState(MessageTypes.STATE_CONNECTERROR);
	            return;
	        }
	    }
	    // everything was OK
	    if (m_oReceiveHandler != null) {
	        sendState(MessageTypes.STATE_CONNECTED);
	    }
    }

    protected void sendToast(String toastText) {
    	Utils.sendMessage(m_oReceiveHandler, MessageTypes.DISPLAY_TOAST, toastText);
    }

    protected void sendState(int i_nCmd) {
    	Utils.sendMessage(m_oReceiveHandler, i_nCmd, null);
    }

}
