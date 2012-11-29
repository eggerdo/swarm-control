package org.dobots.robots.roboscooper;

import org.dobots.robots.MoveRepeater.MoveCommand;

import android.util.Log;

class RoboScooperMoveRunner implements Runnable {

	public enum SubMoveCommand {
		LEFT, RIGHT, STRAIGHT
	}

	private final RoboScooper m_oRoboScooper;
	public MoveCommand eMove;
	public SubMoveCommand eCmd;

	protected int nRunCount = 0;
	
	public RoboScooperMoveRunner(RoboScooper i_oRoboScooper, MoveCommand i_eMove, SubMoveCommand i_eCmd) {
		m_oRoboScooper = i_oRoboScooper;
		eMove = i_eMove;
		eCmd = i_eCmd;
	}

	public void setSubMove(SubMoveCommand i_eCmd) {
		eCmd = i_eCmd;
		if (nRunCount > 0) {
			nRunCount = 1;
		}
	}
	
	@Override
	public void run() {
		String strMove = null;
		
		if (nRunCount == 0) {
			Log.d(RoboScooper.TAG, String.format("%s %s", eMove.toString(), eCmd.toString()));
			strMove = getBrainlinkMoveCommand(eMove, eCmd, false);
			m_oRoboScooper.sendCommand(strMove);
		} if (nRunCount == 1) {
			Log.d(RoboScooper.TAG, String.format("fast %s %s", eMove.toString(), eCmd.toString()));
			strMove = getBrainlinkMoveCommand(eMove, eCmd, true);
			m_oRoboScooper.sendCommand(strMove);
		}
		nRunCount++;
	}
	
	private String getBrainlinkMoveCommand(MoveCommand i_eMove, SubMoveCommand i_eCmd, boolean i_bFastMove) {
		switch (i_eMove) {
		case MOVE_FWD:
			switch (i_eCmd) {
			case STRAIGHT:
				if (i_bFastMove) {
					return RoboScooperTypes.FAST_FORWARD;
				} else {
					return RoboScooperTypes.FORWARD;
				}
			case LEFT:
				if (i_bFastMove) {
					return RoboScooperTypes.FAST_LEFT_FORWARD;
				} else {
					return RoboScooperTypes.LEFT_FORWARD;
				}
			case RIGHT:
				if (i_bFastMove) {
					return RoboScooperTypes.FAST_RIGHT_FORWARD;
				} else {
					return RoboScooperTypes.RIGHT_FORWARD;
				}
			}
		case MOVE_BWD:
			switch (i_eCmd) {
			case STRAIGHT:
				if (i_bFastMove) {
					return RoboScooperTypes.FAST_BACKWARD;
				} else {
					return RoboScooperTypes.BACKWARD;
				}
			case LEFT:
				if (i_bFastMove) {
					return RoboScooperTypes.FAST_LEFT_BACKWARD;
				} else {
					return RoboScooperTypes.LEFT_BACKWARD;
				}
			case RIGHT:
				if (i_bFastMove) {
					return RoboScooperTypes.FAST_RIGHT_BACKWARD;
				} else {
					return RoboScooperTypes.RIGHT_BACKWARD;
				}
			}
		case ROTATE_LEFT:
			if (i_bFastMove) {
				return RoboScooperTypes.FAST_LEFT;
			} else {
				return RoboScooperTypes.LEFT;
			}
		case ROTATE_RIGHT:
			if (i_bFastMove) {
				return RoboScooperTypes.FAST_RIGHT;
			} else {
				return RoboScooperTypes.RIGHT;
			}
		}
		return null;
	}
}
