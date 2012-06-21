package edu.cmu.ri.createlab.terk.robot.finch.services;

//import java.awt.Color;
import edu.cmu.ri.createlab.terk.TerkConstants;
import edu.cmu.ri.createlab.terk.properties.BasicPropertyManager;
import edu.cmu.ri.createlab.terk.properties.PropertyManager;
import edu.cmu.ri.createlab.terk.robot.finch.FinchConstants;
import edu.cmu.ri.createlab.terk.robot.finch.FinchController;
import edu.cmu.ri.createlab.terk.services.led.BaseFullColorLEDServiceImpl;
import edu.cmu.ri.createlab.terk.services.led.FullColorLEDService;
//import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class FullColorLEDServiceImpl extends BaseFullColorLEDServiceImpl
   {
//   private static final Logger LOG = Logger.getLogger(FullColorLEDServiceImpl.class);

   static FullColorLEDServiceImpl create(final FinchController finchController)
      {
      final BasicPropertyManager basicPropertyManager = new BasicPropertyManager();

      basicPropertyManager.setReadOnlyProperty(TerkConstants.PropertyKeys.DEVICE_COUNT, FinchConstants.FULL_COLOR_LED_DEVICE_COUNT);
      basicPropertyManager.setReadOnlyProperty(FullColorLEDService.PROPERTY_NAME_MIN_INTENSITY, FinchConstants.FULL_COLOR_LED_DEVICE_MIN_INTENSITY);
      basicPropertyManager.setReadOnlyProperty(FullColorLEDService.PROPERTY_NAME_MAX_INTENSITY, FinchConstants.FULL_COLOR_LED_DEVICE_MAX_INTENSITY);

      return new FullColorLEDServiceImpl(finchController,
                                         basicPropertyManager,
                                         FinchConstants.FULL_COLOR_LED_DEVICE_COUNT);
      }

   private final FinchController finchController;

   private FullColorLEDServiceImpl(final FinchController finchController,
                                   final PropertyManager propertyManager,
                                   final int deviceCount)
      {
      super(propertyManager, deviceCount);
      this.finchController = finchController;
      }

   public int[] set(final boolean[] mask, final int[] colors)
      {
      if (mask != null && colors != null)
         {
         if (mask.length > 0 && colors.length > 0)
            {
            if (mask[0])
               {
               if (finchController.setFullColorLED(colors[0]))
                  {
                  return new int[]{colors[0]};
                  }
               }
            }
         }
      return null;
      }
   }