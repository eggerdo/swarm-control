package edu.cmu.ri.createlab.terk.services.audio;

import edu.cmu.ri.createlab.terk.expression.ExpressionOperationExecutor;
import edu.cmu.ri.createlab.terk.services.DeviceController;
import edu.cmu.ri.createlab.terk.services.ExceptionHandler;
import edu.cmu.ri.createlab.terk.services.Service;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface AudioService extends Service, DeviceController, ExpressionOperationExecutor
   {
   String TYPE_ID = "::TeRK::AudioController";

   String PROPERTY_NAME_MIN_AMPLITUDE = TYPE_ID + "::min-amplitude";
   String PROPERTY_NAME_MAX_AMPLITUDE = TYPE_ID + "::max-amplitude";
   String PROPERTY_NAME_MIN_DURATION = TYPE_ID + "::min-duration";
   String PROPERTY_NAME_MAX_DURATION = TYPE_ID + "::max-duration";
   String PROPERTY_NAME_MIN_FREQUENCY = TYPE_ID + "::min-frequency";
   String PROPERTY_NAME_MAX_FREQUENCY = TYPE_ID + "::max-frequency";

   /**
    * <p>
    * Plays a tone with the given <code>frequency</code> (hz), <code>amplitude</code>, and <code>duration</code> (ms).
    * This method may block until the peer is done playing the tone.
    * </p>
    * @param frequency the frequency of the tone, in hertz
    * @param amplitude the amplitude (volume) of the tone
    * @param duration the duration of the tone, in milliseconds
    */
   void playTone(final int frequency, final int amplitude, final int duration);

   /**
    * Plays a sound represented by the given array of <code>byte</code>s.  For peers that don't enqueue sounds to be
    * played in a separate thread, this method may block until the peer is done playing the sound.
    *
    * @param sound the sound to be executed
    */
   void playSound(final byte[] sound);

   /**
    * <p>
    * Plays a tone asynchronously with the given <code>frequency</code> (hz), <code>amplitude</code>, and
    * <code>duration</code> (ms).  The caller may provide an optional {@link ExceptionHandler} to handle any exceptions
    * that may occur.  If the caller doesn't care about handling exceptions, the given {@link ExceptionHandler} should
    * simply be <code>null</code>.
    * </p>
    * @param frequency the frequency of the tone, in hertz
    * @param amplitude the amplitude (volume) of the tone
    * @param duration the duration of the tone, in milliseconds
    * @param callback the {@link ExceptionHandler} to handle exceptions; may be <code>null</code>
    */
   void playToneAsynchronously(final int frequency, final int amplitude, final int duration, final ExceptionHandler callback);

   /**
    * Plays a sound represented by the given array of <code>byte</code>s.  This method returns immediately.  The caller
    * may provide an optional {@link ExceptionHandler} to handle any exceptions that may occur.  If the caller doesn't
    * care about handling exceptions, the given {@link ExceptionHandler} should simply be <code>null</code>.
    *
    * @param sound the sound to be executed
    * @param callback the {@link ExceptionHandler} to handle exceptions; may be <code>null</code>
    */
   void playSoundAsynchronously(final byte[] sound, final ExceptionHandler callback);

   /**
    * If supported, this method converts the given text into speech, and returns the resulting WAV sound clip as a byte
    * array.  If speech is not not supported, this method simply returns <code>null</code>.
    *
    * @see #isSpeechSupported()
    */
   byte[] getSpeech(final String whatToSay);

   /**
    * If supported, this method converts the given text into audio and plays it.  Does nothing if speech is not
    * supported.
    *
    * @see #isSpeechSupported()
    */
   void speak(final String whatToSay);

   /** Returns whether speech is supported. */
   boolean isSpeechSupported();
   }
