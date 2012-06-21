package edu.cmu.ri.createlab.terk.services.motor.unitconversionstrategies;

import edu.cmu.ri.createlab.terk.services.motor.PositionControllableMotorUnitConversionStrategy;

/**
 * <p>
 * <code>PositionControllableMotorUnitConversionStrategyFinch</code> performs unit conversions for the Finch motors.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class PositionControllableMotorUnitConversionStrategyFinch implements PositionControllableMotorUnitConversionStrategy
   {
   private static final PositionControllableMotorUnitConversionStrategyFinch INSTANCE = new PositionControllableMotorUnitConversionStrategyFinch();

   public static final String DEVICE_ID = "Finch";
   private static final double CENTIMETERS_PER_TICK = 0.7747;
   private static final double CENTIMETERS_PER_NATIVE_SPEED_UNIT = 1.5494;

   public static PositionControllableMotorUnitConversionStrategyFinch getInstance()
      {
      return INSTANCE;
      }

   private PositionControllableMotorUnitConversionStrategyFinch()
      {
      // private to prevent instantiation
      }

   public String getDeviceId()
      {
      return DEVICE_ID;
      }

   public Double convertToCentimeters(final Integer valueInTicks)
      {
      if (valueInTicks != null)
         {
         return valueInTicks * CENTIMETERS_PER_TICK;
         }
      return null;
      }

   public final double[] convertToCentimeters(final int[] valuesInTicks)
      {
      if (valuesInTicks != null)
         {
         final double[] distanceDeltas = new double[valuesInTicks.length];

         for (int i = 0; i < distanceDeltas.length; i++)
            {
            distanceDeltas[i] = convertToCentimeters(valuesInTicks[i]);
            }
         return distanceDeltas;
         }
      return null;
      }

   public Integer convertToTicks(final Double valueInCentimeters)
      {
      if (valueInCentimeters != null)
         {
         return (int)(valueInCentimeters / CENTIMETERS_PER_TICK);
         }
      return null;
      }

   public final int[] convertToTicks(final double[] valuesInCentimeters)
      {
      if (valuesInCentimeters != null)
         {
         final int[] positionDeltas = new int[valuesInCentimeters.length];

         for (int i = 0; i < valuesInCentimeters.length; i++)
            {
            positionDeltas[i] = convertToTicks(valuesInCentimeters[i]);
            }
         return positionDeltas;
         }
      return null;
      }

   public Double convertToCentimetersPerSecond(final Integer nativeSpeed)
      {
      if (nativeSpeed != null)
         {
         return nativeSpeed * CENTIMETERS_PER_NATIVE_SPEED_UNIT;
         }
      return null;
      }

   public double[] convertToCentimetersPerSecond(final int[] nativeSpeeds)
      {
      if (nativeSpeeds != null)
         {
         final double[] centimetersPerSecond = new double[nativeSpeeds.length];

         for (int i = 0; i < centimetersPerSecond.length; i++)
            {
            centimetersPerSecond[i] = convertToCentimetersPerSecond(nativeSpeeds[i]);
            }
         return centimetersPerSecond;
         }
      return null;
      }

   public Integer convertToNativeSpeed(final Double speedInCentimeters)
      {
      if (speedInCentimeters != null)
         {
         return (int)(speedInCentimeters / CENTIMETERS_PER_NATIVE_SPEED_UNIT);
         }
      return null;
      }

   public final int[] convertToNativeSpeed(final double[] speedsInCentimeters)
      {
      if (speedsInCentimeters != null)
         {
         final int[] nativeSpeeds = new int[speedsInCentimeters.length];

         for (int i = 0; i < speedsInCentimeters.length; i++)
            {
            nativeSpeeds[i] = convertToNativeSpeed(speedsInCentimeters[i]);
            }
         return nativeSpeeds;
         }
      return null;
      }
   }