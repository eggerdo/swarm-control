package org.dobots.swarmcontrol.behaviours.dancing;

import java.util.ArrayList;
import java.util.List;

import org.dobots.robots.RobotDevice;
import org.dobots.robots.RobotDeviceFactory;
import org.dobots.robots.nxt.NXT;
import org.dobots.robots.roomba.Roomba;
import org.dobots.swarmcontrol.BluetoothConnectionHelper;
import org.dobots.swarmcontrol.BluetoothConnectionListener;
import org.dobots.swarmcontrol.ConnectListener;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.RobotInventory;
import org.dobots.swarmcontrol.SwarmControlActivity;
import org.dobots.swarmcontrol.behaviours.ActivityResultListener;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.swarmcontrol.robots.RobotViewFactory;
import org.dobots.swarmcontrol.robots.nxt.NXTRobot;
import org.dobots.swarmcontrol.robots.roomba.RoombaRobot;
import org.dobots.utility.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class RobotList extends Activity {

	private Activity m_oActivity;

	private ArrayList<ActivityResultListener> m_oActivityResultListener;
	
	private boolean m_bShowRemoveRobot = false;
	
	private int m_nRobotItemHeight = 88;
	
	private ListView m_lvAddedRobots;

	private boolean m_bKeepAlive = false;

	private ArrayList<RobotEntry> m_oRobotList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.m_oActivity = this;
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		m_oActivityResultListener = new ArrayList<ActivityResultListener>();
		m_oRobotList = DancingMain.getInstance().getRobotList();
		
		setProperties();
	}
	
	public void setProperties() {
		setContentView(R.layout.dancing_robotlist);
		

		Button btnAddRobot = (Button) m_oActivity.findViewById(R.id.btnDancing_Add);
		btnAddRobot.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(m_oActivity);
				builder.setTitle("Choose a robot");
				final ArrayAdapter<RobotType> adapter = new ArrayAdapter<RobotType>(m_oActivity, android.R.layout.select_dialog_item,
						RobotType.values());
				builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						RobotType eRobot = adapter.getItem(which);
						dialog.dismiss();
						addRobot(eRobot);
					}
				});
				builder.create().show();
				
			}
		});

		m_lvAddedRobots = (ListView) m_oActivity.findViewById(R.id.lvDancing_RobotList);
		ArrayAdapter<RobotEntry> adapter = new RobotListAdapter(m_oActivity, m_oRobotList);
		m_lvAddedRobots.setAdapter(adapter);
		registerForContextMenu(m_lvAddedRobots);
		
		Button btnRemoveRobots = (Button) m_oActivity.findViewById(R.id.btnDancing_Remove);
		btnRemoveRobots.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!m_bShowRemoveRobot) {
					if (m_lvAddedRobots.getChildCount() > 0) {
						showRemoveRobots();
						((Button)v).setText("Done");
					}
				} else {
					removeRobots();
					((Button)v).setText("Remove Robots...");
				}
			}
		});

	}

	private void showRemoveRobots() {
		m_bShowRemoveRobot = true;
		m_lvAddedRobots.invalidateViews();
	}
	
	private void removeRobots() {
		m_bShowRemoveRobot = false;
		
		for (int i = m_oRobotList.size()-1; i >= 0 ; i--) {
			RobotEntry entry = m_oRobotList.get(i);
			if (entry.bRemove) {
				m_oRobotList.remove(entry);
				entry.destroy();
			}
		}
		m_lvAddedRobots.invalidateViews();
	}

	private void addRobot(RobotType i_eRobot) {
		
		switch(i_eRobot) {
		case RBT_FINCH:
		case RBT_SPYKEE:
		case RBT_SURVEYOR:
		case RBT_TRAKR:
			break;
		default:
			RobotDevice oRobot = RobotDeviceFactory.getRobotDevice(i_eRobot);
			int nIndex = RobotInventory.getInstance().addRobot(oRobot);
			
			RobotEntry entry = new RobotEntry(oRobot, i_eRobot, nIndex);
			m_oRobotList.add(entry);
			
			m_lvAddedRobots.invalidateViews();
		}
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		for (ActivityResultListener listener : m_oActivityResultListener) {
			listener.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	public class RobotEntry {
		
		RobotDevice oRobot;
		RobotType eType;
		int nInventoryIdx;
		boolean bRemove;
		
		public RobotEntry(RobotDevice i_oRobot, RobotType eType, int index) {
			this.oRobot = i_oRobot;
			this.eType = eType;
			this.nInventoryIdx = index;
			this.bRemove = false;
		}

		public void destroy() {
			oRobot.destroy();
			RobotInventory.getInstance().removeRobot(oRobot);
			oRobot = null;
		}
		
	}

	private class RobotListAdapter extends ArrayAdapter<RobotEntry> {
		
		private final Activity context;
		private final List<RobotEntry> list;
		
		public RobotListAdapter(Activity context, List<RobotEntry> list) {
			super(context, R.layout.dancing_robot, list);
			this.context = context;
			this.list = list;
		}
		
		private class ViewHolder {
			protected TextView lblRobotName;
			protected TextView lblRobotAddr;
			protected TextView lblRobotStatus;
			protected Button btnConnect;
			protected Button btnGoto;
			protected CheckBox cbRemove;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			RobotEntry oEntry = list.get(position);
			
			if (convertView == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				view = inflater.inflate(R.layout.dancing_robot, null);
				final ViewHolder viewHolder = new ViewHolder();
				viewHolder.lblRobotName = (TextView) view.findViewById(R.id.lblDancing_RobotName);
				viewHolder.lblRobotAddr = (TextView) view.findViewById(R.id.lblDancing_RobotMacAddr);
				viewHolder.lblRobotStatus = (TextView) view.findViewById(R.id.lblDancing_RobotStatus);
				
				viewHolder.btnConnect = (Button) view.findViewById(R.id.btnDancing_RobotConnect);
				viewHolder.btnConnect.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						final RobotEntry oEntry = (RobotEntry) viewHolder.btnConnect.getTag();
						final BluetoothConnectionHelper oBTHelper = new BluetoothConnectionHelper(context, RobotViewFactory.getRobotMacFilter(oEntry.eType));
						oBTHelper.SetOnConnectListener(new BluetoothConnectionListener() {
							
							@Override
							public void connectToRobot(BluetoothDevice i_oDevice) {
								
								ConnectListener oListener = new ConnectListener() {
									@Override
									public void onConnect(boolean i_bConnected) {
										viewHolder.lblRobotStatus.setText(i_bConnected ? "Connected" : "Disconnected");
										viewHolder.btnGoto.setEnabled(i_bConnected);
										if (i_bConnected) {
											viewHolder.lblRobotAddr.setText(oEntry.oRobot.getAddress());
										}
									}
								};
								
								if (oEntry.eType == RobotType.RBT_NXT) {
									NXTRobot.connectToNXT(context, (NXT)oEntry.oRobot, i_oDevice, oListener);
								} else {
									RoombaRobot.connectToRoomba(context, (Roomba)oEntry.oRobot, i_oDevice, oListener);
								}
								m_oActivityResultListener.remove(oBTHelper);
							}
						});
						m_oActivityResultListener.add(oBTHelper);
						
						if (oBTHelper.initBluetooth()) {
							oBTHelper.selectRobot();
						}
					}
				});
				
				viewHolder.btnGoto = (Button) view.findViewById(R.id.btnDancing_RobotGoto);
				viewHolder.btnGoto.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						m_bKeepAlive = true;
						RobotEntry oEntry = (RobotEntry) viewHolder.btnGoto.getTag();
						((SwarmControlActivity)SwarmControlActivity.getContext()).showRobot(oEntry.eType, oEntry.nInventoryIdx);
					}
				});
				
				viewHolder.cbRemove = (CheckBox) view.findViewById(R.id.cbDancing_Remove);
				viewHolder.cbRemove.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						RobotEntry oEntry = (RobotEntry) viewHolder.cbRemove.getTag();
						oEntry.bRemove = true;
					}
				});

				view.setTag(viewHolder);
				viewHolder.btnConnect.setTag(oEntry);
				viewHolder.btnGoto.setTag(oEntry);
				viewHolder.cbRemove.setTag(oEntry);
			} else {
				view = convertView;
				((ViewHolder) view.getTag()).btnConnect.setTag(oEntry);
				((ViewHolder) view.getTag()).btnGoto.setTag(oEntry);
				((ViewHolder) view.getTag()).cbRemove.setTag(oEntry);
			}

			ViewHolder holder = (ViewHolder) view.getTag();
			
			// the buttons and all other items which can get a focus need to be set
			// to focusable=false so that the onClick event of the listViewItem gets
			// fired!!
			holder.btnConnect.setFocusable(false);
			holder.btnGoto.setFocusable(false);
			holder.cbRemove.setFocusable(false);
			
			holder.lblRobotName.setText(oEntry.eType.toString());
			holder.lblRobotAddr.setText(oEntry.oRobot.getAddress());
			holder.lblRobotStatus.setText(oEntry.oRobot.isConnected() ? "Connected" : "Disconnected");
			holder.btnGoto.setEnabled(oEntry.oRobot.isConnected());
			
			if (m_bShowRemoveRobot) {
				Utils.showView(holder.cbRemove, true);
			} else {
				holder.cbRemove.setChecked(false);
				Utils.showView(holder.cbRemove, false);
			}
			
			return view;
		}
		
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.lvDancing_RobotList) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			RobotEntry oEntry = m_oRobotList.get(info.position);
			menu.setHeaderTitle(String.format("%s: %s", oEntry.eType.toString(), oEntry.oRobot.getAddress()));
			menu.add("Remove");
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		
		m_oRobotList.remove(info.position);
		m_lvAddedRobots.invalidateViews();
		return true;
	}
	
}
