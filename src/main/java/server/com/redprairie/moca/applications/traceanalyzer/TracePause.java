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

package com.redprairie.moca.applications.traceanalyzer;

/**
 * An execution that indicates a pause in the trace.
 */
public abstract class TracePause extends Execution {

    TracePause(TraceLine startLine, Execution parentExecution) {
        super(startLine, parentExecution);
    }
    
    @Override
    public String getBeginLineDetails() {
        return getStartLine().toString();
    }

    @Override
    public String getEndLineDetails() {
        return getEndLine().toString();
    }

}
