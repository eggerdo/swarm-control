package edu.cmu.ri.createlab.terk.services;

/**
 * <p>
 * <code>ExceptionHandler</code> is a class which handles an exception.  It's especially useful as a way for callers of
 * asynchronous commands to handle exceptions.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"NoopMethodInAbstractClass"})
public abstract class ExceptionHandler
   {
   /** Method for handling {@link Exception}s.  Does nothing by default. */
   public void handleException(final Exception exception)
      {
      // do nothing
      }
   }
