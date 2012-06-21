package edu.cmu.ri.createlab.terk.services.thermistor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import edu.cmu.ri.createlab.terk.services.thermistor.unitconversionstrategies.ThermistorUnitConversionStrategyMF52A103F3380;

/**
 * <p>
 * <code>TemperatureConversionStrategyFinder</code> is a singleton for finding {@link ThermistorUnitConversionStrategy}
 * implementations by thermistor device id.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ThermistorUnitConversionStrategyFinder
   {
   private static final ThermistorUnitConversionStrategyFinder INSTANCE = new ThermistorUnitConversionStrategyFinder();

   private static final Map<String, ThermistorUnitConversionStrategy> STRATEGY_MAP;

   static
      {
      final Map<String, ThermistorUnitConversionStrategy> strategyMap = new HashMap<String, ThermistorUnitConversionStrategy>();
      strategyMap.put(ThermistorUnitConversionStrategyMF52A103F3380.DEVICE_ID, ThermistorUnitConversionStrategyMF52A103F3380.getInstance());
      STRATEGY_MAP = Collections.unmodifiableMap(strategyMap);
      }

   public static ThermistorUnitConversionStrategyFinder getInstance()
      {
      return INSTANCE;
      }

   private ThermistorUnitConversionStrategyFinder()
      {
      // private to prevent instantiation
      }

   public ThermistorUnitConversionStrategy lookup(final String deviceId)
      {
      return STRATEGY_MAP.get(deviceId);
      }
   }
