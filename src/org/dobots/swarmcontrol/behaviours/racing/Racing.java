package org.dobots.swarmcontrol.behaviours.racing;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import org.dobots.robots.RobotDevice;
import org.dobots.robots.RobotDeviceFactory;
import org.dobots.swarmcontrol.BluetoothConnectionHelper;
import org.dobots.swarmcontrol.BluetoothConnectionListener;
import org.dobots.swarmcontrol.ConnectListener;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.RobotInventory;
import org.dobots.swarmcontrol.behaviours.ActivityResultListener;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.swarmcontrol.robots.RobotViewFactory;
import org.dobots.utility.Utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class Racing extends Activity {

	private static final int SETUP_ID = Menu.FIRST;
	
	enum RaceRobot {
		RR_NONE("", RobotType.RBT_UNKNOWN),
		RR_ROOMBA_1("Roomba 1", RobotType.RBT_ROOMBA),
		RR_ROOMBA_2("Roomba 2", RobotType.RBT_ROOMBA),
		RR_NXT_1("NXT 1", RobotType.RBT_NXT),
		RR_NXT_2("NXT 2", RobotType.RBT_NXT);
		private String strName;
		private RobotType eType;
		
		private RaceRobot(String i_strName, RobotType i_eType) {
			strName = i_strName;
			eType = i_eType;
		}
		
		public String toString() {
			return strName;
		}
		
		public RobotType getType() {
			return eType;
		}
		
	}
	
	private Activity m_oActivity;
	private Spinner m_spRobotChoice;
	private RobotDevice m_oRobot;
	private int m_nInventoryIndex;
	
	private EnumMap<RaceRobot, BluetoothDevice> m_oAddress = null;

	private ArrayList<ActivityResultListener> m_oActivityResultListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		m_oActivity = this;
		
		m_oActivityResultListener = new ArrayList<ActivityResultListener>();
		
		setProperties();
	
//		testSetup();
		setup();
	}
	
	private void testSetup() {
		m_oAddress = new EnumMap<RaceRobot, BluetoothDevice>(RaceRobot.class);
		m_oAddress.put(RaceRobot.RR_ROOMBA_1, BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:06:66:07:B0:35"));
		m_oAddress.put(RaceRobot.RR_ROOMBA_2, BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:06:66:07:AE:BF"));
		m_oAddress.put(RaceRobot.RR_NXT_1, BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:16:53:01:5D:21"));
		m_oAddress.put(RaceRobot.RR_NXT_2, BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:16:53:04:1D:8E"));
	}
	
	private void setProperties() {
		
		setContentView(R.layout.racing_main);

		final ArrayAdapter<RaceRobot> oRacingRobotAdapter = new ArrayAdapter<RaceRobot>(m_oActivity, 
				android.R.layout.simple_spinner_item, RaceRobot.values());
        oRacingRobotAdapter.setDropDownViewResource(android.R.layout.select_dialog_item);
        
		m_spRobotChoice = (Spinner) m_oActivity.findViewById(R.id.spRobotChoice);
    	// add sensor id as tag to the spinner so we can access it later on
		m_spRobotChoice.setAdapter(oRacingRobotAdapter);
		m_spRobotChoice.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				RaceRobot eRobot = oRacingRobotAdapter.getItem(position);
				
				if (eRobot == RaceRobot.RR_NONE) {
					return;
				}

				if (m_oAddress == null) {
					Utils.showToast("Setup required!", Toast.LENGTH_LONG);
					m_spRobotChoice.setSelection(0);
					return;
				}
				
				try {
					m_oRobot = RobotDeviceFactory.getRobotDevice(eRobot.getType());
					m_nInventoryIndex = RobotInventory.getInstance().addRobot(m_oRobot);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				ConnectListener oListener = new ConnectListener() {
					@Override
					public void onConnect(boolean i_bConnected) {
						if (i_bConnected) {
							showRobotRaceView(m_nInventoryIndex);
						} else {
							reset();
						}
					}
				};
				
				BluetoothDevice oDevice = m_oAddress.get(eRobot);
				try {
					RobotDeviceFactory.connectToRobot(m_oActivity, m_oRobot, oDevice, oListener);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
			
		});
		
	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu.findItem(SETUP_ID) != null) {
			menu.removeItem(SETUP_ID);
		}
    	if (m_oAddress == null) {
    		menu.add(0, SETUP_ID, 1, "Setup");
    	}
    	return true;
    }
    

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case SETUP_ID:
			setup();
		}
		return true;
	}

	private void setup() {
		
		m_oAddress = new EnumMap<RaceRobot, BluetoothDevice>(RaceRobot.class);
		
		Set<RaceRobot> racingRobotSet = EnumSet.allOf(RaceRobot.class);
		Iterator<RaceRobot> rrIterator = racingRobotSet.iterator();
		
		if (rrIterator.hasNext()) {
			RaceRobot eRobot = rrIterator.next();
			if (eRobot == RaceRobot.RR_NONE) {
				eRobot = rrIterator.next();
			}
			
			setupRobot(eRobot, rrIterator);
		}
		
	}
	
	private void setupRobot(final RaceRobot eRobot, final Iterator<RaceRobot> iter) {
		
		final BluetoothConnectionHelper oBTHelper = new BluetoothConnectionHelper(m_oActivity, RobotViewFactory.getRobotAddressFilter(eRobot.getType()));
		oBTHelper.SetOnConnectListener(new BluetoothConnectionListener() {
			
			@Override
			public void connectToRobot(BluetoothDevice i_oDevice) {
				m_oAddress.put(eRobot, i_oDevice);
				m_oActivityResultListener.remove(oBTHelper);
				if (iter.hasNext()) {
					RaceRobot nextRobot = iter.next();
					setupRobot(nextRobot, iter);
				} else {
					Utils.showToast("Setup Done", Toast.LENGTH_LONG);
				}
			}
		});
		m_oActivityResultListener.add(oBTHelper);
		
		oBTHelper.setTitle(eRobot.toString());
		if (oBTHelper.initBluetooth()) {
			oBTHelper.selectRobot();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		for (ActivityResultListener listener : m_oActivityResultListener) {
			listener.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	private void showRobotRaceView(int i_nIndex) {
		Intent intent = new Intent(this, RacingRobot.class);
		intent.putExtra("InventoryIndex", i_nIndex);
		startActivity(intent);
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		
		reset();
	}
	
	private void reset() {
		m_spRobotChoice.setSelection(0);
	}
	
}
