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

package com.redprairie.moca.server.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.RequiredArgumentException;
import com.redprairie.moca.server.exec.ServerContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_Command {
    
    private static class ConcreteCommand extends Command {
        private static final long serialVersionUID = 1L;

        public ConcreteCommand(String name, ComponentLevel level) {
            super(name, level);
        }

        // @see com.redprairie.moca.server.repository.Command#executeWithContext(com.redprairie.moca.server.exec.ServerContext)
        @Override
        protected MocaResults executeWithContext(ServerContext ctx)
                throws MocaException {
            return null;
        }

        // @see com.redprairie.moca.server.repository.Command#getType()
        @Override
        public CommandType getType() {
            return null;
        }
    }
    
    @Before
    public void beforeTest() {
        _command = new ConcreteCommand("test command", new ComponentLevel("test"));
    }

    /**
     * Test method for {@link com.redprairie.moca.server.repository.Command#getArgs(com.redprairie.moca.server.exec.ServerContext)}.
     * @throws MocaException 
     */
    @Test
    public void testGetArgsRequiredNotOnStack() throws MocaException {
        _command.addArgument(new ArgumentInfo("required", null, ArgType.STRING, 
                null, true));
        
        try {
            ServerContext mockContext = Mockito.mock(ServerContext.class);
            // The server context returns null - meaning it wasn't on the stack
            _command.getArgs(mockContext);
            fail("Expected to get a RequiredArgumentException, but passed");
        }
        catch (RequiredArgumentException e) {
            // We expect to get a exception saying that it was required.
        }
    }
    
    /**
     * Test method for {@link com.redprairie.moca.server.repository.Command#getArgs(com.redprairie.moca.server.exec.ServerContext)}.
     * @throws MocaException 
     */
    @Test
    public void testGetArgsRequiredEmptyString() throws MocaException {
        _command.addArgument(new ArgumentInfo("required", null, ArgType.STRING, 
                null, true));
        
        // The server context returns empty - meaning it was on the stack
        // but the value was empty
        ServerContext mockContext = Mockito.mock(ServerContext.class);
        Mockito.when(mockContext.getVariable(Mockito.anyString(), 
                Mockito.anyString(), Mockito.anyBoolean())).
                thenReturn(new MocaValue(MocaType.STRING, ""));
        _command.getArgs(mockContext);
    }

    /**
     * Test method for {@link com.redprairie.moca.server.repository.Command#getArgs(com.redprairie.moca.server.exec.ServerContext)}.
     * @throws MocaException 
     */
    @Test
    public void testGetArgsRequiredNull() throws MocaException {
        _command.addArgument(new ArgumentInfo("required", null, ArgType.STRING, 
                null, true));
        
        try {
            ServerContext mockContext = Mockito.mock(ServerContext.class);
            Mockito.when(mockContext.getVariable(Mockito.anyString(), 
                    Mockito.anyString(), Mockito.anyBoolean())).
                    thenReturn(new MocaValue(MocaType.STRING, null));
            // The server context returns null value - meaning it was on the 
            // stack as a null value
            _command.getArgs(mockContext);
            fail("Expected to get a RequiredArgumentException, but passed");
        }
        catch (RequiredArgumentException e) {
            // We expect to get a exception saying that it was required.
        }
    }
    
    /**
     * Test method for {@link com.redprairie.moca.server.repository.Command#getArgs(com.redprairie.moca.server.exec.ServerContext)}.
     * @throws MocaException 
     */
    @Test
    public void testGetArgsDefaultBooleanOfZero() throws MocaException {
        _command.addArgument(new ArgumentInfo("default", null, ArgType.FLAG, 
                "0", true));
        
        ServerContext mockContext = Mockito.mock(ServerContext.class);
        
        Object[] args = _command.getArgs(mockContext);
        
        assertTrue("We should get back a single argument", args.length == 1);
        assertEquals("The return type should be false.", 
                Boolean.valueOf(false), args[0]);
    }
    
    /**
     * Test method for {@link com.redprairie.moca.server.repository.Command#getArgs(com.redprairie.moca.server.exec.ServerContext)}.
     * @throws MocaException 
     */
    @Test
    public void testGetArgsDefaultBooleanOfZeroWithLetters() throws MocaException {
        _command.addArgument(new ArgumentInfo("default", null, ArgType.FLAG, 
                "0BLAH", true));
        
        ServerContext mockContext = Mockito.mock(ServerContext.class);
        
        Object[] args = _command.getArgs(mockContext);
        
        assertTrue("We should get back a single argument", args.length == 1);
        assertEquals("The return type should be false.", 
                Boolean.valueOf(false), args[0]);
    }
    
    /**
     * Test method for {@link com.redprairie.moca.server.repository.Command#getArgs(com.redprairie.moca.server.exec.ServerContext)}.
     * @throws MocaException 
     */
    @Test
    public void testGetArgsDefaultBooleanWithNumber() throws MocaException {
        _command.addArgument(new ArgumentInfo("default", null, ArgType.FLAG, 
                "1", true));
        
        ServerContext mockContext = Mockito.mock(ServerContext.class);
        
        Object[] args = _command.getArgs(mockContext);
        
        assertTrue("We should get back a single argument", args.length == 1);
        assertEquals("The return type should be true.", 
                Boolean.valueOf(true), args[0]);
    }
    
    /**
     * Test method for {@link com.redprairie.moca.server.repository.Command#getArgs(com.redprairie.moca.server.exec.ServerContext)}.
     * @throws MocaException 
     */
    @Test
    public void testGetArgsDefaultBooleanWithNumberThenLetters() throws MocaException {
        _command.addArgument(new ArgumentInfo("default", null, ArgType.FLAG, 
                "23A", true));
        
        ServerContext mockContext = Mockito.mock(ServerContext.class);
        
        Object[] args = _command.getArgs(mockContext);
        
        assertTrue("We should get back a single argument", args.length == 1);
        assertEquals("The return type should be true.", 
                Boolean.valueOf(true), args[0]);
    }
    
    /**
     * Test method for {@link com.redprairie.moca.server.repository.Command#getArgs(com.redprairie.moca.server.exec.ServerContext)}.
     * @throws MocaException 
     */
    @Test
    public void testGetArgsDefaultInteger() throws MocaException {
        _command.addArgument(new ArgumentInfo("default", null, ArgType.INTEGER, 
                "23", true));
        
        ServerContext mockContext = Mockito.mock(ServerContext.class);
        
        Object[] args = _command.getArgs(mockContext);
        
        assertTrue("We should get back a single argument", args.length == 1);
        assertEquals("The return type should be 23.", 
                Integer.valueOf(23), args[0]);
    }
    
    /**
     * Test method for {@link com.redprairie.moca.server.repository.Command#getArgs(com.redprairie.moca.server.exec.ServerContext)}.
     * @throws MocaException 
     */
    @Test
    public void testGetArgsDefaultIntegerAndLetters() throws MocaException {
        _command.addArgument(new ArgumentInfo("default", null, ArgType.INTEGER, 
                "23ABC", true));
        
        ServerContext mockContext = Mockito.mock(ServerContext.class);
        
        try {
            _command.getArgs(mockContext);
            fail("We should have thrown a NFE since 23ABC isn't an integer");
        }
        catch (NumberFormatException e) {
            // We should throw this
        }
    }
    
    Command _command;
}
