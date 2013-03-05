package org.dobots.robots.spykee;

import org.dobots.robots.MessageTypes;

public class SpykeeMessageTypes {

	public static final int LOGIN_ERROR = MessageTypes.USER;

	public static final int AUDIO_RECEIVED = LOGIN_ERROR + 1;
	public static final int VIDEO_FRAME_RECEIVED = AUDIO_RECEIVED + 1;
	public static final int BATTERY_LEVEL_RECEIVED = VIDEO_FRAME_RECEIVED + 1;
	public static final int DOCKINGSTATE_RECEIVED = BATTERY_LEVEL_RECEIVED + 1;

}
