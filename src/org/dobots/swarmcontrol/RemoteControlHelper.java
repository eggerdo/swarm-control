package org.dobots.swarmcontrol;

import org.dobots.robots.RobotDevice;
import org.dobots.utility.LockableScrollView;
import org.dobots.utility.Utils;
import org.dobots.utility.joystick.Joystick;
import org.dobots.utility.joystick.JoystickListener;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class RemoteControlHelper implements JoystickListener, RemoteControlListener {

	private static final String TAG = "RemoteControlHelper";
	
	public enum Move {
		NONE, FORWARD, BACKWARD, LEFT, RIGHT
	}
	
	private RemoteControlListener m_oRemoteControlListener;
	
	private Move lastMove = Move.NONE;

	private long lastTime = SystemClock.uptimeMillis();
	private double updateFrequency = 5.0; // Hz
	private int threshold = 20;
	
	private Activity m_oActivity;
	
	public boolean m_bControl;
	public boolean m_bAdvancedControl = true;

	private Button m_btnControl;
	private Button m_btnFwd;
	private Button m_btnBwd;
	private Button m_btnLeft;
	private Button m_btnRight;
	
	private LockableScrollView m_oScrollView;
	private LinearLayout m_oAdvancedControl;
	
	private Joystick m_oJoystick;
	
	private RobotDevice m_oRobot;

	// At least one of the parameters i_oRobot or i_oListener has to be assigned! the other can be null.
	// It is also possible to assign both
	public RemoteControlHelper(Activity i_oActivity, RobotDevice i_oRobot, RemoteControlListener i_oListener) {
		this.m_oActivity = i_oActivity;
		
		// one of the two parameters, RobotDevice or RemoteControlListener has to be assigned!
		assert(!(i_oRobot == null && i_oListener == null));
		
		m_oRobot = i_oRobot;
		
		// by default, this class is handling the move commands triggered either by the remote control buttons
		// or by the joystick. However it is possible to overwrite the listener so that move commands
		// can be individually handled
		if (i_oListener == null) {
			m_oRemoteControlListener = this;
		} else {
			m_oRemoteControlListener = i_oListener;
		}
	}
	
	public void setRemoteControlListener(RemoteControlListener i_oListener) {
		m_oRemoteControlListener = i_oListener;
	}
	
	public void removeRemoteControlListener(RemoteControlListener i_oListener) {
		if (m_oRemoteControlListener == i_oListener) {
			m_oRemoteControlListener = this;
		}
	}
	
	public void setProperties() {
		
		m_oScrollView = (LockableScrollView) m_oActivity.findViewById(R.id.scrollview);
		
		m_oAdvancedControl = (LinearLayout) m_oActivity.findViewById(R.id.layAdvancedControl);
		
		m_oJoystick = (Joystick) m_oActivity.findViewById(R.id.oJoystick);
		m_oJoystick.setUpdateListener(this);

		m_btnControl = (Button) m_oActivity.findViewById(R.id.btnRemoteControl);
		m_btnControl.setText("Remote Control: OFF");
		m_btnControl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bControl = !m_bControl;
				if (m_oRemoteControlListener != null) {
					m_oRemoteControlListener.enableControl(m_bControl);
				}
				showControlButtons(m_bControl);
				((Button)v).setText("Remote Control: " + (m_bControl ? "ON" : "OFF"));
			}
		});
	
		m_btnFwd = (Button) m_oActivity.findViewById(R.id.btnFwd);
		m_btnLeft = (Button) m_oActivity.findViewById(R.id.btnLeft);
		m_btnBwd = (Button) m_oActivity.findViewById(R.id.btnBwd);
		m_btnRight = (Button) m_oActivity.findViewById(R.id.btnRight);
		
		m_btnFwd.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (m_oRemoteControlListener != null) {
					int action = e.getAction();
					switch (action & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						m_oRemoteControlListener.onMove(Move.NONE);
						break;
					case MotionEvent.ACTION_POINTER_UP:
						break;
					case MotionEvent.ACTION_DOWN:
						m_oRemoteControlListener.onMove(Move.FORWARD);
						break;
					case MotionEvent.ACTION_POINTER_DOWN:
						break;					
					case MotionEvent.ACTION_MOVE:
						break;
					}
				}
				return true;
			}
		});
		
		m_btnBwd.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (m_oRemoteControlListener != null) {
					int action = e.getAction();
					switch (action & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						m_oRemoteControlListener.onMove(Move.NONE);
						break;
					case MotionEvent.ACTION_POINTER_UP:
						break;
					case MotionEvent.ACTION_DOWN:
						m_oRemoteControlListener.onMove(Move.BACKWARD);
						break;
					case MotionEvent.ACTION_POINTER_DOWN:
						break;					
					case MotionEvent.ACTION_MOVE:
						break;
					}
				}
				return true;
			}
		});

		m_btnLeft.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (m_oRemoteControlListener != null) {
					int action = e.getAction();
					switch (action & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						m_oRemoteControlListener.onMove(Move.NONE);
						break;
					case MotionEvent.ACTION_POINTER_UP:
						break;
					case MotionEvent.ACTION_DOWN:
						m_oRemoteControlListener.onMove(Move.LEFT);
						break;
					case MotionEvent.ACTION_POINTER_DOWN:
						break;					
					case MotionEvent.ACTION_MOVE:
						break;
					}
				}
				return true;
			}
		});

		m_btnRight.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (m_oRemoteControlListener != null) {
					int action = e.getAction();
					switch (action & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						m_oRemoteControlListener.onMove(Move.NONE);
						break;
					case MotionEvent.ACTION_POINTER_UP:
						break;
					case MotionEvent.ACTION_DOWN:
						m_oRemoteControlListener.onMove(Move.RIGHT);
						break;
					case MotionEvent.ACTION_POINTER_DOWN:
						break;					
					case MotionEvent.ACTION_MOVE:
						break;
					}
				}
				return true;
			}
		});
		
		showControlButtons(false);
	}
	
	public void setAdvancedControl(boolean i_bAdvancedControl) {
		m_bAdvancedControl = i_bAdvancedControl;
		Utils.showLayout(m_oAdvancedControl, m_bAdvancedControl);
	}

	public void showControlButtons(boolean visible) {
		if (!visible) {
			Utils.showLayout((LinearLayout)m_oActivity.findViewById(R.id.layRemoteControl), visible);
			if (m_bAdvancedControl) {
				Utils.showLayout(m_oAdvancedControl, visible);
			} 
		} else {
			Utils.showLayout((LinearLayout)m_oActivity.findViewById(R.id.layRemoteControl), visible);
			if (m_bAdvancedControl) {
				Utils.showLayout(m_oAdvancedControl, visible);
			} 
		}
	}
	
	public void resetLayout() {
		m_btnControl.setText("Remote Control: OFF");
		m_bControl = false;
		updateButtons(false);
		showControlButtons(false);
	}

	public void updateButtons(boolean enabled) {
		m_btnControl.setEnabled(enabled);
	}
	
	@Override
	public void onJoystickTouch(boolean start) {
		if (m_oScrollView != null) {
			if (start) {
				m_oScrollView.setScrollingEnabled(false);
			} else {
				m_oScrollView.setScrollingEnabled(true);
			}
		} else {
			Log.e(TAG, "scroll view not lockable!");
		}
	}

	@Override
	public void onUpdate(double i_dblPercentage, double i_dblAngle) {
		
		if (i_dblPercentage == 0 && i_dblAngle == 0) {
			// if percentage and angle is 0 this means the joystick was released
			// so we stop the robot
			m_oRemoteControlListener.onMove(Move.NONE, 0, 0);
			lastMove = Move.NONE;
		} else {

			// only allow a rate of updateFrequency to send drive commands
			// otherwise it will overload the robot's command queue
			if ((SystemClock.uptimeMillis() - lastTime) < 1/updateFrequency * 1000)
				return;

			lastTime = SystemClock.uptimeMillis();
			double dblAbsAngle = Math.abs(i_dblAngle);
			
			// determine which move should be executed based on the
			// last move and the angle of the joystick
			Move thisMove = Move.NONE;
			switch(lastMove) {
			case NONE:
				// for a low percentage (close to the center of the joystick) the
				// angle is too sensitive, so we only start once the percentage
				// is over the threshold
				if (i_dblPercentage < threshold) {
					return;
				}
			case LEFT:
			case RIGHT:
				// if the last move was left (or right respectively) we use a window
				// of +- 30 degrees, otherwise we switch to moving forward or backward
				// depending on the angle
				if (dblAbsAngle < 30) {
					thisMove = Move.LEFT;
				} else if ((180 - dblAbsAngle) < 30) {
					thisMove = Move.RIGHT;
				} else if (i_dblAngle > 0) {
					thisMove = Move.FORWARD;
				} else if (i_dblAngle < 0) {
					thisMove = Move.BACKWARD;
				}
				break;
			case BACKWARD:
				// if the last move was backward and the angle is within
				// 10 degrees of 0 or 180 degrees we still move backward
				// and cap the degree to 0 or 180 respectively
				if (i_dblAngle < 0) {
					thisMove = Move.BACKWARD;
				} else if (i_dblAngle < 10) {
					dblAbsAngle = 0;
					thisMove = Move.BACKWARD;
				} else if (i_dblAngle > 170) {
					dblAbsAngle = 180;
					thisMove = Move.BACKWARD;
				} else {
					thisMove = Move.FORWARD;
				}
				break;
			case FORWARD:
				// if the last move was forward and the angle is within
				// 10 degrees of 0 or 180 degrees we still move forward
				// and cap the degree to 0 or 180 respectively
				if (i_dblAngle > 0) {
					thisMove = Move.FORWARD;
				} else if (i_dblAngle > -10) {
					dblAbsAngle = 0;
					thisMove = Move.FORWARD;
				} else if (i_dblAngle < -170) {
					dblAbsAngle = 180;
					thisMove = Move.FORWARD;
				} else {
					thisMove = Move.BACKWARD;
				}
				break;
			}
			
			m_oRemoteControlListener.onMove(thisMove, i_dblPercentage, dblAbsAngle);
			lastMove = thisMove;
		}
	}


	// called by RemoteControlHelper when the joystick is used (if no other RemoteControlListener
	// was assigned)
	@Override
	public void onMove(Move i_oMove, double i_dblSpeed, double i_dblAngle) {

		// execute this move
		switch(i_oMove) {
		case NONE:
			m_oRobot.moveStop();
			Log.i(TAG, "stop()");
			break;
		case BACKWARD:
			m_oRobot.moveBackward(i_dblSpeed, i_dblAngle);
			Log.i(TAG, String.format("bwd(%f, %f)", i_dblSpeed, i_dblAngle));
			break;
		case FORWARD:
			m_oRobot.moveForward(i_dblSpeed, i_dblAngle);
			Log.i(TAG, String.format("fwd(%f, %f)", i_dblSpeed, i_dblAngle));
			break;
		case LEFT:
			m_oRobot.rotateClockwise(i_dblSpeed);
			Log.i(TAG, String.format("cw(%f)", i_dblSpeed));
			break;
		case RIGHT:
			m_oRobot.rotateCounterClockwise(i_dblSpeed);
			Log.i(TAG, String.format("c cw(%f)", i_dblSpeed));
			break;
		}
	}

	// called by RemoteControlHelper when the buttons are used (if no other RemoteControlListener
	// was assigned)
	@Override
	public void onMove(Move i_oMove) {
		
		// execute this move
		switch(i_oMove) {
		case NONE:
			m_oRobot.moveStop();
			Log.i(TAG, "stop()");
			break;
		case BACKWARD:
			m_oRobot.moveBackward();
			Log.i(TAG, "bwd()");
			break;
		case FORWARD:
			m_oRobot.moveForward();
			Log.i(TAG, "fwd()");
			break;
		case LEFT:
			m_oRobot.rotateClockwise();
			Log.i(TAG, "cw()");
			break;
		case RIGHT:
			m_oRobot.rotateCounterClockwise();
			Log.i(TAG, "c cw()");
			break;
		}
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		m_oRobot.enableControl(i_bEnable);
	}
		
}
