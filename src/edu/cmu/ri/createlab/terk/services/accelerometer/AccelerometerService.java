package edu.cmu.ri.createlab.terk.services.accelerometer;

import edu.cmu.ri.createlab.terk.impression.ImpressionOperationExecutor;
import edu.cmu.ri.createlab.terk.services.DeviceController;
import edu.cmu.ri.createlab.terk.services.Service;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface AccelerometerService extends Service, DeviceController, ImpressionOperationExecutor<Object>
   {
   String TYPE_ID = "::TeRK::accelerometer::AccelerometerService";

   String PROPERTY_NAME_ACCELEROMETER_DEVICE_ID = TYPE_ID + "::accelerometer-device-id";

   String OPERATION_NAME_GET_ACCELEROMETER_STATE = "getAccelerometerState";
   String OPERATION_NAME_GET_ACCELEROMETER_GS = "getAccelerometerGs";

   /**
    * Returns the state of the accelerometer specified by the given <code>id</code>.  Returns <code>null</code> if the
    * state could not be retrieved.
    */
   AccelerometerState getAccelerometerState(final int id);

   /**
    * Returns the state of the accelerometer specified by the given <code>id</code> in g's.  Returns <code>null</code> if
    * the state could not be retrieved.
    *
    * NOTE: This is merely a helper method, identical to calling {@link #convertToGs(AccelerometerState)}, giving it the
    * state returned by {@link #getAccelerometerState(int)}.  Since this method calls
    * {@link #getAccelerometerState(int)}, if you need to use both the raw state and the state in g's, it's more
    * efficient to call {@link #getAccelerometerState(int)} and {@link #convertToGs(AccelerometerState)} rather than
    * {@link #getAccelerometerState(int)} and this method since the accelerometer will only be read once.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion to g's
    *
    * @see #convertToGs(AccelerometerState)
    */
   AccelerometerGs getAccelerometerGs(final int id);

   /**
    * Converts the given {@link AccelerometerState} (as returned by {@link #getAccelerometerState(int)}) to g's.
    * Returns <code>null</code> if the conversion failed.
    *
    * @throws UnsupportedOperationException if the implementation cannot perform the conversion to g's
    *
    * @see #getAccelerometerGs(int)
    */
   AccelerometerGs convertToGs(final AccelerometerState state);

   /**
    * Returns <code>true</code> if the implementation of this service can perform conversions from raw values to g's.
    * Calling this method is a good way to avoid having to use a try-catch block to catch the
    * {@link UnsupportedOperationException} which would be thrown by some methods if unit conversion isn't supported.
    */
   boolean isUnitConversionSupported();

   /**
    * Returns the {@link AccelerometerUnitConversionStrategy} used by this implementation.  May return <code>null</code>
    * if unit conversion is not supported.
    */
   AccelerometerUnitConversionStrategy getAccelerometerUnitConversionStrategy();
   }