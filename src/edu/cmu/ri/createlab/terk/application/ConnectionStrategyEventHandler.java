package edu.cmu.ri.createlab.terk.application;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ConnectionStrategyEventHandler
   {
   void handleAttemptingConnectionEvent();

   void handleConnectionEvent();

   void handleFailedConnectionEvent();

   void handleAttemptingDisconnectionEvent();

   void handleDisconnectionEvent();
   }