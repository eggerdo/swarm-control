package edu.cmu.ri.createlab.terk.robot.finch;

//import java.awt.Color;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface FinchInterface
   {
   /**
    * Sets the color of the LED in the Finch's beak using a Color object.
    *
    * @param     color is a Color object that determines the beaks color
    */
   void setLED(int color);

   /**
    * Sets the color of the LED in the Finch's beak.  The LED can be any color that can be
    * created by mixing red, green, and blue; turning on all three colors in equal amounts results
    * in white light.  Valid ranges for the red, green, and blue elements are 0 to 255.
    *
    * @param     red sets the intensity of the red element of the LED
    * @param     green sets the intensity of the green element of the LED
    * @param     blue sets the intensity of the blue element of the LED
    */

   void setLED(int red, int green, int blue);

   /**
    * Sets the color of the LED in the Finch's beak using a Color object for the length of time specified by duration.
    *
    * @param     color is a Color object that determines the beaks color
    * @param     duration is the length of time the color will display on the beak
    */
   void setLED(int color, int duration);

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

   void setLED(int red, int green, int blue, int duration);

   /**
    * Stops both wheels.
    */
   void stopWheels();

   /**
    * This method simultaneously sets the velocities of both wheels. Current valid values range from
    * -255 to 255; negative values cause a wheel to move backwards.
    *
    * @param leftVelocity The velocity at which to move the left wheel
    * @param rightVelocity The velocity at which to move the right wheel
    */
   void setWheelVelocities(int leftVelocity, int rightVelocity);

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
   void setWheelVelocities(int leftVelocity, int rightVelocity, int timeToHold);

   /**
    * This method uses Thread.sleep to cause the currently running program to sleep for the
    * specified number of seconds.
    *
    * @param ms - the number of milliseconds to sleep for.  Valid values are all positive integers.
    */
   void sleep(int ms);

   /**
    * This method returns the current X-axis acceleration value experienced by the robot.  Values for acceleration
    * range from -1.5 to +1.5g.  The X-axis is the beak-tail axis.
    *
    * @return The X-axis acceleration value
    */
   double getXAcceleration();

   /**
    * This method returns the current Y-axis acceleration value experienced by the robot.  Values for acceleration
    * range from -1.5 to +1.5g.  The Y-axis is the wheel-to-wheel axis.
    *
    * @return The Y-axis acceleration value
    */
   double getYAcceleration();

   /**
    * This method returns the current Z-axis acceleration value experienced by the robot.  Values for acceleration
    * range from -1.5 to +1.5g.  The Z-axis runs perpendicular to the Finch's circuit board.
    *
    * @return The Z-axis acceleration value
    */
   double getZAcceleration();

   /**
    * Use this method to simultaneously return the current X, Y, and Z accelerations experienced by the robot.
    * Values for acceleration can be in the range of -1.5g to +1.5g.  When the robot is on a flat surface,
    * X and Y should be close to 0g, and Z should be near +1.0g.
    *
    * @return a an array of 3 doubles containing the X, Y, and Z acceleration values
    */
   double[] getAccelerations();

   /**
    * This method returns true if the beak is up (Finch sitting on its tail), false otherwise
    *
    * @return true if beak is pointed at ceiling
    */
   boolean isBeakUp();

   /**
    * This method returns true if the beak is pointed at the floor, false otherwise
    *
    * @return true if beak is pointed at the floor
    */
   boolean isBeakDown();

   /**
    * This method returns true if the Finch is on a flat surface
    *
    * @return true if the Finch is level
    */
   boolean isFinchLevel();

   /**
    * This method returns true if the Finch is upside down, false otherwise
    *
    * @return true if Finch is upside down
    */
   boolean isFinchUpsideDown();

   /**
    * This method returns true if the Finch's left wing is pointed at the ground
    *
    * @return true if Finch's left wing is down
    */
   boolean isLeftWingDown();

   /**
    * This method returns true if the Finch's right wing is pointed at the ground
    *
    * @return true if Finch's right wing is down
    */
   boolean isRightWingDown();

   /**
    *  Returns true if the Finch has been shaken since the last accelerometer read
    *
    *  @return true if the Finch was recently shaken
    */
   boolean isShaken();

   /**
    *  Returns true if the Finch has been tapped since the last accelerometer read
    *
    *  @return true if the Finch was recently tapped
    */
   boolean isTapped();

   /**
    * Plays a tone over the computer speakers or headphones at a given frequency (in Hertz) for
    * a specified duration in milliseconds.  Middle C is about 262Hz.  Visit http://www.phy.mtu.edu/~suits/notefreqs.html for
    * frequencies of musical notes.
    *
    * @param frequency The frequency of the tone in Hertz
    * @param duration The time to play the tone in milliseconds
    */
   void playTone(int frequency, int duration);

   /**
    * Plays a tone over the computer speakers or headphones at a given frequency (in Hertz) for
    * a specified duration in milliseconds at a specified volume.  Middle C is about 262Hz.
    * Visit http://www.phy.mtu.edu/~suits/notefreqs.html for frequencies of musical notes.
    *
    * @param frequency The frequency of the tone in Hertz
    * @param volume The volume of the tone on a 1 to 10 scale
    * @param duration The time to play the tone in milliseconds
    */
   void playTone(int frequency, int volume, int duration);

   /**
    * Plays a wav file over computer speakers at the specificied fileLocation path.  If you place the audio
    * file in the same path as your source, you can just specify the name of the file.
    *
    * @param     fileLocation Absolute path of the file or name of the file if located in some directory as source code
    */
   void playClip(String fileLocation);

   /**
    * Takes the text of 'sayThis' and synthesizes it into a sound file and plays the sound file over
    * computer speakers.  sayThis can be arbitrarily long and can include variable arguments.
    *
    * Example:
    *   myFinch.saySomething("My light sensor has a value of "+ lightSensor + " and temperature is " + tempInCelcius);
    *
    * @param     sayThis The string of text that will be spoken by the computer
    */
   void saySomething(String sayThis);

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
   void saySomething(String sayThis, int duration);

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
   void buzz(int frequency, int duration);

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
   void buzzBlocking(final int frequency, final int duration);
   
   /**
    * Returns the value of the left light sensor.  Valid values range from 0 to 255, with higher
    * values indicating more light is being detected by the sensor.
    *
    *
    * @return The current light level at the left light sensor
    */
   int getLeftLightSensor();

   /**
    * Returns the value of the right light sensor.  Valid values range from 0 to 255, with higher
    * values indicating more light is being detected by the sensor.
    *
    *
    * @return The current light level at the right light sensor
    */
   int getRightLightSensor();

   /**
    * Returns a 2 integer array containing the current values of both light sensors.
    * The left sensor is the 0th array element, and the right sensor is the 1st element.
    *
    *
    * @return A 2 int array containing both light sensor readings.
    */
   int[] getLightSensors();

   /**
    * Returns true if the left light sensor is great than the value specified
    * by limit, false otherwise.
    *
    * @param limit The value the light sensor needs to exceed
    * @return whether the light sensor exceeds the value specified by limit
    */
   boolean isLeftLightSensor(int limit);

   /**
    * Returns true if the right light sensor is greater than the value specified
    * by limit, false otherwise.
    *
    * @param limit The value the light sensor needs to exceed
    * @return true if the light sensor exceeds the value specified by limit
    */
   boolean isRightLightSensor(int limit);

   /**
    * Returns true if there is an obstruction in front of the left side of the robot.
    *
    *
    * @return Whether an obstacle exists in front of the left side of the robot.
    */
   boolean isObstacleLeftSide();

   /**
    * Returns true if there is an obstruction in front of the right side of the robot.
    *
    *
    * @return Whether an obstacle exists in front of the right side of the robot.
    */
   boolean isObstacleRightSide();

   /**
    * Returns true if either left or right obstacle sensor detect an obstacle.
    *
    *
    * @return Whether either obstacle sensor sees an obstacle.
    */
   boolean isObstacle();

   /**
    * Returns the value of both obstacle sensors as 2 element boolean array.
    * The left sensor is the 0th element, and the right sensor is the 1st element.
    *
    *
    * @return The values of left and right obstacle sensors in a 2 element array
    */
   boolean[] getObstacleSensors();

   /**
    * The current temperature reading at the temperature probe.  The value
    * returned is in Celsius.  To get Fahrenheit from Celsius, multiply the number
    * by 1.8 and then add 32.
    *
    * @return The current temperature in degrees Celsius
    */
   double getTemperature();

   /**
    * Returns true if the temperature is greater than the value specified
    * by limit, false otherwise.
    *
    * @param limit The value the temperature needs to exceed
    * @return true if the temperature exceeds the value specified by limit
    */
   boolean isTemperature(double limit);

   /**
    * Displays a graph of the X, Y, and Z accelerometer values.  Note that this graph
    * does not update on its own - you need to call updateAccelerometerGraph to
    * do so.
    *
    */

   void showAccelerometerGraph();

   /**
    * updates the accelerometer graph with accelerometer data specified by xVal,
    * yVal, and zVal.
    *
    * @param xVal  The X axis acceleration value
    * @param yVal  The Y axis acceleration value
    * @param zVal  The Z axis acceleration value
    */
   void updateAccelerometerGraph(double xVal, double yVal, double zVal);

   /**
    * Closes the opened Accelerometer Graph
    */
   void closeAccelerometerGraph();

   /**
    * Displays a graph of the left and right light sensor values.  Note that this graph
    * does not update on its own - you need to call updateLightSensorGraph to
    * do so.
    *
    */

   void showLightSensorGraph();

   /**
    * Updates the light sensor graph with the left and right light sensor data.
    *
    * @param leftSensor  Variable containing left light sensor value
    * @param rightSensor  Variable containing right light sensor value
    */
   void updateLightSensorGraph(int leftSensor, int rightSensor);

   /**
    * Closes the opened Light sensor Graph
    */
   void closeLightSensorGraph();

   /**
    * Displays a graph of the temperature value.  Note that this graph
    * does not update on its own - you need to call updateTemperatureGraph to
    * do so.
    *
    */

   void showTemperatureGraph();

   /**
    * Updates the temperature graph with the most recent temperature data.
    *
    * @param temp   variable containing a temperature value
    */

   void updateTemperatureGraph(double temp);

   /**
    * Closes the opened temperature Graph
    */
   void closeTemperatureGraph();

   /**
    * This method properly closes the connection with the Finch and resets the Finch so that
    * it is immediately ready to be controlled by subsequent programs.  Note that if this
    * method is not called at the end of the program, the Finch will continue to act on its
    * most recent command (such as drive forward) for 5 seconds before automatically timing
    * out and resetting.  This is why we recommend you always call the quit method at the end
    * of your program.
    */
   void quit();
   }