package edu.cmu.ri.createlab.terk.robot.finch;

//import java.awt.Color;
import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerGs;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerState;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface FinchController extends CreateLabDeviceProxy
   {
   /**
    * Returns the state of the accelerometer; returns <code>null</code> if an error occurred while trying to read the
    * state.
    */
   AccelerometerState getAccelerometerState();

   /**
    * Returns the state of the accelerometer in g's; returns <code>null</code> if an error occurred while trying to read
    * the state.
    */
   AccelerometerGs getAccelerometerGs();

   /**
    * Returns the state of the obstacle detector specified by the given <code>id</code> where 0 denotes the left
    * obstacle detector and 1 denotes the right obstacle detector.  Returns <code>null</code> if an error occurred while
    * trying to read the value.
    */
   Boolean isObstacleDetected(final int id);

   /**
    * Returns the state of the obstacle detectors as an array of <code>boolean</code>s where element 0 denotes the left
    * obstacle detector and element 1 denotes the right obstacle detector.  Returns <code>null</code> if an error
    * occurred while trying to read the values.
    */
   boolean[] areObstaclesDetected();

   /**
    * Returns the current values of the photoresistors as an array of <code>int</code>s where element 0 denotes the left
    * photoresistor and element 1 denotes the right photoresistor.  Returns <code>null</code> if an error occurred while
    * trying to read the values.
    */
   int[] getPhotoresistors();

   /**
    * Returns the current value of the thermistor specified by the given <code>id</code>.  Invalid thermistor ids cause
    * this method to return <code>null</code>.  This method also returns <code>null</code> if an error occurred while
    * trying to read the value.
    */
   Integer getThermistor(final int id);

   /**
    * Returns the current value of the thermistor.  Returns <code>null</code> if an error occurred while
    * trying to read the value.
    */
   Integer getThermistor();

   /**
    * Returns the current value of the thermistor.  Returns <code>null</code> if an error occurred while
    * trying to read the values.
    */
   Double getThermistorCelsiusTemperature();

   /**
    * Sets the full-color LED to the given red, green, and blue intensities.  Returns <code>true</code> if the command
    * succeeded, <code>false</code> otherwise.
    *
    * @param red the intensity of the LED's red component [0 to 255]
    * @param green the intensity of the LED's green component [0 to 255]
    * @param blue the intensity of the LED's blue component [0 to 255]
    */
   boolean setFullColorLED(final int red, final int green, final int blue);

   /**
    * Sets the full-color LED to the given {@link Color color}.  Returns <code>true</code> if the command succeeded,
    * <code>false</code> otherwise.
    */
   boolean setFullColorLED(final int color);

   /**
    * Sets the motors to the given velocities.  Returns <code>true</code> if the command succeeded, <code>false</code>
    * otherwise.
    *
    * @param leftVelocity velocity of the left motor [-255 to 255]
    * @param rightVelocity velocity of the left motor [-255 to 255]
    */
   boolean setMotorVelocities(final int leftVelocity, final int rightVelocity);

   /**
    * Sets the buzzer to the given <code>frequency</code> for the given <code>durationInMilliseconds</code>. Returns
    * <code>true</code> if the command succeeded, <code>false</code> otherwise.
    *
    * @param frequency the frequency of the tone [0 to 32767]
    * @param durationInMilliseconds the duration of the tone in milliseconds [0 to 32767]
    */
   boolean playBuzzerTone(final int frequency, final int durationInMilliseconds);

   /** Plays a tone having the given <code>frequency</code>, <code>amplitude</code>, and <code>duration</code>. */
   void playTone(final int frequency, final int amplitude, final int duration);

   /** Plays the sound clip contained in the given <code>byte</code> array. */
   void playClip(final byte[] data);

   /** Converts the given text into speech, and returns the resulting WAV sound clip as a byte array. */
   byte[] getSpeech(final String whatToSay);

   /** Converts the given text into audio and plays it. */
   void speak(final String whatToSay);

   /**
    * Turns off both motors and the full-color LED. Returns <code>true</code> if the command succeeded,
    * <code>false</code> otherwise.
    */
   boolean emergencyStop();

   void disconnect();

   /**
    * Returns <code>true</code> if {@link #disconnect()} has been called; <code>false</code> otherwise.
    */
   boolean isDisconnected();
   }