package edu.cmu.ri.createlab.terk.services;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ServiceCreator<DeviceClass>
   {
   Service createService(final DeviceClass device);
   }