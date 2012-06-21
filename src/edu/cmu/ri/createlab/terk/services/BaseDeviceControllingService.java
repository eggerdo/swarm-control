package edu.cmu.ri.createlab.terk.services;

import edu.cmu.ri.createlab.terk.properties.PropertyManager;
import edu.cmu.ri.createlab.terk.properties.PropertyManagerWrapper;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseDeviceControllingService extends PropertyManagerWrapper implements Service, DeviceController
   {
   private final int deviceCount;

   protected BaseDeviceControllingService(final PropertyManager propertyManager, final int deviceCount)
      {
      super(propertyManager);
      this.deviceCount = deviceCount;
      }

   public final int getDeviceCount()
      {
      return deviceCount;
      }
   }
