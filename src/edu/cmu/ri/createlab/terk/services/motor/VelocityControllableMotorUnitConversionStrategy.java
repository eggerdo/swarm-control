package edu.cmu.ri.createlab.terk.services.motor;

import edu.cmu.ri.createlab.terk.services.UnitConversionStrategy;

/**
 * <p>
 * <code>VelocityControllableMotorUnitConversionStrategy</code> helps convert between native motor velocities values and
 * velocities in cm/s.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface VelocityControllableMotorUnitConversionStrategy extends UnitConversionStrategy
   {
   /**
    * Converts the given native motor velocity to cm/s. Returns <code>null</code> if the given
    * <code>nativeVelocity</code> is <code>null</code>.
    */
   Double convertToCentimetersPerSecond(final Integer nativeVelocity);

   /**
    * Converts the given native motor velocities to cm/s. Returns <code>null</code> if the given array is
    * <code>null</code>.
    */
   double[] convertToCentimetersPerSecond(final int[] nativeVelocities);

   /**
    * Converts the given velocity from cm/s to native velocity, rounding if necessary.  Returns
    * <code>null</code> if the given <code>velocityInCentimeters</code> is <code>null</code>.
    */
   Integer convertToNativeVelocity(final Double velocityInCentimeters);

   /**
    * Converts the given velocities from cm/s to native velocities, rounding if necessary. Returns
    * <code>null</code> if the given array is <code>null</code>.
    */
   int[] convertToNativeVelocity(final double[] velocitiesInCentimeters);
   }