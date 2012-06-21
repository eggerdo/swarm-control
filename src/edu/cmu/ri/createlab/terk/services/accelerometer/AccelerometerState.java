package edu.cmu.ri.createlab.terk.services.accelerometer;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class AccelerometerState
   {
   private final int x;
   private final int y;
   private final int z;
   private final boolean wasShaken;
   private final boolean wasTapped;

   public AccelerometerState(final int x, final int y, final int z)
      {
      this(x, y, z, false, false);
      }

   public AccelerometerState(final int x, final int y, final int z, final boolean wasShaken, final boolean wasTapped)
      {
      this.x = x;
      this.y = y;
      this.z = z;
      this.wasShaken = wasShaken;
      this.wasTapped = wasTapped;
      }

   public int getX()
      {
      return x;
      }

   public int getY()
      {
      return y;
      }

   public int getZ()
      {
      return z;
      }

   public boolean wasShaken()
      {
      return wasShaken;
      }

   public boolean wasTapped()
      {
      return wasTapped;
      }

   @Override
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

      final AccelerometerState that = (AccelerometerState)o;

      if (wasShaken != that.wasShaken)
         {
         return false;
         }
      if (wasTapped != that.wasTapped)
         {
         return false;
         }
      if (x != that.x)
         {
         return false;
         }
      if (y != that.y)
         {
         return false;
         }
      if (z != that.z)
         {
         return false;
         }

      return true;
      }

   @Override
   public int hashCode()
      {
      int result = x;
      result = 31 * result + y;
      result = 31 * result + z;
      result = 31 * result + (wasShaken ? 1 : 0);
      result = 31 * result + (wasTapped ? 1 : 0);
      return result;
      }

   @Override
   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("AccelerometerState");
      sb.append("{x=").append(x);
      sb.append(", y=").append(y);
      sb.append(", z=").append(z);
      sb.append(", wasShaken=").append(wasShaken);
      sb.append(", wasTapped=").append(wasTapped);
      sb.append('}');
      return sb.toString();
      }
   }