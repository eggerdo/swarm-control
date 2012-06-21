package edu.cmu.ri.createlab.terk.services.motor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import edu.cmu.ri.createlab.terk.services.motor.unitconversionstrategies.VelocityControllableMotorUnitConversionStrategyFinch;

/**
 * <p>
 * <code>VelocityControllableMotorUnitConversionStrategyFinder</code> is a singleton for finding
 * {@link VelocityControllableMotorUnitConversionStrategy} implementations by motor device id.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class VelocityControllableMotorUnitConversionStrategyFinder
   {
   private static final VelocityControllableMotorUnitConversionStrategyFinder INSTANCE = new VelocityControllableMotorUnitConversionStrategyFinder();

   private static final Map<String, VelocityControllableMotorUnitConversionStrategy> STRATEGY_MAP;

   static
      {
      final Map<String, VelocityControllableMotorUnitConversionStrategy> strategyMap = new HashMap<String, VelocityControllableMotorUnitConversionStrategy>();
      strategyMap.put(VelocityControllableMotorUnitConversionStrategyFinch.DEVICE_ID, VelocityControllableMotorUnitConversionStrategyFinch.getInstance());
      STRATEGY_MAP = Collections.unmodifiableMap(strategyMap);
      }

   public static VelocityControllableMotorUnitConversionStrategyFinder getInstance()
      {
      return INSTANCE;
      }

   private VelocityControllableMotorUnitConversionStrategyFinder()
      {
      // private to prevent instantiation
      }

   public VelocityControllableMotorUnitConversionStrategy lookup(final String deviceId)
      {
      return STRATEGY_MAP.get(deviceId);
      }
   }