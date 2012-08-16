package org.dobots.robots.nxt.msg;

import java.util.Arrays;

public class RawDataMsg {
	
	public byte[] rgbyRawData;
	
	public RawDataMsg(byte[] i_rgbyData) {
		rgbyRawData = Arrays.copyOf(i_rgbyData, i_rgbyData.length);
	}
	
}
