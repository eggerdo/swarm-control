/**
 *   Copyright 2010, 2011 Guenther Hoelzl, Shawn Brown
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

package org.dobots.robots.nxt;

import org.dobots.utilities.Utils;



/**
 * Class for composing the proper messages for simple
 * communication over bluetooth
 */
public class LCPMessage {

    // the folowing constants were taken from the leJOS project (http://www.lejos.org) 
    
    // Command types constants. Indicates type of packet being sent or received.
    public static byte DIRECT_COMMAND_REPLY = 0x00;
    public static byte SYSTEM_COMMAND_REPLY = 0x01;
    public static byte REPLY_COMMAND = 0x02;
    public static byte DIRECT_COMMAND_NOREPLY = (byte)0x80; // Avoids ~100ms latency
    public static byte SYSTEM_COMMAND_NOREPLY = (byte)0x81; // Avoids ~100ms latency

    // Direct Commands
    public static final byte START_PROGRAM = 0x00;
    public static final byte STOP_PROGRAM = 0x01;
    public static final byte PLAY_SOUND_FILE = 0x02;
    public static final byte PLAY_TONE = 0x03;
    public static final byte SET_OUTPUT_STATE = 0x04;
    public static final byte SET_INPUT_MODE = 0x05;
    public static final byte GET_OUTPUT_STATE = 0x06;
    public static final byte GET_INPUT_VALUES = 0x07;
    public static final byte RESET_SCALED_INPUT_VALUE = 0x08;
    public static final byte MESSAGE_WRITE = 0x09;
    public static final byte RESET_MOTOR_POSITION = 0x0A;   
    public static final byte GET_BATTERY_LEVEL = 0x0B;
    public static final byte STOP_SOUND_PLAYBACK = 0x0C;
    public static final byte KEEP_ALIVE = 0x0D;
    public static final byte LS_GET_STATUS = 0x0E;
    public static final byte LS_WRITE = 0x0F;
    public static final byte LS_READ = 0x10;
    public static final byte GET_CURRENT_PROGRAM_NAME = 0x11;
    public static final byte MESSAGE_READ = 0x13;
    
    // NXJ additions
    public static byte NXJ_DISCONNECT = 0x20; 
    public static byte NXJ_DEFRAG = 0x21;
    
    // MINDdroidConnector additions
    public static final byte SAY_TEXT = 0x30;
    public static final byte VIBRATE_PHONE = 0x31;
    public static final byte ACTION_BUTTON = 0x32;
    
    // System Commands:
    public static final byte OPEN_READ = (byte)0x80;
    public static final byte OPEN_WRITE = (byte)0x81;
    public static final byte READ = (byte)0x82;
    public static final byte WRITE = (byte)0x83;
    public static final byte CLOSE = (byte)0x84;
    public static final byte DELETE = (byte)0x85;        
    public static final byte FIND_FIRST = (byte)0x86;
    public static final byte FIND_NEXT = (byte)0x87;
    public static final byte GET_FIRMWARE_VERSION = (byte)0x88;
    public static final byte OPEN_WRITE_LINEAR = (byte)0x89;
    public static final byte OPEN_READ_LINEAR = (byte)0x8A;
    public static final byte OPEN_WRITE_DATA = (byte)0x8B;
    public static final byte OPEN_APPEND_DATA = (byte)0x8C;
    public static final byte BOOT = (byte)0x97;
    public static final byte SET_BRICK_NAME = (byte)0x98;
    public static final byte GET_DEVICE_INFO = (byte)0x9B;
    public static final byte DELETE_USER_FLASH = (byte)0xA0;
    public static final byte POLL_LENGTH = (byte)0xA1;
    public static final byte POLL = (byte)0xA2;
    
    public static final byte NXJ_FIND_FIRST = (byte)0xB6;
    public static final byte NXJ_FIND_NEXT = (byte)0xB7;
    public static final byte NXJ_PACKET_MODE = (byte)0xff;
    
    // Sensor Type
    public static final byte NO_SENSOR = (byte)0x00;
    public static final byte SWITCH = (byte)0x01;
    public static final byte TEMPERATURE = (byte)0x02;
    public static final byte REFLECTION = (byte)0x03;
    public static final byte ANGLE = (byte)0x04;
    public static final byte LIGHT_ACTIVE = (byte)0x05;
    public static final byte LIGHT_INACTIVE = (byte)0x06;
    public static final byte SOUND_DB = (byte)0x07;
    public static final byte SOUND_DBA = (byte)0x08;
    public static final byte CUSTOM = (byte)0x09;
    public static final byte LOWSPEED = (byte)0x0A;
    public static final byte LOWSPEED_9V = (byte)0x0B;
    public static final byte NO_OF_SENSOR_TYPES = (byte)0x0C;
    
    // Sensore Mode
    public static final byte RAWMODE = (byte)0x00;
    public static final byte BOOLEANMODE = (byte)0x20;
    public static final byte TRANSITIONCNTMODE = (byte)0x40;
    public static final byte PERIODCOUNTERMODE = (byte)0x60;
    public static final byte PCTFULLSCALEMODE = (byte)0x80;
    public static final byte CELSIUSMODE = (byte)0xA0;
    public static final byte FAHRENHEITMODE = (byte)0xC0;
    public static final byte ANGLESTEPSMODE = (byte)0xE0;
    public static final byte SLOPEMASK = (byte)0x1F;
    public static final byte MODEMASK = (byte)0xE0;
    
    // Error codes    
    public static final byte SUCCESS = (byte)0x00;
    public static final byte TRANSACTION_IN_PROGRESS = (byte)0x20;
    public static final byte MAILBOX_EMPTY = (byte)0x40;
    public static final byte FILE_NOT_FOUND = (byte)0x86;
    public static final byte UNDEFINED_ERROR = (byte) 0x8A;
    public static final byte REQUEST_FAILED = (byte) 0xBD;
    public static final byte UNKNOWN_COMMAND = (byte) 0xBE;
    public static final byte INSANE_PACKET = (byte) 0xBF;
    public static final byte OUT_OF_RANGE = (byte) 0xC0;
    public static final byte COMMUNICATION_BUS_ERROR = (byte) 0xDD;
    public static final byte NO_FREE_COMMUNICATION_MEMORY = (byte) 0xDE;
    public static final byte CHANNEL_NOT_VALID = (byte) 0xDF;
    public static final byte CHANNEL_BUSY = (byte) 0xE0;
    public static final byte NO_ACTIVE_PROGRAM = (byte) 0xEC;
    public static final byte ILLEGAL_SIZE = (byte) 0xED;
    public static final byte ILLEGAL_MAILBOX_ID = (byte) 0xEE;
    public static final byte ACCESS_ERROR = (byte) 0xEF;
    public static final byte BAD_INPUT_OUTPUT = (byte) 0xF0;
    public static final byte INSUFFICIENT_MEMORY = (byte) 0xFB;
    public static final byte DIRECTORY_FULL = (byte) 0xFC;
    public static final byte NOT_IMPLEMENTED = (byte) 0xFD;
    public static final byte BAD_ARGUMENTS = (byte) 0xFF;

    // Firmware codes
    public static byte[] FIRMWARE_VERSION_LEJOSMINDDROID = { 0x6c, 0x4d, 0x49, 0x64 };

    
    ///////////////////////////////////////////////////////////////////
    // Direct Commands
    ///////////////////////////////////////////////////////////////////

    
    public static byte[] getStartProgramMessage(String programName) {
        byte[] message = new byte[22];

        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = START_PROGRAM;

        // copy programName and end with 0 delimiter
//        for (int pos=0; pos<programName.length(); pos++)
//            message[2+pos] = (byte) programName.charAt(pos);
        System.arraycopy(Utils.stringToByteArray(programName), 0, message, 2, programName.length());

        message[programName.length()+2] = 0;

        return message;
    }

    public static byte[] getStopProgramMessage() {
        byte[] message = new byte[2];

        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = STOP_PROGRAM;

        return message;
    }
    
    public static byte[] getPlaySoundFileMessage(boolean loop, String fileName) {
    	byte[] message = new byte[23];
    	
    	message[0] = DIRECT_COMMAND_NOREPLY;
    	message[1] = PLAY_SOUND_FILE;
    	message[2] = loop ? (byte) 1 : 0;

    	// copy the 
        System.arraycopy(Utils.stringToByteArray(fileName), 0, message, 3, fileName.length());
    	
        return message;
    }
    
    public static byte[] getBeepMessage(int frequency, int duration) {
        byte[] message = new byte[6];

        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = PLAY_TONE;
        // Frequency for the tone, Hz (UWORD); Range: 200-14000 Hz
        message[2] = (byte) frequency;
        message[3] = (byte) (frequency >> 8);
        // Duration of the tone, ms (UWORD)
        message[4] = (byte) duration;
        message[5] = (byte) (duration >> 8);

        return message;
    }
    
    public static byte[] getMotorMessage(int motor, int speed) {
        byte[] message = new byte[12];

        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = SET_OUTPUT_STATE;
        // Output port
        message[2] = (byte) motor;

        if (speed == 0) {
            message[3] = 0;
            message[4] = 0;
            message[5] = 0;
            message[6] = 0;
            message[7] = 0;

        } else {
            // Power set option (Range: -100 - 100)
            message[3] = (byte) speed;
            // Mode byte (Bit-field): MOTORON + BREAK
            message[4] = 0x03;
            // Regulation mode: REGULATION_MODE_MOTOR_SPEED
            message[5] = 0x01;
            // Turn Ratio (SBYTE; -100 - 100)
            message[6] = 0x00;
            // RunState: MOTOR_RUN_STATE_RUNNING
            message[7] = 0x20;
        }

        // TachoLimit: run forever
        message[8] = 0;
        message[9] = 0;
        message[10] = 0;
        message[11] = 0;

        return message;

    }

    public static byte[] getMotorMessage(int motor, int speed, int end) {
        byte[] message = getMotorMessage(motor, speed);

        // TachoLimit
        message[8] = (byte) end;
        message[9] = (byte) (end >> 8);
        message[10] = (byte) (end >> 16);
        message[11] = (byte) (end >> 24);

        return message;
    }

    public static byte[] getInputModeMessage(int port, int sensorType, int sensorMode) {
    	byte[] message = new byte[5];
    	
    	message[0] = DIRECT_COMMAND_NOREPLY;
    	message[1] = SET_INPUT_MODE;
    	
    	// input port
    	message[2] = (byte) port;
    	// sensor type
    	message[3] = (byte) sensorType;
    	// sensor mode
    	message[4] = (byte) sensorMode;
    	
    	return message;
    }

    public static byte[] getOutputStateMessage(int motor) {
        byte[] message = new byte[3];

        message[0] = DIRECT_COMMAND_REPLY;
        message[1] = GET_OUTPUT_STATE;
        // Output port
        message[2] = (byte) motor;

        return message;
    }

    public static byte[] getInputValuesMessage(int port) {
    	byte[] message = new byte[3];
    	
    	message[0] = DIRECT_COMMAND_REPLY;
    	message[1] = GET_INPUT_VALUES;
    	
    	// input port
    	message[2] = (byte) port;
    	
    	return message;
    }
    
    public static byte[] getResetInputScaledValueMessage(int port) {
    	byte[] message = new byte[3];
    	
    	message[0] = DIRECT_COMMAND_NOREPLY;
    	message[1] = RESET_SCALED_INPUT_VALUE;
    	
    	// input port
    	message[2] = (byte) port;
    	
    	return message;
    }
    
    public static byte[] getMessageWriteMessage(int inbox, byte[] data) {
    	byte[] message = new byte[data.length + 4];
    	
    	message[0] = DIRECT_COMMAND_NOREPLY;
    	message[1] = MESSAGE_WRITE;
    	
    	// inbox number
    	message[2] = (byte) inbox;
    	// message size
    	message[3] = (byte) data.length;
    	// message
    	System.arraycopy(data, 0, message, 4, data.length);
    	
    	return message;
    }

    public static byte[] getResetMessage(int motor, boolean relative) {
        byte[] message = new byte[4];

        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = RESET_MOTOR_POSITION;
        // Output port
        message[2] = (byte) motor;
        // absolute position
        message[3] = relative ? (byte) 1 : 0;

        return message;
    }
    
    public static byte[] getBatteryLevelMessage() {
    	byte[] message = new byte[2];
    	
    	message[0] = DIRECT_COMMAND_REPLY;
    	message[1] = GET_BATTERY_LEVEL;
    	
    	return message;
    }
    
    public static byte[] getStopSoundPlaybackMessage() {
    	byte[] message = new byte[2];
    	
    	message[0] = DIRECT_COMMAND_NOREPLY;
    	message[1] = STOP_SOUND_PLAYBACK;
    	
    	return message;
    }
    
    public static byte[] getKeepAliveMessage() {
    	byte[] message = new byte[2];
    	
    	message[0] = DIRECT_COMMAND_NOREPLY;
    	message[1] = KEEP_ALIVE;
    	
    	return message;
    }
    
    public static byte[] getLSGetStatusMessage(int port) {
    	byte[] message = new byte[3];
    	
    	message[0] = DIRECT_COMMAND_REPLY;
    	message[1] = LS_GET_STATUS;
    	
    	// port number
    	message[2] = (byte) port;
    	
    	return message;
    }
    
    public static byte[] getLSWriteMessage(int port, int rx_length, int tx_length, byte[] tx_data) {
    	byte[] message = new byte[tx_data.length + 5];
    	
    	message[0] = DIRECT_COMMAND_NOREPLY;
    	message[1] = LS_WRITE;
    	
    	// port number
    	message[2] = (byte) port;
    	// tx data length
    	message[3] = (byte) tx_length;
    	// rx data length
    	message[4] = (byte) rx_length;
    	// tx data
    	System.arraycopy(tx_data, 0, message, 5, tx_length);
    	
    	return message;
    }
    
    public static byte[] getLSReadMessage(int port) {
    	byte[] message = new byte[3];
    	
    	message[0] = DIRECT_COMMAND_REPLY;
    	message[1] = LS_READ;
    	
    	// port number
    	message[2] = (byte) port;
    	
    	return message;
    }

    public static byte[] getProgramNameMessage() {
        byte[] message = new byte[2];

        message[0] = DIRECT_COMMAND_REPLY;
        message[1] = GET_CURRENT_PROGRAM_NAME;

        return message;
    }
    
    public static byte[] getMessageRead(int remote_inbox, int local_inbox, boolean remove) {
    	byte[] message = new byte[5];
    	
    	message[0] = DIRECT_COMMAND_REPLY;
    	message[1] = MESSAGE_READ;
    	
    	// remote inbox number
    	message[2] = (byte) remote_inbox;
    	// local inbox number
    	message[3] = (byte) local_inbox;
    	// remove?
    	message[4] = remove ? (byte) 1 : 0;
    	
    	return message;
    }

    public static byte[] getActionMessage(int actionNr) {
        byte[] message = new byte[3];

        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = ACTION_BUTTON;
        message[2] = (byte) actionNr;
        return message;
    }   

    public static byte[] getFirmwareVersionMessage() {
        byte[] message = new byte[2];

        message[0] = SYSTEM_COMMAND_REPLY;
        message[1] = GET_FIRMWARE_VERSION;

        return message;
    }

    public static byte[] getFindFilesMessage(boolean findFirst, int handle, String searchString) {
        byte[] message;

        if (findFirst)
            message = new byte[22];

        else
            message = new byte[3];

        message[0] = SYSTEM_COMMAND_REPLY;

        if (findFirst) {
            message[1] = FIND_FIRST;

            // copy searchString and end with 0 delimiter
            for (int pos=0; pos<searchString.length(); pos++)
                message[2+pos] = (byte) searchString.charAt(pos);

            message[searchString.length()+2] = 0;

        } else {
            message[1] = FIND_NEXT;
            message[2] = (byte) handle;
        }

        return message;
    }

    public static byte[] getOpenWriteMessage(String fileName, int fileLength) {
        byte[] message = new byte[26];

        message[0] = SYSTEM_COMMAND_REPLY;
        message[1] = OPEN_WRITE;
        
        // copy programName and end with 0 delimiter
        for (int pos=0; pos<fileName.length(); pos++)
            message[2+pos] = (byte) fileName.charAt(pos);

        message[fileName.length()+2] = 0;
        // copy file size
        message[22] = (byte) fileLength;
        message[23] = (byte) (fileLength >>> 8);
        message[24] = (byte) (fileLength >>> 16);
        message[25] = (byte) (fileLength >>> 24);        
        return message;
    }

    public static byte[] getWriteMessage(int handle, byte[] data, int dataLength) {
        byte[] message = new byte[dataLength + 3];

        message[0] = SYSTEM_COMMAND_REPLY;
        message[1] = WRITE;
        
        // copy handle
        message[2] = (byte) handle;
        // copy data
        System.arraycopy(data, 0, message, 3, dataLength);

        return message;
    }
    
    public static byte[] getCloseMessage(int handle) {
        byte[] message = new byte[3];

        message[0] = SYSTEM_COMMAND_REPLY;
        message[1] = CLOSE;
        
        // copy handle
        message[2] = (byte) handle;

        return message;
    }
   
}
