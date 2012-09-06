package org.dobots.utility;

import org.dobots.swarmcontrol.SwarmControlActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class Utils {
	
	public static int setBit(int i_nVal, int i_nBit) {
		return i_nVal | 1 << i_nBit;
	}
	
	public static int clearBit(int i_nVal, int i_nBit) {
		return i_nVal & ~(1 << i_nBit);
	}
	
	public static boolean IsBitSet(int i_nVal, int i_nBit) {
		return ((i_nVal >> i_nBit) & 1) == 1;
	}
	
	public static short HighLowByteToShort(byte i_byHighByte, byte i_byLowByte) {
		return (short)(((i_byHighByte & 0xFF) << 8) | (i_byLowByte & 0xFF));
	}
	
	public static short LittleEndianToBigEndian(short i_sValue) {
		return (short)(((i_sValue >> 8) & 0xFF) | ((i_sValue & 0xFF) << 8));
	}

	public static int LittleEndianToBigEndian(int i_nValue) {
		return (int) (((i_nValue >> 24) & 0xFF) |
					  ((i_nValue >> 8) & 0xFF00) |
					  ((i_nValue & 0xFF00) << 8) |
					  ((i_nValue & 0xFF) << 24));
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

    public static void waitSomeTime(int millis) {
        try {
            Thread.sleep(millis);

        } catch (InterruptedException e) {
        	e.printStackTrace();
        }
    }

	public static void sendMessage(Handler i_oTarget, int i_nMsgID, Object i_oData) {
		Message msg = Message.obtain();
		msg.what = i_nMsgID;
		msg.obj = i_oData;
		i_oTarget.sendMessage(msg);
	}
	
//    public static void sendBundle(Handler target, Bundle bundle) {
//        Message myMessage = Message.obtain();
//        myMessage.setData(bundle);
//        target.sendMessage(myMessage);
//    }
//    
//    public static void sendDataBundle(Handler target, Bundle bundle, Object data) {
//        Message myMessage = Message.obtain();
//        myMessage.setData(bundle);
//        myMessage.obj = data;
//        target.sendMessage(myMessage);
//    }
    
	public static void showLayout(LinearLayout i_oLayout, boolean i_bShow) {
    	if (i_bShow) {
    		i_oLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    	} else {
    		i_oLayout.setLayoutParams(new LinearLayout.LayoutParams(0,0));
    	}
	}
	
	public static void showView(View i_oView, boolean i_bShow) {
//		if (i_bShow) {
//			i_oView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//    	} else {
//    		i_oView.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT));
//    	}
		if (i_bShow) {
			i_oView.setVisibility(View.VISIBLE);
		} else {
			i_oView.setVisibility(View.GONE);
		}
	}
	
    public static void showToast(String textToShow, int duration) {
    	Toast oToast = Toast.makeText(SwarmControlActivity.getContext(), textToShow, duration);
		oToast.show();
	}

	
}
