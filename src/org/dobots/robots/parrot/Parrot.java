package org.dobots.robots.parrot;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.dobots.robots.MessageTypes;
import org.dobots.robots.RobotDevice;
import org.dobots.robots.parrot.ParrotTypes.ParrotMove;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.utility.Utils;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.ARDrone.VideoChannel;
import com.codeminders.ardrone.DroneStatusChangeListener;
import com.codeminders.ardrone.DroneVideoListener;
import com.codeminders.ardrone.NavDataListener;

public class Parrot implements RobotDevice, DroneStatusChangeListener {

	private static String TAG = "Parrot";
	
	private Handler mHandler = new Handler(Looper.getMainLooper());
	
	private Handler m_oRepeatMoveHandler = new Handler();
	private boolean m_bRepeat = false;
	
	private ARDrone m_oController;

	private Handler m_oUiHandler;

	private boolean m_bConnected = false;

	private int m_nWaitID;
	private Object receiveEvent = this;
	private boolean m_bMessageReceived = false;

	private double m_dblBaseSpeed = 40.0;
	
	private VideoChannel m_eVideoChannel = ARDrone.VideoChannel.HORIZONTAL_ONLY;
	
	public Parrot() throws UnknownHostException {
		m_oController = new ARDrone(InetAddress.getByAddress(ParrotTypes.ARDRONE_IP), 10000, 60000);
		
//		m_oReceiver = new ARDroneReceiver();
//		m_oReceiver.start();
		
//		m_oDroneStarter = new DroneStarter();
	}

	public void setHandler(Handler i_oHandler) {
		m_oUiHandler = i_oHandler;
	}
		
	@Override
	public RobotType getType() {
		return RobotType.RBT_ARDRONE;
	}

	@Override
	public String getAddress() {
		return ParrotTypes.ADDRESS;
	}

	@Override
	public void destroy() {
		try {
			m_oController.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setConnection() {
		// TODO Auto-generated method stub
	}
	
	private class DroneStarterOld extends AsyncTask<ARDrone, Integer, Boolean> {
		@Override
	    protected Boolean doInBackground(ARDrone... drones) {
	    		mHandler.post(new Runnable() {
	    			
	    			public void run() {
		    			try {
		    				m_oController.connect();
		    	            m_oController.clearEmergencySignal();
		    	            m_oController.waitForReady(ParrotTypes.CONNECTION_TIMEOUT);
		    	            m_oController.playLED(1, 10, 4);
		    		        m_oController.selectVideoChannel(m_eVideoChannel);
		    		        m_oController.setCombinedYawMode(true);

		    	            m_bConnected = true;
		    	            Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTED, null);
		    	        } catch (Exception e) {
		    	            try {
		    	                m_oController.clearEmergencySignal();
		    	                m_oController.clearImageListeners();
		    	                m_oController.clearNavDataListeners();
		    	                m_oController.clearStatusChangeListeners();
		    	                m_oController.disconnect();
		    	            } catch (Exception e1) {
		    	            }
		    	            
		    	            m_bConnected = false;
		    	            Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTERROR, null);
		    			}
	    			}
	    		});
	    		return true;
		}
	}
	
	private class DroneStarterNew extends AsyncTask<ARDrone, Integer, Boolean> {
	    
	    @Override
	    protected Boolean doInBackground(ARDrone... drones) {	  
	    	try{
	            m_oController.connect();
	            m_oController.clearEmergencySignal();
	            m_oController.waitForReady(ParrotTypes.CONNECTION_TIMEOUT);
	            m_oController.playLED(1, 10, 4);
	            m_oController.selectVideoChannel(ARDrone.VideoChannel.HORIZONTAL_ONLY);
	            m_oController.setCombinedYawMode(true);
	            return true;
	        } catch (Exception e) {
	            try {
	                m_oController.clearEmergencySignal();
	                m_oController.clearImageListeners();
	                m_oController.clearNavDataListeners();
	                m_oController.clearStatusChangeListeners();
	                m_oController.disconnect();
	            } catch (Exception e1) {
	            }
	          
	        }
	        return false;
	    }
	
	    protected void onPostExecute(Boolean success) {
	        if (success.booleanValue()) {
	        	m_bConnected = true;
	            Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTED, null);
	        } else {
	        	m_bConnected = false;
	            Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTERROR, null);
	        }
	    }
	}

	@Override
	public void connect() {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
			(new DroneStarterNew()).execute(m_oController); 
		} else {
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					(new DroneStarterOld()).execute(m_oController);
				}
			});
		}
	}
	
	public void setVideoListener(DroneVideoListener i_oListener) {
		m_oController.addImageListener(i_oListener);
	}
	
	public void clearVideoListener() {
		m_oController.clearImageListeners();
	}
	
	public void setNavDataListener(NavDataListener i_oListener) {
		m_oController.addNavDataListener(i_oListener);
	}
	
	public void clearNavDataListener() {
		m_oController.clearNavDataListeners();
	}
	
	@Override
	public void disconnect() {
		try {
			m_oController.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean isConnected() {
		return m_bConnected;
	}

	public void setVideoChannel(VideoChannel i_oChannel) {
		try {
			m_oController.selectVideoChannel(i_oChannel);
			m_eVideoChannel = i_oChannel;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void switchCamera() {
		switch (m_eVideoChannel) {
		case HORIZONTAL_ONLY:
			setVideoChannel(VideoChannel.VERTICAL_ONLY);
			break;
		case VERTICAL_ONLY:
			setVideoChannel(VideoChannel.HORIZONTAL_ONLY);
			break;
		}
	}
	
	public VideoChannel getVidoeChannel() {
		return m_eVideoChannel;
	}

	private void startMove(ParrotMove i_eMove, double i_dblSpeed, boolean i_bRepeat) {
		Runnable runner;
		switch (i_eMove) {
		case MOVE_BWD:
			runner = new MoveBackwardsRunnable(i_dblSpeed);
			break;
		case MOVE_FWD:
			runner = new MoveForwardsRunnable(i_dblSpeed);
			break;
		case MOVE_DOWN:
			runner = new DecreaseAltitudeRunnable(i_dblSpeed);
			break;
		case MOVE_UP:
			runner = new IncreaseAltitudeRunnable(i_dblSpeed);
			break;
		case MOVE_LEFT:
			runner = new MoveLeftRunnable(i_dblSpeed);
			break;
		case MOVE_RIGHT:
			runner = new MoveRightRunnable(i_dblSpeed);
			break;
		case ROTATE_LEFT:
			runner = new RotateCounterClockwiseRunnable(i_dblSpeed);
			break;
		case ROTATE_RIGHT:
			runner = new RotateClockwiseRunnable(i_dblSpeed);
			break;
		default:
			return;
		}
		m_bRepeat = i_bRepeat;
		m_oRepeatMoveHandler.post(runner);
	}
	
	private void stopRepeatedMove() {
		Log.d(TAG, "done");
		m_bRepeat = false;
		hover();
	}

    public void takeOff() {
        try {
        	m_oController.clearEmergencySignal();
        	m_oController.trim();
        	m_oController.takeOff();
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }
    
    public void land() {
        try {
        	m_oController.land();
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }
    
    // Increase Altitude
    
    public void increaseAltitude(double i_dblSpeed) {
    	startMove(ParrotMove.MOVE_UP, i_dblSpeed, true);
    }

	class IncreaseAltitudeRunnable implements Runnable {

		private double m_dblSpeed;

		public IncreaseAltitudeRunnable(double i_dblSpeed) {
			m_dblSpeed = i_dblSpeed;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "increase");
			doIncreaseAltitude(m_dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(new IncreaseAltitudeRunnable(m_dblSpeed), 100);
			}
		}
	}
	
	private void doIncreaseAltitude(double i_dblSpeed) {
		try {
			m_oController.move(0f, 0f, (float)i_dblSpeed / 100f, 0f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    // Decrease Altitude
    
    public void decreaseAltitude(double i_dblSpeed) {
    	startMove(ParrotMove.MOVE_DOWN, i_dblSpeed, true);
    }

	class DecreaseAltitudeRunnable implements Runnable {

		private double m_dblSpeed;

		public DecreaseAltitudeRunnable(double i_dblSpeed) {
			m_dblSpeed = i_dblSpeed;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "decrease");
			doDecreaseAltitude(m_dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(new DecreaseAltitudeRunnable(m_dblSpeed), 100);
			}
		}
	};
	
    public void doDecreaseAltitude(double i_dblSpeed) {
    	try {
			m_oController.move(0f, 0f, -(float)i_dblSpeed / 100f, 0f);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    // Hover
    
    public void hover() {
    	try {
			m_oController.hover();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	@Override
	public void enableControl(boolean i_bEnable) {
//		m_oController.control(i_bEnable);
	}

	private double capSpeed(double io_dblSpeed) {
		// if a negative value was provided as speed
		// use the absolute value of it.
		io_dblSpeed = Math.abs(io_dblSpeed);
		io_dblSpeed = Math.min(io_dblSpeed, 100);
		io_dblSpeed = Math.max(io_dblSpeed, 0);
		
		return io_dblSpeed;
	}
	
	private int capRadius(int io_nRadius) {
//		io_nRadius = Math.min(io_nRadius, DottyTypes.MAX_RADIUS);
//		io_nRadius = Math.max(io_nRadius, -DottyTypes.MAX_RADIUS);

		return io_nRadius;
	}

    // Move Forward
    
	@Override
	public void moveForward(double i_dblSpeed) {
		startMove(ParrotMove.MOVE_FWD, i_dblSpeed, true);
	}
	
	class MoveForwardsRunnable implements Runnable {

		private double m_dblSpeed;

		public MoveForwardsRunnable(double i_dblSpeed) {
			m_dblSpeed = i_dblSpeed;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "move fwd");
			doMoveForward(m_dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(new MoveForwardsRunnable(m_dblSpeed), 100);
			}
		}
	}

	public void doMoveForward(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		try {
			m_oController.move(0f, -(float)i_dblSpeed / 100f, 0f, 0f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		i_nRadius = capRadius(i_nRadius);
	}

	// Move Backward
	
	@Override
	public void moveBackward(double i_dblSpeed) {
		startMove(ParrotMove.MOVE_BWD, i_dblSpeed, true);
	}

	class MoveBackwardsRunnable implements Runnable {

		private double m_dblSpeed;

		public MoveBackwardsRunnable(double i_dblSpeed) {
			m_dblSpeed = i_dblSpeed;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "move bwd");
			doMoveBackward(m_dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(new MoveBackwardsRunnable(m_dblSpeed), 100);
			}
		}
	}
	
	public void doMoveBackward(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		try {
			m_oController.move(0f, (float)i_dblSpeed / 100f, 0f, 0f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		i_nRadius = capRadius(i_nRadius);

	}
	
	// Move Left

	public void moveLeft(double i_dblSpeed) {
		startMove(ParrotMove.MOVE_LEFT, i_dblSpeed, true);
	}
	
	class MoveLeftRunnable implements Runnable {

		private double m_dblSpeed;

		public MoveLeftRunnable(double i_dblSpeed) {
			m_dblSpeed = i_dblSpeed;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "move left");
			doMoveLeft(m_dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(new MoveLeftRunnable(m_dblSpeed), 100);
			}
		}
	}

	public void doMoveLeft(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		try {
			m_oController.move(-(float)i_dblSpeed / 100f, 0f, 0f, 0f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Move Right

	public void moveRight(double i_dblSpeed) {
		startMove(ParrotMove.MOVE_RIGHT, i_dblSpeed, true);
	}
	
	class MoveRightRunnable implements Runnable {

		private double m_dblSpeed;

		public MoveRightRunnable(double i_dblSpeed) {
			m_dblSpeed = i_dblSpeed;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "move right");
			doMoveRight(m_dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(new MoveRightRunnable(m_dblSpeed), 100);
			}
		}
	}

	public void doMoveRight(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);
		try {
			m_oController.move((float)i_dblSpeed / 100f, 0f, 0f, 0f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Rotate Right / Clockwise

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		startMove(ParrotMove.ROTATE_RIGHT, i_dblSpeed, true);
	}

	class RotateClockwiseRunnable implements Runnable {

		private double m_dblSpeed;

		public RotateClockwiseRunnable(double i_dblSpeed) {
			m_dblSpeed = i_dblSpeed;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "rotate right");
			doRrotateClockwise(m_dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(new RotateClockwiseRunnable(m_dblSpeed), 100);
			}
		}
	}

	public void doRrotateClockwise(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);

		try {
			m_oController.move(0, 0, 0, (float)i_dblSpeed / 100f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Rotate Left / Counterclockwise

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		startMove(ParrotMove.ROTATE_LEFT, i_dblSpeed, true);
	}

	class RotateCounterClockwiseRunnable implements Runnable {

		private double m_dblSpeed;

		public RotateCounterClockwiseRunnable(double i_dblSpeed) {
			m_dblSpeed = i_dblSpeed;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "rotate left");
			doRotateCounterClockwise(m_dblSpeed);
			if (m_bRepeat) {
				m_oRepeatMoveHandler.postDelayed(new RotateCounterClockwiseRunnable(m_dblSpeed), 100);
			}
		}
	}

	public void doRotateCounterClockwise(double i_dblSpeed) {
		i_dblSpeed = capSpeed(i_dblSpeed);

		try {
			m_oController.move(0, 0, 0, -(float)i_dblSpeed / 100f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Move Stop

	@Override
	public void moveStop() {
		stopRepeatedMove();
	}
	
	// Execute Circle

	@Override
	public void executeCircle(double i_nTime, double i_nSpeed) {
		// TODO Auto-generated method stub
	}

	@Override
	public void moveForward() {
		moveForward(m_dblBaseSpeed);
	}

	@Override
	public void moveBackward() {
		moveBackward(m_dblBaseSpeed);
	}
	
	public void moveLeft() {
		moveLeft(m_dblBaseSpeed);
	}
	
	public void moveRight() {
		moveRight(m_dblBaseSpeed);
	}

	@Override
	public void rotateCounterClockwise() {
		rotateCounterClockwise(m_dblBaseSpeed);
	}

	@Override
	public void rotateClockwise() {
		rotateClockwise(m_dblBaseSpeed);
	}
	
	public void increaseAltitude() {
		increaseAltitude(m_dblBaseSpeed);
	}
	
	public void decreaseAltitude() {
		decreaseAltitude(m_dblBaseSpeed);
	}

	@Override
	public void setBaseSpeed(double i_dblSpeed) {
		m_dblBaseSpeed = i_dblSpeed;
	}

	@Override
	public double getBaseSped() {
		return m_dblBaseSpeed;
	}

	@Override
	public void ready() {
		m_bConnected = true;
	}

}
