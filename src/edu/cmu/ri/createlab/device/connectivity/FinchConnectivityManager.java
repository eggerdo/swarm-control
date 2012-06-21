package edu.cmu.ri.createlab.device.connectivity;

import edu.cmu.ri.createlab.terk.robot.finch.DefaultFinchController;
import edu.cmu.ri.createlab.terk.robot.finch.FinchController;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class FinchConnectivityManager extends BaseCreateLabDeviceConnectivityManager<FinchController>
   {
   @Override
   protected FinchController scanForDeviceAndCreateProxy()
      {
      return DefaultFinchController.create();
      }
   }
