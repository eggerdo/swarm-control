package edu.cmu.ri.createlab.terk.robot.finch.services;

import java.io.File;
import edu.cmu.ri.createlab.CreateLabConstants;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class DefaultFinchServiceFactoryHelper implements FinchServiceFactoryHelper
   {
   private static final DefaultFinchServiceFactoryHelper INSTANCE = new DefaultFinchServiceFactoryHelper();

   public static DefaultFinchServiceFactoryHelper getInstance()
      {
      return INSTANCE;
      }

   private DefaultFinchServiceFactoryHelper()
      {
      // private to prevent instantiation
      }

   @Override
   public File getAudioDirectory()
      {
      return CreateLabConstants.FilePaths.AUDIO_DIR;
      }
   }
