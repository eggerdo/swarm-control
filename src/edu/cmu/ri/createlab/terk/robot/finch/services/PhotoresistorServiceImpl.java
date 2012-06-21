package edu.cmu.ri.createlab.terk.robot.finch.services;

import edu.cmu.ri.createlab.terk.TerkConstants;
import edu.cmu.ri.createlab.terk.properties.BasicPropertyManager;
import edu.cmu.ri.createlab.terk.properties.PropertyManager;
import edu.cmu.ri.createlab.terk.robot.finch.FinchConstants;
import edu.cmu.ri.createlab.terk.robot.finch.FinchController;
import edu.cmu.ri.createlab.terk.services.photoresistor.BasePhotoresistorServiceImpl;
import edu.cmu.ri.createlab.terk.services.thermistor.ThermistorService;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class PhotoresistorServiceImpl extends BasePhotoresistorServiceImpl
   {
   static PhotoresistorServiceImpl create(final FinchController finchController)
      {
      final BasicPropertyManager basicPropertyManager = new BasicPropertyManager();

      basicPropertyManager.setReadOnlyProperty(TerkConstants.PropertyKeys.DEVICE_COUNT, FinchConstants.PHOTORESISTOR_DEVICE_COUNT);
      basicPropertyManager.setReadOnlyProperty(ThermistorService.PROPERTY_NAME_MIN_VALUE, FinchConstants.PHOTORESISTOR_MIN_VALUE);
      basicPropertyManager.setReadOnlyProperty(ThermistorService.PROPERTY_NAME_MAX_VALUE, FinchConstants.PHOTORESISTOR_MAX_VALUE);

      return new PhotoresistorServiceImpl(finchController,
                                          basicPropertyManager,
                                          FinchConstants.PHOTORESISTOR_DEVICE_COUNT);
      }

   private final FinchController finchController;

   private PhotoresistorServiceImpl(final FinchController finchController,
                                    final PropertyManager propertyManager,
                                    final int deviceCount)
      {
      super(propertyManager, deviceCount);
      this.finchController = finchController;
      }

   public Integer getPhotoresistorValue(final int id)
      {
      final int[] values = getPhotoresistorValues();
      if ((values != null) && (id >= 0) && (id < values.length))
         {
         return values[id];
         }

      return null;
      }

   public int[] getPhotoresistorValues()
      {
      return finchController.getPhotoresistors();
      }
   }