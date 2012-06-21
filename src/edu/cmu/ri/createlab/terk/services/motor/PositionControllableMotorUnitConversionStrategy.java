package edu.cmu.ri.createlab.terk.services.motor;

import edu.cmu.ri.createlab.terk.services.UnitConversionStrategy;

/**
 * <p>
 * <code>PositionControllableMotorUnitConversionStrategy</code> helps convert between raw motor positions values and
 * distances.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface PositionControllableMotorUnitConversionStrategy extends UnitConversionStrategy
   {
   /**
    * Converts the given raw motor position from ticks to centimeters. Returns <code>null</code> if the given
    * <code>valueInTicks</code> is <code>null</code>.
    */
   Double convertToCentimeters(final Integer valueInTicks);

   /**
    * Converts the given raw motor positions from ticks to centimeters. Returns <code>null</code> if the given array is
    * <code>null</code>.
    */
   double[] convertToCentimeters(final int[] valuesInTicks);

   /**
    * Converts the given distance from centimeters to ticks, rounding to the nearest tick if necessary.  Returns
    * <code>null</code> if the given <code>valueInCentimeters</code> is <code>null</code>.
    */
   Integer convertToTicks(final Double valueInCentimeters);

   /**
    * Converts the given distances from centimeters to ticks, rounding to the nearest tick if necessary. Returns
    * <code>null</code> if the given array is <code>null</code>.
    */
   int[] convertToTicks(final double[] valuesInCentimeters);

   /**
    * Converts the given native motor speed to cm/s. Returns <code>null</code> if the given <code>nativeSpeed</code> is
    * <code>null</code>.
    */
   Double convertToCentimetersPerSecond(final Integer nativeSpeed);

   /**
    * Converts the given native motor speeds to cm/s. Returns <code>null</code> if the given array is <code>null</code>.
    */
   double[] convertToCentimetersPerSecond(final int[] nativeSpeeds);

   /**
    * Converts the given speed from cm/s to native speed, rounding if necessary.  Returns <code>null</code> if the given
    * <code>speedInCentimeters</code> is <code>null</code>.
    */
   Integer convertToNativeSpeed(final Double speedInCentimeters);

   /**
    * Converts the given speeds from cm/s to native speeds, rounding if necessary. Returns <code>null</code> if the
    * given array is <code>null</code>.
    */
   int[] convertToNativeSpeed(final double[] speedsInCentimeters);
   }