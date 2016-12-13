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

package com.redprairie.moca.cache.infinispan.extension.commands.visitable;

import java.util.Map;

import org.infinispan.remoting.transport.Address;

/**
 * This is an exception meant to be thrown in the instance when an
 * action is expected to be performed on a cluster and the action is
 * performed on <b>any</b> node.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author j1014808
 */
public class NoActionClusterRequestException extends ClusterRequestRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Standard constructor to set the desired message
     * @param msg The message
     */
    public NoActionClusterRequestException(String msg) {
        super(msg);
    }
    
    /**
     * Construct an exception from map of nodes to exception status responses.
     * @param exceptionResponses A map of Address to NodeResponse that have an Exception status.
     */
    public static NoActionClusterRequestException fromNodeResponses(Map<Address, NodeResponse> exceptionResponses) { 
        return new NoActionClusterRequestException(getNoActionRequestsMesssage(exceptionResponses));
    }
    
    /**
     * Construct an exception from map of (nodes) to (map of exception request ids to exception responses).
     * @param exceptionResponses A map of Address of node to Map of requests to Exception status responses.
     */
    public static NoActionClusterRequestException fromNodeResponsesMap(Map<Address, Map<String, NodeResponse>> exceptionResponses) { 
        return new NoActionClusterRequestException(getNoActionRequestsMesssageMap(exceptionResponses));
    }
    
    /*
     * Get an exception message from Node responses that returned a NodeResponse
     */
    private static String getNoActionRequestsMesssage(Map<Address, NodeResponse> exceptionResponses) {
        final StringBuilder msg = new StringBuilder();
        
        if (exceptionResponses != null && exceptionResponses.size() > 0) {
            msg.append("The following request(s) resulted in no actions:\n");
            
            for (Map.Entry<Address, NodeResponse> exceptionResponse : exceptionResponses.entrySet()) {
                msg.append("Request on node [")
                    .append(exceptionResponse.getKey())
                    .append("] was invalid and resulted in no action being taken.\n");
            }
        }
        
        return msg.toString();
    }

    /*
     * Get an exception message from Node responses that returned a map of request IDs to NodeResponses
     */
    private static String getNoActionRequestsMesssageMap(Map<Address, Map<String, NodeResponse>> exceptionResponses) {
        final StringBuilder msg = new StringBuilder();
        
        if (exceptionResponses != null && exceptionResponses.size() > 0) {
            msg.append("The following request(s) resulted in no actions:\n");
            
            for (Map.Entry<Address, Map<String, NodeResponse>> exceptionResponse : exceptionResponses.entrySet()) {
                final Address node = exceptionResponse.getKey();
                final Map<String, NodeResponse> response = exceptionResponse.getValue();
                
                for (Map.Entry<String, NodeResponse> request : response.entrySet()) {
                    msg.append("Request [")
                        .append(request.getKey())
                        .append("] on node [")
                        .append(node)
                        .append("] was invalid and resulted in no action being taken.\n");
                }
            }
        }
        
        return msg.toString();
    }
}
