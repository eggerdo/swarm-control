package edu.cmu.ri.createlab.terk.robot.finch.services;

import edu.cmu.ri.createlab.terk.TerkConstants;
import edu.cmu.ri.createlab.terk.properties.BasicPropertyManager;
import edu.cmu.ri.createlab.terk.properties.PropertyManager;
import edu.cmu.ri.createlab.terk.robot.finch.FinchConstants;
import edu.cmu.ri.createlab.terk.robot.finch.FinchController;
import edu.cmu.ri.createlab.terk.services.obstacle.BaseSimpleObstacleDetectorServiceImpl;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class SimpleObstacleDetectorServiceImpl extends BaseSimpleObstacleDetectorServiceImpl
   {
   static SimpleObstacleDetectorServiceImpl create(final FinchController finchController)
      {
      final BasicPropertyManager basicPropertyManager = new BasicPropertyManager();

      basicPropertyManager.setReadOnlyProperty(TerkConstants.PropertyKeys.DEVICE_COUNT, FinchConstants.SIMPLE_OBSTACLE_SENSOR_DEVICE_COUNT);

      return new SimpleObstacleDetectorServiceImpl(finchController,
                                                   basicPropertyManager,
                                                   FinchConstants.SIMPLE_OBSTACLE_SENSOR_DEVICE_COUNT);
      }

   private final FinchController finchController;

   private SimpleObstacleDetectorServiceImpl(final FinchController finchController,
                                             final PropertyManager propertyManager,
                                             final int deviceCount)
      {
      super(propertyManager, deviceCount);
      this.finchController = finchController;
      }

   public Boolean isObstacleDetected(final int id)
      {
      return finchController.isObstacleDetected(id);
      }

   public boolean[] areObstaclesDetected()
      {
      return finchController.areObstaclesDetected();
      }
   }