package edu.cmu.ri.createlab.terk.application;

import java.util.HashSet;
import java.util.Set;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
//import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class ConnectionStrategy
   {
//   private static final Logger LOG = Logger.getLogger(ConnectionStrategy.class);

   private final Set<ConnectionStrategyEventHandler> eventHandlers = new HashSet<ConnectionStrategyEventHandler>();

   public final void addConnectionStrategyEventHandler(final ConnectionStrategyEventHandler handler)
      {
      if (handler != null)
         {
         eventHandlers.add(handler);
         }
      }

   public final void removeConnectionStrategyEventHandler(final ConnectionStrategyEventHandler handler)
      {
      if (handler != null)
         {
         eventHandlers.remove(handler);
         }
      }

   @SuppressWarnings({"ConstantConditions"})
   protected final void notifyListenersOfAttemptingConnectionEvent()
      {
      for (final ConnectionStrategyEventHandler handler : eventHandlers)
         {
         try
            {
            handler.handleAttemptingConnectionEvent();
            }
         catch (Exception e)
            {
//            LOG.error("Exception while notifying listener of attempting connection event", e);
            }
         }
      }

   @SuppressWarnings({"ConstantConditions"})
   protected final void notifyListenersOfConnectionEvent()
      {
      for (final ConnectionStrategyEventHandler handler : eventHandlers)
         {
         try
            {
            handler.handleConnectionEvent();
            }
         catch (Exception e)
            {
//            LOG.error("Exception while notifying listener of connection event", e);
            }
         }
      }

   @SuppressWarnings({"ConstantConditions"})
   protected final void notifyListenersOfFailedConnectionEvent()
      {
      for (final ConnectionStrategyEventHandler handler : eventHandlers)
         {
         try
            {
            handler.handleFailedConnectionEvent();
            }
         catch (Exception e)
            {
//            LOG.error("Exception while notifying listener of failed connection event", e);
            }
         }
      }

   @SuppressWarnings({"ConstantConditions"})
   protected final void notifyListenersOfAttemptingDisconnectionEvent()
      {
      for (final ConnectionStrategyEventHandler handler : eventHandlers)
         {
         try
            {
            handler.handleAttemptingDisconnectionEvent();
            }
         catch (Exception e)
            {
//            LOG.error("Exception while notifying listener of attempting disconnection event", e);
            }
         }
      }

   @SuppressWarnings({"ConstantConditions"})
   protected final void notifyListenersOfDisconnectionEvent()
      {
      for (final ConnectionStrategyEventHandler handler : eventHandlers)
         {
         try
            {
            handler.handleDisconnectionEvent();
            }
         catch (Exception e)
            {
//            LOG.error("Exception while notifying listener of disconnection event", e);
            }
         }
      }

   /** Returns the {@link ServiceManager} if a connection has been established; returns <code>null</code> otherwise. */
   public abstract ServiceManager getServiceManager();

   /** Returns <code>true</code> if a connection has been established; returns <code>false</code> otherwise. */
   public abstract boolean isConnected();

   /** Returns <code>true</code> if a connection is being established; returns <code>false</code> otherwise. */
   public abstract boolean isConnecting();

   /** Initiates a connection to the target entity. */
   public abstract void connect();

   /** Attempts to cancel an in-progress connection. */
   public abstract void cancelConnect();

   /** Initiates a disconnect from the target entity. */
   public abstract void disconnect();

   /** Executes any necessary cleanup before system shutdown. */
   public abstract void prepareForShutdown();
   }