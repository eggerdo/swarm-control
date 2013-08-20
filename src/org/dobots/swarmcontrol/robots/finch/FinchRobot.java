package org.dobots.swarmcontrol.robots.finch;

import java.util.Random;
import java.util.Timer;

import org.dobots.swarmcontrol.R;
import org.dobots.utilities.BaseActivity;

import robots.RobotType;
import robots.ctrl.IRobotDevice;
import robots.gui.RobotView;
import robots.gui.SensorGatherer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class FinchRobot extends RobotView {
	
	//	private Finch oFinch;
	private FinchSensorData oData;
	private FinchSensorGatherer oSensorGatherer;
	private Timer oTimer;
	private UpdateSensorDataTask oGUIUpdater;
	private Handler oHandler;
	
	private Random oRand;

	private class FinchSensorData {
		public double dblAccelX, dblAccelY, dblAccelZ;
		double dblTemperature;
		int nLightLeft, nLightRight;
		boolean bIRLeft = false, bIRRight = false;
	}
	
	protected enum FinchSensorType {
		SENS_LIGHT("Light"),
		SENS_TEMPERATURE("Temperature"),
		SENS_ACCELERATION("Acceleration"),
		SENS_OBSTACLE("Obstacle Detection");
		private String strDisplayName;
		
		FinchSensorType(String i_strDisplayName) {
			this.strDisplayName = i_strDisplayName;
		}

		@Override
		public String toString() {
			return strDisplayName;
		}
	}
	
	private class UpdateSensorDataTask implements Runnable {
		
		FinchSensorType eSensor;

		public void setSensor(FinchSensorType i_eSensor) {
			eSensor = i_eSensor;
		}
	
		public void run() {
			switch (eSensor) {
				case SENS_LIGHT:
					
					break;
				case SENS_TEMPERATURE:
					
					break;
				case SENS_ACCELERATION:
//					TextView x_axis = (TextView) m_oActivity.findViewById(R.id.x_axis_value);
//					x_axis.setText(String.format("%.4f", oData.dblAccelZ));
//	
//					TextView y_axis = (TextView) m_oActivity.findViewById(R.id.y_axis_value);
//					y_axis.setText(String.format("%.4f", oData.dblAccelY));
//	
//					TextView z_axis = (TextView) m_oActivity.findViewById(R.id.z_axis_value);
//					z_axis.setText(String.format("%.4f", oData.dblAccelZ));
					break;
				case SENS_OBSTACLE:
					
					break;
			}

		    oHandler.postDelayed(this, 100);

		}

	}
	
	private class FinchSensorGatherer extends Thread {
		
		FinchSensorType eSensor;
		
		public void setSensor(FinchSensorType i_eSensor) {
			eSensor = i_eSensor;
			start();
		}
	
		@Override
		public void run() {
			
			while (true) {
			FetchSensorData();
			
			try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
			}
		}
		
		private void FetchSensorData() {
			switch (eSensor) {
				case SENS_LIGHT:
//					oData.nLightLeft = oFinch.getLeftLightSensor();
//					oData.nLightRight = oFinch.getRightLightSensor();
					break;
				case SENS_TEMPERATURE:
//					oData.dblTemperature = oFinch.getTemperature();
					break;
				case SENS_ACCELERATION:
//					oData.dblAccelX = oFinch.getXAcceleration();
//					oData.dblAccelY = oFinch.getYAcceleration();
//					oData.dblAccelZ = oFinch.getZAcceleration();

//					oData.dblAccelX = oRand.nextDouble();
//					oData.dblAccelY = oRand.nextDouble();
//					oData.dblAccelZ = oRand.nextDouble();
					break;
				case SENS_OBSTACLE:
//					oData.bIRLeft = oFinch.isObstacleLeftSide();
//					oData.bIRRight = oFinch.isObstacleRightSide();
					break;
				default:
					suspend();
					break;
			}
		}
		
	}

	public FinchRobot(BaseActivity i_oOwner) {
		super(i_oOwner);
	}

	protected IRobotDevice getRobot() {
		return null;
	}

	protected SensorGatherer getSensorGatherer() {
		return null;
	}

//	@Override
//	public void show(Activity myActivity, RobotType i_eRobot) {
//		super.show(myActivity, i_eRobot);

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		
//		oFinch = new Finch();
		oData = new FinchSensorData();
		oSensorGatherer = new FinchSensorGatherer();
		oHandler = new Handler();
		oGUIUpdater = new UpdateSensorDataTask();
		
		oRand = new Random();
	}

	@Override
	protected void setProperties(RobotType i_eRobot) {
		// fill spinner
		Spinner spSensors = (Spinner) m_oActivity.findViewById(R.id.spSensors);
		spSensors.setVisibility(View.VISIBLE);
		final ArrayAdapter<FinchSensorType> adapter = new ArrayAdapter<FinchSensorType>(m_oActivity.getApplicationContext(), 
				android.R.layout.simple_spinner_item, FinchSensorType.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spSensors.setAdapter(adapter);
		spSensors.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				FinchSensorType eSensor = adapter.getItem(position);
				displaySensor(eSensor);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// do nothing
			}
			
		});
	}
	
	protected void displaySensor(FinchSensorType i_eSensor) {
		switch (i_eSensor) {
		case SENS_ACCELERATION:
			showAcceleration(true);
			break;
		default:
			showAcceleration(false);
			break;
		}
	}
	
	private void showAcceleration(Boolean i_bShow) {
//		TableLayout tblAcceleration = (TableLayout) m_oActivity.findViewById(R.id.tblAcceleration);
//		if (i_bShow) {
//			tblAcceleration.setVisibility(View.VISIBLE);
//			oSensorGatherer.setSensor(FinchSensorType.SENS_ACCELERATION);
//			oGUIUpdater.setSensor(FinchSensorType.SENS_ACCELERATION);
//			oHandler.postDelayed(oGUIUpdater, 100);
//		} else {
//			tblAcceleration.setVisibility(View.INVISIBLE);
//			oHandler.removeCallbacks(oGUIUpdater);
//		}
	}

	@Override
	protected void onConnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDisconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void connectToRobot() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void disconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void resetLayout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateButtons(boolean i_bEnabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onConnectError() {
		// TODO Auto-generated method stub
		
	}
	
	
}
