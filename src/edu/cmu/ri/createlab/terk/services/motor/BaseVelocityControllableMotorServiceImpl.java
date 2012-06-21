package edu.cmu.ri.createlab.terk.services.motor;

import java.util.Arrays; 
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import edu.cmu.ri.createlab.terk.properties.PropertyManager;
import edu.cmu.ri.createlab.terk.services.BaseDeviceControllingService;
import edu.cmu.ri.createlab.terk.xml.XmlDevice;
import edu.cmu.ri.createlab.terk.xml.XmlOperation;
import edu.cmu.ri.createlab.terk.xml.XmlParameter;
//import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseVelocityControllableMotorServiceImpl extends BaseDeviceControllingService implements VelocityControllableMotorService
   {
//   private static final Logger LOG = Logger.getLogger(BaseVelocityControllableMotorServiceImpl.class);

   private VelocityControllableMotorUnitConversionStrategy unitConversionStrategy;
   private final boolean[] maskAllOn;
   private final boolean[] maskAllOff;
   private final int[] allZeros;
   private final Map<Integer, boolean[]> motorIdToMaskArrayMap;

   public BaseVelocityControllableMotorServiceImpl(final PropertyManager propertyManager, final int deviceCount)
      {
      super(propertyManager, deviceCount);

      final String deviceId = propertyManager.getProperty(VelocityControllableMotorService.PROPERTY_NAME_MOTOR_DEVICE_ID);
      unitConversionStrategy = VelocityControllableMotorUnitConversionStrategyFinder.getInstance().lookup(deviceId);

      // create and initialize the all-on mask array
      maskAllOn = new boolean[deviceCount];
      Arrays.fill(maskAllOn, true);

      // create and initialize the all-off mask array
      maskAllOff = new boolean[deviceCount];
      Arrays.fill(maskAllOff, false);

      // create and initialize the array used for zero velocitys
      allZeros = new int[deviceCount];
      Arrays.fill(allZeros, 0);

      // build the mask arrays for each motor and store them in a map indexed on motor id
      final Map<Integer, boolean[]> motorIdToMaskMapTemp = new HashMap<Integer, boolean[]>(deviceCount);
      for (int i = 0; i < deviceCount; i++)
         {
         final boolean[] mask = new boolean[deviceCount];
         mask[i] = true;
         motorIdToMaskMapTemp.put(i, mask);
         }
      motorIdToMaskArrayMap = Collections.unmodifiableMap(motorIdToMaskMapTemp);
      }

   public final String getTypeId()
      {
      return TYPE_ID;
      }

   @Override
   public final int[] executeExpressionOperation(final XmlOperation operation)
      {
      if (operation != null)
         {
         if (OPERATION_NAME_SET_VELOCITY.equalsIgnoreCase(operation.getName()))
            {
            return setVelocity(operation);
            }
         else
            {
            throw new UnsupportedOperationException();
            }
         }
      return null;
      }

   @Override
   public final int[] executeImpressionOperation(final XmlOperation operation)
      {
      if (operation != null)
         {
         if (OPERATION_NAME_GET_VELOCITIES.equalsIgnoreCase(operation.getName()))
            {
            // return all values, ignoring specified devices
            return getVelocities();
            }
         else
            {
            throw new UnsupportedOperationException("The operation [" + operation.getName() + "] is not supported.");
            }
         }
      return null;
      }

   private int[] setVelocity(final XmlOperation o)
      {
      final Set<XmlDevice> devices = o.getDevices();
      final Map<Integer, Integer> data = new HashMap<Integer, Integer>(devices.size() * 2);

      for (final XmlDevice d : devices)
         {
         final Set<XmlParameter> params = d.getParameters();
         int velocity = 0;
         for (final XmlParameter p : params)
            {
            if (PARAMETER_NAME_VELOCITY.equalsIgnoreCase(p.getName()))
               {
               velocity = Integer.parseInt(p.getValue());
               }
            }
         data.put(d.getId(), velocity);
         }

      return setVelocities(data);
      }

   private int[] setVelocities(final Map<Integer, Integer> velocityData)
      {
      final Set<Map.Entry<Integer, Integer>> entries = velocityData.entrySet();

      final boolean[] mask = new boolean[getDeviceCount()];
      Arrays.fill(mask, false);

      final int[] velocities = new int[getDeviceCount()];
      Arrays.fill(velocities, 0);

      for (final Map.Entry<Integer, Integer> e : entries)
         {
         if (e.getKey() < getDeviceCount())
            {
            mask[e.getKey()] = true;
            velocities[e.getKey()] = e.getValue();
//            if (LOG.isDebugEnabled())
//               {
//               LOG.debug("Setting velocity-controllable motor device " + e.getKey() + " to " + e.getValue());
//               }
            }
         }

      return execute(mask, velocities);
      }

   public final void setVelocity(final int motorId, final int velocity)
      {
      final int[] velocities = new int[getDeviceCount()];
      velocities[motorId] = velocity;

      execute(getMask(motorId), velocities);
      }

   public final void setVelocities(final int[] velocities)
      {
      execute(maskAllOn, velocities);
      }

   public final void setVelocities(final boolean[] motorMask, final int[] velocities)
      {
      execute(motorMask, velocities);
      }

   public final void setVelocity(final int motorId, final double velocity)
      {
      setVelocity(motorId, convertToNativeVelocity(velocity));
      }

   public final void setVelocities(final double[] velocities)
      {
      setVelocities(convertToNativeVelocity(velocities));
      }

   public final void setVelocities(final boolean[] motorMask, final double[] velocities)
      {
      setVelocities(motorMask, convertToNativeVelocity(velocities));
      }

   public final void stop(final int... motorIds)
      {
      final boolean[] mask;
      if (motorIds == null || motorIds.length == 0)
         {
         mask = maskAllOn;
         }
      else
         {
         mask = new boolean[getDeviceCount()];
         Arrays.fill(mask, false);
         for (final int i : motorIds)
            {
            mask[i] = true;
            }
         }

      execute(mask, allZeros);
      }

   public final int[] getVelocities()
      {
      return execute(maskAllOff, allZeros);
      }

   public final double[] getVelocitiesInCentimetersPerSecond()
      {
      return convertToCentimetersPerSecond(getVelocities());
      }

   public final Double convertToCentimetersPerSecond(final Integer nativeVelocity)
      {
      if (unitConversionStrategy != null)
         {
         return unitConversionStrategy.convertToCentimetersPerSecond(nativeVelocity);
         }
      throw new UnsupportedOperationException("Method not supported since no VelocityControllableMotorUnitConversionStrategy is defined for this implementation.");
      }

   public final double[] convertToCentimetersPerSecond(final int[] nativeVelocities)
      {
      if (unitConversionStrategy != null)
         {
         return unitConversionStrategy.convertToCentimetersPerSecond(nativeVelocities);
         }
      throw new UnsupportedOperationException("Method not supported since no VelocityControllableMotorUnitConversionStrategy is defined for this implementation.");
      }

   public final Integer convertToNativeVelocity(final Double velocityInCentimeters)
      {
      if (unitConversionStrategy != null)
         {
         return unitConversionStrategy.convertToNativeVelocity(velocityInCentimeters);
         }
      throw new UnsupportedOperationException("Method not supported since no VelocityControllableMotorUnitConversionStrategy is defined for this implementation.");
      }

   public final int[] convertToNativeVelocity(final double[] velocitiesInCentimeters)
      {
      if (unitConversionStrategy != null)
         {
         return unitConversionStrategy.convertToNativeVelocity(velocitiesInCentimeters);
         }
      throw new UnsupportedOperationException("Method not supported since no VelocityControllableMotorUnitConversionStrategy is defined for this implementation.");
      }

   private boolean[] getMask(final int motorid)
      {
      return motorIdToMaskArrayMap.get(motorid);
      }

   protected abstract int[] execute(final boolean[] mask, final int[] velocities);
   }