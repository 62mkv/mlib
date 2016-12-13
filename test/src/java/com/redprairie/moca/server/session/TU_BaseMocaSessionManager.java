/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.server.session;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.redprairie.moca.server.SecurityLevel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test classes for the base MOCA session manager behavior.  This does not 
 * test the mechanism by which "tracked" session IDs are stored.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class TU_BaseMocaSessionManager {

    @Test
    public void testTrackedSessionValidation() {
        BaseMocaSessionManager manager = new InMemoryMocaSessionManager("test", null, 300, 300, false);
        BaseMocaSessionManager spy = Mockito.spy(manager);

        String newKey = spy.createTracked("testuser", null, null, SecurityLevel.ALL);
        assertNotNull(newKey);
        
        SessionToken token = spy.validate(newKey);
        assertNotNull(token);
        String sid = token.getSessionId();
        
        // Make sure the manager has our SID.
        assertTrue(manager.checkSession(sid));
        
        Mockito.verify(spy).saveSession(Mockito.anyString(),
            (SessionData) Mockito.any());
        Mockito.verify(spy).checkSession(Mockito.anyString());
        Mockito.verify(spy, Mockito.times(0)).removeSession(Mockito.anyString());
        
        // Now, remove the SID, and make sure the validation fails.
        spy.removeSession(sid);
        assertNull(spy.validate(newKey));
        
        // Verify that the appropriate number of save/check/remove calls happened.
        Mockito.verify(spy).saveSession(Mockito.anyString(),
            (SessionData) Mockito.any());
        Mockito.verify(spy, Mockito.times(2)).checkSession(Mockito.anyString());
        Mockito.verify(spy).removeSession(Mockito.anyString());
    }
    
    @Test
    public void testTrackedSessionValidationWithEnv() {
        BaseMocaSessionManager manager = new InMemoryMocaSessionManager("test", null, 300, 300, false);
        BaseMocaSessionManager spy = Mockito.spy(manager);
        
        Map<String, String> env = new HashMap<String, String>();
        env.put("TESTVAR", "hello");
        
        String newKey = spy.createTracked("testuser", null, env, SecurityLevel.ALL);
        assertNotNull(newKey);
        
        ArgumentCaptor<SessionData> argument = ArgumentCaptor
                .forClass(SessionData.class);
            Mockito.verify(spy).saveSession(Mockito.anyString(), argument.capture());
        
        SessionData sd = argument.getValue();
        assertEquals(env, sd.getEnvironment());
        
        SessionToken token = spy.validate(newKey);
        String sid = token.getSessionId();
        assertNotNull(token);
        
        // Make sure the manager has our SID.
        assertTrue(manager.checkSession(sid));
        
        Mockito.verify(spy).saveSession(Mockito.anyString(),
            (SessionData) Mockito.any());
        Mockito.verify(spy).checkSession(Mockito.anyString());
        Mockito.verify(spy, Mockito.times(0)).removeSession(Mockito.anyString());
        
        // Now, remove the SID, and make sure the validation fails.
        spy.removeSession(sid);
        assertNull(spy.validate(newKey));
        
        // Verify that the appropriate number of save/check/remove calls happened.
        Mockito.verify(spy).saveSession(Mockito.anyString(),
            (SessionData) Mockito.any());
        Mockito.verify(spy, Mockito.times(2)).checkSession(Mockito.anyString());
        Mockito.verify(spy).removeSession(Mockito.anyString());
    }
     
    @Test
    public void testRemoteSessionValidation() throws InterruptedException {
        // Make a session manager that allows 5 seconds for remote sessions
        BaseMocaSessionManager manager = new InMemoryMocaSessionManager("test", null, 300, 5, false);
        BaseMocaSessionManager spy = Mockito.spy(manager);
        
        String newKey = spy.createRemote("testuser");
        assertNotNull(newKey);
        
        SessionToken token = spy.validate(newKey);
        String sid = token.getSessionId();
        assertNotNull(token);
        assertNull(sid);
        
        Mockito.verify(spy,Mockito.times(0)).saveSession(Mockito.anyString(),
            (SessionData) Mockito.any());
        Mockito.verify(spy, Mockito.times(0)).checkSession(Mockito.anyString());
        Mockito.verify(spy, Mockito.times(0)).removeSession(Mockito.anyString());

        // Now, wait 6 seconds, and make sure the validation fails.
        // Ensures timeout.
        Thread.sleep(6000L);
        assertNull(manager.validate(newKey));
        
        // Verify that the appropriate number of save/check/remove calls happened.
        Mockito.verify(spy,Mockito.times(0)).saveSession(Mockito.anyString(),
            (SessionData) Mockito.any());
        Mockito.verify(spy, Mockito.times(0)).checkSession(Mockito.anyString());
        Mockito.verify(spy, Mockito.times(0)).removeSession(Mockito.anyString());

    }
    
    @Test
    public void testRemoteSessionWithAlternateDomain() throws InterruptedException {
        BaseMocaSessionManager localManager = new InMemoryMocaSessionManager("BBB#WXYZ", null, 300, 300, false);
        // Make a session manager that allows 5 seconds for remote sessions
        BaseMocaSessionManager remoteManager = new InMemoryMocaSessionManager("AAA#ABCD", new String[] {"BBB#WXYZ", "CCC", "DDD"}, 300, 5, false);
        BaseMocaSessionManager remoteSpy = Mockito.spy(remoteManager);
        
        String newKey = localManager.createRemote("testuser");
        assertNotNull(newKey);
        
        SessionToken token = remoteSpy.validate(newKey);
        assertNotNull(token);
        String sid = token.getSessionId();
        assertNull(sid);
        
        Mockito.verify(remoteSpy,Mockito.times(0)).saveSession(Mockito.anyString(),
            (SessionData) Mockito.any());
        Mockito.verify(remoteSpy, Mockito.times(0)).checkSession(Mockito.anyString());
        Mockito.verify(remoteSpy, Mockito.times(0)).removeSession(Mockito.anyString());
        
        // Now, wait 6 seconds, and make sure the validation fails.
        Thread.sleep(6000L);
        assertNull(remoteManager.validate(newKey));
        
        // Verify that the appropriate number of save/check/remove calls happened.
        Mockito.verify(remoteSpy,Mockito.times(0)).saveSession(Mockito.anyString(),
            (SessionData) Mockito.any());
        Mockito.verify(remoteSpy, Mockito.times(0)).checkSession(Mockito.anyString());
        Mockito.verify(remoteSpy, Mockito.times(0)).removeSession(Mockito.anyString());
    }
    
    @Test
    public void testRemoteSessionWithWrongDomain() throws InterruptedException {
        BaseMocaSessionManager localManager = new InMemoryMocaSessionManager("BBB#FOO", null, 300, 300, false);
        // Make a session manager that allows 5 seconds for remote sessions
        BaseMocaSessionManager remoteManager = new InMemoryMocaSessionManager("AAA#ABCD", new String[] {"BBB#WXYZ", "CCC", "DDD"}, 300, 300, false);
        BaseMocaSessionManager remoteSpy = Mockito.spy(remoteManager);

        String newKey = localManager.createRemote("testuser");
        assertNotNull(newKey);
        assertNull(remoteManager.validate(newKey));
        
        // Verify that the appropriate number of save/check/remove calls happened.
        Mockito.verify(remoteSpy,Mockito.times(0)).saveSession(Mockito.anyString(),
            (SessionData) Mockito.any());
        Mockito.verify(remoteSpy, Mockito.times(0)).checkSession(Mockito.anyString());
        Mockito.verify(remoteSpy, Mockito.times(0)).removeSession(Mockito.anyString());
    }
}
