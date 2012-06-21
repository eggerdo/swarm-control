package edu.cmu.ri.createlab.terk.services.motor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import edu.cmu.ri.createlab.terk.services.motor.unitconversionstrategies.PositionControllableMotorUnitConversionStrategyFinch;

/**
 * <p>
 * <code>PositionControllableMotorUnitConversionStrategyFinder</code> is a singleton for finding
 * {@link PositionControllableMotorUnitConversionStrategy} implementations by motor device id.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class PositionControllableMotorUnitConversionStrategyFinder
   {
   private static final PositionControllableMotorUnitConversionStrategyFinder INSTANCE = new PositionControllableMotorUnitConversionStrategyFinder();

   private static final Map<String, PositionControllableMotorUnitConversionStrategy> STRATEGY_MAP;

   static
      {
      final Map<String, PositionControllableMotorUnitConversionStrategy> strategyMap = new HashMap<String, PositionControllableMotorUnitConversionStrategy>();
      strategyMap.put(PositionControllableMotorUnitConversionStrategyFinch.DEVICE_ID, PositionControllableMotorUnitConversionStrategyFinch.getInstance());
      STRATEGY_MAP = Collections.unmodifiableMap(strategyMap);
      }

   public static PositionControllableMotorUnitConversionStrategyFinder getInstance()
      {
      return INSTANCE;
      }

   private PositionControllableMotorUnitConversionStrategyFinder()
      {
      // private to prevent instantiation
      }

   public PositionControllableMotorUnitConversionStrategy lookup(final String deviceId)
      {
      return STRATEGY_MAP.get(deviceId);
      }
   }