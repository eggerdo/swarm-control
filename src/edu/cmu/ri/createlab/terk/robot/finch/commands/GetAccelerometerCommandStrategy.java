package edu.cmu.ri.createlab.terk.robot.finch.commands;

import edu.cmu.ri.createlab.terk.services.accelerometer.AccelerometerState;
import edu.cmu.ri.createlab.usb.hid.CreateLabHIDReturnValueCommandStrategy;
import edu.cmu.ri.createlab.usb.hid.HIDCommandResponse;
import edu.cmu.ri.createlab.util.ByteUtils;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GetAccelerometerCommandStrategy extends CreateLabHIDReturnValueCommandStrategy<AccelerometerState>
   {
   /** The command character used to request the value of the finch's accelerometer. */
   private static final byte[] COMMAND = {'A'};

   /**
    * The size of the expected response, in bytes.  This is 5 bytes because the first byte is bogus data, the next three
    * are the X, Y, and Z values, and the last byte contains the bits for determining whether it was tapped or shaken.
    */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 5;

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
   public AccelerometerState convertResponse(final HIDCommandResponse result)
      {
      if (result != null && result.wasSuccessful())
         {
         final byte[] responseData = result.getData();
         final byte tapShake = responseData[4];
         final boolean wasTapped = (tapShake & 32) == 32;
         final boolean wasShaken = (tapShake & 128) == 128;

         // NOTE: we're ignoring responseData[0] here on purpose since it's bogus data (see SIZE_IN_BYTES_OF_EXPECTED_RESPONSE above)
         return new AccelerometerState(ByteUtils.unsignedByteToInt(responseData[1]),
                                       ByteUtils.unsignedByteToInt(responseData[2]),
                                       ByteUtils.unsignedByteToInt(responseData[3]),
                                       wasShaken,
                                       wasTapped);
         }

      return null;
      }
   }