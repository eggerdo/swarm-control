package edu.cmu.ri.createlab.terk.services.analog;

import java.util.Set;
import edu.cmu.ri.createlab.terk.properties.PropertyManager;
import edu.cmu.ri.createlab.terk.services.BaseDeviceControllingService;
import edu.cmu.ri.createlab.terk.xml.XmlDevice;
import edu.cmu.ri.createlab.terk.xml.XmlOperation;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseAnalogInputsServiceImpl extends BaseDeviceControllingService implements AnalogInputsService
   {
   public BaseAnalogInputsServiceImpl(final PropertyManager propertyManager, final int deviceCount)
      {
      super(propertyManager, deviceCount);
      }

   public final String getTypeId()
      {
      return AnalogInputsService.TYPE_ID;
      }

   /**
    * <p>
    * Returns an {@link Integer} if the {@link XmlOperation#getName()} operation name} is
    * {@link #OPERATION_NAME_GET_ANALOG_INPUT_VALUE} and returns an array of <code>int</code>s if the
    * {@link XmlOperation#getName()} operation name} is {@link #OPERATION_NAME_GET_ANALOG_INPUT_VALUES}.
    * Throws an {@link UnsupportedOperationException} for any other operation name.
    * </p>
    * <p>
    * Note that
    * in the case of the {@link #OPERATION_NAME_GET_ANALOG_INPUT_VALUES}, this method returns the value of <i>all</i>
    * analog inputs, not only the one(s) specified in the {@link XmlOperation}'s set of {@link XmlDevice}.  This
    * effectively means that the set of {@link XmlDevice}s in the {@link XmlOperation} is ignored.
    * </p>
    */
   @Override
   public final Object executeImpressionOperation(final XmlOperation operation)
      {
      if (operation != null)
         {
         if (OPERATION_NAME_GET_ANALOG_INPUT_VALUE.equalsIgnoreCase(operation.getName()))
            {
            // The operation is "getAnalogInputValue" (i.e. singular), so just get the first device
            final Set<XmlDevice> devices = operation.getDevices();
            if (devices != null && !devices.isEmpty())
               {
               final XmlDevice device = devices.iterator().next();
               if (device != null)
                  {
                  return getAnalogInputValue(device.getId());
                  }
               }
            }
         else if (OPERATION_NAME_GET_ANALOG_INPUT_VALUES.equalsIgnoreCase(operation.getName()))
            {
            // return all values, ignoring specified devices
            return getAnalogInputValues();
            }
         else
            {
            throw new UnsupportedOperationException("The operation [" + operation.getName() + "] is not supported.");
            }
         }
      return null;
      }
   }
