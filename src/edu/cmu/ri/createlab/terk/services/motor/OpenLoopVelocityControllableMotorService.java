package edu.cmu.ri.createlab.terk.services.motor;

import edu.cmu.ri.createlab.terk.expression.ExpressionOperationExecutor;
import edu.cmu.ri.createlab.terk.services.DeviceController;
import edu.cmu.ri.createlab.terk.services.Service;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface OpenLoopVelocityControllableMotorService extends Service, DeviceController, ExpressionOperationExecutor<Boolean>
   {
   String TYPE_ID = "::TeRK::motor::OpenLoopVelocityControllableMotorService";

   String PROPERTY_NAME_MOTOR_DEVICE_ID = TYPE_ID + "::motor-device-id";

   String PROPERTY_NAME_MIN_VELOCITY = TYPE_ID + "::min-velocity";
   String PROPERTY_NAME_MAX_VELOCITY = TYPE_ID + "::max-velocity";

   String OPERATION_NAME_SET_VELOCITY = "setVelocity";
   String PARAMETER_NAME_VELOCITY = "velocity";

   /**
    * Sets the given motor to the the given <code>velocity</code>.  Returns <code>true</code> upon success;
    * <code>false</code> otherwise.
    * </p>
    * <p>
    * Note that, depending on the implementation, other motors might be set to 0.  For example, if the service controls
    * two motors (IDs 0 and 1), and a call to this method sets the velocity of motor 0, then the velocity of motor 1 may
    * be set to 0.
    * </p>
    */
   boolean setVelocity(final int motorId, final int velocity);

   /**
    * <p>
    * Sets the motor velocities to the given <code>velocities</code> (in native units).  Returns <code>true</code> upon
    * success; <code>false</code> otherwise.
    * </p>
    * <p>
    * Note that, depending on the implementation, motors not specified in the array may be set to 0.  For example, if
    * the service controls two motors, and a call to this method only provides an array of length 1, then the second
    * motor may have its velocity set to 0.
    * </p>
    */
   boolean setVelocities(final int[] velocities);

   /**
    * <p>
    * Stops the given motor(s) (specified by motor ID).  Halts all the motors if none are specified.  Returns
    * <code>true</code> upon success; <code>false</code> otherwise.
    * </p>
    * <p>
    * Note that, depending on the implementation, unspecified motors may still be set to 0.  For example, if the service
    * controls two motors, and a call to this method only specifies one of the motors, then the other motor may have
    * still have its velocity set to 0.
    * </p>
    */
   boolean stop(final int... motorIds);
   }