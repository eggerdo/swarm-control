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
public abstract class BaseSpeedControllableMotorServiceImpl extends BaseDeviceControllingService implements SpeedControllableMotorService
   {
//   private static final Logger LOG = Logger.getLogger(BaseSpeedControllableMotorServiceImpl.class);

   private final boolean[] maskAllOn;
   private final boolean[] maskAllOff;
   private final int[] allZeros;
   private final Map<Integer, boolean[]> motorIdToMaskArrayMap;

   public BaseSpeedControllableMotorServiceImpl(final PropertyManager propertyManager, final int deviceCount)
      {
      super(propertyManager, deviceCount);

      // create and initialize the all-on mask array
      maskAllOn = new boolean[deviceCount];
      Arrays.fill(maskAllOn, true);

      // create and initialize the all-off mask array
      maskAllOff = new boolean[deviceCount];
      Arrays.fill(maskAllOff, false);

      // create and initialize the array used for zero speeds
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
         if (OPERATION_NAME_SET_SPEED.equalsIgnoreCase(operation.getName()))
            {
            return setSpeeds(operation);
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
         if (OPERATION_NAME_GET_SPEEDS.equalsIgnoreCase(operation.getName()))
            {
            // return all values, ignoring specified devices
            return getSpeeds();
            }
         else
            {
            throw new UnsupportedOperationException("The operation [" + operation.getName() + "] is not supported.");
            }
         }
      return null;
      }

   private int[] setSpeeds(final XmlOperation o)
      {
      final Set<XmlDevice> devices = o.getDevices();
      final Map<Integer, Integer> data = new HashMap<Integer, Integer>(devices.size() * 2);

      for (final XmlDevice d : devices)
         {
         final Set<XmlParameter> params = d.getParameters();
         int speed = 0;
         for (final XmlParameter p : params)
            {
            if (PARAMETER_NAME_SPEED.equalsIgnoreCase(p.getName()))
               {
               speed = Integer.parseInt(p.getValue());
               }
            }
         data.put(d.getId(), speed);
         }

      return setSpeeds(data);
      }

   private int[] setSpeeds(final Map<Integer, Integer> speedData)
      {
      final Set<Map.Entry<Integer, Integer>> entries = speedData.entrySet();

      final int deviceCount = getDeviceCount();
      final boolean[] mask = new boolean[deviceCount];
      Arrays.fill(mask, false);

      final int[] speeds = new int[deviceCount];
      Arrays.fill(speeds, 0);

      for (final Map.Entry<Integer, Integer> e : entries)
         {
         if (e.getKey() < deviceCount)
            {
            mask[e.getKey()] = true;
            speeds[e.getKey()] = e.getValue();
//            if (LOG.isDebugEnabled())
//               {
//               LOG.debug("Setting speed-controllable motor device " + e.getKey() + " to " + e.getValue());
//               }
            }
         }

      return execute(mask, speeds);
      }

   public final void setSpeed(final int motorId, final int speed)
      {
      final int[] speeds = new int[getDeviceCount()];
      speeds[motorId] = speed;

      execute(getMask(motorId), speeds);
      }

   public final void setSpeeds(final int[] speeds)
      {
      execute(maskAllOn, speeds);
      }

   public final void setSpeeds(final boolean[] motorMask, final int[] speeds)
      {
      execute(motorMask, speeds);
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

   public final int[] getSpeeds()
      {
      return execute(maskAllOff, allZeros);
      }

   protected abstract int[] execute(final boolean[] mask, final int[] speeds);

   private boolean[] getMask(final int motorid)
      {
      return motorIdToMaskArrayMap.get(motorid);
      }
   }