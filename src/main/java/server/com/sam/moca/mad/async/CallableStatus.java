/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.sam.moca.mad.async;

/**
 * The status of a callable being executed asynchronously.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
public class CallableStatus {
    public CallableStatus(String callableString, long threadId, Status status) {
        _callableString = callableString;
        _threadId = threadId;
        _status = status;
    }
    
    public String getCallableString() {
        return _callableString;
    }
    
    public long getThreadId() {
        return _threadId;
    }
    
    public Status getStatus() {
        return _status;
    }

    private final String _callableString;
    private final long _threadId;
    private final Status _status;
}