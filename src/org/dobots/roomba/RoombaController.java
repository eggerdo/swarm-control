package org.dobots.roomba;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

import android.bluetooth.BluetoothSocket;

public class RoombaController {
	
	private BTConnectionThread m_oConnectionHandler; 
	
	private boolean m_bMsgReceived;
	private byte[] m_rgRxBuffer;
	private int m_nRxBytes;
	
	private class BTConnectionThread extends Thread {
		private BluetoothSocket m_oSocket;
		private InputStream m_oInStream;
		private OutputStream m_oOutStream;
		
		private Object m_oParent;
		
		public BTConnectionThread(Object i_oParent, BluetoothSocket i_oSocket) {
			m_oSocket = i_oSocket;
			m_oParent = i_oParent;
			
			try {
				m_oInStream = m_oSocket.getInputStream();
				m_oOutStream = m_oSocket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void run() {
//			byte[] buffer = new byte[1024];
			m_rgRxBuffer = new byte[1024];
//			int bytes;
			
			while (true) {
				try {
					m_nRxBytes = m_oInStream.read(m_rgRxBuffer);
					m_bMsgReceived = true;
					synchronized(m_oParent) {
						m_oParent.notify();
					}
//					m_oHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//							  .sendToTarget();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
			}
		}
		
		public void write(byte[] buffer) {
			try {
				m_oOutStream.write(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void close() {
			try {
				m_oSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////
	/// Public Functions
	/////////////////////////////////////////////////////////////////////////

	public void setConnection(BluetoothSocket i_oSocket) {
		m_oConnectionHandler = new BTConnectionThread(this, i_oSocket);
		m_oConnectionHandler.start();
	}
	
	public boolean isConnected() {
		return m_oConnectionHandler != null;
	}
		
	/*
	 * Command opcode: 128		Number of data bytes: 0
	 * 
	 * Starts the SCI. The Start command must be sent before any
	 * other SCI commands. This command puts the SCI in passive
     * mode.
     *
     * Serial sequence: [128]
	 */
	public void start() {
		executeCommand(128);
	}
	
	/*
	 * Command opcode: 129		Number of data bytes: 1
	 * 
	 * Sets the baud rate in bits per second (bps) at which SCI
	 * commands and data are sent according to the baud code sent
	 * in the data byte. The default baud rate at power up is 57600
	 * bps. (See Serial Port Settings, above.) Once the baud rate is
	 * changed, it will persist until Roomba is power cycled by removing
	 * the battery (or until the battery voltage falls below the minimum
	 * required for processor operation). You must wait 100ms after
	 * sending this command before sending additional commands
	 * at the new baud rate. The SCI must be in passive, safe, or full
	 * mode to accept this command. This command puts the SCI in
	 * passive mode.
	 * 
	 * Serial sequence: [129] [Baud Code]
	 * Baud data byte 1: Baud Code (0 – 11)
	 */
	public void baud(byte i_byBaudCode) {
		executeCommand(129, i_byBaudCode);
	}
	
	/*
	 * Command opcode: 130		Number of data bytes: 0
	 * 
	 * Enables user control of Roomba. This command must be sent
	 * after the start command and before any control commands are
	 * sent to the SCI. The SCI must be in passive mode to accept this
	 * command. This command puts the SCI in safe mode.
	 * 
	 * Serial sequence: [130]
	 */
	public void control() {
		executeCommand(130);
	}
	
	/*
	 * Command opcode: 131		Number of data bytes: 0
	 * 
	 * This command puts the SCI in safe mode. The SCI must be in
	 * full mode to accept this command.
	 * 
	 * Note: In order to go from passive mode to safe mode, use the Control
	 * command
	 * 
	 * Serial sequence: [131]
	 */
	public void safe() {
		executeCommand(131);
	}
	
	/*
	 * Command opcode: 132		Number of data bytes: 0
	 * 
	 * Enables unrestricted control of Roomba through the SCI and
	 * turns off the safety features. The SCI must be in safe mode to
	 * accept this command. This command puts the SCI in full mode.
	 * 
	 * Serial sequence: [132]
	 */
	public void full() {
		executeCommand(132);
	}

	/*
	 * Command opcode: 133		Number of data bytes: 0
	 * 
	 * Puts Roomba to sleep, the same as a normal “power” button
	 * press. The Device Detect line must be held low for 500 ms to
	 * wake up Roomba from sleep. The SCI must be in safe or full
	 * mode to accept this command. This command puts the SCI in
	 * passive mode.
	 * 
	 * Serial sequence: [133]
	 */
	public void power() {
		executeCommand(133);
	}

	/*
	 * Command opcode: 134		Number of data bytes: 0
	 * 
	 * Starts a spot cleaning cycle, the same as a normal “spot” 
	 * button press. The SCI must be in safe or full mode to accept this 
	 * command. This command puts the SCI in passive mode. 
	 * 
	 * Serial sequence: [134]
	 */
	public void spot() {
		executeCommand(134);
	}

	/*
	 * Command opcode: 135		Number of data bytes: 0
	 * 
	 * Starts a normal cleaning cycle, the same as a normal “clean”
	 * button press. The SCI must be in safe or full mode to accept this
	 * command. This command puts the SCI in passive mode.
	 * 
	 * Serial sequence: [135]
	 */
	public void clean() {
		executeCommand(135);
	}

	/*
	 * Command opcode: 136		Number of data bytes: 0
	 * 
	 * Starts a maximum time cleaning cycle, the same as a normal
	 * “max” button press. The SCI must be in safe or full mode to
	 * accept this command. This command puts the SCI in passive
	 * mode.
	 * 
	 * Serial sequence: [136]
	 */
	public void max() {
		executeCommand(136);
	}

	/*
	 *  Command opcode: 137		Number of data bytes: 4
	 *  
	 *  Controls Roomba’s drive wheels. The command takes four data
	 *  bytes, which are interpreted as two 16 bit signed values using
	 *  twos-complement. The first two bytes specify the average velocity
	 *  of the drive wheels in millimeters per second (mm/s), with the
	 *  high byte sent first. The next two bytes specify the radius, in
	 *  millimeters, at which Roomba should turn. The longer radii make
	 *  Roomba drive straighter; shorter radii make it turn more. A Drive
	 *  command with a positive velocity and a positive radius will make
	 *  Roomba drive forward while turning toward the left. A negative
	 *  radius will make it turn toward the right. Special cases for the
	 *  radius make Roomba turn in place or drive straight, as specified
	 *  below. The SCI must be in safe or full mode to accept this
	 *  command. This command does change the mode.
	 *  
	 *  Note: The robot system and its environment impose restrictions that may
	 *  prevent the robot from accurately carrying out some drive commands. For
	 *  example, it may not be possible to drive at full speed in an arc with a large
	 *  radius of curvature.
	 *  
	 *  Serial sequence: [137] [Velocity high byte] [Velocity low byte]
	 *					 [Radius high byte] [Radius low byte]
	 *
	 *  Drive data bytes 1 and 2: Velocity (-500 – 500 mm/s)
	 *  
	 *  Drive data bytes 3 and 4: Radius (-2000 – 2000 mm)
	 *  Special cases: 	Straight = 32768 = hex 8000
	 *  				Turn in place clockwise = -1
	 *  				Turn in place counter-clockwise = 1
	 */
	public void drive(int i_nVelocity, int i_nRadius) {
		byte nVelocityH, nVelocityL;
		byte nRadiusH, nRadiusL;
		
		nVelocityH = getHighByte(i_nVelocity);
		nVelocityL = getLowByte(i_nVelocity);
		
		nRadiusH = getHighByte(i_nRadius);
		nRadiusL = getLowByte(i_nRadius);
				
		executeCommand(137, nVelocityH, nVelocityL, nRadiusH, nRadiusL);
	}

	/*
	 * Command opcode: 138		Number of data bytes: 1
	 * 
	 * Controls Roomba’s cleaning motors. The state of each motor is
	 * specified by one bit in the data byte. The SCI must be in safe
	 * or full mode to accept this command. This command does not
	 * change the mode.
	 * 
	 * Serial sequence: [138] [Motor Bits]
	 * 
	 * Motors data byte 1: Motor Bits (0 – 7)
	 * 0 = off, 1 = on
	 * 
	 * Bit		7	  6		5	  4		3	  2			  1			0
	 * Motor	n/a	  n/a	n/a	  n/a	n/a	  MainBrush	  Vacuum	SideBrush
	 * 
	 */
	public void motors(byte i_byMotorBits) {
		executeCommand(138, i_byMotorBits);
	}

	/*
	 * Command opcode: 139		Number of data bytes: 3
	 * 
	 * Controls Roomba’s LEDs. The state of each of the spot, clean,
	 * max, and dirt detect LEDs is specified by one bit in the first data
	 * byte. The color of the status LED is specified by two bits in the
	 * first data byte. The power LED is specified by two data bytes, one
	 * for the color and one for the intensity. The SCI must be in safe
	 * or full mode to accept this command. This command does not
	 * change the mode.
	 * 
	 * Serial sequence: [139] [Led Bits] [Power Color] [Power Intensity]
	 * 
	 * Leds data byte 1: Led Bits (0 – 63)
	 * 
	 * Dirt Detect uses a blue LED: 0 = off, 1 = on
	 * Spot, Clean, and Max use green LEDs: 0 = off, 1 = on
	 * Status uses a bicolor (red/green) LED: 00 = off, 01 = red, 
	 * 										  10 = green, 11 = amber
	 * 
	 * Bit		7	  6		5	  4			3		2		1		0
	 * Motor	n/a	  n/a	Status (2 bits)	Spot	Clean	Max		Dirt Detect
	 * 
	 * Power uses a bicolor (red/green) LED whose intensity and color
	 * can be controlled with 8-bit resolution.
	 * 
	 * Leds data byte 2: Power Color (0 – 255)
	 * 0 = green, 255 = red. Intermediate values are intermediate colors.
	 * 
	 * Leds data byte 3: Power Intensity (0 – 255) 
	 * 0 = off, 255 = full intensity. Intermediate values are intermediate
	 * intensities.
	 */
	public void leds(byte i_byLedBits, byte i_byPowerColor, byte i_byPowerIntensity) {
		executeCommand(139, i_byLedBits, i_byPowerColor, i_byPowerIntensity);
	}

	/*
	 * Command opcode: 140		Number of data bytes: 2N + 2, where N is the
													  number of notes in the song
	 *
	 * Specifies a song to the SCI to be played later. Each song is
	 * associated with a song number which the Play command uses
	 * to select the song to play. Users can specify up to 16 songs
	 * with up to 16 notes per song. Each note is specified by a note
	 * number using MIDI note definitions and a duration specified
	 * in fractions of a second. The number of data bytes varies
	 * depending on the length of the song specified. A one note song
	 * is specified by four data bytes. For each additional note, two data
	 * bytes must be added. The SCI must be in passive, safe, or full
	 * mode to accept this command. This command does not change
	 * the mode.
	 * 
	 * Serial sequence: [140] [Song Number] [Song Length] [Note Number 1] 
	 * 					[Note Duration 1] [Note Number 2] [Note Duration 2] etc.
	 * 
	 *  Song data byte 1: Song Number (0 – 15) 60 C 261.6 109 C# 4434.9
	 *  Specifies the number of the song being specified. If you send
	 *  a second Song command with the same song number, the old
	 *  song will be overwritten. 
	 *  
	 *  Song data bytes 3, 5, 7, etc.: Note Number (31 – 127)
	 *  Specifies the pitch of the note to be played in terms of the MIDI
	 *  note numbering scheme. The lowest note that Roomba can
	 *  play is note number 31. See the note number table for specific
	 *  notes. Any note number outside of the range of 31 to 127 will
	 *  be interpreted as a rest note and no sound will be played during
	 *  this note duration.
	 *  
	 *  Song data byte 2: Song Length (1 – 16)
	 *  Specifies the length of the song in terms of the number of notes.
	 *  
	 *  Song data bytes 4, 6, 8, etc.: Note Duration (0 – 255)
	 *  Specifies the duration of the note in increments of 1/64 of a
	 *  second. Therefore, half-second long note will have a duration
	 *  value of 32.
	 */
	public void song(byte i_bySongNr, byte i_bynSongLength, byte[][] i_rgbySong) {
//		executeCommand(140, ...)
	}

	/*
	 * Command opcode: 141		Number of data bytes: 1
	 * 
	 * Plays one of 16 songs, as specified by an earlier Song
	 * command. If the requested song has not been specified yet,
	 * the Play command does nothing. The SCI must be in safe or full
	 * mode to accept this command. This command does not change
	 * the mode.
	 * 
	 * Serial sequence: [141] [Song Number]
	 * 
	 * Play data byte 1: Song Number (0 – 15)
	 * Specifies the number of the song to be played. This must match
	 * the song number of a song previously specified by a Song
	 * command.
	 */
	public void play(byte i_bySongNr) {
		executeCommand(141, i_bySongNr);
	}

	/*
	 * Command opcode: 142		Number of data bytes: 1
	 * 
	 * Requests the SCI to send a packet of sensor data bytes. The
	 * user can select one of four different sensor packets. The sensor
	 * data packets are explained in more detail in the next section.
	 * The SCI must be in passive, safe, or full mode to accept this
	 * command. This command does not change the mode.
	 * 
	 * Serial sequence: [142] [Packet Code]
	 * 
	 * Sensors data byte 1: Packet Code (0 – 3)
	 * Specifies which of the four sensor data packets should be sent
	 * back by the SCI. A value of 0 specifies a packet with all of the
	 * sensor data. Values of 1 through 3 specify specific subsets of
	 * the sensor data.
	 */
	public byte[] sensors(byte i_byPackage, int i_nResultLength) throws TimeoutException {
		int nLength;
		switch (i_byPackage) {
			case 1: nLength = 10;
					break;
			case 2: nLength = 6;
					break;
			case 3: nLength = 10;
					break;
			default: nLength = 26;
					break;
		}
		byte[] result = new byte[nLength];
		
		executeCommand(142, i_byPackage);
		getResults(result);
		
		return result;
	}

	/*
	 * Command opcode: 143		Number of data bytes: 1
	 * 
	 * Turns on force-seeking-dock mode, which causes the robot
	 * to immediately attempt to dock during its cleaning cycle if it
	 * encounters the docking beams from the Home Base. (Note,
	 * however, that if the robot was not active in a clean, spot or max
	 * cycle it will not attempt to execute the docking.) Normally the
	 * robot attempts to dock only if the cleaning cycle has completed
	 * or the battery is nearing depletion. This command can be sent
	 * anytime, but the mode will be cancelled if the robot turns off,
	 * begins charging, or is commanded into SCI safe or full modes.
	 * 
	 * Serial sequence: [143]
	 */
	public void dock() {
		executeCommand(143);
	}
	
	/////////////////////////////////////////////////////////////////////////
	/// Private Functions
	/////////////////////////////////////////////////////////////////////////
	
	private void executeCommand(int i_byCmd, byte ... args) {
		byte[] buffer = new byte[args.length + 1];
		
		buffer[0] = (byte) i_byCmd;
		int nPos = 1;
		for (int byArg : args) {
			buffer[nPos++] = (byte)byArg;
		}
		
		// send buffer into connection object ...
		if (m_oConnectionHandler != null) {
			m_oConnectionHandler.write(buffer);
		}
	}
		
	private synchronized void getResults(byte[] results) throws TimeoutException {
		int nRequiredBytes = results.length;
		int nReceivedBytes = 0;
		byte[] buffer = new byte[results.length];
		
		while (nReceivedBytes != nRequiredBytes) {
			try {
				wait(5000);
				if (!m_bMsgReceived) {
					// TODO error, no answer received
					throw new TimeoutException("No answer received");
				} else {
					m_bMsgReceived = false;
					
					System.arraycopy(m_rgRxBuffer, 0, buffer, nReceivedBytes, m_nRxBytes);

					nReceivedBytes += m_nRxBytes;
					m_nRxBytes = 0;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for (int i = 0; i < nReceivedBytes; i++) {
			results[i] = buffer[i];
		}
	}
	
	private byte getHighByte(int i_nValue) {
		return (byte) ((i_nValue >>> 8) & 0xFF);
	}
	
	private byte getLowByte(int i_nValue) {
		return (byte) (i_nValue & 0xFF);
	}
		
}
