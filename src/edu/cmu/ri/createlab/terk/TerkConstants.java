package edu.cmu.ri.createlab.terk;

import java.io.File;

/**
 * <p>
 * <code>TerkConstants</code> defines various constants of the TeRK architecture.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class TerkConstants
   {
   @Deprecated
   public static final class FilePaths
      {
      public static final String TERK_PATH = System.getProperty("user.home") + File.separator + "TeRK" + File.separator;
      public static final File AUDIO_DIR = new File(TERK_PATH, "Audio");
      public static final File EXPRESSIONS_DIR = new File(TERK_PATH, "Expressions");
      public static final File SEQUENCES_DIR = new File(TERK_PATH, "Sequences");
      public static final File CONDITIONS_DIR = new File(TERK_PATH, "Conditions");
      public static final File EXPRESSIONS_ICONS_DIR = new File(EXPRESSIONS_DIR, "Icons");
      public static final File CONDITIONS_ICONS_DIR = new File(CONDITIONS_DIR, "Icons");
      public static final File SEQUENCES_ICONS_DIR = new File(SEQUENCES_DIR, "Icons");
      public static final File EXPRESSIONS_PUBLIC_DIR = new File(EXPRESSIONS_DIR, "Public");
      public static final File SEQUENCES_PUBLIC_DIR = new File(SEQUENCES_DIR, "Public");

      private FilePaths()
         {
         // private to prevent instantiation
         }
      }

   public static final class PropertyKeys
      {
      public static final String DEVICE_COUNT = "device.count";
      public static final String HARDWARE_TYPE = "hardware.type";
      public static final String HARDWARE_VERSION = "hardware.version";
      public static final String HARDWARE_VERSION_MAJOR = "hardware.version.major";
      public static final String HARDWARE_VERSION_MINOR = "hardware.version.minor";
      public static final String HARDWARE_VERSION_REVISION = "hardware.version.revision";
      public static final String FIRMWARE_VERSION = "firmware.version";
      public static final String FIRMWARE_VERSION_MAJOR = "firmware.version.major";
      public static final String FIRMWARE_VERSION_MINOR = "firmware.version.minor";
      public static final String FIRMWARE_VERSION_REVISION = "firmware.version.revision";

      private PropertyKeys()
         {
         // private to prevent instantiation
         }
      }

   private TerkConstants()
      {
      // private to prevent instantiation
      }
   }
