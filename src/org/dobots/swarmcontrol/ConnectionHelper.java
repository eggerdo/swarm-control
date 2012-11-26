package org.dobots.swarmcontrol;

import org.dobots.robots.RobotDevice;
import org.dobots.robots.RobotDeviceFactory;
import org.dobots.robots.dotty.Dotty;
import org.dobots.robots.nxt.NXT;
import org.dobots.robots.parrot.Parrot;
import org.dobots.robots.roboscooper.RoboScooper;
import org.dobots.robots.roomba.Roomba;
import org.dobots.robots.spykee.Spykee;
import org.dobots.swarmcontrol.robots.BluetoothRobot;
import org.dobots.swarmcontrol.robots.RobotView;
import org.dobots.swarmcontrol.robots.RobotViewFactory;
import org.dobots.swarmcontrol.robots.WifiRobot;
import org.dobots.swarmcontrol.robots.dotty.DottyRobot;
import org.dobots.swarmcontrol.robots.nxt.NXTBluetooth;
import org.dobots.swarmcontrol.robots.nxt.NXTRobot;
import org.dobots.swarmcontrol.robots.parrot.ParrotRobot;
import org.dobots.swarmcontrol.robots.roboscooper.RoboScooperRobot;
import org.dobots.swarmcontrol.robots.roomba.RoombaRobot;
import org.dobots.swarmcontrol.robots.spykee.SpykeeRobot;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ConnectionHelper {
	
	public static boolean establishConnection(BaseActivity i_oActivity, RobotDevice i_oRobot, ConnectListener i_oListener) {
		
		switch(i_oRobot.getType()) {
		case RBT_DOTTY:
		case RBT_NXT:
		case RBT_ROOMBA:
		case RBT_ROBOSCOOPER:
			return establishBluetoothConnection(i_oActivity, i_oRobot, i_oListener);
		case RBT_PARROT:
		case RBT_SPYKEE:
			return establishWifiConnection(i_oActivity, i_oRobot, i_oListener);
		}

		return false;
		
	}
	
	public static boolean establishBluetoothConnection(final BaseActivity i_oActivity, final RobotDevice i_oRobot, final ConnectListener i_oListener) {
		
		final BluetoothConnectionHelper oBTHelper = new BluetoothConnectionHelper(i_oActivity, RobotViewFactory.getRobotAddressFilter(i_oRobot.getType()));
		oBTHelper.SetOnConnectListener(new BluetoothConnectionListener() {
			
			@Override
			public void connectToRobot(BluetoothDevice i_oDevice) {
				
				try {
					ConnectionHelper.connectToBluetoothRobot(i_oActivity, i_oRobot, i_oDevice, i_oListener);
				} catch (Exception e) {
					Toast.makeText(i_oActivity, "Robot not available", Toast.LENGTH_LONG);
				}
			}
		});
		
		if (oBTHelper.initBluetooth()) {
			oBTHelper.selectRobot();
		}
		
		return true;
	}
	
	public static boolean establishWifiConnection(final BaseActivity i_oActivity, final RobotDevice i_oRobot, final ConnectListener i_oListener) {
		
		final WifiConnectionHelper oWifiHelper = new WifiConnectionHelper(i_oActivity, RobotViewFactory.getRobotAddressFilter(i_oRobot.getType()));
		
		if (oWifiHelper.initWifi()) {
			try {
				ConnectionHelper.connectToWifiRobot(i_oActivity, i_oRobot, i_oListener);
			} catch (Exception e) {
				Toast.makeText(i_oActivity, "Robot not available", Toast.LENGTH_LONG);
			}
		}
		
		return true;
	}
	

	public static void connectToBluetoothRobot(BaseActivity context, RobotDevice oRobot,
			BluetoothDevice i_oDevice, final ConnectListener oListener) throws Exception {
		switch (oRobot.getType()) {
		case RBT_NXT:
			NXTRobot.connectToNXT(context, (NXT)oRobot, i_oDevice, oListener);
			break;
		case RBT_ROOMBA:
			RoombaRobot.connectToRoomba(context, (Roomba)oRobot, i_oDevice, oListener);
			break;
		case RBT_DOTTY:
			DottyRobot.connectToDotty(context, (Dotty)oRobot, i_oDevice, oListener);
			break;
		case RBT_ROBOSCOOPER:
			RoboScooperRobot.connectToRoboScooper(context, (RoboScooper)oRobot, i_oDevice, oListener);
			break;
		default:
			throw new Exception();
		}
	}

	private static void connectToWifiRobot(BaseActivity context, RobotDevice oRobot,
			ConnectListener oListener) throws Exception {
		switch (oRobot.getType()) {
		case RBT_PARROT:
			ParrotRobot.connectToARDrone(context, (Parrot)oRobot, oListener);
		case RBT_SPYKEE:
			SpykeeRobot.connectToSpykee(context, (Spykee)oRobot, oListener);
		default:
			throw new Exception();
		}
	}


}
