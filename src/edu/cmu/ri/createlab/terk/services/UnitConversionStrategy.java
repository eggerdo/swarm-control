package edu.cmu.ri.createlab.terk.services;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface UnitConversionStrategy
   {
   /**
    * Returns the unique ID for the device which this conversion strategy applies.  This will typically be a make and
    * model number for the device, or some other unique identifier.
    */
   String getDeviceId();
   }