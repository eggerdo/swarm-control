package org.dobots.swarmcontrol.robots.ac13;

import org.dobots.robots.ac13.AC13Rover;
import org.dobots.robots.ac13.AC13RoverTypes.VideoResolution;
import org.dobots.robots.ac13.IAC13VideoListener;
import org.dobots.swarmcontrol.BaseActivity;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.SensorGatherer;
import org.dobots.utility.ScalableImageView;
import org.dobots.utility.Utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class AC13RoverSensorGatherer extends SensorGatherer implements IAC13VideoListener {

	private AC13Rover m_oRover;

	private boolean m_bVideoEnabled = true;
	private boolean m_bVideoConnected = false;
	private boolean m_bVideoScaled = false;

	private ProgressBar m_pbLoading;
	private ScalableImageView m_ivVideo;
	
	private FrameLayout m_layCamera;

	final Handler m_oSensorDataUiUpdater = new Handler();
	
	public AC13RoverSensorGatherer(BaseActivity i_oActivity, AC13Rover i_oRover) {
		super(i_oActivity);
		m_oRover = i_oRover;
		
		setProperties();

		initialize();
		
		start();
	}
	
	private void initialize() {
		m_bVideoConnected = false;
	}

	public void resetLayout() {
		initialize();
		
		showView(m_ivVideo, false);
	}
	
	private void setProperties() {
		m_pbLoading = (ProgressBar) m_oActivity.findViewById(R.id.pbLoading);
		m_ivVideo = (ScalableImageView) m_oActivity.findViewById(R.id.ivCamera);
		
		m_layCamera = (FrameLayout) m_oActivity.findViewById(R.id.layCamera);
	}

	@Override
	public void frameReceived(final Bitmap bmp) {

		if (m_bVideoEnabled) {
			m_oSensorDataUiUpdater.post(new Runnable() {
				@Override
				public void run() {

					if (!m_bVideoConnected) {
						m_oSensorDataUiUpdater.removeCallbacks(m_oTimeoutRunnable);
						m_bVideoConnected = true;
						showVideoLoading(false);
					}
					
					m_ivVideo.setImageBitmap(bmp);
				}
			});
		}
	}
	
	private void showVideoLoading(final boolean i_bShow) {
		m_oSensorDataUiUpdater.post(new Runnable() {
			@Override
			public void run() {
				showView(m_ivVideo, !i_bShow);
				showView(m_pbLoading, i_bShow);
			}
		});
	}
	
	private void showView(View i_oView, boolean i_bShow) {
		if (i_bShow) {
			i_oView.setVisibility(View.VISIBLE);
		} else {
			i_oView.setVisibility(View.INVISIBLE);
		}
	}

	public void sendMessage(int message, Object data) {
		Utils.sendMessage(m_oSensorDataUiUpdater, message, data);
	}
	
	public void dispatchMessage(Message msg) {
		m_oSensorDataUiUpdater.dispatchMessage(msg);
	}

	public void setVideoEnabled(boolean i_bVideoEnabled) {
		
		m_bVideoEnabled = i_bVideoEnabled;
		if (m_bVideoEnabled) {
			m_oRover.setVideoListener(this);
			m_oRover.startStreaming();
		} else {
			m_oRover.removeVideoListener(this);
			m_oRover.stopStreaming();
		}
		
		if (i_bVideoEnabled) {
			startVideo();
		} else {
			m_bVideoConnected = false;
			showVideoMsg("Video OFF");
		}
	}
	
	private void startVideo() {
		m_bVideoConnected = false;
		showVideoLoading(true);
		m_oSensorDataUiUpdater.postDelayed(m_oTimeoutRunnable, 15000);
	}
	
	private Runnable m_oTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			if (!m_bVideoConnected) {
				setVideoEnabled(false);
				showVideoLoading(false);
				showVideoMsg("Video Connection Failed");
			}
		}
	};
	
	private void showVideoMsg(String i_strMsg) {
		Bitmap bmp = Bitmap.createBitmap(m_layCamera.getWidth(), m_layCamera.getHeight(), Bitmap.Config.RGB_565);
		Utils.writeToCanvas(m_oActivity, new Canvas(bmp), i_strMsg, true);
		m_ivVideo.setImageBitmap(bmp);
	}

	public void onConnect() {
		setVideoEnabled(m_bVideoEnabled);
	}

	public boolean isVideoEnabled() {
		return m_bVideoEnabled;
	}

	public boolean isVideoScaled() {
		return m_bVideoScaled;
	}

	public void setVideoScaled(boolean i_bScaled) {
		m_bVideoScaled = i_bScaled;
		m_ivVideo.setScale(i_bScaled);
	}

	public void setResolution(final VideoResolution i_eResolution) {
		startVideo();
		m_oRover.setResolution(i_eResolution);
	}

}
