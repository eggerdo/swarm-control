package org.dobots.robots;

import android.os.Handler;
import android.util.Log;

public class MoveRepeater {
	
	private static final String TAG = "MoveRepeater";
	
	public interface MoveRepeaterListener {
		void onDoMove(MoveCommand i_eMove, double i_dblSpeed);
		void onDoMove(MoveCommand i_eMove, double i_dblSpeed, int i_nRadius);
	}
	
    public enum MoveCommand {
    	MOVE_UP, MOVE_DOWN, MOVE_FWD, MOVE_BWD, MOVE_LEFT, MOVE_RIGHT, ROTATE_LEFT, ROTATE_RIGHT
    }

    private MoveRepeaterListener m_oRobot;

	private Object m_oMoveMutex = new Object();
	private Handler m_oHandler = new Handler();
	
	private int m_nInterval;
	private boolean m_bRepeat;
	
	public MoveRepeater(MoveRepeaterListener i_oRobot, int i_nInterval) {
		m_oRobot = i_oRobot;
		m_nInterval = i_nInterval;
	}
	
	public Object getMutex() {
		return m_oMoveMutex;
	}
	
	public boolean isRepeating() {
		return m_bRepeat;
	}
	
	public void startMove(MoveCommand i_eMove, double i_dblSpeed, int i_nRadius, boolean i_bRepeat) {
		startMove(new MoveRunner(i_eMove, i_dblSpeed, i_nRadius), i_bRepeat);
	}

	public void startMove(MoveCommand i_eMove, double i_dblSpeed, boolean i_bRepeat) {
		startMove(new MoveRunner(i_eMove, i_dblSpeed, 0), i_bRepeat);
	}
	
	public void startMove(MoveRunnable i_oMoveRunnable, boolean i_bRepeat) {
		stopMove();
		
		synchronized (m_oMoveMutex) {
			i_oMoveRunnable.oHandler = m_oHandler;
			i_oMoveRunnable.nInterval = m_nInterval;
			m_oHandler.post(i_oMoveRunnable);
			m_bRepeat = i_bRepeat;
		}
	}

	public void stopMove() {
		Log.d(TAG, "done");
		m_bRepeat = false;
		m_oHandler.removeCallbacksAndMessages(null);
	}
	
	public abstract class MoveRunnable implements Runnable {
		public Handler oHandler;
		public int nInterval;
	}
	
	class MoveRunner extends MoveRunnable implements Runnable {
		
		private MoveCommand eMove;
		private double dblSpeed;
		private int nRadius;
		
		public MoveRunner(MoveCommand i_eMove, double i_dblSpeed, int i_nRadius) {
			super();
			
			eMove = i_eMove;
			dblSpeed = i_dblSpeed;
		}
		
		public MoveRunner clone() {
			MoveRunner clone = new MoveRunner(this.eMove, this.dblSpeed, this.nRadius);
			clone.oHandler = this.oHandler;
			clone.nInterval = this.nInterval;
			return clone;
		}
		
		@Override
		public void run() {
			synchronized (m_oMoveMutex) {
				Log.d(TAG, eMove.toString());
				if (nRadius == 0) {
					m_oRobot.onDoMove(eMove, dblSpeed);
				} else {
					m_oRobot.onDoMove(eMove, dblSpeed, nRadius);
				}
				if (m_bRepeat) {
					oHandler.postDelayed(this.clone(), nInterval);
				}
			}
		}
		
	}

}
