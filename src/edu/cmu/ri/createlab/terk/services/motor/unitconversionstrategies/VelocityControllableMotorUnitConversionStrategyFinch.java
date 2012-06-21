package edu.cmu.ri.createlab.terk.services.motor.unitconversionstrategies;

import edu.cmu.ri.createlab.terk.services.motor.VelocityControllableMotorUnitConversionStrategy;

/**
 * <p>
 * <code>VelocityControllableMotorUnitConversionStrategyFinch</code> performs unit conversions for the Finch motors.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class VelocityControllableMotorUnitConversionStrategyFinch implements VelocityControllableMotorUnitConversionStrategy
   {
   private static final VelocityControllableMotorUnitConversionStrategyFinch INSTANCE = new VelocityControllableMotorUnitConversionStrategyFinch();

   public static final String DEVICE_ID = "Finch";
   private static final double CENTIMETERS_PER_NATIVE_UNIT = 1.5494;

   public static VelocityControllableMotorUnitConversionStrategyFinch getInstance()
      {
      return INSTANCE;
      }

   private VelocityControllableMotorUnitConversionStrategyFinch()
      {
      // private to prevent instantiation
      }

   public String getDeviceId()
      {
      return DEVICE_ID;
      }

   public Double convertToCentimetersPerSecond(final Integer nativeVelocity)
      {
      if (nativeVelocity != null)
         {
         return nativeVelocity * CENTIMETERS_PER_NATIVE_UNIT;
         }
      return null;
      }

   public double[] convertToCentimetersPerSecond(final int[] nativeVelocities)
      {
      if (nativeVelocities != null)
         {
         final double[] centimetersPerSecond = new double[nativeVelocities.length];

         for (int i = 0; i < centimetersPerSecond.length; i++)
            {
            centimetersPerSecond[i] = convertToCentimetersPerSecond(nativeVelocities[i]);
            }
         return centimetersPerSecond;
         }
      return null;
      }

   public Integer convertToNativeVelocity(final Double velocityInCentimeters)
      {
      if (velocityInCentimeters != null)
         {
         return (int)(velocityInCentimeters / CENTIMETERS_PER_NATIVE_UNIT);
         }
      return null;
      }

   public final int[] convertToNativeVelocity(final double[] velocitiesInCentimeters)
      {
      if (velocitiesInCentimeters != null)
         {
         final int[] nativeVelocities = new int[velocitiesInCentimeters.length];

         for (int i = 0; i < velocitiesInCentimeters.length; i++)
            {
            nativeVelocities[i] = convertToNativeVelocity(velocitiesInCentimeters[i]);
            }
         return nativeVelocities;
         }
      return null;
      }
   }