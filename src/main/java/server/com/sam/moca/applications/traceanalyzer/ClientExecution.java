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

package com.sam.moca.applications.traceanalyzer;

/**
 * Represents an execution gap from a trace file where 
 * time is spent inside of the client/network or waiting
 * on user interaction which we don't have visibility on from
 * the server side trace.
 */
public class ClientExecution extends Execution {

    ClientExecution(TraceLine startLine, TraceLine endLine, Execution parentExecution) {
        super(startLine, parentExecution);
        this.done(endLine);
    }

    @Override
    public String getExecutionType() {
        return "CLIENT/NETWORK/USER INTERACTION";
    }

    @Override
    public String getBeginLineDetails() {
        return getStartLine().getFullLine();
    }

    @Override
    public String getEndLineDetails() {
        return getEndLine().getFullLine();
    }
}
