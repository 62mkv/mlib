/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
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
import com.redprairie.moca.MocaType;
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
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class TU_DecodeFunction {
    
    @Before
    public void beforeTests() throws MocaException {
        // We don't expect this to be used for anything.
        _mockContext = Mockito.mock(ServerContext.class);
    }
    
    @Test
    public void testNonEvaluatedArgs() throws MocaException {
        List<Expression> args = new ArrayList<Expression>();
        args.add(new LiteralExpression("A"));
        args.add(new LiteralExpression("B"));
        // If this argument is evaluated, an exception will be thrown.
        args.add(new DieQuicklyExpression("unexpected"));
        args.add(new LiteralExpression(256));

        // Expect normal evaluation -- no exceptions
        MocaFunction f = new DecodeFunction();
        MocaValue result = f.evaluate(_mockContext, args);
        assertEquals(256, result.asInt());
        assertEquals(MocaType.INTEGER, result.getType());
    }
    
    @Test
    public void testEvaluatedArgs() throws MocaException {
        List<Expression> args = new ArrayList<Expression>();
        args.add(new LiteralExpression("A"));
        args.add(new LiteralExpression("B"));
        // If this argument is evaluated, an exception will be thrown.
        args.add(new DieQuicklyExpression("unexpected"));
        args.add(new LiteralExpression("A"));
        
        // If this argument is evaluated, an exception will be thrown.
        args.add(new DieQuicklyExpression("normal"));
        args.add(new LiteralExpression(256));
        
        MocaFunction f = new DecodeFunction();
        try {
            // Expect the second exception to be thrown, not the first.
            MocaValue result = f.evaluate(_mockContext, args);
            fail("Expected Exception, got" + result);
        }
        catch (QuickDeathException e) {
            assertEquals("QUICK DEATH: normal", e.getMessage());
        }
    }
    
    protected ServerContext _mockContext;
}
