/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.redprairie.moca.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadMetrics;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.server.exec.ServerContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class is to test some of the general functionality provided
 * by the ThreadTask class.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_ThreadTask {

    /**
     * Test method for {@link com.redprairie.moca.task.ThreadTask#instantiateRunnableFromCommandLine(java.lang.CharSequence)}.
     */
    @Test
    public void testInstantiateRunnableFromCommandLineForStringArray() {
        Runnable runnable = ThreadTask.instantiateRunnableFromCommandLine(
                StringArray.class.getName() + 
                " test -o*\t\"arg \"something\not\"herst\"uff\"more\tstuff 1\" \"last thing\" not");
        
        assertTrue(runnable instanceof StringArray);
        StringArray stringarray = (StringArray)runnable;
        
        List<String> args = new ArrayList<String>();
        
        args.add("test");
        args.add("-o*");
        args.add("arg something");
        args.add("otherstuffmore\tstuff 1");
        args.add("last thing");
        args.add("not");
        
        assertEquals(args, Arrays.asList(stringarray._args));
    }

    /**
     * Test method for {@link com.redprairie.moca.task.ThreadTask#instantiateRunnableFromCommandLine(com.redprairie.moca.server.ServerContext; java.lang.CharSequence)}.
     */
    @Test
    public void testInstantiateRunnableFromCommandLineForMocaStringArray() {
        MocaContext mockMoca = Mockito.mock(MocaContext.class);
        ServerContext mockContext = Mockito.mock(ServerContext.class);
        Mockito.when(mockContext.getComponentContext()).thenReturn(mockMoca);
        
        Runnable runnable = ThreadTask.instantiateRunnableFromCommandLine(
                mockContext, MocaStringArray.class.getName() + 
                " test -o*\t\"arg \"something\not\"herst\"uff\"more\tstuff 1\" \"last thing\" not");
        
        assertTrue(runnable instanceof MocaStringArray);
        MocaStringArray stringarray = (MocaStringArray)runnable;
        
        List<String> args = new ArrayList<String>();
        
        args.add("test");
        args.add("-o*");
        args.add("arg something");
        args.add("otherstuffmore\tstuff 1");
        args.add("last thing");
        args.add("not");
        
        assertEquals(mockMoca, stringarray._moca);
        assertEquals(args, Arrays.asList(stringarray._args));
    }
    
    /**
     * A test method to verify that when requested the MadFactory is properly injected.
     */
    @Test
    public void testInstantiateRunnableFromCommandLineForMadFactoryRunnable() {
        // Setup MadMetrics to use a Mock Factory...
        MadFactory prevFactory = MadMetrics.getFactory();
        MadFactory factory = Mockito.mock(MadFactory.class);
        MadMetrics.setFactory(factory);
        
        // Create the runnable using the constructor with MadFactory
        Runnable runnable = ThreadTask.instantiateRunnableFromCommandLine(null,
            MadFactoryRunnable.class.getName());
        
        // Verify that the mad factory was injected properly.
        assertTrue(runnable instanceof MadFactoryRunnable);
        MadFactoryRunnable factoryRunnable = (MadFactoryRunnable)runnable;
        assertEquals(factory, factoryRunnable._factory);
        
        // Set the Mad Metrics factory to what is was initially.
        MadMetrics.setFactory(prevFactory);
    }
    
    /**
     * A test method to verify that when requested the MadFactory and MocaContext are properly injected.
     */
    @Test
    public void testInstantiateRunnableFromCommandLineForMadFactoryAndContextRunnable() {
        // Setup MadMetrics to use a Mock Factory...
        MadFactory prevFactory = MadMetrics.getFactory();
        MadFactory factory = Mockito.mock(MadFactory.class);
        MadMetrics.setFactory(factory);
        
        // Setup the mock Moca Contexts to behanve properly.
        MocaContext mocaContext = Mockito.mock(MocaContext.class);
        ServerContext serverContext = Mockito.mock(ServerContext.class);
        Mockito.when(serverContext.getComponentContext()).thenReturn(mocaContext);
        
        // Create the runnable using the constructor with MadFactory and MocaContext
        Runnable runnable = ThreadTask.instantiateRunnableFromCommandLine(serverContext,
            MadFactoryAndContextRunnable.class.getName());
        
        // Verify that the mad factory and moca context were injected properly.
        assertTrue(runnable instanceof MadFactoryAndContextRunnable);
        MadFactoryAndContextRunnable factoryAndContextRunnable = (MadFactoryAndContextRunnable)runnable;
        assertEquals(mocaContext, factoryAndContextRunnable._context);
        assertEquals(factory, factoryAndContextRunnable._factory);
        
        // Set the Mad Metrics factory to what is was initially.
        MadMetrics.setFactory(prevFactory);
    }
    
    @Test
    public void testInstantiateRunnableFromCommandLineForMadFactoryAndContextAndStringRunnable() {
        // Setup MadMetrics to use a Mock Factory...
        MadFactory prevFactory = MadMetrics.getFactory();
        MadFactory factory = Mockito.mock(MadFactory.class);
        MadMetrics.setFactory(factory);
        
        // Setup the mock Moca Contexts to behanve properly.
        MocaContext mocaContext = Mockito.mock(MocaContext.class);
        ServerContext serverContext = Mockito.mock(ServerContext.class);
        Mockito.when(serverContext.getComponentContext()).thenReturn(mocaContext);
        
        String arg = "argument";
        
        // Create the runnable using the constructor with MadFactory and MocaContext
        Runnable runnable = ThreadTask.instantiateRunnableFromCommandLine(serverContext,
            MadFactoryAndContextAndStringRunnable.class.getName() + " " + arg);
        
        // Verify that the mad factory and moca context were injected properly.
        assertTrue(runnable instanceof MadFactoryAndContextAndStringRunnable);
        MadFactoryAndContextAndStringRunnable factoryAndContextRunnable = (MadFactoryAndContextAndStringRunnable) runnable;
        assertEquals(mocaContext, factoryAndContextRunnable._context);
        assertEquals(factory, factoryAndContextRunnable._factory);
        assertEquals(arg, factoryAndContextRunnable._arg);
        
        // Set the Mad Metrics factory to what is was initially.
        MadMetrics.setFactory(prevFactory);
    }
    
    /**
     * A test method to verify that when MadFactory and MocaContext are not in
     * the proper order a constructor will not be found.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testInstantiateRunnableFromCommandLineForMadFactoryAndContextWrongOrder() {
        // Setup the mock Moca Contexts properly.
        MocaContext mocaContext = Mockito.mock(MocaContext.class);
        ServerContext serverContext = Mockito.mock(ServerContext.class);
        Mockito.when(serverContext.getComponentContext()).thenReturn(mocaContext);
        
        // Try to create the runnable using the constructor with MadFactory and MocaContext
        ThreadTask.instantiateRunnableFromCommandLine(serverContext,
            MadFactoryAndContextWrongOrder.class.getName());
    }
    
    /**
     * Test method for {@link com.redprairie.moca.task.ThreadTask#instantiateRunnableFromCommandLine(java.lang.CharSequence)}.
     */
    @Test
    public void testInstantiateRunnableFromCommandLineForStringArrayUnmatchParenthesis() {
        try {
            ThreadTask.instantiateRunnableFromCommandLine(
                    StringArray.class.getName() + " test -o*\t\"arg ");
            fail("We should have thrown an illegal argument exception");
        }
        catch (IllegalArgumentException e) {
            // We should have thrown this
        }
    }

    /**
     * Test method for {@link com.redprairie.moca.task.ThreadTask#instantiateRunnableFromCommandLine(java.lang.CharSequence)}.
     */
    @Test
    public void testInstantiateRunnableFromCommandLineForNoArg() {
        Runnable runnable = ThreadTask.instantiateRunnableFromCommandLine(
                NoArg.class.getName() + " test -o*\t\"arg \"something\notherstuff");
        
        assertTrue(runnable instanceof NoArg);
    }
    
    public static class StringArray implements Runnable {
        public StringArray(String[] args) {
            _args = Arrays.copyOf(args, args.length);
        }

        // @see java.lang.Runnable#run()
        @Override
        public void run() {}
        
        public final String[] _args;
    }
    
    public static class MocaStringArray implements Runnable {
        public MocaStringArray(MocaContext moca, String[] args) {
            _moca = moca;
            _args = Arrays.copyOf(args, args.length);
        }

        // @see java.lang.Runnable#run()
        @Override
        public void run() {}
        
        public final String[] _args;
        public final MocaContext _moca;
    }
    
    public static class NoArg implements Runnable {
        public NoArg() {}
        
        // @see java.lang.Runnable#run()
        @Override
        public void run() {}
    }
    
    public static class MadFactoryRunnable implements Runnable {       
        public MadFactoryRunnable(MadFactory factory) {
            _factory = factory;
        }
        
        @Override
        public void run() {}
        
        public final MadFactory _factory;
    }
    
    public static class MadFactoryAndContextRunnable implements Runnable {      
        public MadFactoryAndContextRunnable(MocaContext context, MadFactory factory) {
            _context = context;
            _factory = factory;
        }
        
        @Override
        public void run() {}
        
        public final MocaContext _context;
        public final MadFactory _factory;
    }
    
    public static class MadFactoryAndContextAndStringRunnable implements Runnable {      
        public MadFactoryAndContextAndStringRunnable(MocaContext context, MadFactory factory, String[] args) {
            _context = context;
            _factory = factory;
            _arg = args[0];
        }
        
        @Override
        public void run() {}
        
        public final MocaContext _context;
        public final MadFactory _factory;
        public final String _arg;
    }
    
    
    public static class MadFactoryAndContextWrongOrder implements Runnable {      
        public MadFactoryAndContextWrongOrder(MadFactory factory, MocaContext context) {
            _context = context;
            _factory = factory;
        }
        
        @Override
        public void run() {}
        
        public final MocaContext _context;
        public final MadFactory _factory;
    }
}
