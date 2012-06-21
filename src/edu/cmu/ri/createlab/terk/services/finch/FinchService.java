package edu.cmu.ri.createlab.terk.services.finch;

import edu.cmu.ri.createlab.terk.services.Service;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface FinchService extends Service
   {
   String TYPE_ID = "::TeRK::finch::FinchService";

   /** Sets both motors and the LED to off. */
   void emergencyStop();
   }