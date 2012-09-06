package org.dobots.swarmcontrol;

import org.dobots.utility.Utils;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class RemoteControlHelper {
	
	public interface OnButtonPress {
		
		void buttonPressed(boolean i_bDown); 
		
	}
	
	private Activity m_oActivity;
	
	public boolean m_bControl;

	private Button m_btnControl;
	private Button m_btnFwd;
	private Button m_btnBwd;
	private Button m_btnLeft;
	private Button m_btnRight;
	
	private OnButtonPress OnFwdPress;
	private OnButtonPress OnBwdPress;
	private OnButtonPress OnLeftPress;
	private OnButtonPress OnRightPress;
	private OnButtonPress OnControlPress;

	public RemoteControlHelper(Activity i_oActivity) {
		this.m_oActivity = i_oActivity;
	}
	
	public void setProperties() {

		m_btnControl = (Button) m_oActivity.findViewById(R.id.btnRemoteControl);
		m_btnControl.setText("Remote Control: OFF");
		m_btnControl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_bControl = !m_bControl;
				if (OnControlPress != null) {
					OnControlPress.buttonPressed(m_bControl);
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
				if (OnFwdPress != null) {
					int action = e.getAction();
					switch (action & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						OnFwdPress.buttonPressed(false);
						break;
					case MotionEvent.ACTION_POINTER_UP:
						break;
					case MotionEvent.ACTION_DOWN:
						OnFwdPress.buttonPressed(true);
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
				if (OnBwdPress != null) {
					int action = e.getAction();
					switch (action & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						OnBwdPress.buttonPressed(false);
						break;
					case MotionEvent.ACTION_POINTER_UP:
						break;
					case MotionEvent.ACTION_DOWN:
						OnBwdPress.buttonPressed(true);
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
				if (OnLeftPress != null) {
					int action = e.getAction();
					switch (action & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						OnLeftPress.buttonPressed(false);
						break;
					case MotionEvent.ACTION_POINTER_UP:
						break;
					case MotionEvent.ACTION_DOWN:
						OnLeftPress.buttonPressed(true);
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
				if (OnRightPress != null) {
					int action = e.getAction();
					switch (action & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						OnRightPress.buttonPressed(false);
						break;
					case MotionEvent.ACTION_POINTER_UP:
						break;
					case MotionEvent.ACTION_DOWN:
						OnRightPress.buttonPressed(true);
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
	
	public void setControlPressListener(OnButtonPress i_oListener) {
		OnControlPress = i_oListener;
	}
	
	public void setFwdPressListener(OnButtonPress i_oListener) {
		OnFwdPress = i_oListener;
	}
	
	public void setBwdPressListener(OnButtonPress i_oListener) {
		OnBwdPress = i_oListener;
	}

	public void setLeftPressListener(OnButtonPress i_oListener) {
		OnLeftPress = i_oListener;
	}

	public void setRightPressListener(OnButtonPress i_oListener) {
		OnRightPress = i_oListener;
	}
	
	public void showControlButtons(boolean visible) {
		Utils.showLayout((LinearLayout)m_oActivity.findViewById(R.id.layRemoteControl), visible);
	}
	
	public void resetLayout() {
		m_btnControl.setText("Remote Control: OFF");
		m_bControl = false;
		updateButtons(false);
		showControlButtons(false);
	}

	public void updateButtons(boolean b) {
		m_btnControl.setEnabled(true);
	}
	
}
