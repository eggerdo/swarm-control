package org.dobots.robots.parrot;

import java.net.InetAddress;

import org.dobots.utility.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ProgressBar;

public class ParrotVideoProcessor extends Thread {
	
	public interface OnConnectEvent {
		public void onConnect(boolean i_bConnected);
	}

	private static final String TAG = "Parrot Video";

    private final static int VIDEO_DATA_ID = 1;
    
    private Bitmap m_bmpVideo;
    private Handler mHandler;
    private boolean mRun;
    private boolean mPause = false;
	private boolean mClose = false;
    private boolean mVideoOpened;
    private SurfaceHolder m_oVideoSurface;
    
    private Activity m_oActivity;
    
    private OnConnectEvent m_oConnectListener;
    
    private boolean m_bVideoConnected = false;

    
	public ParrotVideoProcessor(Activity i_oActivity, SurfaceHolder i_oVideoSurface) {
		super("Parrot Video Processor");
		m_oActivity = i_oActivity;
		m_oVideoSurface = i_oVideoSurface;
		
		Rect videoSize = m_oVideoSurface.getSurfaceFrame();
//        m_bmpVideo = Bitmap.createBitmap(videoSize.width(), videoSize.height(), Bitmap.Config.RGB_565); //ARGB_8888
      m_bmpVideo = Bitmap.createBitmap(640, 360, Bitmap.Config.RGB_565); //ARGB_8888
       
	}

	public void setOnConnect(OnConnectEvent i_oListener) {
		m_oConnectListener = i_oListener;
	}
	
	public void connect() {

		mRun = false;
		
		String strVideoAddr = String.format("http://%s:%d", ParrotTypes.PARROT_IP, ParrotTypes.VIDEO_PORT);
        if (nativeOpenFromURL(strVideoAddr, ParrotTypes.VIDEO_CODEC) != 0)
        {
            nativeClose();
            Log.i(TAG, "nativeOpen() failed, throwing RuntimeException");
            m_oConnectListener.onConnect(false);
            return;
        }

        mVideoOpened = nativeOpenVideo(m_bmpVideo) == 0;
        if (!mVideoOpened) 
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
    		if (nativeDecodeFrame() == VIDEO_DATA_ID)
            {
    			if (!m_bVideoConnected) {
    		        m_oConnectListener.onConnect(true);
    			}
    			
                Canvas canvas = null;
                try
                {
                    try 
                    {
                        canvas = m_oVideoSurface.lockCanvas(null);
                        if (nativeUpdateBitmap() == 0) {
                        	canvas.drawBitmap(m_bmpVideo, 0, 0, null);
                        }
                    }
                    finally
                    {
                        if (canvas != null)
                        {
                        	m_oVideoSurface.unlockCanvasAndPost(canvas);
                        }
                    }
                }
                catch (Exception e)
                {
                	Log.e(TAG, "fatal error");
                }
            }
        }
        
        //once again synchronise nativeClose() with
        //nativeCloseVideo()/nativeOpenVideo() from setSurfaceSize()
        synchronized (m_oVideoSurface)
        {
            nativeClose();
        }
        
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
