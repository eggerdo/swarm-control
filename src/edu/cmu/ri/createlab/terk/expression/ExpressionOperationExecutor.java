package edu.cmu.ri.createlab.terk.expression;

import edu.cmu.ri.createlab.terk.xml.XmlOperation;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ExpressionOperationExecutor<ReturnType>
   {
   ReturnType executeExpressionOperation(final XmlOperation o);
   }