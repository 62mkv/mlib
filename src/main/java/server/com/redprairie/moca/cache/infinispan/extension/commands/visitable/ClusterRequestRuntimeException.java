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
 * A runtime exception indicating there was an issue
 * fulfilling a request across the cluster.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class ClusterRequestRuntimeException extends RuntimeException {
    
    /**
     * Standard constructor to set the desired message
     * @param msg The message
     */
    public ClusterRequestRuntimeException(String msg) {
        super(msg);
    }
    
    /**
     * Construct an exception from map of nodes to exception status responses.
     * @param exceptionResponses A map of Address to NodeResponse that have an Exception status.
     */
    public static ClusterRequestRuntimeException fromNodeResponses(Map<Address, NodeResponse> exceptionResponses) { 
        return new ClusterRequestRuntimeException(getFailedRequestsMesssage(exceptionResponses));
    }
    
    /**
     * Construct an exception from map of (nodes) to (map of exception request ids to exception responses).
     * @param exceptionResponses A map of Address of node to Map of requests to Exception status responses.
     */
    public static ClusterRequestRuntimeException fromNodeResponsesMap(Map<Address, Map<String, NodeResponse>> exceptionResponses) { 
        return new ClusterRequestRuntimeException(getFailedRequestsMesssageMap(exceptionResponses));
    }
    
    /*
     * Get an exception message from Node responses that returned a NodeResponse
     */
    private static String getFailedRequestsMesssage(Map<Address, NodeResponse> exceptionResponses) {
        final StringBuilder msg = new StringBuilder();
        
        if (exceptionResponses != null && exceptionResponses.size() > 0) {
            msg.append("The following request(s) resulted in an exception:\n");
            
            for (Map.Entry<Address, NodeResponse> exceptionResponse : exceptionResponses.entrySet()) {
                final Address node = exceptionResponse.getKey();
                final NodeResponse response = exceptionResponse.getValue();
                
                msg.append("Request on node [")
                   .append(node);
                
                // The exception response may or may not have set a value, namely an exception
                final Object exception = response.getValue();
                if (exception instanceof Exception) {
                    msg.append("] resulted in an exception: ")
                    .append(((Exception)exception).getMessage());
                }
                else {
                    msg.append("] resulted in an exception. ");
                }
            }
        }
        
        return msg.toString();
    }

    /*
     * Get an exception message from Node responses that returned a map of request IDs to NodeResponses
     */
    private static String getFailedRequestsMesssageMap(Map<Address, Map<String, NodeResponse>> exceptionResponses) {
        final StringBuilder msg = new StringBuilder();
        
        if (exceptionResponses != null && exceptionResponses.size() > 0) {
            msg.append("The following request(s) resulted in an exception:\n");
            
            for (Map.Entry<Address, Map<String, NodeResponse>> exceptionResponse : exceptionResponses.entrySet()) {
                final Address node = exceptionResponse.getKey();
                final Map<String, NodeResponse> response = exceptionResponse.getValue();
                
                for (Map.Entry<String, NodeResponse> request : response.entrySet()) {
                    msg.append("Request [")
                        .append(request.getKey())
                        .append("] on node [")
                        .append(node);
                    
                    // The exception response may or may not have set a value, namely an exception
                    final Object exception = request.getValue().getValue();
                    if (exception instanceof Exception) {
                        msg.append("] resulted in an exception: ")
                        .append(((Exception)exception).getMessage());
                    }
                    else {
                        msg.append("] resulted in an exception. \n");
                    }
                }
            }
        }
        
        return msg.toString();
    }

    private static final long serialVersionUID = 5240799988955601912L;
}
