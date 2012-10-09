package org.dobots.swarmcontrol.robots.parrot;

import org.dobots.robots.parrot.Parrot;
import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.robots.SensorGatherer;
import org.dobots.utility.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codeminders.ardrone.DroneVideoListener;
import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavDataListener;

public class ParrotSensorGatherer extends SensorGatherer implements NavDataListener, DroneVideoListener {

	private Parrot m_oARDrone;

	private Handler mHandler = new Handler(Looper.getMainLooper());

	private boolean m_bSensorsEnabled;
	private boolean m_bVidoeEnabled;
	
	TextView txtControlState;
	TextView txtBattery;
	TextView txtAltitude;
	TextView txtPitch;
	TextView txtRoll;
	TextView txtYaw;
	TextView txtVX;
	TextView txtVY;
	TextView txtVZ;

	private ImageView m_ivVideo;
	
	LinearLayout laySensors;
		
	public ParrotSensorGatherer(Activity i_oActivity, Parrot i_oARDrone) {
		super(i_oActivity);
		m_oARDrone = i_oARDrone;
		
		setProperties();
		
		initialize();
	}
	
	public void setProperties() {
		txtControlState = (TextView) m_oActivity.findViewById(R.id.txtControlState);
		txtBattery = (TextView) m_oActivity.findViewById(R.id.txtBattery);
		txtAltitude = (TextView) m_oActivity.findViewById(R.id.txtAltitude);
		txtPitch = (TextView) m_oActivity.findViewById(R.id.txtPitch);
		txtRoll = (TextView) m_oActivity.findViewById(R.id.txtRoll);
		txtYaw = (TextView) m_oActivity.findViewById(R.id.txtYaw);
		txtVX = (TextView) m_oActivity.findViewById(R.id.txtVX);
		txtVY = (TextView) m_oActivity.findViewById(R.id.txtVY);
		txtVZ = (TextView) m_oActivity.findViewById(R.id.txtVZ);

        m_ivVideo = (ImageView) m_oActivity.findViewById(R.id.ivARDrone_Video);
        
		laySensors = (LinearLayout) m_oActivity.findViewById(R.id.laySensors);
	}

	public void initialize() {
		m_bSensorsEnabled = false;
		m_bVidoeEnabled = true;
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
					setText(txtControlState, nd.getControlState().toString());
					setText(txtBattery, nd.getBattery());
					setText(txtAltitude, nd.getAltitude());
					setText(txtPitch, nd.getPitch());
					setText(txtRoll, nd.getRoll());
					setText(txtYaw, nd.getYaw());
					setText(txtVX, nd.getVx());
					setText(txtVY, nd.getLongitude());
					setText(txtVZ, nd.getVz());
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
		showLayout(laySensors, i_bShow);
	}
	
	private void showLayout(View v, boolean i_bShow) {
		if (i_bShow) {
			v.setVisibility(View.VISIBLE);
		} else {
			v.setVisibility(View.GONE);
		}
	}

	@Override
    public void frameReceived(final int startX, final int startY, final int w, final int h, final
            int[] rgbArray, final int offset, final int scansize) {
		if (m_bVidoeEnabled) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
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
            return null;
        }
        
        @Override
        protected void onPostExecute(Void param) {;
            ((BitmapDrawable)m_ivVideo.getDrawable()).getBitmap().recycle(); 
            m_ivVideo.setImageBitmap(b);
        }
    }

}
