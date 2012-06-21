package edu.cmu.ri.createlab.terk.services.led;

//import java.awt.Color;
import edu.cmu.ri.createlab.terk.expression.ExpressionOperationExecutor;
import edu.cmu.ri.createlab.terk.impression.ImpressionOperationExecutor;
import edu.cmu.ri.createlab.terk.services.DeviceController;
import edu.cmu.ri.createlab.terk.services.Service;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface FullColorLEDService extends Service, DeviceController, ExpressionOperationExecutor<int[]>, ImpressionOperationExecutor<int[]>
   {
   String TYPE_ID = "::TeRK::led::FullColorLEDService";

   String PROPERTY_NAME_MIN_INTENSITY = TYPE_ID + "::min-intensity";
   String PROPERTY_NAME_MAX_INTENSITY = TYPE_ID + "::max-intensity";

   String OPERATION_NAME_SET_COLOR = "setColor";
   String OPERATION_NAME_GET_COLORS = "getColors";

   /** Sets the full-color LED specified by the given <code>id</code> to the given {@link Color}. */
   void set(final int id, final int color);

   /**
    * Sets the given full-color LEDs to off.  Sets all full-color LEDs off if no index is specified.
    *
    *@param ids the list of values in the range [0,getDeviceCount()) indicating which full-color LEDs should be set off
    */
   void setOff(int... ids);

   /** Returns the current full-color LED colors. */
   int[] getColors();
   }