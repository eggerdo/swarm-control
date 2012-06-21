package edu.cmu.ri.createlab.terk.expression;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.ri.createlab.terk.xml.XmlHelper;
import edu.cmu.ri.createlab.terk.xml.XmlObject;
import edu.cmu.ri.createlab.terk.xml.XmlService;
//import edu.cmu.ri.createlab.xml.XmlHelper;
//import edu.cmu.ri.createlab.xml.XmlObject;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class XmlExpression extends XmlObject
   {
   public static final String ELEMENT_NAME = "expression";
   public static final String ATTR_VERSION = "version";

   public static final String SERVICES_ELEMENT_NAME = "services";

   public static final String DOCTYPE_PUBLIC_ID = "-//CREATE Lab//TeRK//Expression//EN";
   public static final String DOCTYPE_SYSTEM_ID = "http://www.createlab.ri.cmu.edu/dtd/terk/expression.dtd";
   private static final DocType DOC_TYPE = new DocType(ELEMENT_NAME, DOCTYPE_PUBLIC_ID, DOCTYPE_SYSTEM_ID);

   private static final String DEFAULT_VERSION = "1.0";

   private String name;

   public static XmlExpression create(final String xml) throws IOException, JDOMException
      {
      final Document document = XmlHelper.createDocument(xml);
      document.setDocType((DocType)DOC_TYPE.clone());
      final Element element = XmlHelper.createElement(XmlHelper.writeDocumentToString(document));
      return new XmlExpression(element);
      }

   public static XmlExpression create(final InputStream inputStream) throws IOException, JDOMException
      {
      return new XmlExpression(XmlHelper.createElement(inputStream));
      }

   public static XmlExpression create(final File file) throws IOException, JDOMException
      {
      return create(new FileInputStream(file));
      }

   /** Creates an <code>Expression</code> having the given {@link XmlService}. */
   public static XmlExpression create(final XmlService service)
      {
      final Set<XmlService> services = new HashSet<XmlService>(1);
      services.add(service);
      return new XmlExpression(services);
      }

   /** Creates an <code>Expression</code> having the given {@link XmlService}s. */
   public static XmlExpression create(final Set<XmlService> services)
      {
      return new XmlExpression(services);
      }

   private final Set<XmlService> services = new HashSet<XmlService>();

   private XmlExpression(final Set<XmlService> services)
      {
      getElement().setName(ELEMENT_NAME);
      getElement().setAttribute(ATTR_VERSION, DEFAULT_VERSION);
      getElement().setContent(createServicesElement());
      addServices(services);
      }

   private XmlExpression(final Element element)
      {
      super(element);
      final List serviceElements = getServicesElement().getChildren(XmlService.ELEMENT_NAME);
      if ((serviceElements != null) && (!serviceElements.isEmpty()))
         {
         for (final Object serviceElementObj : serviceElements)
            {
            final Element serviceElement = (Element)serviceElementObj;
            final XmlService service = new XmlService(serviceElement);
            services.add(service);
            }
         }
      }

   public String toXmlDocumentStringFormatted()
      {
      Document document = getElement().getDocument();
      if (document == null)
         {
         document = new Document(getElement(), (DocType)DOC_TYPE.clone());
         }
      return XmlHelper.writeDocumentToStringFormatted(document);
      }

   public void addService(final XmlService service)
      {
      if (service != null)
         {
         getServicesElement().addContent(service.toElement());
         services.add(service);
         }
      }

   private void addServices(final Set<XmlService> services)
      {
      if ((services != null) && (!services.isEmpty()))
         {
         for (final XmlService service : services)
            {
            if (service != null)
               {
               addService(service);
               }
            }
         }
      }

   /** Returns an unmodifiable {@link Set} of the services. */
   public Set<XmlService> getServices()
      {
      return Collections.unmodifiableSet(services);
      }

   /**
    * Returns the &lt;services&gt; element, creating it first, if necessary.
    */
   private Element getServicesElement()
      {
      Element servicesElement = getElement().getChild(SERVICES_ELEMENT_NAME);
      if (servicesElement == null)
         {
         servicesElement = createServicesElement();
         getElement().addContent(servicesElement);
         }

      return servicesElement;
      }

   private Element createServicesElement()
      {
      return new Element(SERVICES_ELEMENT_NAME);
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

      final XmlExpression that = (XmlExpression)o;

      if (services != null ? !services.equals(that.services) : that.services != null)
         {
         return false;
         }

      return true;
      }

   public int hashCode()
      {
      return (services != null ? services.hashCode() : 0);
      }

   public String getName()
      {
      return this.name;
      }

   public void setName(final String name)
      {
      this.name = name;
      }
   }
