package edu.cmu.ri.createlab.terk.services.motor;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class PositionControllableMotorState
   {
   private final int currentPosition;
   private final int specifiedPosition;
   private final int specifiedSpeed;

   /**
    * Copy constructor.
    */
   public PositionControllableMotorState(final PositionControllableMotorState state)
      {
      this.currentPosition = state.currentPosition;
      this.specifiedPosition = state.specifiedPosition;
      this.specifiedSpeed = Math.abs(state.specifiedSpeed);
      }

   public PositionControllableMotorState(final int currentPosition,
                                         final int specifiedPosition,
                                         final int specifiedSpeed)
      {
      this.currentPosition = currentPosition;
      this.specifiedPosition = specifiedPosition;
      this.specifiedSpeed = Math.abs(specifiedSpeed);
      }

   public int getCurrentPosition()
      {
      return currentPosition;
      }

   public int getSpecifiedPosition()
      {
      return specifiedPosition;
      }

   public int getSpecifiedSpeed()
      {
      return specifiedSpeed;
      }

   public boolean equals(final Object o)
      {
      if (this == o)
         {
         return true;
         }
      if (o == null || getClass() != o.getClass())
         {
         return false;
         }

      final PositionControllableMotorState that = (PositionControllableMotorState)o;

      if (currentPosition != that.currentPosition)
         {
         return false;
         }
      if (specifiedPosition != that.specifiedPosition)
         {
         return false;
         }
      if (specifiedSpeed != that.specifiedSpeed)
         {
         return false;
         }

      return true;
      }

   public int hashCode()
      {
      int result;
      result = currentPosition;
      result = 31 * result + specifiedPosition;
      result = 31 * result + specifiedSpeed;
      return result;
      }

   public String toString()
      {
      return "PositionControllableMotorState{" +
             "currentPosition=" + currentPosition +
             ", specifiedPosition=" + specifiedPosition +
             ", specifiedSpeed=" + specifiedSpeed +
             '}';
      }
   }
