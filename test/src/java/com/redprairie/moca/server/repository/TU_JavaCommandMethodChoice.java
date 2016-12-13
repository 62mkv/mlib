/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.server.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.components.mocatest.TestJavaMethodNameComponent;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.ServerContextStatus;
import com.redprairie.moca.server.repository.ArgType;
import com.redprairie.moca.server.repository.ArgumentInfo;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.JavaCommand;

import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.when;

/**
 * This Class tests which methods are chosen by MOCA
 * 
 * Copyright (c) 2012 Sam Corporation All Rights Reserved
 * 
 * @author eknapp
 */
public class TU_JavaCommandMethodChoice {

    private Class<?> testClass = TestJavaMethodNameComponent.class;

    private static final String methodName = "testMethod";

    private static final String argName = "name";

    private static final String argValue = "value";

    @Mock
    ComponentLevel level;

    @Mock
    ArgumentInfo arg1;

    @Mock
    ArgumentInfo arg2;

    @Mock
    ArgumentInfo arg3;

    @Mock
    ServerContext context;

    @Mock
    MocaContext mocaContext;

    @Mock
    MocaValue mocaValue;

    @Before
    public void testSetup() {
        // Initialize Mock objects.
        MockitoAnnotations.initMocks(this);

        // Setup proper behavior for the Argument Info objects.
        when(arg1.getName()).thenReturn(argName);
        when(arg1.getAlias()).thenReturn(argName);
        when(arg1.getDatatype()).thenReturn(ArgType.STRING);
        when(arg1.getDefaultValue()).thenReturn(argValue);
        when(arg1.isRequired()).thenReturn(true);

        when(arg2.getName()).thenReturn(argName);
        when(arg2.getAlias()).thenReturn(argName);
        when(arg2.getDatatype()).thenReturn(ArgType.STRING);
        when(arg2.getDefaultValue()).thenReturn(argValue);
        when(arg2.isRequired()).thenReturn(true);

        when(arg3.getName()).thenReturn(argName);
        when(arg3.getAlias()).thenReturn(argName);
        when(arg3.getDatatype()).thenReturn(ArgType.STRING);
        when(arg3.getDefaultValue()).thenReturn(argValue);
        when(arg3.isRequired()).thenReturn(true);

        // Setup our MocaValue mock properly
        when(mocaValue.getValue()).thenReturn(argValue);
        when(mocaValue.getType()).thenReturn(MocaType.STRING);

        // Setup the context mock to behave properly.
        when(context.getCurrentStatus()).thenReturn(ServerContextStatus.INACTIVE);
        when(context.getComponentContext()).thenReturn(mocaContext);
        when(context.getVariable(argName, argName, true)).thenReturn(mocaValue);
        when(level.getPackage()).thenReturn(testClass.getPackage().getName());

        when(level.getPackage()).thenReturn(testClass.getPackage().getName());
    }

    @Test
    public void testExecuteMethodNoArgs() throws MocaException {
        JavaCommand command = new JavaCommand(methodName, level);
        command.setClassName(testClass.getSimpleName());
        command.setMethod(methodName);

        // Execute the command and get the results.
        MocaResults results = command.execute(context);

        // The results set must have one row.
        assertTrue(results.next());

        boolean match = false;
        String result = results.getString(0);

        if (result.equals("MocaContext") || result.equals("MadFactory")
                || result.equals("MocaContext MadFactory")) {
            match = true;
        }

        // With no arguments provided the method chosen can feasably have three
        // options of parameters; only a moca context, only a mad factory, or
        // both. This check verifies that one of them is chosen.
        assertTrue(match);
    }

    @Test
    public void testExecuteMethodOneStringArg() throws MocaException {
        JavaCommand command = new JavaCommand(methodName, level);
        command.setClassName(testClass.getSimpleName());
        command.setMethod(methodName);

        // Add a single String argument
        command.addArgument(arg1);

        // Execute the command and get the results.
        MocaResults results = command.execute(context);

        // The results set must have one row.
        assertTrue(results.next());

        boolean match = false;
        String result = results.getString(0);

        if (result.equals("MocaContext String") || result.equals("MadFactory String")
                || result.equals("MocaContext MadFactory String")
                || result.equals("String")) {
            match = true;
        }

        // With one argument provided the method chosen can feasably have four
        // options of parameters; a single string, a moca context with a string,
        // mad factory with a string, or both the mad factory and moca context
        // with a string. This check verifies that one of them is chosen.
        assertTrue(match);
    }

    @Test
    public void testExecuteMethodtTwoStringArg() throws MocaException {
        JavaCommand command = new JavaCommand(methodName, level);
        command.setClassName(testClass.getSimpleName());
        command.setMethod(methodName);

        // Add arguments to the command
        command.addArgument(arg1);
        command.addArgument(arg2);

        // Execute the command and get the results.
        MocaResults results = command.execute(context);

        // The results set must have one row.
        assertTrue(results.next());

        boolean match = false;
        String result = results.getString(0);

        if (result.equals("MocaContext String String")
                || result.equals("MadFactory String String")
                || result.equals("MocaContext MadFactory String String")
                || result.equals("String String")) {
            match = true;
        }

        // With two arguments provided the method chosen can feasably have four
        // options of parameters; two strings, a moca context and two strings, a
        // mad factory and two strings, or both a moca context and a mad factory
        // with two strings. This check verifies that one of them is chosen.
        assertTrue(match);
    }

    @Test
    public void testExecuteMethodtThreeStringArg() throws MocaException {
        JavaCommand command = new JavaCommand(methodName, level);
        command.setClassName(testClass.getSimpleName());
        command.setMethod(methodName);

        // Add arguments to the command
        command.addArgument(arg1);
        command.addArgument(arg2);
        command.addArgument(arg3);

        // Execute the command and get the results.
        MocaResults results = command.execute(context);

        // The results set must have one row.
        assertTrue(results.next());

        boolean match = false;
        String result = results.getString(0);

        if (result.equals("String String String")) {
            match = true;
        }

        // With three arguments provided the method chosen can feasably have one
        // option. This check verifies that the correct option is chosen.
        assertTrue(match);
    }

}
