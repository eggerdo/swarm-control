package edu.cmu.ri.createlab.terk.services.accelerometer;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class AccelerometerGs
   {
   private final double x;
   private final double y;
   private final double z;

   public AccelerometerGs(final double x, final double y, final double z)
      {
      this.x = x;
      this.y = y;
      this.z = z;
      }

   public double getX()
      {
      return x;
      }

   public double getY()
      {
      return y;
      }

   public double getZ()
      {
      return z;
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

      final AccelerometerGs that = (AccelerometerGs)o;

      if (Double.compare(that.x, x) != 0)
         {
         return false;
         }
      if (Double.compare(that.y, y) != 0)
         {
         return false;
         }
      if (Double.compare(that.z, z) != 0)
         {
         return false;
         }

      return true;
      }

   public int hashCode()
      {
      int result;
      long temp;
      temp = x != +0.0d ? Double.doubleToLongBits(x) : 0L;
      result = (int)(temp ^ (temp >>> 32));
      temp = y != +0.0d ? Double.doubleToLongBits(y) : 0L;
      result = 31 * result + (int)(temp ^ (temp >>> 32));
      temp = z != +0.0d ? Double.doubleToLongBits(z) : 0L;
      result = 31 * result + (int)(temp ^ (temp >>> 32));
      return result;
      }

   public String toString()
      {
      return "AccelerometerGs{" +
             "x=" + x +
             ", y=" + y +
             ", z=" + z +
             '}';
      }
   }