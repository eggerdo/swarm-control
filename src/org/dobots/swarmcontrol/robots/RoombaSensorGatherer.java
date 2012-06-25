package org.dobots.swarmcontrol.robots;

import org.dobots.roomba.Roomba;
import org.dobots.roomba.RoombaTypes.ERoombaSensorPackages;
import org.dobots.roomba.RoombaTypes.SensorPackage;
import org.dobots.roomba.RoombaTypes.SensorPackage1;
import org.dobots.roomba.RoombaTypes.SensorPackage2;
import org.dobots.roomba.RoombaTypes.SensorPackage3;
import org.dobots.swarmcontrol.R;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.widget.TableLayout;
import android.widget.TextView;

public class RoombaSensorGatherer extends Thread {

	private ERoombaSensorPackages eSensor;
	private Roomba oRoomba;

	private Handler oHandler;
	private UpdateSensorDataTask oGUIUpdater;
	private SensorPackage oSensorData;
	private Activity m_oActivity;
	
	private boolean m_bEnabled = false;
	
	public RoombaSensorGatherer(Activity i_oActivity, Roomba i_oRoomba) {
		m_oActivity = i_oActivity;
		oRoomba = i_oRoomba;
		
		oHandler = new Handler();
		oGUIUpdater = new UpdateSensorDataTask();
		
		start();
	}

	public void setSensor(ERoombaSensorPackages i_eSensor) {
		eSensor = i_eSensor;
	}

	@Override
	public void run() {
		
		while (true) {
			if (m_bEnabled) {
				oSensorData = oRoomba.getSensors(eSensor);
			}
		
			try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
	}

	public void showSensorPackage(ERoombaSensorPackages eSensorPkg) {
		TableLayout oSensorPackage1 = (TableLayout) m_oActivity.findViewById(R.id.tblSensorData1);
		TableLayout oSensorPackage2 = (TableLayout) m_oActivity.findViewById(R.id.tblSensorData2);
		TableLayout oSensorPackage3 = (TableLayout) m_oActivity.findViewById(R.id.tblSensorData3);

		oSensorPackage1.setLayoutParams(new TableLayout.LayoutParams(0, 0));
		oSensorPackage2.setLayoutParams(new TableLayout.LayoutParams(0, 0));
		oSensorPackage3.setLayoutParams(new TableLayout.LayoutParams(0, 0));
		
		if (eSensorPkg == ERoombaSensorPackages.sensPkg_None) {
			oHandler.removeCallbacks(oGUIUpdater);
			m_bEnabled = false;
		} else {
			switch (eSensorPkg) {
			case sensPkg_1:
				oSensorPackage1.setLayoutParams(new TableLayout.LayoutParams());
				break;
			case sensPkg_2:
				oSensorPackage2.setLayoutParams(new TableLayout.LayoutParams());
				break;
			case sensPkg_3:
				oSensorPackage3.setLayoutParams(new TableLayout.LayoutParams());
				break;
			}
	
			setSensor(eSensorPkg);
			oHandler.postDelayed(oGUIUpdater, 100);
			m_bEnabled = true;
		}
	}

	private class UpdateSensorDataTask implements Runnable {

		public void run() {
			
			switch (eSensor) {
			case sensPkg_1:
				if (SensorPackage1.class.isInstance(oSensorData)) 
					updateSensorData1();
				break;
			case sensPkg_2:
				if (SensorPackage2.class.isInstance(oSensorData))
					updateSensorData2();
				break;
			case sensPkg_3:
				if (SensorPackage3.class.isInstance(oSensorData))
					updateSensorData3();
				break;
			}
		    oHandler.postDelayed(this, 100);
		}
			
		private void updateSensorData1() {
	
			SensorPackage1 oSensorData1 = (SensorPackage1)oSensorData;
			
			TextView oElement = (TextView) m_oActivity.findViewById(R.id.txtCasterWD);
			setBoolElement(oElement, oSensorData1.oBumpsWheeldrops.bCaster_Wheeldrop);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtLeftWD);
			setBoolElement(oElement, oSensorData1.oBumpsWheeldrops.bLeft_Wheeldrop);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtRightWD);
			setBoolElement(oElement, oSensorData1.oBumpsWheeldrops.bRight_Wheeldrop);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtBumpLeft);
			setBoolElement(oElement, oSensorData1.oBumpsWheeldrops.bLeft_Bump);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtBumpRight);
			setBoolElement(oElement, oSensorData1.oBumpsWheeldrops.bRight_Bump);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtWall);
			setBoolElement(oElement, oSensorData1.bWall);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtCliffLeft);
			setBoolElement(oElement, oSensorData1.bCliffLeft);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtCliffFrontLeft);
			setBoolElement(oElement, oSensorData1.bCliffFrontLeft);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtCliffFrontRight);
			setBoolElement(oElement, oSensorData1.bCliffFrontRight);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtCliffRight);
			setBoolElement(oElement, oSensorData1.bCliffRight);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtVirtualWall);
			setBoolElement(oElement, oSensorData1.bVirtualWall);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtDriveLeft);
			setBoolElement(oElement, oSensorData1.oMotorOvercurrents.bDriveLeft);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtDriveRight);
			setBoolElement(oElement, oSensorData1.oMotorOvercurrents.bDriveRight);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtMainBrush);
			setBoolElement(oElement, oSensorData1.oMotorOvercurrents.bMainBrush);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtVacuum);
			setBoolElement(oElement, oSensorData1.oMotorOvercurrents.bVacuum);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtSideBrush);
			setBoolElement(oElement, oSensorData1.oMotorOvercurrents.bSideBrush);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtDirtLeft);
			oElement.setText(String.valueOf(oSensorData1.byDirtDetectionLeft));
			
			oElement = (TextView) m_oActivity.findViewById(R.id.txtDirtRight);
			oElement.setText(String.valueOf(oSensorData1.byDirtDetectionRight));
			
		}
		
		private void updateSensorData2() {
	
			SensorPackage2 oSensorData2 = (SensorPackage2)oSensorData;
			
			TextView oElement = (TextView) m_oActivity.findViewById(R.id.txtRemoteOpCode);
			oElement.setText(String.valueOf(oSensorData2.byRemoteOpCode));
			
			oElement = (TextView) m_oActivity.findViewById(R.id.txtPower);
			setBoolElement(oElement, oSensorData2.oButtonsPressed.bPower);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtSpot);
			setBoolElement(oElement, oSensorData2.oButtonsPressed.bSpot);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtClean);
			setBoolElement(oElement, oSensorData2.oButtonsPressed.bClean);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtMax);
			setBoolElement(oElement, oSensorData2.oButtonsPressed.bMax);
			
			oElement = (TextView) m_oActivity.findViewById(R.id.txtDistance);
			oElement.setText(String.valueOf(oSensorData2.sDistance) + " mm");
			
			oElement = (TextView) m_oActivity.findViewById(R.id.txtAngle);
			oElement.setText(String.valueOf(oSensorData2.sAngle) + " mm");
			
		}
		
		private void updateSensorData3() {
	
			SensorPackage3 oSensorData3 = (SensorPackage3)oSensorData;
			
			TextView oElement = (TextView) m_oActivity.findViewById(R.id.txtChargingState);
			oElement.setText(oSensorData3.eChargingState.toString());
			
			oElement = (TextView) m_oActivity.findViewById(R.id.txtCharge);
			CharSequence strTmp = String.format("%.2f", (float)oSensorData3.sCharge / oSensorData3.sCapacity * 100) + "% ( " + oSensorData3.sCharge + 
								  " / " + oSensorData3.sCapacity + " mAh )";
			oElement.setText(strTmp);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtPower);
			strTmp = "( " + oSensorData3.sCurrent + " mA, " + oSensorData3.sVoltage + " mV )";
			oElement.setText(strTmp);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtTemperature);
			oElement.setText(oSensorData3.byTemperature + " C");
			
		}

		private void setBoolElement(TextView io_oElement, boolean i_bBool) {
			io_oElement.setText(Boolean.toString(i_bBool));
			if (i_bBool) {
				io_oElement.setBackgroundColor(Color.RED);
			} else {
				io_oElement.setBackgroundColor(Color.GREEN);
			}
		}
		
	}

}
