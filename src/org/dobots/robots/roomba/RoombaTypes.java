package org.dobots.robots.roomba;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import org.dobots.utilities.Utils;

public class RoombaTypes {

	public static final UUID ROOMBA_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	public static final String MAC_FILTER = "00:06:66";

    public static int MAX_SPEED = 100;
	
	public static int MAX_VELOCITY = 500; // -500 - 500 mm/s
//	public static int MAX_RADIUS = 2000;  // -2000 - 2000 mm/s
	public static int MAX_RADIUS = 1000;  // -2000 - 2000 mm/s
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
		sensPkg_1(1, "Environment"),
		sensPkg_2(2, "Actuators"),
		sensPkg_3(3, "Power"),
		sensPkg_All(100, "Everything");
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
	
	public class LightBumper {
		private static final int LT_BUMPER_RIGHT 		= 5;
		private static final int LT_BUMPER_FRONT_RIGHT 	= 4;
		private static final int LT_BUMPER_CENTER_RIGHT	= 3;
		private static final int LT_BUMPER_CENTER_lEFT 	= 2;
		private static final int LT_BUMPER_FRONT_LEFT 	= 1;
		private static final int LT_BUMPER_LEFT 		= 0;
		
		public boolean bLtBumperRight;
		public boolean bLtBumperFrontRight;
		public boolean bLtBumperCenterRight;
		public boolean bLtBumperCenterLeft;
		public boolean bLtBumperFrontLeft;
		public boolean bLtBumperLeft;
		
		LightBumper(int i_nVal) {
			bLtBumperRight			= Utils.IsBitSet(i_nVal, LT_BUMPER_RIGHT);
			bLtBumperFrontRight		= Utils.IsBitSet(i_nVal, LT_BUMPER_FRONT_RIGHT);
			bLtBumperCenterRight	= Utils.IsBitSet(i_nVal, LT_BUMPER_CENTER_RIGHT);
			bLtBumperCenterLeft		= Utils.IsBitSet(i_nVal, LT_BUMPER_CENTER_lEFT);
			bLtBumperFrontLeft		= Utils.IsBitSet(i_nVal, LT_BUMPER_FRONT_LEFT);
			bLtBumperLeft			= Utils.IsBitSet(i_nVal, LT_BUMPER_LEFT);
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

	public enum OIMode {
		oi_off("Off"),
		oi_passive("Passive"),
		oi_safe("Safe"),
		oi_full("Full"),
		oi_unknown("Unknown");
		private String strName;
		
		OIMode(String i_strName) {
			this.strName = i_strName;
		}
		
		public static OIMode OrdToEnum(int i_nVal) {
			try {
				return OIMode.values()[i_nVal];
			} catch (ArrayIndexOutOfBoundsException e) {
				return oi_unknown;
			}
		}
		
		public String toString() {
			return strName;
		}
	}
	
	public enum ChargeMode {
		chgmode_HomeBase("Home Base", 2),
		chgmode_Internal("Internal", 1),
		chgmode_None("None", 0),
		chgmode_Unknown("Unknown", -1);
		private String strName;
		private int nValue;
		
		ChargeMode(String i_strName, int i_nValue) {
			this.strName = i_strName;
			this.nValue = i_nValue;
		}
		
		public static ChargeMode valToEnum(int i_nVal) {
			for (ChargeMode eMode : ChargeMode.values()) {
				if (eMode.toValue() == i_nVal) {
					return eMode;
				}
			}
			return chgmode_Unknown;
		}
		
		public String toString() {
			return strName;
		}
		
		public int toValue() {
			return nValue;
		}
	}
	
	public enum IROpCode {
		irop_none("None", 0),
		irop_left("Left", 129),
		irop_forward("Forward", 130),
		irop_right("Right", 131),
		irop_spot("Spot", 132),
		irop_max("Max", 133),
		irop_small("Small", 134),
		irop_medium("Medium", 135),
		irop_clean("Clean", 136),
		irop_stop("Stop", 137),
		irop_power("Power", 138),
		irop_arcleft("Arc Left", 139),
		irop_arcright("Arc Right", 140),
		irop_stop2("Stop", 141),
		irop_download("Download", 142),
		irop_seekdock("Seek Dock", 143),
		irop_discovery_reserved("Reserved", 240),
		irop_discovery_redbuoy("Red Buoy", 248),
		irop_discovery_greenbuoy("Green Buoy", 244),
		irop_discovery_forcefield("Force Field", 242),
		irop_discovery_rbgb("Red Buoy and Green Buoy", 252),
		irop_discovery_rbff("Red Buoy and Force Field", 250),
		irop_discovery_gbff("Green Buoy and Force Field", 246),
		irop_discovery_rbgbff("Red Buoy, Green Buoy and Force Field", 254),
		irop_charge_reserved("Reserved", 160),
		irop_charge_redbuoy("Red Buoy", 168),
		irop_charge_greenbuoy("Green Buoy", 164),
		irop_charge_forcefield("Force Field", 161),
		irop_charge_rbgb("Red Buoy and Green Buoy", 172),
		irop_charge_rbff("Red Buoy and Force Field", 169),
		irop_charge_gbff("Green Buoy and Force Field", 165),
		irop_charge_rbgbff("Red Buoy, Green Buoy and Force Field", 173),
		irop_virtualwall("Virtual Wall", 162),
//		irop_lighthouse("...", ...)
		irop_unknown("Unknown", -1);
		private String strName;
		private int nValue;
		
		private IROpCode(String i_strName, int i_nVal) {
			strName = i_strName;
			nValue = i_nVal;
		}
		
		public String toString() {
			return strName;
		}
		
		public int toValue() {
			return nValue;
		}
		
		public static IROpCode valToEnum(int i_nVal) {
			for (IROpCode eCode : IROpCode.values()) {
				if (eCode.toValue() == i_nVal) {
					return eCode;
				}
			}
			return irop_unknown;
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

	public enum SensorType {
		WHEELDROP_CASTER("Caster Wheel Drop"),
		WHEELDROP_LEFT("Left Wheel Drop"),
		WHEELDROP_RIGHT("Right Wheel Drop"),
		BUMP_LEFT("Left Bumper"),
		BUMP_RIGHT("Right Bumper"),
		WALL("Wall"),
		CLIFF_LEFT("Cliff Left"),
		CLIFF_FRONT_LEFT("Cliff Front Left"),
		CLIFF_FRONT_RIGHT("Cliff Front Right"),
		CLIFF_RIGHT("Cliff Right"),
		VIRTUAL_WALL("Virtual Wall"),
		OVERCURRENT_LEFT("Motor OC Left Whel"),
		OVERCURRENT_RIGHT("Motor OC Right Wheel"),
		OVERCURRENT_MAIN_BRUSH("Motor OC Main Brush"),
		OVERCURRENT_VACUUM("Motor OC Vacuum"),
		OVERCURRENT_SIDEBRUSH("Motor OC Side Brush"),
		DIRT_DETECTOR_LEFT("Dirt Detector Left"),
		DIRT_DETECTOR_RIGHT("Dirt Detector Right"),
		IR_OPCODE_OMNI("IR Op-Code Omni"),
		PRESSED_POWER("Power Button pressed"),
		PRESSED_SPOT("Spot Button pressed"),
		PRESSED_CLEAN("Clean Button pressed"),
		PRESSED_MAX("Max button pressed"),
		DISTANCE("Distance [mm]"),
		ANGLE("Angle [mm]"),
		CHARGING_STATE("Charging State"),
		VOLTAGE("Voltage [mV]"),
		CURRENT("Current [mA]"),
		BATTERY_TEMPRERATURE("Battery Temperature [C]"),
		CHARGE("Charge [mAh]"),
		CAPACITY("Capacity [mAh]"),
		WALL_SIGNAL("Wall Signal"),
		CLIFF_LEFT_SIGNAL("Cliff Left Signal"),
		CLIFF_FRONT_LEFT_SIGNAL("Cliff Front Left Signal"),
		CLIFF_FRONT_RIGHT_SIGNAL("Cliff Front Right Signal"),
		CLIFF_RIGHT_SIGNAL("Cliff Right Signal"),
		USER_DIGITAL_INPUTS("User Digital Inputs"),
		USER_ANALOG_INPUT("User Analog Input"),
		CHARGING_SOURCES_AVAILABLE("Charging Sources Available"),
		OI_MODE("Open Interface Mode"),
		SONG_NUMBER("Song Number"),
		SONG_PLAYING("Song Playing"),
		NUMBER_OF_STREAM_PACKETS("Number of Stream Packets"),
		REQUESTED_VELOCITY("Requested Velocity [mm/s]"),
		REQUESTED_RADIUS("Requested Velocity [mm/s]"),
		REQUESTED_RIGHT_VELOCITY("Requested Velocity Right [mm/s]"),
		REQUESTED_LEFT_VELOCITY("Requested Velocity Left [mm/s]"),
		ENCODER_COUNTS_LEFT("Encoder Counts Left"),
		ENCODER_COUNTS_RIGHT("Encoder Counts Right"),
		LIGHT_BUMPER_LEFT("Light Bumper Left"),
		LIGHT_BUMPER_FRONT_LEFT("Light Bumper Front Left"),
		LIGHT_BUMPER_CENTER_LEFT("Light Bumper Center Left"),
		LIGHT_BUMPER_CENTER_RIGHT("Light Bumper Center Right"),
		LIGHT_BUMPER_FRONT_RIGHT("Light Bumper Front Right"),
		LIGHT_BUMPER_RIGHT("Light Bumper Right"),
		LIGHT_BUMP_LEFT_SIGNAL("Light Bump Left Signal"),
		LIGHT_BUMP_FRONT_LEFT_SIGNAL("Light Bump Front Left Signal"),
		LIGHT_BUMP_CENTER_LEFT_SIGNAL("Light Bump Center Left Signal"),
		LIGHT_BUMP_CENTER_RIGHT_SIGNAL("Light Bump Center Right Signal"),
		LIGHT_BUMP_FRONT_RIGHT_SIGNAL("Light Bump Front Right Signal"),
		LIGHT_BUMP_RIGHT_SIGNAL("Light Bump Right Signal"),
		IR_OPCODE_LEFT("IR Op-Code Left"),
		IR_OPCODE_RIGHT("IR Op-Code Right"),
		LEFT_MOTOR_CURRENT("Left Motor Current [mA]"),
		RIGHT_MOTOR_CURRENT("Right Motor Current [mA]"),
		MAIN_BRUSH_CURRENT("Main Brush Current [mA]"),
		SIDE_BRUSH_CURRENT("Side Brush Current [mA]"),
		STASIS("Stasis"),
		ALL("Show All");
		private String strName;
		
		SensorType(String i_strName) {
			this.strName = i_strName;
		}
		
		public String toString() {
			return strName;
		}
	}
	
	public class SensorPackageAll implements SensorPackage {
		public BumpsWheeldrops bumps_wheeldrops;
		public boolean wall;
		public boolean cliff_left;
		public boolean cliff_front_left;
		public boolean cliff_front_right;
		public boolean cliff_right;
		public boolean virtual_wall;
		public MotorOvercurrents motor_overcurrents;
		public short dirt_detector_left;
		public short dirt_detector_right;
		public IROpCode remote_opcode;
		public ButtonsPressed buttons;
		public short distance;
		public short angle;
		public EChargingState charging_state;
		public int voltage;
		public short current;
		public byte temprerature;
		public int charge;
		public int capacity;
		public int wall_signal;
		public int cliff_left_signal;
		public int cliff_front_left_signal;
		public int cliff_front_right_signal;
		public int cliff_right_signal;
		public short user_digital_inputs;
		public int user_analog_input;
		public ChargeMode charging_sources_available;
		public OIMode oi_mode;
		public short song_number;
		public boolean song_playing;
		public short number_of_stream_packets;
		public short requested_velocity;
		public short requested_radius;
		public short requested_right_velocity;
		public short requested_left_velocity;
		public int encoder_counts_left;
		public int encoder_counts_right;
		public LightBumper light_bumper;
		public int light_bump_left_signal;
		public int light_bump_front_left_signal;
		public int light_bump_center_left_signal;
		public int light_bump_center_right_signal;
		public int light_bump_front_right_signal;
		public int light_bump_right_signal;
		public IROpCode ir_opcode_left;
		public IROpCode ir_opcode_right;
		public short left_motor_current;
		public short right_motor_current;
		public short main_brush_current;
		public short side_brush_current;
		public byte stasis;
		
		
		public SensorPackageAll(byte[] i_rgbyValues) {
			ByteBuffer buffer = ByteBuffer.wrap(i_rgbyValues);
			buffer.order(ByteOrder.BIG_ENDIAN);
			
			bumps_wheeldrops = new BumpsWheeldrops(Utils.getUnsignedByte(buffer));
			wall = Utils.getBoolean(buffer);
			cliff_left = Utils.getBoolean(buffer);
			cliff_front_left = Utils.getBoolean(buffer);
			cliff_front_right = Utils.getBoolean(buffer);
			cliff_right = Utils.getBoolean(buffer);
			virtual_wall = Utils.getBoolean(buffer);
			motor_overcurrents = new MotorOvercurrents(Utils.getUnsignedByte(buffer));
			dirt_detector_left = Utils.getUnsignedByte(buffer);
			dirt_detector_right = Utils.getUnsignedByte(buffer);
			remote_opcode = IROpCode.valToEnum(Utils.getUnsignedByte(buffer));
			buttons = new ButtonsPressed(Utils.getUnsignedByte(buffer));
			distance = buffer.getShort();
			angle = buffer.getShort();
			charging_state = EChargingState.OrdToEnum(Utils.getUnsignedByte(buffer));
			voltage = Utils.getUnsignedShort(buffer);
			current = buffer.getShort();
			temprerature = buffer.get();
			charge = Utils.getUnsignedShort(buffer);
			capacity = Utils.getUnsignedShort(buffer);
			wall_signal = Utils.getUnsignedShort(buffer);
			cliff_left_signal = Utils.getUnsignedShort(buffer);
			cliff_front_left_signal = Utils.getUnsignedShort(buffer);
			cliff_front_right_signal = Utils.getUnsignedShort(buffer);
			cliff_right_signal = Utils.getUnsignedShort(buffer);
			user_digital_inputs = Utils.getUnsignedByte(buffer);
			user_analog_input = Utils.getUnsignedShort(buffer);
			charging_sources_available = ChargeMode.valToEnum(Utils.getUnsignedByte(buffer));
			oi_mode = OIMode.OrdToEnum(Utils.getUnsignedByte(buffer));
			song_number = Utils.getUnsignedByte(buffer);
			song_playing = Utils.getBoolean(buffer);
			number_of_stream_packets = Utils.getUnsignedByte(buffer);
			requested_velocity = buffer.getShort();
			requested_radius = buffer.getShort();
			requested_right_velocity = buffer.getShort();
			requested_left_velocity = buffer.getShort();
			encoder_counts_left = Utils.getUnsignedShort(buffer);
			encoder_counts_right = Utils.getUnsignedShort(buffer);
			light_bumper = new LightBumper(Utils.getUnsignedByte(buffer));
			light_bump_left_signal = Utils.getUnsignedShort(buffer);
			light_bump_front_left_signal = Utils.getUnsignedShort(buffer);
			light_bump_center_left_signal = Utils.getUnsignedShort(buffer);
			light_bump_center_right_signal = Utils.getUnsignedShort(buffer);
			light_bump_front_right_signal = Utils.getUnsignedShort(buffer);
			light_bump_right_signal = Utils.getUnsignedShort(buffer);
			ir_opcode_left = IROpCode.valToEnum(Utils.getUnsignedByte(buffer));
			ir_opcode_right = IROpCode.valToEnum(Utils.getUnsignedByte(buffer));
			left_motor_current = buffer.getShort();
			right_motor_current = buffer.getShort();
			main_brush_current = buffer.getShort();
			side_brush_current = buffer.getShort();
			stasis = buffer.get();
		}
		
		public String toString() {
			return "";
		}
	}
	
	public static SensorPackage assembleSensorPackage(ERoombaSensorPackages i_ePackage, byte[] i_bySensorData) {
		RoombaTypes oRoombaTypes = new RoombaTypes();
		switch (i_ePackage) {
			case sensPkg_All:
				return oRoombaTypes.new SensorPackageAll(i_bySensorData);
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
