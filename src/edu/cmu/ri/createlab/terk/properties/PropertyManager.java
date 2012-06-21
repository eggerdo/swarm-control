package edu.cmu.ri.createlab.terk.properties;

import java.util.Map;
import java.util.Set;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface PropertyManager
   {
   String getProperty(final String key);

   /**
    * Returns the value for the given <code>key</code> as an <code>Integer</code>.  Returns <code>null</code> if the
    * value cannot be converted to an <code>int</code>.
    */
   Integer getPropertyAsInteger(final String key);

   /** Returns all property keys and values in an unmodifiable {@link Map}. */
   Map<String, String> getProperties();

   /** Returns all property keys in an unmodifiable {@link Set}. */
   Set<String> getPropertyKeys();

   void setProperty(final String key, final String value) throws ReadOnlyPropertyException;

   void setProperty(final String key, final int value) throws ReadOnlyPropertyException;
   }