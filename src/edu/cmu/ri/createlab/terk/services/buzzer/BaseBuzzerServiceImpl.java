package edu.cmu.ri.createlab.terk.services.buzzer;

import java.util.Map;
import java.util.Set;
import edu.cmu.ri.createlab.terk.properties.PropertyManager;
import edu.cmu.ri.createlab.terk.services.BaseDeviceControllingService;
import edu.cmu.ri.createlab.terk.xml.XmlDevice;
import edu.cmu.ri.createlab.terk.xml.XmlOperation;
import edu.cmu.ri.createlab.terk.xml.XmlParameter;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseBuzzerServiceImpl extends BaseDeviceControllingService implements BuzzerService
   {
   public BaseBuzzerServiceImpl(final PropertyManager propertyManager, final int deviceCount)
      {
      super(propertyManager, deviceCount);
      }

   public final String getTypeId()
      {
      return TYPE_ID;
      }

   @Override
   public final Boolean executeExpressionOperation(final XmlOperation operation)
      {
      if (operation != null)
         {
         if (OPERATION_NAME_PLAY_TONE.equalsIgnoreCase(operation.getName()))
            {
            final Set<XmlDevice> xmlDevices = operation.getDevices();
            if ((xmlDevices != null) && (!xmlDevices.isEmpty()))
               {
               for (final XmlDevice xmlDevice : xmlDevices)
                  {
                  if (xmlDevice != null)
                     {
                     final Map<String, XmlParameter> parametersMap = xmlDevice.getParametersAsMap();
                     if (parametersMap != null)
                        {
                        final XmlParameter frequencyParameter = parametersMap.get(PARAMETER_NAME_FREQUENCY);
                        final XmlParameter durationParameter = parametersMap.get(PARAMETER_NAME_DURATION);
                        if (frequencyParameter != null && durationParameter != null)
                           {
                           final Integer freq = frequencyParameter.getValueAsInteger();
                           final Integer dur = durationParameter.getValueAsInteger();
                           if (freq != null && dur != null)
                              {
                              playTone(xmlDevice.getId(), freq, dur);
                              return Boolean.TRUE;
                              }
                           }
                        }
                     }
                  }
               }
            }
         else
            {
            throw new UnsupportedOperationException("The operation [" + operation.getName() + "] is not supported.");
            }
         }
      return Boolean.FALSE;
      }
   }