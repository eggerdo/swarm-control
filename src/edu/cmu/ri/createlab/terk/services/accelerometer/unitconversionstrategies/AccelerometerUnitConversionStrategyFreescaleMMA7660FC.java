package edu.cmu.ri.createlab.terk.services.accelerometer.unitconversionstrategies;

import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerGs;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerState;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerUnitConversionStrategy;

/**
 * <p>
 * <code>AccelerometerUnitConversionStrategyFreescaleMMA7660FC</code> performs unit conversions for the Freescale
 * MMA7660FC accelerometer.
 * </p>
 * <p>
 * The accelerometer has a range from 0x00 to 0x3F (6 bit).  0x00 through 0x1F map to +0g through +1.453g.  0x3F through
 * 0x20 map to -0.047g through -1.5g.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class AccelerometerUnitConversionStrategyFreescaleMMA7660FC implements AccelerometerUnitConversionStrategy
   {
   private static final AccelerometerUnitConversionStrategyFreescaleMMA7660FC INSTANCE = new AccelerometerUnitConversionStrategyFreescaleMMA7660FC();

   public static final String DEVICE_ID = "FreescaleMMA7660FC";

   public static final int MIN_NATIVE_VALUE = 0x00;        // 0
   public static final int MIDPOINT_NATIVE_VALUE = 0x1F;   // 31
   public static final int MAX_NATIVE_VALUE = 0x3F;        // 63

   private static final double MULTIPLIER = 1.5 / 32.0;
   public static final double MIN_G = -1.5;
   public static final double MAX_G = 1.453125;

   public static AccelerometerUnitConversionStrategyFreescaleMMA7660FC getInstance()
      {
      return INSTANCE;
      }

   private AccelerometerUnitConversionStrategyFreescaleMMA7660FC()
      {
      // private to prevent instantiation
      }

   public String getDeviceId()
      {
      return DEVICE_ID;
      }

   @Override
   public double getMinGs()
      {
      return MIN_G;
      }

   @Override
   public double getMaxGs()
      {
      return MAX_G;
      }

   /**
    * Converts the native values in the given {@link AccelerometerState} to Gs.  Native values which are less than
    * {@link #MIN_NATIVE_VALUE} will be mapped to {@link #MIN_G} and native values which are greater than
    * {@link #MAX_NATIVE_VALUE} will be mapped to -0.046875 Gs.
    */
   public AccelerometerGs convert(final AccelerometerState accelerometerState)
      {
      if (accelerometerState != null)
         {
         return new AccelerometerGs(convertToGs(accelerometerState.getX()),
                                    convertToGs(accelerometerState.getY()),
                                    convertToGs(accelerometerState.getZ()));
         }
      return null;
      }

   /**
    * Converts the G values in the given {@link AccelerometerState} to native values.  G values which are less than
    * {@link #MIN_G} will be mapped to 32 and G values which are greater than {@link #MAX_G} will
    * be mapped to {@link #MIDPOINT_NATIVE_VALUE}.
    */
   public AccelerometerState convert(final AccelerometerGs accelerometerState)
      {
      if (accelerometerState != null)
         {
         return new AccelerometerState(convertToNative(accelerometerState.getX()),
                                       convertToNative(accelerometerState.getY()),
                                       convertToNative(accelerometerState.getZ()));
         }
      return null;
      }

   private static int convertNativeToSixBit(final int val)
      {
      final int cleanedVal = Math.max(Math.min(val, MAX_NATIVE_VALUE), MIN_NATIVE_VALUE);
      if (cleanedVal <= MIDPOINT_NATIVE_VALUE)
         {
         return cleanedVal;
         }

      return cleanedVal - 64;
      }

   private static int convertSixBitToNative(final int val)
      {
      if (val < 0)
         {
         return val + 64;
         }

      return val;
      }

   public double convertToGs(final int nativeValue)
      {
      final int sixBitValue = convertNativeToSixBit(nativeValue);
      final double g = sixBitValue * MULTIPLIER;

      if (g < MIN_G)
         {
         return MIN_G;
         }
      else if (g > MAX_G)
         {
         return MAX_G;
         }

      return g;
      }

   public int convertToNative(final double gValue)
      {
      final double cleanedGVal = Math.max(Math.min(gValue, MAX_G), MIN_G);

      final int sixBitValue = (int)(cleanedGVal / MULTIPLIER);
      final int nativeValue = convertSixBitToNative(sixBitValue);

      if (nativeValue < MIN_NATIVE_VALUE)
         {
         return MIN_NATIVE_VALUE;
         }
      else if (nativeValue > MAX_NATIVE_VALUE)
         {
         return MAX_NATIVE_VALUE;
         }

      return nativeValue;
      }
   }