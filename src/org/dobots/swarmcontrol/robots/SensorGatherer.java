package org.dobots.swarmcontrol.robots;

import android.app.Activity;
import android.os.Handler;

public class SensorGatherer extends Thread {

	private volatile boolean m_bStopped = false;
	private volatile boolean m_bPaused = false;
	
	protected Handler m_oHandler;
	protected Runnable m_oGUIUpdater;
	protected Activity m_oActivity;
	
	protected boolean m_bEnabled = false;
	
//	public SensorGatherer() {
//		// why?
//	}
	
	public SensorGatherer(Activity i_oActivity) {
		m_oActivity = i_oActivity;

		m_oHandler = new Handler();
	}
	
	public void stopThread() {
		m_bStopped = true;
		interrupt();
	}
	
	public void pauseThread() {
		m_bPaused = true;
	}
	
	public void resumeThread() {
		m_bPaused = false;
	}

	@Override
	public void run() {
		
		while (!m_bStopped) {
			if (!m_bPaused) {
				execute();
			}
		
			try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
	}
	
	protected void execute() {
		// needs to be defined by child class
	}
}
