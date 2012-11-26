package org.dobots.robots.roboscooper;

import edu.cmu.ri.createlab.brainlink.BluetoothConnection;
import edu.cmu.ri.createlab.brainlink.BrainLink;

public class BrainlinkDevice {
	
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

	protected void sendCommand(String i_strCommand) {
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
	
}
