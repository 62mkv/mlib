/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.expression.function;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.expression.Expression;
import com.redprairie.moca.server.expression.LiteralExpression;
import com.redprairie.moca.server.expression.function.DieQuicklyExpression.QuickDeathException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class TU_ConditionalFunction {
    
    @Before
    public void beforeTests() throws MocaException {
        // We don't expect this to be used for anything.
        _mockContext = Mockito.mock(ServerContext.class);
    }
    
    @Test
    public void testBasicBehavior() throws MocaException {
        testLiteralArgs("A", true, "A");
        testLiteralArgs("A", true, "A", "B");
        testLiteralArgs(null, false, "A");
        testLiteralArgs("B", false, "A", "B");
    }
    
    @Test
    public void testNonEvaluatedArg() throws MocaException {
        List<Expression> args = new ArrayList<Expression>();
        args.add(new LiteralExpression(false));
        // If this argument is evaluated, an exception will be thrown.
        args.add(new DieQuicklyExpression("unexpected"));
        args.add(new LiteralExpression("X"));

        // Expect normal evaluation -- no exceptions
        MocaFunction f = new ConditionalFunction();
        MocaValue result = f.evaluate(_mockContext, args);
        assertEquals("X", result.getValue());

        args.clear();
        args.add(new LiteralExpression(true));
        args.add(new LiteralExpression("Y"));
        // If this argument is evaluated, an exception will be thrown.
        args.add(new DieQuicklyExpression("unexpected"));

        // Expect normal evaluation -- no exceptions
        result = f.evaluate(_mockContext, args);
        assertEquals("Y", result.getValue());
    }
    
    @Test
    public void testEvaluatedArgs() throws MocaException {
        List<Expression> args = new ArrayList<Expression>();
        args.add(new LiteralExpression(true));
        // If this argument is evaluated, an exception will be thrown.
        args.add(new DieQuicklyExpression("normal"));
        args.add(new LiteralExpression("A"));
        
        MocaFunction f = new ConditionalFunction();
        try {
            // Expect the second exception to be thrown, not the first.
            MocaValue result = f.evaluate(_mockContext, args);
            fail("Expected Exception, got" + result);
        }
        catch (QuickDeathException e) {
            assertEquals("QUICK DEATH: normal", e.getMessage());
        }

        args.clear();
        args.add(new LiteralExpression(false));
        args.add(new LiteralExpression("B"));
        // If this argument is evaluated, an exception will be thrown.
        args.add(new DieQuicklyExpression("normal too"));
        
        try {
            // Expect the second exception to be thrown, not the first.
            MocaValue result = f.evaluate(_mockContext, args);
            fail("Expected Exception, got" + result);
        }
        catch (QuickDeathException e) {
            assertEquals("QUICK DEATH: normal too", e.getMessage());
        }
    
    }
    
    private void testLiteralArgs(Object expected, Object... args) throws MocaException {
        List<Expression> exprArgs = new ArrayList<Expression>();
        for (Object a : args) {
            exprArgs.add(new LiteralExpression(a));
        }
        
        MocaFunction f = new ConditionalFunction();
        MocaValue result = f.evaluate(_mockContext, exprArgs);
        assertEquals(expected, result.getValue());
    }
    
    protected ServerContext _mockContext;
}
