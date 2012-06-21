package edu.cmu.ri.createlab.terk.robot.finch.commands;

import edu.cmu.ri.createlab.usb.hid.CreateLabHIDReturnValueCommandStrategy;
import edu.cmu.ri.createlab.usb.hid.HIDCommandResponse;
import edu.cmu.ri.createlab.util.ByteUtils;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GetThermistorCommandStrategy extends CreateLabHIDReturnValueCommandStrategy<Integer>
   {
   /** The command character used to request the value of the finch's thermistor. */
   private static final byte[] COMMAND = {'T'};

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 1;

   @Override
   protected int getSizeOfExpectedResponse()
      {
      return SIZE_IN_BYTES_OF_EXPECTED_RESPONSE;
      }

   @Override
   protected byte[] getCommand()
      {
      return COMMAND.clone();
      }

   @Override
   public Integer convertResponse(final HIDCommandResponse response)
      {
      if (response != null && response.wasSuccessful())
         {
         return ByteUtils.unsignedByteToInt(response.getData()[0]);
         }
      return null;
      }
   }