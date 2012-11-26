package org.dobots.swarmcontrol.robots.roboscooper;

import java.util.EnumMap;

import org.dobots.robots.dotty.DottyTypes.EDottySensors;
import org.dobots.robots.nxt.NXTTypes.ENXTMotorID;
import org.dobots.robots.nxt.NXTTypes.ENXTMotorSensorType;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorID;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorType;
import org.dobots.robots.roboscooper.BrainlinkDevice.AccelerometerData;
import org.dobots.robots.roboscooper.BrainlinkDevice.BrainlinkSensors;
import org.dobots.robots.roboscooper.BrainlinkDevice;
import org.dobots.robots.roboscooper.RoboScooper;
import org.dobots.swarmcontrol.BaseActivity;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.SensorGatherer;
import org.dobots.utility.Utils;

import android.widget.TableLayout;
import android.widget.TextView;

public class BrainlinkSensorGatherer extends SensorGatherer {
	
	private BrainlinkDevice m_oDevice;
	
	private EnumMap<BrainlinkSensors, Boolean> m_oSensorEnabled;

	private TextView m_txtXAxisValue;
	private TextView m_txtYAxisValue;
	private TextView m_txtZAxisValue;
	private TextView m_txtBattery;
	private TextView m_txtLight;
	
	private TableLayout m_tblAccelerometer;
	private TableLayout m_tblBattery;
	private TableLayout m_tblLight;

	public BrainlinkSensorGatherer(BaseActivity i_oActivity, BrainlinkDevice i_oDevice) {
		super(i_oActivity);
		
		m_oDevice = i_oDevice;
		
		m_oSensorEnabled = new EnumMap<BrainlinkSensors, Boolean>(BrainlinkSensors.class);

		m_txtXAxisValue = (TextView) m_oActivity.findViewById(R.id.txtXAxisValue);
		m_txtYAxisValue = (TextView) m_oActivity.findViewById(R.id.txtYAxisValue);
		m_txtZAxisValue = (TextView) m_oActivity.findViewById(R.id.txtZAxisValue);
		m_txtBattery = (TextView) m_oActivity.findViewById(R.id.txtBatteryValue);
		m_txtLight = (TextView) m_oActivity.findViewById(R.id.txtLightIntensityValue);
		
		m_tblAccelerometer = (TableLayout) m_oActivity.findViewById(R.id.tblAccelerometer_data);
		m_tblBattery = (TableLayout) m_oActivity.findViewById(R.id.tblBattery_data);
		m_tblLight = (TableLayout) m_oActivity.findViewById(R.id.tblLight_data);
		
		// set up the maps
		initialize();
		
		start();
	}
	
	public void initialize() {
		// set up the maps
		for (BrainlinkSensors sensor : BrainlinkSensors.values()) {
			m_oSensorEnabled.put(sensor, false);
			showSensor(sensor, false);
		}
	}
	
	@Override
	protected void execute() {

		if (m_oDevice.isConnected()) {
			for (BrainlinkSensors sensor : m_oSensorEnabled.keySet()) {
				if (m_oSensorEnabled.get(sensor)) {
					switch(sensor) {
					case ACCELEROMETER:
						updateAccelerometer();
						break;
					case BATTERY:
						updateBattery();
						break;
					case LIGHT:
						updateLight();
						break;
					}
				}
			}
		}
	}
	
	private void updateAccelerometer() {
		final AccelerometerData oData = m_oDevice.getAccelerometerData();
		
		if (oData != null) {
			m_oUiHandler.post(new Runnable() {
				
				@Override
				public void run() {
					setText(m_txtXAxisValue, (float)oData.xaxis);
					setText(m_txtYAxisValue, (float)oData.yaxis);
					setText(m_txtZAxisValue, (float)oData.zaxis);
				}
			});
		}
	}
	
	private void updateBattery() {
		final Integer nBattery = m_oDevice.getBattery();

		if (nBattery != null) {
			m_oUiHandler.post(new Runnable() {
				
				@Override
				public void run() {
					setText(m_txtBattery, nBattery);
				}
			});
		}
	}
	
	private void updateLight() {
		final Integer nLight = m_oDevice.getLight();

		if (nLight != null) {
			m_oUiHandler.post(new Runnable() {
				
				@Override
				public void run() {
					setText(m_txtLight, nLight);
				}
			});
		}
	}
	

	public void enableSensor(BrainlinkSensors i_eSensor, boolean i_bEnabled) {
		m_oSensorEnabled.put(i_eSensor, i_bEnabled);
		showSensor(i_eSensor, i_bEnabled);
	}
	

	public void showSensor(BrainlinkSensors i_eSensor, boolean i_bShow) {
		switch(i_eSensor) {
		case ACCELEROMETER:
			Utils.showLayout(m_tblAccelerometer, i_bShow);
			break;
		case BATTERY:
			Utils.showLayout(m_tblBattery, i_bShow);
			break;
		case LIGHT:
			Utils.showLayout(m_tblLight, i_bShow);
			break;
		}
	}
	
}
