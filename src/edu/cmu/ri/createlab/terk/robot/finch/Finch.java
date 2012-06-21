package edu.cmu.ri.createlab.terk.robot.finch;

//import java.awt.Color;
//import java.awt.Component;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import android.graphics.Color;
//import javax.swing.JFrame;
//import javax.swing.SwingUtilities;
//import javax.swing.WindowConstants;
import edu.cmu.ri.createlab.terk.application.ConnectionStrategyEventHandlerAdapter;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerGs;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerService;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerState;
import edu.cmu.ri.createlab.terk.services.audio.AudioService;
import edu.cmu.ri.createlab.terk.services.buzzer.BuzzerService;
import edu.cmu.ri.createlab.terk.services.led.FullColorLEDService;
import edu.cmu.ri.createlab.terk.services.motor.OpenLoopVelocityControllableMotorService;
import edu.cmu.ri.createlab.terk.services.obstacle.SimpleObstacleDetectorService;
import edu.cmu.ri.createlab.terk.services.photoresistor.PhotoresistorService;
import edu.cmu.ri.createlab.terk.services.thermistor.ThermistorService;
//import edu.cmu.ri.createlab.userinterface.component.DatasetPlotter;
import edu.cmu.ri.createlab.util.FileUtils;
//import org.apache.log4j.Logger;

/**
 * Contains all methods necessary to program for the Finch robot
 *
 * @author Tom Lauwers (tlauwers@birdbraintechnologies.com)
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class Finch extends BaseFinchApplication implements FinchInterface
   {
//   private static final Logger LOG = Logger.getLogger(Finch.class);

   private static final String DEFAULT_CONNECTION_STRATEGY_IMPLEMENTATION_CLASS = "edu.cmu.ri.createlab.terk.robot.finch.LocalFinchConnectionStrategy";

   private final Semaphore connectionCompleteSemaphore = new Semaphore(1);

   // set new plotters to graph sensor values
//   private final DatasetPlotter<Double> accelerometerPlotter = new DatasetPlotter<Double>(-1.7, 1.7, 340, 340, 10, TimeUnit.MILLISECONDS);
//   private final DatasetPlotter<Integer> lightPlotter = new DatasetPlotter<Integer>(-10, 270, 340, 340, 10, TimeUnit.MILLISECONDS);
//   private final DatasetPlotter<Double> temperaturePlotter = new DatasetPlotter<Double>(0.0, 40.0, 340, 340, 10, TimeUnit.MILLISECONDS);

   // create accelerometer, temperature, and light sensor jFrames
//   private JFrame jFrameAccel;
//   private JFrame jFrameTemp;
//   private JFrame jFrameLight;

   public Finch()
      {
      super(DEFAULT_CONNECTION_STRATEGY_IMPLEMENTATION_CLASS);

      System.out.println("Connecting to Finch...this may take a few seconds...");

      this.addConnectionStrategyEventHandler(
            new ConnectionStrategyEventHandlerAdapter()
            {
            public void handleConnectionEvent()
               {
//               LOG.trace("Finch.handleConnectionEvent()");

               // connection complete, so release the lock
               connectionCompleteSemaphore.release();
               }

            public void handleFailedConnectionEvent()
               {
//               LOG.trace("Finch.handleFailedConnectionEvent()");

               // connection failed, so release the lock
               connectionCompleteSemaphore.release();
               }
            });

//      LOG.trace("Finch.Finch(): 1) aquiring connection lock");

      // acquire the lock, which will be released once the connection is complete
      connectionCompleteSemaphore.acquireUninterruptibly();

//      LOG.trace("Finch.Finch(): 2) connecting");

      // try to connect
      connect();

//      LOG.trace("Finch.Finch(): 3) waiting for connection to complete");

      // try to acquire the lock again, which will block until the connection is complete
      connectionCompleteSemaphore.acquireUninterruptibly();

//      LOG.trace("Finch.Finch(): 4) releasing lock");

      // we know the connection has completed (i.e. either connected or the connection failed) at this point, so just release the lock
      connectionCompleteSemaphore.release();

//      LOG.trace("Finch.Finch(): 5) make sure we're actually connected");

      // if we're not connected, then throw an exception
      if (!isConnected())
         {
//         LOG.error("Finch.Finch(): Failed to connect to the finch!  Aborting.");
         System.exit(1);
         }

//      LOG.trace("Finch.Finch(): 6) All done!");

      // Set system properties to point to the freeTTS directory for saySomething support
      System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
      }

   /**
    * Sets the color of the LED in the Finch's beak using a Color object.
    *
    * @param     color is a Color object that determines the beaks color
    */
   @Override
   public void setLED(final int color)
      {
      if (color != 0)
         {
         final FullColorLEDService service = getFullColorLEDService();
         if (service != null)
            {
            service.set(0, color);
            }
         else
            {
            System.out.println("LED not responding, check Finch connection");
            }
         }
      else
         {
         System.out.println("Color object was null, LED could not be set");
         }
      }

   /**
    * Sets the color of the LED in the Finch's beak.  The LED can be any color that can be
    * created by mixing red, green, and blue; turning on all three colors in equal amounts results
    * in white light.  Valid ranges for the red, green, and blue elements are 0 to 255.
    *
    * @param     red sets the intensity of the red element of the LED
    * @param     green sets the intensity of the green element of the LED
    * @param     blue sets the intensity of the blue element of the LED
    */

   @Override
   public void setLED(final int red, final int green, final int blue)
      {
      boolean inRange = true;
      if (red > FinchConstants.FULL_COLOR_LED_DEVICE_MAX_INTENSITY)
         {
         inRange = false;
         System.out.println("Red value exceeds appropriate values (0-255), LED will not be set");
         }
      if (red < FinchConstants.FULL_COLOR_LED_DEVICE_MIN_INTENSITY)
         {
         inRange = false;
         System.out.println("Red value is negative, LED will not be set");
         }

      if (green > FinchConstants.FULL_COLOR_LED_DEVICE_MAX_INTENSITY)
         {
         inRange = false;
         System.out.println("Green value exceeds appropriate values (0-255), LED will not be set");
         }
      if (green < FinchConstants.FULL_COLOR_LED_DEVICE_MIN_INTENSITY)
         {
         inRange = false;
         System.out.println("Green value is negative, LED will not be set");
         }

      if (blue > FinchConstants.FULL_COLOR_LED_DEVICE_MAX_INTENSITY)
         {
         inRange = false;
         System.out.println("Blue value exceeds appropriate values (0-255), LED will not be set");
         }
      if (blue < FinchConstants.FULL_COLOR_LED_DEVICE_MIN_INTENSITY)
         {
         inRange = false;
         System.out.println("Blue value is negative, LED will not be set");
         }

      if (inRange)
         {
         setLED(Color.rgb(red, green, blue));
         }
      }

   /**
    * Sets the color of the LED in the Finch's beak using a Color object for the length of time specified by duration.
    *
    * @param     color is a Color object that determines the beaks color
    * @param     duration is the length of time the color will display on the beak
    */
   @Override
   public void setLED(final int color, final int duration)
      {
      if (color != 0)
         {
         final FullColorLEDService service = getFullColorLEDService();
         if (service != null)
            {
            service.set(0, color);
            sleep(duration);
            setLED(Color.rgb(0, 0, 0));
            }
         else
            {
            System.out.println("LED not responding, check Finch connection");
            }
         }
      else
         {
         System.out.println("Color object was null, LED could not be set");
         }
      }

   /**
    * Sets the color of the LED in the Finch's beak for the length of time specified by duration.  
    * The LED can be any color that can be created by mixing red, green, and blue; turning on all three colors in equal amounts results
    * in white light.  Valid ranges for the red, green, and blue elements are 0 to 255.
    *
    * @param     red sets the intensity of the red element of the LED
    * @param     green sets the intensity of the green element of the LED
    * @param     blue sets the intensity of the blue element of the LED
    * @param     duration is the length of time the color will display on the beak
    */

   @Override
   public void setLED(final int red, final int green, final int blue, final int duration)
      {
      boolean inRange = true;
      if (red > FinchConstants.FULL_COLOR_LED_DEVICE_MAX_INTENSITY)
         {
         inRange = false;
         System.out.println("Red value exceeds appropriate values (0-255), LED will not be set");
         }
      if (red < FinchConstants.FULL_COLOR_LED_DEVICE_MIN_INTENSITY)
         {
         inRange = false;
         System.out.println("Red value is negative, LED will not be set");
         }

      if (green > FinchConstants.FULL_COLOR_LED_DEVICE_MAX_INTENSITY)
         {
         inRange = false;
         System.out.println("Green value exceeds appropriate values (0-255), LED will not be set");
         }
      if (green < FinchConstants.FULL_COLOR_LED_DEVICE_MIN_INTENSITY)
         {
         inRange = false;
         System.out.println("Green value is negative, LED will not be set");
         }

      if (blue > FinchConstants.FULL_COLOR_LED_DEVICE_MAX_INTENSITY)
         {
         inRange = false;
         System.out.println("Blue value exceeds appropriate values (0-255), LED will not be set");
         }
      if (blue < FinchConstants.FULL_COLOR_LED_DEVICE_MIN_INTENSITY)
         {
         inRange = false;
         System.out.println("Blue value is negative, LED will not be set");
         }

      if (inRange)
         {
         setLED(Color.rgb(red, green, blue));
         sleep(duration);
         setLED(Color.rgb(0, 0, 0));
         }
      }

   /**
    * Stops both wheels.
    */
   @Override
   public void stopWheels()
      {
      setWheelVelocities(0, 0);
      }

   /**
    * This method simultaneously sets the velocities of both wheels. Current valid values range from
    * -255 to 255; negative values cause a wheel to move backwards.
    *
    * @param leftVelocity The velocity at which to move the left wheel
    * @param rightVelocity The velocity at which to move the right wheel
    */
   @Override
   public void setWheelVelocities(final int leftVelocity, final int rightVelocity)
      {
      setWheelVelocities(leftVelocity, rightVelocity, -1);
      }

   /**
    * This method simultaneously sets the velocities of both wheels. Current valid values range from
    * -255 to 255.  If <code>timeToHold</code> is positive, this method blocks further program execution for the amount
    * of time specified by timeToHold, and then stops the wheels once time has elapsed.
    *
    * @param leftVelocity The velocity in native units at which to move the left wheel
    * @param rightVelocity The velocity in native units at which to move the right wheel
    * @param timeToHold The amount of time in milliseconds to hold the velocity for; if 0 or negative, program
    *                   execution is not blocked and the wheels are not stopped.
    */
   @Override
   public void setWheelVelocities(final int leftVelocity, final int rightVelocity, final int timeToHold)
      {
      final OpenLoopVelocityControllableMotorService service = getOpenLoopVelocityControllableMotorService();
      if (service != null)
         {
         if (leftVelocity <= FinchConstants.MOTOR_DEVICE_MAX_VELOCITY &&
             leftVelocity >= FinchConstants.MOTOR_DEVICE_MIN_VELOCITY &&
             rightVelocity <= FinchConstants.MOTOR_DEVICE_MAX_VELOCITY &&
             rightVelocity >= FinchConstants.MOTOR_DEVICE_MIN_VELOCITY)
            {
            service.setVelocities(new int[]{leftVelocity, rightVelocity});
            if (timeToHold > 0)
               {
               sleep(timeToHold);
               stopWheels();
               }
            }
         else
            {
            System.out.println("Velocity values out of range");
            }
         }
      else
         {
         System.out.println("Couldn't set motors, check Finch connection");
         }
      }

   /**
    * This method uses Thread.sleep to cause the currently running program to sleep for the
    * specified number of seconds.
    *
    * @param ms - the number of milliseconds to sleep for.  Valid values are all positive integers.
    */
   @Override
   public void sleep(final int ms)
      {
      if (ms < 0)
         {
         System.out.println("Program sent a negative time to sleep for");
         }
      else
         {
         try
            {
            Thread.sleep(ms);
            }
         catch (InterruptedException ignored)
            {
            System.out.println("Error:  sleep was interrupted for some reason");
            }
         }
      }

   /**
    * This method returns the current X-axis acceleration value experienced by the robot.  Values for acceleration
    * range from -1.5 to +1.5g.  The X-axis is the beak-tail axis.
    *
    * @return The X-axis acceleration value
    */
   @Override
   public double getXAcceleration()
      {
      final AccelerometerService service = getAccelerometerService();
      if (service != null)
         {
         final AccelerometerGs accelerometerGs = service.getAccelerometerGs(0);
         if (accelerometerGs != null)
            {
            return accelerometerGs.getX();
            }
         }
      System.out.println("Accelerometer not responding, check Finch connection");
      return 0.0;
      }

   /**
    * This method returns the current Y-axis acceleration value experienced by the robot.  Values for acceleration
    * range from -1.5 to +1.5g.  The Y-axis is the wheel-to-wheel axis.
    *
    * @return The Y-axis acceleration value
    */
   @Override
   public double getYAcceleration()
      {
      final AccelerometerService service = getAccelerometerService();
      if (service != null)
         {
         final AccelerometerGs accelerometerGs = service.getAccelerometerGs(0);
         if (accelerometerGs != null)
            {
            return accelerometerGs.getY();
            }
         }
      System.out.println("Accelerometer not responding, check Finch connection");
      return 0.0;
      }

   /**
    * This method returns the current Z-axis acceleration value experienced by the robot.  Values for acceleration
    * range from -1.5 to +1.5g.  The Z-axis runs perpendicular to the Finch's circuit board.
    *
    * @return The Z-axis acceleration value
    */
   @Override
   public double getZAcceleration()
      {
      final AccelerometerService service = getAccelerometerService();
      if (service != null)
         {
         final AccelerometerGs accelerometerGs = service.getAccelerometerGs(0);
         if (accelerometerGs != null)
            {
            return accelerometerGs.getZ();
            }
         }
      System.out.println("Accelerometer not responding, check Finch connection");
      return 0.0;
      }

   /**
    * Use this method to simultaneously return the current X, Y, and Z accelerations experienced by the robot.
    * Values for acceleration can be in the range of -1.5g to +1.5g.  When the robot is on a flat surface,
    * X and Y should be close to 0g, and Z should be near +1.0g.
    *
    * @return a an array of 3 doubles containing the X, Y, and Z acceleration values
    */
   @Override
   public double[] getAccelerations()
      {
      final AccelerometerService service = getAccelerometerService();
      if (service != null)
         {
         final AccelerometerGs accelerometerGs = service.getAccelerometerGs(0);
         if (accelerometerGs != null)
            {
            final double[] accelerations = new double[3];
            accelerations[0] = accelerometerGs.getX();
            accelerations[1] = accelerometerGs.getY();
            accelerations[2] = accelerometerGs.getZ();
            return accelerations;
            }
         }
      System.out.println("Accelerometer not responding, check Finch connection");
      return null;
      }

   /**
    * This method returns true if the beak is up (Finch sitting on its tail), false otherwise
    *
    * @return true if beak is pointed at ceiling
    */
   @Override
   public boolean isBeakUp()
      {
      final double[] accels = getAccelerations();
      if (accels != null)
         {
         if (accels[0] < -0.8 && accels[0] > -1.5 && accels[1] > -0.3 && accels[1] < 0.3 && accels[2] > -0.3 && accels[2] < 0.3)
            {
            return true;
            }
         }
      return false;
      }

   /**
    * This method returns true if the beak is pointed at the floor, false otherwise
    *
    * @return true if beak is pointed at the floor
    */
   @Override
   public boolean isBeakDown()
      {
      final double[] accels = getAccelerations();
      if (accels != null)
         {
         if (accels[0] < 1.5 && accels[0] > 0.8 && accels[1] > -0.3 && accels[1] < 0.3 && accels[2] > -0.3 && accels[2] < 0.3)
            {
            return true;
            }
         }
      return false;
      }

   /**
    * This method returns true if the Finch is on a flat surface
    *
    * @return true if the Finch is level
    */
   @Override
   public boolean isFinchLevel()
      {
      final double[] accels = getAccelerations();
      if (accels != null)
         {
         if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > -0.5 && accels[1] < 0.5 && accels[2] > 0.65 && accels[2] < 1.5)
            {
            return true;
            }
         }
      return false;
      }

   /**
    * This method returns true if the Finch is upside down, false otherwise
    *
    * @return true if Finch is upside down
    */
   @Override
   public boolean isFinchUpsideDown()
      {
      final double[] accels = getAccelerations();
      if (accels != null)
         {
         if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > -0.5 && accels[1] < 0.5 && accels[2] > -1.5 && accels[2] < -0.65)
            {
            return true;
            }
         }
      return false;
      }

   /**
    * This method returns true if the Finch's left wing is pointed at the ground
    *
    * @return true if Finch's left wing is down
    */
   @Override
   public boolean isLeftWingDown()
      {
      final double[] accels = getAccelerations();
      if (accels != null)
         {
         if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > 0.7 && accels[1] < 1.5 && accels[2] > -0.5 && accels[2] < 0.5)
            {
            return true;
            }
         }
      return false;
      }

   /**
    * This method returns true if the Finch's right wing is pointed at the ground
    *
    * @return true if Finch's right wing is down
    */
   @Override
   public boolean isRightWingDown()
      {
      final double[] accels = getAccelerations();
      if (accels != null)
         {
         if (accels[0] > -0.5 && accels[0] < 0.5 && accels[1] > -1.5 && accels[1] < -0.7 && accels[2] > -0.5 && accels[2] < 0.5)
            {
            return true;
            }
         }
      return false;
      }

   /**
    *  Returns true if the Finch has been shaken since the last accelerometer read
    *
    *  @return true if the Finch was recently shaken
    */
   @Override
   public boolean isShaken()
      {
      final AccelerometerService service = getAccelerometerService();
      if (service != null)
         {
         final AccelerometerState accelerometerState = service.getAccelerometerState(0);
         if (accelerometerState != null)
            {
            return accelerometerState.wasShaken();
            }
         }
      System.out.println("Accelerometer not responding, check Finch connection");
      return false;
      }

   /**
    *  Returns true if the Finch has been tapped since the last accelerometer read
    *
    *  @return true if the Finch was recently tapped
    */
   @Override
   public boolean isTapped()
      {
      final AccelerometerService service = getAccelerometerService();
      if (service != null)
         {
         final AccelerometerState accelerometerState = service.getAccelerometerState(0);
         if (accelerometerState != null)
            {
            return accelerometerState.wasTapped();
            }
         }
      System.out.println("Accelerometer not responding, check Finch connection");
      return false;
      }

   /**
    * Plays a tone over the computer speakers or headphones at a given frequency (in Hertz) for
    * a specified duration in milliseconds.  Middle C is about 262Hz.  Visit http://www.phy.mtu.edu/~suits/notefreqs.html for
    * frequencies of musical notes.
    *
    * @param frequency The frequency of the tone in Hertz
    * @param duration The time to play the tone in milliseconds
    */
   @Override
   public void playTone(final int frequency, final int duration)
      {
      playTone(frequency, FinchConstants.AUDIO_DEVICE_MAX_AMPLITUDE, duration);
      }

   /**
    * Plays a tone over the computer speakers or headphones at a given frequency (in Hertz) for
    * a specified duration in milliseconds at a specified volume.  Middle C is about 262Hz.
    * Visit http://www.phy.mtu.edu/~suits/notefreqs.html for frequencies of musical notes.
    *
    * @param frequency The frequency of the tone in Hertz
    * @param volume The volume of the tone on a 1 to 10 scale
    * @param duration The time to play the tone in milliseconds
    */
   @Override
   public void playTone(final int frequency, final int volume, final int duration)
      {
      final AudioService service = getAudioService();
      if (service != null)
         {
         service.playTone(frequency, volume, duration);
         }
      else
         {
         System.out.println("Audio not responding, check Finch connection");
         }
      }

   /**
    * Plays a wav file over computer speakers at the specificied fileLocation path.  If you place the audio
    * file in the same path as your source, you can just specify the name of the file.
    *
    * @param     fileLocation Absolute path of the file or name of the file if located in some directory as source code
    */
   @Override
   public void playClip(final String fileLocation)
      {
      final AudioService service = getAudioService();
      if (service != null)
         {
         try
            {
            final File file = new File(fileLocation);
            final byte[] rawSound = FileUtils.getFileAsBytes(file);
            service.playSound(rawSound);
            }
         catch (IOException e)
            {
//            LOG.error("IOException while trying to play sound at [" + fileLocation + "]", e);
            System.out.println("Failed to play sound.");
            }
         }
      else
         {
         System.out.println("Audio not responding, check Finch connection");
         }
      }

   /**
    * Takes the text of 'sayThis' and synthesizes it into a sound file and plays the sound file over
    * computer speakers.  sayThis can be arbitrarily long and can include variable arguments.
    *
    * Example:
    *   myFinch.saySomething("My light sensor has a value of "+ lightSensor + " and temperature is " + tempInCelcius);
    *
    * @param     sayThis The string of text that will be spoken by the computer
    */
   @Override
   public void saySomething(final String sayThis)
      {
      if (sayThis != null && sayThis.length() > 0)
         {
         final AudioService service = getAudioService();
         if (service != null)
            {
            service.speak(sayThis);
            }
         else
            {
            System.out.println("Audio not responding, check Finch connection");
            }
         }
      else
         {
         System.out.println("Given text to speak was null or empty");
         }
      }

   /**
    * Takes the text of 'sayThis' and synthesizes it into a sound file and plays the sound file over
    * computer speakers. sayThis can be arbitrarily long and can include variable arguments. The duration
    * argument allows you to delay program execution for a number of milliseconds. 
    *
    * Example:
    *   myFinch.saySomething("My light sensor has a value of "+ lightSensor + " and temperature is " + tempInCelcius);
    *
    * @param     sayThis The string of text that will be spoken by the computer
    * @param     duration The time in milliseconds to halt further program execution
    */
   @Override
   public void saySomething(final String sayThis, final int duration)
      {
      if (sayThis != null && sayThis.length() > 0)
         {
         final AudioService service = getAudioService();
         if (service != null)
            {
            service.speak(sayThis);
            sleep(duration);
            }
         else
            {
            System.out.println("Audio not responding, check Finch connection");
            }
         }
      else
         {
         System.out.println("Given text to speak was null or empty");
         }
      }

   /**
    * Plays a tone at the specified frequency for the specified duration on the Finch's internal buzzer.
    * Middle C is about 262Hz.
    * Visit http://www.phy.mtu.edu/~suits/notefreqs.html for frequencies of musical notes.
    * Note that this is different from playTone, which plays a tone on the computer's speakers.
    * Also note that buzz is non-blocking - so if you call two buzz methods in a row without
    * an intervening sleep, you will only hear the second buzz (it will over-write the first buzz).
    *
    * @param     frequency Frequency in Hertz of the tone to be played
    * @param     duration  Duration in milliseconds of the tone
    */
   @Override
   public void buzz(final int frequency, final int duration)
      {
      final BuzzerService service = getBuzzerService();
      if (service != null)
         {
         service.playTone(0, frequency, duration);
         }
      else
         {
         System.out.println("Buzzer not responding, check Finch connection");
         }
      }

   /**
    * Plays a tone at the specified frequency for the specified duration on the Finch's internal buzzer.
    * Middle C is about 262Hz.
    * Visit http://www.phy.mtu.edu/~suits/notefreqs.html for frequencies of musical notes.
    * Note that this is different from playTone, which plays a tone on the computer's speakers.
    * Unlike the buzz method, this method will block program execution for the time specified by duration.
    *
    * @param     frequency Frequency in Hertz of the tone to be played
    * @param     duration  Duration in milliseconds of the tone
    */
   @Override
   public void buzzBlocking(final int frequency, final int duration)
      {
      buzz(frequency, duration);
      sleep(duration);
      }

   /**
    * Returns the value of the left light sensor.  Valid values range from 0 to 255, with higher
    * values indicating more light is being detected by the sensor.
    *
    *
    * @return The current light level at the left light sensor
    */
   @Override
   public int getLeftLightSensor()
      {
      final PhotoresistorService service = getPhotoresistorService();
      if (service != null)
         {
         final int[] values = service.getPhotoresistorValues();
         if (values != null)
            {
            return values[0];
            }
         }

      System.out.println("Light sensor not responding, check Finch connection");
      return 0;
      }

   /**
    * Returns the value of the right light sensor.  Valid values range from 0 to 255, with higher
    * values indicating more light is being detected by the sensor.
    *
    *
    * @return The current light level at the right light sensor
    */
   @Override
   public int getRightLightSensor()
      {
      final PhotoresistorService service = getPhotoresistorService();
      if (service != null)
         {
         final int[] values = service.getPhotoresistorValues();
         if (values != null)
            {
            return values[1];
            }
         }

      System.out.println("Light sensor not responding, check Finch connection");
      return 0;
      }

   /**
    * Returns a 2 integer array containing the current values of both light sensors.
    * The left sensor is the 0th array element, and the right sensor is the 1st element.
    *
    *
    * @return A 2 int array containing both light sensor readings.
    */
   @Override
   public int[] getLightSensors()
      {
      final PhotoresistorService service = getPhotoresistorService();
      if (service != null)
         {
         return service.getPhotoresistorValues();
         }
      else
         {
         System.out.println("Light sensor not responding, check Finch connection");
         return null;
         }
      }

   /**
    * Returns true if the left light sensor is greater than the value specified
    * by limit, false otherwise.
    *
    * @param limit The value the light sensor needs to exceed
    * @return whether the light sensor exceeds the value specified by limit
    */
   @Override
   public boolean isLeftLightSensor(final int limit)
      {
      return (limit < getLeftLightSensor());
      }

   /**
    * Returns true if the right light sensor is greater than the value specified
    * by limit, false otherwise.
    *
    * @param limit The value the light sensor needs to exceed
    * @return true if the light sensor exceeds the value specified by limit
    */
   @Override
   public boolean isRightLightSensor(final int limit)
      {
      return (limit < getRightLightSensor());
      }

   /**
    * Returns true if there is an obstruction in front of the left side of the robot.
    *
    *
    * @return Whether an obstacle exists in front of the left side of the robot.
    */
   @Override
   public boolean isObstacleLeftSide()
      {
      final SimpleObstacleDetectorService service = getSimpleObstacleDetectorService();
      if (service != null)
         {
         return service.isObstacleDetected(0);
         }
      else
         {
         System.out.println("Obstacle sensor not responding, check Finch connection");
         return false;
         }
      }

   /**
    * Returns true if there is an obstruction in front of the right side of the robot.
    *
    *
    * @return Whether an obstacle exists in front of the right side of the robot.
    */
   @Override
   public boolean isObstacleRightSide()
      {
      final SimpleObstacleDetectorService service = getSimpleObstacleDetectorService();
      if (service != null)
         {
         return service.isObstacleDetected(1);
         }
      else
         {
         System.out.println("Obstacle sensor not responding, check Finch connection");
         return false;
         }
      }

   /**
    * Returns true if either left or right obstacle sensor detect an obstacle.
    *
    *
    * @return Whether either obstacle sensor sees an obstacle.
    */
   @Override
   public boolean isObstacle()
      {
      final SimpleObstacleDetectorService service = getSimpleObstacleDetectorService();
      if (service != null)
         {
         return (service.isObstacleDetected(0) || service.isObstacleDetected(1));
         }
      else
         {
         System.out.println("Obstacle sensor not responding, check Finch connection");
         return false;
         }
      }

   /**
    * Returns the value of both obstacle sensors as 2 element boolean array.
    * The left sensor is the 0th element, and the right sensor is the 1st element.
    *
    *
    * @return The values of left and right obstacle sensors in a 2 element array
    */
   @Override
   public boolean[] getObstacleSensors()
      {
      final SimpleObstacleDetectorService service = getSimpleObstacleDetectorService();
      if (service != null)
         {
         return service.areObstaclesDetected();
         }
      else
         {
         System.out.println("Obstacle sensors not responding, check Finch connection");
         return null;
         }
      }

   /**
    * The current temperature reading at the temperature probe.  The value
    * returned is in Celsius.  To get Fahrenheit from Celsius, multiply the number
    * by 1.8 and then add 32.
    *
    * @return The current temperature in degrees Celsius
    */
   @Override
   public double getTemperature()
      {
      final ThermistorService service = getThermistorService();
      if (service != null)
         {
         return service.getCelsiusTemperature(0);
         }
      else
         {
         System.out.println("Temperature sensor not responding, check Finch connection");
         return 0;
         }
      }

   /**
    * Returns true if the temperature is greater than the value specified
    * by limit, false otherwise.
    *
    * @param limit The value the temperature needs to exceed
    * @return true if the temperature exceeds the value specified by limit
    */
   @Override
   public boolean isTemperature(final double limit)
      {
      return (limit < getTemperature());
      }

   /**
    * Displays a graph of the X, Y, and Z accelerometer values.  Note that this graph
    * does not update on its own - you need to call updateAccelerometerGraph to
    * do so.
    *
    */

   @Override
   public void showAccelerometerGraph()
      {
//      accelerometerPlotter.addDataset(Color.RED);
//      accelerometerPlotter.addDataset(Color.GREEN);
//      accelerometerPlotter.addDataset(Color.BLUE);
//
//      //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
//      SwingUtilities.invokeLater(
//            new Runnable()
//            {
//            public void run()
//               {
//               final Component plotComponent = accelerometerPlotter.getComponent();
//
//               // create the main frame
//               jFrameAccel = new JFrame("Accelerometer Values");
//
//               // add the root panel to the JFrame
//               jFrameAccel.add(plotComponent);
//
//               // set various properties for the JFrame
//               jFrameAccel.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//               jFrameAccel.addWindowListener(
//                     new WindowAdapter()
//                     {
//                     @Override
//                     public void windowClosing(final WindowEvent e)
//                        {
//                        jFrameAccel.setVisible(false);
//                        jFrameAccel.dispose();
//                        }
//                     });
//               jFrameAccel.setBackground(Color.WHITE);
//               jFrameAccel.setResizable(false);
//               jFrameAccel.pack();
//               jFrameAccel.setLocation(400, 200);// center the window on the screen
//               jFrameAccel.setVisible(true);
//               }
//            });
      }

   /**
    * updates the accelerometer graph with accelerometer data specified by xVal,
    * yVal, and zVal.
    *
    * @param xVal  The X axis acceleration value
    * @param yVal  The Y axis acceleration value
    * @param zVal  The Z axis acceleration value
    */
   @Override
   public void updateAccelerometerGraph(final double xVal, final double yVal, final double zVal)
      {
//      accelerometerPlotter.setCurrentValues(xVal, yVal, zVal);
      }

   /**
    * Closes the opened Accelerometer Graph
    */
   @Override
   public void closeAccelerometerGraph()
      {
//      jFrameAccel.setVisible(false);
//      jFrameAccel.dispose();
      }

   /**
    * Displays a graph of the left and right light sensor values.  Note that this graph
    * does not update on its own - you need to call updateLightSensorGraph to
    * do so.
    *
    */

   @Override
   public void showLightSensorGraph()
      {
//      lightPlotter.addDataset(Color.RED);
//      lightPlotter.addDataset(Color.BLUE);
//
//      //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
//      SwingUtilities.invokeLater(
//            new Runnable()
//            {
//            public void run()
//               {
//               final Component plotComponent = lightPlotter.getComponent();
//
//               // create the main frame
//               jFrameLight = new JFrame("Light Sensor Values");
//
//               // add the root panel to the JFrame
//               jFrameLight.add(plotComponent);
//
//               // set various properties for the JFrame
//               jFrameLight.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//               jFrameLight.addWindowListener(
//                     new WindowAdapter()
//                     {
//                     @Override
//                     public void windowClosing(final WindowEvent e)
//                        {
//                        jFrameLight.setVisible(false);
//                        jFrameLight.dispose();
//                        }
//                     });
//               jFrameLight.setBackground(Color.WHITE);
//               jFrameLight.setResizable(false);
//               jFrameLight.pack();
//               jFrameLight.setLocation(20, 200);// center the window on the screen
//               jFrameLight.setVisible(true);
//               }
//            });
      }

   /**
    * Updates the light sensor graph with the left and right light sensor data.
    *
    * @param leftSensor  Variable containing left light sensor value
    * @param rightSensor  Variable containing right light sensor value
    */
   @Override
   public void updateLightSensorGraph(final int leftSensor, final int rightSensor)
      {
//      lightPlotter.setCurrentValues(leftSensor, rightSensor);
      }

   /**
    * Closes the opened Light sensor Graph
    */
   @Override
   public void closeLightSensorGraph()
      {
//      jFrameLight.setVisible(false);
//      jFrameLight.dispose();
      }

   /**
    * Displays a graph of the temperature value.  Note that this graph
    * does not update on its own - you need to call updateTemperatureGraph to
    * do so.
    *
    */

   @Override
   public void showTemperatureGraph()
      {
//      temperaturePlotter.addDataset(Color.GREEN);
//
//      //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
//      SwingUtilities.invokeLater(
//            new Runnable()
//            {
//            public void run()
//               {
//               final Component plotComponent = temperaturePlotter.getComponent();
//
//               // create the main frame
//               jFrameTemp = new JFrame("Temperature Values");
//
//               // add the root panel to the JFrame
//               jFrameTemp.add(plotComponent);
//
//               // set various properties for the JFrame
//               jFrameTemp.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//               jFrameTemp.addWindowListener(
//                     new WindowAdapter()
//                     {
//                     @Override
//                     public void windowClosing(final WindowEvent e)
//                        {
//                        jFrameTemp.setVisible(false);
//                        jFrameTemp.dispose();
//                        }
//                     });
//               jFrameTemp.setBackground(Color.WHITE);
//               jFrameTemp.setResizable(false);
//               jFrameTemp.pack();
//               jFrameTemp.setLocation(780, 200);// center the window on the screen
//               jFrameTemp.setVisible(true);
//               }
//            });
      }

   /**
    * Updates the temperature graph with the most recent temperature data.
    *
    * @param temp   variable containing a temperature value
    */

   @Override
   public void updateTemperatureGraph(final double temp)
      {
//      temperaturePlotter.setCurrentValues(temp);
      }

   /**
    * Closes the opened temperature Graph
    */
   @Override
   public void closeTemperatureGraph()
      {
//      jFrameTemp.setVisible(false);
//      jFrameTemp.dispose();
      }

   /**
    * This method properly closes the connection with the Finch and resets the Finch so that
    * it is immediately ready to be controlled by subsequent programs.  Note that if this
    * method is not called at the end of the program, the Finch will continue to act on its
    * most recent command (such as drive forward) for 5 seconds before automatically timing
    * out and resetting.  This is why we recommend you always call the quit method at the end
    * of your program.
    */
   @Override
   public void quit()
      {
//      if (jFrameAccel != null)
//         {
//         closeAccelerometerGraph();
//         }
//      if (jFrameLight != null)
//         {
//         closeLightSensorGraph();
//         }
//      if (jFrameTemp != null)
//         {
//         closeTemperatureGraph();
//         }

      shutdown();
      }
   }


