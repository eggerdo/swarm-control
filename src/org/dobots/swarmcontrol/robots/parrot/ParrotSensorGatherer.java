package org.dobots.swarmcontrol.robots.parrot;

import org.dobots.robots.parrot.Parrot;
import org.dobots.robots.parrot.ParrotTypes;
import org.dobots.robots.parrot.ParrotVideoProcessor;
import org.dobots.swarmcontrol.BaseActivity;
import org.dobots.swarmcontrol.IConnectListener;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.SensorGatherer;
import org.dobots.utility.ScalableImageView;
import org.dobots.utility.Utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codeminders.ardrone.DroneVideoListener;
import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavDataListener;

public class ParrotSensorGatherer extends SensorGatherer implements NavDataListener, DroneVideoListener, IConnectListener {

	private Parrot m_oParrot;

	private ParrotVideoProcessor m_oVideoProcessor = null;

	private Handler mHandler = new Handler(Looper.getMainLooper());

	private boolean m_bSensorsEnabled = false;
	private boolean m_bVideoEnabled = true;
	private boolean m_bVideoConnected = false;
	private boolean m_bVideoScaled = false;
	
	private TextView m_txtControlState;
	private TextView m_txtBattery;
	private TextView m_txtAltitude;
	private TextView m_txtPitch;
	private TextView m_txtRoll;
	private TextView m_txtYaw;
	private TextView m_txtVX;
	private TextView m_txtVY;
	private TextView m_txtVZ;

	private FrameLayout m_layVideo;

	private ScalableImageView m_ivVideo;
	private ProgressBar m_pbLoading;
	
	LinearLayout laySensors;
	
	public ParrotSensorGatherer(BaseActivity i_oActivity, Parrot i_oARDrone) {
		super(i_oActivity);
		m_oParrot = i_oARDrone;
		
		setProperties();
		
		initialize();
	}
	
	public void setProperties() {
		m_txtControlState = (TextView) m_oActivity.findViewById(R.id.txtControlState);
		m_txtBattery = (TextView) m_oActivity.findViewById(R.id.txtBattery);
		m_txtAltitude = (TextView) m_oActivity.findViewById(R.id.txtAltitude);
		m_txtPitch = (TextView) m_oActivity.findViewById(R.id.txtPitch);
		m_txtRoll = (TextView) m_oActivity.findViewById(R.id.txtRoll);
		m_txtYaw = (TextView) m_oActivity.findViewById(R.id.txtYaw);
		m_txtVX = (TextView) m_oActivity.findViewById(R.id.txtVX);
		m_txtVY = (TextView) m_oActivity.findViewById(R.id.txtVY);
		m_txtVZ = (TextView) m_oActivity.findViewById(R.id.txtVZ);
		
		m_layVideo = (FrameLayout)m_oActivity.findViewById(R.id.layParrot_Video);

        m_ivVideo = (ScalableImageView) m_oActivity.findViewById(R.id.ivParrot_Video);
        m_ivVideo.setMaxWidth(ParrotTypes.VIDEO_WIDTH);
		
        m_pbLoading = (ProgressBar) m_oActivity.findViewById(R.id.pbLoading);
        
		laySensors = (LinearLayout) m_oActivity.findViewById(R.id.laySensors);
	}

	public void initialize() {
		m_bSensorsEnabled = false;
	}
	
	protected void execute() {
		// for the AR Drone we don't have to poll for sensor, it is
		// pushing them by itself and notifying the listeners if
		// data is available, thus we only need to update the sensor
		// values in the navDataReceived event.
		Utils.waitSomeTime(500);
	}

	@Override
	public void navDataReceived(final NavData nd) {
		if (m_bSensorsEnabled) {
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					setText(m_txtControlState, nd.getControlState().toString());
					setText(m_txtBattery, nd.getBattery());
					setText(m_txtAltitude, nd.getAltitude());
					setText(m_txtPitch, nd.getPitch());
					setText(m_txtRoll, nd.getRoll());
					setText(m_txtYaw, nd.getYaw());
					setText(m_txtVX, nd.getVx());
					setText(m_txtVY, nd.getLongitude());
					setText(m_txtVZ, nd.getVz());
				}
			});
		}
	}

	public void enableSensors(boolean i_bEnabled) {
		m_bSensorsEnabled = i_bEnabled;
		showSensors(i_bEnabled);
	}
	
	public void showSensors(boolean i_bShow) {
		Utils.showView(laySensors, i_bShow);
	}
	
	@Override
    public void frameReceived(final int startX, final int startY, final int w, final int h, final
            int[] rgbArray, final int offset, final int scansize) {
		
		if (m_bVideoEnabled) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {

					if (!m_bVideoConnected) {
						m_bVideoConnected = true;
						showVideoLoading(false);
					}
					
					(new VideoDisplayer(startX, startY, w, h, rgbArray, offset, scansize)).execute();
				}
			});
		}
	}
	
	private class VideoDisplayer extends AsyncTask<Void, Integer, Void> {
        
        public Bitmap b;
        public int[]rgbArray;
        public int offset;
        public int scansize;
        public int w;
        public int h;
        
        public VideoDisplayer(int x, int y, int width, int height, int[] arr, int off, int scan) {
            // do stuff
            rgbArray = arr;
            offset = off;
            scansize = scan;
            w = width;
            h = height;
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            b =  Bitmap.createBitmap(rgbArray, offset, scansize, w, h, Bitmap.Config.RGB_565);
            return null;
        }
        
        @Override
        protected void onPostExecute(Void param) {
        	Bitmap oldBitmap = m_ivVideo.getDrawingCache();
        	if (oldBitmap != null) {
        		oldBitmap.recycle();
        	}
            m_ivVideo.setImageBitmap(b);
        }
    }

	// call when robot connected
	public void onConnect() {
		m_oParrot.setNavDataListener(this);
		setVideoEnabled(m_bVideoEnabled);
	}

	// call when robot disconnected
	public void onDisconnect() {
		m_oParrot.removeNavDataListener(this);
	}

	@Override
	public void stopThread() {
		super.stopThread();
		disconnectVideo();
	}

	public void disconnectVideo() {
		if (m_oVideoProcessor != null) {
			m_oVideoProcessor.close();
			m_oVideoProcessor = null;
		}
	}

	@Override
	public void pauseThread() {
		super.pauseThread();
		if (m_oVideoProcessor != null) {
			m_oVideoProcessor.pauseThread();
		}
	}

	@Override
	public void startThread() {
		super.startThread();
		if (m_oVideoProcessor != null) {
			m_oVideoProcessor.resumeThread();
		}
	}
	
	private void showVideoLoading(final boolean i_bShow) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Utils.showView(m_ivVideo, !i_bShow);
				Utils.showView(m_pbLoading, i_bShow);
			}
		});
	}

	private void showVideoMsg(String i_strMsg) {
		Bitmap bmp = Bitmap.createBitmap(m_layVideo.getWidth(), m_layVideo.getHeight(), Bitmap.Config.RGB_565);
		Utils.writeToCanvas(m_oActivity, new Canvas(bmp), i_strMsg, true);
		m_ivVideo.setImageBitmap(bmp);
	}

	private void startVideo() {
		m_bVideoConnected = false;
		showVideoLoading(true);
		mHandler.postDelayed(new Timeout(), 15000);
	}
	
	private class Timeout implements Runnable {
		@Override
		public void run() {
			if (!m_bVideoConnected) {
				setVideoEnabled(false);
				showVideoLoading(false);
				showVideoMsg("Video Connection Failed");
			}
		}
	}

	public void setVideoEnabled(boolean i_bVideoEnabled) {
		
		m_bVideoEnabled = i_bVideoEnabled;
		
		if (i_bVideoEnabled) {
			startVideo();
		} else {
			m_bVideoConnected = false;
			showVideoMsg("Video OFF");
		}
		
		if (m_oParrot.isARDrone1()) {

			if (i_bVideoEnabled) {
				m_oParrot.setVideoListener(this);
			} else {
				m_oParrot.removeVideoListener(this);
			}
				
		} else {

			if (i_bVideoEnabled) {

				m_oVideoProcessor = new ParrotVideoProcessor(m_oActivity, m_ivVideo);
				m_oVideoProcessor.setOnConnect(this);
				m_oVideoProcessor.connect();
				
			} else {
				if (m_oVideoProcessor != null) {
					m_oVideoProcessor.close();
					m_oVideoProcessor = null;
				}
			}
		}
	}

	@Override
	// called by video processor when connection established or failed
	public void onConnect(boolean i_bConnected) {
		m_bVideoConnected = i_bConnected;
		showVideoLoading(false);
		
		if (!i_bConnected) {
			m_oUiHandler.post(new Runnable() {
				
				@Override
				public void run() {
					showVideoMsg("Video Connection Failed");
				}
			});
		}
		
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

}
