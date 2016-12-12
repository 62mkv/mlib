/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.server.expression;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * Test for evaluating IF expressions in MOCA.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_IfExpressions extends AbstractMocaTestCase {

    public void testStringLiteral() throws MocaException {
        _runIfTest("'x'", true);
    }

    public void testEmptyString() throws MocaException {
        _runIfTest("''", false);
    }

    public void testNullLiteral() throws MocaException {
        _runIfTest("''", false);
    }

    public void testIntegerLiteral() throws MocaException {
        _runIfTest("1", true);
        _runIfTest("0", false);
    }

    public void testBooleanLiteral() throws MocaException {
        _runIfTest("true", true);
        _runIfTest("false", false);
    }
    
    public void testFloatLiteral() throws MocaException {
        _runIfTest("0.4", true);
        _runIfTest("0.0", false);
    }
    
    public void testVariableReference() throws MocaException {
        _runIfTest("@novar", false);
        _runIfTest("@c1", true);
        _runIfTest("@c2", false);
        _runIfTest("@n1", true);
        _runIfTest("@n2", false);
        _runIfTest("@f1", true);
        _runIfTest("@f2", false);
        _runIfTest("@b1", true);
        _runIfTest("@b2", false);
        _runIfTest("@nn", false);
    }
    
    public void testNotVariableReference() throws MocaException {
        _runIfTest("!@novar", true);
        _runIfTest("!@c1", false);
        _runIfTest("!@c2", true);
        _runIfTest("!@n1", false);
        _runIfTest("!@n2", true);
        _runIfTest("!@f1", false);
        _runIfTest("!@f2", true);
        _runIfTest("!@b1", false);
        _runIfTest("!@b2", true);
        _runIfTest("!@nn", true);
    }
    
    public void testAndExpression() throws MocaException {
        _runIfTest("@c1 and @n1", true);
        _runIfTest("!@c2 and @n1", true);
    }
    
    public void testAndExpressionNoSideEffects() throws MocaException {
        // Since the first part evaluated as true, the script will be executed
        _runIfTest("!@c2 AND [[moca.setTransactionAttribute('foo', 'xxx'); true]]", true);
        assertEquals("xxx", _moca.getTransactionAttribute("foo"));
        
        // The first part of the expression is false, so the script should not run.
        _runIfTest("@c2 AND [[moca.setTransactionAttribute('bar', 'xxx'); true]]", false);
        assertNull(_moca.getTransactionAttribute("bar"));
    }
    
    public void testOrExpression() throws MocaException {
        _runIfTest("@c2 or @n1", true);
        _runIfTest("!@c2 or @n2", true);
    }
    
    public void testOrExpressionNoSideEffects() throws MocaException {
        // Since the first part evaluates as false, the script will be executed
        _runIfTest("@c2 OR [[moca.setTransactionAttribute('foo', 'zzz'); true]]", true);
        assertEquals("zzz", _moca.getTransactionAttribute("foo"));
        
        // Now run the script only if the first part of the expression is false
        _runIfTest("@c1 OR [[moca.setTransactionAttribute('bar', 'zzz'); true]]", true);
        assertNull(_moca.getTransactionAttribute("bar"));
    }
    
    public void testEqualsExpression() throws MocaException {
        // Same data types and same values.
        _runIfTest("@b1 = @b1", true);
        _runIfTest("@c1 = @c1", true);
        _runIfTest("@n1 = @n1", true);
        _runIfTest("@f1 = @f1", true);
        
        // Same data types and different values.
        _runIfTest("@b1 = @b2", false);
        _runIfTest("@c1 = @c2", false);
        _runIfTest("@n1 = @n2", false);
        _runIfTest("@f1 = @f2", false);
        
        // Different data types and same values.
        _runIfTest("@fc1 = @f1", true);
        _runIfTest("@f1 = @fc1", true);
        
        // Different data types and different values.
        _runIfTest("@nc1 = @nc2", false);
        _runIfTest("@fc1 = @fc2", false);
        
        // NULL on right side tested against NULL values. 
        _runIfTest("@c2 = @nn", true);      
        _runIfTest("@n2 = @nn", false);
        _runIfTest("@f2 = @nn", false);
    
        // NULL on right side tested against non-NULL values.
        _runIfTest("@c1 = @nn", false);
        _runIfTest("@n1 = @nn", false);
        _runIfTest("@f1 = @nn", false);
        
        // NULL on left side tested against NULL values. 
        _runIfTest("@nn = @c2", true);
        _runIfTest("@nn = @n2", false);
        _runIfTest("@nn = @f2", false);
    
        // NULL on left side tested against non-NULL values.
        _runIfTest("@nn = @c1", false);
        _runIfTest("@nn = @n1", false);
        _runIfTest("@nn = @f1", false);
        
        // NULL on left side tested against zero-equivalent string values.
        _runIfTest("@nn = @nc2", false);
        _runIfTest("@nn = @fc2", false);

        // Check literal string against literal numeric
        _runIfTest("'' = 2", false);
        _runIfTest("'2' = 2", true);
        _runIfTest("2 = '2'", true);
        _runIfTest("2.0 = '2'", true);
        _runIfTest("'2' = 2.0", true);

        _runIfTest("2.1 = 2", false);
        _runIfTest("2 = 2.1", false);
        _runIfTest("2 = 2.0", true);
        _runIfTest("2.0 = 2", true);

        // Check literal strings against booleans
        _runIfTest("true = '0'", false);
        _runIfTest("'0' = true", false);
        _runIfTest("true = '1'", true);
        
        _runIfTest("false = '0'", true);
        _runIfTest("'0' = false", true);
        _runIfTest("false = '1'", false);

        // NULL on both sides.
        _runIfTest("@nn = @nn", true);
        
        // Empty Strings compared to nulls
        _runIfTest("@nn = ''", true);
        _runIfTest("@c2 = ''", true);
        _runIfTest("@nn = null", true);
        _runIfTest("@c2 = null", true);

        // Double conversion to strings
        _runIfTest("string(12345678901234) = '12345678901234'", true);
        _runIfTest("string(12345678901234.123) = '12345678901234.123'", true);
    }
    
    public void testNotEqualsExpression() throws MocaException {
        // Same data types and same values.
        _runIfTest("@b1 != @b1", false);
        _runIfTest("@c1 != @c1", false);
        _runIfTest("@n1 != @n1", false);
        _runIfTest("@f1 != @f1", false);
        
        // Same data types and different values.
        _runIfTest("@b1 != @b2", true);
        _runIfTest("@c1 != @c2", true);
        _runIfTest("@n1 != @n2", true);
        _runIfTest("@f1 != @f2", true);
        
        // Different data types and same values.
        _runIfTest("@fc1 != @f1", false);
        _runIfTest("@f1 != @fc1", false);
        
        // Different data types and different values.
        _runIfTest("@nc1 != @nc2", true);
        _runIfTest("@fc1 != @fc2", true);
        
        // NULL on right side tested against NULL values. 
        _runIfTest("@c2 != @nn", false);      
        _runIfTest("@n2 != @nn", true);
        _runIfTest("@f2 != @nn", true);
    
        // NULL on right side tested against non-NULL values.
        _runIfTest("@c1 != @nn", true);
        _runIfTest("@n1 != @nn", true);
        _runIfTest("@f1 != @nn", true);
        
        // NULL on left side tested against NULL values. 
        _runIfTest("@nn != @c2", false);
        _runIfTest("@nn != @n2", true);
        _runIfTest("@nn != @f2", true);
    
        // NULL on left side tested against non-NULL values.
        _runIfTest("@nn != @c1", true);
        _runIfTest("@nn != @n1", true);
        _runIfTest("@nn != @f1", true);
        
        // NULL on left side tested against zero-equivalent string values.
        _runIfTest("@nn != @nc2", true);
        _runIfTest("@nn != @fc2", true);

        // Check literal string against literal numeric
        _runIfTest("'' != 2", true);
        _runIfTest("'2' != 2", false);
        _runIfTest("2 != '2'", false);
        _runIfTest("'2' != 2.0", false);
        _runIfTest("2.0 != '2'", false);

        _runIfTest("2.1 != 2", true);
        _runIfTest("2 != 2.1", true);
        _runIfTest("2 != 2.0", false);
        _runIfTest("2.0 != 2", false);
        
        // Check literal strings against booleans
        _runIfTest("true != '0'", true);
        _runIfTest("'0' != true", true);
        _runIfTest("true != '1'", false);
        
        _runIfTest("false != '0'", false);
        _runIfTest("'0' != false", false);
        _runIfTest("false != '1'", true);

        // NULL on both sides.
        _runIfTest("@nn != @nn", false);
        
        // Empty Strings compared to nulls
        _runIfTest("@nn != ''", false);
        _runIfTest("@c2 != ''", false);
        _runIfTest("@nn != null", false);
        _runIfTest("@c2 != null", false);
    }
    
    public void testComparisons() throws MocaException {
        // Same data types and same values.
        _runIfTest("2 < 2.2", true);
        _runIfTest("2.2 > 2", true);
        _runIfTest("'' < 'abc'", true);
        _runIfTest("'2' > 100", true);
        _runIfTest("'20000' <= 5", true);
        _runIfTest("'5' < 5.0", false);
        _runIfTest("'5' > 5.0", false);

        // Null is treated as a string.
        _runIfTest("0 >= @nn", true);
        _runIfTest("0 <= @nn", false);

        // Cast null to a numeric type.
        _runIfTest("0 >= int(null)", true);
        _runIfTest("0 <= int(null)", true);

        _runIfTest("0 > int(null)", false);
        _runIfTest("0 < int(null)", false);
    }
    
    public void testTypeMismatch() throws MocaException {
        _runIfTest("'Y' = 1", false);
        _runIfTest("'Y' < 1", false);
        _runIfTest("'Y' >= 1", true);
    }
    
    /**
     * Runs the test with the following variables on the stack.
     *  
     *   b1  - a boolean ('true')
     *   b2  - a boolean ('false')
     *   c1  - a string ('xxx')
     *   c2  - an empty string
     *   n1  - an integer (3)
     *   n2  - a zero integer
     *   nc1 - a string ('3')
     *   nc2 - a string ('0')
     *   f1  - a floating value (-902.2)
     *   f2  - a zero floating value (0.0)
     *   fc1 - a string ('-902.2')
     *   fc2 - a string ('0.0')
     *   nn  - a null value
     */
    private void _runIfTest(String expr, boolean expected) throws MocaException {
        MocaResults results = _moca.executeCommand(
            "publish data where c1 = 'xxx' and c2 = ''" +
            "               and n1 = 3 and n2 = 0 and nc1 = '3' and nc2 = '0'" +
            "               and f1 = -902.2 and f2 = 0.0 and fc1 = '-902.2' and fc2 = '0.0'" +
            "               and b1 = true and b2 = false and nn = null |" +
            "if (" + expr + ") {" +
            "   publish data where result = true" +
            "} else {" +
            "   publish data where result = false" +
            "}");
        RowIterator iter = results.getRows();
        assertTrue(iter.next());
        boolean actual = iter.getBoolean("result");
        assertEquals("Expression [" + expr + "]", expected, actual);
        assertFalse(iter.next());
    }
}
