package edu.cmu.ri.createlab.terk.robot.finch;

import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerUnitConversionStrategy;
import edu.cmu.ri.createlab.terk.services.accelerometer.unitconversionstrategies.AccelerometerUnitConversionStrategyFreescaleMMA7660FC;
import edu.cmu.ri.createlab.terk.services.thermistor.ThermistorUnitConversionStrategy;
import edu.cmu.ri.createlab.terk.services.thermistor.unitconversionstrategies.ThermistorUnitConversionStrategyMF52A103F3380;
import edu.cmu.ri.createlab.usb.hid.HIDDeviceDescriptor;

/**
 * <p>
 * <code>FinchConstants</code> defines various constants for finches.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class FinchConstants
   {
   public static final short USB_VENDOR_ID = 0x2354;
   public static final short USB_PRODUCT_ID = 0x1111;

   private static final int INPUT_REPORT_LENGTH_IN_BYTES = 9;  // count includes the report ID
   private static final int OUTPUT_REPORT_LENGTH_IN_BYTES = 9; // count includes the report ID

   public static final HIDDeviceDescriptor FINCH_HID_DEVICE_DESCRIPTOR = new HIDDeviceDescriptor(USB_VENDOR_ID,
                                                                                                 USB_PRODUCT_ID,
                                                                                                 INPUT_REPORT_LENGTH_IN_BYTES,
                                                                                                 OUTPUT_REPORT_LENGTH_IN_BYTES,
                                                                                                 "Finch");

   /** The number of accelerometers */
   public static final int ACCELEROMETER_DEVICE_COUNT = 1;

   /**
    * The unique device id for the accelerometer used by all finches.  This value is used to lookup the appropriate
    * {@link AccelerometerUnitConversionStrategy} for converting acclerometer values into g's.
    */
   public static final String ACCELEROMETER_DEVICE_ID = AccelerometerUnitConversionStrategyFreescaleMMA7660FC.DEVICE_ID;

   /** The number of audio outputs */
   public static final int AUDIO_DEVICE_COUNT = 1;

   /** The minimum supported tone frequency */
   public static final int AUDIO_DEVICE_MIN_AMPLITUDE = 0;

   /** The maximum supported tone frequency */
   public static final int AUDIO_DEVICE_MAX_AMPLITUDE = 10;

   /** The minimum supported tone duration */
   public static final int AUDIO_DEVICE_MIN_DURATION = 0;

   /** The maximum supported tone duration */
   public static final int AUDIO_DEVICE_MAX_DURATION = Integer.MAX_VALUE;

   /** The minimum supported tone frequency */
   public static final int AUDIO_DEVICE_MIN_FREQUENCY = 0;

   /** The maximum supported tone frequency */
   public static final int AUDIO_DEVICE_MAX_FREQUENCY = Integer.MAX_VALUE;

   /** The number of buzzers */
   public static final int BUZZER_DEVICE_COUNT = 1;

   /** The minimum supported buzzer duration */
   public static final int BUZZER_DEVICE_MIN_DURATION = 0;

   /** The maximum supported buzzer duration */
   public static final int BUZZER_DEVICE_MAX_DURATION = 65535;

   /** The minimum supported buzzer frequency */
   public static final int BUZZER_DEVICE_MIN_FREQUENCY = 0;

   /** The maximum supported buzzer frequency */
   public static final int BUZZER_DEVICE_MAX_FREQUENCY = 65535;

   /** The number of finches */
   public static final int FINCH_DEVICE_COUNT = 1;

   /** The number of full-color LEDS */
   public static final int FULL_COLOR_LED_DEVICE_COUNT = 1;

   /** The minimum supported full-color LED intensity */
   public static final int FULL_COLOR_LED_DEVICE_MIN_INTENSITY = 0;

   /** The maximum supported full-color LED intensity */
   public static final int FULL_COLOR_LED_DEVICE_MAX_INTENSITY = 255;

   /** The number of motors */
   public static final int MOTOR_DEVICE_COUNT = 2;

   /** The minimum supported velocity (for velocity control) */
   public static final int MOTOR_DEVICE_MIN_VELOCITY = -255;

   /** The maximum supported velocity (for velocity control) */
   public static final int MOTOR_DEVICE_MAX_VELOCITY = 255;

   /** The number of photoresistors */
   public static final int PHOTORESISTOR_DEVICE_COUNT = 2;

   /** The minimum supported photoresistor value */
   public static final int PHOTORESISTOR_MIN_VALUE = 0;

   /** The maximum supported photoresistor value */
   public static final int PHOTORESISTOR_MAX_VALUE = 255;

   /** The number of simple obstacle sensors */
   public static final int SIMPLE_OBSTACLE_SENSOR_DEVICE_COUNT = 2;

   /** The number of thermistors */
   public static final int THERMISTOR_DEVICE_COUNT = 1;

   /** The minimum supported thermistor value */
   public static final int THERMISTOR_MIN_VALUE = 0;

   /** The maximum supported thermistor value */
   public static final int THERMISTOR_MAX_VALUE = 255;

   /**
    * The unique device id for the thermistor used by all finches.  This value is used to lookup the appropriate
    * {@link ThermistorUnitConversionStrategy} for converting thermistor values into temperatures.
    */
   public static final String THERMISTOR_DEVICE_ID = ThermistorUnitConversionStrategyMF52A103F3380.DEVICE_ID;

   public static final String HARDWARE_TYPE = "finch";

   public static final String HARDWARE_VERSION = "2.0";

   private FinchConstants()
      {
      // private to prevent instantiation
      }
   }