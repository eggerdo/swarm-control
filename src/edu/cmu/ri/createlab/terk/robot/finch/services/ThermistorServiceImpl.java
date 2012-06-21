package edu.cmu.ri.createlab.terk.robot.finch.services;

import edu.cmu.ri.createlab.terk.TerkConstants;
import edu.cmu.ri.createlab.terk.properties.BasicPropertyManager;
import edu.cmu.ri.createlab.terk.properties.PropertyManager;
import edu.cmu.ri.createlab.terk.robot.finch.FinchConstants;
import edu.cmu.ri.createlab.terk.robot.finch.FinchController;
import edu.cmu.ri.createlab.terk.services.thermistor.BaseThermistorServiceImpl;
import edu.cmu.ri.createlab.terk.services.thermistor.ThermistorService;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class ThermistorServiceImpl extends BaseThermistorServiceImpl
   {
   static ThermistorServiceImpl create(final FinchController finchController)
      {
      final BasicPropertyManager basicPropertyManager = new BasicPropertyManager();

      basicPropertyManager.setReadOnlyProperty(TerkConstants.PropertyKeys.DEVICE_COUNT, FinchConstants.THERMISTOR_DEVICE_COUNT);
      basicPropertyManager.setReadOnlyProperty(ThermistorService.PROPERTY_NAME_THERMISTOR_DEVICE_ID, FinchConstants.THERMISTOR_DEVICE_ID);
      basicPropertyManager.setReadOnlyProperty(ThermistorService.PROPERTY_NAME_MIN_VALUE, FinchConstants.THERMISTOR_MIN_VALUE);
      basicPropertyManager.setReadOnlyProperty(ThermistorService.PROPERTY_NAME_MAX_VALUE, FinchConstants.THERMISTOR_MAX_VALUE);

      return new ThermistorServiceImpl(finchController,
                                       basicPropertyManager,
                                       FinchConstants.THERMISTOR_DEVICE_COUNT);
      }

   private final FinchController finchController;

   private ThermistorServiceImpl(final FinchController finchController,
                                 final PropertyManager propertyManager,
                                 final int deviceCount)
      {
      super(propertyManager, deviceCount);
      this.finchController = finchController;
      }

   public Integer getThermistorValue(final int id)
      {
      return finchController.getThermistor(id);
      }
   }