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

package com.sam.moca.cache.infinispan.extension.commands.visitable;

import java.io.Serializable;

/**
 * Response of a node to a cluster RPC call.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author mdobrinin
 */
public class NodeResponse implements Serializable {
    
    /*
     * Constructor for testing
     */
    NodeResponse(Status status, Object value) {
        _status = status;
        _value = value;
    }
    
    /**
     * Return successful response with a value. Value
     * should be used for a return value, such as when information
     * is needed from the node rather than request tracking.
     * @param value return value
     * @return A successful response with a value
     */
    public static NodeResponse sucessfulResponse(Object value) {
        return new NodeResponse(Status.SUCCESS, value);
    }
    
    /**
     * Return empty successful response.
     * @return empty successful response
     */
    public static NodeResponse sucessfulEmptyResponse() {
        return SUCCESSFULEMPTYINSTANCE;
    }
    
    /**
     * Return empty no action response.
     * @return empty no action response
     */
    public static NodeResponse noActionEmptyResponse() {
        return NOACTIONEMPTYINSTANCE;
    }
    
    /**
     * Return an exception response with a saved exception.
     * @param e exception
     * @return exception response with a saved exception
     */
    public static NodeResponse exceptionResponse(Exception e) {
        return new NodeResponse(Status.EXCEPTION, e);
    }
    
    /**
     * Get the value from this response.
     * @return value from this response
     */
    public Object getValue() {
        return _value;
    }
    
    /**
     * Get the Response.
     */
    public Status getStatus() {
        return _status;
    }
    
    /**
     * Return empty exception response.
     * @return empty exception response
     */
    public static NodeResponse exceptionEmptyResponse() {
        return EXCEPTIONEMPTYINSTANCE;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_status == null) ? 0 : _status.hashCode());
        result = prime * result + ((_value == null) ? 0 : _value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        NodeResponse other = (NodeResponse) obj;
        if (_status != other._status) return false;
        if (_value == null) {
            if (other._value != null) return false;
        }
        else if (!_value.equals(other._value)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "[" + _status +
                (_value == null ? 
                    "]" : 
                    ("|" + _value + "]"));
    }

    /**
     * The status type of the cluster RPC call response.
     * 
     * @author mdobrinin
     */
    public static enum Status {
        /**
         * Indicates no action was taken on the node. This status is also considered
         * for failures when no actions succeeded for a particular request on any other nodes.
         */
        NO_ACTION,
        
        /**
         * Indicates the requested action was successful.
         */
        SUCCESS,
        
        /**
         * Indicates an exception occurred on the node when processing the request.
         */
        EXCEPTION;

        /**
         * Convenience method to check if this response is successful.
         * @return
         */
        public boolean isSuccessful() {
            return this == SUCCESS;
        }
        
        /**
         * Convenience method to check if this response is no action.
         * @return
         */
        public boolean isNoAction() {
            return this == NO_ACTION;
        }
        
        /**
         * Convenience method to check if this response is an exception.
         * @return
         */
        public boolean isException() {
            return this == EXCEPTION;
        }
    }
    
    private Object _value = null;
    private final Status _status;
    
    private static final NodeResponse NOACTIONEMPTYINSTANCE = new NodeResponse(Status.NO_ACTION, null);
    private static final NodeResponse EXCEPTIONEMPTYINSTANCE = new NodeResponse(Status.EXCEPTION, null);
    private static final NodeResponse SUCCESSFULEMPTYINSTANCE = new NodeResponse(Status.SUCCESS, null);
    
    private static final long serialVersionUID = -1675557950589853971L;
}
