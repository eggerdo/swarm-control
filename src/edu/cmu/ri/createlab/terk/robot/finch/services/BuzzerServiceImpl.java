package edu.cmu.ri.createlab.terk.robot.finch.services;

import edu.cmu.ri.createlab.terk.TerkConstants;
import edu.cmu.ri.createlab.terk.properties.BasicPropertyManager;
import edu.cmu.ri.createlab.terk.properties.PropertyManager;
import edu.cmu.ri.createlab.terk.robot.finch.FinchConstants;
import edu.cmu.ri.createlab.terk.robot.finch.FinchController;
import edu.cmu.ri.createlab.terk.services.buzzer.BaseBuzzerServiceImpl;
import edu.cmu.ri.createlab.terk.services.buzzer.BuzzerService;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class BuzzerServiceImpl extends BaseBuzzerServiceImpl
   {
   static BuzzerServiceImpl create(final FinchController finchController)
      {
      final BasicPropertyManager basicPropertyManager = new BasicPropertyManager();

      basicPropertyManager.setReadOnlyProperty(TerkConstants.PropertyKeys.DEVICE_COUNT, FinchConstants.BUZZER_DEVICE_COUNT);
      basicPropertyManager.setReadOnlyProperty(BuzzerService.PROPERTY_NAME_MIN_DURATION, FinchConstants.BUZZER_DEVICE_MIN_DURATION);
      basicPropertyManager.setReadOnlyProperty(BuzzerService.PROPERTY_NAME_MAX_DURATION, FinchConstants.BUZZER_DEVICE_MAX_DURATION);
      basicPropertyManager.setReadOnlyProperty(BuzzerService.PROPERTY_NAME_MIN_FREQUENCY, FinchConstants.BUZZER_DEVICE_MIN_FREQUENCY);
      basicPropertyManager.setReadOnlyProperty(BuzzerService.PROPERTY_NAME_MAX_FREQUENCY, FinchConstants.BUZZER_DEVICE_MAX_FREQUENCY);

      return new BuzzerServiceImpl(finchController,
                                   basicPropertyManager,
                                   FinchConstants.BUZZER_DEVICE_COUNT);
      }

   private final FinchController finchController;

   private BuzzerServiceImpl(final FinchController finchController,
                             final PropertyManager propertyManager,
                             final int deviceCount)
      {
      super(propertyManager, deviceCount);
      this.finchController = finchController;
      }

   public void playTone(final int id, final int frequency, final int durationInMilliseconds)
      {
      finchController.playBuzzerTone(frequency, durationInMilliseconds);
      }
   }