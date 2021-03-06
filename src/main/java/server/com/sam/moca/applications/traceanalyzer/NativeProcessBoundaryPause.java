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
 * Execution that represents going between the main JVM and
 * native process boundary.
 * 
 * @author rrupp
 */
public class NativeProcessBoundaryPause extends TracePause {

    NativeProcessBoundaryPause(TraceLine startLine, Execution parentExecution) {
        super(startLine, parentExecution);
    }

    @Override
    public String getExecutionType() {
        return "Native Process->JVM boundary";
    }

}
