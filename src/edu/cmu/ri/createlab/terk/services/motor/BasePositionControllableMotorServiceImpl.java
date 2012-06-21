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
public abstract class BasePositionControllableMotorServiceImpl extends BaseDeviceControllingService implements PositionControllableMotorService
   {
//   private static final Logger LOG = Logger.getLogger(BasePositionControllableMotorServiceImpl.class);

   private PositionControllableMotorUnitConversionStrategy unitConversionStrategy;
   private final boolean[] maskAllOn;
   private final int[] allZeros;
   private final Map<Integer, boolean[]> motorIdToMaskArrayMap;

   public BasePositionControllableMotorServiceImpl(final PropertyManager propertyManager, final int deviceCount)
      {
      super(propertyManager, deviceCount);

      final String deviceId = propertyManager.getProperty(PositionControllableMotorService.PROPERTY_NAME_MOTOR_DEVICE_ID);
      unitConversionStrategy = PositionControllableMotorUnitConversionStrategyFinder.getInstance().lookup(deviceId);

      // create and initialize the all-on mask array
      maskAllOn = new boolean[deviceCount];
      Arrays.fill(maskAllOn, true);

      // create and initialize the array used for zero positions
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

   /**
    * Executes the given operation.  Note that this implementation will always return <code>null</code>, even upon
    * successful execution.
    */
   @Override
   public final Object executeExpressionOperation(final XmlOperation operation)
      {
      if (operation != null)
         {
         if (OPERATION_NAME_SET_POSITION.equalsIgnoreCase(operation.getName()))
            {
            setPosition(operation);
            }
         else
            {
            throw new UnsupportedOperationException();
            }
         }
      return null;
      }

   private void setPosition(final XmlOperation o)
      {
      final int deviceCount = getDeviceCount();

      final boolean[] mask = new boolean[deviceCount];
      Arrays.fill(mask, false);

      final int[] positions = new int[getDeviceCount()];
      Arrays.fill(positions, 0);

      final int[] speeds = new int[getDeviceCount()];
      Arrays.fill(speeds, 0);

      for (final XmlDevice device : o.getDevices())
         {
         final int id = device.getId();
         final Set<XmlParameter> params = device.getParameters();

         // don't bother processing this device if it specifies an invalid device
         if (id >= 0 && id < deviceCount)
            {
            boolean foundParam = false;
            for (final XmlParameter p : params)
               {
               if (PARAMETER_NAME_POSITION.equalsIgnoreCase(p.getName()))
                  {
                  foundParam = true;
                  positions[id] = Integer.parseInt(p.getValue());
                  }
               else if (PARAMETER_NAME_SPEED.equalsIgnoreCase(p.getName()))
                  {
                  foundParam = true;
                  speeds[id] = Integer.parseInt(p.getValue());
                  }
               }

            if (foundParam)
               {
               mask[id] = true;
//               LOG.debug("Setting position-controllable motor device [" + id + "] to position [" + positions[id] + "] and speed [" + speeds[id] + "]");
               }
            }
         }

      preExecute(mask, positions, speeds);
      }

   public final void setPosition(final int motorId, final int positionDelta, final int speed)
      {
      final int[] positionDeltas = new int[getDeviceCount()];
      positionDeltas[motorId] = positionDelta;
      final int[] speeds = new int[getDeviceCount()];
      speeds[motorId] = speed;

      preExecute(getMask(motorId), positionDeltas, speeds);
      }

   public final void setPositions(final int[] positionDeltas, final int[] speeds)
      {
      preExecute(maskAllOn, positionDeltas, speeds);
      }

   public final void setPositions(final boolean[] motorMask, final int[] positionDeltas, final int[] speeds)
      {
      preExecute(motorMask, positionDeltas, speeds);
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

      preExecute(mask, allZeros, allZeros);
      }

   private boolean[] getMask(final int motorid)
      {
      return motorIdToMaskArrayMap.get(motorid);
      }

   public final void setPosition(final int motorId, final double distanceDelta, final double speed)
      {
      setPosition(motorId, convertToTicks(distanceDelta), convertToNativeSpeed(speed));
      }

   public final void setPositions(final double[] distanceDeltas, final double[] speeds)
      {
      setPositions(convertToTicks(distanceDeltas), convertToNativeSpeed(speeds));
      }

   public final void setPositions(final boolean[] motorMask, final double[] distanceDeltas, final double[] speeds)
      {
      setPositions(motorMask, convertToTicks(distanceDeltas), convertToNativeSpeed(speeds));
      }

   public final Double getCurrentPositionInCentimeters(final int motorId)
      {
      final Integer rawValue = getCurrentPosition(motorId);
      if (rawValue != null)
         {
         return convertToCentimeters(rawValue);
         }
      return null;
      }

   public final Double getSpecifiedPositionInCentimeters(final int motorId)
      {
      final Integer rawValue = getSpecifiedPosition(motorId);
      if (rawValue != null)
         {
         return convertToCentimeters(rawValue);
         }
      return null;
      }

   public final Double getSpecifiedSpeedInCentimetersPerSecond(final int motorId)
      {
      final Integer rawValue = getSpecifiedSpeed(motorId);
      if (rawValue != null)
         {
         return convertToCentimetersPerSecond(rawValue);
         }
      return null;
      }

   public final Double convertToCentimeters(final Integer rawMotorPosition)
      {
      if (unitConversionStrategy != null)
         {
         return unitConversionStrategy.convertToCentimeters(rawMotorPosition);
         }
      throw new UnsupportedOperationException("Method not supported since no PositionControllableMotorUnitConversionStrategy is defined for this implementation.");
      }

   public final double[] convertToCentimeters(final int[] positionDeltas)
      {
      if (unitConversionStrategy != null)
         {
         return unitConversionStrategy.convertToCentimeters(positionDeltas);
         }
      throw new UnsupportedOperationException("Method not supported since no PositionControllableMotorUnitConversionStrategy is defined for this implementation.");
      }

   public final Integer convertToTicks(final Double distanceInCentimeters)
      {
      if (unitConversionStrategy != null)
         {
         return unitConversionStrategy.convertToTicks(distanceInCentimeters);
         }
      throw new UnsupportedOperationException("Method not supported since no PositionControllableMotorUnitConversionStrategy is defined for this implementation.");
      }

   public final int[] convertToTicks(final double[] distanceDeltas)
      {
      if (unitConversionStrategy != null)
         {
         return unitConversionStrategy.convertToTicks(distanceDeltas);
         }
      throw new UnsupportedOperationException("Method not supported since no PositionControllableMotorUnitConversionStrategy is defined for this implementation.");
      }

   public final Double convertToCentimetersPerSecond(final Integer nativeSpeed)
      {
      if (unitConversionStrategy != null)
         {
         return unitConversionStrategy.convertToCentimetersPerSecond(nativeSpeed);
         }
      throw new UnsupportedOperationException("Method not supported since no PositionControllableMotorUnitConversionStrategy is defined for this implementation.");
      }

   public final double[] convertToCentimetersPerSecond(final int[] nativeSpeeds)
      {
      if (unitConversionStrategy != null)
         {
         return unitConversionStrategy.convertToCentimetersPerSecond(nativeSpeeds);
         }
      throw new UnsupportedOperationException("Method not supported since no PositionControllableMotorUnitConversionStrategy is defined for this implementation.");
      }

   public final Integer convertToNativeSpeed(final Double speedInCentimeters)
      {
      if (unitConversionStrategy != null)
         {
         return unitConversionStrategy.convertToNativeSpeed(speedInCentimeters);
         }
      throw new UnsupportedOperationException("Method not supported since no PositionControllableMotorUnitConversionStrategy is defined for this implementation.");
      }

   public final int[] convertToNativeSpeed(final double[] speedsInCentimeters)
      {
      if (unitConversionStrategy != null)
         {
         return unitConversionStrategy.convertToNativeSpeed(speedsInCentimeters);
         }
      throw new UnsupportedOperationException("Method not supported since no PositionControllableMotorUnitConversionStrategy is defined for this implementation.");
      }

   public final boolean isUnitConversionSupported()
      {
      return (unitConversionStrategy != null);
      }

   private void preExecute(final boolean[] mask, final int[] positionDeltas, final int[] speeds)
      {
      // make sure all the speeds are unsigned
      final int[] unsignedSpeeds = (speeds == null) ? null : new int[speeds.length];
      if (unsignedSpeeds != null)
         {
         for (int i = 0; i < unsignedSpeeds.length; i++)
            {
            unsignedSpeeds[i] = Math.abs(speeds[i]);
            }
         }
      execute(mask, positionDeltas, unsignedSpeeds);
      }

   protected abstract void execute(final boolean[] mask, final int[] positionDeltas, final int[] speeds);
   }