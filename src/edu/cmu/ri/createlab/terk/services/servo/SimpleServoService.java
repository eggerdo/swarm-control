package edu.cmu.ri.createlab.terk.services.servo;

import edu.cmu.ri.createlab.terk.expression.ExpressionOperationExecutor;
import edu.cmu.ri.createlab.terk.impression.ImpressionOperationExecutor;
import edu.cmu.ri.createlab.terk.services.DeviceController;
import edu.cmu.ri.createlab.terk.services.Service;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface SimpleServoService extends Service, DeviceController, ExpressionOperationExecutor<int[]>, ImpressionOperationExecutor<int[]>
   {
   String TYPE_ID = "::TeRK::servo::SimpleServoService";

   String OPERATION_NAME_SET_POSITION = "setPosition";
   String PARAMETER_NAME_POSITION = "position";

   String OPERATION_NAME_GET_POSITIONS = "getPositions";

   String PROPERTY_NAME_MIN_POSITION = TYPE_ID + "::min-position";
   String PROPERTY_NAME_MAX_POSITION = TYPE_ID + "::max-position";

   String PROPERTY_NAME_MIN_SAFE_POSITION = TYPE_ID + "::min-safe-position";
   String PROPERTY_NAME_MAX_SAFE_POSITION = TYPE_ID + "::max-safe-position";

   /**
    *	Sets the servo specified by the given id to the given position.
    *
    *   @param servoId the servo number in [0, getDeviceCount() )
    * @param position a position in the range [0,255]
    */
   void setPosition(final int servoId, final int position);

   /**
    *	<p>Sets the given servos to the given positions.  The variable-length argument list is assumed to:</p>
    * <ol>
    *    <li>have an even number of arguments (and throws an {@link IllegalArgumentException} otherwise); and</li>
    *    <li>have values which alternatingly specify servo ids and servo positions.</li>
    * </ol>
    * <p>
    * The order of the pairs of values does not matter.  For example, to set servo 3 to 128, servo 9 to 16, and servo 1
    * to 255, you could write any of the following:
    * <blockquote>
    * <code>setPositions(3, 128, 9, 16, 1, 255);</code><br>
    * <code>setPositions(3, 128, 1, 255, 9, 16);</code><br>
    * <code>setPositions(9, 16, 3, 128, 1, 255);</code><br>
    * <code>setPositions(9, 16, 1, 255, 3, 128);</code><br>
    * <code>setPositions(1, 255, 3, 128, 9, 16);</code><br>
    * <code>setPositions(1, 255, 9, 16, 3, 128);</code>
    * </blockquote>
    * </p>
    *
    *   @param servoIdsAndPositions the ids and positions of the servos to set
    * @throws IllegalArgumentException if the number of arguments is not even
    */
   void setPositions(final int... servoIdsAndPositions);

   /** Returns the current servo positions or <code>null</code> if the positions could not be retrieved. */
   int[] getPositions();
   }