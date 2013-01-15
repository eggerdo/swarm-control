package org.dobots.swarmcontrol.robots;

import java.util.concurrent.TimeoutException;

public interface IBluetoothConnection {
	
	public void send(byte[] buffer);
	
	public byte[] read(int i_nBytes) throws TimeoutException;
	
}
