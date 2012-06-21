package edu.cmu.ri.createlab.terk.services.analog;

import edu.cmu.ri.createlab.terk.impression.ImpressionOperationExecutor;
import edu.cmu.ri.createlab.terk.services.DeviceController;
import edu.cmu.ri.createlab.terk.services.Service;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface AnalogInputsService extends Service, DeviceController, ImpressionOperationExecutor<Object>
   {
   String TYPE_ID = "::TeRK::analog::AnalogInputsService";

   String PROPERTY_NAME_MIN_VALUE = TYPE_ID + "::min-value";
   String PROPERTY_NAME_MAX_VALUE = TYPE_ID + "::max-value";

   String OPERATION_NAME_GET_ANALOG_INPUT_VALUE = "getAnalogInputValue";
   String OPERATION_NAME_GET_ANALOG_INPUT_VALUES = "getAnalogInputValues";

   /**
    * Returns the value of the analog input specified by the given <code>id</code>.  Returns <code>null</code> if the
    * value could not be retrieved.
    *
    * @throws IllegalArgumentException if the <code>analogInputPortId</code> specifies an invalid port
    */
   Integer getAnalogInputValue(final int analogInputPortId);

   /**
    * Returns the value of all analog inputs or <code>null</code> if the values could not be retrieved.
    */
   int[] getAnalogInputValues();
   }