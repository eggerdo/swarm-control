package edu.cmu.ri.createlab.terk.robot.finch.commands;

import edu.cmu.ri.createlab.terk.robot.finch.FinchConstants;
import edu.cmu.ri.createlab.usb.hid.CreateLabHIDCommandStrategy;
import edu.cmu.ri.createlab.util.ByteUtils;
import edu.cmu.ri.createlab.util.MathUtils;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class FullColorLEDCommandStrategy extends CreateLabHIDCommandStrategy
   {
   /** The command character used to turn on a full-color LED. */
   private static final byte COMMAND_PREFIX = 'O';

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 0;

   private final byte[] command;

   public FullColorLEDCommandStrategy(final int red, final int green, final int blue)
      {
      this.command = new byte[]{COMMAND_PREFIX,
                                ByteUtils.intToUnsignedByte(MathUtils.ensureRange(red, FinchConstants.FULL_COLOR_LED_DEVICE_MIN_INTENSITY, FinchConstants.FULL_COLOR_LED_DEVICE_MAX_INTENSITY)),
                                ByteUtils.intToUnsignedByte(MathUtils.ensureRange(green, FinchConstants.FULL_COLOR_LED_DEVICE_MIN_INTENSITY, FinchConstants.FULL_COLOR_LED_DEVICE_MAX_INTENSITY)),
                                ByteUtils.intToUnsignedByte(MathUtils.ensureRange(blue, FinchConstants.FULL_COLOR_LED_DEVICE_MIN_INTENSITY, FinchConstants.FULL_COLOR_LED_DEVICE_MAX_INTENSITY))};
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