package edu.cmu.ri.createlab.terk.services.photoresistor;

import edu.cmu.ri.createlab.terk.impression.ImpressionOperationExecutor;
import edu.cmu.ri.createlab.terk.services.DeviceController;
import edu.cmu.ri.createlab.terk.services.Service;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface PhotoresistorService extends Service, DeviceController, ImpressionOperationExecutor<Object>
   {
   String TYPE_ID = "::TeRK::photoresistor::PhotoresistorService";

   String PROPERTY_NAME_MIN_VALUE = TYPE_ID + "::min-value";
   String PROPERTY_NAME_MAX_VALUE = TYPE_ID + "::max-value";

   String OPERATION_NAME_GET_PHOTORESISTOR_VALUE = "getPhotoresistorValue";
   String OPERATION_NAME_GET_PHOTORESISTOR_VALUES = "getPhotoresistorValues";

   /**
    * Returns the value of the photoresistor specified by the given <code>id</code>.  Returns <code>null</code> if the
    * value could not be retrieved.
    */
   Integer getPhotoresistorValue(final int id);

   /**
    * Returns the value of each photoresistor.  Returns <code>null</code> if the values could not be retrieved.
    */
   int[] getPhotoresistorValues();
   }