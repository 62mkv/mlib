/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.probes;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.server.exec.ServerContext;

import static org.junit.Assert.*;

/**
 * Tests for the abstract call MocaPollingProbe
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_MocaPollingProbe {
    
    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(_mockServerContext.getComponentContext()).thenReturn(_mockMocaContext);
    }
    
    // Tests that commit occurs after a successful execution
    @Test
    public void testPollingProbeSuccess() throws MocaException {
        MocaPollingProbe probe = new MocaPollingProbe() {

            @Override
            protected void executeWithContext(MocaContext ctx, MadFactory mFact)
                    throws MocaException {
                ctx.executeCommand(TEST_CMD);
            }
            
        };
        
        probe.execute(_mockServerContext, _mockMadFactory);
        Mockito.verify(_mockMocaContext).executeCommand(TEST_CMD);
        Mockito.verify(_mockServerContext).commit();
        Mockito.verify(_mockServerContext).close();
    }
    
    // Tests that a commit does not occur and the server context is closed (effectively 
    // doing a rollback) if a MocaException is thrown
    @Test
    public void testPollingProbeMocaException() throws MocaException {
        MocaPollingProbe probe = new MocaPollingProbe() {

            @Override
            protected void executeWithContext(MocaContext ctx, MadFactory mFact)
                    throws MocaException {
                ctx.executeCommand(TEST_CMD);
            }
            
        };
        
        Mockito.when(_mockMocaContext.executeCommand(TEST_CMD)).thenThrow(new NotFoundException());
        probe.execute(_mockServerContext, _mockMadFactory);
        Mockito.verify(_mockMocaContext).executeCommand(TEST_CMD);
        Mockito.verify(_mockServerContext, Mockito.never()).commit();
        Mockito.verify(_mockServerContext).close();
    }
    
    // Tests that a commit does not occur and the server context is closed (effectively 
    // doing a rollback) if a RuntimeException is thrown
    @Test
    public void testPollingProbeRuntimeException() throws MocaException {
        
        MocaPollingProbe probe = new MocaPollingProbe() {

            @Override
            protected void executeWithContext(MocaContext ctx, MadFactory mFact)
                    throws MocaException {
                throw new NullPointerException();
            }
            
        };

        try {
            probe.execute(_mockServerContext, _mockMadFactory);
            fail("Test implementation should have thrown a NPE");
        }
        catch (NullPointerException expected) {
            Mockito.verify(_mockServerContext, Mockito.never()).commit();
            Mockito.verify(_mockServerContext).close();
        }
    }
    
    @Mock
    ServerContext _mockServerContext;
    
    @Mock
    MocaContext _mockMocaContext;
    
    @Mock
    MadFactory _mockMadFactory;
    
    private static final String TEST_CMD = "get probe data where probe = 'myprobe'";
}
