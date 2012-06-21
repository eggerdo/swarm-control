package edu.cmu.ri.createlab.terk.application;

/**
 * <p>
 * <code>ConnectionStrategyEventHandlerAdapter</code> is an abstract adapter class for handling
 * {@link ConnectionStrategy} events. The methods in this class are empty. This class exists as convenience for creating
 * event handlers.
 * </p>
 * <p>
 * Extend this class to create a {@link ConnectionStrategyEventHandler} and override the methods for the events of
 * interest. (If you implement the {@link ConnectionStrategyEventHandler} interface, you have to define all of the
 * methods in it. This abstract class defines no-op methods for them all, so you only have to define methods for events
 * you care about.)
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"NoopMethodInAbstractClass"})
public class ConnectionStrategyEventHandlerAdapter implements ConnectionStrategyEventHandler
   {
   public void handleAttemptingConnectionEvent()
      {
      }

   public void handleConnectionEvent()
      {
      }

   public void handleFailedConnectionEvent()
      {
      }

   public void handleAttemptingDisconnectionEvent()
      {
      }

   public void handleDisconnectionEvent()
      {
      }
   }
