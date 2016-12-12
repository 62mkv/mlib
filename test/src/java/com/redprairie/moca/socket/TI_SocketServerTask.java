/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.redprairie.moca.client.ConnectionFailedException;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * Integration tests for the SocketServerTask
 * 
 * Copyright (c) 2013 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TI_SocketServerTask extends AbstractMocaTestCase {
    
    // Tests that when -s is used unique session IDs are generated
    @Test
    public void testUniqueSessionIds() throws Exception {
        // We set the pool size (-P 5) equal to the number of requests just because
        // it will start reusing the threads after that and we want to verify uniqueness.
        RunResults results = runAndGetSessions(new String[]{"-P", NUM_REQUESTS.toString(),
                "-s", "-p", "0", "-c", TEST_COMMAND}, NUM_REQUESTS);
        
        
        List<String> checks = new ArrayList<String>(results._sessionIds.size());
        // Verify each session starts with the task prefix (thread name in this case)
        // and that they're all unique
        for (String sessionId : results._sessionIds) {
            assertTrue(sessionId.startsWith(results._taskName + "-"));
            assertFalse("The session was not unique", checks.contains(sessionId));
            checks.add(sessionId);
        }
    }
    
    // Tests that when -s is NOT used then the session IDs are just inherited and are the same
    @Test
    public void testSameSessionIds() throws Exception {
        // The session ID should just be inherited in this case
        String expectedSessionId = ServerUtils.getCurrentContext().getSession().getSessionId();
        RunResults results = runAndGetSessions(new String[]{"-p", "0", "-c", TEST_COMMAND}, NUM_REQUESTS);
        for (String sessionId : results._sessionIds) {
            assertEquals(expectedSessionId, sessionId);
        }
    }
    
    // Runs the SocketServerTask with the given arguments and the specified number
    // of requests. This will return a list of session IDs used on the server side
    // in the RunResults.
    private RunResults runAndGetSessions(String[] args, int requests) throws Exception {
        // Startup the SocketServerTask
        SocketServerTask serverTask = new SocketServerTask(args);
        Thread task = new Thread(serverTask);
        task.start();
        int port = 0;
        while (port == 0) {
            Thread.sleep(5);
            port = serverTask.getPort();
        }
        
        // Act as a client and make some requests
        InetSocketAddress addr = new InetSocketAddress("localhost", port);
        if (addr.isUnresolved()) {
            throw new ConnectionFailedException("Unable to resolve address");
        }
        
        SocketChannel _sc = SocketChannel.open(addr);
        Socket _s = _sc.socket();
        
        OutputStream _out = new BufferedOutputStream(_s.getOutputStream()); 
        InputStream _in = new BufferedInputStream(_s.getInputStream());
        // We're just pushing a single byte * the number of requests, for each one
        // it will delegate control to the calling command, the calling command (see TEST_COMMAND)
        // will just get the session ID and print it back
        for (int i = 0; i < requests; i++) {
            _out.write(1);
        }
        _out.flush();

        // Read all the responses which should be the session ID
        // used for each request
        List<String> sessionIds = new ArrayList<String>(3);
        for (int i = 0; i < requests; i++) {
            byte[] bytes = new byte[1028];
            int bytesRead = _in.read(bytes);
            assertTrue(bytesRead > 0);
            sessionIds.add(new String(bytes, Charset.defaultCharset()).trim());
        }
        
        RunResults results = new RunResults(task.getName(), sessionIds);
        task.interrupt();
        
        return results;
    }
    
    private static class RunResults {
        
        private RunResults(String taskName, List<String> sessionIds) {
            _taskName = taskName;
            _sessionIds = sessionIds;
        }
        
        private final List<String> _sessionIds;
        private final String _taskName;
    }
    
    
    // The number of client requests to make in the tests
    private static final Integer NUM_REQUESTS = 5;
    
    // This is the test command that the SocketServerTask calls, all
    // it does is gets the current session ID and writes it back to the output
    // stream on the socket.
    private static final String TEST_COMMAND = "[[ id = com.redprairie.moca.server.ServerUtils.getCurrentContext().getSession().getSessionId(); socket.getOutputStream().write(id.getBytes()) ]]";
}
