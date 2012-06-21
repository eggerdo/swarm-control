package edu.cmu.ri.createlab.terk.properties;

import java.util.Map;
import java.util.Set;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class PropertyManagerWrapper implements PropertyManager
   {
   private final PropertyManager propertyManager;

   public PropertyManagerWrapper(final PropertyManager propertyManager)
      {
      this.propertyManager = propertyManager;
      }

   public final String getProperty(final String key)
      {
      return propertyManager.getProperty(key);
      }

   public final Integer getPropertyAsInteger(final String key)
      {
      return propertyManager.getPropertyAsInteger(key);
      }

   public final Map<String, String> getProperties()
      {
      return propertyManager.getProperties();
      }

   public final Set<String> getPropertyKeys()
      {
      return propertyManager.getPropertyKeys();
      }

   public final void setProperty(final String key, final String value) throws ReadOnlyPropertyException
      {
      propertyManager.setProperty(key, value);
      }

   public final void setProperty(final String key, final int value) throws ReadOnlyPropertyException
      {
      propertyManager.setProperty(key, value);
      }
   }
