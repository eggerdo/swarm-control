package edu.cmu.ri.createlab.terk.properties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
//import org.apache.log4j.Logger;

/**
 * <p>
 * <code>BasicPropertyManager</code> helps manage writable and read-only properties.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class BasicPropertyManager implements PropertyManager
   {
//   private static final Logger LOG = Logger.getLogger(BasicPropertyManager.class);

   private final Map<String, String> properties = new HashMap<String, String>();
   private final Map<String, String> readOnlyProperties = new HashMap<String, String>();

   /** Returns the property named by the given key. */
   public final String getProperty(final String key)
      {
      return properties.get(key);
      }

   /**
    * Returns the value for the given <code>key</code> as an <code>Integer</code>.  Returns <code>null</code> if the
    * value cannot be converted to an <code>int</code>.
    */
   public final Integer getPropertyAsInteger(final String key)
      {
      final int i;
      try
         {
         i = Integer.parseInt(getProperty(key));
         }
      catch (NumberFormatException e)
         {
         return null;
         }
      return i;
      }

   /** Returns an unmodifiable {@link Map} of property keys and values. */
   public final Map<String, String> getProperties()
      {
      return Collections.unmodifiableMap(properties);
      }

   /** Returns an unmodifiable {@link Set} of all property keys. */
   public final Set<String> getPropertyKeys()
      {
      return Collections.unmodifiableSet(properties.keySet());
      }

   /**
    * Sets a property using the given <code>key</code> to the given <code>value</code>.  Throws a
    * {@link ReadOnlyPropertyException} if the property is read-only.  If the property already exists, it is overwritten
    * with the new <code>value</code>.
    */
   public final void setProperty(final String key, final String value) throws ReadOnlyPropertyException
      {
      if (isReadOnly(key))
         {
         throw new ReadOnlyPropertyException("Could not set property [" + key + "] because it is read-only.");
         }

      // not read-only, so set the property
      properties.put(key, value);
      }

   /**
    * Sets a property using the given <code>key</code> to the given integer <code>value</code>.  Throws a
    * {@link ReadOnlyPropertyException} if the property is read-only.  If the property already exists, it is overwritten
    * with the new <code>value</code>.
    */
   public final void setProperty(final String key, final int value) throws ReadOnlyPropertyException
      {
      setProperty(key, String.valueOf(value));
      }

   /**
    * Sets a read-only property using the given <code>key</code> to the given <code>value</code>. If the property
    * already exists, it is overwritten with the new <code>value</code>.
    */
   public final void setReadOnlyProperty(final String key, final String value)
      {
//      if (LOG.isDebugEnabled())
//         {
//         LOG.debug("ServantPropertyManager.setReadOnlyProperty(" + key + "," + value + ")");
//         }

      properties.put(key, value);
      readOnlyProperties.put(key, value);
      }

   /**
    * Sets a read-only property using the given <code>key</code> to the given <code>value</code> after first converting
    * it to a String. If the property already exists, it is overwritten with the new <code>value</code>.
    */
   public final void setReadOnlyProperty(final String key, final int value)
      {
//      if (LOG.isDebugEnabled())
//         {
//         LOG.debug("ServantPropertyManager.setReadOnlyProperty(" + key + "," + value + ")");
//         }

      final String valueStr = String.valueOf(value);
      properties.put(key, valueStr);
      readOnlyProperties.put(key, valueStr);
      }

   public final boolean isReadOnly(final String key)
      {
      return readOnlyProperties.containsKey(key);
      }
   }