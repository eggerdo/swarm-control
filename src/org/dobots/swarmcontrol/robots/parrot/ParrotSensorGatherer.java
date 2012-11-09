package org.dobots.swarmcontrol.robots.parrot;

import org.dobots.robots.parrot.Parrot;
import org.dobots.robots.parrot.ParrotTypes;
import org.dobots.robots.parrot.ParrotVideoProcessor;
import org.dobots.swarmcontrol.BaseActivity;
import org.dobots.swarmcontrol.ConnectListener;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.SensorGatherer;
import org.dobots.utility.Utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codeminders.ardrone.DroneVideoListener;
import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavDataListener;

public class ParrotSensorGatherer extends SensorGatherer implements NavDataListener, DroneVideoListener, ConnectListener {

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

	private ImageView m_ivVideo;
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
		
		((FrameLayout)m_oActivity.findViewById(R.id.layParrot_Video)).getLayoutParams().height = ParrotTypes.VIDEO_HEIGHT;

        m_ivVideo = (ImageView) m_oActivity.findViewById(R.id.ivParrot_Video);
		m_ivVideo.getLayoutParams().height = ParrotTypes.VIDEO_HEIGHT;
		m_ivVideo.getLayoutParams().width = ParrotTypes.VIDEO_WIDTH;
		
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

	private void setText(TextView i_oView, String i_strValue) {
		i_oView.setText(i_strValue);
	}
	
	private void setText(TextView i_oView, int i_nValue) {
		i_oView.setText(String.valueOf(i_nValue));
	}

	private void setText(TextView i_oView, float i_fValue) {
		i_oView.setText(String.valueOf(i_fValue));
	}

	public void enableSensors(boolean i_bEnabled) {
		m_bSensorsEnabled = i_bEnabled;
		showSensors(i_bEnabled);
	}
	
	public void showSensors(boolean i_bShow) {
		showView(laySensors, i_bShow);
	}
	
	private void showView(View v, boolean i_bShow) {
		if (i_bShow) {
			v.setVisibility(View.VISIBLE);
		} else {
			v.setVisibility(View.GONE);
		}
	}

	@Override
    public void frameReceived(final int startX, final int startY, final int w, final int h, final
            int[] rgbArray, final int offset, final int scansize) {
		
		if (m_bVideoEnabled) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {

					if (!m_bVideoConnected) {
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
            b.setDensity(100);
            if (m_bVideoScaled) {
            	w = ParrotTypes.VIDEO_WIDTH;
            	h = ParrotTypes.VIDEO_HEIGHT;
            	b = Bitmap.createScaledBitmap(b, w, h, false);
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void param) {
        	Bitmap oldBitmap = m_ivVideo.getDrawingCache();
        	if (oldBitmap != null) {
        		oldBitmap.recycle();
        	}
            m_ivVideo.setImageBitmap(b);
            m_ivVideo.getLayoutParams().width = w;
            m_ivVideo.getLayoutParams().height = h;
        }
    }

	public void onConnect() {
		m_oParrot.setNavDataListener(this);
		setVideoEnabled(m_bVideoEnabled);
	}

	public void onDisconnect() {
		m_oParrot.removeNavDataListener(this);
	}

	public void close() {
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
	public void resumeThread() {
		super.resumeThread();
		if (m_oVideoProcessor != null) {
			m_oVideoProcessor.resumeThread();
		}
	}
	
	private void showVideoLoading(final boolean i_bShow) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				m_bVideoConnected = !i_bShow;
				showView(m_ivVideo, !i_bShow);
				showView(m_pbLoading, i_bShow);
			}
		});
	}

	public void setVideoEnabled(boolean i_bVideoEnabled) {
		
		m_bVideoEnabled = i_bVideoEnabled;
		
		if (i_bVideoEnabled) {
			showVideoLoading(true);
		} else {
			Utils.writeToImageView(m_oActivity, m_ivVideo, "Video OFF", true);
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
	public void onConnect(boolean i_bConnected) {
		showVideoLoading(false);
		
		if (!i_bConnected) {
			m_oUiHandler.post(new Runnable() {
				
				@Override
				public void run() {
					Utils.writeToImageView(m_oActivity, m_ivVideo, "Video Connection Failed", true);
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
	}

}
