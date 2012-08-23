package org.dobots.swarmcontrol.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dobots.robots.RobotDevice;
import org.dobots.robots.RobotDeviceFactory;
import org.dobots.robots.nxt.NXT;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorID;
import org.dobots.robots.nxt.NXTTypes.ENXTSensorType;
import org.dobots.robots.roomba.Roomba;
import org.dobots.swarmcontrol.BluetoothConnectionHelper;
import org.dobots.swarmcontrol.BluetoothConnectionListener;
import org.dobots.swarmcontrol.ConnectListener;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.RemoteControlHelper;
import org.dobots.swarmcontrol.RobotInventory;
import org.dobots.swarmcontrol.SwarmControlActivity;
import org.dobots.swarmcontrol.robots.RobotView;
import org.dobots.swarmcontrol.robots.RobotViewFactory;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.swarmcontrol.robots.nxt.NXTBluetooth;
import org.dobots.swarmcontrol.robots.nxt.NXTRobot;
import org.dobots.swarmcontrol.robots.roomba.RoombaRobot;
import org.dobots.utility.Utils;
import org.dobots.swarmcontrol.RemoteControlHelper.OnButtonPress;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class Dancing extends Activity {
	
	private Activity m_oActivity;

	private ArrayList<ActivityResultListener> m_oActivityResultListener;
	
	private ArrayList<RobotEntry> m_oRobotList;
	
	private boolean m_bShowRemoveRobot = false;
	
	private int m_nRobotItemHeight = 88;
	
	private ListView m_lvAddedRobots;
	
	private RemoteControlHelper m_oRemoteCtrl;
	
	private boolean m_bKeepAlive = false;
	
	private Handler uiHandler = new Handler();
	
	private int m_nStepCounter = 1;
	
	private ArrayList<DanceEntry> m_oDanceList;

	private ListView m_lvDanceList;

	private int m_nDanceItemHeight = 45;
	
	private enum DanceMoves {
		dm_Nothing("Nothing"),
		dm_forward("Forward"),
		dm_backward("Backward"),
		dm_rotateLeft("Rotate Left"),
		dm_rotateRight("Rotate Right"),
		dm_delay("Dealy");
		String strName;
		
		private DanceMoves(String i_strName) {
			this.strName = i_strName;
		}
		
		public String toString() {
			return strName;
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
		this.m_oActivity = this;

		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
//		m_oRobotViews = new ArrayList<LinearLayout>();
		m_oActivityResultListener = new ArrayList<ActivityResultListener>();
//		m_oRobots = new ArrayList<RobotDevice>();
		
		m_oRobotList = new ArrayList<RobotEntry>();
		
		m_oRemoteCtrl = new RemoteControlHelper(m_oActivity);
		
		m_oDanceList = new ArrayList<DanceEntry>();

		setProperties();
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		for (RobotEntry entry : m_oRobotList) {
			entry.destroy();
		}
	}

	public void onStop() {
		super.onStop();
		
//		if (!m_bKeepAlive) {
//			for (RobotEntry entry : m_oRobotList) {
//				entry.oRobot.disconnect();
//			}
//		}
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
		
//		if (!m_bKeepAlive) {
//			for (RobotEntry entry : m_oRobotList) {
//				try {
//					entry.oRobot.connect();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		m_bKeepAlive = false;
	}
	
	private class RobotEntry {
		
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
			holder.lblRobotName.setText(oEntry.eType.toString());
			holder.lblRobotAddr.setText(oEntry.oRobot.getAddress());
			holder.lblRobotStatus.setText(oEntry.oRobot.isConnected() ? "Connected" : "Disconnected");
			
			if (m_bShowRemoveRobot) {
				Utils.showView(holder.cbRemove, true);
			} else {
				holder.cbRemove.setChecked(false);
				Utils.showView(holder.cbRemove, false);
			}
			
			return view;
		}
		
	}
	
	private void setProperties() {
        m_oActivity.setContentView(R.layout.dancing2);
    	
        m_oRemoteCtrl.setProperties();
        m_oRemoteCtrl.setFwdPressListener(new OnButtonPress() {
			
			@Override
			public void buttonPressed(boolean i_bDown) {
				if (i_bDown) {
					driveForward();
				} else {
					driveStop();
				}
			}
		});
        
		m_oRemoteCtrl.setBwdPressListener(new OnButtonPress() {
			
			@Override
			public void buttonPressed(boolean i_bDown) {
				if (i_bDown) {
					driveBackward();
				} else {
					driveStop();
				}
			}
		});
		
		m_oRemoteCtrl.setLeftPressListener(new OnButtonPress() {
			
			@Override
			public void buttonPressed(boolean i_bDown) {
				if (i_bDown) {
					rotateCounterClockwise();
				} else {
					driveStop();
				}
			}
		});
		
		m_oRemoteCtrl.setRightPressListener(new OnButtonPress() {
			
			@Override
			public void buttonPressed(boolean i_bDown) {
				if (i_bDown) {
					rotateClockwise();
				} else {
					driveStop();
				}
			}
		});

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
		
		Button btnRemoveRobots = (Button) m_oActivity.findViewById(R.id.btnDancing_Remove);
		btnRemoveRobots.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (m_lvAddedRobots.getChildCount() > 0) {
					showRemoveRobots();
				}
			}
		});
		
		final Button btnRemoveDone = (Button) m_oActivity.findViewById(R.id.btnDancing_RemoveDone);
		btnRemoveDone.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bShowRemoveRobot = false;
				
				int height = m_lvAddedRobots.getHeight();
				for (int i = m_oRobotList.size()-1; i >= 0 ; i--) {
					RobotEntry entry = m_oRobotList.get(i);
					if (entry.bRemove) {
						m_oRobotList.remove(entry);
						entry.destroy();
						height -= m_nRobotItemHeight;
					}
				}
				m_lvAddedRobots.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height));
				m_lvAddedRobots.invalidateViews();
				btnRemoveDone.setVisibility(View.INVISIBLE);
			}
		});
		
		final EditText edtTurnDuration = (EditText) m_oActivity.findViewById(R.id.edtTurnDuration);
		
		Button btnTurnLeft = (Button) m_oActivity.findViewById(R.id.btnDancing_TurnLeft);
		btnTurnLeft.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String strValue = edtTurnDuration.getText().toString();
				if (strValue != "") {
					int nDuration = Integer.parseInt(strValue);
					turnLeft(nDuration);
				}
			}
		});
		
		Button btnTurnRight = (Button) m_oActivity.findViewById(R.id.btnDancing_TurnRight);
		btnTurnRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String strValue = edtTurnDuration.getText().toString();
				if (strValue != "") {
					int nDuration = Integer.parseInt(strValue);
					turnRight(nDuration);
				}
			}
		});
		

		final EditText edtMoveDuration = (EditText) m_oActivity.findViewById(R.id.edtMoveDuration);
		
		Button btnMoveFwd = (Button) m_oActivity.findViewById(R.id.btnDancing_MoveFwd);
		btnMoveFwd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String strValue = edtMoveDuration.getText().toString();
				if (strValue != "") {
					int nDuration = Integer.parseInt(strValue);
					moveFwd(nDuration);
				}
			}
		});
		
		Button btnMoveBwd = (Button) m_oActivity.findViewById(R.id.btnDancing_MoveBwd);
		btnMoveBwd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String strValue = edtMoveDuration.getText().toString();
				if (strValue != "") {
					int nDuration = Integer.parseInt(strValue);
					moveBwd(nDuration);
				}
			}
		});
		
		m_lvDanceList = (ListView) m_oActivity.findViewById(R.id.lvDancing_DanceList);
		ArrayAdapter<DanceEntry> oDanceListAdapter = new DanceListAdapter(m_oActivity, m_oDanceList);
		m_lvDanceList.setAdapter(oDanceListAdapter);

		final EditText edtDancingDuration = (EditText) m_oActivity.findViewById(R.id.edtDancing_Duration);
		final Spinner spMoves = (Spinner) m_oActivity.findViewById(R.id.spDancing_Moves);
		
		ArrayAdapter<DanceMoves> movesAdapter = new ArrayAdapter<DanceMoves>(m_oActivity, 
				android.R.layout.simple_spinner_item, DanceMoves.values());
		movesAdapter.setDropDownViewResource(android.R.layout.select_dialog_item);
		spMoves.setAdapter(movesAdapter);		
//		spMoves.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//			@Override
//			public void onItemSelected(AdapterView<?> parent, View view,
//					int position, long id) {
//				DanceMoves eMove = (DanceMoves)parent.getItemAtPosition(position);
//				String strValue = edtDancingDuration.getText().toString();
//				
//				switch(eMove) {
//				case dm_Nothing:
//					break;
//				case dm_backward:
//				case dm_forward:
//				case dm_rotateLeft:
//				case dm_rotateRight:
//				case dm_delay:
//					if (strValue != "") {
//						int nValue = Integer.parseInt(strValue);
//						addMove(eMove, nValue);
//					}
//					break;
//				}
//				
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> arg0) {
//				// do nothing
//			}
//			
//		});
		
		Button btnAddMove = (Button) m_oActivity.findViewById(R.id.btnDancing_AddMove);
		btnAddMove.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DanceMoves eMove = (DanceMoves)spMoves.getSelectedItem();
				String strValue = edtDancingDuration.getText().toString();
				if (!strValue.isEmpty()) {
					addMove(eMove, Integer.parseInt(strValue));
				}
			}
		});
		
		Button btnStartDance = (Button) m_oActivity.findViewById(R.id.btnDancing_StartDance);
		btnStartDance.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startDance();
			}
		});
		
		Button btnClear = (Button) m_oActivity.findViewById(R.id.btnClear);
		btnClear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oDanceList.clear();
				m_lvDanceList.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0));
				m_lvDanceList.invalidateViews();
			}
		});

		Button btnDance1 = (Button) m_oActivity.findViewById(R.id.btnDance1);
		btnDance1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dance1();
			}
		});
		
	}
	
	private class DanceEntry {
		
		DanceMoves eMove;
		int nDuration;
		boolean bRemove;
		
		public DanceEntry(DanceMoves i_eMove, int i_nDuration) {
			this.eMove = i_eMove;
			this.nDuration = i_nDuration;
			this.bRemove = false;
		}
		
	}

	private class DanceListAdapter extends ArrayAdapter<DanceEntry> {
		
		private final Activity context;
		private final List<DanceEntry> list;
		private boolean m_bShowRemoveDance;
		
		public DanceListAdapter(Activity context, List<DanceEntry> list) {
			super(context, R.layout.dancing_move, list);
			this.context = context;
			this.list = list;
		}
		
		private class ViewHolder {
			protected TextView lblStep;
			protected TextView lblMove;
			protected TextView lblDuration;
			protected CheckBox cbRemove;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			DanceEntry oEntry = list.get(position);
			
			if (convertView == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				view = inflater.inflate(R.layout.dancing_move, null);
				final ViewHolder viewHolder = new ViewHolder();
				viewHolder.lblStep = (TextView) view.findViewById(R.id.lblDancing_Step);
				viewHolder.lblMove = (TextView) view.findViewById(R.id.lblDancing_Move);
				viewHolder.lblDuration = (TextView) view.findViewById(R.id.lblDancing_Duration);
				
//				viewHolder.cbRemove = (CheckBox) view.findViewById(R.id.cbDancing_RemoveMove);
//				viewHolder.cbRemove.setOnClickListener(new OnClickListener() {
//					
//					@Override
//					public void onClick(View v) {
//						RobotEntry oEntry = (RobotEntry) viewHolder.cbRemove.getTag();
//						oEntry.bRemove = true;
//					}
//				});

				view.setTag(viewHolder);
//				viewHolder.cbRemove.setTag(oEntry);
			} else {
				view = convertView;
//				((ViewHolder) view.getTag()).cbRemove.setTag(oEntry);
			}
					
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.lblStep.setText(Integer.toString(position + 1) + '.');
			holder.lblMove.setText(oEntry.eMove.toString());
			holder.lblDuration.setText(Integer.toString(oEntry.nDuration) + 's');
			
//			if (m_bShowRemoveDance) {
//				Utils.showView(holder.cbRemove, true);
//			} else {
//				holder.cbRemove.setChecked(false);
//				Utils.showView(holder.cbRemove, false);
//			}
			
			return view;
		}
		
	}
	private void addMove(DanceMoves i_eMove, int i_nDuration) {
		
		if (i_eMove != DanceMoves.dm_Nothing) {
			DanceEntry entry = new DanceEntry(i_eMove, i_nDuration);
			m_oDanceList.add(entry);
	
			m_lvDanceList.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, m_lvDanceList.getHeight() + m_nDanceItemHeight));
			m_lvDanceList.invalidateViews();
		}
	}

	private void showRemoveRobots() {
		m_bShowRemoveRobot = true;
		Button btnDone = (Button) m_oActivity.findViewById(R.id.btnDancing_RemoveDone);
		btnDone.setVisibility(View.VISIBLE);
		m_lvAddedRobots.invalidateViews();
	}

	private void addRobot(RobotType i_eRobot) {
		
		RobotDevice oRobot = RobotDeviceFactory.getRobotDevice(i_eRobot);
		int nIndex = RobotInventory.getInstance().addRobot(oRobot);
		
		RobotEntry entry = new RobotEntry(oRobot, i_eRobot, nIndex);
		m_oRobotList.add(entry);
		
		m_lvAddedRobots.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, m_lvAddedRobots.getHeight() + m_nRobotItemHeight));
		m_lvAddedRobots.invalidateViews();
		
	}
	
	private void startDance() {
		if (m_oRobotList.isEmpty()) {
			return;
		}
		
		for (DanceEntry entry : m_oDanceList) {
			
			switch(entry.eMove) {
			case dm_backward:
				moveBwd(entry.nDuration);
				break;
			case dm_delay:
				break;
			case dm_forward:
				moveFwd(entry.nDuration);
				break;
			case dm_rotateLeft:
				turnLeft(entry.nDuration);
				break;
			case dm_rotateRight:
				turnRight(entry.nDuration);
				break;
			}
		}
	}
	
	private void driveForward() {
		
		int nSpeed = 50;
		for (RobotEntry entry : m_oRobotList) {
			entry.oRobot.driveForward(nSpeed);
		}
		
	}
	
	private void driveBackward() {

		int nSpeed = 50;
		for (RobotEntry entry : m_oRobotList) {
			entry.oRobot.driveBackward(nSpeed);
		}
		
	}
	
	private void rotateCounterClockwise() {

		int nSpeed = 50;
		for (RobotEntry entry : m_oRobotList) {
			entry.oRobot.rotateCounterClockwise(nSpeed);
		}
		
	}
	
	private void rotateClockwise() {

		int nSpeed = 50;
		for (RobotEntry entry : m_oRobotList) {
			entry.oRobot.rotateClockwise(nSpeed);
		}
		
	}
	
	private void driveStop() {

		for (RobotEntry entry : m_oRobotList) {
			entry.oRobot.driveStop();
		}
		
	}
	
	private Runnable stopAll = new Runnable() {
		
		@Override
		public void run() {
			driveStop();
		}
	};
	
	private Runnable turnLeftAll = new Runnable() {
		
		@Override
		public void run() {
			rotateCounterClockwise();
		}
	};
	
	private Runnable turnRightAll = new Runnable() {
		
		@Override
		public void run() {
			rotateClockwise();
		}
	};
	
	private Runnable moveFwdAll = new Runnable() {
		
		@Override
		public void run() {
			driveForward();
		}
	};
	
	private Runnable moveBwdAll = new Runnable() {
		
		@Override
		public void run() {
			driveBackward();
		}
	};
	
	private class DelayTask implements Runnable {
		int delay;
		
		public DelayTask(int delay) {
			this.delay = delay;
		}
		
		public void run() {
			Utils.waitSomeTime(delay);
		}
	}
	
	// duration in seconds
	private void turnLeft(int i_nDuration) {
		uiHandler.post(turnLeftAll);
		uiHandler.post(new DelayTask(i_nDuration * 1000));
		uiHandler.post(stopAll);
	}
	
	private void turnRight(int i_nDuration) {
		uiHandler.post(turnRightAll);
		uiHandler.post(new DelayTask(i_nDuration * 1000));
		uiHandler.post(stopAll);
	}
	
	private void moveFwd(int i_nDuration) {
		uiHandler.post(moveFwdAll);
		uiHandler.post(new DelayTask(i_nDuration * 1000));
		uiHandler.post(stopAll);
	}
	
	private void moveBwd(int i_nDuration) {
		uiHandler.post(moveBwdAll);
		uiHandler.post(new DelayTask(i_nDuration * 1000));
		uiHandler.post(stopAll);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		for (ActivityResultListener listener : m_oActivityResultListener) {
			listener.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	private void dance1() {
		
		addMove(DanceMoves.dm_forward, 1);
		addMove(DanceMoves.dm_backward, 1);
		addMove(DanceMoves.dm_rotateLeft, 2);
		addMove(DanceMoves.dm_rotateRight, 4);
		addMove(DanceMoves.dm_rotateLeft, 2);
		addMove(DanceMoves.dm_forward, 2);
		addMove(DanceMoves.dm_rotateLeft, 1);
		addMove(DanceMoves.dm_rotateRight, 1);
		addMove(DanceMoves.dm_backward, 2);
		
		m_lvDanceList.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 9 * m_nDanceItemHeight));
		
	}
	
}
