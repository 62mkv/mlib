/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.server.repository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadMetrics;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.components.mocatest.TestJavaComponent;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.ServerContextStatus;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.when;

/**
 * This is a simple JavaCommand test unit class that verifies that outside of
 * the actal execution of the java command method, the setup and executiuon is
 * done properly and as expected.
 * 
 * Copyright (c) 2012 RedPrairie Corporation All Rights Reserved
 * 
 * @author eknapp
 */
public class TU_JavaCommand {

    @Mock
    ServerContext context;

    @Mock
    MocaContext mocaContext;

    @Mock
    ComponentLevel level;

    @Mock
    MadFactory factory;

    @Mock
    MocaValue mocaValue;

    @Mock
    ArgumentInfo argumentInfo;

    Class<?> testClass = TestJavaComponent.class;

    String argName = "string-arg";

    String argValue = "string-val";

    private static final MadFactory prevFactory = MadMetrics.getFactory();

    @Before
    public void testSetup() {
        MockitoAnnotations.initMocks(this);

        // Set the MadFactory for use.
        MadMetrics.setFactory(factory);

        // Setup our argument info mock
        when(argumentInfo.getName()).thenReturn(argName);
        when(argumentInfo.getAlias()).thenReturn(argName);
        when(argumentInfo.getDatatype()).thenReturn(ArgType.STRING);
        when(argumentInfo.getDefaultValue()).thenReturn(argValue);
        when(argumentInfo.isRequired()).thenReturn(true);

        // Setup our MocaValue mock properly
        when(mocaValue.getValue()).thenReturn(argValue);
        when(mocaValue.getType()).thenReturn(MocaType.STRING);

        // Setup the context mock to behave properly.
        when(context.getCurrentStatus()).thenReturn(ServerContextStatus.INACTIVE);
        when(context.getComponentContext()).thenReturn(mocaContext);
        when(context.getVariable(argName, argName, true)).thenReturn(mocaValue);
        when(level.getPackage()).thenReturn(testClass.getPackage().getName());
    }

    @Test
    public void testJavaCommandWithContext() throws MocaException {
        // Create a JavaCommand with a method with only a MocaContext parameter
        String commandName = "testMethodWithContext";
        JavaCommand command = new JavaCommand(commandName, level);
        command.setClassName(testClass.getSimpleName());
        command.setMethod(commandName);

        // Execute the command and get the results.
        MocaResults results = command.execute(context);

        // There must be one row returned.
        assertTrue(results.next());

        // Now Verify that all of the values are as expected.
        assertTrue(results.getBoolean("Context-Present"));
        assertFalse(results.getBoolean("Factory-Present"));
        assertFalse(results.getBoolean("Arg-Present"));
    }

    @Test
    public void testJavaCommandWithContextAndArg() throws MocaException {
        // Create a JavaCommand with a method with only a MocaContext parameter
        String commandName = "testMethodWithContextAndArgs";
        JavaCommand command = new JavaCommand(commandName, level);
        command.setClassName(testClass.getSimpleName());
        command.setMethod(commandName);

        // Build the arg...
        command.addArgument(argumentInfo);

        // Execute the command and get the results.
        MocaResults results = command.execute(context);

        // There must be one row returned.
        assertTrue(results.next());

        // Now Verify that all of the values are as expected.
        assertTrue(results.getBoolean("Context-Present"));
        assertFalse(results.getBoolean("Factory-Present"));
        assertTrue(results.getBoolean("Arg-Present"));
    }

    @Test
    public void testJavaCommandWithFactory() throws MocaException {
        // Create a JavaCommand with a method with only a MocaContext parameter
        String commandName = "testMethodWithFactory";
        JavaCommand command = new JavaCommand(commandName, level);
        command.setClassName(testClass.getSimpleName());
        command.setMethod(commandName);

        // Execute the command and get the results.
        MocaResults results = command.execute(context);

        // There must be one row returned.
        assertTrue(results.next());

        // Now Verify that all of the values are as expected.
        assertFalse(results.getBoolean("Context-Present"));
        assertTrue(results.getBoolean("Factory-Present"));
        assertFalse(results.getBoolean("Arg-Present"));
    }

    @Test
    public void testJavaCommandWithContextAndFactory() throws MocaException {
        // Create a JavaCommand with a method with only a MocaContext parameter
        String commandName = "testMethodWithContextAndFactory";
        JavaCommand command = new JavaCommand(commandName, level);
        command.setClassName(testClass.getSimpleName());
        command.setMethod(commandName);

        // Execute the command and get the results.
        MocaResults results = command.execute(context);

        // There must be one row returned.
        assertTrue(results.next());

        // Now Verify that all of the values are as expected.
        assertTrue(results.getBoolean("Context-Present"));
        assertTrue(results.getBoolean("Factory-Present"));
        assertFalse(results.getBoolean("Arg-Present"));
    }

    @Test
    public void testJavaCommandWithContextAndFactoryAndArg() throws MocaException {
        // Create a JavaCommand with a method with only a MocaContext parameter
        String commandName = "testMethodWithContextAndFactoryAndArgs";
        JavaCommand command = new JavaCommand(commandName, level);
        command.setClassName(testClass.getSimpleName());
        command.setMethod(commandName);

        // Build the arg...
        command.addArgument(argumentInfo);

        // Execute the command and get the results.
        MocaResults results = command.execute(context);

        // There must be one row returned.
        assertTrue(results.next());

        // Now Verify that all of the values are as expected.
        assertTrue(results.getBoolean("Context-Present"));
        assertTrue(results.getBoolean("Factory-Present"));
        assertTrue(results.getBoolean("Arg-Present"));
    }

    @After
    public void testCleanup() {
        MadMetrics.setFactory(prevFactory);
    }

}
