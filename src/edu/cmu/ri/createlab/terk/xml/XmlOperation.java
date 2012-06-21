package edu.cmu.ri.createlab.terk.xml;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
//import edu.cmu.ri.createlab.xml.XmlObject;
import org.jdom.Element;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class XmlOperation extends XmlObject
   {
   public static final String ELEMENT_NAME = "operation";
   public static final String ATTR_NAME = "name";

   private final String name;
   private final Set<XmlDevice> devices = new HashSet<XmlDevice>();

   // this is private since, according to the DTD, operations must have one or more devices

   private XmlOperation(final String name)
      {
      this.name = name;
      getElement().setName(ELEMENT_NAME);
      getElement().setAttribute(ATTR_NAME, name);
      }

   public XmlOperation(final String name, final XmlDevice device)
      {
      this(name);
      if (!addDevice(device))
         {
         throw new IllegalArgumentException("<operation>s must have at least one <device>");
         }
      }

   public XmlOperation(final String name, final Set<XmlDevice> devices)
      {
      this(name);
      if (addDevices(devices) <= 0)
         {
         throw new IllegalArgumentException("<operation>s must have at least one <device>");
         }
      }

   XmlOperation(final Element element)
      {
      super(element);
      this.name = element.getAttributeValue(ATTR_NAME);
      final List deviceElements = element.getChildren(XmlDevice.ELEMENT_NAME);
      if ((deviceElements != null) && (!deviceElements.isEmpty()))
         {
         for (final Object deviceElementObj : deviceElements)
            {
            final Element deviceElement = (Element)deviceElementObj;
            devices.add(new XmlDevice(deviceElement));
            }
         }
      }

   /**
    * Adds the given {@link XmlDevice}.  Returns <code>true</code> if the device was added, <code>false</code> otherwise
    */
   public boolean addDevice(final XmlDevice device)
      {
      if (device != null)
         {
         getElement().addContent(device.toElement());
         devices.add(device);
         return true;
         }
      return false;
      }

   private int addDevices(final Set<XmlDevice> devices)
      {
      int numDevicesAdded = 0;
      if ((devices != null) && (!devices.isEmpty()))
         {
         for (final XmlDevice device : devices)
            {
            if (addDevice(device))
               {
               numDevicesAdded++;
               }
            }
         }
      return numDevicesAdded;
      }

   public String getName()
      {
      return name;
      }

   /** Returns an unmodifiable {@link Set} of the devices. */
   public Set<XmlDevice> getDevices()
      {
      return Collections.unmodifiableSet(devices);
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

      final XmlOperation operation = (XmlOperation)o;

      if (devices != null ? !devices.equals(operation.devices) : operation.devices != null)
         {
         return false;
         }
      if (name != null ? !name.equals(operation.name) : operation.name != null)
         {
         return false;
         }

      return true;
      }

   public int hashCode()
      {
      int result = (name != null ? name.hashCode() : 0);
      result = 31 * result + (devices != null ? devices.hashCode() : 0);
      return result;
      }
   }