package edu.cmu.ri.createlab.terk.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
//import edu.cmu.ri.createlab.xml.XmlObject;
import org.jdom.Element;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class XmlService extends XmlObject
   {
   public static final String ELEMENT_NAME = "service";
   public static final String ATTR_TYPE_ID = "type-id";

   private final String typeId;
   private final List<XmlOperation> operations = new ArrayList<XmlOperation>();

   public XmlService(final String typeId, final XmlOperation op)
      {
      this.typeId = typeId;
      getElement().setName(ELEMENT_NAME);
      getElement().setAttribute(ATTR_TYPE_ID, typeId);
      addOperation(op);
      }

   public XmlService(final String typeId, final List<XmlOperation> ops)
      {
      this.typeId = typeId;
      getElement().setName(ELEMENT_NAME);
      getElement().setAttribute(ATTR_TYPE_ID, typeId);

      addOperations(ops);
      }

   public XmlService(final Element element)
      {
      super(element);
      this.typeId = element.getAttributeValue(ATTR_TYPE_ID);

      final List opElements = element.getChildren(XmlOperation.ELEMENT_NAME);
      if ((opElements != null) && (!opElements.isEmpty()))
         {
         for (final Object opElementObj : opElements)
            {
            final Element opElement = (Element)opElementObj;
            operations.add(new XmlOperation(opElement));
            }
         }
      }

   public void addOperation(final XmlOperation op)
      {
      if (op != null)
         {
         getElement().addContent(op.toElement());
         operations.add(op);
         }
      else
         {
         throw new NullPointerException("XmlOperation cannot be null!");
         }
      }

   public void addOperations(final List<XmlOperation> ops)
      {
      if (ops != null && !ops.isEmpty())
         {
         for (final XmlOperation op : ops)
            {
            addOperation(op);
            }
         }
      else
         {
         throw new IllegalArgumentException("XmlOperation list cannot be null or empty!");
         }
      }

   public String getTypeId()
      {
      return typeId;
      }

   public List<XmlOperation> getOperations()
      {
      return Collections.unmodifiableList(operations);
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

      final XmlService service = (XmlService)o;

      if (operations.size() != service.operations.size())
         {
         return false;
         }

      final Iterator<XmlOperation> one = operations.iterator();
      final Iterator<XmlOperation> two = service.operations.iterator();

      while (one.hasNext())
         {
         if (!one.next().equals(two.next()))
            {
            return false;
            }
         }

      if (typeId != null ? !typeId.equals(service.typeId) : service.typeId != null)
         {
         return false;
         }

      return true;
      }

   public int hashCode()
      {
      int result = (typeId != null ? typeId.hashCode() : 0);
      result = 31 * result + (operations != null ? operations.hashCode() : 0);
      return result;
      }
   }