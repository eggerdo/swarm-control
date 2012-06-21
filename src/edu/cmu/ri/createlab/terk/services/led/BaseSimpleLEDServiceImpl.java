package edu.cmu.ri.createlab.terk.services.led;

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
public abstract class BaseSimpleLEDServiceImpl extends BaseDeviceControllingService implements SimpleLEDService
   {
//   private static final Logger LOG = Logger.getLogger(BaseSimpleLEDServiceImpl.class);

   private static final int OFF_INTENSITY = 0;

   private final boolean[] maskAllOn;
   private final boolean[] maskAllOff;
   private final int[] allOff;
   private final Map<Integer, boolean[]> ledIdToMaskArrayMap;

   public BaseSimpleLEDServiceImpl(final PropertyManager propertyManager, final int deviceCount)
      {
      super(propertyManager, deviceCount);

      // create and initialize the all-on mask array
      maskAllOn = new boolean[deviceCount];
      Arrays.fill(maskAllOn, true);

      // create and initialize the all-off mask array
      maskAllOff = new boolean[deviceCount];
      Arrays.fill(maskAllOff, false);

      // create and initialize the all-off array
      allOff = new int[deviceCount];
      Arrays.fill(allOff, OFF_INTENSITY);

      // build the mask arrays for each LED and store them in a map indexed on LED id
      final Map<Integer, boolean[]> ledIdToMaskMapTemp = new HashMap<Integer, boolean[]>(deviceCount);
      for (int i = 0; i < deviceCount; i++)
         {
         final boolean[] mask = new boolean[deviceCount];
         mask[i] = true;
         ledIdToMaskMapTemp.put(i, mask);
         }
      ledIdToMaskArrayMap = Collections.unmodifiableMap(ledIdToMaskMapTemp);
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
         if (OPERATION_NAME_SET_INTENSITY.equalsIgnoreCase(operation.getName()))
            {
            return setIntensities(operation);
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
         if (OPERATION_NAME_GET_INTENSITIES.equalsIgnoreCase(operation.getName()))
            {
            // return all values, ignoring specified devices
            return getIntensities();
            }
         else
            {
            throw new UnsupportedOperationException("The operation [" + operation.getName() + "] is not supported.");
            }
         }
      return null;
      }

   private int[] setIntensities(final XmlOperation o)
      {
      final Set<XmlDevice> devices = o.getDevices();
      final Map<Integer, Integer> data = new HashMap<Integer, Integer>(devices.size() * 2);

      for (final XmlDevice d : devices)
         {
         final Set<XmlParameter> params = d.getParameters();
         int intensity = 0;
         for (final XmlParameter p : params)
            {
            if (PARAMETER_NAME_INTENSITY.equalsIgnoreCase(p.getName()))
               {
               intensity = Integer.parseInt(p.getValue());
               }
            }
         data.put(d.getId(), intensity);
         }

      return set(data);
      }

   private int[] set(final Map<Integer, Integer> data)
      {
      final Set<Map.Entry<Integer, Integer>> entries = data.entrySet();

      final int deviceCount = getDeviceCount();
      final boolean[] mask = new boolean[deviceCount];
      Arrays.fill(mask, false);

      final int[] values = new int[deviceCount];
      Arrays.fill(values, 0);

      for (final Map.Entry<Integer, Integer> e : entries)
         {
         if (e.getKey() < deviceCount)
            {
            mask[e.getKey()] = true;
            values[e.getKey()] = e.getValue();
//            LOG.debug("Setting led device " + e.getKey() + " to " + e.getValue());
            }
         }

      return execute(mask, values);
      }

   public final void set(final int id, final int intensity)
      {
      final int[] intensities = new int[getDeviceCount()];
      intensities[id] = intensity;

      execute(getMask(id), intensities);
      }

   public final void setOff(final int... ids)
      {
      final boolean[] mask;
      if (ids == null || ids.length == 0)
         {
         mask = maskAllOn;
         }
      else
         {
         mask = maskAllOff.clone();
         for (final int i : ids)
            {
            mask[i] = true;
            }
         }

      execute(mask, allOff);
      }

   public final int[] getIntensities()
      {
      return execute(maskAllOff, allOff);
      }

   protected abstract int[] execute(final boolean[] mask, final int[] intensities);

   private boolean[] getMask(final int id)
      {
      return ledIdToMaskArrayMap.get(id);
      }
   }