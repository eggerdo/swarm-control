package edu.cmu.ri.createlab.terk.services.thermistor;

import edu.cmu.ri.createlab.terk.services.UnitConversionStrategy;

/**
 * <p>
 * <code>TemperatureConversionStrategy</code> helps convert raw thermistor values to temperatures.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ThermistorUnitConversionStrategy extends UnitConversionStrategy
   {
   /**
    * Converts the given raw thermistor value to degrees Celsius. Returns <code>null</code> if the given
    * <code>rawValue</code> is <code>null</code>.
    */
   Double convertToCelsius(final Integer rawValue);
   }