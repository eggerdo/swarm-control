package edu.cmu.ri.createlab.terk.services.led;

//import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.R.integer;
import android.graphics.Color;
import edu.cmu.ri.createlab.terk.properties.PropertyManager;
import edu.cmu.ri.createlab.terk.services.BaseDeviceControllingService;
import edu.cmu.ri.createlab.terk.xml.XmlDevice;
import edu.cmu.ri.createlab.terk.xml.XmlOperation;
import edu.cmu.ri.createlab.terk.xml.XmlParameter;
//import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseFullColorLEDServiceImpl extends BaseDeviceControllingService implements FullColorLEDService
   {
//   private static final Logger LOG = Logger.getLogger(BaseFullColorLEDServiceImpl.class);

   private static final int OFF_COLOR = Color.rgb(0, 0, 0);

   private final boolean[] maskAllOn;
   private final boolean[] maskAllOff;
   private final int[] allOff;
   private final Map<Integer, boolean[]> ledIdToMaskArrayMap;

   protected BaseFullColorLEDServiceImpl(final PropertyManager propertyManager, final int deviceCount)
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
      Arrays.fill(allOff, OFF_COLOR);

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
         if (OPERATION_NAME_SET_COLOR.equalsIgnoreCase(operation.getName()))
            {
            setColors(operation);
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
         if (OPERATION_NAME_GET_COLORS.equalsIgnoreCase(operation.getName()))
            {
            // return all values, ignoring specified devices
            return getColors();
            }
         else
            {
            throw new UnsupportedOperationException("The operation [" + operation.getName() + "] is not supported.");
            }
         }
      return null;
      }

   private int[] setColors(final XmlOperation o)
      {
      final Set<XmlDevice> devices = o.getDevices();
      final Map<Integer, Integer> data = new HashMap<Integer, Integer>(devices.size() * 2);

      for (final XmlDevice d : devices)
         {
         final Set<XmlParameter> params = d.getParameters();
         int red = 0;
         int blue = 0;
         int green = 0;
         for (final XmlParameter p : params)
            {
            if ("red".equalsIgnoreCase(p.getName()))
               {
               red = Integer.parseInt(p.getValue());
               }
            if ("blue".equalsIgnoreCase(p.getName()))
               {
               blue = Integer.parseInt(p.getValue());
               }
            if ("green".equalsIgnoreCase(p.getName()))
               {
               green = Integer.parseInt(p.getValue());
               }
            }
         data.put(d.getId(), Color.rgb(red, green, blue));
         }

      return setColors(data);
      }

   private int[] setColors(final Map<Integer, Integer> data)
      {
      final Set<Map.Entry<Integer, Integer>> entries = data.entrySet();

      final boolean[] mask = new boolean[getDeviceCount()];
      Arrays.fill(mask, false);

      final int[] values = new int[getDeviceCount()];
      Arrays.fill(values, Color.rgb(0, 0, 0));

      for (final Map.Entry<Integer, Integer> e : entries)
         {
         if (e.getKey() < getDeviceCount())
            {
            mask[e.getKey()] = true;
            values[e.getKey()] = e.getValue();
//            LOG.debug("Setting full color led device " + e.getKey() + " to (R,G,B) = (" + e.getValue().getRed() + ", " + e.getValue().getGreen() + ", " + e.getValue().getBlue() + ")");
            }
         }

      return set(mask, values);
      }

   public final void set(final int id, final int color)
      {
      final int[] colors = new int[getDeviceCount()];
      Arrays.fill(colors, OFF_COLOR);
      colors[id] = color;

      set(getMask(id), colors);
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

      set(mask, allOff);
      }

   public final int[] getColors()
      {
      return set(maskAllOff, allOff);
      }

   private boolean[] getMask(final int id)
      {
      return ledIdToMaskArrayMap.get(id);
      }

   public abstract int[] set(final boolean[] mask, final int[] colors);
   }