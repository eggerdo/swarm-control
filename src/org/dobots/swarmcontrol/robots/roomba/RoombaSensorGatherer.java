package org.dobots.swarmcontrol.robots.roomba;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.dobots.robots.roomba.Roomba;
import org.dobots.robots.roomba.RoombaTypes.ERoombaSensorPackages;
import org.dobots.robots.roomba.RoombaTypes.SensorPackage;
import org.dobots.robots.roomba.RoombaTypes.SensorPackage1;
import org.dobots.robots.roomba.RoombaTypes.SensorPackage2;
import org.dobots.robots.roomba.RoombaTypes.SensorPackage3;
import org.dobots.robots.roomba.RoombaTypes.SensorPackageAll;
import org.dobots.robots.roomba.RoombaTypes.SensorType;
import org.dobots.swarmcontrol.BaseActivity;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.SensorGatherer;
import org.dobots.utility.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

public class RoombaSensorGatherer extends SensorGatherer {

	private ERoombaSensorPackages m_eSensor;
	private Roomba m_oRoomba;

	private SensorPackage m_oSensorData;
	
	private TableLayout m_tblSensorPackage1;
	private TableLayout m_tblSensorPackage2;
	private TableLayout m_tblSensorPackage3;
	
	private LinearLayout m_laySensorDataAll;
	private ListView m_lvSensorDataAll;

	private ArrayList<SensorEntry> m_oSensorList;
	private EnumMap<SensorType, SensorEntry> m_oSensorSelected;
	private ArrayAdapter<SensorType> oSensorTypeAdapter;
	
	private Button m_btnSensorItem;

	private boolean m_bShowRemoveElement = false;
	
	public RoombaSensorGatherer(BaseActivity i_oActivity, Roomba i_oRoomba) {
		super(i_oActivity);
		m_oRoomba = i_oRoomba;
		
		m_oGUIUpdater = new UpdateSensorDataTask();
		
		m_oSensorList = new ArrayList<SensorEntry>();
		m_oSensorSelected = new EnumMap<SensorType, SensorEntry>(SensorType.class);
		
		setProperties();

		start();
	}
	
	private void setProperties() {
		m_tblSensorPackage1 = (TableLayout) m_oActivity.findViewById(R.id.tblSensorData1);
		m_tblSensorPackage2 = (TableLayout) m_oActivity.findViewById(R.id.tblSensorData2);
		m_tblSensorPackage3 = (TableLayout) m_oActivity.findViewById(R.id.tblSensorData3);
		
		m_laySensorDataAll = (LinearLayout) m_oActivity.findViewById(R.id.laySensorDataAll);
		m_lvSensorDataAll = (ListView) m_oActivity.findViewById(R.id.lvSensorDataAll);
		ArrayAdapter<SensorEntry> oSensorEntryAdapter = new SensorListAdapter(m_oActivity, m_oSensorList);
//		adapter.setNotifyOnChange(true);
		m_lvSensorDataAll.setAdapter(oSensorEntryAdapter);
		m_oActivity.registerForContextMenu(m_lvSensorDataAll);
//		m_lvSensorPackageAll.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		  
        m_btnSensorItem = (Button) m_oActivity.findViewById(R.id.btnSensorItem);
        m_btnSensorItem.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showSensorItemSelectionDialog();
			}
		});
		
		oSensorTypeAdapter = new ArrayAdapter<SensorType>(m_oActivity, 
				android.R.layout.select_dialog_item, SensorType.values());
        oSensorTypeAdapter.setDropDownViewResource(android.R.layout.select_dialog_item);
        
	}

	private void showSensorItemSelectionDialog() {
		AlertDialog dialog = Utils.CreateAdapterDialog(m_oActivity, "Choose a sensor", oSensorTypeAdapter,
				new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SensorType eSensor = oSensorTypeAdapter.getItem(which);
				if (eSensor == SensorType.ALL) {
					addAllSensors();
				} else {
					addSensor(eSensor, true);
				}
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	public void addSensor(SensorType i_eSensor, boolean i_bUpdate) {
		
		if (m_oSensorSelected.get(i_eSensor) == null) {
			SensorEntry entry = new SensorEntry(i_eSensor);
			m_oSensorList.add(entry);
			m_oSensorSelected.put(i_eSensor, entry);

			if (i_bUpdate) {
				updateListView();
			}
		}
	}
	
	public void addAllSensors() {
		for (SensorType eSensor : SensorType.values()) {
			if (eSensor != SensorType.ALL) {
				addSensor(eSensor, false);
			}
		}
		// all is not a sensor, remove it again. all is only
		// used to be able to display all sensors at once
		
		updateListView();
	}

	@Override
	public void execute() {
		if (m_bEnabled && m_oRoomba.isPowerOn() && m_oRoomba.isConnected()) {
			m_oSensorData = m_oRoomba.getSensors(m_eSensor);
			m_oUiHandler.postDelayed(m_oGUIUpdater, 10);
		} else {
			m_oSensorData = null;
		}
	}

	public void setSensorPackage(ERoombaSensorPackages i_eSensor) {
		m_eSensor = i_eSensor;
	}

	public void showSensorPackage(ERoombaSensorPackages eSensorPkg) {

		m_tblSensorPackage1.setLayoutParams(new TableLayout.LayoutParams(0, 0));
		m_tblSensorPackage2.setLayoutParams(new TableLayout.LayoutParams(0, 0));
		m_tblSensorPackage3.setLayoutParams(new TableLayout.LayoutParams(0, 0));
		
		Utils.showLayout(m_laySensorDataAll, false);
		
		if (eSensorPkg == ERoombaSensorPackages.sensPkg_None) {
			m_oUiHandler.removeCallbacks(m_oGUIUpdater);
			m_bEnabled = false;
		} else if (eSensorPkg != m_eSensor) {
			switch (eSensorPkg) {
			case sensPkg_1:
				m_tblSensorPackage1.setLayoutParams(new TableLayout.LayoutParams());
				break;
			case sensPkg_2:
				m_tblSensorPackage2.setLayoutParams(new TableLayout.LayoutParams());
				break;
			case sensPkg_3:
				m_tblSensorPackage3.setLayoutParams(new TableLayout.LayoutParams());
				break;
			case sensPkg_All:
				clearSensorSelection();
				updateListView();
				Utils.showLayout(m_laySensorDataAll, true);
				break;
			}
	
			setSensorPackage(eSensorPkg);
			m_oUiHandler.postDelayed(m_oGUIUpdater, 100);
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
				case sensPkg_All:
					if (SensorPackageAll.class.isInstance(m_oSensorData))
						updateSensorDataAll();
				}
//			    oHandler.postDelayed(this, 100);
			    
			} else {
				resetSensorData1();
				resetSensorData2();
				resetSensorData3();
				resetSensorDataAll();
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
		
		private void updateSensorDataAll() {
			
			SensorPackageAll oData = (SensorPackageAll) m_oSensorData;
			for (SensorType eType : SensorType.values()) {
				SensorEntry oEntry = m_oSensorSelected.get(eType);
				if (oEntry != null) {
					switch (oEntry.eSensor) {
					case WHEELDROP_CASTER:
						oEntry.strValue = String.valueOf(oData.bumps_wheeldrops.bCaster_Wheeldrop);
						break;
					case WHEELDROP_LEFT:
						oEntry.strValue = String.valueOf(oData.bumps_wheeldrops.bLeft_Wheeldrop);
						break;
					case WHEELDROP_RIGHT:
						oEntry.strValue = String.valueOf(oData.bumps_wheeldrops.bRight_Wheeldrop);
						break;
					case BUMP_LEFT:
						oEntry.strValue = String.valueOf(oData.bumps_wheeldrops.bLeft_Bump);
						break;
					case BUMP_RIGHT:
						oEntry.strValue = String.valueOf(oData.bumps_wheeldrops.bRight_Bump);
						break;
					case WALL:
						oEntry.strValue = String.valueOf(oData.wall);
						break;
					case CLIFF_LEFT:
						oEntry.strValue = String.valueOf(oData.cliff_left);
						break;
					case CLIFF_FRONT_LEFT:
						oEntry.strValue = String.valueOf(oData.cliff_front_left);
						break;
					case CLIFF_FRONT_RIGHT:
						oEntry.strValue = String.valueOf(oData.cliff_front_right);
						break;
					case CLIFF_RIGHT:
						oEntry.strValue = String.valueOf(oData.cliff_right);
						break;
					case VIRTUAL_WALL:
						oEntry.strValue = String.valueOf(oData.virtual_wall);
						break;
					case OVERCURRENT_LEFT:
						oEntry.strValue = String.valueOf(oData.motor_overcurrents.bDriveLeft);
						break;
					case OVERCURRENT_RIGHT:
						oEntry.strValue = String.valueOf(oData.motor_overcurrents.bDriveRight);
						break;
					case OVERCURRENT_MAIN_BRUSH:
						oEntry.strValue = String.valueOf(oData.motor_overcurrents.bMainBrush);
						break;
					case OVERCURRENT_VACUUM:
						oEntry.strValue = String.valueOf(oData.motor_overcurrents.bVacuum);
						break;
					case OVERCURRENT_SIDEBRUSH:
						oEntry.strValue = String.valueOf(oData.motor_overcurrents.bSideBrush);
						break;
					case DIRT_DETECTOR_LEFT:
						oEntry.strValue = String.valueOf(oData.dirt_detector_left);
						break;
					case DIRT_DETECTOR_RIGHT:
						oEntry.strValue = String.valueOf(oData.dirt_detector_right);
						break;
					case IR_OPCODE_OMNI:
						oEntry.strValue = oData.remote_opcode.toString();
						break;
					case PRESSED_POWER:
						oEntry.strValue = String.valueOf(oData.buttons.bPower);
						break;
					case PRESSED_SPOT:
						oEntry.strValue = String.valueOf(oData.buttons.bSpot);
						break;
					case PRESSED_CLEAN:
						oEntry.strValue = String.valueOf(oData.buttons.bClean);
						break;
					case PRESSED_MAX:
						oEntry.strValue = String.valueOf(oData.buttons.bMax);
						break;
					case DISTANCE:
						oEntry.strValue = String.valueOf(oData.distance);
						break;
					case ANGLE:
						oEntry.strValue = String.valueOf(oData.angle);
						break;
					case CHARGING_STATE:
						oEntry.strValue = oData.charging_state.toString();
						break;
					case VOLTAGE:
						oEntry.strValue = String.valueOf(oData.voltage);
						break;
					case CURRENT:
						oEntry.strValue = String.valueOf(oData.current);
						break;
					case BATTERY_TEMPRERATURE:
						oEntry.strValue = String.valueOf(oData.temprerature);
						break;
					case CHARGE:
						oEntry.strValue = String.valueOf(oData.charge);
						break;
					case CAPACITY:
						oEntry.strValue = String.valueOf(oData.capacity);
						break;
					case WALL_SIGNAL:
						oEntry.strValue = String.valueOf(oData.wall_signal);
						break;
					case CLIFF_LEFT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.cliff_left_signal);
						break;
					case CLIFF_FRONT_LEFT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.cliff_front_left_signal);
						break;
					case CLIFF_FRONT_RIGHT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.cliff_front_right_signal);
						break;
					case CLIFF_RIGHT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.cliff_right_signal);
						break;
					case USER_DIGITAL_INPUTS:
						oEntry.strValue = String.valueOf(oData.user_digital_inputs);
						break;
					case USER_ANALOG_INPUT:
						oEntry.strValue = String.valueOf(oData.user_analog_input);
						break;
					case CHARGING_SOURCES_AVAILABLE:
						oEntry.strValue = oData.charging_sources_available.toString();
						break;
					case OI_MODE:
						oEntry.strValue = oData.oi_mode.toString();
						break;
					case SONG_NUMBER:
						oEntry.strValue = String.valueOf(oData.song_number);
						break;
					case SONG_PLAYING:
						oEntry.strValue = String.valueOf(oData.song_playing);
						break;
					case NUMBER_OF_STREAM_PACKETS:
						oEntry.strValue = String.valueOf(oData.number_of_stream_packets);
						break;
					case REQUESTED_VELOCITY:
						oEntry.strValue = String.valueOf(oData.requested_velocity);
						break;
					case REQUESTED_RADIUS:
						oEntry.strValue = String.valueOf(oData.requested_radius);
						break;
					case REQUESTED_RIGHT_VELOCITY:
						oEntry.strValue = String.valueOf(oData.requested_right_velocity);
						break;
					case REQUESTED_LEFT_VELOCITY:
						oEntry.strValue = String.valueOf(oData.requested_left_velocity);
						break;
					case ENCODER_COUNTS_LEFT:
						oEntry.strValue = String.valueOf(oData.encoder_counts_left);
						break;
					case ENCODER_COUNTS_RIGHT:
						oEntry.strValue = String.valueOf(oData.encoder_counts_right);
						break;
					case LIGHT_BUMPER_CENTER_LEFT:
						oEntry.strValue = String.valueOf(oData.light_bumper.bLtBumperCenterLeft);
						break;
					case LIGHT_BUMPER_CENTER_RIGHT:
						oEntry.strValue = String.valueOf(oData.light_bumper.bLtBumperCenterRight);
						break;
					case LIGHT_BUMPER_FRONT_LEFT:
						oEntry.strValue = String.valueOf(oData.light_bumper.bLtBumperFrontLeft);
						break;
					case LIGHT_BUMPER_FRONT_RIGHT:
						oEntry.strValue = String.valueOf(oData.light_bumper.bLtBumperFrontRight);
						break;
					case LIGHT_BUMPER_LEFT:
						oEntry.strValue = String.valueOf(oData.light_bumper.bLtBumperLeft);
						break;
					case LIGHT_BUMPER_RIGHT:
						oEntry.strValue = String.valueOf(oData.light_bumper.bLtBumperRight);
						break;
					case LIGHT_BUMP_LEFT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.light_bump_left_signal);
						break;
					case LIGHT_BUMP_FRONT_LEFT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.light_bump_front_left_signal);
						break;
					case LIGHT_BUMP_CENTER_LEFT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.light_bump_center_left_signal);
						break;
					case LIGHT_BUMP_CENTER_RIGHT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.light_bump_center_right_signal);
						break;
					case LIGHT_BUMP_FRONT_RIGHT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.light_bump_front_right_signal);
						break;
					case LIGHT_BUMP_RIGHT_SIGNAL:
						oEntry.strValue = String.valueOf(oData.light_bump_right_signal);
						break;
					case IR_OPCODE_LEFT:
						oEntry.strValue = oData.ir_opcode_left.toString();
						break;
					case IR_OPCODE_RIGHT:
						oEntry.strValue = oData.ir_opcode_right.toString();
						break;
					case LEFT_MOTOR_CURRENT:
						oEntry.strValue = String.valueOf(oData.left_motor_current);
						break;
					case RIGHT_MOTOR_CURRENT:
						oEntry.strValue = String.valueOf(oData.right_motor_current);
						break;
					case MAIN_BRUSH_CURRENT:
						oEntry.strValue = String.valueOf(oData.main_brush_current);
						break;
					case SIDE_BRUSH_CURRENT:
						oEntry.strValue = String.valueOf(oData.side_brush_current);
						break;
					case STASIS:
						oEntry.strValue = String.valueOf(oData.stasis);
						break;
					}
				}
			}

			m_lvSensorDataAll.invalidateViews();
		}
		
		private void resetSensorDataAll() {

			SensorPackageAll oData = (SensorPackageAll) m_oSensorData;
			for (SensorType eType : SensorType.values()) {
				SensorEntry oEntry = m_oSensorSelected.get(eType);
				if (oEntry != null) {
					oEntry.strValue = "---";
				}
			}

			m_lvSensorDataAll.invalidateViews();
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
		setSensorPackage(ERoombaSensorPackages.sensPkg_None);
		showSensorPackage(ERoombaSensorPackages.sensPkg_None);
		m_bEnabled = false;
		clearSensorSelection();
	}

	public void clearSensorSelection() {
		m_oSensorList.clear();
		m_oSensorSelected.clear();
	}
	

	public class SensorEntry {
		
		SensorType eSensor;
		String strValue;
		boolean bRemove;
		
		public SensorEntry(SensorType i_eSensor) {
			this.eSensor = i_eSensor;
			this.bRemove = false;
		}
		
	}

	private class SensorListAdapter extends ArrayAdapter<SensorEntry> {
		
		private final Activity context;
		private final List<SensorEntry> list;
		
		public SensorListAdapter(Activity context, List<SensorEntry> list) {
			super(context, R.layout.sensor_entry, list);
			this.context = context;
			this.list = list;
		}
		
		private class ViewHolder {
			protected TextView lblSensorName;
			protected TextView txtSensorValue;
			protected CheckBox cbRemove;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			SensorEntry oEntry = list.get(position);
			
			if (convertView == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				view = inflater.inflate(R.layout.sensor_entry, null);
				final ViewHolder viewHolder = new ViewHolder();
				viewHolder.lblSensorName = (TextView) view.findViewById(R.id.lblSensorName);
				viewHolder.txtSensorValue = (TextView) view.findViewById(R.id.txtSensorValue);
				
				viewHolder.cbRemove = (CheckBox) view.findViewById(R.id.cbRemoveSensor);
				viewHolder.cbRemove.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						SensorEntry oEntry = (SensorEntry) viewHolder.cbRemove.getTag();
						oEntry.bRemove = true;
					}
				});
				
				view.setTag(viewHolder);
				viewHolder.cbRemove.setTag(oEntry);
			} else {
				view = convertView;
				((ViewHolder) view.getTag()).cbRemove.setTag(oEntry);
			}
			
			ViewHolder holder = (ViewHolder) view.getTag();
			
			// the buttons and all other items which can get a focus need to be set
			// to focusable=false so that the onClick event of the listViewItem gets
			// fired!!
			holder.cbRemove.setFocusable(false);
			
			holder.lblSensorName.setText(oEntry.eSensor.toString());
			holder.txtSensorValue.setText(oEntry.strValue);
			
			if (m_bShowRemoveElement) {
				Utils.showView(holder.cbRemove, true);
			} else {
				holder.cbRemove.setChecked(false);
				Utils.showView(holder.cbRemove, false);
			}
			
			return view;
		}
		
	}
	
	private void removeEntries() {
		for (int i = m_oSensorList.size()-1; i >= 0 ; i--) {
			SensorEntry entry = m_oSensorList.get(i);
			if (entry.bRemove) {
				m_oSensorList.remove(entry);
				m_oSensorSelected.put(entry.eSensor, null);
			}
		}
		updateListView();
	}
	
	private void updateListView() {
		m_lvSensorDataAll.invalidateViews();
		Utils.setListViewHeightBasedOnChildren(m_lvSensorDataAll);
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.lvSensorDataAll) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			SensorEntry oEntry = m_oSensorList.get(info.position);
			menu.setHeaderTitle(String.format("%s", oEntry.eSensor.toString()));
			menu.add("Remove");
		}
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		
		SensorEntry oEntry = m_oSensorList.get(info.position);
		m_oSensorList.remove(oEntry);
		m_oSensorSelected.put(oEntry.eSensor, null);
		updateListView();
		return true;
	}

	public void updateButtons(boolean enabled) {
		m_btnSensorItem.setEnabled(enabled);
	}

}
