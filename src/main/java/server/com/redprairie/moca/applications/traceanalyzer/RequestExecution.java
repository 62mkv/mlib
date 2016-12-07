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

package com.redprairie.moca.applications.traceanalyzer;

/**
 * Execution that represents a request made from a client to
 * the /service endpoint to execute a command/sql
 */
public class RequestExecution extends CommandExecution {
    
    RequestExecution(String command, TraceLine startLine, Execution parentExecution) {
        super(command, startLine, parentExecution);
    }

    @Override
    public String getExecutionType() {
        return "REQUEST";
    }
}
