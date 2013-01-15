package org.dobots.robots.ac13;

// This Code is based on Anne van Rossum's code (RoverOpen) with some additions
// taken from Ucetra's library (which can be found at 
// https://sourceforge.net/projects/ac13javalibrary/)

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dobots.robots.ac13.AC13RoverTypes.AC13RoverParameters;
import org.dobots.utility.log.Loggable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AC13Controller extends Loggable {
	
	public static final String TAG = "AC13Ctrl";
	
	// TCP/IP sockets
	private Socket cSock;
	private Socket vSock;

	// Flags to store state of the video
	boolean streaming = false;
	// receives new frame events
	private IAC13VideoListener oVideoListener = null;

	// Infrared on/off
	boolean infrared;

	// Flags to store state of the connection
	boolean connected;

	// The maximal image buffer will be sufficient for hi-res images, notice
	// that jpeg images do not have a default image size. The tcp buffer is
	// large enough for the packages send by the Rover, but the latter tends
	// to chop images across multiple TCP chunks so it is not large enough
	// for one image.
	int maxTCPBuffer = 2048;
	int maxImageBuffer = 131072;
	byte[] imageBuffer = new byte[maxImageBuffer];
	int imagePtr = 0;
	int tcpPtr = 0;

	private AC13RoverParameters parameters;
	
	public AC13Controller() {
		parameters = new AC13RoverTypes().new AC13RoverParameters();
	}

	public void setVideoListener(IAC13VideoListener listener) {
		this.oVideoListener = listener;
	}
	
	public void removeVideoListener(IAC13VideoListener listener) {
		if (this.oVideoListener == listener) {
			this.oVideoListener = null;
		}
	}

	public boolean startStreaming() {
		if (!streaming) {
			streaming = true;
			Thread vThread = new Thread(new VideoThread());
			vThread.start();
			return true;
		} else {
			return false;
		}
	}
	
	public boolean connect() {

		try {
			debug(TAG, "connecting...");
			
			//Initializing command socket
			SocketAddress sockaddr = new InetSocketAddress(AC13RoverTypes.ADDRESS, AC13RoverTypes.PORT);
			cSock = new Socket();
			cSock.connect(sockaddr, 10000);
				
			//Setting the connection
			writeStart();
			receiveAnswer(0);
			
			cSock.close();
			
			//Reinitializing the command socket
			cSock = new Socket();
			cSock.connect(sockaddr, 10000);
			
			byte[] buffer = new byte[2048];
			
			for (int i = 1; i < 4; i++) {
				
				writeCmd(i, null);
				buffer = receiveAnswer(i);
			}
			
			byte[] imgid = new byte[4];
			
			for (int i = 0; i < 4; i++)
				imgid[i] = buffer[i + 25];

			vSock = new Socket();
			vSock.connect(sockaddr, 10000);
			writeCmd(4, imgid);

			requestAllParameters();
			
			startStreaming();
			
			connected = true;
			
		} catch (Exception e) {
			 return false;	
		}
		
		return true;
	}

	public boolean disconnect(){
		
		try {
		    streaming = false;
		    
		    if(infrared)
		    	switchInfrared();
		    
			cSock.close();
			vSock.close();
			
			connected = false;
			
		} catch (Exception e) {
			return false;
		}
		
		return true;	
	}

	public void keepAlive() {
		writeCmd(1, null);
	}

	public void switchInfrared() {
		
		if (infrared)
			writeCmd(11,null);
		else
			writeCmd(10,null);

		infrared = !infrared;
	}

	public void stopStreaming() {
		streaming = false;
	}

	// call is nonblocking. returns directly and doesn't wait for an answer
	public void switchTo640X480Resolution(){
		new Thread(new ResolutionCommandRunnable(32)).start();	
	}

	// call is made blocking until answer received
	public boolean setResolution640x480() {
		ResolutionCommandRunnable oRunner = new ResolutionCommandRunnable(32);
		oRunner.run();
		return oRunner.success;
	}
	
	// call is made blocking until answer received
	public void switchTo320X240Resolution(){
		new Thread(new ResolutionCommandRunnable(8)).start();
	}
	
	public boolean setResolution320x240() {
		ResolutionCommandRunnable oRunner = new ResolutionCommandRunnable(8);
		oRunner.run();
		return oRunner.success;
	}

	private class ResolutionCommandRunnable implements Runnable {
		
		int command;
		public boolean success = false;
		
		public ResolutionCommandRunnable(int command) {
			this.command = command;
		}
		
		public void run() {
			try {
		         HttpClient mClient= new DefaultHttpClient();
		         HttpGet get = new HttpGet("http://192.168.1.100/set_params.cgi?resolution=" + command);
		         get.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("AC13", "AC13"), "UTF-8", false));
		         
		         mClient.execute(get);
		         HttpResponse response = mClient.execute(get);
		        
		         BufferedReader rd = new BufferedReader(new InputStreamReader(
							response.getEntity().getContent()));
		         String line = "";
		         while ((line = rd.readLine()) != null) {
		        	 info(TAG, "RESOLUTION COMMAND: " + line + " " + command);
		        	 
		        	 if (line.startsWith("ok")) {
		        		 success = true;
		        	 }
		         }
			} catch (Exception e) {
			
				 error(TAG, "Resolution Command Error "+  e.toString());
			}
		}
	}

	public boolean isConnected(){
		return connected;
	}
	
	public boolean isInfraredEnabled() {
		return infrared;
	}
	
	public boolean isStreaming(){
		return streaming;	
	}
	
	// STOP

	public void moveStop(){
		moveForward((byte)0);
	}
	
	// FORWARD
	
	public void moveForward(int velocity){
		moveLeftForward(velocity);
		moveRightForward(velocity);
	}
	
	public void moveForward(int leftVelocity, int rightVelocity) {
		moveLeftForward(leftVelocity);
		moveRightForward(rightVelocity);
	}
	
	public void moveLeftForward(int velocity){
		writeCmd(5, new byte[] {(byte) velocity});	
	}
	
	public void moveRightForward(int velocity){
		writeCmd(7, new byte[] {(byte) velocity});
	}
	
	// BACKWARD
	
	public void moveBackward(int velocity){
		moveLeftBackward(velocity);
		moveRightBackward(velocity);
	}
	
	public void moveBackward(int leftVelocity, int rightVelocity) {
		moveLeftBackward(leftVelocity);
		moveRightBackward(rightVelocity);
	}
	
	public void moveLeftBackward(int velocity){
		writeCmd(6, new byte[] {(byte) velocity});
	}
	
	public void moveRightBackward(int velocity){
		writeCmd(8, new byte[] {(byte) velocity});	
	}
	
	// LEFT
		
    public void rotateLeft(int velocity){
    	moveLeftForward(velocity);
    	moveRightBackward(velocity);
	}
    
    // RIGHT
    
	public void rotateRight(int velocity){
		moveRightForward(velocity);
		moveLeftBackward(velocity);
	}
	
	/**
	 * Separate thread to handle TCP/IP data stream. The result is stored in
	 * image1 and image2. It is communicated to the original activity via the
	 * handler.
	 */
	private class VideoThread implements Runnable {
		public void run() {
			try {
				while (connected && streaming) {
					receiveImage();
				}

			} catch (Exception e) {
				error(TAG, "Socket read error", e);
			}
		}

	}
	
	private void writeStart() {
		try {
			debug(TAG, "HTTP GET cmd (authentication)");
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(cSock.getOutputStream())), true);
			out.println("GET /check_user.cgi?user=AC13&pwd=AC13 HTTP/1.1\r\nHost: 192.168.1.100:80\r\nUser-Agent: WifiCar/1.0 CFNetwork/485.12.7 Darwin/10.4.0\r\nAccept: */*\r\nAccept-Language: en-us\r\nAccept-Encoding: gzip, deflate\r\nConnection: keep-alive\r\n\r\n!");
		} catch (Exception e) {
			error(TAG, "S: Error", e);
			e.printStackTrace();
		}
	}

	private byte[] receiveAnswer(int i) {
		byte[] buffer = new byte[2048];
		try {
			int len = cSock.getInputStream().read(buffer, 0, 2048);
			if (len > 0) {
				String str = new String(buffer, 0, len);
				debug(TAG, String.format("Read i=%d, str=%s", i, str));
			}
		} catch (Exception eg) {
			error(TAG, "General: input stream error", eg);
			eg.printStackTrace();
		}
		return buffer;
	}

	private boolean imgStart(byte[] start) {
		return (start[0] == 'M' && start[1] == 'O' && start[2] == '_' && start[3] == 'V');
	}

	private void receiveImage() {
		debug("ReceiveImage", "Get image");
		try {
			int len = 0;
			int newPtr = tcpPtr;
			int imageLength = 0;
			boolean fnew = false;
			while (!fnew && newPtr < maxImageBuffer - maxTCPBuffer) {
				len = vSock.getInputStream().read(imageBuffer, newPtr,
						maxTCPBuffer);
				// todo: check if this happens too often and exit
				if (len <= 0) {
					connected = false;
				}

				byte[] f4 = new byte[4];
				for (int i = 0; i < 4; i++)
					f4[i] = imageBuffer[newPtr + i];
				if (imgStart(f4) && (imageLength > 0))
					fnew = true;
				if (!fnew) {
					newPtr += len;
					imageLength = newPtr - imagePtr;
				} else {
					debug(TAG, "Total image size is "
							+ (imageLength - 36));

					Bitmap rawmap = BitmapFactory.decodeByteArray(
							imageBuffer, imagePtr + 36, imageLength - 36);
					if (rawmap != null) {
						if (oVideoListener != null) {
							oVideoListener.frameReceived(rawmap);
						}
					}
					if (newPtr > maxImageBuffer / 2) {
						// copy first chunk of new arrived image to start of
						// array
						for (int i = 0; i < len; i++)
							imageBuffer[i] = imageBuffer[newPtr + i];
						imagePtr = 0;
						tcpPtr = len;
					} else {
						imagePtr = newPtr;
						tcpPtr = newPtr + len;
					}
					debug("Var", "imagePtr =" + imagePtr);
					debug("Var", "tcpPtr =" + tcpPtr);
					debug("Var", "imageLength =" + imageLength);
					debug("Var", "newPtr =" + newPtr);
					debug("Var", "len =" + len);
				}
			}
			// reset if ptr runs out of boundaries
			if (newPtr >= maxImageBuffer - maxTCPBuffer) {
				warn(TAG, "Out of index, should not happen!");
				imagePtr = 0;
				tcpPtr = 0;
			}
		} catch (Exception eg) {
			error(TAG, "General input stream error", eg);
			eg.printStackTrace();
		}
	}

	private void writeCmd(int index, byte[] extra_input) {
		int len = 0;
		switch (index) {
		case 1:
			len = 23;
			break;
		case 2:
			len = 49;
			break;
		case 3:
			len = 24;
			break;
		case 4:
			len = 27;
			break;
		case 5: // forward, right
			len = 25;
			break;
		case 6: // backward, right
			len = 25;
			break;
		case 7: // forward, left
			len = 25;
			break;
		case 8: // backward, left
			len = 25;
			break;
		case 9:
			len = 23;
			break;
		case 10: // infrared ON
			len = 24;
			break;
		case 11: // infrared OFF
			len = 24;
			break;
		}
		byte[] buffer = new byte[len];
		for (int i = 4; i < len; i++)
			buffer[i] = '\0';
		buffer[0] = 'M';
		buffer[1] = 'O';
		buffer[2] = '_';
		buffer[3] = 'O';
		if (index == 4) {
			buffer[3] = 'V';
		}

		switch (index) {
		case 1:
			break;
		case 2:
			buffer[4] = 0x02;
			buffer[15] = 0x1a;
			buffer[23] = 'A';
			buffer[24] = 'C';
			buffer[25] = '1';
			buffer[26] = '3';
			buffer[36] = 'A';
			buffer[37] = 'C';
			buffer[38] = '1';
			buffer[39] = '3';
			break;
		case 3:
			buffer[4] = 0x04;
			buffer[15] = 0x01;
			buffer[19] = 0x01;
			buffer[23] = 0x02;
			break;
		case 4:
			buffer[15] = 0x04;
			buffer[19] = 0x04;
			for (int i = 0; i < 4; i++)
				buffer[i + 23] = extra_input[i];
			break;
		case 5: // forward, left
			buffer[4] = (byte) 0xfa;
			buffer[15] = 0x02;
			buffer[19] = 0x01;
			buffer[23] = 0x04;
			buffer[24] = extra_input[0];
			break;
		case 6: // backward, left
			buffer[4] = (byte) 0xfa;
			buffer[15] = 0x02;
			buffer[19] = 0x01;
			buffer[23] = 0x05;
			buffer[24] = extra_input[0];
			break;
		case 7: // forward, right
			buffer[4] = (byte) 0xfa;
			buffer[15] = 0x02;
			buffer[19] = 0x01;
			buffer[23] = 0x01;
			buffer[24] = extra_input[0];
			break;
		case 8: // backward, right
			buffer[4] = (byte) 0xfa;
			buffer[15] = 0x02;
			buffer[19] = 0x01;
			buffer[23] = 0x02;
			buffer[24] = extra_input[0];
			break;
		case 9: // IR off(?)
			buffer[4] = (byte) 0xff;
			break;
		case 10: // infrared ON
			buffer[4] = (byte) 0x0e;
			buffer[15] = 0x01;
			buffer[19] = 0x01;
			buffer[23] = (byte)0x5e;
			break;
		case 11: // infrared OFF
			buffer[4] = (byte) 0x0e;
			buffer[15] = 0x01;
			buffer[19] = 0x01;
			buffer[23] = (byte)0x5f;
			break;
		}

		String str = new String(buffer, 0, len);
		debug(TAG, String.format("Write i=%d, str=%s", index, str));
		if (index != 4) {
			try {
				cSock.getOutputStream().write(buffer, 0, len);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				vSock.getOutputStream().write(buffer, 0, len);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public AC13RoverParameters getParameters() {
		return parameters;
	}

	public void requestAllParameters()
	{
		//Getting Parameters
		new Thread(new GetParametersRunnable()).start();
	}
	
	private class GetParametersRunnable implements Runnable {
		
		public void run() {
			try {	
				
				 ArrayList<String> params = new ArrayList<String>();
				 
		         HttpClient mClient= new DefaultHttpClient();
		         HttpGet get = new HttpGet("http://192.168.1.100/get_params.cgi" );
		         get.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("AC13", "AC13"),"UTF-8", false));
		         
		         mClient.execute(get);
		         HttpResponse response = mClient.execute(get);
		      
		         BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				 String line = "";
				 
				 while ((line = rd.readLine()) != null) 
					 params.add((line.substring(line.indexOf("=")+1,line.indexOf(";"))).replace("'", ""));
					
				parameters.fillParameters(params);
				
			} catch (Exception e) {
				e.printStackTrace();
				error(TAG, "GET PARAMETERS ERROR: " +  e.toString());
			}
		}
	}
}
