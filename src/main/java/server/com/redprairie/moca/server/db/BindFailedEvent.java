/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.db;

import com.redprairie.moca.server.profile.CommandPath;

/**
 * Used to log the event of a bind failure (and success via retry).
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class BindFailedEvent {
    
    public BindFailedEvent(String sql, CommandPath commandPath, int errorCode, String sqlState, String errorMessage) {
        _sql = sql;
        _commandPath = commandPath;
        _errorCode = errorCode;
        _sqlState = sqlState == null ? "" : sqlState;
        _errorMessage = errorMessage == null ? "" : errorMessage;
    }
    
    public String getSql() {
        return _sql;
    }
    
    public CommandPath getCommandPath() {
        return _commandPath;
    }
    
    public int getErrorCode() {
        return _errorCode;
    }
    
    public String getErrorMessage() {
        return _errorMessage;
    }

    public String getSqlState() {
        return _sqlState;
    }
    
    // @see java.lang.Object#toString()
    
    @Override
    public String toString() {
        char newLine = '\n';
        StringBuilder out = new StringBuilder();
        out.append("SQL:        ").append(_sql.trim()).append(newLine);
        out.append("Status:     ").append(_errorCode).append(newLine);
        out.append("SQLState:   ").append(_sqlState.trim()).append(newLine);
        out.append("Message:    ").append(_errorMessage.trim()).append(newLine);
        out.append("CommandPath:").append(_commandPath);
        return out.toString();
    }

    private final String _sql;
    private final CommandPath _commandPath;
    private final int _errorCode;
    private final String _errorMessage;
    private final String _sqlState;
}
