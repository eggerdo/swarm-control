package edu.cmu.ri.createlab.terk.services.led;

import edu.cmu.ri.createlab.terk.services.DeviceController;
import edu.cmu.ri.createlab.terk.services.Service;

/**
 * @author Michael Safyan (michaelsafyan@wustl.edu)
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface LEDService extends Service, DeviceController
   {
   String TYPE_ID = "::TeRK::LEDController";

   /**
    *<p>Sets the state of all LEDs.</p>
    *@param mask the list of LEDs to change
    *@param modes the list of states for the LEDs
    */
   void execute(boolean[] mask, LEDMode[] modes);

   /**
    *<p>Sets the given LEDs to the given mode.</p>
    *
    *@param mode one of the modes specified by {@link LEDMode}
    *@param ledIds the list of values in [0,getDeviceCount() ) indicating which LEDs should be affected by the command
    */
   void set(LEDMode mode, int... ledIds);

   /**
    *<p>Sets the given LEDs to on.</p>
    *
    *@param ledIds the list of values in [0,getDeviceCount() ) indicating which LEDs should be set on
    */
   void setOn(int... ledIds);

   /**
    *<p>Sets the given LEDs to off.</p>
    *
    *@param ledIds the list of values in [0,getDeviceCount() ) indicating which LEDs should be set off
    */
   void setOff(int... ledIds);

   /**
    *<p>Sets the given LEDs to blinking.</p>
    *
    *@param ledIds the list of values in [0,getDeviceCount() ) indicating which LEDs should be set blinking
    */
   void setBlinking(int... ledIds);
   }