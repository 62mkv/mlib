/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.web.console;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.server.db.DBAdapter;
import com.redprairie.moca.server.db.jdbc.ConnectionStatistics;
import com.redprairie.moca.server.exec.UnimplementedOperationException;
import com.redprairie.moca.server.profile.CommandPath;
import com.redprairie.util.Pair;

/**
 * Get information regarding the current database connections in the pool.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author mlange
 */
public class DatabaseConnectionInformation {
    
    public DatabaseConnectionInformation(DBAdapter dbAdapter) throws ConsoleException {
        // Ensure that we were given a db adapter.
        if (dbAdapter == null) {
            throw new ConsoleException("A valid database adapter was not provided.  Please report this as a bug.");
        }
        
        _dbAdapter = dbAdapter;
    }
    
    public MocaResults getDatabaseConnections() throws ConsoleException { 
        EditableResults res = new SimpleResults();
        
        res.addColumn("id", MocaType.STRING);
        res.addColumn("thread_id", MocaType.STRING);    
        res.addColumn("last_sql_dt", MocaType.DATETIME);
        res.addColumn("last_sql", MocaType.STRING);
        res.addColumn("executions", MocaType.INTEGER);
        res.addColumn("command_path", MocaType.STRING);
     
        // Get the current connection statistics.
        try {
            Map<Connection, ConnectionStatistics> allConnStats = 
                _dbAdapter.getConnectionStatistics();
        
            for (ConnectionStatistics connStats : allConnStats.values()) {
                String id = connStats.getConnectionIdentifier();
    
                Thread thread = connStats.getThread();
                String threadId = (thread != null) ? Long.toString(thread.getId()) : null;
                
                Pair<String,Date> pair = connStats.getLastSQL();
                String lastSql = pair.getFirst();
                Date lastSqlDatetime = pair.getSecond();
                
                Integer count = connStats.getSQLStatementCount();
                int executions = (count != null) ? count : 0;
                
                res.addRow();
                res.setStringValue(0, id);
                res.setStringValue(1, threadId);  
                res.setDateValue(2, lastSqlDatetime);
                res.setStringValue(3, lastSql);
                res.setIntValue(4, executions);
                CommandPath path = connStats.getLastCommandPath();
                if (path != null) {
                    res.setStringValue(5, path.toString());
                }
            }
        }
        catch (UnimplementedOperationException e) {
            throw new ConsoleException("The server is not currently configured to connect to a database.");
        }
        
        return res;
    }

    private final DBAdapter _dbAdapter;
}
