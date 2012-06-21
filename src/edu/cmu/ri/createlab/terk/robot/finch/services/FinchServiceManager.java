package edu.cmu.ri.createlab.terk.robot.finch.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import edu.cmu.ri.createlab.terk.robot.finch.FinchController;
import edu.cmu.ri.createlab.terk.services.AbstractServiceManager;
import edu.cmu.ri.createlab.terk.services.Service;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerService;
import edu.cmu.ri.createlab.terk.services.audio.AudioService;
import edu.cmu.ri.createlab.terk.services.buzzer.BuzzerService;
import edu.cmu.ri.createlab.terk.services.finch.FinchService;
import edu.cmu.ri.createlab.terk.services.led.FullColorLEDService;
import edu.cmu.ri.createlab.terk.services.motor.OpenLoopVelocityControllableMotorService;
import edu.cmu.ri.createlab.terk.services.obstacle.SimpleObstacleDetectorService;
import edu.cmu.ri.createlab.terk.services.photoresistor.PhotoresistorService;
import edu.cmu.ri.createlab.terk.services.thermistor.ThermistorService;
//import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class FinchServiceManager extends AbstractServiceManager
   {
//   private static final Logger LOG = Logger.getLogger(FinchServiceManager.class);

   private final FinchController finchController;
   private final FinchServiceFactory serviceFactory;
   private final Map<String, Service> loadedServices = Collections.synchronizedMap(new HashMap<String, Service>());

   public FinchServiceManager(final FinchController finchController, final FinchServiceFactoryHelper finchServiceFactoryHelper)
      {
      if (finchController == null)
         {
         throw new IllegalArgumentException("FinchController may not be null");
         }

      this.finchController = finchController;
      this.serviceFactory = new FinchServiceFactory(finchServiceFactoryHelper);

      // get the collection of supported services from the peer's proxy
      final Set<String> supportedServices = new HashSet<String>();
      supportedServices.add(AccelerometerService.TYPE_ID);
      supportedServices.add(AudioService.TYPE_ID);
      supportedServices.add(BuzzerService.TYPE_ID);
      supportedServices.add(FinchService.TYPE_ID);
      supportedServices.add(FullColorLEDService.TYPE_ID);
      supportedServices.add(PhotoresistorService.TYPE_ID);
      supportedServices.add(SimpleObstacleDetectorService.TYPE_ID);
      supportedServices.add(ThermistorService.TYPE_ID);
      supportedServices.add(OpenLoopVelocityControllableMotorService.TYPE_ID);

      // register the supported services with the superclass
      registerSupportedServices(supportedServices);
      }

   protected final Service loadService(final String typeId)
      {
//      if (LOG.isTraceEnabled())
//         {
//         LOG.trace("FinchServiceManager.loadService(" + typeId + ")");
//         }

      if (serviceFactory != null)
         {
         Service service;

         synchronized (loadedServices)
            {
            // see whether we've already loaded this service
            service = loadedServices.get(typeId);

            // load the service
            if (service == null)
               {
//               if (LOG.isDebugEnabled())
//                  {
//                  LOG.debug("FinchServiceManager.loadService() needs to load the [" + typeId + "] service");
//                  }

               service = serviceFactory.createService(typeId, finchController);

               // cache this service so future calls won't have to create it
               loadedServices.put(typeId, service);
               }
            }

         return service;
         }

      return null;
      }
   }

