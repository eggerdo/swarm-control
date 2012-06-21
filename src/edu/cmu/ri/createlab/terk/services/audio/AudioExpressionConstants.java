package edu.cmu.ri.createlab.terk.services.audio;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * <code>AudioExpressionConstants</code> defines various constans for audio expressions.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"PublicStaticCollectionField"})
public final class AudioExpressionConstants
   {
   public static final String OPERATION_NAME_PLAY_TONE = "playTone";
   public static final String OPERATION_NAME_PLAY_TONE_ASYNCHRONOUSLY = "playTone";
   public static final String PARAMETER_NAME_TONE_FREQUENCY = "frequency";
   public static final String PARAMETER_NAME_TONE_AMPLITUDE = "amplitude";
   public static final String PARAMETER_NAME_TONE_DURATION = "duration";

   public static final String OPERATION_NAME_PLAY_CLIP = "playClip";
   public static final String OPERATION_NAME_PLAY_CLIP_ASYNCHRONOUSLY = "playClipAsynchronously";
   public static final String PARAMETER_NAME_CLIP_FILE = "file";

   public static final String OPERATION_NAME_SPEAK = "speak";
   public static final String PARAMETER_NAME_SPEAK_TEXT = "text";

   public static final Set<String> PLAY_TONE_PARAMETER_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(PARAMETER_NAME_TONE_FREQUENCY, PARAMETER_NAME_TONE_AMPLITUDE, PARAMETER_NAME_TONE_DURATION)));
   public static final Set<String> PLAY_CLIP_PARAMETER_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(PARAMETER_NAME_CLIP_FILE)));
   public static final Set<String> SPEAK_PARAMETER_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(PARAMETER_NAME_SPEAK_TEXT)));
   public static final Map<String, Set<String>> OPERATIONS_TO_PARAMETERS_MAP;

   static
      {
      final Map<String, Set<String>> operationsToParametersMap = new HashMap<String, Set<String>>();
      operationsToParametersMap.put(OPERATION_NAME_PLAY_TONE, PLAY_TONE_PARAMETER_NAMES);
      operationsToParametersMap.put(OPERATION_NAME_PLAY_TONE_ASYNCHRONOUSLY, PLAY_TONE_PARAMETER_NAMES);
      operationsToParametersMap.put(OPERATION_NAME_PLAY_CLIP, PLAY_CLIP_PARAMETER_NAMES);
      operationsToParametersMap.put(OPERATION_NAME_PLAY_CLIP_ASYNCHRONOUSLY, PLAY_CLIP_PARAMETER_NAMES);
      operationsToParametersMap.put(OPERATION_NAME_SPEAK, SPEAK_PARAMETER_NAMES);
      OPERATIONS_TO_PARAMETERS_MAP = Collections.unmodifiableMap(operationsToParametersMap);
      }

   private AudioExpressionConstants()
      {
      // private to prevent instantiation
      }
   }
