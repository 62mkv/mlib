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
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.exec.ResultMapper;
import com.redprairie.moca.server.exec.ServerContext;

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
public abstract class AbstractFunctionTest {
    
    @SuppressWarnings("unchecked")
    @Before
    public void beforeTests() throws MocaException {
        _mockContext = Mockito.mock(ServerContext.class);
        Mockito.when(_mockContext.executeCommand(Mockito.anyString(), 
                Mockito.anyMap(), Mockito.anyBoolean()))
                .thenAnswer(new Answer<MocaResults>() {
                    // The answer just returns a result set containing the
                    // string passed in as the value.
                    @Override
                    public MocaResults answer(InvocationOnMock invocation)
                            throws Throwable {
                        return ResultMapper.createResults("executed ["
                                + invocation.getArguments()[0] + "]");
                    }
                });
    }
    
    protected void runTestWithObjectArgs(Object expected, Object... args) throws MocaException {
        BaseFunction testFunction = getFunction();
        List<MocaValue> functionArgs = new ArrayList<MocaValue>();
        for (Object arg : args) {
            functionArgs.add(new MocaValue(MocaType.forValue(arg), arg));
        }

        MocaValue result = testFunction.invoke(_mockContext, functionArgs);
        
        assertEquals(expected, result.getValue());
    }
    
    protected void runTestWithObjectArgsExpectingError(int expectedCode, Object... args) throws MocaException {
        BaseFunction testFunction = getFunction();
        List<MocaValue> functionArgs = new ArrayList<MocaValue>();
        for (Object arg : args) {
            functionArgs.add(new MocaValue(MocaType.forValue(arg), arg));
        }

        try {
            MocaValue result = testFunction.invoke(_mockContext, functionArgs);
            fail("Expected error, got value: " + result);
        }
        catch (MocaException e) {
            assertEquals(expectedCode, e.getErrorCode());
        }
    }
    
    abstract protected BaseFunction getFunction();

    protected ServerContext _mockContext;
}
