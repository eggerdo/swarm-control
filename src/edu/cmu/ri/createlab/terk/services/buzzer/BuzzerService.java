package edu.cmu.ri.createlab.terk.services.buzzer;

import edu.cmu.ri.createlab.terk.expression.ExpressionOperationExecutor;
import edu.cmu.ri.createlab.terk.services.DeviceController;
import edu.cmu.ri.createlab.terk.services.Service;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface BuzzerService extends Service, DeviceController, ExpressionOperationExecutor<Boolean>
   {
   String TYPE_ID = "::TeRK::buzzer::BuzzerService";

   String OPERATION_NAME_PLAY_TONE = "playTone";
   String PARAMETER_NAME_FREQUENCY = "frequency";
   String PARAMETER_NAME_DURATION = "duration";

   String PROPERTY_NAME_MIN_DURATION = TYPE_ID + "::min-duration";
   String PROPERTY_NAME_MAX_DURATION = TYPE_ID + "::max-duration";
   String PROPERTY_NAME_MIN_FREQUENCY = TYPE_ID + "::min-frequency";
   String PROPERTY_NAME_MAX_FREQUENCY = TYPE_ID + "::max-frequency";

   /**
    * <p>
    * Commands the buzzer specified by the given <code>id</code> to a tone with the given <code>frequency</code> (hz)
    * and <code>duration</code> (ms).
    * </p>
    * @param id the buzzer
    * @param frequency the frequency of the tone, in hertz
    * @param durationInMilliseconds the duration of the tone, in milliseconds
    */
   void playTone(final int id, final int frequency, final int durationInMilliseconds);
   }