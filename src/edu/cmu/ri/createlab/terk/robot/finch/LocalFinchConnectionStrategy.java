package edu.cmu.ri.createlab.terk.robot.finch;

import edu.cmu.ri.createlab.device.connectivity.CreateLabDeviceConnectionEventListener;
import edu.cmu.ri.createlab.device.connectivity.CreateLabDeviceConnectionState;
import edu.cmu.ri.createlab.device.connectivity.FinchConnectivityManager;
import edu.cmu.ri.createlab.terk.application.ConnectionStrategy;
import edu.cmu.ri.createlab.terk.robot.finch.services.DefaultFinchServiceFactoryHelper;
import edu.cmu.ri.createlab.terk.robot.finch.services.FinchServiceManager;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class LocalFinchConnectionStrategy extends ConnectionStrategy
   {
//   private static final Logger LOG = Logger.getLogger(LocalFinchConnectionStrategy.class);

   private ServiceManager serviceManager = null;
   private final FinchConnectivityManager finchConnectivityManager = new FinchConnectivityManager();

   public LocalFinchConnectionStrategy()
      {
      finchConnectivityManager.addConnectionEventListener(
            new CreateLabDeviceConnectionEventListener()
            {
            public void handleConnectionStateChange(final CreateLabDeviceConnectionState oldState, final CreateLabDeviceConnectionState newState, final String portName)
               {
//               if (LOG.isDebugEnabled())
//                  {
//                  LOG.debug("LocalFinchConnectionStrategy.handleConnectionStateChange(): OLD [" + oldState.getStateName() + "]  NEW [" + newState.getStateName() + "]  port [" + portName + "]");
//                  }
               switch (newState)
                  {
                  case CONNECTED:
                     serviceManager = new FinchServiceManager(finchConnectivityManager.getCreateLabDeviceProxy(), DefaultFinchServiceFactoryHelper.getInstance());
                     notifyListenersOfConnectionEvent();
                     break;
                  case DISCONNECTED:
                     serviceManager = null;
                     notifyListenersOfDisconnectionEvent();
                     break;
                  case SCANNING:
                     notifyListenersOfAttemptingConnectionEvent();
                     break;
                  default:
//                     if (LOG.isEnabledFor(Level.ERROR))
//                        {
//                        LOG.error("Unexpected CreateLabDeviceConnectionState [" + newState + "]");
//                        }
                  }
               }
            });
      }

   public boolean isConnected()
      {
      return CreateLabDeviceConnectionState.CONNECTED.equals(finchConnectivityManager.getConnectionState());
      }

   public boolean isConnecting()
      {
      return CreateLabDeviceConnectionState.SCANNING.equals(finchConnectivityManager.getConnectionState());
      }

   public ServiceManager getServiceManager()
      {
      return serviceManager;
      }

   public void connect()
      {
      finchConnectivityManager.scanAndConnect();
      }

   public void cancelConnect()
      {
      finchConnectivityManager.cancelConnecting();
      }

   public void disconnect()
      {
//      LOG.debug("LocalFinchConnectionStrategy.disconnect()");
      notifyListenersOfAttemptingDisconnectionEvent();
      finchConnectivityManager.disconnect();
      }

   public void prepareForShutdown()
      {
//      LOG.debug("LocalFinchConnectionStrategy.prepareForShutdown()");
      disconnect();
      }
   }
