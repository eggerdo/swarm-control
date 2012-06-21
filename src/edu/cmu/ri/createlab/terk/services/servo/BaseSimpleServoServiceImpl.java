package edu.cmu.ri.createlab.terk.services.servo;

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
public abstract class BaseSimpleServoServiceImpl extends BaseDeviceControllingService implements SimpleServoService
   {
//   private static final Logger LOG = Logger.getLogger(BaseSimpleServoServiceImpl.class);

   private final boolean[] maskAllOff;
   private final int[] allZeros;
   private final Map<Integer, boolean[]> servoIdToMaskArrayMap;

   public BaseSimpleServoServiceImpl(final PropertyManager propertyManager, final int deviceCount)
      {
      super(propertyManager, deviceCount);

      // create and initialize the all-off mask array
      maskAllOff = new boolean[deviceCount];
      Arrays.fill(maskAllOff, false);

      // create and initialize the array used for zero speeds
      allZeros = new int[deviceCount];
      Arrays.fill(allZeros, 0);

      // build the mask arrays for each servo and store them in a map indexed on servo id
      final Map<Integer, boolean[]> servoIdToMaskMapTemp = new HashMap<Integer, boolean[]>(deviceCount);
      for (int i = 0; i < deviceCount; i++)
         {
         final boolean[] mask = new boolean[deviceCount];
         mask[i] = true;
         servoIdToMaskMapTemp.put(i, mask);
         }
      servoIdToMaskArrayMap = Collections.unmodifiableMap(servoIdToMaskMapTemp);
      }

   public final String getTypeId()
      {
      return SimpleServoService.TYPE_ID;
      }

   @Override
   public final int[] executeExpressionOperation(final XmlOperation operation)
      {
      if (operation != null)
         {
         if (OPERATION_NAME_SET_POSITION.equalsIgnoreCase(operation.getName()))
            {
            return setPositions(operation);
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
         if (OPERATION_NAME_GET_POSITIONS.equalsIgnoreCase(operation.getName()))
            {
            // return all values, ignoring specified devices
            return getPositions();
            }
         else
            {
            throw new UnsupportedOperationException("The operation [" + operation.getName() + "] is not supported.");
            }
         }
      return null;
      }

   private int[] setPositions(final XmlOperation o)
      {
      final Set<XmlDevice> devices = o.getDevices();
      final Map<Integer, Integer> data = new HashMap<Integer, Integer>(devices.size() * 2);

      for (final XmlDevice d : devices)
         {
         final Set<XmlParameter> params = d.getParameters();
         int position = 0;
         for (final XmlParameter p : params)
            {
            if (PARAMETER_NAME_POSITION.equalsIgnoreCase(p.getName()))
               {
               position = Integer.parseInt(p.getValue());
               }
            }
         data.put(d.getId(), position);
         }

      return setPositions(data);
      }

   private int[] setPositions(final Map<Integer, Integer> positionData)
      {
      final Set<Map.Entry<Integer, Integer>> entries = positionData.entrySet();
      final int deviceCount = getDeviceCount();
      final boolean[] mask = new boolean[deviceCount];
      Arrays.fill(mask, false);

      final int[] positions = new int[deviceCount];
      Arrays.fill(positions, 0);

      for (final Map.Entry<Integer, Integer> e : entries)
         {
         if (e.getKey() < deviceCount)
            {
            mask[e.getKey()] = true;
            positions[e.getKey()] = e.getValue();
//            if (LOG.isDebugEnabled())
//               {
//               LOG.debug("Setting servo device " + e.getKey() + " to " + e.getValue());
//               }
            }
         }

      return execute(mask, positions);
      }

   public final void setPosition(final int servoId, final int position)
      {
      final int[] positions = new int[getDeviceCount()];
      positions[servoId] = position;
      execute(getMask(servoId), positions);
      }

   public final void setPositions(final int... servoIdsAndPositions)
      {
      if (servoIdsAndPositions != null && servoIdsAndPositions.length > 0)
         {
         if (servoIdsAndPositions.length % 2 == 0)
            {
            final int deviceCount = getDeviceCount();
            final boolean[] mask = new boolean[deviceCount];
            final int[] positions = new int[deviceCount];
            for (int i = 0; i < servoIdsAndPositions.length; i += 2)
               {
               final int id = servoIdsAndPositions[i];
               final int position = servoIdsAndPositions[i + 1];
               mask[id] = true;
               positions[id] = position;
               }
            execute(mask, positions);
            }
         else
            {
            throw new IllegalArgumentException("Number of arguments to setPositions() must be even!");
            }
         }
      }

   public final int[] getPositions()
      {
      return execute(maskAllOff, allZeros);
      }

   protected abstract int[] execute(final boolean[] mask, final int[] positions);

   private boolean[] getMask(final int id)
      {
      return servoIdToMaskArrayMap.get(id);
      }
   }