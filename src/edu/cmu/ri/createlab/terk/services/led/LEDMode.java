package edu.cmu.ri.createlab.terk.services.led;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public enum LEDMode
   {
      On("On"),
      Off("Off"),
      Blinking("Blinking");

   private final String name;

   private LEDMode(final String name)
      {
      this.name = name;
      }

   public String getName()
      {
      return name;
      }

   public String toString()
      {
      return "LEDMode{" + name + "}";
      }
   }
