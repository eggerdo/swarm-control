package edu.cmu.ri.createlab.terk.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import edu.cmu.ri.createlab.xml.XmlObject;
//import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class XmlDevice extends XmlObject
   {
//   private static final Logger LOG = Logger.getLogger(XmlDevice.class);

   public static final String ELEMENT_NAME = "device";
   public static final String ATTR_ID = "id";

   private final int id;
   private final Set<XmlParameter> parameters = new HashSet<XmlParameter>();

   public XmlDevice(final int id)
      {
      getElement().setName(ELEMENT_NAME);
      getElement().setAttribute(ATTR_ID, String.valueOf(id));
      this.id = id;
      }

   public XmlDevice(final int id, final XmlParameter parameter)
      {
      this(id);
      addParameter(parameter);
      }

   public XmlDevice(final int id, final Set<XmlParameter> parameters)
      {
      this(id);
      addParameters(parameters);
      }

   XmlDevice(final Element element)
      {
      super(element);
      final String idStr = element.getAttributeValue(ATTR_ID);
      int idTemp;
      try
         {
         idTemp = Integer.parseInt(idStr);
         }
      catch (NumberFormatException e)
         {
//         LOG.error("NumberFormatException while trying to convert [" + idStr + "] to an integer.  Defaulting to zero.", e);
         idTemp = 0;
         }
      this.id = idTemp;

      final List parameterElements = element.getChildren(XmlParameter.ELEMENT_NAME);
      if ((parameterElements != null) && (!parameterElements.isEmpty()))
         {
         for (final Object parameterElementObj : parameterElements)
            {
            final Element parameterElement = (Element)parameterElementObj;
            parameters.add(new XmlParameter(parameterElement));
            }
         }
      }

   public void addParameter(final XmlParameter parameter)
      {
      if (parameter != null)
         {
         getElement().addContent(parameter.toElement());
         parameters.add(parameter);
         }
      }

   private void addParameters(final Set<XmlParameter> parameters)
      {
      if ((parameters != null) && (!parameters.isEmpty()))
         {
         for (final XmlParameter parameter : parameters)
            {
            if (parameter != null)
               {
               addParameter(parameter);
               }
            }
         }
      }

   public int getId()
      {
      return id;
      }

   /** Returns the parameter having the given name, or <code>null</code> if no such parameter exists. */
   public XmlParameter getParameter(final String parameterName)
      {
      if (parameterName != null)
         {
         final Map<String, XmlParameter> parametersMap = getParametersAsMap();
         if ((parameters != null) && (!parameters.isEmpty()))
            {
            return parametersMap.get(parameterName);
            }
         }

      return null;
      }

   /** Returns an unmodifiable {@link Set} of the parameters. */
   public Set<XmlParameter> getParameters()
      {
      return Collections.unmodifiableSet(parameters);
      }

   /** Returns a {@link Map} of parameter names to {@link XmlParameter}s. */
   public Map<String, XmlParameter> getParametersAsMap()
      {
      final Map<String, XmlParameter> map = new HashMap<String, XmlParameter>();
      for (final XmlParameter parameter : parameters)
         {
         map.put(parameter.getName(), parameter);
         }
      return map;
      }

   /** Returns a {@link Map} of parameter names to values. */
   public Map<String, String> getParametersValuesAsMap()
      {
      final Map<String, String> map = new HashMap<String, String>();
      for (final XmlParameter parameter : parameters)
         {
         map.put(parameter.getName(), parameter.getValue());
         }
      return map;
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

      final XmlDevice device = (XmlDevice)o;

      if (id != device.id)
         {
         return false;
         }
      if (parameters != null ? !parameters.equals(device.parameters) : device.parameters != null)
         {
         return false;
         }

      return true;
      }

   public int hashCode()
      {
      int result = id;
      result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
      return result;
      }
   }