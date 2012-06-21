package edu.cmu.ri.createlab.terk.services.obstacle;

import java.util.Set;
import edu.cmu.ri.createlab.terk.properties.PropertyManager;
import edu.cmu.ri.createlab.terk.services.BaseDeviceControllingService;
import edu.cmu.ri.createlab.terk.xml.XmlDevice;
import edu.cmu.ri.createlab.terk.xml.XmlOperation;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseSimpleObstacleDetectorServiceImpl extends BaseDeviceControllingService implements SimpleObstacleDetectorService
   {
   public BaseSimpleObstacleDetectorServiceImpl(final PropertyManager propertyManager, final int deviceCount)
      {
      super(propertyManager, deviceCount);
      }

   public final String getTypeId()
      {
      return TYPE_ID;
      }

   /**
    * <p>
    * Returns an {@link Boolean} if the {@link XmlOperation#getName()} operation name} is
    * {@link #OPERATION_NAME_IS_OBSTACLE_DETECTED} and returns an array of <code>boolean</code>s if the
    * {@link XmlOperation#getName()} operation name} is {@link #OPERATION_NAME_ARE_OBSTACLES_DETECTED}.
    * Throws an {@link UnsupportedOperationException} for any other operation name.
    * </p>
    * <p>
    * Note that
    * in the case of the {@link #OPERATION_NAME_ARE_OBSTACLES_DETECTED}, this method returns the state of <i>all</i>
    * obstacle detectors, not only the one(s) specified in the {@link XmlOperation}'s set of {@link XmlDevice}.  This
    * effectively means that the set of {@link XmlDevice}s in the {@link XmlOperation} is ignored.
    * </p>
    */
   @Override
   public final Object executeImpressionOperation(final XmlOperation operation)
      {
      if (operation != null)
         {
         if (OPERATION_NAME_IS_OBSTACLE_DETECTED.equalsIgnoreCase(operation.getName()))
            {
            // The operation is "isObstacleDetected" (i.e. singular), so just get the first device
            final Set<XmlDevice> devices = operation.getDevices();
            if (devices != null && !devices.isEmpty())
               {
               final XmlDevice device = devices.iterator().next();
               if (device != null)
                  {
                  return isObstacleDetected(device.getId());
                  }
               }
            }
         else if (OPERATION_NAME_ARE_OBSTACLES_DETECTED.equalsIgnoreCase(operation.getName()))
            {
            // return all values, ignoring specified devices
            return areObstaclesDetected();
            }
         else
            {
            throw new UnsupportedOperationException("The operation [" + operation.getName() + "] is not supported.");
            }
         }
      return null;
      }
   }