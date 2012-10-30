package org.dobots.utility.joystick;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

public class JoystickSurfaceThread extends Thread {

	private SurfaceHolder m_oSurfaceHolder;

	//for consistent rendering
	private long m_lSleepTime;
	// last update
	private long m_lUpdateTime;
	//amount of time to sleep for (in milliseconds)
	private long m_lDelay=70;
	
	public final static int RUNNING = 1;
	public final static int PAUSED = 2;
	public final static int STOPPED = 3;
	public int m_nState = RUNNING;

	Joystick m_oJoystick;

	public JoystickSurfaceThread(Joystick i_oJoystick){
		m_oJoystick = i_oJoystick;
		
		m_oSurfaceHolder = m_oJoystick.getHolder();
	}

	//This is the most important part of the code. It is invoked when the call to start() is
	//made from the SurfaceView class. It loops continuously until the thread is stopped
	@Override
	public void run() {

		//UPDATE
		while (m_nState==RUNNING) {
//			Log.d("State","Thread is runnig");
			//time before update
			m_lUpdateTime = System.nanoTime();

			//DRAW
			Canvas canvas = null;
			synchronized (m_oSurfaceHolder) {
				try {
					//lock canvas so nothing else can use it
					canvas = m_oSurfaceHolder.lockCanvas(null);
					//clear the screen with the black painter.
					//reset the canvas
					canvas.drawColor(Color.BLACK);
					//This is where we draw the joystick
					m_oJoystick.doDraw(canvas);
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (canvas != null) {
						m_oSurfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
			}

			//SLEEP
			//Sleep time. Time required to sleep to keep game consistent
			//This starts with the specified delay time (in milliseconds) then subtracts from that the
			//actual time it took to update and render the game. This allows the joystick to render smoothly.
			this.m_lSleepTime = m_lDelay-((System.nanoTime()-m_lUpdateTime)/1000000L);

			try {
				//actual sleep code
				if(m_lSleepTime>0){
					Thread.sleep(m_lSleepTime);
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(JoystickSurfaceThread.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			while (m_nState==PAUSED){
//				Log.d("State","Thread is pausing");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}}
