package edu.cmu.ri.createlab.terk.services.thermistor;

import java.util.Set;
import edu.cmu.ri.createlab.terk.properties.PropertyManager;
import edu.cmu.ri.createlab.terk.services.BaseDeviceControllingService;
import edu.cmu.ri.createlab.terk.xml.XmlDevice;
import edu.cmu.ri.createlab.terk.xml.XmlOperation;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseThermistorServiceImpl extends BaseDeviceControllingService implements ThermistorService
   {
   private final ThermistorUnitConversionStrategy unitConversionStrategy;

   public BaseThermistorServiceImpl(final PropertyManager propertyManager, final int deviceCount)
      {
      super(propertyManager, deviceCount);

      final String deviceId = propertyManager.getProperty(ThermistorService.PROPERTY_NAME_THERMISTOR_DEVICE_ID);
      unitConversionStrategy = ThermistorUnitConversionStrategyFinder.getInstance().lookup(deviceId);
      }

   public final String getTypeId()
      {
      return TYPE_ID;
      }

   public final Double getCelsiusTemperature(final int id)
      {
      final Integer rawValue = getThermistorValue(id);
      if (rawValue != null)
         {
         return convertToCelsius(rawValue);
         }
      return null;
      }

   public final Double convertToCelsius(final Integer rawValue)
      {
      if (unitConversionStrategy != null)
         {
         return unitConversionStrategy.convertToCelsius(rawValue);
         }
      throw new UnsupportedOperationException("Method not supported since no ThermistorUnitConversionStrategy is defined for this implementation.");
      }

   public final boolean isUnitConversionSupported()
      {
      return (unitConversionStrategy != null);
      }

   @Override
   public final Integer executeImpressionOperation(final XmlOperation operation)
      {
      if (operation != null)
         {
         if (OPERATION_NAME_GET_THERMISTOR_VALUE.equalsIgnoreCase(operation.getName()))
            {
            // The operation is "getThermistorValue" (i.e. singular), so just get the first device
            final Set<XmlDevice> devices = operation.getDevices();
            if (devices != null && !devices.isEmpty())
               {
               final XmlDevice device = devices.iterator().next();
               if (device != null)
                  {
                  return getThermistorValue(device.getId());
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