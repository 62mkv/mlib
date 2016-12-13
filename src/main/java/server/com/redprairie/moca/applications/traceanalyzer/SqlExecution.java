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
 * Represents a SQL Execution
 */
public class SqlExecution extends Execution {

    SqlExecution(String sql, TraceLine startLine, Execution parentExecution) {
        super(startLine, parentExecution);
        sqlStatement = sql;
    }
    
    public String getCallingCommand() {
        Execution parent = getParentExecution();
        if (parent instanceof CommandExecution) {
            return ((CommandExecution)parent).getCommand();
        }
        else {
            return "<none>";
        }
            
    }

    @Override
    public String getExecutionType() {
        return "SQL";
    }

    @Override
    public String getBeginLineDetails() {
        return String.format("Total time: %s - SQL statement - %s", TraceAnalyzer.formatTime(getExecutionTime()),
            sqlStatement.substring(0, sqlStatement.length() > 100 ? 100 : sqlStatement.length()));
    }

    @Override
    public String getEndLineDetails() {
        return null;
    }
    
    private final String sqlStatement;
}
