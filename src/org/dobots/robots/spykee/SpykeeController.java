// Based on the source of Jack Veenstra and modified to
// provide additional functionality. The original can be found
// at 
//   http://code.google.com/p/spykee-remote/
//

// Original source code license:
// 
// Copyright 2011 Jack Veenstra
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.dobots.robots.spykee;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.security.auth.login.LoginException;

import org.dobots.robots.MessageTypes;
import org.dobots.utilities.log.Loggable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * This class handles communication with the Spykee robot.  It creates a thread
 * to do the network IO in the background.  It uses the supplied Handler to
 * send messages to the UI thread.
 */
public class SpykeeController extends Loggable {
	
	public static final String TAG = "SpykeeCtrl";
	
	private Handler mHandler;
	private Socket mSocket;
	private DataInputStream mInput;
	private DataOutputStream mOutput;
	public enum DockState { DOCKED, UNDOCKED, DOCKING };
	private DockState mDockState;
	private boolean mConnected;

	private static final byte[] CMD_LOGIN       = { 'P', 'K', 0x0a, 0 };
	private static final byte[] CMD_UNDOCK      = { 'P', 'K', 0x10, 0, 1, 5 };
	private static final byte[] CMD_DOCK        = { 'P', 'K', 0x10, 0, 1, 6 };
	private static final byte[] CMD_CANCEL_DOCK = { 'P', 'K', 0x10, 0, 1, 7 };
	private static final byte[] CMD_START_VIDEO = { 'P', 'K', 0x0f, 0, 2, 1, 1 };
	private static final byte[] CMD_STOP_VIDEO  = { 'P', 'K', 0x0f, 0, 2, 1, 0 };
	private static final byte[] CMD_START_AUDIO = { 'P', 'K', 0x0f, 0, 2, 2, 1 };
	private static final byte[] CMD_STOP_AUDIO  = { 'P', 'K', 0x0f, 0, 2, 2, 0 };
	private static byte[] mCmdSoundEffect       = { 'P', 'K', 0x07, 0, 1, 0 };
	private static byte[] mCmdSetVolume         = { 'P', 'K', 0x09, 0, 1, SpykeeTypes.DEFAULT_VOLUME };
	private static byte[] mCmdMove              = { 'P', 'K', 0x05, 0, 0x02, 0, 0 };
	private static byte[] mCmdLed				= { 'P', 'K', 0x04, 0, 0x02, 0, 0 };
	
	// Create a single Runnable that we can reuse for stopping the motor.
	private MotorStopper mMotorStopper = new MotorStopper();

	// Keep track of the time that we want to stop the motor. This allows us
	// to keep the motor running smoothly if another motor command arrives
	// before the time to stop the motor.
	private long mStopMotorTime;

	private int mImageFileNumber;
	private static final int NUM_IMAGE_FILES = 1000;

	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	public void connect(String host, int port, String login, String password)
	        throws UnknownHostException, IOException, LoginException {
		debug(TAG, "connecting to " + host + ":" + port);
		mSocket = new Socket();
		SocketAddress addr = new InetSocketAddress(host, port);
		mSocket.connect(addr, 5000);
		mOutput = new DataOutputStream(mSocket.getOutputStream());
		mInput = new DataInputStream(mSocket.getInputStream());
		mConnected = true;
		debug(TAG, "Connection OK");
		
		sendLogin(login, password);
		readLoginResponse();
		startNetworkReaderThread();
		debug(TAG, "Login OK");
	}

	public void close() {
		try {
			mConnected = false;
			if (mOutput != null) {
				mOutput.close();
				mInput.close();
				mSocket.close();
			}
		} catch (IOException e) {
		}
	}

	private void sendLogin(String login, String password) throws IOException {
		int len = CMD_LOGIN.length + login.length() + password.length() + 3;
		byte[] bytes = new byte[len];
		System.arraycopy(CMD_LOGIN, 0, bytes, 0, CMD_LOGIN.length);
		int pos = CMD_LOGIN.length;
		bytes[pos++] = (byte) (login.length() + password.length() + 2);
		bytes[pos++] = (byte) login.length();
		System.arraycopy(login.getBytes(), 0, bytes, pos, login.length());
		pos += login.length();
		bytes[pos++] = (byte) password.length();
		System.arraycopy(password.getBytes(), 0, bytes, pos, password.length());
		showBuffer("send", bytes, bytes.length);
		sendBytes(bytes);
	}

	private void readLoginResponse() throws IOException, LoginException {
		byte[] bytes = new byte[2048];
		int num = readBytes(bytes, 0, 5);
		showBuffer("recv", bytes, num);

		// The fifth byte is the number of remaining bytes to read
		int len = bytes[4];
		num = readBytes(bytes, 0, len);
		showBuffer("recv", bytes, num);
		if (len < 8) {
			throw new LoginException("login failed");
		}

		int pos = 1;
		int nameLen = bytes[pos++];
		String name1 = new String(bytes, pos, nameLen, "ISO-8859-1");
		pos += nameLen;
		nameLen = bytes[pos++];
		String name2 = new String(bytes, pos, nameLen, "ISO-8859-1");
		pos += nameLen;
		nameLen = bytes[pos++];
		String name3 = new String(bytes, pos, nameLen, "ISO-8859-1");
		pos += nameLen;
		nameLen = bytes[pos++];
		String version = new String(bytes, pos, nameLen, "ISO-8859-1");
		pos += nameLen;

		if (bytes[pos] == 0) {
			setDockingState(DockState.DOCKED);
		} else {
			setDockingState(DockState.UNDOCKED);
		}
		
		info(TAG, name1 + " " + name2 + " " + name3 + " " + version + " docked: " + mDockState);
	}
	
	public void setLed(int i_nLed, boolean i_bOn) {
		mCmdLed[5] = (byte) i_nLed;
		mCmdLed[6] = i_bOn ? (byte) 0x01 : 0x00;
		try {
			sendBytes(mCmdLed);
		} catch (IOException e) {
		}
	} 

	public void moveForward(int i_nVelocity) {
		debug(TAG, String.format("fwd (v=%d)", i_nVelocity));
		
		mCmdMove[5] = (byte) i_nVelocity;
		mCmdMove[6] = (byte) i_nVelocity;
		try {
			showBuffer("moveForward", mCmdMove, mCmdMove.length);
			sendBytes(mCmdMove);
//			stopMotorAfterDelay(300);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void moveForward(int i_nLeftVelocity, int i_nRightVelocity) {
		debug(TAG, String.format("fwd (vl=%d, vr=%d)", i_nLeftVelocity, i_nRightVelocity));
		
		mCmdMove[5] = (byte) i_nLeftVelocity;
		mCmdMove[6] = (byte) i_nRightVelocity;
		try {
			showBuffer("moveForward", mCmdMove, mCmdMove.length);
			sendBytes(mCmdMove);
//			stopMotorAfterDelay(300);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void moveBackward(int i_nVelocity) {
		debug(TAG, String.format("bwd (v=%d)", i_nVelocity));
		
		mCmdMove[5] = (byte) -i_nVelocity;
		mCmdMove[6] = (byte) -i_nVelocity;
		mCmdMove[6] = (byte) (255 - i_nVelocity);
		try {
			showBuffer("moveBackward", mCmdMove, mCmdMove.length);
			sendBytes(mCmdMove);
//			stopMotorAfterDelay(200);
		} catch (IOException e) {
		}
	}

	public void moveBackward(int i_nLeftVelocity, int i_nRightVelocity) {
		debug(TAG, String.format("bwd (vl=%d, vr=%d)", i_nLeftVelocity, i_nRightVelocity));
		
		mCmdMove[5] = (byte) -i_nLeftVelocity;
		mCmdMove[6] = (byte) -i_nRightVelocity;
		try {
			showBuffer("moveBackward", mCmdMove, mCmdMove.length);
			sendBytes(mCmdMove);
//			stopMotorAfterDelay(200);
		} catch (IOException e) {
		}
	}

	public void moveLeft(int i_nVelocity) {
		debug(TAG, String.format("c cw (v=%d)", i_nVelocity));
		
		mCmdMove[5] = (byte) -i_nVelocity;
		mCmdMove[6] = (byte) i_nVelocity;
		try {
			showBuffer("moveLeft", mCmdMove, mCmdMove.length);
			sendBytes(mCmdMove);
//			stopMotorAfterDelay(200);
		} catch (IOException e) {
		}
	}

	public void moveRight(int i_nVelocity) {
		debug(TAG, String.format("cw (v=%d)", i_nVelocity));
		
		mCmdMove[5] = (byte) i_nVelocity;
		mCmdMove[6] = (byte) -i_nVelocity;
		try {
			showBuffer("moveRight", mCmdMove, mCmdMove.length);
			sendBytes(mCmdMove);
//			stopMotorAfterDelay(200);
		} catch (IOException e) {
		}
	}

	public void stopMotor() {
		debug(TAG, "stop");
		
		mCmdMove[5] = 0;
		mCmdMove[6] = 0;
		try {
			showBuffer("moveStop", mCmdMove, mCmdMove.length);
			sendBytes(mCmdMove);
		} catch (IOException e) {
		}
	}

	public void move(int nleft, int nright) {
		debug(TAG, String.format("move (vl=%d, vr=%d)", nleft, nright));
		
		mCmdMove[5] = (byte) nleft;
		mCmdMove[6] = (byte) nright;
		try {
			showBuffer("move", mCmdMove, mCmdMove.length);
			sendBytes(mCmdMove);
		} catch (IOException e) {
		}
	}

	private void stopMotorAfterDelay(long delayMillis) {
		mStopMotorTime = SystemClock.uptimeMillis() + delayMillis;
		mHandler.postDelayed(mMotorStopper, delayMillis);
	}

	private class MotorStopper implements Runnable {
		public void run() {
			// Check the time so that we don't stop the motor if another
			// motor command arrived after the one that posted this runnable.
			long currentTime = SystemClock.uptimeMillis();
			if (currentTime >= mStopMotorTime) {
				stopMotor();
			}
		}
	}

	public void activate() {
		if (mDockState == DockState.DOCKED) {
			undock();
		}
		// don't start video and audio automatically but only on request
//		startVideo();
//		startAudio();
		setVolume(SpykeeTypes.DEFAULT_VOLUME);
	}

	public DockState getDockState() {
		return mDockState;
	}
	
	private void setDockingState(DockState i_eState) {
		mDockState = i_eState;

		// inform the UI about the docking state change
		Message msg = mHandler.obtainMessage(SpykeeMessageTypes.DOCKINGSTATE_RECEIVED);
		msg.obj = i_eState;
		mHandler.sendMessage(msg);
	}

	public void dock() {
		try {
			sendBytes(CMD_DOCK);
			setDockingState(DockState.DOCKING);
		} catch (IOException e) {
		}
	}

	public void undock() {
		try {
			sendBytes(CMD_UNDOCK);
			setDockingState(DockState.UNDOCKED);
		} catch (IOException e) {
		}
	}

	public void cancelDock() {
		try {
			sendBytes(CMD_CANCEL_DOCK);
			setDockingState(DockState.UNDOCKED);
		} catch (IOException e) {
		}
	}

	public void setVolume(int volume) {
		mCmdSetVolume[5] = (byte) volume;
		try {
			sendBytes(mCmdSetVolume);
		} catch (IOException e) {
		}
	}

	public void startVideo() {
		try {
			sendBytes(CMD_START_VIDEO);
		} catch (IOException e) {
		}
	}

	public void stopVideo() {
		try {
			sendBytes(CMD_STOP_VIDEO);
		} catch (IOException e) {
		}
	}

	public void startAudio() {
		try {
			sendBytes(CMD_START_AUDIO);
		} catch (IOException e) {
		}
	}

	public void stopAudio() {
		try {
			sendBytes(CMD_STOP_AUDIO);
		} catch (IOException e) {
		}
	}

	public void playSoundAlarm() {
		mCmdSoundEffect[5] = 0;
		try {
			sendBytes(mCmdSoundEffect);
		} catch (IOException e) {
		}
	}

	public void playSoundBomb() {
		mCmdSoundEffect[5] = 1;
		try {
			sendBytes(mCmdSoundEffect);
		} catch (IOException e) {
		}
	}

	public void playSoundLazer() {
		mCmdSoundEffect[5] = 2;
		try {
			sendBytes(mCmdSoundEffect);
		} catch (IOException e) {
		}
	}

	public void playSoundAhAhAh() {
		mCmdSoundEffect[5] = 3;
		try {
			sendBytes(mCmdSoundEffect);
		} catch (IOException e) {
		}
	}

	public void playSoundEngine() {
		mCmdSoundEffect[5] = 4;
		try {
			sendBytes(mCmdSoundEffect);
		} catch (IOException e) {
		}
	}

	public void playSoundRobot() {
		mCmdSoundEffect[5] = 5;
		try {
			sendBytes(mCmdSoundEffect);
		} catch (IOException e) {
		}
	}

	public void playSoundCustom1() {
		mCmdSoundEffect[5] = 6;
		try {
			sendBytes(mCmdSoundEffect);
		} catch (IOException e) {
		}
	}

	public void playSoundCustom2() {
		mCmdSoundEffect[5] = 7;
		try {
			sendBytes(mCmdSoundEffect);
		} catch (IOException e) {
		}
	}

	private void startNetworkReaderThread() {
		new Thread(new Runnable() {
			public void run() {
				readFromSpykee(); 
			}
		}).start();
	}

	/**
	 * Reads network packets from the Spykee robot. This runs in a background
	 * thread.
	 */
	private void readFromSpykee() {
		Message msg;
		byte[] bytes = new byte[8192];
		while (true) {
			int num, len = 0;
			int cmd = -1;
			byte[] frame = null;
			try {
				num = readBytes(bytes, 0, 5);
				if (num == 5 && (bytes[0] & 0xff) == 'P' && (bytes[1] & 0xff) == 'K') {
					cmd = bytes[2] & 0xff;
					len = ((bytes[3] & 0xff) << 8) | (bytes[4] & 0xff);
					debug(TAG, "cmd: " + cmd + " len: " + len);
					switch (cmd) {
					case SpykeeTypes.SPYKEE_BATTERY_LEVEL:
						num += readBytes(bytes, 5, len);
						int level = bytes[5] & 0xff;
						msg = mHandler.obtainMessage(SpykeeMessageTypes.BATTERY_LEVEL_RECEIVED);
						msg.arg1 = level;
						mHandler.sendMessage(msg);
						break;
					case SpykeeTypes.SPYKEE_VIDEO_FRAME:
						// Avoid an extra data copy by reading directly into
						// the video frame
						frame = new byte[len];
						num += readBytes(frame, 0, len);
						//showBuffer("video", frame, len);
						//writeNextImageFile(frame, len);
		    			Bitmap bitmap = BitmapFactory.decodeByteArray(frame, 0, len);
		    			if (bitmap == null) {
		    				break;
		    			}
						msg = mHandler.obtainMessage(SpykeeMessageTypes.VIDEO_FRAME_RECEIVED);
						msg.obj = bitmap;
						mHandler.sendMessage(msg);
						break;
					case SpykeeTypes.SPYKEE_AUDIO:
						// Avoid an extra data copy by reading directly into
						// the audio buffer
						frame = new byte[len];
						num += readBytes(frame, 0, len);
//						writeNextAudioFile(frame, len);
						//showBuffer("audio", frame, len);
						msg = mHandler.obtainMessage(SpykeeMessageTypes.AUDIO_RECEIVED);
						mHandler.sendMessage(msg);
						break;
					case SpykeeTypes.SPYKEE_DOCK:
						num += readBytes(bytes, 5, len);
						showBuffer("recv", bytes, num);
						int val = bytes[5] & 0xff;
						if (val == SpykeeTypes.SPYKEE_DOCK_DOCKED) {
							setDockingState(DockState.DOCKED);
						} else if (val == SpykeeTypes.SPYKEE_DOCK_UNDOCKED) {
							setDockingState(DockState.UNDOCKED);
						}
						break;
					default:
						num += readBytes(bytes, 5, len);
						showBuffer("recv", bytes, num);
					}
				} else if (num == 0) {
					msg = mHandler.obtainMessage(MessageTypes.STATE_RECEIVEERROR);
					mHandler.sendMessage(msg);
				} else {
					error(TAG, "unexpected data, num: " + num);
					showBuffer("recv", bytes, num);
				}
			} catch (IOException e) {
				error(TAG, "IO exception: " + e);
				break;
			}
		}
	}

	/**
	 * Sends a command to Spykee.
	 * @param bytes the byte array containing the Spykee command
	 * @throws IOException
	 */
	private void sendBytes(byte[] bytes) throws IOException {
		if (mConnected) {
			mOutput.write(bytes);
		}
	}

	/**
	 * Tries to read "len" bytes into the given byte array. Returns the number
	 * of bytes actually read.
	 *
	 * @param bytes the destination byte array
	 * @param offset the starting offset into the byte array for the first byte
	 * @param len the number of bytes to read
	 * @return the actual number of bytes read
	 * @throws IOException
	 */
	private int readBytes(byte[] bytes, int offset, int len) throws IOException {
		if (mConnected) {
			int remaining = len;
			while (remaining > 0) {
				int numRead = mInput.read(bytes, offset, remaining);
				//Log.i(TAG, "readBytes(): " + numRead);
				if (numRead <= 0) {
					break;
				}
				offset += numRead;
				remaining -= numRead;
			}
			return len - remaining;
		} else {
			return -1;
		}
	}

	/**
	 * Displays the bytes in the given array, both in hex and in ascii.
	 *
	 * @param bytes the array of bytes
	 */
	private void showBuffer(String tag, byte[] bytes, int len) {
		if (len > 256) {
			len = 256;
		}
		int charsPerLine = SpykeeTypes.CHARS_PER_LINE;
		if (len < charsPerLine) {
			charsPerLine = len;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < len; i += charsPerLine) {
			builder.append(tag + " ");
			for (int j = 0; j < charsPerLine; j++) {
				if (i + j >= len) {
					break;
				}
				byte val = bytes[i + j];
				builder.append(String.format("%02x ", val));
				if ((j & SpykeeTypes.EXTRA_SPACE_MASK) == SpykeeTypes.EXTRA_SPACE_MASK) {
					builder.append(" ");
				}
			}
			if (len - i < charsPerLine) {
				for (int j = len - i; j < charsPerLine; j++) {
					builder.append("   ");
					if ((j & SpykeeTypes.EXTRA_SPACE_MASK) == SpykeeTypes.EXTRA_SPACE_MASK) {
						builder.append(" ");
					}
				}
			}

			// Put an extra space before the ascii character dump
			builder.append(" ");
			for (int j = 0; j < charsPerLine; j++) {
				if (i + j >= len) {
					break;
				}
				byte val = bytes[i + j];
				if (val < 0x20 || val > 0x7e) {
					val = '.';
				}
				builder.append(String.format("%c", val));
				if ((j & SpykeeTypes.EXTRA_SPACE_MASK) == SpykeeTypes.EXTRA_SPACE_MASK) {
					builder.append(" ");
				}
			}
//			Log.d(TAG, builder.toString());
			builder.setLength(0);
		}
	}


// include if storing of audio is an issue
	
//    // The number of audio buffer files that we cycle through. Each audio
//    // packet from Spyke usually contains 1/8 second of sound (2000 16-bit
//    // samples from a 16KHz stream), but sometimes packets are delayed
//    // and contain more than 2000 audio samples.
//    // Since the audio buffers don't arrive in precise intervals, we need
//    // to buffer them and play them slightly delayed from real time.
//    private static final int MAX_AUDIO_BUFFERS = 16;
//
//    // If the number of audio buffers that we have downloaded gets too far
//    // ahead of the playback, then we will start dropping buffers to catch
//    // up.
//    private static final int DROP_AUDIO_THRESHOLD = 8;
//
//    // The number of buffers we have downloaded but not yet played.
//    private static int sNumAudioBuffers;
//
//    // The number of the audio buffer that we are downloading (wraps to zero).
//    private static int sDownloadingAudioNum;
//
//    // The number of the audio buffer that we are playing (wraps to zero).
//    private int mPlayingAudioNum;
//    
//    // Keep track of the number of times we had to wait for an audio
//    // buffer to download, and the number of times that we had to skip
//    // an audio buffer because playback was falling behind.
//    private int mNumWaits;
//    private int mNumSkips;
//
//	// The File object for the storage directory.
//    private static File sStorageRoot;
//
//	// The name of the directory under /sdcard/ where we store temporary files.
//	private static final String SPYKEE_DIR = "spykee";
//
//    private void init() {
//    	sStorageRoot = Environment.getExternalStorageDirectory();
//    	if (!sStorageRoot.canWrite()) {
//			Log.w(TAG, "Cannot write to external storage: " + sStorageRoot.getAbsolutePath());
//		} else {
//			// Create the spykee directory if it doesn't exist
//			File dir = new File(sStorageRoot, SPYKEE_DIR);
//			if (!dir.exists()) {
//				dir.mkdir();
//			}
//			sStorageRoot = dir;
//		}
//		sNumAudioBuffers = 0;
//		sDownloadingAudioNum = 0;
//		mPlayingAudioNum = 0;
//    }
//    
//    /**
//     * The file header for a .wav file.  We have to fill in the "Chunksize"
//     * and the "data size" bytes.  This is used for creating a sound file
//     * from the raw audio bytes that Spykee sends us.
//     */
//    private static byte[] sWaveHeader = {
//		0x52, 0x49, 0x46, 0x46,   // "RIFF"
//		0, 0, 0, 0,               // Chunksize (little-endian) = data size + 36
//		0x57, 0x41, 0x56, 0x45,   // "WAVE"
//		0x66, 0x6d, 0x74, 0x20,   // "fmt "
//		16, 0, 0, 0,              // subchunk1 size
//		1, 0, 1, 0,               // PCM, mono
//		(byte) 0x80, 0x3e, 0, 0,  // sample rate = 16KHz
//		(byte) 0x80, 0x3e, 0, 0,  // byte rate = 16000
//		1, 0, 8, 0,               // Channels * bytes/sample, bits/sample
//		0x64, 0x61, 0x74, 0x61,   // "data"
//		0, 0, 0, 0                // data size (little-endian)
//    };
//
//    /** The starting byte offset for the "Chunksize" field in the wave header */
//    private static final int WAVE_CHUNK_SIZE_OFFSET = 4;
//
//    /** The starting byte offset for the "data size" field in the wave header */
//    private static final int WAVE_DATA_SIZE_OFFSET = 40;
//
//    private static void convert16bit8bit(byte[] bytes16, byte[] bytes8, int len) {
//    	for (int i = 0, j = 0; i < len; i += 2, j += 1) {
//    		int val = (bytes16[i+1] << 8) + (bytes16[i] & 0xff);
//    		bytes8[j] = (byte) ((val + 0x8000) >> 8);
//    	}
//    }
//
//    /**
//     * Writes the given integer "value" as 4 bytes to the given "bytes" array
//     * starting at the given "offset".  The 32-bit integer value is written in
//     * little-endian order.
//     * @param value the 32-bit integer value to write
//     * @param bytes the destination byte array
//     * @param offset the index of the first byte to write
//     */
//    private static void int2Bytes(int value, byte[] bytes, int offset) {
//    	bytes[offset] = (byte) (value & 0xff);
//    	bytes[offset + 1] = (byte) ((value >> 8) & 0xff);
//    	bytes[offset + 2] = (byte) ((value >> 16) & 0xff);
//    	bytes[offset + 3] = (byte) ((value >> 24) & 0xff);
//    }
//
//	/**
//	 * Constructs the filename for the audio buffer from the index.
//	 * @param index the buffer number
//	 * @return the filename
//	 */
//	private static String getAudioFilename(int index) {
//		return "audio" + index + ".wav";
//	}
//
//	/**
//	 * Writes the next audio packet to a file using the ".wav" format.
//	 * The input is a stream of signed 16-bit integers (little-endian)
//	 * audio samples at 16000 Hz. This is converted to to 8-bit unsigned
//	 * samples (still at 16KHz) so that the playback is slightly faster.
//	 * @param bytes the input stream of 16-bit audio samples
//	 * @param len the number of bytes in the array
//	 */
//	static void writeNextAudioFile(byte[] bytes, int len) {
//		String filename = getAudioFilename(sDownloadingAudioNum);
//		sDownloadingAudioNum += 1;
//		if (sDownloadingAudioNum >= MAX_AUDIO_BUFFERS) {
//			sDownloadingAudioNum = 0;
//		}
//		File waveFile = new File(sStorageRoot, filename);
//		RandomAccessFile output = null;
//		try {
//			byte[] samples = new byte[len / 2];
//			convert16bit8bit(bytes, samples, len);
//			output = new RandomAccessFile(waveFile, "rw");
//			output.setLength(0);
//			len = len / 2;
//	
//			// Fill in the wave header based on the input size.
//	    	int2Bytes(len + 36, sWaveHeader, WAVE_CHUNK_SIZE_OFFSET);
//			int2Bytes(len, sWaveHeader, WAVE_DATA_SIZE_OFFSET);
//	
//			// Write the wave header to the file, followed by the data.
//			output.write(sWaveHeader, 0, sWaveHeader.length);
//			output.write(samples, 0, len);
//		} catch (FileNotFoundException e) {
//			Log.i(TAG, waveFile.getAbsolutePath() + ": " + e);
//			return;
//		} catch (IOException e) {
//			Log.i(TAG, waveFile.getAbsolutePath() + ": " + e);
//			return;
//		} finally {
//			try {
//				if (output != null) {
//					output.close();
//				}
//			} catch (IOException e) {
//				Log.i(TAG, waveFile.getAbsolutePath() + ": " + e);
//			}
//		}
//	}
	
}

