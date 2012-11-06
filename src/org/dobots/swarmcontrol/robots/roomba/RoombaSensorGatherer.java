package org.dobots.swarmcontrol.robots.roomba;

import org.dobots.robots.nxt.NXTTypes.ENXTMotorID;
import org.dobots.robots.nxt.NXTTypes.ENXTMotorSensorType;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorID;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorType;
import org.dobots.robots.roomba.Roomba;
import org.dobots.robots.roomba.RoombaTypes.ERoombaSensorPackages;
import org.dobots.robots.roomba.RoombaTypes.SensorPackage;
import org.dobots.robots.roomba.RoombaTypes.SensorPackage1;
import org.dobots.robots.roomba.RoombaTypes.SensorPackage2;
import org.dobots.robots.roomba.RoombaTypes.SensorPackage3;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.SensorGatherer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.widget.TableLayout;
import android.widget.TextView;

public class RoombaSensorGatherer extends SensorGatherer {

	private ERoombaSensorPackages m_eSensor;
	private Roomba m_oRoomba;

	private SensorPackage m_oSensorData;
	
	public RoombaSensorGatherer(Activity i_oActivity, Roomba i_oRoomba) {
		super(i_oActivity);
		m_oRoomba = i_oRoomba;
		
		m_oGUIUpdater = new UpdateSensorDataTask();
		
		start();
	}

	@Override
	public void execute() {
		if (m_bEnabled && m_oRoomba.isPowerOn()) {
			m_oSensorData = m_oRoomba.getSensors(m_eSensor);
			m_oHandler.postDelayed(m_oGUIUpdater, 10);
		} else {
			m_oSensorData = null;
		}
	}

	public void setSensor(ERoombaSensorPackages i_eSensor) {
		m_eSensor = i_eSensor;
	}

	public void showSensorPackage(ERoombaSensorPackages eSensorPkg) {
		TableLayout oSensorPackage1 = (TableLayout) m_oActivity.findViewById(R.id.tblSensorData1);
		TableLayout oSensorPackage2 = (TableLayout) m_oActivity.findViewById(R.id.tblSensorData2);
		TableLayout oSensorPackage3 = (TableLayout) m_oActivity.findViewById(R.id.tblSensorData3);

		oSensorPackage1.setLayoutParams(new TableLayout.LayoutParams(0, 0));
		oSensorPackage2.setLayoutParams(new TableLayout.LayoutParams(0, 0));
		oSensorPackage3.setLayoutParams(new TableLayout.LayoutParams(0, 0));
		
		if (eSensorPkg == ERoombaSensorPackages.sensPkg_None) {
			m_oHandler.removeCallbacks(m_oGUIUpdater);
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
			m_oHandler.postDelayed(m_oGUIUpdater, 100);
			m_bEnabled = true;
		}
	}

	private class UpdateSensorDataTask implements Runnable {

		public void run() {
			
			if (m_oRoomba.isPowerOn()) {
			
				switch (m_eSensor) {
				case sensPkg_1:
					if (SensorPackage1.class.isInstance(m_oSensorData)) 
						updateSensorData1();
					break;
				case sensPkg_2:
					if (SensorPackage2.class.isInstance(m_oSensorData))
						updateSensorData2();
					break;
				case sensPkg_3:
					if (SensorPackage3.class.isInstance(m_oSensorData))
						updateSensorData3();
					break;
				}
//			    oHandler.postDelayed(this, 100);
			    
			} else {
				resetSensorData1();
				resetSensorData2();
				resetSensorData3();
			}
		}
		
		private void resetSensorData1() {

			TextView oElement = (TextView) m_oActivity.findViewById(R.id.txtCasterWD);
			oElement.setText("---");
			
			oElement = (TextView) m_oActivity.findViewById(R.id.txtLeftWD);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtRightWD);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtBumpLeft);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtBumpRight);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtWall);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtCliffLeft);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtCliffFrontLeft);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtCliffFrontRight);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtCliffRight);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtVirtualWall);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtDriveLeft);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtDriveRight);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtMainBrush);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtVacuum);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtSideBrush);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtDirtLeft);
			oElement.setText("---");
			
			oElement = (TextView) m_oActivity.findViewById(R.id.txtDirtRight);
			oElement.setText("---");
			
		}
			
		private void updateSensorData1() {
	
			SensorPackage1 oSensorData1 = (SensorPackage1)m_oSensorData;
			
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
		
		private void resetSensorData2() {

			TextView oElement = (TextView) m_oActivity.findViewById(R.id.txtRemoteOpCode);
			oElement.setText("---");
			
			oElement = (TextView) m_oActivity.findViewById(R.id.txtPowerBtn);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtSpotBtn);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtCleanBtn);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtMaxBtn);
			oElement.setText("---");
			
			oElement = (TextView) m_oActivity.findViewById(R.id.txtDistance);
			oElement.setText("---");
			
			oElement = (TextView) m_oActivity.findViewById(R.id.txtAngle);
			oElement.setText("---");
			
		}
		
		private void updateSensorData2() {
	
			SensorPackage2 oSensorData2 = (SensorPackage2)m_oSensorData;
			
			TextView oElement = (TextView) m_oActivity.findViewById(R.id.txtRemoteOpCode);
			oElement.setText(String.valueOf(oSensorData2.byRemoteOpCode));
			
			oElement = (TextView) m_oActivity.findViewById(R.id.txtPowerBtn);
			setBoolElement(oElement, oSensorData2.oButtonsPressed.bPower);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtSpotBtn);
			setBoolElement(oElement, oSensorData2.oButtonsPressed.bSpot);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtCleanBtn);
			setBoolElement(oElement, oSensorData2.oButtonsPressed.bClean);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtMaxBtn);
			setBoolElement(oElement, oSensorData2.oButtonsPressed.bMax);
			
			oElement = (TextView) m_oActivity.findViewById(R.id.txtDistance);
			oElement.setText(String.valueOf(oSensorData2.sDistance) + " mm");
			
			oElement = (TextView) m_oActivity.findViewById(R.id.txtAngle);
			oElement.setText(String.valueOf(oSensorData2.sAngle) + " mm");
			
		}
		
		private void resetSensorData3() {

			TextView oElement = (TextView) m_oActivity.findViewById(R.id.txtChargingState);
			oElement.setText("---");
			
			oElement = (TextView) m_oActivity.findViewById(R.id.txtCharge);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtPower);
			oElement.setText("---");
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtTemperature);
			oElement.setText("---");
			
		}
		
		private void updateSensorData3() {
	
			SensorPackage3 oSensorData3 = (SensorPackage3)m_oSensorData;
			
			TextView oElement = (TextView) m_oActivity.findViewById(R.id.txtChargingState);
			oElement.setText(oSensorData3.eChargingState.toString());
			
			oElement = (TextView) m_oActivity.findViewById(R.id.txtCharge);
			CharSequence strTmp = String.format("%.2f", (float)oSensorData3.sCharge / oSensorData3.sCapacity * 100) + "%" + 
								  " ( " + oSensorData3.sCharge + " / " + oSensorData3.sCapacity + " mAh )";
			oElement.setText(strTmp);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtPower);
			strTmp = String.format("%.2f", (float)oSensorData3.sCurrent / 1000 * oSensorData3.sVoltage / 1000) + "W" +
					 " ( " + oSensorData3.sCurrent + " mA, " + oSensorData3.sVoltage + " mV )";
			oElement.setText(strTmp);
	
			oElement = (TextView) m_oActivity.findViewById(R.id.txtTemperature);
			oElement.setText(oSensorData3.byTemperature + " C");
			
		}

		private void setBoolElement(TextView io_oElement, boolean i_bBool) {
			io_oElement.setText(Boolean.toString(i_bBool));
			if (i_bBool) {
				io_oElement.setBackgroundColor(Color.RED);
				io_oElement.setTextColor(Color.LTGRAY);
			} else {
				io_oElement.setBackgroundColor(Color.GREEN);
				io_oElement.setTextColor(Color.BLACK);
			}
		}
		
	}
	
	public void initialize() {
		setSensor(ERoombaSensorPackages.sensPkg_None);
	}
	

}
