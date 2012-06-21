package edu.cmu.ri.createlab.terk.robot.finch.commands;

import edu.cmu.ri.createlab.terk.robot.finch.FinchConstants;
import edu.cmu.ri.createlab.usb.hid.CreateLabHIDCommandStrategy;
import edu.cmu.ri.createlab.util.MathUtils;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class BuzzerCommandStrategy extends CreateLabHIDCommandStrategy
   {
   /** The command character used to set the buzzer frequency. */
   private static final byte COMMAND_PREFIX = 'B';

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 0;

   private final byte[] command;

   public BuzzerCommandStrategy(final int frequency, final int durationInMilliseconds)
      {
      final int cleanedFrequency = MathUtils.ensureRange(frequency, FinchConstants.BUZZER_DEVICE_MIN_FREQUENCY, FinchConstants.BUZZER_DEVICE_MAX_FREQUENCY);
      final int cleanedDurationInMilliseconds = MathUtils.ensureRange(durationInMilliseconds, FinchConstants.BUZZER_DEVICE_MIN_DURATION, FinchConstants.BUZZER_DEVICE_MAX_DURATION);
      this.command = new byte[]{COMMAND_PREFIX,
                                getHighByteFromInt(cleanedDurationInMilliseconds),
                                getLowByteFromInt(cleanedDurationInMilliseconds),
                                getHighByteFromInt(cleanedFrequency),
                                getLowByteFromInt(cleanedFrequency)};
      }

   private byte getHighByteFromInt(final int val)
      {
      return (byte)((val << 16) >> 24);
      }

   private byte getLowByteFromInt(final int val)
      {
      return (byte)((val << 24) >> 24);
      }

   @Override
   protected int getSizeOfExpectedResponse()
      {
      return SIZE_IN_BYTES_OF_EXPECTED_RESPONSE;
      }

   @Override
   protected byte[] getCommand()
      {
      return command.clone();
      }
   }