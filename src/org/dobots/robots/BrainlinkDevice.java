package org.dobots.robots;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.res.Resources;
import android.os.Environment;
import edu.cmu.ri.createlab.brainlink.BluetoothConnection;
import edu.cmu.ri.createlab.brainlink.BrainLink;

public class BrainlinkDevice {
	
	public static final String TAG = "BrainlinkDevice";
	
	public static final String CONFIG_DIRECTORY = "/BirdBrainTechnologies/BrainLink/devices/";
	
	public enum BrainlinkSensors {
		ACCELEROMETER,
		LIGHT,
		BATTERY
	}
	
	public class AccelerometerData {
		public double xaxis;
		public double yaxis;
		public double zaxis;
		
		public AccelerometerData(double[] i_dblValues) {
			xaxis = i_dblValues[0];
			yaxis = i_dblValues[1];
			zaxis = i_dblValues[2];
		}
	}

	protected BluetoothConnection m_oConnection;
	protected BrainLink m_oBrainLink;
	protected boolean m_bConnected;

	public void sendCommand(String i_strCommand) {
		m_oBrainLink.transmitIRSignal(i_strCommand);
	}
	
	public AccelerometerData getAccelerometerData() {
		double[] data = m_oBrainLink.getAccelerometerValuesInGs();
		if (data != null) {
			return new AccelerometerData(data);
		} else {
			return null;
		}
	}
	
	public double getXAccelerometer() {
		return m_oBrainLink.getXAccelerometer();
	}

	public double getYAccelerometer() {
		return m_oBrainLink.getYAccelerometer();
	}

	public double getZAccelerometer() {
		return m_oBrainLink.getZAccelerometer();
	}
	
	public Integer getLight() {
		return m_oBrainLink.getLightSensor();
	}
	
	public Integer getBattery() {
		return m_oBrainLink.getBatteryVoltage();
	}
	
	public boolean wasTapped() {
		return m_oBrainLink.wasTapped();
	}
	
	public boolean wasShaken() {
		return m_oBrainLink.wasShaken();
	}

	public boolean isConnected() {
		return m_bConnected;
	}
	
	public void close() {
		m_oBrainLink.close();
	}
	
	public static boolean checkForConfigFile(Resources i_oResources, String i_strName, boolean i_bEncoded) {
		String strFileName = i_strName + (i_bEncoded ? ".encsig" : ".rawsig");
		String strPath = Environment.getExternalStorageDirectory() + CONFIG_DIRECTORY;
		String strFileNamePath = strPath + strFileName;
		
		// check if the directory path exists and if not create it
		File directory = new File(strPath);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		
		File helper = new File(strFileNamePath);
		if (helper.exists()) {
			// already there, nothing to do
			return true;
		}
		
		try {
			InputStream is = i_oResources.getAssets().open(strFileName);
			
			byte[] bytes = new byte[is.available()];
			DataInputStream dis = new DataInputStream(is);
			dis.readFully(bytes);
			
			FileOutputStream dest = new FileOutputStream(strFileNamePath);
			dest.write(bytes);
			dest.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
}
