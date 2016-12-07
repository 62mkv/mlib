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

package com.redprairie.moca.cache.infinispan.extension.interceptors;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.context.InvocationContext;
import org.infinispan.interceptors.base.BaseCustomInterceptor;
import org.infinispan.remoting.responses.CacheNotFoundResponse;
import org.infinispan.remoting.responses.Response;
import org.infinispan.remoting.responses.SuccessfulResponse;
import org.infinispan.remoting.rpc.RpcManager;
import org.infinispan.remoting.transport.Address;

import com.redprairie.moca.cache.infinispan.extension.MocaVisitor;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.ClusterRequestRuntimeException;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.GetNodeUrlsCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.MultipleResponseNodeCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.NoActionClusterRequestException;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.NodeResponse;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.RestartServerCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.RestartTasksCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.ScheduleJobsCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.SingleResponseNodeCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.StartTasksCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.StopTasksCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.UnscheduleJobsCommand;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.infinispan.InfinispanNode;

/**
 * This is the MOCA interceptor
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaInterceptor extends BaseCustomInterceptor implements MocaVisitor {
    
    public MocaInterceptor(Node localNode) {
        super();
        _localNode = localNode;
    }
    
    // For testing to mock the cache
    MocaInterceptor(Cache<?, ?> cache, Node localNode) {
        this.cache = cache;
        this._localNode = localNode;
    }
    
    // @see com.redprairie.moca.cache.infinispan.extension.MocaVisitor#visitRestartServerCommand(org.infinispan.context.InvocationContext, com.redprairie.moca.cache.infinispan.extension.commands.visitable.RestartServerCommand)
    @Override
    public Object visitRestartServerCommand(InvocationContext ctx,
        RestartServerCommand command) throws Throwable {
        if (ctx.isOriginLocal()) {
            // Note that the usual replication interceptor will not handle this 
            // new command! So we'd need to replicate it here if needed.
            cache.getAdvancedCache().getRpcManager().broadcastRpcCommand(command, true);
        }

        return super.invokeNextInterceptor(ctx, command);
    }
    
    // @see com.redprairie.moca.cache.infinispan.extension.MocaVisitor#visitStartJobCommand(org.infinispan.context.InvocationContext, com.redprairie.moca.cache.infinispan.extension.commands.visitable.StopJobCommand)
    @Override
    public Object visitScheduleJobsCommand(InvocationContext ctx,
            ScheduleJobsCommand command) throws Throwable {
        return handleMultipleNodeResponseCommand(ctx, command);
    }
         
    // @see com.redprairie.moca.cache.infinispan.extension.MocaVisitor#visitStopJobCommand(org.infinispan.context.InvocationContext, com.redprairie.moca.cache.infinispan.extension.commands.visitable.StopJobCommand)
    @Override
    public Object visitUnscheduleJobsCommand(InvocationContext ctx,
            UnscheduleJobsCommand command) throws Throwable {
        return handleMultipleNodeResponseCommand(ctx, command);
    }
    
    // @see com.redprairie.moca.cache.infinispan.extension.MocaVisitor#visitStartTasksCommand(org.infinispan.context.InvocationContext, com.redprairie.moca.cache.infinispan.extension.commands.visitable.UnscheduleJobsCommand)
     @Override
     public Object visitStartTasksCommand(InvocationContext ctx,
             StartTasksCommand command) throws Throwable {
         return handleMultipleNodeResponseCommand(ctx, command);
     }

     // @see com.redprairie.moca.cache.infinispan.extension.MocaVisitor#visitStopTasksCommand(org.infinispan.context.InvocationContext, com.redprairie.moca.cache.infinispan.extension.commands.visitable.UnscheduleJobsCommand)
     @Override
     public Object visitStopTasksCommand(InvocationContext ctx,
             StopTasksCommand command) throws Throwable {
         return handleMultipleNodeResponseCommand(ctx, command);
     }

     // @see com.redprairie.moca.cache.infinispan.extension.MocaVisitor#visitRestartTasksCommand(org.infinispan.context.InvocationContext, com.redprairie.moca.cache.infinispan.extension.commands.visitable.UnscheduleJobsCommand)
     @Override
     public Object visitRestartTasksCommand(InvocationContext ctx,
             RestartTasksCommand command) throws Throwable {
         return handleMultipleNodeResponseCommand(ctx, command);
     }
    
     // @see com.redprairie.moca.cache.infinispan.extension.MocaVisitor#visitGetNodeUrlsCommand(org.infinispan.context.InvocationContext, com.redprairie.moca.cache.infinispan.extension.commands.visitable.GetNodeUrlsCommand)
     @Override
     public Object visitGetNodeUrlsCommand(InvocationContext ctx,
             GetNodeUrlsCommand command) throws Throwable {
         return handleSingleNodeResponseCommand(ctx, command);
     }
     
    // Handles command requests that return NodeResponses for multiple request Ids
    @SuppressWarnings("unchecked")
    private Object handleMultipleNodeResponseCommand(InvocationContext ctx, MultipleResponseNodeCommand command)
            throws Throwable {
        if (ctx.isOriginLocal() && cache.getAdvancedCache().getRpcManager() != null) {
            final RpcManager manager = cache.getAdvancedCache().getRpcManager();
            
            // Handle remote broadcasting here
            final Map<Address, Response> remoteResponses = 
                    manager.invokeRemotely(command.getRequestAddresses(), command, true);
            _logger.debug("Received remote responses: " + remoteResponses);

            // Command gets executed locally here
            // It's possible the command has been setup to not be executed on
            // the origin node so we check for that.
            Map<Address, Map<String, NodeResponse>> localResults = null;
            if (command.isRequestedForAddress(cache.getCacheManager().getAddress())) {
                final Map<String, NodeResponse> localResponse = (Map<String, NodeResponse>) super.invokeNextInterceptor(ctx, command);
                localResults = Collections.singletonMap(((InfinispanNode)_localNode).getAddress(), localResponse);
                _logger.debug("Received local response: " + localResults);
            }
            
            // Aggregate the local and remote results, checking for exceptions
            return handleNodeResponseMaps(command.getRequestIds(), ((InfinispanNode)_localNode).getAddress(), localResults, remoteResponses);
        }
        else {
            // Remote node or non clustered execution is here
            return super.invokeNextInterceptor(ctx, command);
        }
    }
    
    // Handles command requests that return single NodeResponse
    private Object handleSingleNodeResponseCommand(InvocationContext ctx, SingleResponseNodeCommand command)
            throws Throwable {
        final RpcManager manager = cache.getAdvancedCache().getRpcManager();
        if (ctx.isOriginLocal() && manager != null) {
            
            // Handle remote broadcasting here
            final Map<Address, Response> remoteResponses = 
                manager.invokeRemotely(command.getRequestAddresses(), command, true);
            _logger.debug("Received remote responses: " + remoteResponses);

            // Command gets executed locally here
            // It's possible the command has been setup to not be executed on
            // the origin node so we check for that.
            Map<Address, NodeResponse> localResults = null;
            if (command.isRequestedForAddress(cache.getCacheManager().getAddress())) {
                final NodeResponse localResponse = (NodeResponse) super.invokeNextInterceptor(ctx, command);
                localResults = Collections.singletonMap(((InfinispanNode)_localNode).getAddress(), localResponse);
                _logger.debug("Received local response: " + localResults);
            }
            
            // Aggregate the local and remote results, checking for exceptions
            return handleNodeResponses(((InfinispanNode)_localNode).getAddress(), localResults, remoteResponses);
        }
        else {
            // Remote node or non clustered execution is here
            return super.invokeNextInterceptor(ctx, command);
        }
    }

    /**
     * Handle remote and local responses for RPCs with multiple requests/responses i.e. MultipleResponseNodeCommand
     * @param requestIds
     * @param localAddress
     * @param localResponse
     * @param remoteResponses
     * @return
     * @throws NoActionClusterRequestException if there are requests which did not succeed on any node.
     */
    @SuppressWarnings("unchecked")
    private Map<Address, Map<String, NodeResponse>> handleNodeResponseMaps(List<String> requestIds,
                                                                Address localAddress,
                                                                Map<Address, Map<String, NodeResponse>> localResponse, 
                                                                Map<Address, Response> remoteResponses) throws NoActionClusterRequestException {
        final Map<Address, Map<String, NodeResponse>> exceptionResponses = new HashMap<Address, Map<String, NodeResponse>>();
        final Map<Address, Map<String, NodeResponse>> noActionResponses = new HashMap<Address, Map<String, NodeResponse>>();
        final Map<Address, Map<String, NodeResponse>> requestsCompleted = new HashMap<Address, Map<String, NodeResponse>>();
        
        // Check the local responses
        if (localResponse != null) {
            for (Map.Entry<Address, Map<String, NodeResponse>> localEntry : localResponse.entrySet()) {
                filterNodeResponseMap(localEntry.getKey(), localEntry.getValue(), exceptionResponses, noActionResponses, requestsCompleted);
            }
        }

        // Check the remote responses
        for (Map.Entry<Address, Response> remoteResponseEntry : remoteResponses.entrySet()) {
            final Address remoteAddress = remoteResponseEntry.getKey();
            final Response remoteValue = remoteResponseEntry.getValue();
            
            // Infinispan wraps our responses with their own so we need to peel it
            if (remoteValue instanceof SuccessfulResponse) {
                final Object remoteValues = ((SuccessfulResponse) remoteValue).getResponseValue();
                
                if (!(remoteValues instanceof Map<?, ?>)) {
                    throw new ClusterRequestRuntimeException("MultipleResponseNodeCommand must return Map<String, NodeResponse> but was: " 
                                                            + remoteValue.getClass().toString());
                }
                
                filterNodeResponseMap(remoteAddress, (Map<String, NodeResponse>) remoteValues, exceptionResponses, noActionResponses,  requestsCompleted);
            }
            else if (remoteValue instanceof CacheNotFoundResponse) {
                _logger.debug("Node not ready to receive RPC commands - " + remoteValue.toString());
            }
            else {
                throw new ClusterRequestRuntimeException("Illegal response type: " + remoteValue.getClass().toString());
            }
        }
        
        if (exceptionResponses.size() > 0) {
            throw ClusterRequestRuntimeException.fromNodeResponsesMap(exceptionResponses);
        }
        
        checkEachRequestHasAtLeastOneSuccess(requestIds, requestsCompleted, noActionResponses);
        
        return requestsCompleted;
    }

    /**
     * Handle remote and local responses for RPCs with single response i.e. SingleResponseNodeCommand
     * @param localAddress
     * @param localResponse
     * @param remoteResponses
     * @return
     */
    private Map<Address, NodeResponse> handleNodeResponses(Address localAddress,
                                                 Map<Address, NodeResponse> localResponse, 
                                                 Map<Address, Response> remoteResponses) {
        final Map<Address, NodeResponse> exceptionResponses = new HashMap<Address, NodeResponse>();
        final Map<Address, NodeResponse> noActionResponses = new HashMap<Address, NodeResponse>();
        final Map<Address, NodeResponse> requestsCompleted = new HashMap<Address, NodeResponse>();
        
        // Check the local responses
        if (localResponse != null) {
            for (Map.Entry<Address, NodeResponse> localEntry : localResponse.entrySet()) {
                filterNodeResponse(localEntry.getKey(), localEntry.getValue(), exceptionResponses, noActionResponses, requestsCompleted);
            }
        }

        // Check the remote responses
        for (Map.Entry<Address, Response> remoteResponseEntry : remoteResponses.entrySet()) {
            Response remoteValue = remoteResponseEntry.getValue();
            
            // Infinispan wraps our responses with their own so we need to peel it
            if (remoteValue instanceof SuccessfulResponse) {
                Object innerResponse = ((SuccessfulResponse) remoteValue).getResponseValue();
                if (!(innerResponse instanceof NodeResponse)) {
                    throw new ClusterRequestRuntimeException("SingleResponseNodeCommand must return NodeResponse but was: " 
                                                            + remoteValue.getClass().toString());
                }
                
                filterNodeResponse(remoteResponseEntry.getKey(), (NodeResponse) innerResponse, exceptionResponses, noActionResponses, requestsCompleted);
            }
            else if (remoteValue instanceof CacheNotFoundResponse) {
                _logger.debug("Node not ready to receive RPC commands - " + remoteValue.toString());
            }
            else {
                throw new ClusterRequestRuntimeException("Illegal response type: " + remoteValue.getClass().toString());
            }
        }
        
        // Check if we had some failed requests or exceptions
        if (exceptionResponses.size() > 0) {
            throw ClusterRequestRuntimeException.fromNodeResponses(exceptionResponses);
        }
        
        return requestsCompleted;
    }
    
    /**
     * Check that each multiple node request succeeded on at least one node.
     * @param requestIds list of request ids
     * @param requestsCompleted requests which were successful
     * @param noActionResponses requests which received no response
     * @throws NoActionClusterRequestException if there is a requests which did not succeed on any node
     */
    private void checkEachRequestHasAtLeastOneSuccess(List<String> requestIds,
                                                     Map<Address, Map<String, NodeResponse>> requestsCompleted,
                                                     Map<Address, Map<String, NodeResponse>> noActionResponses) throws NoActionClusterRequestException {
        final Set<String> failedRequests = new HashSet<String>();
        final Set<String> successfulRequestIds = new HashSet<String>();
        
        // get all completed request ids
        for (Map.Entry<Address, Map<String, NodeResponse>> nodeEntry : requestsCompleted.entrySet()) {
            final Map<String, NodeResponse> requests = nodeEntry.getValue();
            
            for (Map.Entry<String, NodeResponse> requestEntry : requests.entrySet()) {
                final NodeResponse requestResponse = requestEntry.getValue();
                if (requestResponse.getStatus().isSuccessful()) {
                    successfulRequestIds.add(requestEntry.getKey());
                }
            }
            
        }
        
        // iterate all failed requests and check to see that there is at least one success for each request
        for (Map.Entry<Address, Map<String, NodeResponse>> nodeEntry : noActionResponses.entrySet()) {
            final Map<String, NodeResponse> requests = nodeEntry.getValue();
            for (Map.Entry<String, NodeResponse> requestEntry : requests.entrySet()) {
                final String requestId = requestEntry.getKey();
                if (!successfulRequestIds.contains(requestId)) {
                    failedRequests.add(requestId);
                }
            }
        }
        
        if (failedRequests.size() > 0) {
            final StringBuilder noActionException = new StringBuilder();
            for (String reqId : failedRequests) {
                noActionException.append("Request for ID [")
                .append( reqId)
                .append("] was invalid as no action was taken on any node\n");
            }
            
            throw new NoActionClusterRequestException(noActionException.toString());
        }
    }
    
    /**
     * Checks status of response and filters them based on status.
     * Will map requests to each category if at least one requestId
     * ended in that status. That means that if one request failed and one 
     * succeeded, then that request will both be in failed and successful maps.
     * @param address The address of the response
     * @param innerResponse The given node response
     * @param exceptionRequests A map of failed requests to add to
     * @param noActionRequests A map of no action requests to add to
     * @param successfulRequests A map of success requests to add to
     */
    private void filterNodeResponseMap(final Address address,
                                      final Map<String, NodeResponse> innerResponse,
                                      final Map<Address,  Map<String, NodeResponse>> exceptionRequests,
                                      final Map<Address,  Map<String, NodeResponse>> noActionRequests,
                                      final Map<Address, Map<String, NodeResponse>> successfulRequests) {
        for (Map.Entry<String, NodeResponse> entry : innerResponse.entrySet()) {
            final String requestId = entry.getKey();
            final NodeResponse response = entry.getValue();
            final NodeResponse.Status status = response.getStatus();
            
            switch (status) {
                case SUCCESS: {
                    addNodeRequestResponse(address, requestId, response, successfulRequests);
                    break;
                }
                case NO_ACTION: {
                    addNodeRequestResponse(address, requestId, response, noActionRequests);
                    break;
                }
                case EXCEPTION: {
                    addNodeRequestResponse(address, requestId, response, exceptionRequests);
                    break;
                }
                default: {
                    throw new ClusterRequestRuntimeException("Unkown response status: " + status);
                }
            }
        }
    }

    /**
     * Add a response to a request to the request map. If it's the first response for the address
     * then create a new map first.
     * @param address the node address
     * @param requestId the request ID for the action
     * @param response the response to the request ID
     * @param filterResponseMap the filter map e.g., successful requests 
     */
    private void addNodeRequestResponse(final Address address,
                                        final String requestId,
                                        final NodeResponse response,
                                        final Map<Address, Map<String, NodeResponse>> filterResponseMap) {
        final Map<String, NodeResponse> resp = new HashMap<String, NodeResponse>();
        resp.put(requestId, response);
        
        if (filterResponseMap.containsKey(address)) {
            filterResponseMap.get(address).put(requestId, response);
        }
        else {
            filterResponseMap.put(address, resp);
        }
    }
    
    /**
     * Checks the status of a responses and filters them based on status.
     * @param address The address from the response
     * @param response The given Node Response
     * @param exceptionRequests A map to add failed requests to
     * @param noActionRequests A map to add no action requests to
     * @param successfulRequests A list of successful requests to add to
     */
    private void filterNodeResponse(final Address address,
                                   final NodeResponse response,
                                   final Map<Address, NodeResponse> exceptionRequests,
                                   final Map<Address, NodeResponse> noActionRequests,
                                   final Map<Address, NodeResponse> successfulRequests) {
        final NodeResponse.Status status = response.getStatus();
        
        switch (status) {
            case SUCCESS: {
                successfulRequests.put(address, response);
                break;
            }
            case NO_ACTION: {
                noActionRequests.put(address, response);
                break;
            }
            case EXCEPTION: {
                exceptionRequests.put(address, response);
                break;
            }
            default: {
                throw new ClusterRequestRuntimeException("Unkown response status: " + status);
            }
        }
    }

    private static final Logger _logger = Logger.getLogger(MocaInterceptor.class);
    private final Node _localNode;
}
