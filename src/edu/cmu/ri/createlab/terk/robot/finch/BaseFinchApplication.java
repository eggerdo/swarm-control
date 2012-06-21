package edu.cmu.ri.createlab.terk.robot.finch;

import edu.cmu.ri.createlab.terk.application.TerkApplication;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerService;
import edu.cmu.ri.createlab.terk.services.audio.AudioService;
import edu.cmu.ri.createlab.terk.services.buzzer.BuzzerService;
import edu.cmu.ri.createlab.terk.services.led.FullColorLEDService;
import edu.cmu.ri.createlab.terk.services.motor.OpenLoopVelocityControllableMotorService;
import edu.cmu.ri.createlab.terk.services.obstacle.SimpleObstacleDetectorService;
import edu.cmu.ri.createlab.terk.services.photoresistor.PhotoresistorService;
import edu.cmu.ri.createlab.terk.services.thermistor.ThermistorService;

/**
 * <p>
 * <code>BaseFinchApplication</code> provides core functionality for Finch applications.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseFinchApplication extends TerkApplication
   {
   protected BaseFinchApplication()
      {
      super();
      }

   protected BaseFinchApplication(final String defaultConnectionStrategyClassName)
      {
      super(defaultConnectionStrategyClassName);
      }

   protected final AccelerometerService getAccelerometerService()
      {
      if (getServiceManager() != null)
         {
         return ((AccelerometerService)(getServiceManager().getServiceByTypeId(AccelerometerService.TYPE_ID)));
         }
      return null;
      }

   protected final AudioService getAudioService()
      {
      if (getServiceManager() != null)
         {
         return ((AudioService)(getServiceManager().getServiceByTypeId(AudioService.TYPE_ID)));
         }
      return null;
      }

   protected final BuzzerService getBuzzerService()
      {
      if (getServiceManager() != null)
         {
         return ((BuzzerService)(getServiceManager().getServiceByTypeId(BuzzerService.TYPE_ID)));
         }
      return null;
      }

   protected final FullColorLEDService getFullColorLEDService()
      {
      if (getServiceManager() != null)
         {
         return ((FullColorLEDService)(getServiceManager().getServiceByTypeId(FullColorLEDService.TYPE_ID)));
         }
      return null;
      }

   protected final PhotoresistorService getPhotoresistorService()
      {
      if (getServiceManager() != null)
         {
         return ((PhotoresistorService)(getServiceManager().getServiceByTypeId(PhotoresistorService.TYPE_ID)));
         }
      return null;
      }

   protected final SimpleObstacleDetectorService getSimpleObstacleDetectorService()
      {
      if (getServiceManager() != null)
         {
         return ((SimpleObstacleDetectorService)(getServiceManager().getServiceByTypeId(SimpleObstacleDetectorService.TYPE_ID)));
         }
      return null;
      }

   protected final ThermistorService getThermistorService()
      {
      if (getServiceManager() != null)
         {
         return ((ThermistorService)(getServiceManager().getServiceByTypeId(ThermistorService.TYPE_ID)));
         }
      return null;
      }

   protected final OpenLoopVelocityControllableMotorService getOpenLoopVelocityControllableMotorService()
      {
      if (getServiceManager() != null)
         {
         return ((OpenLoopVelocityControllableMotorService)(getServiceManager().getServiceByTypeId(OpenLoopVelocityControllableMotorService.TYPE_ID)));
         }
      return null;
      }
   }
