package edu.cmu.ri.createlab.terk.properties;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class ReadOnlyPropertyException extends Exception
   {
   public ReadOnlyPropertyException()
      {
      }

   public ReadOnlyPropertyException(final String message)
      {
      super(message);
      }

   public ReadOnlyPropertyException(final String message, final Throwable cause)
      {
      super(message, cause);
      }

   public ReadOnlyPropertyException(final Throwable cause)
      {
      super(cause);
      }
   }
