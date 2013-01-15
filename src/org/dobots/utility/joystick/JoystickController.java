package org.dobots.utility.joystick;

import android.graphics.Point;
import android.view.MotionEvent;

public class JoystickController {

	public Point m_oOrigin;
	public Point m_oTouchingPoint;
	
	private boolean m_bDragging = false;
	private boolean m_bContinuous = false;
	
	private MotionEvent lastEvent;
	
	public int m_nMaxMoveRadius;
	
	private IJoystickListener m_oListener;

//	public JoystickController(Point i_oPosition, int i_nMaxMoveRadius) {
//		m_oOrigin = i_oPosition;
//		m_oTouchingPoint = new Point(m_oOrigin.x, m_oOrigin.y);
//		m_nMaxMoveRadius = i_nMaxMoveRadius;
//	}
	
	public void setPosition(Point i_oPosition) {
		m_oOrigin = i_oPosition;
		m_oTouchingPoint = new Point(m_oOrigin.x, m_oOrigin.y);
	}
	
	public void setMaxMoveRadius(int i_nMaxMoveRadius) {
		m_nMaxMoveRadius = i_nMaxMoveRadius;
	}
	
	public void setUpdateListener(IJoystickListener i_oListener) {
		m_oListener = i_oListener;
	}

	public void removeUpdateListener(IJoystickListener i_oListener) {
		if (m_oListener == i_oListener) {
			m_oListener = null;
		}
	}
	
	// continuous means that if the joystick is "pressed" without moving
	// then the last event will be re-executed (until the joystick is released)
	// otherwise only if the joystick is actually being moved is an event 
	// triggered (default)
	public void setContinuous(boolean i_bContinuous) {
		m_bContinuous = i_bContinuous;
	}

	public void update(MotionEvent event){
		if (event == null && lastEvent == null)
		{
			return;
		}else if(event == null && lastEvent != null) {
			if (m_bContinuous) {
				event = lastEvent; // if joystick is not moved, continue moving object in the same direction as last joystick move
			} else {
				return; // only move object if joystick is moved
			}
		}else {
			lastEvent = event;
		}
		
		//drag drop 
		if ( event.getAction() == MotionEvent.ACTION_DOWN ){
			m_oListener.onJoystickTouch(true);
			m_bDragging = true;
		}else if ( event.getAction() == MotionEvent.ACTION_UP){
			m_oListener.onJoystickTouch(false);
			m_bDragging = false;
		}

		if ( m_bDragging ){
			// get the pos
			m_oTouchingPoint.x = (int)event.getX();
			m_oTouchingPoint.y = (int)event.getY();

			//get the angle
			double angle = Math.atan2(m_oTouchingPoint.y - m_oOrigin.y,m_oTouchingPoint.x - m_oOrigin.x)/(Math.PI/180.0);
			
			int dx = m_oTouchingPoint.x - m_oOrigin.x;
			int dy = m_oTouchingPoint.y - m_oOrigin.y;
			double distance = Math.sqrt(dx * dx + dy * dy);
			
			if (distance > m_nMaxMoveRadius) {
				m_oTouchingPoint.x = (int)Math.round(m_oOrigin.x + Math.cos(angle*(Math.PI/180.0))*m_nMaxMoveRadius);
				m_oTouchingPoint.y = (int)Math.round(m_oOrigin.y + Math.sin(angle*(Math.PI/180.0))*m_nMaxMoveRadius);
			}

			if (m_oListener != null) {
				m_oListener.onUpdate(Math.min(distance / m_nMaxMoveRadius, 1.0) * 100, -angle);
			}

		} else if (!m_bDragging)
		{
			// Snap back to center when the joystick is released
			m_oTouchingPoint.x = m_oOrigin.x;
			m_oTouchingPoint.y = m_oOrigin.y;

			if (m_oListener != null) {
				m_oListener.onUpdate(0, 0);
				lastEvent = null;
			}
			
		}
	}

	public Point getTouchPoint() {
		return m_oTouchingPoint;
	}

}
