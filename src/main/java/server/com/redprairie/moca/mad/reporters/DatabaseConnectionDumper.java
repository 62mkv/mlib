/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.mad.reporters;

import com.redprairie.moca.MocaResults;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.db.DBAdapter;
import com.redprairie.moca.web.console.ConsoleException;
import com.redprairie.moca.web.console.DatabaseConnectionInformation;

/**
 * Utility class to capture current database
 * connection information.
 * 
 * Copyright (c) 2012 RedPrairie Corporation All Rights Reserved
 * 
 * @author rrupp
 */
public class DatabaseConnectionDumper {
    
    /**
     * Dumps the current database connections as a string report
     * @return A report on the current database connections
     */
    public static String dumpDatabaseConnectionInformation() {
        StringBuilder output = new StringBuilder();
        DBAdapter dbAdapter = ServerUtils.globalAttribute(ServerContextFactory.class)
                                         .getDBAdapter();
        try {
            DatabaseConnectionInformation databaseConnectionInformation = new DatabaseConnectionInformation(
                dbAdapter);
            MocaResults res = databaseConnectionInformation
                .getDatabaseConnections();
            
            // Summary information
            output.append("Total number of database connections: ")
                .append(res.getRowCount()).append("\n\n");
            
            // Detail information
            while (res.next()) {
                for (int i = 0; i < res.getColumnCount(); i++) {
                    String value = res.getString(i);
                    output.append(res.getColumnName(i)).append(": ")
                        .append(value != null ? value : "").append("\n");
                }
                output.append("\n");
            }
        }
        catch (ConsoleException e) {
            return "Unable to dump database connection information:" + e;
        }

        return output.toString();
    }
}
