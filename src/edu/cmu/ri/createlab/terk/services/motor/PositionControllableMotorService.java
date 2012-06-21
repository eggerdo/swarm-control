package edu.cmu.ri.createlab.terk.services.motor;

import edu.cmu.ri.createlab.terk.expression.ExpressionOperationExecutor;
import edu.cmu.ri.createlab.terk.services.DeviceController;
import edu.cmu.ri.createlab.terk.services.Service;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface PositionControllableMotorService extends Service, DeviceController, ExpressionOperationExecutor
   {
   String TYPE_ID = "::TeRK::motor::PositionControllableMotorService";

   String PROPERTY_NAME_MOTOR_DEVICE_ID = TYPE_ID + "::motor-device-id";

   String OPERATION_NAME_SET_POSITION = "setPosition";
   String PARAMETER_NAME_POSITION = "position";
   String PARAMETER_NAME_SPEED = "speed";

   String PROPERTY_NAME_MIN_POSITION_DELTA = TYPE_ID + "::min-position-delta";
   String PROPERTY_NAME_MAX_POSITION_DELTA = TYPE_ID + "::max-position-delta";
   String PROPERTY_NAME_MIN_SPEED = TYPE_ID + "::min-speed";
   String PROPERTY_NAME_MAX_SPEED = TYPE_ID + "::max-speed";

   /**
    * Directs the given motor to move to the given <code>positionDelta</code> at the given <code>speed</code>.  The
    * position value is relative to the current position, and its sign determines the motor's direction.
    */
   void setPosition(final int motorId, final int positionDelta, final int speed);

   /**
    * Directs the motors to move to the given <code>positionDeltas</code> at the given <code>speeds</code>. A position
    * value is relative to the current position, and its sign determines the motor's direction.
    */
   void setPositions(final int[] positionDeltas, final int[] speeds);

   /**
    * Directs the motors specified by the given <code>motorMask</code> to move to the given <code>positionDeltas</code>
    * at the given <code>speeds</code>. A position value is relative to the current position, and its sign determines
    * the motor's direction.
    */
   void setPositions(final boolean[] motorMask, final int[] positionDeltas, final int[] speeds);

   /**
    * Directs the given motor to move to the given <code>distanceDelta</code> at the given <code>speed</code>.  The
    * distance value is in centimeters, relative to the current position, and its sign determines the motor's direction.
    * The speed is in cm/s.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the unit conversions
    */
   void setPosition(final int motorId, final double distanceDelta, final double speed);

   /**
    * Directs the motors to move to the given <code>distanceDeltas</code> at the given <code>speeds</code>. A distance
    * value is in centimeters, relative to the current position, and its sign determines the motor's direction.  The
    * speeds are in cm/s.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the unit conversions
    */
   void setPositions(final double[] distanceDeltas, final double[] speeds);

   /**
    * Directs the motors specified by the given <code>motorMask</code> to move to the given <code>distanceDeltas</code>
    * at the given <code>speeds</code>. A distance value is in centimeters, relative to the current position, and its
    * sign determines the motor's direction.  The speeds are in cm/s.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the unit conversions
    */
   void setPositions(final boolean[] motorMask, final double[] distanceDeltas, final double[] speeds);

   /** Stops the given motor(s) (specified by motor ID).  Halts all the motors if none are specified. */
   void stop(final int... motorIds);

   /**
    * Returns the current position of the motor specified by the given <code>motorId</code>.  Returns <code>null</code>
    * if the value could not be retrieved.
    */
   Integer getCurrentPosition(final int motorId);

   /**
    * Returns the current position of the motor specified by the given <code>motorId</code> in centimeters.  Returns
    * <code>null</code> if the value could not be retrieved.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from ticks to cm
    */
   Double getCurrentPositionInCentimeters(final int motorId);

   /**
    * Returns the specified position of the motor specified by the given <code>motorId</code>.  Returns
    * <code>null</code> if the value could not be retrieved.
    */
   Integer getSpecifiedPosition(final int motorId);

   /**
    * Returns the specified position of the motor specified by the given <code>motorId</code> in centimeters.  Returns
    * <code>null</code> if the value could not be retrieved.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from ticks to cm
    */
   Double getSpecifiedPositionInCentimeters(final int motorId);

   /**
    * Returns the specified speed of the motor specified by the given <code>motorId</code>.  Returns
    * <code>null</code> if the value could not be retrieved.
    */
   Integer getSpecifiedSpeed(final int motorId);

   /**
    * Returns the specified speed of the motor (in cm/s) specified by the given <code>motorId</code>.  Returns
    * <code>null</code> if the value could not be retrieved.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from native speed to cm/s
    */
   Double getSpecifiedSpeedInCentimetersPerSecond(final int motorId);

   /**
    * Returns a the {@link PositionControllableMotorState} of the motor specified by the given <code>motorId</code>.
    * Returns <code>null</code> if the state could not be retrieved.
    */
   PositionControllableMotorState getState(final int motorId);

   /**
    * Returns an array of {@link PositionControllableMotorState}s.  Returns <code>null</code> if the states could not
    * be retrieved.
    */
   PositionControllableMotorState[] getStates();

   /**
    * Converts the given <code>rawMotorPosition</code> to centimeters.  Returns <code>null</code> if the given
    * <code>rawMotorPosition</code> is <code>null</code>.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from ticks to cm
    */
   Double convertToCentimeters(final Integer rawMotorPosition);

   /**
    * Converts the given array of position deltas to centimeters.  Returns <code>null</code> if the given array is
    * <code>null</code>.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from ticks to cm
    */
   double[] convertToCentimeters(final int[] positionDeltas);

   /**
    * Converts the given <code>distanceInCentimeters</code> to ticks, rounding to the nearest tick if necessary.
    * Returns <code>null</code> if the given <code>distanceInCentimeters</code> is <code>null</code>.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from cm to ticks
    */
   Integer convertToTicks(final Double distanceInCentimeters);

   /**
    * Converts the given array of distance deltas to ticks, rounding to the nearest tick if necessary.
    * Returns <code>null</code> if the given array is <code>null</code>.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from cm to ticks
    */
   int[] convertToTicks(final double[] distanceDeltas);

   /**
    * Converts the given native motor speed to cm/s. Returns <code>null</code> if the given <code>nativeSpeed</code> is
    * <code>null</code>.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from native speed to cm/s
    */
   Double convertToCentimetersPerSecond(final Integer nativeSpeed);

   /**
    * Converts the given native motor speeds to cm/s. Returns <code>null</code> if the given array is <code>null</code>.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from native speed to cm/s
    */
   double[] convertToCentimetersPerSecond(final int[] nativeSpeeds);

   /**
    * Converts the given speed from cm/s to native speed, rounding if necessary.  Returns <code>null</code> if the given
    * <code>speedInCentimeters</code> is <code>null</code>.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from cm/s to native speed
    */
   Integer convertToNativeSpeed(final Double speedInCentimeters);

   /**
    * Converts the given speeds from cm/s to native speeds, rounding if necessary. Returns <code>null</code> if the
    * given array is <code>null</code>.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion from cm/s to native speed
    */
   int[] convertToNativeSpeed(final double[] speedsInCentimeters);

   /**
    * Returns <code>true</code> if the implementation of this service can perform conversions between ticks and
    * centimeters.  Calling this method is a good way to avoid having to use a try-catch block to catch the
    * {@link UnsupportedOperationException} which would be thrown by some methods if unit conversion isn't supported.
    */
   boolean isUnitConversionSupported();
   }