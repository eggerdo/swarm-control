package org.dobots.swarmcontrol.robots.dotty;

import java.io.IOException;

import org.dobots.robots.dotty.DottyTypes;
import org.dobots.utilities.Utils;

import robots.gui.BluetoothConnection;
import robots.gui.MessageTypes;
import robots.nxt.MsgTypes;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

public class DottyBluetooth extends BluetoothConnection {

    private byte[] returnMessage;
    
	public DottyBluetooth(BluetoothDevice i_oDevice) {
		super(i_oDevice, DottyTypes.DOTTY_UUID);
	}

//    @Override
//    public void execute() throws IOException {
//		returnMessage = receiveMessage();
//		if (returnMessage != null) {
//			dispatchMessage(returnMessage);
//		}
//    }

	protected byte[] receiveMessage() throws IOException {
		int nHeader = m_oInStream.read();
		byte[] receiveMessage;
		int nBytesExpected;
		int nReceivedBytes;
		
		if (nHeader == 0xa5) {
			receiveMessage = new byte[DottyTypes.DATA_PKG_SIZE];

			nBytesExpected = receiveMessage.length;
			nReceivedBytes = 1;
			
		} else if (nHeader == 0xa6) {
			nBytesExpected = m_oInStream.read() + 2; // + 1 for header + 1 for length byte
			nReceivedBytes = 2;
			receiveMessage = new byte[nBytesExpected];
			
			receiveMessage[1] = (byte)(nBytesExpected - 2);
			
		} else {
			return null;
		}

		receiveMessage[0] = (byte)nHeader;
		while (nReceivedBytes != nBytesExpected) { 
			int nReadBytes = m_oInStream.read(receiveMessage, nReceivedBytes, nBytesExpected - nReceivedBytes);
			if (nReadBytes == -1) {
				return null;
			} else {
				nReceivedBytes += nReadBytes;
			}
		}
		return receiveMessage;
		
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

    private void dispatchMessage(byte[] message) {
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
    	Utils.sendMessage(m_oUiHandler, i_nCmd, MsgTypes.assembleRawDataMsg(i_rgbyData));
    }

}
