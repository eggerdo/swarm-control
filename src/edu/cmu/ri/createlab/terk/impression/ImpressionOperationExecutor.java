package edu.cmu.ri.createlab.terk.impression;

import edu.cmu.ri.createlab.terk.xml.XmlOperation;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ImpressionOperationExecutor<ReturnType>
   {
   ReturnType executeImpressionOperation(final XmlOperation o);
   }