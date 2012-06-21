package edu.cmu.ri.createlab.terk.services.motor;

import edu.cmu.ri.createlab.terk.expression.ExpressionOperationExecutor;
import edu.cmu.ri.createlab.terk.impression.ImpressionOperationExecutor;
import edu.cmu.ri.createlab.terk.services.DeviceController;
import edu.cmu.ri.createlab.terk.services.Service;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface VelocityControllableMotorService extends Service, DeviceController, ExpressionOperationExecutor<int[]>, ImpressionOperationExecutor<int[]>
   {
   String TYPE_ID = "::TeRK::motor::VelocityControllableMotorService";

   String PROPERTY_NAME_MOTOR_DEVICE_ID = TYPE_ID + "::motor-device-id";

   String OPERATION_NAME_SET_VELOCITY = "setVelocity";
   String PARAMETER_NAME_VELOCITY = "velocity";

   String OPERATION_NAME_GET_VELOCITIES = "getVelocities";

   String PROPERTY_NAME_MIN_VELOCITY = TYPE_ID + "::min-velocity";
   String PROPERTY_NAME_MAX_VELOCITY = TYPE_ID + "::max-velocity";

   /** Sets the given motor to the the given <code>velocity</code> (in native units). */
   void setVelocity(final int motorId, final int velocity);

   /** Sets the motor velocities to the given <code>velocities</code> (in native units). */
   void setVelocities(final int[] velocities);

   /**
    * Sets the motor(s) specified by the given <code>motorMask</code> to the velocities specified by the given
    * <code>velocities</code> (in native units).
    */
   void setVelocities(final boolean[] motorMask, final int[] velocities);

   /**
    * Sets the given motor to the the given <code>velocity</code> (in cm/s).
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from native units to cm/s
    */
   void setVelocity(final int motorId, final double velocity);

   /**
    * Sets the motor velocities to the given <code>velocities</code> (in cm/s).
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from native units to cm/s
    */
   void setVelocities(final double[] velocities);

   /**
    * Sets the motor(s) specified by the given <code>motorMask</code> to the velocities specified by the given
    * <code>velocities</code> (in cm/s).
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from native units to cm/s
    */
   void setVelocities(final boolean[] motorMask, final double[] velocities);

   /** Stops the given motor(s) (specified by motor ID).  Halts all the motors if none are specified. */
   void stop(final int... motorIds);

   /** Returns the current motor velocities (in native units). */
   int[] getVelocities();

   /**
    * Returns the current motor velocities (in cm/s).
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from native units to cm/s
    */
   double[] getVelocitiesInCentimetersPerSecond();

   /**
    * Converts the given <code>nativeVelocity</code> to cm/s.  Returns <code>null</code> if the given
    * <code>nativeVelocity</code> is <code>null</code>.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from native units to cm
    */
   Double convertToCentimetersPerSecond(final Integer nativeVelocity);

   /**
    * Converts the given array of native velocities to cm/s.  Returns <code>null</code> if the given array is
    * <code>null</code>.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from native units to cm
    */
   double[] convertToCentimetersPerSecond(final int[] nativeVelocities);

   /**
    * Converts the given velocity from cm/s to native velocity, rounding if necessary.  Returns
    * <code>null</code> if the given <code>velocityInCentimeters</code> is <code>null</code>.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from native units to cm
    */
   Integer convertToNativeVelocity(final Double velocityInCentimeters);

   /**
    * Converts the given velocities from cm/s to native velocities, rounding if necessary. Returns
    * <code>null</code> if the given array is <code>null</code>.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from native units to cm
    */
   int[] convertToNativeVelocity(final double[] velocitiesInCentimeters);
   }