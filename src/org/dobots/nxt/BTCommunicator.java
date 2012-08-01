/**
 *   Copyright 2010 Guenther Hoelzl, Shawn Brown
 *
 *   This file is part of MINDdroid.
 *
 *   MINDdroid is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   MINDdroid is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with MINDdroid.  If not, see <http://www.gnu.org/licenses/>.
**/

package org.dobots.nxt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import org.dobots.nxt.NXTTypes.ENXTSensorID;
import org.dobots.nxt.msg.RawDataMsg;
import org.dobots.swarmcontrol.R;
import org.dobots.utility.Utils;

/**
 * This class is for talking to a LEGO NXT robot via bluetooth.
 * The communciation to the robot is done via LCP (LEGO communication protocol).
 * Objects of this class can either be run as standalone thread or controlled
 * by the owners, i.e. calling the send/recive methods by themselves.
 */
public class BTCommunicator extends Thread {

    private Resources mResources;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket nxtBTsocket = null;
    private OutputStream nxtOutputStream = null;
    private InputStream nxtInputStream = null;
    private boolean connected = false;

    private Handler receiveHandler;
    private String mMACaddress;
    private BTConnectable myOwner;

    private byte[] returnMessage;

    public BTCommunicator(BTConnectable myOwner, Handler receiveHandler, BluetoothAdapter btAdapter, Resources resources) {
        this.myOwner = myOwner;
        this.receiveHandler = receiveHandler;
        this.btAdapter = btAdapter;
        this.mResources = resources;
    }

    public byte[] getReturnMessage() {
        return returnMessage;
    }

    public void setMACAddress(String mMACaddress) {
        this.mMACaddress = mMACaddress;
    }

    /**
     * @return The current status of the connection
     */            
    public boolean isConnected() {
        return connected;
    }

    /**
     * Creates the connection, waits for incoming messages and dispatches them. The thread will be terminated
     * on closing of the connection.
     */
    @Override
    public void run() {

        try {        
            createNXTconnection();
        }
        catch (IOException e) { }

        while (connected) {
            try {
                returnMessage = receiveMessage();
                if ((returnMessage.length >= 2) && ((returnMessage[0] == LCPMessage.REPLY_COMMAND) ||
                    (returnMessage[0] == LCPMessage.DIRECT_COMMAND_NOREPLY)))
                    dispatchMessage(returnMessage);

            } catch (IOException e) {
                // don't inform the user when connection is already closed
                if (connected) {
                	connected = false;
                    sendState(NXTTypes.STATE_RECEIVEERROR);
                }
                return;
            }
        }
    }

    /**
     * Create a bluetooth connection with SerialPortServiceClass_UUID
     * @see <a href=
     *      "http://lejos.sourceforge.net/forum/viewtopic.php?t=1991&highlight=android"
     *      />
     * On error the method either sends a message to it's owner or creates an exception in the
     * case of no message handler.
     */
    public void createNXTconnection() throws IOException {
        try {
            BluetoothSocket nxtBTSocketTemporary;
            BluetoothDevice nxtDevice = null;
            nxtDevice = btAdapter.getRemoteDevice(mMACaddress);
            if (nxtDevice == null) {
                if (receiveHandler == null)
                    throw new IOException();
                else {
                    sendToast(mResources.getString(R.string.no_paired_nxt));
                    sendState(NXTTypes.STATE_CONNECTERROR);
                    return;
                }
            }
            nxtBTSocketTemporary = nxtDevice.createRfcommSocketToServiceRecord(NXTTypes.SERIAL_PORT_SERVICE_CLASS_UUID);
            try {
                nxtBTSocketTemporary.connect();
            }
            catch (IOException e) {  
                if (myOwner.isPairing()) {
                    if (receiveHandler != null) {
                        sendToast(mResources.getString(R.string.pairing_message));
                        sendState(NXTTypes.STATE_CONNECTERROR_PAIRING);
                    }
                    else
                        throw e;
                    return;
                }

                // try another method for connection, this should work on the HTC desire, credits to Michael Biermann
                try {
                    Method mMethod = nxtDevice.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
                    nxtBTSocketTemporary = (BluetoothSocket) mMethod.invoke(nxtDevice, Integer.valueOf(1));            
                    nxtBTSocketTemporary.connect();
                }
                catch (Exception e1){
                    if (receiveHandler == null)
                        throw new IOException();
                    else
                        sendState(NXTTypes.STATE_CONNECTERROR);
                    return;
                }
            }
            nxtBTsocket = nxtBTSocketTemporary;
            nxtInputStream = nxtBTsocket.getInputStream();
            nxtOutputStream = nxtBTsocket.getOutputStream();
            connected = true;
        } catch (IOException e) {
            if (receiveHandler == null)
                throw e;
            else {
                if (myOwner.isPairing())
                    sendToast(mResources.getString(R.string.pairing_message));
                sendState(NXTTypes.STATE_CONNECTERROR);
                return;
            }
        }
        // everything was OK
        if (receiveHandler != null)
            sendState(NXTTypes.STATE_CONNECTED);
    }

    /**
     * Closes the bluetooth connection. On error the method either sends a message
     * to it's owner or creates an exception in the case of no message handler.
     */
    public void destroyNXTconnection() throws IOException {
        try {
            if (nxtBTsocket != null) {
                connected = false;
                nxtBTsocket.close();
                nxtBTsocket = null;
            }

            nxtInputStream = null;
            nxtOutputStream = null;

        } catch (IOException e) {
            if (receiveHandler == null)
                throw e;
            else
                sendToast(mResources.getString(R.string.problem_at_closing));
        }
    }

    /**
     * Sends a message on the opened OutputStream
     * @param message, the message as a byte array
     */
    public void sendMessage(byte[] message) throws IOException {
        if (nxtOutputStream == null)
            throw new IOException();

        // send message length
        int messageLength = message.length;
        nxtOutputStream.write(messageLength);
        nxtOutputStream.write(messageLength >> 8);
        nxtOutputStream.write(message, 0, message.length);
    }  

    /**
     * Receives a message on the opened InputStream
     * @return the message
     */                
    public byte[] receiveMessage() throws IOException {
        if (nxtInputStream == null)
            throw new IOException();

        int length = nxtInputStream.read();
        length = (nxtInputStream.read() << 8) + length;
        byte[] returnMessage = new byte[length];
        nxtInputStream.read(returnMessage);
        return returnMessage;
    }    

    /**
     * Sends a message on the opened OutputStream. In case of 
     * an error the state is sent to the handler.
     * @param message, the message as a byte array
     */
    private void sendMessageAndState(byte[] message) {
        if (nxtOutputStream == null)
            return;

        try {
            sendMessage(message);
        }
        catch (IOException e) {
        	connected = false;
            sendState(NXTTypes.STATE_SENDERROR);
        }
    }

    private void dispatchMessage(byte[] message) {
        switch (message[1]) {

            case LCPMessage.GET_OUTPUT_STATE:

                if (message.length >= 25)
                    sendStateAndData(NXTTypes.MOTOR_STATE, message);

                break;

            case LCPMessage.GET_FIRMWARE_VERSION:

                if (message.length >= 7)
                    sendStateAndData(NXTTypes.FIRMWARE_VERSION, message);

                break;

            case LCPMessage.FIND_FIRST:
            case LCPMessage.FIND_NEXT:

                if (message.length >= 28) {
                    // Success
                    if (message[2] == 0)
                        sendStateAndData(NXTTypes.FIND_FILES, message);
                }

                break;
                
            case LCPMessage.GET_CURRENT_PROGRAM_NAME:

                if (message.length >= 23) {
                    sendStateAndData(NXTTypes.PROGRAM_NAME, message);
                }
                
                break;
                
            case LCPMessage.SAY_TEXT:
                
                if (message.length == 22) {
                    sendStateAndData(NXTTypes.SAY_TEXT, message);
                }
                
            case LCPMessage.VIBRATE_PHONE:
                if (message.length == 3) {
                    sendStateAndData(NXTTypes.VIBRATE_PHONE, message);
                }               
                
            case LCPMessage.GET_INPUT_VALUES:
            	if (message.length == 16) {
            		sendStateAndData(NXTTypes.GET_INPUT_VALUES, message);
            	}
            	
            case LCPMessage.LS_GET_STATUS:
            	if (message.length == 4) {
            		sendStateAndData(NXTTypes.LS_GET_STATUS, message);
            	}
            	
            case LCPMessage.LS_READ:
            	if (message.length == 20) {
            		sendStateAndData(NXTTypes.LS_READ, message);
            	}
            
        }
    }
    
    public void keepAlive() {
    	byte[] message = LCPMessage.getKeepAliveMessage();
    	sendMessageAndState(message);
    }

    public void doBeep(int frequency, int duration) {
        byte[] message = LCPMessage.getBeepMessage(frequency, duration);
        sendMessageAndState(message);
        Utils.waitSomeTime(20);
    }
    
    public void doAction(int actionNr) {
        byte[] message = LCPMessage.getActionMessage(actionNr);
        sendMessageAndState(message);
    }

    public void startProgram(String programName) {
        byte[] message = LCPMessage.getStartProgramMessage(programName);
        sendMessageAndState(message);
    }

    public void stopProgram() {
        byte[] message = LCPMessage.getStopProgramMessage();
        sendMessageAndState(message);
    }
    
    public void requestProgramName() {
        byte[] message = LCPMessage.getProgramNameMessage();
        sendMessageAndState(message);
    }
    
    public void setMotorSpeed(int motor, int speed) {
        if (speed > 100)
            speed = 100;

        else if (speed < -100)
            speed = -100;

        byte[] message = LCPMessage.getMotorMessage(motor, speed);
        sendMessageAndState(message);
    }

    public void rotateTo(int motor, int end) {
        byte[] message = LCPMessage.getMotorMessage(motor, -80, end);
        sendMessageAndState(message);
    }

    public void requestMotorState(int motor) {
        byte[] message = LCPMessage.getOutputStateMessage(motor);
        sendMessageAndState(message);
    }

    public void requestFirmwareVersion() {
        byte[] message = LCPMessage.getFirmwareVersionMessage();
        sendMessageAndState(message);
    }

    public void findFiles(boolean findFirst, int handle) {
        byte[] message = LCPMessage.getFindFilesMessage(findFirst, handle, "*.*");
        sendMessageAndState(message);
    }
    
    public void setInputMode(int port, byte sensorType, byte sensorMode) {
    	byte[] message = LCPMessage.getInputModeMessage(port, sensorType, sensorMode);
    	sendMessageAndState(message);
    }
    
    public void requestInputValues(int port) {
    	byte[] message = LCPMessage.getInputValuesMessage(port);
    	sendMessageAndState(message);
    }
    
    public void LSWrite(int port, byte[] data, int expectedBytes) {
    	byte[] message = LCPMessage.getLSWriteMessage(port, expectedBytes, data.length, data);
    	sendMessageAndState(message);
    }
    
    public void LSGetStatus(int port) {
    	byte[] message = LCPMessage.getLSGetStatusMessage(port);
    	sendMessageAndState(message);
    }
    
    public void LSRead(int port) {
    	byte[] message = LCPMessage.getLSReadMessage(port);
    	sendMessageAndState(message);
    }
    
    public void resetInputScale(int port) {
    	byte[] message = LCPMessage.getResetInputScaledValueMessage(port);
    	sendMessageAndState(message);
    }
    
    public void resetMotorPosition(int motor, boolean relative) {
    	byte[] message = LCPMessage.getResetMessage(motor, relative);
    	sendMessageAndState(message);
    }
    
    public void requestBatteryLevel() {
    	byte[] message = LCPMessage.getBatteryLevelMessage();
    	sendMessageAndState(message);
    }

    private void sendToast(String toastText) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", NXTTypes.DISPLAY_TOAST);
        myBundle.putString("toastText", toastText);
        Utils.sendBundle(receiveHandler, myBundle);
    }

    private void sendStateAndData(int message, byte[] data) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", message);
        RawDataMsg msgData = new RawDataMsg(data);
        Utils.sendDataBundle(receiveHandler, myBundle, msgData);
    }

    private void sendState(int message) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", message);
        Utils.sendBundle(receiveHandler, myBundle);
    }
}
