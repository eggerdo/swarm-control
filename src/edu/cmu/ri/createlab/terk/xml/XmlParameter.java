package edu.cmu.ri.createlab.terk.xml;

//import edu.cmu.ri.createlab.xml.XmlObject;
//import org.apache.log4j.Logger;
import org.jdom.CDATA;
import org.jdom.Content;
import org.jdom.Element;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class XmlParameter extends XmlObject
   {
//   private static final Logger LOG = Logger.getLogger(XmlParameter.class);

   public static final String ELEMENT_NAME = "parameter";
   public static final String ATTR_NAME = "name";

   private final String name;
   private final String value;

   public XmlParameter(final String name, final Object value)
      {
      this(name, String.valueOf(value));
      }

   public XmlParameter(final String name, final String value)
      {
      this.name = name;
      this.value = value;
      getElement().setName(ELEMENT_NAME);
      getElement().setAttribute(ATTR_NAME, name);
      getElement().setContent(new CDATA(value));
      }

   XmlParameter(final Element element)
      {
      super(element);
      this.name = element.getAttributeValue(ATTR_NAME);
      final Content content = element.getContent(0);
      this.value = content == null ? null : content.getValue();
      }

   public String getName()
      {
      return name;
      }

   public String getValue()
      {
      return value;
      }

   /**
    * Returns the value as an Integer, if it can be converted.  If conversion fails, this method returns
    * <code>null</code>.
    */
   public Integer getValueAsInteger()
      {
      try
         {
         return Integer.parseInt(value);
         }
      catch (NumberFormatException e)
         {
//         LOG.trace("NumberFormatException while trying to convert [" + value + "] to an integer.  Returning null.", e);
         }
      return null;
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

      final XmlParameter parameter = (XmlParameter)o;

      if (name != null ? !name.equals(parameter.name) : parameter.name != null)
         {
         return false;
         }
      if (value != null ? !value.equals(parameter.value) : parameter.value != null)
         {
         return false;
         }

      return true;
      }

   public int hashCode()
      {
      int result = (name != null ? name.hashCode() : 0);
      result = 31 * result + (value != null ? value.hashCode() : 0);
      return result;
      }
   }
