package edu.cmu.ri.createlab.terk.robot.finch.services;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import edu.cmu.ri.createlab.terk.TerkConstants;
import edu.cmu.ri.createlab.terk.properties.BasicPropertyManager;
import edu.cmu.ri.createlab.terk.properties.PropertyManager;
import edu.cmu.ri.createlab.terk.robot.finch.FinchConstants;
import edu.cmu.ri.createlab.terk.robot.finch.FinchController;
import edu.cmu.ri.createlab.terk.services.ExceptionHandler;
import edu.cmu.ri.createlab.terk.services.audio.AudioService;
import edu.cmu.ri.createlab.terk.services.audio.BaseAudioServiceImpl;
//import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class AudioServiceImpl extends BaseAudioServiceImpl
   {
//   private static final Logger LOG = Logger.getLogger(AudioServiceImpl.class);

   static AudioServiceImpl create(final FinchController finchController, final File audioDirectory)
      {
      final BasicPropertyManager basicPropertyManager = new BasicPropertyManager();

      basicPropertyManager.setReadOnlyProperty(TerkConstants.PropertyKeys.DEVICE_COUNT, FinchConstants.AUDIO_DEVICE_COUNT);
      basicPropertyManager.setReadOnlyProperty(AudioService.PROPERTY_NAME_MIN_AMPLITUDE, FinchConstants.AUDIO_DEVICE_MIN_AMPLITUDE);
      basicPropertyManager.setReadOnlyProperty(AudioService.PROPERTY_NAME_MAX_AMPLITUDE, FinchConstants.AUDIO_DEVICE_MAX_AMPLITUDE);
      basicPropertyManager.setReadOnlyProperty(AudioService.PROPERTY_NAME_MIN_DURATION, FinchConstants.AUDIO_DEVICE_MIN_DURATION);
      basicPropertyManager.setReadOnlyProperty(AudioService.PROPERTY_NAME_MAX_DURATION, FinchConstants.AUDIO_DEVICE_MAX_DURATION);
      basicPropertyManager.setReadOnlyProperty(AudioService.PROPERTY_NAME_MIN_FREQUENCY, FinchConstants.AUDIO_DEVICE_MIN_FREQUENCY);
      basicPropertyManager.setReadOnlyProperty(AudioService.PROPERTY_NAME_MAX_FREQUENCY, FinchConstants.AUDIO_DEVICE_MAX_FREQUENCY);

      return new AudioServiceImpl(finchController,
                                  basicPropertyManager,
                                  audioDirectory);
      }

   private final FinchController finchController;
   private final Executor executor = Executors.newCachedThreadPool();

   private AudioServiceImpl(final FinchController finchController,
                            final PropertyManager propertyManager,
                            final File audioDirectory)
      {
      super(propertyManager, audioDirectory);
      this.finchController = finchController;
      }

   public void playTone(final int frequency, final int amplitude, final int duration)
      {
      finchController.playTone(frequency, amplitude, duration);
      }

   public void playSound(final byte[] sound)
      {
      finchController.playClip(sound);
      }

   public void playToneAsynchronously(final int frequency, final int amplitude, final int duration, final ExceptionHandler callback)
      {
      try
         {
         executor.execute(
               new Runnable()
               {
               public void run()
                  {
                  finchController.playTone(frequency, amplitude, duration);
                  }
               });
         }
      catch (Exception e)
         {
//         LOG.error("Exception while trying to play the tone asynchronously", e);
         callback.handleException(e);
         }
      }

   public void playSoundAsynchronously(final byte[] sound, final ExceptionHandler callback)
      {
      try
         {
         executor.execute(
               new Runnable()
               {
               public void run()
                  {
                  finchController.playClip(sound);
                  }
               });
         }
      catch (Exception e)
         {
//         LOG.error("Exception while trying to play the clip asynchronously", e);
         callback.handleException(e);
         }
      }

   @Override
   public byte[] getSpeech(final String whatToSay)
      {
      return finchController.getSpeech(whatToSay);
      }

   @Override
   public void speak(final String whatToSay)
      {
      finchController.speak(whatToSay);
      }

   @Override
   public boolean isSpeechSupported()
      {
      return true;
      }
   }