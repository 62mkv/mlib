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

package com.redprairie.moca.server.log;

import org.joda.time.DateTime;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.server.exec.DefaultServerContext;

/**
 * A class to hold general MOCA tracing methods.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class MocaTraceMessaging {
    
    /***
     * A method that will log library version information.
     * @param MocaContext
     * @see MocaContext
     */
     public static void logLibraryVersions(MocaContext moca){
        DateTime date = new DateTime();
        moca.logInfo("Tracing started " + date.toString("MM/dd/yyyy") + 
                " at " + date.toString("HH:mm:ss"));
        
        String regFile = System.getProperty("com.redprairie.moca.config");
        if (regFile == null) {
            regFile = System.getenv("MOCA_REGISTRY");
        }
        
        moca.logInfo("");
        moca.logInfo("   Registry: " + (regFile != null ? regFile : "Not set"));
        // TODO the old version output host, port
        moca.logInfo("");
        moca.logInfo("Category        Library         Version");
        moca.logInfo("--------------- --------------- -------------------------");
        
        MocaResults results = DefaultServerContext.listLibraryVersions(
                moca, null);
        
        RowIterator rowIter = results.getRows();
        
        // Now we want to output the component library versions
        while (rowIter.next()) {
            String category = rowIter.getString("category");
            String library = rowIter.getString("library_name");
            String version = rowIter.getString("version");
            
            /* Log the library information to the new trace file. */
            moca.logInfo(String.format("%-15.15s %-15.15s %-25.25s",
                    category != null ? category : "",
                    library != null ? library : "",
                    version != null ? version : ""));

        }
    }
}
