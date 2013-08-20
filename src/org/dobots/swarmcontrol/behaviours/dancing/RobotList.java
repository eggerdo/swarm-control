package org.dobots.swarmcontrol.behaviours.dancing;

import java.util.ArrayList;
import java.util.List;

import org.dobots.robots.RobotDeviceFactory;
import org.dobots.swarmcontrol.ConnectionHelper;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.SwarmControlActivity;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.IActivityResultListener;
import org.dobots.utilities.Utils;

import robots.RobotType;
import robots.ctrl.IRobotDevice;
import robots.gui.IConnectListener;
import robots.gui.RobotInventory;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RobotList extends BaseActivity {

	private BaseActivity m_oActivity;

	private ArrayList<IActivityResultListener> m_oActivityResultListener;
	
	private boolean m_bShowRemoveRobot = false;
	
	private int m_nRobotItemHeight = 88;
	
	private ListView m_lvAddedRobots;

	private ArrayList<RobotEntry> m_oRobotList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_oActivity = this;
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		m_oActivityResultListener = new ArrayList<IActivityResultListener>();
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
		
		IRobotDevice oRobot;
		try {
			oRobot = RobotDeviceFactory.getRobotDevice(i_eRobot);
			String strRobotID = RobotInventory.getInstance().addRobot(oRobot);
			
			RobotEntry entry = new RobotEntry(oRobot, i_eRobot, strRobotID);
			m_oRobotList.add(entry);
			
			m_lvAddedRobots.invalidateViews();
		} catch (Exception e) {
			Toast.makeText(m_oActivity, "Robot not available", Toast.LENGTH_LONG);
		}
		
	}

	public class RobotEntry {
		
		IRobotDevice oRobot;
		RobotType eType;
		String strRobotID;
		boolean bRemove;
		
		public RobotEntry(IRobotDevice i_oRobot, RobotType eType, String id) {
			this.oRobot = i_oRobot;
			this.eType = eType;
			this.strRobotID = id;
			this.bRemove = false;
		}

		public void destroy() {
			oRobot.destroy();
			RobotInventory.getInstance().removeRobot(oRobot.getID());
			oRobot = null;
		}
		
	}

	private class RobotListAdapter extends ArrayAdapter<RobotEntry> {
		
		private final BaseActivity context;
		private final List<RobotEntry> list;
		
		public RobotListAdapter(BaseActivity context, List<RobotEntry> list) {
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
						
						IConnectListener oListener = new IConnectListener() {
							@Override
							public void onConnect(boolean i_bConnected) {
								viewHolder.lblRobotStatus.setText(i_bConnected ? "Connected" : "Disconnected");
								viewHolder.btnGoto.setEnabled(i_bConnected);
								if (i_bConnected) {
									viewHolder.lblRobotAddr.setText(oEntry.oRobot.getAddress());
								}
							}
						};
						
						ConnectionHelper.establishConnection(context, oEntry.oRobot, oListener);
					}
				});
				
				viewHolder.btnGoto = (Button) view.findViewById(R.id.btnDancing_RobotGoto);
				viewHolder.btnGoto.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						RobotEntry oEntry = (RobotEntry) viewHolder.btnGoto.getTag();
						((SwarmControlActivity)SwarmControlActivity.getContext()).showRobot(oEntry.eType, oEntry.strRobotID);
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
		
		RobotEntry entry = m_oRobotList.get(info.position);
		m_oRobotList.remove(entry);
		entry.destroy();
		m_lvAddedRobots.invalidateViews();
		return true;
	}
	
}
