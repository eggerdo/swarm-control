package org.dobots.swarmcontrol.robots.robo40;

import java.io.DataInputStream;
import java.io.IOException;

import org.dobots.robots.BaseBluetooth;
import org.dobots.robots.msg.MsgTypes;
import org.dobots.robots.robo40.Robo40Types;
import org.dobots.utilities.Utils;

import robots.gui.MessageTypes;
import android.bluetooth.BluetoothDevice;

public class Robo40Bluetooth extends BaseBluetooth {

    private byte[] returnMessage;
    
    private DataInputStream dis;
    
	public Robo40Bluetooth(BluetoothDevice i_oDevice) {
		super(i_oDevice);
		m_oUUID = Robo40Types.DOTTY_UUID;
        m_strRobotName = "Dotty";
	}
	
    @Override
    public void run() {

    	startUp();
    	
		while (connected && !m_bStopped) {
			try {
				returnMessage = receiveMessage();
				if (returnMessage != null) {
					dispatchMessage(returnMessage);
				}
			} catch (IOException e) {
				if (connected) {
                	connected = false;
                	// TODO Auto-generated catch block
                	e.printStackTrace();
				}
			}
			
		}

    }
    
    @Override
    public void connect() throws IOException {
    	// TODO Auto-generated method stub
    	super.connect();
    	
    	dis = new DataInputStream(m_oInStream);
    }

	private byte[] receiveMessage() throws IOException {
		if (dis.available() > 0) {
			String jsonString = dis.readLine();
			return jsonString.getBytes();
		}
		return null;
	}
	
	public void sendMessage(byte[] buffer) {
		try {
			m_oOutStream.write(buffer);
		} catch (IOException e) {
			connected = false;
            sendState(MessageTypes.STATE_SENDERROR);
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
	
    private void dispatchMessage(byte[] message) {
//    	switch (message[0]) {
//    	case Robo40Types.HEADER:
//    		switch (message[3]) {
//            case Robo40Types.SENSOR_DATA:
            	sendStateAndData(Robo40Types.SENSOR_DATA, message);
//            	break;
//            }
//    		break;
//    	case Robo40Types.LOGGING:
//    		sendStateAndData(Robo40Types.LOGGING, message);
//        	break;
//    	}
        
    }

    private void sendStateAndData(int i_nCmd, byte[] i_rgbyData) {
    	Utils.sendMessage(m_oReceiveHandler, i_nCmd, MsgTypes.assembleRawDataMsg(i_rgbyData));
    }

}
