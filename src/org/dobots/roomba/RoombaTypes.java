package org.dobots.roomba;

import java.util.Arrays;
import java.util.UUID;

import org.dobots.utility.Utils;

public class RoombaTypes {

	public static final UUID ROOMBA_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	public static String MAC_FILTER = "00:06:66";

    public static int MAX_SPEED = 100;
	
	public static int MAX_VELOCITY = 500; // -500 - 500 mm/s
	public static int MAX_RADIUS = 2000;  // -2000 - 2000 mm/s
	public static int STRAIGHT = 32768;
	public static int CLOCKWISE = -1;
	public static int COUNTER_CLOCKWISE = 1;
	
	public enum ERoombaModes {
		mod_Unknown,
		mod_Passive,
		mod_Safe,
		mod_Full,
		mod_PowerOff
	}
	
	public enum ERoombaBaudRates {
		baud_300(0),
		baud_600(1),
		baud_1200(2),
		baud_2400(3),
		baud_4800(4),
		baud_9600(5),
		baud_14400(6),
		baud_19200(7),
		baud_28800(8),
		baud_38400(9),
		baud_57600(10),
		baud_115200(11);
		private int id;
		
		private ERoombaBaudRates(int id) {
			this.id = id;
		}
		
		public int getID() {
			return id;
		}
	}
	
	public enum ERoombaMotors {
		motor_SideBrush(0),
		motor_Vacuum(1),
		motor_MainBrush(2);
		private int id;
		
		private ERoombaMotors(int id) {
			this.id = id;
		}
		
		public int getID() {
			return id;
		}
	}
	
	public enum ERoombaOnOffLEDs {
		led_DirtDetect(0),
		led_Max(1),
		led_Clean(2),
		led_Spot(3);
		private int id;
		
		private ERoombaOnOffLEDs(int id) {
			this.id = id;
		}
		
		public int getID() {
			return id;
		}
	}
	
	public static final int STATUS_LED_LOW_BIT 	= 4;
	public static final int STATUS_LED_HIGH_BIT	= 5;
	
	public enum ERoombaStatusLEDColours {
		ledCol_Red,
		ledCol_Green,
		ledCol_Amber
	}
	
	public static final int POWER_LED_GREEN = 0;
	public static final int POWER_LED_RED	= 255;
	
	public enum ERoombaSensorPackages {
		sensPkg_None(-1, "Nothing"),
//		sensPkg_All(0, "Everything"),
		sensPkg_1(1, "Environment"),
		sensPkg_2(2, "Actuators"),
		sensPkg_3(3, "Power");
		private int id;
		private String strName;
		
		private ERoombaSensorPackages(int id, String name) {
			this.id = id;
			this.strName = name;
		}
		
		public int getID() {
			return id;
		}
		
		public String toString() {
			return strName;
		}
	}
	
	public class BumpsWheeldrops {
		private static final int CASTER_WHEELDROP 	= 4;
		private static final int LEFT_WHEELDROP 	= 3;
		private static final int RIGHT_WHEELDROP 	= 2;
		private static final int LEFT_BUMP 			= 1;
		private static final int RIGHT_BUMP		 	= 0;
		
		public boolean bCaster_Wheeldrop;
		public boolean bLeft_Wheeldrop;
		public boolean bRight_Wheeldrop;
		public boolean bLeft_Bump;
		public boolean bRight_Bump;
	
		BumpsWheeldrops(int i_nVal) {
			bCaster_Wheeldrop 	= Utils.IsBitSet(i_nVal, CASTER_WHEELDROP);
			bLeft_Wheeldrop		= Utils.IsBitSet(i_nVal, LEFT_WHEELDROP);
			bRight_Wheeldrop	= Utils.IsBitSet(i_nVal, RIGHT_WHEELDROP);
			bLeft_Bump			= Utils.IsBitSet(i_nVal, LEFT_BUMP);
			bRight_Bump			= Utils.IsBitSet(i_nVal, RIGHT_BUMP);
		}

		public String toString() {
			return "Caster_Wheeldrop=" + bCaster_Wheeldrop + ", " +
				   "Left_Wheeldrop=" + bLeft_Wheeldrop + ", " +
				   "Right_Wheeldrop=" + bRight_Wheeldrop + ", " +
				   "Left_Bump=" + bLeft_Bump + ", " +
				   "Right_Bump=" + bRight_Bump;
		}
	}
	
	public class MotorOvercurrents {
		private static final int DRIVE_LEFT		= 4;
		private static final int DRIVE_RIGHT	= 3;
		private static final int MAIN_BRUSH		= 2;
		private static final int VACUUM			= 1;
		private static final int SIDE_BRUSH		= 0;
		
		public boolean bDriveLeft;
		public boolean bDriveRight;
		public boolean bMainBrush;
		public boolean bVacuum;
		public boolean bSideBrush;
		
		MotorOvercurrents(int i_nVal) {
			bDriveLeft	= Utils.IsBitSet(i_nVal, DRIVE_LEFT);
			bDriveRight	= Utils.IsBitSet(i_nVal, DRIVE_RIGHT);
			bMainBrush	= Utils.IsBitSet(i_nVal, MAIN_BRUSH);
			bVacuum		= Utils.IsBitSet(i_nVal, VACUUM);
			bSideBrush	= Utils.IsBitSet(i_nVal, SIDE_BRUSH);
		}

		public String toString() {
			return "DriveLeft=" + bDriveLeft + ", " +
				   "DriveRight=" + bDriveRight + ", " +
				   "MainBrush=" + bMainBrush + ", " +
				   "Vacuum=" + bVacuum + ", " +
				   "bSideBrush=" + bSideBrush;
		}
	}
	
	public class ButtonsPressed {
		private static final int POWER	= 3;
		private static final int SPOT	= 2;
		private static final int CLEAN	= 1;
		private static final int MAX	= 0;
		
		public boolean bPower;
		public boolean bSpot;
		public boolean bClean;
		public boolean bMax;
		
		ButtonsPressed(int i_nVal) {
			bPower	= Utils.IsBitSet(i_nVal, POWER);
			bSpot	= Utils.IsBitSet(i_nVal, SPOT);
			bClean	= Utils.IsBitSet(i_nVal, CLEAN);
			bMax	= Utils.IsBitSet(i_nVal, MAX);
		}
		
		public String toString() {
			return "Power=" + bPower + ", " +
				   "Spot=" + bSpot + ", " +
				   "Clean=" + bClean + ", " +
				   "Max=" + bMax;
		}
	}
	
	public enum EChargingState {
		chg_notCharging("Not Charging"),
		chg_chargingRecovery("Recovery Charging"),
		chg_charging("Charging"),
		chg_trickleCharging("Trickle Charging"),
		chg_Waiting("Waiting"),
		chg_ChargingError("Charging Error"),
		chg_Unknown("Unknown");
		private String strName;
		
		EChargingState(String i_strName) {
			this.strName = i_strName;
		}
		
		public static EChargingState OrdToEnum(int i_nVal) {
			try {
				return EChargingState.values()[i_nVal];
			} catch (ArrayIndexOutOfBoundsException e) {
				return chg_Unknown;
			}
		}
		
		public String toString() {
			return strName;
		}
	}
	
	public interface SensorPackage{
		public String toString();
	};
	
	public class SensorPackage1 implements SensorPackage {
		public static final int IDX_BUMPWHEELDROPS		= 0;
		public static final int IDX_WALL				= 1;
		public static final int IDX_CLIFFLEFT			= 2;
		public static final int IDX_CLIFFFRONTLEFT		= 3;
		public static final int IDX_CLIFFFRONTRIGHT		= 4;
		public static final int IDX_CLIFFRIGHT			= 5;
		public static final int IDX_VIRTUALWALL			= 6;
		public static final int IDX_MOTOROVERCURRENTS	= 7;
		public static final int IDX_DIRTDETECTIONLEFT	= 8;
		public static final int IDX_DIRTDETECTIONRIGHT	= 9;
		
		public BumpsWheeldrops oBumpsWheeldrops;
		public boolean bWall;
		public boolean bCliffLeft;
		public boolean bCliffFrontLeft;
		public boolean bCliffFrontRight;
		public boolean bCliffRight;
		public boolean bVirtualWall;
		public MotorOvercurrents oMotorOvercurrents;
		public byte byDirtDetectionLeft;
		public byte byDirtDetectionRight;
		
		public SensorPackage1(byte[] i_rgbyValues) {
			for (int i = 0; i < i_rgbyValues.length; i++) {
				switch (i) {
					case IDX_BUMPWHEELDROPS:
						oBumpsWheeldrops = new BumpsWheeldrops(i_rgbyValues[i]);
						break;
					case IDX_WALL:
						bWall = Utils.IsBitSet(i_rgbyValues[i], 0);
						break;
					case IDX_CLIFFLEFT:
						bCliffLeft = Utils.IsBitSet(i_rgbyValues[i], 0);
						break;
					case IDX_CLIFFFRONTLEFT:
						bCliffFrontLeft = Utils.IsBitSet(i_rgbyValues[i], 0);
						break;
					case IDX_CLIFFFRONTRIGHT:
						bCliffFrontRight = Utils.IsBitSet(i_rgbyValues[i], 0);
						break;
					case IDX_CLIFFRIGHT:
						bCliffRight = Utils.IsBitSet(i_rgbyValues[i], 0);
						break;
					case IDX_VIRTUALWALL:
						bVirtualWall = Utils.IsBitSet(i_rgbyValues[i], 0);
						break;
					case IDX_MOTOROVERCURRENTS:
						oMotorOvercurrents = new MotorOvercurrents(i_rgbyValues[i]);
						break;
					case IDX_DIRTDETECTIONLEFT:
						byDirtDetectionLeft = i_rgbyValues[i];
						break;
					case IDX_DIRTDETECTIONRIGHT:
						byDirtDetectionRight = i_rgbyValues[i];
						break;
					default:
						throw new IndexOutOfBoundsException("Array has too many fields");
				}
			}
		}

		public String toString() {
			return oBumpsWheeldrops.toString() + ", " +
			       "Wall=" + bWall + ", " +
			       "CliffLeft=" + bCliffLeft + ", " +
			       "CliffFrontLeft=" + bCliffFrontLeft + ", " +
			       "CliffFrontRight=" + bCliffFrontRight + ", " +
			       "CliffRight=" + bCliffRight + ", " +
			       "VirtualWall=" + bVirtualWall + ", " +
			       oMotorOvercurrents.toString() + ", " +
			       "DirtDetectionLeft=" + byDirtDetectionLeft + ", " +
			       "DirtDetectionRight=" + byDirtDetectionRight;
		}
	}
	
	public class SensorPackage2 implements SensorPackage {
		private static final int IDX_REMOTEOPCODE		= 0;
		private static final int IDX_BUTTONSPRESSED		= 1;
		private static final int IDX_DISTANCE			= 2; // 2 bytes
		private static final int IDX_ANGLE				= 4; // 2 bytes
		
		public byte byRemoteOpCode;
		public ButtonsPressed oButtonsPressed;
		public short sDistance;
		public short sAngle;
		
		public SensorPackage2(byte[] i_rgbyValues) {
			for (int i = 0; i < i_rgbyValues.length; i++) {
				switch (i) {
					case IDX_REMOTEOPCODE:
						byRemoteOpCode = i_rgbyValues[i];
						break;
					case IDX_BUTTONSPRESSED:
						oButtonsPressed = new ButtonsPressed(i_rgbyValues[i]);
						break;
					case IDX_DISTANCE:
						sDistance = Utils.HighLowByteToShort(i_rgbyValues[i], i_rgbyValues[i+1]);
						break;
					case IDX_DISTANCE+1:
						break;
					case IDX_ANGLE:
						sAngle = Utils.HighLowByteToShort(i_rgbyValues[i], i_rgbyValues[i+1]);
					case IDX_ANGLE+1:
						break;
					default:
						throw new IndexOutOfBoundsException("Array has too many fields");
				}
			}
		}

		public String toString() {
			return "RemoteOpCode=" + byRemoteOpCode + ", " +
				   oButtonsPressed.toString() + ", " +
				   "Distance=" + sDistance + ", " +
				   "Angle=" + sAngle;
		}
	}
	
	public class SensorPackage3 implements SensorPackage {
		private static final int IDX_CHARGINGSTATE		= 0;
		private static final int IDX_VOLTAGE			= 1; // 2 bytes
		private static final int IDX_CURRENT			= 3; // 2 bytes
		private static final int IDX_TEMPERATURE		= 5;
		private static final int IDX_CHARGE				= 6; // 2 bytes
		private static final int IDX_CAPACITY			= 8; // 2 bytes
		
		public EChargingState eChargingState;
		public short sVoltage;
		public short sCurrent;
		public byte byTemperature;
		public short sCharge;
		public short sCapacity;
		
		public SensorPackage3(byte[] i_rgbyValues) {
			for (int i = 0; i < i_rgbyValues.length; i++) {
				switch (i) {
					case IDX_CHARGINGSTATE:
						eChargingState = EChargingState.OrdToEnum(i_rgbyValues[i]);
						break;
					case IDX_VOLTAGE:
						sVoltage = Utils.HighLowByteToShort(i_rgbyValues[i], i_rgbyValues[i+1]);
						break;
					case IDX_VOLTAGE+1:
						break;
					case IDX_CURRENT:
						sCurrent = Utils.HighLowByteToShort(i_rgbyValues[i], i_rgbyValues[i+1]);
						break;
					case IDX_CURRENT+1:
						break;
					case IDX_TEMPERATURE:
						byTemperature = i_rgbyValues[i];
						break;
					case IDX_CHARGE:
						sCharge = Utils.HighLowByteToShort(i_rgbyValues[i], i_rgbyValues[i+1]);
						break;
					case IDX_CHARGE+1:
						break;
					case IDX_CAPACITY:
						sCapacity = Utils.HighLowByteToShort(i_rgbyValues[i], i_rgbyValues[i+1]);
						break;
					case IDX_CAPACITY+1:
						break;
					default:
						throw new IndexOutOfBoundsException("Array has too many fields");
				}
			}
		}

		public String toString() {
			return "ChargingState=" + eChargingState + ", " +
				   "Voltage=" + sVoltage + ", " +
				   "Current=" + sCurrent + ", " +
				   "Temperature=" + byTemperature + ", " +
				   "Charge=" + sCharge + ", " +
				   "Capacity=" + sCapacity;
		}
	}
	
	public class SensorPackageAll implements SensorPackage {
		private static final int IDX_SENSORPACKAGE1_START 	= 0;
		private static final int IDX_SENSORPACKAGE1_END		= 9;
		private static final int IDX_SENSORPACKAGE2_START 	= 10;
		private static final int IDX_SENSORPACKAGE2_END		= 15;
		private static final int IDX_SENSORPACKAGE3_START 	= 16;
		private static final int IDX_SENSORPACKAGE3_END		= 25;
		
		
		SensorPackage1 oSensorPackage1;
		SensorPackage2 oSensorPackage2;
		SensorPackage3 oSensorPackage3;
		
		public SensorPackageAll(byte[] i_rgbyValues) {
			oSensorPackage1 = new SensorPackage1(Arrays.copyOfRange(i_rgbyValues, IDX_SENSORPACKAGE1_START, IDX_SENSORPACKAGE1_END));
			oSensorPackage2 = new SensorPackage2(Arrays.copyOfRange(i_rgbyValues, IDX_SENSORPACKAGE2_START, IDX_SENSORPACKAGE2_END));
			oSensorPackage3 = new SensorPackage3(Arrays.copyOfRange(i_rgbyValues, IDX_SENSORPACKAGE3_START, IDX_SENSORPACKAGE3_END));
		}
		
		public String toString() {
			return oSensorPackage1.toString() + ", " +
				   oSensorPackage2.toString() + ", " +
				   oSensorPackage3.toString();
		}
	}
	
	public static SensorPackage assembleSensorPackage(ERoombaSensorPackages i_ePackage, byte[] i_bySensorData) {
		RoombaTypes oRoombaTypes = new RoombaTypes();
		switch (i_ePackage) {
//			case sensPkg_All:
//				return oRoombaTypes.new SensorPackageAll(i_bySensorData);
			case sensPkg_1:
				return oRoombaTypes.new SensorPackage1(i_bySensorData);
			case sensPkg_2:
				return oRoombaTypes.new SensorPackage2(i_bySensorData);
			case sensPkg_3:
				return oRoombaTypes.new SensorPackage3(i_bySensorData);
			default:
				return null;
		}
	}

	/////////////////////////////////////////////////////////////////////////
	/// Private Functions
	/////////////////////////////////////////////////////////////////////////

}
