package edu.cmu.ri.createlab.terk.services.accelerometer;

import java.util.Set;
import edu.cmu.ri.createlab.terk.properties.PropertyManager;
import edu.cmu.ri.createlab.terk.services.BaseDeviceControllingService;
import edu.cmu.ri.createlab.terk.xml.XmlDevice;
import edu.cmu.ri.createlab.terk.xml.XmlOperation;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseAccelerometerServiceImpl extends BaseDeviceControllingService implements AccelerometerService
   {
   private final AccelerometerUnitConversionStrategy unitConversionStrategy;

   public BaseAccelerometerServiceImpl(final PropertyManager propertyManager, final int deviceCount)
      {
      super(propertyManager, deviceCount);

      final String deviceId = propertyManager.getProperty(AccelerometerService.PROPERTY_NAME_ACCELEROMETER_DEVICE_ID);
      unitConversionStrategy = AccelerometerUnitConversionStrategyFinder.getInstance().lookup(deviceId);
      }

   public final String getTypeId()
      {
      return TYPE_ID;
      }

   public final AccelerometerGs getAccelerometerGs(final int id)
      {
      final AccelerometerState state = getAccelerometerState(id);
      if (state != null)
         {
         return convertToGs(state);
         }
      return null;
      }

   public final AccelerometerGs convertToGs(final AccelerometerState state)
      {
      if (unitConversionStrategy != null)
         {
         return unitConversionStrategy.convert(state);
         }
      throw new UnsupportedOperationException("Method not supported since no AccelerometerUnitConversionStrategy is defined for this implementation.");
      }

   public final boolean isUnitConversionSupported()
      {
      return (unitConversionStrategy != null);
      }

   public final AccelerometerUnitConversionStrategy getAccelerometerUnitConversionStrategy()
      {
      return unitConversionStrategy;
      }

   /**
    * Returns an {@link AccelerometerState} if the {@link XmlOperation#getName()} operation name} is
    * {@link #OPERATION_NAME_GET_ACCELEROMETER_STATE} and return an {@link AccelerometerGs} if the
    * {@link XmlOperation#getName()} operation name} is {@link #OPERATION_NAME_GET_ACCELEROMETER_GS}.
    * Throws an {@link UnsupportedOperationException} for any other operation name.
    */
   @Override
   public final Object executeImpressionOperation(final XmlOperation operation)
      {
      if (operation != null)
         {
         if (OPERATION_NAME_GET_ACCELEROMETER_STATE.equalsIgnoreCase(operation.getName()))
            {
            // The operation is "getAccelerometerState" (i.e. singular), so just get the first device
            final Set<XmlDevice> devices = operation.getDevices();
            if (devices != null && !devices.isEmpty())
               {
               final XmlDevice device = devices.iterator().next();
               if (device != null)
                  {
                  return getAccelerometerState(device.getId());
                  }
               }
            }
         else if (OPERATION_NAME_GET_ACCELEROMETER_GS.equalsIgnoreCase(operation.getName()))
            {
            // The operation is "getAccelerometerGs" (i.e. singular), so just get the first device
            final Set<XmlDevice> devices = operation.getDevices();
            if (devices != null && !devices.isEmpty())
               {
               final XmlDevice device = devices.iterator().next();
               if (device != null)
                  {
                  return getAccelerometerGs(device.getId());
                  }
               }
            }
         else
            {
            throw new UnsupportedOperationException("The operation [" + operation.getName() + "] is not supported.");
            }
         }
      return null;
      }
   }