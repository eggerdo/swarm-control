package edu.cmu.ri.createlab.terk.application;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
//import javax.swing.SwingUtilities;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;

/**
 * An abstract class for creating applications which want to connect to and control some TeRK entity (that is, something
 * that provides services via a {@link ServiceManager}), but also need the ability to vary the way in which that entity
 * is connected to.  Varying the way connections are managed is done with implementations of the
 * {@link ConnectionStrategy} class.  Users of this class must specify the {@link ConnectionStrategy} to be used by
 * defining a Java system property called <code>terk-application.connection-strategy.class.name</code> whose value
 * should be the full classname of the implementation class.
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class TerkApplication
   {
//   private static final Logger LOG = Logger.getLogger(TerkApplication.class);

   public static final String CONNECTION_STRATEGY_CLASS_NAME_PROPERTY = "terk-application.connection-strategy.class.name";

   private static boolean isConnectionStrategyImplementationClassDefined()
      {
      return System.getProperty(CONNECTION_STRATEGY_CLASS_NAME_PROPERTY) != null;
      }

   private static ConnectionStrategy instantiateConnectionStrategy(final String connectionStrategyClassName)
      {
      try
         {
         final Class clazz = Class.forName(connectionStrategyClassName);
         final Constructor constructor = clazz.getConstructor();
         if (constructor != null)
            {
            final ConnectionStrategy tempConnectionStrategy = (ConnectionStrategy)constructor.newInstance();
            if (tempConnectionStrategy == null)
               {
//               LOG.error("Instantiation of ConnectionStrategy implementation [" + connectionStrategyClassName + "] returned null.  Weird.");
               }
            else
               {
               return tempConnectionStrategy;
               }
            }
         }
      catch (ClassNotFoundException e)
         {
//         LOG.error("ClassNotFoundException while trying to find ConnectionStrategy implementation [" + connectionStrategyClassName + "]", e);
         }
      catch (NoSuchMethodException e)
         {
//         LOG.error("NoSuchMethodException while trying to find no-arg constructor for ConnectionStrategy implementation [" + connectionStrategyClassName + "]", e);
         }
      catch (IllegalAccessException e)
         {
//         LOG.error("IllegalAccessException while trying to instantiate ConnectionStrategy implementation [" + connectionStrategyClassName + "]", e);
         }
      catch (InvocationTargetException e)
         {
//         LOG.error("InvocationTargetException while trying to instantiate ConnectionStrategy implementation [" + connectionStrategyClassName + "]", e);
         }
      catch (InstantiationException e)
         {
//         LOG.error("InstantiationException while trying to instantiate ConnectionStrategy implementation [" + connectionStrategyClassName + "]", e);
         }

      return null;
      }

   private final ConnectionStrategy connectionStrategy;

   private ExecutorService executor = Executors.newCachedThreadPool();

   private final Runnable connectRunnable =
         new Runnable()
         {
         public void run()
            {
            connectionStrategy.connect();
            }
         };

   private final Runnable cancelConnectRunnable =
         new Runnable()
         {
         public void run()
            {
            connectionStrategy.cancelConnect();
            }
         };

   private final Runnable disconnectRunnable =
         new Runnable()
         {
         public void run()
            {
            connectionStrategy.disconnect();
            }
         };

   private final Runnable shutdownRunnable =
         new Runnable()
         {
         public void run()
            {
//            LOG.debug("TerkApplication$shutdownRunnable.run()");
            connectionStrategy.prepareForShutdown();
            }
         };

   /**
    * Creates a <code>TerkApplication</code> using the {@link ConnectionStrategy} implementation class defined in the
    * <code>terk-application.connection-strategy.class.name</code> system property.
    */
   protected TerkApplication()
      {
      this(System.getProperty(CONNECTION_STRATEGY_CLASS_NAME_PROPERTY));
      }

   /**
    * Creates a <code>TerkApplication</code> using the {@link ConnectionStrategy} implementation class defined in the
    * <code>terk-application.connection-strategy.class.name</code> system property, if defined and valid, otherwise
    * tries to use the implementation class specified by the given <code>defaultConnectionStrategyClassName</code>.
    *
    * @throws IllegalStateException if the {@link ConnectionStrategy} implementation class could not be instantiated
    */
   protected TerkApplication(final String defaultConnectionStrategyClassName)
      {
//      LOG.debug("TerkApplication.TerkApplication()");

      if (isConnectionStrategyImplementationClassDefined())
         {
         final String systemPropertyConnectionStrategyClassName = System.getProperty(CONNECTION_STRATEGY_CLASS_NAME_PROPERTY);
         ConnectionStrategy tempConnectionStrategy = instantiateConnectionStrategy(systemPropertyConnectionStrategyClassName);
         if (tempConnectionStrategy == null)
            {
//            if (LOG.isEnabledFor(Level.ERROR))
//               {
//               LOG.error("TerkApplication.TerkApplication(): System property [" + CONNECTION_STRATEGY_CLASS_NAME_PROPERTY + "] specifies an invalid ConnectionStrategy implementation class [" + systemPropertyConnectionStrategyClassName + "].  Attempting to use default [" + defaultConnectionStrategyClassName + "] implementation class instead.");
//               }

            tempConnectionStrategy = instantiateConnectionStrategy(defaultConnectionStrategyClassName);
            if (tempConnectionStrategy == null)
               {
//               LOG.error("TerkApplication.TerkApplication(): default ConnectionStrategy implementation class is invalid.");
               throw new IllegalStateException("System property [" + CONNECTION_STRATEGY_CLASS_NAME_PROPERTY + "] specifies an invalid ConnectionStrategy implementation class [" + systemPropertyConnectionStrategyClassName + "], and default implementation class [" + defaultConnectionStrategyClassName + "] is invalid!");
               }
            else
               {
//               if (LOG.isDebugEnabled())
//                  {
//                  LOG.debug("TerkApplication.TerkApplication(): successfully instantiated ConnectionStrategy implementation class [" + defaultConnectionStrategyClassName + "]");
//                  }
               connectionStrategy = tempConnectionStrategy;
               }
            }
         else
            {
//            if (LOG.isDebugEnabled())
//               {
//               LOG.debug("TerkApplication.TerkApplication(): successfully instantiated ConnectionStrategy implementation class [" + systemPropertyConnectionStrategyClassName + "]");
//               }
            connectionStrategy = tempConnectionStrategy;
            }
         }
      else
         {
//         if (LOG.isInfoEnabled())
//            {
//            LOG.info("TerkApplication.TerkApplication(): System property [" + CONNECTION_STRATEGY_CLASS_NAME_PROPERTY + "] is not defined.  Attempting to use default [" + defaultConnectionStrategyClassName + "] implementation class instead.");
//            }
         final ConnectionStrategy tempConnectionStrategy = instantiateConnectionStrategy(defaultConnectionStrategyClassName);
         if (tempConnectionStrategy == null)
            {
//            LOG.error("TerkApplication.TerkApplication(): default ConnectionStrategy implementation class is invalid.");
            throw new IllegalStateException("System property [" + CONNECTION_STRATEGY_CLASS_NAME_PROPERTY + "] is not defined, and default implementation class [" + defaultConnectionStrategyClassName + "] is invalid!");
            }
         else
            {
//            if (LOG.isDebugEnabled())
//               {
//               LOG.debug("TerkApplication.TerkApplication(): successfully instantiated ConnectionStrategy implementation class [" + defaultConnectionStrategyClassName + "]");
//               }
            connectionStrategy = tempConnectionStrategy;
            }
         }
      }

   /**
    * Adds a {@link ConnectionStrategyEventHandler} by delegating to the {@link ConnectionStrategy}'s
    * {@link ConnectionStrategy#addConnectionStrategyEventHandler(ConnectionStrategyEventHandler) addConnectionStrategyEventHandler()}
    * method.
    */
   protected final void addConnectionStrategyEventHandler(final ConnectionStrategyEventHandler handler)
      {
      connectionStrategy.addConnectionStrategyEventHandler(handler);
      }

   /**
    * Returns <code>true</code> if connected, <code>false</code> otherwise.  Connection status is determined by
    * delegating to the {@link ConnectionStrategy}'s {@link ConnectionStrategy#isConnected() isConnected()} method.
    */
   protected final boolean isConnected()
      {
      return connectionStrategy.isConnected();
      }

   /**
    * Returns <code>true</code> if a connection is in the process of being established, <code>false</code> otherwise.
    * Connection status is determined by delegating to the {@link ConnectionStrategy}'s
    * {@link ConnectionStrategy#isConnecting() isConnecting()} method.
    */
   protected final boolean isConnecting()
      {
      return connectionStrategy.isConnecting();
      }

   /**
    * Initiates a connection by delegating to the {@link ConnectionStrategy}'s
    * {@link ConnectionStrategy#connect() connect()} method.  This method ensures that the delegated call is not
    * executed in the GUI thread.
    */
   protected final void connect()
      {
//      if (SwingUtilities.isEventDispatchThread())
//         {
         executor.execute(connectRunnable);
//         }
//      else
//         {
//         connectRunnable.run();
//         }
      }

   /**
    * Cancels an in-progress connection by delegating to the {@link ConnectionStrategy}'s
    * {@link ConnectionStrategy#cancelConnect() cancelConnect()} method.  This method ensures that the delegated call is
    * not executed in the GUI thread.
    */
   protected final void cancelConnect()
      {
//      if (SwingUtilities.isEventDispatchThread())
//         {
         executor.execute(cancelConnectRunnable);
//         }
//      else
//         {
//         cancelConnectRunnable.run();
//         }
      }

   /**
    * Terminates a connection by delegating to the {@link ConnectionStrategy}'s
    * {@link ConnectionStrategy#disconnect() disconnect()} method.  This method ensures that the delegated call is not
    * executed in the GUI thread.
    */
   protected final void disconnect()
      {
//      if (SwingUtilities.isEventDispatchThread())
//         {
         executor.execute(disconnectRunnable);
//         }
//      else
//         {
//         disconnectRunnable.run();
//         }
      }

   /**
    * Shuts down by delegating to the {@link ConnectionStrategy}'s
    * {@link ConnectionStrategy#prepareForShutdown() prepareForShutdown()} method.  This method ensures that the
    * delegated call is not executed in the GUI thread.
    */
   protected final void shutdown()
      {
//      LOG.debug("TerkApplication.shutdown()");
//      if (SwingUtilities.isEventDispatchThread())
//         {
         executor.execute(shutdownRunnable);
//         }
//      else
//         {
//         shutdownRunnable.run();
//         }

      try
         {
         executor.shutdown();
         executor.awaitTermination(5, TimeUnit.SECONDS);
         }
      catch (InterruptedException e)
         {
//         LOG.error("InterruptedException while awaiting termination of TerkApplication's executor.", e);
         }
      }

   /**
    * Returns the {@link ServiceManager} created by the {@link ConnectionStrategy} by delegating to the
    * {@link ConnectionStrategy}'s {@link ConnectionStrategy#getServiceManager() getServiceManager()} method.  This
    * method will return <code>null</code> if the the {@link ServiceManager} has not been created (e.g. when no
    * connection has been established).
    */
   protected final ServiceManager getServiceManager()
      {
      if (connectionStrategy != null)
         {
         return connectionStrategy.getServiceManager();
         }
      return null;
      }
   }
