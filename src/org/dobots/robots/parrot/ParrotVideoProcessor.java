package org.dobots.robots.parrot;

import org.dobots.swarmcontrol.BaseActivity;
import org.dobots.swarmcontrol.ConnectListener;
import org.dobots.utility.Utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

public class ParrotVideoProcessor extends Thread {

	private static final String TAG = "Parrot Video";

    private Bitmap m_bmpVideo;
    private boolean mRun = false;
    private boolean mPause = false;
	private boolean mClose = false;

    private ImageView m_oImage;
    
    private BaseActivity m_oActivity;
    
    private ConnectListener m_oConnectListener;
    
    private boolean m_bVideoConnected = false;
    
	private Handler m_oUiHandler = new Handler(Looper.getMainLooper());
	
	public ParrotVideoProcessor(BaseActivity i_oActivity, ImageView i_oImage) {
		super("Parrot Video Processor");
		m_oActivity = i_oActivity;
		m_oImage = i_oImage;
		
		m_bmpVideo = Bitmap.createBitmap(ParrotTypes.VIDEO_WIDTH, ParrotTypes.VIDEO_HEIGHT, Bitmap.Config.RGB_565); //ARGB_8888
		m_oImage.getLayoutParams().height = ParrotTypes.VIDEO_HEIGHT;
		m_oImage.getLayoutParams().width = ParrotTypes.VIDEO_WIDTH;
		m_oImage.setImageBitmap(m_bmpVideo);
	}

	public void setOnConnect(ConnectListener i_oListener) {
		m_oConnectListener = i_oListener;
	}
	
	public void connect() {

		mRun = false;
		
		String strVideoAddr = String.format("http://%s:%d", ParrotTypes.PARROT_IP, ParrotTypes.VIDEO_PORT);
		
        if (nativeOpenFromURL(strVideoAddr, ParrotTypes.VIDEO_CODEC) != ParrotTypes.SUCCESS)
        {
            nativeClose();
            Log.i(TAG, "nativeOpen() failed, throwing RuntimeException");
            m_oConnectListener.onConnect(false);
            return;
        }

        if (nativeOpenVideo(m_bmpVideo) != ParrotTypes.SUCCESS) 
        {
            nativeCloseVideo();
	        Log.i(TAG, "unable to open a stream, throwing RuntimeException");
	        m_oConnectListener.onConnect(false);
            return;
        }

        mRun = true;

        start();
	}
	
	public void run() {
        Log.d(TAG, "entering run()");
        
        // give the ffmpeg library some time to settle before
        // starting to decode the frames
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        while (mRun)
        {
        	if (mPause) {
        		try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		continue;
        	}
        	
        	int nResult = nativeDecodeFrame();
    		if (nResult == ParrotTypes.SUCCESS)
            {
    			if (!m_bVideoConnected) {
    		        m_oConnectListener.onConnect(true);
    			}

    			if (nativeUpdateBitmap() == ParrotTypes.SUCCESS) {
    				m_oUiHandler.post(new Runnable() {
						
						@Override
						public void run() {
							// image must be updated by GUI thread (main thread)
							m_oImage.invalidate();
						}
					});
    			}
            } else if (nResult == ParrotTypes.READ_FRAME_FAILED) {
            	mRun = false;
            	m_oConnectListener.onConnect(false);
            }
        }
        
        // close video
        nativeClose();
        
        mClose = true;

        Log.d(TAG, "leaving run()");
	}
	
	public void close() {
        mRun = false;
        while (!mClose) {
        	Utils.waitSomeTime(20);
        }
	}
	
	public void pauseThread() {
		mPause = true;
	}
	
	public void resumeThread() {
		mPause = false;
	}
	
	static {
    	System.loadLibrary("ffmpeg");
    	System.loadLibrary("avjni");
	}

    //native methods are described in jni/avjni.c
    private native int nativeOpenFromURL(String url, String format);
    private native void nativeClose();
    private native int nativeOpenVideo(Object aBitmapRef);
    private native void nativeCloseVideo();
    private native int nativeDecodeFrame(); //never touch the bitmap here
    private native int nativeUpdateBitmap();

}
