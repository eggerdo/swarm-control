package edu.cmu.ri.createlab.terk.services.accelerometer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import edu.cmu.ri.createlab.terk.services.accelerometer.unitconversionstrategies.AccelerometerUnitConversionStrategyFreescaleMMA7660FC;
import edu.cmu.ri.createlab.terk.services.accelerometer.unitconversionstrategies.AccelerometerUnitConversionStrategyMMA7260Q;

/**
 * <p>
 * <code>AccelerometerConversionStrategyFinder</code> is a singleton for finding
 * {@link AccelerometerUnitConversionStrategy} implementations by accelerometer device id.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class AccelerometerUnitConversionStrategyFinder
   {
   private static final AccelerometerUnitConversionStrategyFinder INSTANCE = new AccelerometerUnitConversionStrategyFinder();

   private static final Map<String, AccelerometerUnitConversionStrategy> STRATEGY_MAP;

   static
      {
      final Map<String, AccelerometerUnitConversionStrategy> strategyMap = new HashMap<String, AccelerometerUnitConversionStrategy>();
      strategyMap.put(AccelerometerUnitConversionStrategyMMA7260Q.DEVICE_ID, AccelerometerUnitConversionStrategyMMA7260Q.getInstance());
      strategyMap.put(AccelerometerUnitConversionStrategyFreescaleMMA7660FC.DEVICE_ID, AccelerometerUnitConversionStrategyFreescaleMMA7660FC.getInstance());
      STRATEGY_MAP = Collections.unmodifiableMap(strategyMap);
      }

   public static AccelerometerUnitConversionStrategyFinder getInstance()
      {
      return INSTANCE;
      }

   private AccelerometerUnitConversionStrategyFinder()
      {
      // private to prevent instantiation
      }

   public AccelerometerUnitConversionStrategy lookup(final String deviceId)
      {
      return STRATEGY_MAP.get(deviceId);
      }
   }