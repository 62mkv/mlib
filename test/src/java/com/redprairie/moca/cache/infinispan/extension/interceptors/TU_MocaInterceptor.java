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

package com.sam.moca.cache.infinispan.extension.interceptors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.commands.VisitableCommand;
import org.infinispan.commands.Visitor;
import org.infinispan.context.InvocationContext;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.responses.Response;
import org.infinispan.remoting.responses.SuccessfulResponse;
import org.infinispan.remoting.rpc.RpcManager;
import org.infinispan.remoting.transport.Address;
import org.junit.Test;
import org.mockito.Mockito;

import com.sam.moca.cache.infinispan.extension.MocaVisitor;
import com.sam.moca.cache.infinispan.extension.commands.visitable.ClusterRequestRuntimeException;
import com.sam.moca.cache.infinispan.extension.commands.visitable.GetNodeUrlsCommand;
import com.sam.moca.cache.infinispan.extension.commands.visitable.NodeResponse;
import com.sam.moca.cache.infinispan.extension.commands.visitable.NodeSpecificCommand;
import com.sam.moca.cache.infinispan.extension.commands.visitable.ScheduleJobsCommand;
import com.sam.moca.cluster.infinispan.InfinispanNode;

import static org.junit.Assert.*;

/**
 * Tests for MOCA Infinispan Interceptor
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_MocaInterceptor {
    
    /* #################################
     * MultipleResponseNodeCommand tests
     * #################################
     */
     
    // Scenario where the job is requested to be scheduled and is able
    // to be scheduled on the remote node
    @Test
    public void testJobResponseSuccessfulOnRemoteNode() throws Throwable {
        final List<String> jobIds = new ArrayList<String>(Arrays.asList("job1", "job2"));
        
        Map<String, NodeResponse> remoteResponse = new HashMap<String, NodeResponse>(2);
        remoteResponse.put(jobIds.get(0), NodeResponse.sucessfulEmptyResponse());
        remoteResponse.put(jobIds.get(1), NodeResponse.sucessfulEmptyResponse());
        
        Map<String, NodeResponse> localResponse = new HashMap<String, NodeResponse>(2);
        localResponse.put(jobIds.get(0), NodeResponse.noActionEmptyResponse());
        localResponse.put(jobIds.get(1), NodeResponse.noActionEmptyResponse());
        
        testJobResponseRunnerMultipleResponseCommand(jobIds, localResponse, remoteResponse);
    }
    
    // Scenario where we're working with multiple remote nodes and
    // one of them successfully can execute the start job operation
    @Test
    public void testMultipleRemoteNodeResponse() throws Throwable {
        final int numberOfRemoteNodes = 4;
        final List<String> jobIds = new ArrayList<String>(Arrays.asList("job1", "job2"));
        
        List<Map<String, NodeResponse>> remoteResponses = new ArrayList<Map<String, NodeResponse>>(numberOfRemoteNodes);
        for (int i = 1; i <= numberOfRemoteNodes; i++) {
            Map<String, NodeResponse> remoteResponse = new HashMap<String, NodeResponse>(2);
            // Third node will actually be successful
            if (i == 3) {
                remoteResponse.put(jobIds.get(0), NodeResponse.sucessfulEmptyResponse());
                remoteResponse.put(jobIds.get(1), NodeResponse.sucessfulEmptyResponse());
            }
            else {
                remoteResponse.put(jobIds.get(0), NodeResponse.noActionEmptyResponse());
                remoteResponse.put(jobIds.get(1), NodeResponse.noActionEmptyResponse());
            }
            remoteResponses.add(remoteResponse);
        }
        
        Map<String, NodeResponse> localResponse = new HashMap<String, NodeResponse>(2);
        localResponse.put(jobIds.get(0), NodeResponse.noActionEmptyResponse());
        localResponse.put(jobIds.get(1), NodeResponse.noActionEmptyResponse());
        
        testJobResponseRunnerMultipleResponseCommand(jobIds, localResponse, remoteResponses);
    }
    
    // Scenario where we're working with multiple remote nodes and
    // local node can successfully run
    @Test
    public void testMultipleRemoteNodeResponseLocalSuccessful() throws Throwable {
        final int numberOfRemoteNodes = 4;
        final List<String> jobIds = new ArrayList<String>(Arrays.asList("job1", "job2"));
        
        List<Map<String, NodeResponse>> remoteResponses = new ArrayList<Map<String, NodeResponse>>(numberOfRemoteNodes);
        for (int i = 1; i <= numberOfRemoteNodes; i++) {
            Map<String, NodeResponse> remoteResponse = new HashMap<String, NodeResponse>(2);
            remoteResponse.put(jobIds.get(0), NodeResponse.noActionEmptyResponse());
            remoteResponse.put(jobIds.get(1), NodeResponse.noActionEmptyResponse());
            remoteResponses.add(remoteResponse);
        }
        
        Map<String, NodeResponse> localResponse = new HashMap<String, NodeResponse>(2);
        localResponse.put(jobIds.get(0), NodeResponse.sucessfulEmptyResponse());
        localResponse.put(jobIds.get(1), NodeResponse.sucessfulEmptyResponse());
        
        testJobResponseRunnerMultipleResponseCommand(jobIds, localResponse, remoteResponses);
    }
    
    // Scenario where we're working with multiple remote notes and none
    // of them are able to run the operation, exception expected 
    @Test
    public void testMultipleRemoteNodeResponseUnsuccessful() throws Throwable {
        final int numberOfRemoteNodes = 4;
        final List<String> jobIds = new ArrayList<String>(Arrays.asList("job1", "job2"));
        
        List<Map<String, NodeResponse>> remoteResponses = new ArrayList<Map<String, NodeResponse>>(numberOfRemoteNodes);
        for (int i = 1; i <= numberOfRemoteNodes; i++) {
            Map<String, NodeResponse> remoteResponse = new HashMap<String, NodeResponse>(2);
            remoteResponse.put(jobIds.get(0), NodeResponse.noActionEmptyResponse());
            remoteResponse.put(jobIds.get(1), NodeResponse.noActionEmptyResponse());
            remoteResponses.add(remoteResponse);
        }
        
        Map<String, NodeResponse> localResponse = new HashMap<String, NodeResponse>(2);
        localResponse.put(jobIds.get(0), NodeResponse.noActionEmptyResponse());
        localResponse.put(jobIds.get(1), NodeResponse.noActionEmptyResponse());
        
        try {
            testJobResponseRunnerMultipleResponseCommand(jobIds, localResponse, remoteResponses);
            fail("JobRuntimeException should have occurred because the job action did not successfully " +
                 "occur on either the local node or remote nodes");
        }
        catch (ClusterRequestRuntimeException expected) {
            assertTrue(expected.getMessage().contains(jobIds.get(0)));
            assertTrue(expected.getMessage().contains(jobIds.get(1)));
        }
    }
    
    // Scenario where the job is requested to be scheduled and is able
    // to be scheduled on the local (origin node)
    @Test
    public void testJobResponseSuccessfulOnOriginNode() throws Throwable {
        final List<String> jobIds = new ArrayList<String>(Arrays.asList("job1", "job2"));
        
        Map<String, NodeResponse> remoteResponse = new HashMap<String, NodeResponse>(2);
        remoteResponse.put(jobIds.get(0), NodeResponse.noActionEmptyResponse());
        remoteResponse.put(jobIds.get(1), NodeResponse.noActionEmptyResponse());
        
        Map<String, NodeResponse> localResponse = new HashMap<String, NodeResponse>(2);
        localResponse.put(jobIds.get(0), NodeResponse.sucessfulEmptyResponse());
        localResponse.put(jobIds.get(1), NodeResponse.sucessfulEmptyResponse());
        
        testJobResponseRunnerMultipleResponseCommand(jobIds, localResponse, remoteResponse);
    }
    
    // Scenario where jobs are requested to be unscheduled but
    // neither can be unscheduled
    @Test
    public void testJobResponseUnsuccessful() throws Throwable {
        final List<String> jobIds = new ArrayList<String>(Arrays.asList("job1", "job2"));
        
        Map<String, NodeResponse> remoteResponse = new HashMap<String, NodeResponse>(2);
        remoteResponse.put(jobIds.get(0), NodeResponse.noActionEmptyResponse());
        remoteResponse.put(jobIds.get(1), NodeResponse.noActionEmptyResponse());
        
        Map<String, NodeResponse> localResponse = new HashMap<String, NodeResponse>();
        localResponse.put(jobIds.get(0), NodeResponse.noActionEmptyResponse());
        localResponse.put(jobIds.get(1), NodeResponse.noActionEmptyResponse());
        
        try {
            testJobResponseRunnerMultipleResponseCommand(jobIds, localResponse, remoteResponse);
            fail("JobRuntimeException should have occurred because the job action did not successfully " +
                 "occur on either the local node or remote node");
        }
        catch (ClusterRequestRuntimeException expected) {
            assertTrue(expected.getMessage().contains(jobIds.get(0)));
            assertTrue(expected.getMessage().contains(jobIds.get(1)));
        }
    }
    
    // Scenario where two jobs are requested to be scheduled but
    // only one is successful (should result in exception)
    @Test
    public void testJobResponseOneUnsuccesful() throws Throwable {
        final List<String> jobIds = new ArrayList<String>(Arrays.asList("job1", "job2"));
        
        Map<String, NodeResponse> remoteResponse = new HashMap<String, NodeResponse>(2);
        remoteResponse.put(jobIds.get(0), NodeResponse.sucessfulEmptyResponse());
        remoteResponse.put(jobIds.get(1), NodeResponse.noActionEmptyResponse());
        
        Map<String, NodeResponse> localResponse = new HashMap<String, NodeResponse>();
        localResponse.put(jobIds.get(0), NodeResponse.noActionEmptyResponse());
        localResponse.put(jobIds.get(1), NodeResponse.noActionEmptyResponse());
        
        try {
            testJobResponseRunnerMultipleResponseCommand(jobIds, localResponse, remoteResponse);
            fail("JobRuntimeException should have occurred because the job action did not successfully " +
                 "occur on either the local node or remote node");
        }
        catch (ClusterRequestRuntimeException expected) {
            // Only the second job requested failed
            assertTrue(expected.getMessage().contains(jobIds.get(1)));
            assertFalse(expected.getMessage().contains(jobIds.get(0)));
        }
    }
    
    @Test
    public void testJobResponseRemoteException() throws Throwable {
        final List<String> jobIds = new ArrayList<String>(Arrays.asList("job1", "job2"));
        
        Map<String, NodeResponse> remoteResponse = new HashMap<String, NodeResponse>(2);
        remoteResponse.put(jobIds.get(0), NodeResponse.exceptionEmptyResponse());
        remoteResponse.put(jobIds.get(1), NodeResponse.sucessfulEmptyResponse());
        
        Map<String, NodeResponse> localResponse = new HashMap<String, NodeResponse>();
        localResponse.put(jobIds.get(0), NodeResponse.noActionEmptyResponse());
        localResponse.put(jobIds.get(1), NodeResponse.noActionEmptyResponse());
        
        
        try {
            testJobResponseRunnerMultipleResponseCommand(jobIds, localResponse, remoteResponse);
            fail("JobRuntimeException should have occurred because the job action did not successfully " +
                 "occur on either the local node or remote node");
        }
        catch (ClusterRequestRuntimeException expected) {
            // Only the second job requested failed
            assertTrue(expected.getMessage().contains(jobIds.get(0)));
        }
    }
    
    @Test
    public void testJobResponseLocalException() throws Throwable {
        final List<String> jobIds = new ArrayList<String>(Arrays.asList("job1", "job2"));
        
        Map<String, NodeResponse> remoteResponse = new HashMap<String, NodeResponse>(2);
        remoteResponse.put(jobIds.get(0), NodeResponse.noActionEmptyResponse());
        remoteResponse.put(jobIds.get(1), NodeResponse.noActionEmptyResponse());
        
        Map<String, NodeResponse> localResponse = new HashMap<String, NodeResponse>();
        localResponse.put(jobIds.get(0), NodeResponse.exceptionEmptyResponse());
        localResponse.put(jobIds.get(1), NodeResponse.sucessfulEmptyResponse());
        
        try {
            testJobResponseRunnerMultipleResponseCommand(jobIds, localResponse, remoteResponse);
            fail("JobRuntimeException should have occurred because the job action did not successfully " +
                 "occur on either the local node or remote node");
        }
        catch (ClusterRequestRuntimeException expected) {
            // Only the first job requested failed
            assertTrue(expected.getMessage().contains(jobIds.get(0)));
            assertFalse(expected.getMessage().contains(jobIds.get(1)));
        }
    }
    
    @Test
    public void testScheduleJobOnlyOnRemoteNode() throws Throwable {
        final List<String> jobIds = new ArrayList<String>(Arrays.asList("job1", "job2"));
        
        Map<String, NodeResponse> remoteResponse = new HashMap<String, NodeResponse>(2);
        remoteResponse.put(jobIds.get(0), NodeResponse.sucessfulEmptyResponse());
        remoteResponse.put(jobIds.get(1), NodeResponse.sucessfulEmptyResponse());
        
        Map<String, NodeResponse> localResponse = new HashMap<String, NodeResponse>(); 

        List<Address> addresses = Arrays.asList(Mockito.mock(Address.class));
        RunnerMocks mocks = testJobResponseRunnerMultipleResponseCommand(jobIds, localResponse,
                remoteResponse, false, addresses);
        // Invoke remote should always occur, we just pass the appropriate addresses
        Mockito.verify(mocks._manager).invokeRemotely(Mockito.eq(addresses), Mockito.any(NodeSpecificCommand.class), Mockito.anyBoolean());
        // Invoke on the origin node should not have occurred so we can verify
        // acceptVisitor was never called.
        Mockito.verify(mocks._command, Mockito.never()).acceptVisitor(Mockito.any(InvocationContext.class),
                Mockito.any(Visitor.class));
    }
    
    @Test
    public void testScheduleJobOnlyOnRemoteNodeWithNoAction() throws Throwable {
        final List<String> jobIds = new ArrayList<String>(Arrays.asList("job1", "job2"));
        
        Map<String, NodeResponse> remoteResponse = new HashMap<String, NodeResponse>(2);
        remoteResponse.put(jobIds.get(0), NodeResponse.noActionEmptyResponse());
        remoteResponse.put(jobIds.get(1), NodeResponse.sucessfulEmptyResponse());
        
        Map<String, NodeResponse> localResponse = new HashMap<String, NodeResponse>(); 
        
        try {
            List<Address> addresses = Arrays.asList(Mockito.mock(Address.class));
            testJobResponseRunnerMultipleResponseCommand(jobIds, localResponse,
                    remoteResponse, false, addresses);
            fail("JobRuntimeException should have occurred because the job action did not successfully " +
                     "occur on the remote node and the request wasn't for the origin node");
        }
        catch (ClusterRequestRuntimeException expected) {
            // Only the first job requested failed
            assertTrue(expected.getMessage().contains(jobIds.get(0)));
            assertFalse(expected.getMessage().contains(jobIds.get(1)));
        }
    }
    
    /* ###############################
     * SingleResponseNodeCommand tests
     * ###############################
     */
    
    // Base test
    @Test
    public void testSingleResponseRpcBasic() throws Throwable {
        final NodeResponse localResponse = NodeResponse.sucessfulEmptyResponse();
        final NodeResponse remoteResponse = NodeResponse.sucessfulEmptyResponse();
        
        testJobResponseRunnerSingleResponseCommand(localResponse, remoteResponse);
    }
    
    // No exception when tasks fail
    @Test
    public void testSingleResponseRpcWithNoAction() throws Throwable {
        final NodeResponse localResponse = NodeResponse.noActionEmptyResponse();
        final NodeResponse remoteResponse = NodeResponse.noActionEmptyResponse();
        
        try {
            testJobResponseRunnerSingleResponseCommand(localResponse, remoteResponse);
        }
        catch (ClusterRequestRuntimeException expected) {
            fail("NO ACTION responses on SingleNodeResponseCommand should not throw exceptions");
        }
    }
    
    // Remote failure should throw and exception
    @Test
    public void testSingleResponseRpcWithRemoteFailure() throws Throwable {
        final String message = "OutOfMemoryError";
        final Exception ex = new RuntimeException(message);

        final NodeResponse localResponse = NodeResponse.noActionEmptyResponse();
        final NodeResponse remoteResponse = NodeResponse.exceptionResponse(ex);

        try {
            testJobResponseRunnerSingleResponseCommand(localResponse, remoteResponse);
            fail("A remote exception response should throw an exception!");
        }
        catch (ClusterRequestRuntimeException expected) {
            assertTrue(expected.getMessage().contains(message));
        }
    }
    
    // Remote failure should throw and exception
    @Test
    public void testSingleResponseRpcWithLocalFailure() throws Throwable {
        final String message = "OutOfMemoryError";
        final Exception ex = new RuntimeException(message);

        final NodeResponse localResponse = NodeResponse.exceptionResponse(ex);
        final NodeResponse remoteResponse = NodeResponse.noActionEmptyResponse();

        try {
            testJobResponseRunnerSingleResponseCommand(localResponse, remoteResponse);
            fail("A local exception response should throw an exception!");
        }
        catch (ClusterRequestRuntimeException expected) {
            assertTrue(expected.getMessage().contains(message));
        }
    }
   
    private void testJobResponseRunnerMultipleResponseCommand(List<String> jobIds, 
            final Map<String, NodeResponse> localResponse,
            final Map<String, NodeResponse> remoteResponses) throws Throwable {
        testJobResponseRunnerMultipleResponseCommand(jobIds, localResponse, new ArrayList<Map<String, NodeResponse>>(Arrays.asList(remoteResponses)));
    }

    private void testJobResponseRunnerMultipleResponseCommand(List<String> jobIds,
            final Map<String, NodeResponse> localResponse,
            final List<Map<String, NodeResponse>> remoteResponses) throws Throwable {
        testJobResponseRunnerMultipleResponseCommand(jobIds, localResponse, remoteResponses, true, null);
    }
    
    private RunnerMocks testJobResponseRunnerMultipleResponseCommand(List<String> jobIds,
            final Map<String, NodeResponse> localResponse,
            Map<String, NodeResponse> remoteResponses,
            boolean executeOnOrigin,
            List<Address> requestAddresses) throws Throwable {
        return testJobResponseRunnerMultipleResponseCommand(jobIds, localResponse,
                new ArrayList<Map<String,NodeResponse>>(Arrays.asList(remoteResponses)),
                executeOnOrigin, requestAddresses);
    }
    
    private void testJobResponseRunnerSingleResponseCommand(final NodeResponse localResponse,
            final NodeResponse remoteResponses) throws Throwable {
        testJobResponseRunnerSingleResponseCommand(localResponse, Arrays.asList(remoteResponses));
    }
    
    private void testJobResponseRunnerSingleResponseCommand(final NodeResponse localResponse,
            final List<NodeResponse> remoteResponses) throws Throwable {
        testJobResponseRunnerSingleResponseCommand(localResponse, remoteResponses, true, null);
    }
    
    /**
     * 
     * @param jobIds
     * @param localResponse response to return on command
     * @param remoteResponses
     * @param executeOnOrigin whether command is requested for any address
     * @param requestAddresses command request address
     * @return
     * @throws Throwable
     */
    @SuppressWarnings("unchecked")
    private RunnerMocks testJobResponseRunnerMultipleResponseCommand(List<String> jobIds,
            final Map<String, NodeResponse> localResponse,
            final List<Map<String, NodeResponse>> remoteResponses,
            boolean executeOnOrigin,
            List<Address> requestAddresses) throws Throwable {
        InvocationContext ctx = Mockito.mock(InvocationContext.class);
        Mockito.when(ctx.isOriginLocal()).thenReturn(true);
        
        ScheduleJobsCommand command = Mockito.mock(ScheduleJobsCommand.class);
        Cache<String, String> mockCache = Mockito.mock(Cache.class);
        AdvancedCache<String, String> advMockCache = Mockito.mock(AdvancedCache.class);
        Address mockOriginAddress = Mockito.mock(Address.class);
        EmbeddedCacheManager cacheManager = Mockito.mock(EmbeddedCacheManager.class);
        
        Mockito.when(command.isRequestedForAddress(Mockito.any(Address.class))).thenReturn(executeOnOrigin);
        Mockito.when(command.getRequestAddresses()).thenReturn(requestAddresses);
        // Local response mock is here
        Mockito.when(command.acceptVisitor(Mockito.any(InvocationContext.class), Mockito.any(MocaVisitor.class)))
               .thenReturn(localResponse);
        
        RpcManager mockRpcManager = Mockito.mock(RpcManager.class);
        Mockito.when(mockCache.getAdvancedCache()).thenReturn(advMockCache);
        Mockito.when(mockCache.getCacheManager()).thenReturn(cacheManager);
        Mockito.when(cacheManager.getAddress()).thenReturn(mockOriginAddress);
        Mockito.when(advMockCache.getRpcManager()).thenReturn(mockRpcManager);
        
        // Build out mocked remote node responses
        // Infinispan will wrap the response in a SuccessfulResponse
        Map<Address, Response> mockResponse = new HashMap<Address, Response>();
        for (Map<String, NodeResponse> remoteResponse : remoteResponses) {
            SuccessfulResponse sResponse = Mockito.mock(SuccessfulResponse.class);
            Mockito.when(sResponse.isSuccessful()).thenReturn(true);
            Mockito.when(sResponse.isValid()).thenReturn(true);
            Mockito.when(sResponse.getResponseValue()).thenReturn(remoteResponse);
            mockResponse.put(Mockito.mock(Address.class), sResponse);
        }
        
        Mockito.when(mockRpcManager.invokeRemotely(Mockito.eq(requestAddresses),Mockito.eq(command), Mockito.anyBoolean())).thenReturn(mockResponse);
        
        InfinispanNode mockNode = Mockito.mock(InfinispanNode.class);
        Address mockAddress = Mockito.mock(Address.class);
        Mockito.when(mockNode.getAddress()).thenReturn(mockAddress);
        
        // Need to stub out the next invoke call to return the local response
        MocaInterceptor interceptor = new MocaInterceptor(mockCache, mockNode);
        interceptor.visitScheduleJobsCommand(ctx, command);
        
        // Return mock RPC manager for verifying behavior
        return new RunnerMocks(command, mockRpcManager);
    }
    
    /**
     * 
     * @param localResponse response to return on command
     * @param remoteResponses
     * @param executeOnOrigin whether command is requested for any address
     * @param requestAddresses command request address
     * @return
     * @throws Throwable
     */
    @SuppressWarnings("unchecked")
    private RunnerMocks testJobResponseRunnerSingleResponseCommand(final NodeResponse localResponse,
            final List<NodeResponse> remoteResponses,
            boolean executeOnOrigin,
            List<Address> requestAddresses) throws Throwable {
        InvocationContext ctx = Mockito.mock(InvocationContext.class);
        Mockito.when(ctx.isOriginLocal()).thenReturn(true);
        
        GetNodeUrlsCommand command = Mockito.mock(GetNodeUrlsCommand.class);
        Cache<String, String> mockCache = Mockito.mock(Cache.class);
        AdvancedCache<String, String> advMockCache = Mockito.mock(AdvancedCache.class);
        Address mockOriginAddress = Mockito.mock(Address.class);
        EmbeddedCacheManager cacheManager = Mockito.mock(EmbeddedCacheManager.class);
        
        Mockito.when(command.isRequestedForAddress(Mockito.any(Address.class))).thenReturn(executeOnOrigin);
        Mockito.when(command.getRequestAddresses()).thenReturn(requestAddresses);
        // Local response mock is here
        Mockito.when(command.acceptVisitor(Mockito.any(InvocationContext.class), Mockito.any(MocaVisitor.class)))
               .thenReturn(localResponse);
        
        RpcManager mockRpcManager = Mockito.mock(RpcManager.class);
        Mockito.when(mockCache.getAdvancedCache()).thenReturn(advMockCache);
        Mockito.when(mockCache.getCacheManager()).thenReturn(cacheManager);
        Mockito.when(cacheManager.getAddress()).thenReturn(mockOriginAddress);
        Mockito.when(advMockCache.getRpcManager()).thenReturn(mockRpcManager);
        
        // Build out mocked remote node responses
        // Infinispan will wrap the response in a SuccessfulResponse
        Map<Address, Response> mockResponse = new HashMap<Address, Response>();
        for (NodeResponse remoteResponse : remoteResponses) {
            SuccessfulResponse sResponse = Mockito.mock(SuccessfulResponse.class);
            Mockito.when(sResponse.isSuccessful()).thenReturn(true);
            Mockito.when(sResponse.isValid()).thenReturn(true);
            Mockito.when(sResponse.getResponseValue()).thenReturn(remoteResponse);
            mockResponse.put(Mockito.mock(Address.class), sResponse);
        }
        
        Mockito.when(mockRpcManager.invokeRemotely(Mockito.eq(requestAddresses),
                Mockito.eq(command), Mockito.anyBoolean())).thenReturn(mockResponse);
        
        InfinispanNode mockNode = Mockito.mock(InfinispanNode.class);
        Address mockAddress = Mockito.mock(Address.class);
        Mockito.when(mockNode.getAddress()).thenReturn(mockAddress);
        
        // Need to stub out the next invoke call to return the local response
        MocaInterceptor interceptor = new MocaInterceptor(mockCache, mockNode);
        interceptor.visitGetNodeUrlsCommand(ctx, command);
        
        // Return mock RPC manager for verifying behavior
        return new RunnerMocks(command, mockRpcManager);
    }
    
    // For verifying behavior on mocks
    private static class RunnerMocks {
        public RunnerMocks(VisitableCommand command, RpcManager manager) {
            _command = command;
            _manager = manager;
        }
        
        private final VisitableCommand _command;
        private final RpcManager _manager;
    }

}
