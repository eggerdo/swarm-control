package edu.cmu.ri.createlab.terk.robot.finch;

//import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.graphics.Color;

//import sun.reflect.generics.reflectiveObjects.NotImplementedException;
//import edu.cmu.ri.createlab.audio.AudioHelper;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
//import edu.cmu.ri.createlab.speech.Mouth;
import edu.cmu.ri.createlab.terk.robot.finch.commands.BuzzerCommandStrategy;
import edu.cmu.ri.createlab.terk.robot.finch.commands.DisconnectCommandStrategy;
import edu.cmu.ri.createlab.terk.robot.finch.commands.EmergencyStopCommandStrategy;
import edu.cmu.ri.createlab.terk.robot.finch.commands.FullColorLEDCommandStrategy;
import edu.cmu.ri.createlab.terk.robot.finch.commands.GetAccelerometerCommandStrategy;
import edu.cmu.ri.createlab.terk.robot.finch.commands.GetObstacleSensorCommandStrategy;
import edu.cmu.ri.createlab.terk.robot.finch.commands.GetPhotoresistorCommandStrategy;
import edu.cmu.ri.createlab.terk.robot.finch.commands.GetThermistorCommandStrategy;
import edu.cmu.ri.createlab.terk.robot.finch.commands.MotorVelocityCommandStrategy;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerGs;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerState;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerUnitConversionStrategy;
import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerUnitConversionStrategyFinder;
import edu.cmu.ri.createlab.terk.services.thermistor.ThermistorUnitConversionStrategy;
import edu.cmu.ri.createlab.terk.services.thermistor.ThermistorUnitConversionStrategyFinder;
import edu.cmu.ri.createlab.usb.hid.HIDCommandExecutionQueue;
import edu.cmu.ri.createlab.usb.hid.HIDCommandResponse;
import edu.cmu.ri.createlab.usb.hid.HIDConnectionException;
import edu.cmu.ri.createlab.usb.hid.HIDDevice;
import edu.cmu.ri.createlab.usb.hid.HIDDeviceFactory;
import edu.cmu.ri.createlab.usb.hid.HIDDeviceFailureException;
import edu.cmu.ri.createlab.usb.hid.HIDDeviceNoReturnValueCommandExecutor;
import edu.cmu.ri.createlab.usb.hid.HIDDeviceNotConnectedException;
import edu.cmu.ri.createlab.usb.hid.HIDDeviceNotFoundException;
import edu.cmu.ri.createlab.usb.hid.HIDDeviceReturnValueCommandExecutor;
import edu.cmu.ri.createlab.util.MathUtils;
import edu.cmu.ri.createlab.util.NotImplementedException;
import edu.cmu.ri.createlab.util.commandexecution.CommandExecutionFailureHandler;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
//import org.apache.commons.lang.NotImplementedException;
//import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class DefaultFinchController implements FinchController
   {
//   private static final Logger LOG = Logger.getLogger(DefaultFinchController.class);
   private static final int DELAY_BETWEEN_PEER_PINGS = 2;

   @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
   public static FinchController create()
      {
      try
         {
         // create the HID device
//         if (LOG.isDebugEnabled())
//            {
//            LOG.debug("DefaultFinchController.create(): creating HID device for vendor ID [" + Integer.toHexString(FinchConstants.USB_VENDOR_ID) + "] and product ID [" + Integer.toHexString(FinchConstants.USB_PRODUCT_ID) + "]");
//            }
         final HIDDevice hidDevice = HIDDeviceFactory.create(FinchConstants.FINCH_HID_DEVICE_DESCRIPTOR);

//         LOG.debug("DefaultFinchController.create(): attempting connection...");
         hidDevice.connectExclusively();

         // create the HID device command execution queue (which will attempt to connect to the device)
         final HIDCommandExecutionQueue commandQueue = new HIDCommandExecutionQueue(hidDevice);
         if (commandQueue != null)
            {
            // create the FinchController
            final DefaultFinchController finchController = new DefaultFinchController(commandQueue, hidDevice);

            // call the emergency stop command immediately, to make sure the LED and motors are turned off.
            finchController.emergencyStop();

            return finchController;
            }
         }
      catch (NotImplementedException e)
         {
//         LOG.error("NotImplementedException caught while trying to create the HIDCommandExecutionQueue", e);
         System.err.println(e);
         System.exit(1);
         }
      catch (HIDConnectionException e)
         {
//         LOG.error("HIDConnectionException while trying to connect to the Finch, returning null", e);
         }
      catch (HIDDeviceNotFoundException e)
         {
//         LOG.error("HIDDeviceNotFoundException while trying to connect to the Finch, returning null", e);
         }
      return null;
      }

   private boolean isDisconnected = false;
   private final HIDCommandExecutionQueue commandQueue;
   private final HIDDevice hidDevice;
   private final FinchPinger pinger = new FinchPinger();
   private final ScheduledExecutorService peerPingScheduler = Executors.newScheduledThreadPool(1, new DaemonThreadFactory("FinchController.peerPingScheduler"));
   private final ScheduledFuture<?> peerPingScheduledFuture;

   private final DisconnectCommandStrategy disconnectHIDCommandStrategy = new DisconnectCommandStrategy();
   private final GetAccelerometerCommandStrategy getAccelerometerCommandStrategy = new GetAccelerometerCommandStrategy();
   private final GetObstacleSensorCommandStrategy getObstacleSensorCommandStrategy = new GetObstacleSensorCommandStrategy();
   private final GetPhotoresistorCommandStrategy getPhotoresistorCommandStrategy = new GetPhotoresistorCommandStrategy();
   private final GetThermistorCommandStrategy getThermistorCommandStrategy = new GetThermistorCommandStrategy();
   private final EmergencyStopCommandStrategy emergencyStopCommandStrategy = new EmergencyStopCommandStrategy();

   private final HIDDeviceNoReturnValueCommandExecutor noReturnValueCommandExecutor;
   private final HIDDeviceReturnValueCommandExecutor<AccelerometerState> accelerometerStateReturnValueCommandExecutor;
   private final HIDDeviceReturnValueCommandExecutor<boolean[]> booleanArrayStateReturnValueCommandExecutor;
   private final HIDDeviceReturnValueCommandExecutor<int[]> intArrayrStateReturnValueCommandExecutor;
   private final HIDDeviceReturnValueCommandExecutor<Integer> integerReturnValueCommandExecutor;

   private final AccelerometerUnitConversionStrategy accelerometerUnitConversionStrategy = AccelerometerUnitConversionStrategyFinder.getInstance().lookup(FinchConstants.ACCELEROMETER_DEVICE_ID);
   private final ThermistorUnitConversionStrategy thermistorUnitConversionStrategy = ThermistorUnitConversionStrategyFinder.getInstance().lookup(FinchConstants.THERMISTOR_DEVICE_ID);
   private final Collection<CreateLabDevicePingFailureEventListener> createLabDevicePingFailureEventListeners = new HashSet<CreateLabDevicePingFailureEventListener>();

   private DefaultFinchController(final HIDCommandExecutionQueue commandQueue, final HIDDevice hidDevice)
      {
      this.commandQueue = commandQueue;
      this.hidDevice = hidDevice;

      final CommandExecutionFailureHandler commandExecutionFailureHandler =
            new CommandExecutionFailureHandler()
            {
            public void handleExecutionFailure()
               {
               pinger.forceFailure();
               }
            };

      noReturnValueCommandExecutor = new HIDDeviceNoReturnValueCommandExecutor(commandQueue, commandExecutionFailureHandler);
      accelerometerStateReturnValueCommandExecutor = new HIDDeviceReturnValueCommandExecutor<AccelerometerState>(commandQueue, commandExecutionFailureHandler);
      booleanArrayStateReturnValueCommandExecutor = new HIDDeviceReturnValueCommandExecutor<boolean[]>(commandQueue, commandExecutionFailureHandler);
      intArrayrStateReturnValueCommandExecutor = new HIDDeviceReturnValueCommandExecutor<int[]>(commandQueue, commandExecutionFailureHandler);
      integerReturnValueCommandExecutor = new HIDDeviceReturnValueCommandExecutor<Integer>(commandQueue, commandExecutionFailureHandler);

      // schedule periodic peer pings
      peerPingScheduledFuture = peerPingScheduler.scheduleAtFixedRate(pinger,
                                                                      DELAY_BETWEEN_PEER_PINGS, // delay before first ping
                                                                      DELAY_BETWEEN_PEER_PINGS, // delay between pings
                                                                      TimeUnit.SECONDS);
      }

   public String getPortName()
      {
      return hidDevice.getDeviceFilename();
      }

   public void addCreateLabDevicePingFailureEventListener(final CreateLabDevicePingFailureEventListener listener)
      {
      if (listener != null)
         {
         createLabDevicePingFailureEventListeners.add(listener);
         }
      }

   public void removeCreateLabDevicePingFailureEventListener(final CreateLabDevicePingFailureEventListener listener)
      {
      if (listener != null)
         {
         createLabDevicePingFailureEventListeners.remove(listener);
         }
      }

   public AccelerometerState getAccelerometerState()
      {
      return accelerometerStateReturnValueCommandExecutor.execute(getAccelerometerCommandStrategy);
      }

   public boolean[] areObstaclesDetected()
      {
      return booleanArrayStateReturnValueCommandExecutor.execute(getObstacleSensorCommandStrategy);
      }

   public int[] getPhotoresistors()
      {
      return intArrayrStateReturnValueCommandExecutor.execute(getPhotoresistorCommandStrategy);
      }

   public Integer getThermistor(final int id)
      {
      if (id >= 0 && id < FinchConstants.THERMISTOR_DEVICE_COUNT)
         {
         return integerReturnValueCommandExecutor.execute(getThermistorCommandStrategy);
         }

      return null;
      }

   public boolean setFullColorLED(final int red, final int green, final int blue)
      {
      return noReturnValueCommandExecutor.execute(new FullColorLEDCommandStrategy(red, green, blue));
      }

   public boolean setMotorVelocities(final int leftVelocity, final int rightVelocity)
      {
      return noReturnValueCommandExecutor.execute(new MotorVelocityCommandStrategy(MathUtils.ensureRange(leftVelocity, FinchConstants.MOTOR_DEVICE_MIN_VELOCITY, FinchConstants.MOTOR_DEVICE_MAX_VELOCITY),
                                                                                   MathUtils.ensureRange(rightVelocity, FinchConstants.MOTOR_DEVICE_MIN_VELOCITY, FinchConstants.MOTOR_DEVICE_MAX_VELOCITY)));
      }

   public boolean playBuzzerTone(final int frequency, final int durationInMilliseconds)
      {
      return noReturnValueCommandExecutor.execute(new BuzzerCommandStrategy(frequency, durationInMilliseconds));
      }

   public void playTone(final int frequency, final int amplitude, final int duration)
      {
//      AudioHelper.playTone(frequency, amplitude, duration);
      }

   public void playClip(final byte[] data)
      {
//      AudioHelper.playClip(data);
      }

   @Override
   public final byte[] getSpeech(final String whatToSay)
      {
//      if (whatToSay != null && whatToSay.length() > 0)
//         {
//         final Mouth mouth = Mouth.getInstance();
//
//         if (mouth != null)
//            {
//            return mouth.getSpeech(whatToSay);
//            }
//         }
      return null;
      }

   @Override
   public final void speak(final String whatToSay)
      {
//      final byte[] speechAudio = getSpeech(whatToSay);
//      if (speechAudio != null)
//         {
//         // play it this way since Mouth.speak() is deprecated
//         AudioHelper.playClip(speechAudio);
//         }
      }

   public boolean emergencyStop()
      {
      return noReturnValueCommandExecutor.execute(emergencyStopCommandStrategy);
      }

   public void disconnect()
      {
      disconnect(true);
      }

   private void disconnect(final boolean willAddDisconnectCommandToQueue)
      {
      // turn off the peer pinger
      try
         {
//         LOG.debug("DefaultFinchController.disconnect(): Shutting down finch pinger...");
         peerPingScheduledFuture.cancel(false);
         peerPingScheduler.shutdownNow();
//         LOG.debug("DefaultFinchController.disconnect(): Successfully shut down finch pinger.");
         }
      catch (Exception e)
         {
//         LOG.error("DefaultFinchController.disconnect(): Exception caught while trying to shut down peer pinger", e);
         }

      if (willAddDisconnectCommandToQueue)
         {
//         LOG.debug("DefaultFinchController.disconnect(): Now attempting to send the disconnect command to the finch");
         try
            {
            if (commandQueue.executeAndReturnStatus(disconnectHIDCommandStrategy))
               {
//               LOG.debug("DefaultFinchController.disconnect(): Successfully disconnected from the finch.");
               }
            else
               {
//               LOG.error("DefaultFinchController.disconnect(): Failed to disconnect from the finch.");
               }
            }
         catch (HIDDeviceNotConnectedException e)
            {
//            LOG.error("HIDDeviceNotConnectedException while trying to disconnect from the finch", e);
            }
         catch (HIDDeviceFailureException e)
            {
//            LOG.error("HIDDeviceFailureException while trying to disconnect from the finch", e);
            }
         }
      else
         {
//         LOG.debug("DefaultFinchController.disconnect(): Won't try to disconnect from the Finch since willAddDisconnectCommandToQueue was false");
         }

//      LOG.debug("DefaultFinchController.disconnect(): Now shutting down the HIDCommandExecutionQueue...");
      commandQueue.shutdown();
      isDisconnected = true;
      }

   public boolean isDisconnected()
      {
      return isDisconnected;
      }

   /**
    * Returns the state of the accelerometer in g's; returns <code>null</code> if an error occurred while trying to read
    * the state.
    */
   public AccelerometerGs getAccelerometerGs()
      {
      if (accelerometerUnitConversionStrategy != null)
         {
         return accelerometerUnitConversionStrategy.convert(getAccelerometerState());
         }

      return null;
      }

   public Boolean isObstacleDetected(final int id)
      {
      if (id >= 0 && id < FinchConstants.SIMPLE_OBSTACLE_SENSOR_DEVICE_COUNT)
         {
         final boolean[] isDetected = areObstaclesDetected();
         if (isDetected != null && id < isDetected.length)
            {
            return isDetected[id];
            }
         }
      return null;
      }

   public Integer getThermistor()
      {
      return getThermistor(0);
      }

   public Double getThermistorCelsiusTemperature()
      {
      if (thermistorUnitConversionStrategy != null)
         {
         return thermistorUnitConversionStrategy.convertToCelsius(getThermistor(0));
         }

      return null;
      }

   /**
    * Sets the full-color LED to the given {@link Color color}.  Returns the current {@link Color} if the command
    * succeeded, <code>null</code> otherwise.
    */
   public boolean setFullColorLED(final int color)
      {
      return setFullColorLED(Color.red(color),
                             Color.green(color),
                             Color.blue(color));
      }

   private class FinchPinger implements Runnable
      {
      public void run()
         {
         try
            {
//            LOG.trace("FinchProxy$FinchPinger.run()");

            // for pings, we simply get the state of the thermistor
            final HIDCommandResponse response = commandQueue.execute(getThermistorCommandStrategy);
            final boolean pingSuccessful = (response != null && response.wasSuccessful());

            // if the ping failed, then we know we have a problem so disconnect (which
            // probably won't work) and then notify the listeners
            if (!pingSuccessful)
               {
               handlePingFailure();
               }
            }
         catch (Exception e)
            {
//            LOG.error("FinchProxy$FinchPinger.run(): Exception caught while executing the peer pinger", e);
            }
         }

      private void handlePingFailure()
         {
         try
            {
//            LOG.error("FinchProxy$FinchPinger.run(): Peer ping failed (received a null state).  Attempting to disconnect...");
            disconnect(false);
//            LOG.error("FinchProxy$FinchPinger.run(): Done disconnecting from the finch");
            }
         catch (Exception e)
            {
//            LOG.error("FinchProxy$FinchPinger.run(): Exeption caught while trying to disconnect from the finch", e);
            }

//         if (LOG.isDebugEnabled())
//            {
//            LOG.debug("FinchProxy$FinchPinger.run(): Notifying " + createLabDevicePingFailureEventListeners.size() + " listeners of ping failure...");
//            }
         for (final CreateLabDevicePingFailureEventListener listener : createLabDevicePingFailureEventListeners)
            {
            try
               {
//               if (LOG.isDebugEnabled())
//                  {
//                  LOG.debug("   FinchProxy$FinchPinger.run(): Notifying " + listener);
//                  }
               listener.handlePingFailureEvent();
               }
            catch (Exception e)
               {
//               LOG.error("FinchProxy$FinchPinger.run(): Exeption caught while notifying CreateLabDevicePingFailureEventListener", e);
               }
            }
         }

      private void forceFailure()
         {
         handlePingFailure();
         }
      }
   }
