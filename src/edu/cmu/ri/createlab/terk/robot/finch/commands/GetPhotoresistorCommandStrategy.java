package edu.cmu.ri.createlab.terk.robot.finch.commands;

import edu.cmu.ri.createlab.usb.hid.CreateLabHIDReturnValueCommandStrategy;
import edu.cmu.ri.createlab.usb.hid.HIDCommandResponse;
import edu.cmu.ri.createlab.util.ByteUtils;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GetPhotoresistorCommandStrategy extends CreateLabHIDReturnValueCommandStrategy<int[]>
   {
   /** The command character used to request the value of the finch's photoresistors. */
   private static final byte[] COMMAND = {'L'};

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 2;

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
   public int[] convertResponse(final HIDCommandResponse response)
      {
      if (response != null && response.wasSuccessful())
         {
         final byte[] responseData = response.getData();
         return new int[]{ByteUtils.unsignedByteToInt(responseData[0]),
                          ByteUtils.unsignedByteToInt(responseData[1])};
         }

      return null;
      }
   }