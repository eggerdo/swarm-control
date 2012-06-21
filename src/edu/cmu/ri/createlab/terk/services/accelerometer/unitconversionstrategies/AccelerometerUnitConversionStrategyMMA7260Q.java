package edu.cmu.ri.createlab.terk.services.accelerometer.unitconversionstrategies;

import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerGs;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerState;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerUnitConversionStrategy;

/**
 * <p>
 * <code>AccelerometerUnitConversionStrategyMF52A103F3380</code> performs unit conversions for the MF52A103F3380
 * accelerometer.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class AccelerometerUnitConversionStrategyMMA7260Q implements AccelerometerUnitConversionStrategy
   {
   private static final AccelerometerUnitConversionStrategyMMA7260Q INSTANCE = new AccelerometerUnitConversionStrategyMMA7260Q();

   public static final String DEVICE_ID = "MMA7260Q";

   private static final int MAX_NATIVE_VALUE = 255;
   private static final int MIN_NATIVE_VALUE = 0;
   private static final int NATIVE_VALUE_MIDPOINT = 128;

   private static final double MULTIPLIER = 2.0625 / (double)NATIVE_VALUE_MIDPOINT;

   // the accelerometer caps at +- 1.5 g
   private static final double MIN_G = -1.5;
   private static final double MAX_G = 1.5;

   public static AccelerometerUnitConversionStrategyMMA7260Q getInstance()
      {
      return INSTANCE;
      }

   private AccelerometerUnitConversionStrategyMMA7260Q()
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

   public double convertToGs(final int nativeValue)
      {
      final double g = (nativeValue - NATIVE_VALUE_MIDPOINT) * MULTIPLIER;

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
      final int nativeValue = (int)(gValue / MULTIPLIER + NATIVE_VALUE_MIDPOINT);

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