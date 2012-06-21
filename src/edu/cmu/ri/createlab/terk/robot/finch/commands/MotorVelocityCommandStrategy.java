package edu.cmu.ri.createlab.terk.robot.finch.commands;

import edu.cmu.ri.createlab.terk.robot.finch.FinchConstants;
import edu.cmu.ri.createlab.usb.hid.CreateLabHIDCommandStrategy;
import edu.cmu.ri.createlab.util.MathUtils;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class MotorVelocityCommandStrategy extends CreateLabHIDCommandStrategy
   {
   /** The command character used to set the motor velocities. */
   private static final byte COMMAND_PREFIX = 'M';

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 0;

   private final byte[] command;

   public MotorVelocityCommandStrategy(final int leftVelocity, final int rightVelocity)
      {
      final int cleanedLeftVelocity = MathUtils.ensureRange(leftVelocity, FinchConstants.MOTOR_DEVICE_MIN_VELOCITY, FinchConstants.MOTOR_DEVICE_MAX_VELOCITY);
      final int cleanedRightVelocity = MathUtils.ensureRange(rightVelocity, FinchConstants.MOTOR_DEVICE_MIN_VELOCITY, FinchConstants.MOTOR_DEVICE_MAX_VELOCITY);
      final byte leftDirection = (byte)((cleanedLeftVelocity < 0) ? 1 : 0);
      final byte leftSpeed = (byte)Math.abs(cleanedLeftVelocity);
      final byte rightDirection = (byte)((cleanedRightVelocity < 0) ? 1 : 0);
      final byte rightSpeed = (byte)Math.abs(cleanedRightVelocity);
      this.command = new byte[]{COMMAND_PREFIX,
                                leftDirection,
                                leftSpeed,
                                rightDirection,
                                rightSpeed};
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