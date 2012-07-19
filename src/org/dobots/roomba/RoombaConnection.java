package org.dobots.roomba;

import java.util.concurrent.TimeoutException;

public interface RoombaConnection {
	
	public void send(byte[] buffer);
	
	public byte[] read(int i_nBytes) throws TimeoutException;
	
}
