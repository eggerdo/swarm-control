package edu.cmu.ri.createlab.terk.services;

import java.util.Set;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ServiceManager
   {
   /**
    * Returns <code>true</code> if the given type ID is supported by this service manager; <code>false</code>
    * otherwise.  The type ID is the unique identifier for the service.
    */
   boolean isServiceSupported(final String typeId);

   /**
    * Returns the {@link Service} associated with the given <code>typeId</code>; returns <code>null</code> if no such
    * {@link Service} is registered with this service manager.
    */
   Service getServiceByTypeId(final String typeId);

   /** Returns an unmodifiable {@link Set} of the type IDs registered with this service manager. */
   Set<String> getTypeIdsOfSupportedServices();
   }