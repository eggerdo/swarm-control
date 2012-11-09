package org.dobots.swarmcontrol.robots;

import org.dobots.swarmcontrol.BaseActivity;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

public class SensorGatherer extends Thread {

	private volatile boolean m_bStopped = false;
	private volatile boolean m_bPaused = false;
	
	protected Handler m_oUiHandler;
	protected Runnable m_oGUIUpdater;
	protected BaseActivity m_oActivity;
	
	protected boolean m_bEnabled = false;
	
//	public SensorGatherer() {
//		// why?
//	}
	
	public SensorGatherer(BaseActivity i_oActivity) {
		m_oActivity = i_oActivity;

		m_oUiHandler = new Handler(Looper.getMainLooper());
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
