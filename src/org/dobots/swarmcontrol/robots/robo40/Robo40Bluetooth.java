package org.dobots.swarmcontrol.robots.robo40;

import java.io.DataInputStream;
import java.io.IOException;

import org.dobots.robots.robo40.Robo40Types;
import org.dobots.utilities.Utils;

import robots.gui.BluetoothConnection;
import robots.gui.MessageTypes;
import robots.nxt.MsgTypes;
import android.bluetooth.BluetoothDevice;

public class Robo40Bluetooth extends BluetoothConnection {

//    private byte[] returnMessage;
    
	public Robo40Bluetooth(BluetoothDevice i_oDevice) {
		super(i_oDevice, Robo40Types.DOTTY_UUID);
	}
	
//    @Override
//    public void execute() throws IOException {
//		returnMessage = receiveMessage();
//		if (returnMessage != null) {
//			dispatchMessage(returnMessage);
//		}
//    }

	protected byte[] receiveMessage() throws IOException {
		if (m_oDataIn.available() > 0) {
			String jsonString = m_oDataIn.readLine();
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

}
