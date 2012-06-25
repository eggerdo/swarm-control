package org.dobots.swarmcontrol.robots;

import org.dobots.roomba.Roomba;
import org.dobots.roomba.RoombaTypes.ERoombaSensorPackages;
import org.dobots.roomba.RoombaTypes.SensorPackage;
import org.dobots.roomba.RoombaTypes.SensorPackage3;
import org.dobots.swarmcontrol.R;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class RoombaSensorGatherer extends Thread {

	private ERoombaSensorPackages eSensor;
	private Roomba oRoomba;

	private Handler oHandler;
	private UpdateSensorDataTask oGUIUpdater;
	private SensorPackage oSensorData;
	private Activity m_oActivity;
	
	public RoombaSensorGatherer(Activity i_oActivity, Roomba i_oRoomba) {
		m_oActivity = i_oActivity;
		oRoomba = i_oRoomba;
		
		oHandler = new Handler();
		oGUIUpdater = new UpdateSensorDataTask();
	}

	public void setSensor(ERoombaSensorPackages i_eSensor) {
		eSensor = i_eSensor;
		start();
	}
	
	private class UpdateSensorDataTask implements Runnable {

		public void run() {
			TableRow oSensorPackage3 = (TableRow) m_oActivity.findViewById(R.id.tblrwSensorPackage3);
			oSensorPackage3.setVisibility(View.INVISIBLE);
			
			switch (eSensor) {
//			case sensPkg_1:
//				TableRow BumpsWheeldrops_Title = new TableRow(m_oActivity);
//				
//				TextView oElement = new TextView(m_oActivity);
//				oElement.setText("Caster");
//				BumpsWheeldrops_Title.addView(oElement);
//
//				oElement = new TextView(m_oActivity);
//				oElement.setText("Left");
//				BumpsWheeldrops_Title.addView(oElement);
//
//				oElement = new TextView(m_oActivity);
//				oElement.setText("Right");
//				BumpsWheeldrops_Title.addView(oElement);
//
//				oElement = new TextView(m_oActivity);
//				oElement.setText("Bump Left");
//				BumpsWheeldrops_Title.addView(oElement);
//
//				oElement = new TextView(m_oActivity);
//				oElement.setText("Bump Right");
//				BumpsWheeldrops_Title.addView(oElement);
//				
//				oElement = new TextView(m_oActivity);
//				oElement.setText("Left");
//				BumpsWheeldrops_Title.addView(oElement);
			case sensPkg_3:
				if (oSensorData != null) {
					oSensorPackage3.setVisibility(View.VISIBLE);
					SensorPackage3 oData = (SensorPackage3)oSensorData;
					TextView oElement;
					
					oElement = (TextView) m_oActivity.findViewById(R.id.txtChargingState);
					oElement.setText(oData.eChargingState.toString());
					
					oElement = (TextView) m_oActivity.findViewById(R.id.txtCharge);
					CharSequence strTmp = String.format("%.2f", (float)oData.sCharge / oData.sCapacity * 100) + "% ( " + oData.sCharge + 
										  " / " + oData.sCapacity + " mAh )";
					oElement.setText(strTmp);
	
					oElement = (TextView) m_oActivity.findViewById(R.id.txtPower);
					strTmp = "( " + oData.sCurrent + " mA, " + oData.sVoltage + " mV )";
					oElement.setText(strTmp);
	
					oElement = (TextView) m_oActivity.findViewById(R.id.txtTemperature);
					oElement.setText(oData.byTemperature + " C");
				}
			}

		    oHandler.postDelayed(this, 100);
		}
		
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
		case sensPkg_3:
			oSensorData = oRoomba.getSensors(ERoombaSensorPackages.sensPkg_3);
			break;
		default:
			stop();
			break;
		}
	}

	public void showSensorPackage3(Boolean i_bShow) {
		TableLayout tblLayout = (TableLayout) m_oActivity.findViewById(R.id.tblData);
		if (i_bShow) {
			tblLayout.setVisibility(View.VISIBLE);
			setSensor(ERoombaSensorPackages.sensPkg_3);
			oHandler.postDelayed(oGUIUpdater, 100);
		} else {
			tblLayout.setVisibility(View.INVISIBLE);
			oHandler.removeCallbacks(oGUIUpdater);
			stop();
		}
	}

}
