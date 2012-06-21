package edu.cmu.ri.createlab.terk.services.thermistor.unitconversionstrategies;

import edu.cmu.ri.createlab.terk.services.thermistor.ThermistorUnitConversionStrategy;

/**
 * <p>
 * <code>ThermistorUnitConversionStrategyMF52A103F3380</code> performs unit conversions for the MF52A103F3380
 * thermistor.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ThermistorUnitConversionStrategyMF52A103F3380 implements ThermistorUnitConversionStrategy
   {
   private static final ThermistorUnitConversionStrategyMF52A103F3380 INSTANCE = new ThermistorUnitConversionStrategyMF52A103F3380();

   public static final String DEVICE_ID = "MF52A103F3380";

   public static ThermistorUnitConversionStrategyMF52A103F3380 getInstance()
      {
      return INSTANCE;
      }

   private ThermistorUnitConversionStrategyMF52A103F3380()
      {
      // private to prevent instantiation
      }

   public String getDeviceId()
      {
      return DEVICE_ID;
      }

   public Double convertToCelsius(final Integer rawValue)
      {
      if (rawValue != null)
         {
         return (double)(rawValue - 127) / 2.4 + 25;
         }
      return null;
      }
   }
