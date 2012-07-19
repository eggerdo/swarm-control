package org.dobots.nxt;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class NXT {
	
	private BTCommunicator m_oConnection;
	private Handler m_oBtcHandler;
	
	int motorLeft;
	private int directionLeft; // +/- 1
	int motorRight;
	private boolean stopAlreadySent = false;
	private int directionRight; // +/- 1
	private int motorAction;
	private int directionAction; // +/- 1
	
	private String programToStart;
	
	private Handler m_oHandler;
	
	public NXT(Handler i_oHandler) {
		m_oHandler = i_oHandler;
	}
	
	public void setConnection(BTCommunicator i_oConnection) {
		m_oConnection = i_oConnection;
	}
	
	public void disconnect() {
		sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DISCONNECT, 0, 0);
	}
	
	public void getFirmwareVersion() {
		sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.GET_FIRMWARE_VERSION, 0, 0);
	}

	/**
	 * Initialization of the motor commands for the different robot types.
	 */
	public void setUpByType() {
		// default
		motorLeft = BTCommunicator.MOTOR_B;
		directionLeft = 1;
		motorRight = BTCommunicator.MOTOR_C;
		directionRight = 1;
		motorAction = BTCommunicator.MOTOR_A;
		directionAction = 1;
	}

	public void findFiles(int par1, int par2) {
		sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.FIND_FILES, par1, par2);
	}

	/**
	 * Depending on the status (whether the program runs already) we stop it, wait and restart it again.
	 * @param status The current status, 0x00 means that the program is already running.
	 */   
	public void startRXEprogram(byte status) {
		if (status == 0x00) {
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.STOP_PROGRAM, 0, 0);
			sendBTCmessage(1000, BTCommunicator.START_PROGRAM, programToStart);
		}    
		else {
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.START_PROGRAM, programToStart);
		}
	}        

	/**
	 * Starts a program on the NXT robot.
	 * @param name The program name to start. Has to end with .rxe on the LEGO firmware and with .nxj on the 
	 *             leJOS NXJ firmware.
	 */   
	public void startProgram(String name) {
		// for .rxe programs: get program name, eventually stop this and start the new one delayed
		// is handled in startRXEprogram()
		if (name.endsWith(".rxe")) {
			programToStart = name;        
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.GET_PROGRAM_NAME, 0, 0);
			return;
		}

		// for .nxj programs: stop bluetooth communication after starting the program
		if (name.endsWith(".nxj")) {
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.START_PROGRAM, name);
			Message myMessage = new Message();
			Bundle myBundle = new Bundle();
			myBundle.putInt("message", BTCommunicator.DESTROY);
			myMessage.setData(myBundle);
			m_oHandler.dispatchMessage(myMessage);
			return;
		}        

		// for all other programs: just start the program
		sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.START_PROGRAM, name);
	}

	/**
	 * Sends the message via the BTCommunicator to the robot.
	 * @param delay time to wait before sending the message.
	 * @param message the message type (as defined in BTCommucator)
	 * @param value1 first parameter
	 * @param value2 second parameter
	 */   
	private void sendBTCmessage(int delay, int message, int value1, int value2) {
		Bundle myBundle = new Bundle();
		myBundle.putInt("message", message);
		myBundle.putInt("value1", value1);
		myBundle.putInt("value2", value2);
		Message myMessage = m_oHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			m_oBtcHandler.sendMessage(myMessage);

		else
			m_oBtcHandler.sendMessageDelayed(myMessage, delay);
	}

	/**
	 * Sends the message via the BTCommuncator to the robot.
	 * @param delay time to wait before sending the message.
	 * @param message the message type (as defined in BTCommucator)
	 * @param String a String parameter
	 */       
	void sendBTCmessage(int delay, int message, String name) {
		Bundle myBundle = new Bundle();
		myBundle.putInt("message", message);
		myBundle.putString("name", name);
		Message myMessage = m_oHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			m_oBtcHandler.sendMessage(myMessage);
		else
			m_oBtcHandler.sendMessageDelayed(myMessage, delay);
	}

}
