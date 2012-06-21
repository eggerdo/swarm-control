package edu.cmu.ri.createlab.terk.xml;

import org.jdom.Element;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface XmlSerializable
   {
   String toXmlString();

   String toXmlStringFormatted();

   Element toElement();
   }