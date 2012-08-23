package org.dobots.swarmcontrol.behaviours.dancing;

import java.util.ArrayList;
import java.util.List;

import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.behaviours.dancing.RobotList.RobotEntry;
import org.dobots.utility.Utils;
import org.dobots.utility.external.NumberPicker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class DanceList extends Activity {

	private Activity m_oActivity;

	private ArrayList<DanceEntry> m_oDanceList;
	private ArrayList<RobotEntry> m_oRobotList;

	private ListView m_lvDanceList;
	private Spinner m_spMoves;
	private Button m_btnAddMove;
	private Button m_btnStartDance;
	private Button m_btnClear;
	private Button m_btnRemoveRobots;
	private Button m_btnDance1;
	private NumberPicker m_npDuration;

	private Handler uiHandler = new Handler();
	
	private boolean m_bShowRemoveSteps = false;
	
	private boolean m_bStopped = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.m_oActivity = this;
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		m_oDanceList = DancingMain.getInstance().getDanceList();
		m_oRobotList = DancingMain.getInstance().getRobotList();
		
		setProperties();
	}
	
	public void setProperties() {
		setContentView(R.layout.dancing_dancelist);

		m_lvDanceList = (ListView) m_oActivity.findViewById(R.id.lvDancing_DanceList);
		ArrayAdapter<DanceEntry> oDanceListAdapter = new DanceListAdapter(m_oActivity, m_oDanceList);
		m_lvDanceList.setAdapter(oDanceListAdapter);
		registerForContextMenu(m_lvDanceList);

		m_npDuration = (NumberPicker) m_oActivity.findViewById(R.id.npDuration);
		m_npDuration.setCurrent(1);
		m_npDuration.setFocusable(false);
		
		m_spMoves = (Spinner) m_oActivity.findViewById(R.id.spDancing_Moves);
		
		ArrayAdapter<DanceMoves> movesAdapter = new ArrayAdapter<DanceMoves>(m_oActivity, 
				android.R.layout.simple_spinner_item, DanceMoves.values());
		movesAdapter.setDropDownViewResource(android.R.layout.select_dialog_item);
		m_spMoves.setAdapter(movesAdapter);		
		
		m_btnAddMove = (Button) m_oActivity.findViewById(R.id.btnDancing_AddMove);
		m_btnAddMove.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DanceMoves eMove = (DanceMoves)m_spMoves.getSelectedItem();
				addMove(eMove, m_npDuration.getCurrent());
			}
		});
		
		m_btnStartDance = (Button) m_oActivity.findViewById(R.id.btnDancing_StartDance);
		m_btnStartDance.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startDance();
			}
		});
		
		m_btnClear = (Button) m_oActivity.findViewById(R.id.btnClear);
		m_btnClear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_oDanceList.clear();
				m_lvDanceList.invalidateViews();
			}
		});
		
		m_btnRemoveRobots = (Button) m_oActivity.findViewById(R.id.btnDancing_RemoveSteps);
		m_btnRemoveRobots.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!m_bShowRemoveSteps) {
					if (m_lvDanceList.getChildCount() > 0) {
						showRemoveSteps();
						m_btnRemoveRobots.setText("Done");
					}
				} else {
					removeSteps();
					m_btnRemoveRobots.setText("Remove Robots...");
				}
			}
		});

		m_btnDance1 = (Button) m_oActivity.findViewById(R.id.btnDance1);
		m_btnDance1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dance1();
			}
		});
		
	}

	private void showRemoveSteps() {
		m_bShowRemoveSteps = true;
		m_lvDanceList.invalidateViews();
	}
	
	private void removeSteps() {
		m_bShowRemoveSteps= false;
		
		for (int i = m_oDanceList.size()-1; i >= 0 ; i--) {
			DanceEntry entry = m_oDanceList.get(i);
			if (entry.bRemove) {
				m_oDanceList.remove(entry);
			}
		}
		m_lvDanceList.invalidateViews();
	}

	private void startDance() {
		if (m_oRobotList.isEmpty()) {
			return;
		}
		
		MultiRobotControl.getInstance().enableControl(true);
		
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
		
	}

	private void addMove(DanceMoves i_eMove, int i_nDuration) {
		
		DanceEntry entry = new DanceEntry(i_eMove, i_nDuration);
		m_oDanceList.add(entry);

		m_lvDanceList.invalidateViews();
	}

	private Runnable stopAll = new Runnable() {
		
		@Override
		public void run() {
			MultiRobotControl.driveStop();
		}
	};
	
	private Runnable turnLeftAll = new Runnable() {
		
		@Override
		public void run() {
			MultiRobotControl.rotateCounterClockwise();
		}
	};
	
	private Runnable turnRightAll = new Runnable() {
		
		@Override
		public void run() {
			MultiRobotControl.rotateClockwise();
		}
	};
	
	private Runnable moveFwdAll = new Runnable() {
		
		@Override
		public void run() {
			MultiRobotControl.driveForward();
		}
	};
	
	private Runnable moveBwdAll = new Runnable() {
		
		@Override
		public void run() {
			MultiRobotControl.driveBackward();
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
	
	private void stopAndCancelAll() {
		uiHandler.removeCallbacksAndMessages(null);
		uiHandler.post(stopAll);
	}
	
	private enum DanceMoves {
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
	
	public class DanceEntry {
		
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
				
				viewHolder.cbRemove = (CheckBox) view.findViewById(R.id.cbDancing_RemoveStep);
				viewHolder.cbRemove.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						DanceEntry oEntry = (DanceEntry) viewHolder.cbRemove.getTag();
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
			
			holder.lblStep.setText(String.format("%d.", position + 1));
			holder.lblMove.setText(oEntry.eMove.toString());
			holder.lblDuration.setText(String.format("%ds", oEntry.nDuration));
			
			if (m_bShowRemoveSteps) {
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
		if (v.getId() == R.id.lvDancing_DanceList) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(String.format("Step %d", info.position + 1));
			menu.add("Remove");
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		
		m_oDanceList.remove(info.position);
		m_lvDanceList.invalidateViews();
		return true;
	}
	
}
