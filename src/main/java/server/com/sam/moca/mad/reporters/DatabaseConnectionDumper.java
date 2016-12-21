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

package com.sam.moca.mad.reporters;

import com.sam.moca.MocaResults;
import com.sam.moca.server.ServerContextFactory;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.db.DBAdapter;
import com.sam.moca.web.console.ConsoleException;
import com.sam.moca.web.console.DatabaseConnectionInformation;

/**
 * Utility class to capture current database
 * connection information.
 * 
 * Copyright (c) 2012 Sam Corporation All Rights Reserved
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
