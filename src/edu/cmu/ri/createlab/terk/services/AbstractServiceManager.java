package edu.cmu.ri.createlab.terk.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class AbstractServiceManager implements ServiceManager
   {
   private final Set<String> supportedServiceTypeIds = Collections.synchronizedSet(new HashSet<String>());

   public final Service getServiceByTypeId(final String typeId)
      {
      return loadService(typeId);
      }

   public final Set<String> getTypeIdsOfSupportedServices()
      {
      return Collections.unmodifiableSet(supportedServiceTypeIds);
      }

   public final boolean isServiceSupported(final String typeId)
      {
      return supportedServiceTypeIds.contains(typeId);
      }

   protected final void registerSupportedService(final String typeId)
      {
      if (typeId != null)
         {
         supportedServiceTypeIds.add(typeId);
         }
      }

   protected final void registerSupportedServices(final Set<String> typeIds)
      {
      if ((typeIds != null) && (!typeIds.isEmpty()))
         {
         for (final String typeId : typeIds)
            {
            registerSupportedService(typeId);
            }
         }
      }

   protected abstract Service loadService(final String typeId);
   }
