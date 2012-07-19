package org.dobots.utility;

public class Utils {
	
	public static boolean IsBitSet(int i_nVal, int i_nBit) {
		return ((i_nVal >> i_nBit) & 1) == 1;
	}
	
	public static short HighLowByteToShort(byte i_byHighByte, byte i_byLowByte) {
		return (short)(((i_byHighByte & 0xFF) << 8) | (i_byLowByte & 0xFF));
	}

	public static int byteToInt(byte byteValue) {
		int intValue = (byteValue & (byte) 0x7f);

		if ((byteValue & (byte) 0x80) != 0)
			intValue |= 0x80;

		return intValue;
	}
	
	public static byte[] stringToByteArray(String i_strText) {
		byte [] result = new byte[i_strText.length()];
		
        for (int i = 0; i < i_strText.length(); i++)
            result[i] = (byte) i_strText.charAt(i);

        return result;
	}
	
	public static String byteArrayToString(byte[] i_rgbyString) {
		String result = "";
		
		for (int i= 0; i < i_rgbyString.length; i++)
			result += (char) i_rgbyString[i];

		// remove CR and LF from string
		result.replaceAll("\\r\\n", "");
		
		return result;
	}

}
