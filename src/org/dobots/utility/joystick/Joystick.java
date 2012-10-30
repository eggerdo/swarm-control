package org.dobots.utility.joystick;


import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.R.drawable;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Joystick extends SurfaceView implements SurfaceHolder.Callback {
	
	public static final String TAG = "Joystick";
	
	private Bitmap m_bmpJoystick;
	private Bitmap m_bmpJoystickBg;

	private JoystickController m_oController;

	private JoystickSurfaceThread m_oThread;
	
	public Point m_oPosition;

    public Joystick(Context aContext, AttributeSet aAttrs) {
        super(aContext, aAttrs);

        getHolder().addCallback(this); //register SurfaceHolder.Callback
        getHolder().setKeepScreenOn(true); //keep screen on when this SurfaceView is displayed
        
        m_oController = new JoystickController();
    }
    
	private void init() {
		// initialize joystick gui elements
		m_oPosition = new Point(getWidth() / 2, getHeight() / 2);

		m_bmpJoystick = (Bitmap)BitmapFactory.decodeResource(getContext().getResources(), R.drawable.joystick);
		m_bmpJoystickBg = (Bitmap)BitmapFactory.decodeResource(getContext().getResources(), R.drawable.joystickbg);
		
		// move distance is the maximum radius in which the center of the joystick can be moved. 
		int nMoveDistance = (int)(m_bmpJoystickBg.getWidth() / 2);

		// scale the bitmaps in such a way that the available space (which is defined by width and height)
		// is used most efficiently. to do that we first get the smaller dimension, and then calculate
		// the scaling factor necessary to fit everything inside. 
		// - the scaling factor is calculated from the available size divided by the maximum extension of the
		//   joystick
		// - the maximum extension is 2 x the move distance of the joystick's center + 2 x the radius of the joystick 
		//   (=width of the joystick), or if that is smaller than the width of the joystick's background we use that
		//   instead
		double dblDimension = Math.min(getWidth(), getHeight());
		double dblScaleFactor = dblDimension / Math.max(nMoveDistance * 2 + m_bmpJoystick.getWidth(), m_bmpJoystickBg.getWidth());

		// scale all the dimensions (width, height and move distance) with the scaling factor
		m_bmpJoystickBg = Bitmap.createScaledBitmap(m_bmpJoystickBg, (int)(dblScaleFactor * m_bmpJoystickBg.getWidth()), 
				(int)(dblScaleFactor * m_bmpJoystickBg.getHeight()), false);
		m_bmpJoystick = Bitmap.createScaledBitmap(m_bmpJoystick, (int)(dblScaleFactor * m_bmpJoystick.getWidth()), 
				(int)(dblScaleFactor * m_bmpJoystick.getHeight()), false);
		
		m_oController.setPosition(m_oPosition);
		m_oController.setMaxMoveRadius((int)(dblScaleFactor * nMoveDistance));

		//initialize our Thread class. A call will be made to start it later
		m_oThread = new JoystickSurfaceThread(this);
		setFocusable(true);
	}

	public void doDraw(Canvas canvas) {
		//update the pointer
		m_oController.update(null);
		
		//draw the joystick background
		canvas.drawBitmap(m_bmpJoystickBg, m_oPosition.x - m_bmpJoystickBg.getWidth() / 2, m_oPosition.y - m_bmpJoystickBg.getHeight() / 2, null);

		//draw the dragable joystick
		Point touchPoint = m_oController.getTouchPoint();
		canvas.drawBitmap(m_bmpJoystick, touchPoint.x - m_bmpJoystick.getWidth() / 2, touchPoint.y - m_bmpJoystick.getHeight() / 2, null);
	}
	
	public void setUpdateListener(JoystickListener i_oListener) {
		m_oController.setUpdateListener(i_oListener);
	}
	
	public void removeUpdateListener(JoystickListener i_oListener) {
		m_oController.removeUpdateListener(i_oListener);
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	m_oController.update(event);
    	return true;
    }
    
	@Override 
	public void surfaceDestroyed(SurfaceHolder arg0) {
		boolean retry = true;
		//code to end joystick gui thread
		m_oThread.m_nState = JoystickSurfaceThread.STOPPED;
		while (retry) {
			try {
				synchronized (getHolder()) {
					//code to kill Thread
					m_oThread.join();
					retry = false;
				}
			} catch (InterruptedException e) {}
		}
		int i = 0;
	}

	@Override 
	public void surfaceCreated(SurfaceHolder arg0) {
        init();
        m_oThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		int i = 0;
	}
	
}
