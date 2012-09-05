package org.dobots.swarmcontrol.behaviours.dancing;

import java.util.ArrayList;
import java.util.List;

import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.behaviours.dancing.RobotList.RobotEntry;
import org.dobots.utility.Utils;
import org.dobots.utility.external.NumberPicker;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
	
	private DanceListAdapter m_oDanceListAdapter;
	private ListView m_lvDanceList;
	private Spinner m_spMoves;
	private Button m_btnAddMove;
	private Button m_btnStartDance;
	private Button m_btnClear;
	private Button m_btnRemoveRobots;
	private Button m_btnDance1;
	private NumberPicker m_npDuration;
	
	private static final int SHOW_STEP = 1000;
	private static final int CLEAR = 1001;

	private boolean m_bShowRemoveSteps = false;
	
	private boolean m_bStopped = false;

	private Button m_btnStopDance;
	private boolean m_bDanceRunning = false;
	
	private Handler m_oDanceHandler;
	private Thread m_oDanceExecutor = new Thread() {

		@Override
		public void run() {
		
			Looper.prepare();
			m_oDanceHandler = new Handler();
			Looper.loop();
		}
		
	};
	
	private Handler uiHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {

            switch (msg.what) {
            case SHOW_STEP:
            	int nStepNr = msg.arg1;
    			m_oDanceListAdapter.setSelection(nStepNr);
    			m_lvDanceList.invalidateViews();
    			break;
            case CLEAR:
            	m_oDanceListAdapter.clearSelection();
            	m_lvDanceList.invalidateViews();
            	break;
            }
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.m_oActivity = this;
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		m_oDanceList = DancingMain.getInstance().getDanceList();
		m_oRobotList = DancingMain.getInstance().getRobotList();
		
		m_oDanceExecutor.start();
		
		setProperties();
	}
	
	public void setProperties() {
		setContentView(R.layout.dancing_dancelist);

		m_lvDanceList = (ListView) m_oActivity.findViewById(R.id.lvDancing_DanceList);
		m_oDanceListAdapter = new DanceListAdapter(m_oActivity, m_oDanceList);
		m_oDanceListAdapter.setNotifyOnChange(true);
		m_lvDanceList.setAdapter(m_oDanceListAdapter);
		registerForContextMenu(m_lvDanceList);
		m_lvDanceList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


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
				if (!m_bDanceRunning) {
					startDance();
				}
			}
		});
		
		m_btnStopDance = (Button) m_oActivity.findViewById(R.id.btnDancing_StopDance);
		m_btnStopDance.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (m_bDanceRunning) {
					stopDance();
				}
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
		
		m_bDanceRunning = true;
		
		MultiRobotControl.getInstance().enableControl(true);
		
//		for (DanceEntry entry : m_oDanceList) {
//			
//			switch(entry.eMove) {
//			case dm_backward:
//				moveBwd(entry.nDuration);
//				break;
//			case dm_delay:
//				break;
//			case dm_forward:
//				moveFwd(entry.nDuration);
//				break;
//			case dm_rotateLeft:
//				turnLeft(entry.nDuration);
//				break;
//			case dm_rotateRight:
//				turnRight(entry.nDuration);
//				break;
//			}
//		}
		for (int i = 0; i < m_oDanceList.size(); i++) {
			DanceEntry entry = m_oDanceList.get(i);
			DanceMove oMove = new DanceMove(entry.eMove, entry.nDuration, i);
			m_oDanceHandler.post(oMove);
		}
		
		danceEnd();
		
	}
	
	private void danceEnd() {
		m_oDanceHandler.post(done);
	}
	
	private void stopDance() {
		stopAndCancelAll();
		m_bDanceRunning = false;
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
		
		if (i_eMove != DanceMoves.dm_nothing) {
			DanceEntry entry = new DanceEntry(i_eMove, i_nDuration);
			m_oDanceList.add(entry);
	
			m_lvDanceList.invalidateViews();
		}
	}
	
	private class DanceMove implements Runnable {
		
		private int nStepNr = -1;
		private int nDuration;
		private DanceMoves eMove;

		DanceMove(DanceMoves i_eMove, int i_nDuration, int i_nStepNr) {
			eMove = i_eMove;
			nDuration = i_nDuration;
			nStepNr = i_nStepNr;
		}
		
		protected void setStepNr(int i_nStepNr) {
			nStepNr = i_nStepNr;
		}
		
		protected void showStep() {
			Message msg = Message.obtain();
			msg.what = SHOW_STEP;
			msg.arg1 = nStepNr;
			uiHandler.sendMessage(msg);
		}

		private void startMove() {
			switch(eMove) {
			case dm_backward:
				MultiRobotControl.driveBackward();
				break;
			case dm_delay:
			case dm_nothing:
				break;
			case dm_forward:
				MultiRobotControl.driveForward();
				break;
			case dm_rotateLeft:
				MultiRobotControl.rotateCounterClockwise();
				break;
			case dm_rotateRight:
				MultiRobotControl.rotateClockwise();
				break;
			}
		}
		
		public void run() {
			showStep();
			startMove();
			Utils.waitSomeTime(nDuration * 1000);
		}
		
	}
	
	private DanceMove done = new DanceMove(DanceMoves.dm_nothing, 0, -1) {
		
		@Override
		public void run() {
			showStep();
			MultiRobotControl.driveStop();
			m_bDanceRunning = false;
		}
		
	};

	private void stopAndCancelAll() {
		m_oDanceHandler.removeCallbacksAndMessages(null);
		done.run();
	}
	
	private enum DanceMoves {
		dm_forward("Forward"),
		dm_backward("Backward"),
		dm_rotateLeft("Rotate Left"),
		dm_rotateRight("Rotate Right"),
		dm_delay("Dealy"),
		dm_nothing("");
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
		boolean bSelected;
		
		public DanceEntry(DanceMoves i_eMove, int i_nDuration) {
			this.eMove = i_eMove;
			this.nDuration = i_nDuration;
			this.bRemove = false;
		}
		
	}

	private class DanceListAdapter extends ArrayAdapter<DanceEntry> {
		
		private final Activity context;
		private final List<DanceEntry> list;
		
		int nSelectedItem = -1;
		
		public DanceListAdapter(Activity context, List<DanceEntry> list) {
			super(context, R.layout.dancing_move, list);
			this.context = context;
			this.list = list;
		}
		
		public void setSelection(int i_nPosition) {
			nSelectedItem = i_nPosition;
		}
		
		public void clearSelection() {
			nSelectedItem = -1;
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
			
			if (position == nSelectedItem) {
				view.setBackgroundColor(Color.RED);
			} else {
				view.setBackgroundColor(Color.TRANSPARENT);
			}
			
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
